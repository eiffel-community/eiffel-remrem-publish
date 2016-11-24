package com.ericsson.eiffel.remrem.publish.listener;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * A simple class which can monitor files and notify interested parties
 * (i.e. listeners) of file changes.
 *
 * This class is kept lean by only keeping methods that are actually being
 * called.
 * 
 * @author esantnc
 */

public class SimpleJarDirectoryWatchService implements DirectoryWatchService, Runnable {

    private final WatchService mWatchService;
    private final AtomicBoolean mIsRunning;
    private final ConcurrentMap<WatchKey, Path> mWatchKeyToDirPathMap;
    private final ConcurrentMap<Path, Set<OnFileChangeListener>> mDirPathToListenersMap;
    private final ConcurrentMap<OnFileChangeListener, Set<PathMatcher>> mListenerToFilePatternsMap;

    /**
     * A simple no argument constructor for creating a <code>SimpleDirectoryWatchService</code>.
     *
     * @throws IOException If an I/O error occurs.
     */
    public SimpleJarDirectoryWatchService() throws IOException {
        mWatchService = FileSystems.getDefault().newWatchService();
        mIsRunning = new AtomicBoolean(false);
        mWatchKeyToDirPathMap = newConcurrentMap();
        mDirPathToListenersMap = newConcurrentMap();
        mListenerToFilePatternsMap = newConcurrentMap();
    }

    @SuppressWarnings("unchecked")
    private final static <T> WatchEvent<T> cast(final WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    private static <K, V> ConcurrentMap<K, V> newConcurrentMap() {
        return new ConcurrentHashMap<>();
    }

    private static <T> Set<T> newConcurrentSet() {
        return Collections.newSetFromMap(newConcurrentMap());
    }

    public final static PathMatcher matcherForGlobExpression(final String globPattern) {
        return FileSystems.getDefault().getPathMatcher("glob:" + globPattern);
    }

    public final static boolean matches(final Path input, final PathMatcher pattern) {
        return pattern.matches(input);
    }

    public final static boolean matchesAny(final Path input, final Set<PathMatcher> patterns) {
        for (final PathMatcher pattern : patterns) {
            if (matches(input, pattern)) {
                return true;
            }
        }

        return false;
    }

    private final Path getDirPath( final WatchKey key) {
        return mWatchKeyToDirPathMap.get(key);
    }

    private final Set<OnFileChangeListener> getListeners( final Path dir) {
        return mDirPathToListenersMap.get(dir);
    }

    private final Set<PathMatcher> getPatterns(final OnFileChangeListener listener) {
        return mListenerToFilePatternsMap.get(listener);
    }

    private final Set<OnFileChangeListener> matchedListeners(final Path dir, final Path file) {
        return getListeners(dir)
                .stream()
                .filter(listener -> matchesAny(file, getPatterns(listener)))
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("rawtypes")
    private final void notifyListeners(final WatchKey key) {
        for (final WatchEvent<?> event : key.pollEvents()) {
            final WatchEvent.Kind eventKind = event.kind();

            // Overflow occurs when the watch event queue is overflown
            // with events.
            if (eventKind.equals(OVERFLOW)) {
                // TODO: Notify all listeners.
                return;
            }

            final WatchEvent<Path> pathEvent = cast(event);
            final Path file = pathEvent.context();

            if (eventKind.equals(ENTRY_CREATE)) {
                matchedListeners(getDirPath(key), file)
                        .forEach(listener -> listener.onFileCreate(file.toString()));
            } else if (eventKind.equals(ENTRY_MODIFY)) {
                matchedListeners(getDirPath(key), file)
                        .forEach(listener -> listener.onFileModify(file.toString()));
            } else if (eventKind.equals(ENTRY_DELETE)) {
                matchedListeners(getDirPath(key), file)
                        .forEach(listener -> listener.onFileDelete(file.toString()));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void register(final OnFileChangeListener listener,final String dirPath,final String... globPatterns)
            throws IOException {
        final Path dir = Paths.get(dirPath);

        if (!Files.isDirectory(dir)) {
            throw new IllegalArgumentException(dirPath + " is not a directory.");
        }

        if (!mDirPathToListenersMap.containsKey(dir)) {
            // May throw
            final WatchKey key = dir.register(
                    mWatchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE
            );

            mWatchKeyToDirPathMap.put(key, dir);
            mDirPathToListenersMap.put(dir, newConcurrentSet());
        }

        getListeners(dir).add(listener);

        final Set<PathMatcher> patterns = newConcurrentSet();

        for (final String globPattern : globPatterns) {
            patterns.add(matcherForGlobExpression(globPattern));
        }

        if (patterns.isEmpty()) {
            patterns.add(matcherForGlobExpression("*")); // Match everything if no filter is found
        }

        mListenerToFilePatternsMap.put(listener, patterns);

    }

    /**
     * Start this <code>SimpleDirectoryWatchService</code> instance by spawning a new thread.
     *
     * @see #stop()
     */
    @Override
    public void start() {
        if (mIsRunning.compareAndSet(false, true)) {
            final Thread runnerThread = new Thread(this, DirectoryWatchService.class.getSimpleName());
            runnerThread.start();
        }
    }

    /**
     * Stop this <code>SimpleDirectoryWatchService</code> thread.
     * The killing happens lazily, giving the running thread an opportunity
     * to finish the work at hand.
     *
     * @see #start()
     */
    @Override
    public void stop() {
        // Kill thread lazily
        mIsRunning.set(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {

        while (mIsRunning.get()) {
            WatchKey key;
            try {
                key = mWatchService.take();
            } catch (InterruptedException e) {
                break;
            }

            if (null == getDirPath(key)) {
                continue;
            }

            notifyListeners(key);

            // Reset key to allow further events for this key to be processed.
            final boolean valid = key.reset();
            if (!valid) {
                mWatchKeyToDirPathMap.remove(key);
                if (mWatchKeyToDirPathMap.isEmpty()) {
                    break;
                }
            }
        }

        mIsRunning.set(false);
    }
}