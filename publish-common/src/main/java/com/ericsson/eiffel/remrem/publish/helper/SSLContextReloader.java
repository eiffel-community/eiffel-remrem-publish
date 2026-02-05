package com.ericsson.eiffel.remrem.publish.helper;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.locks.StampedLock;

import javax.management.*;

import static java.lang.Thread.sleep;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Spring component that automatically reloads SSL certificates without requiring application restart.
 * <p>
 * Monitors keystore and truststore files for changes and gracefully reloads the SSL context
 * by restarting the Tomcat HTTPS connector when certificate updates are detected.
 * </p>
 *
 * <h2>Required System Properties</h2>
 * <ul>
 *   <li>javax.net.ssl.keyStore - path to the keystore file</li>
 *   <li>javax.net.ssl.keyStorePassword - keystore password</li>
 *   <li>javax.net.ssl.keyStoreType - keystore type (e.g., JKS, PKCS12)</li>
 *   <li>javax.net.ssl.trustStore - path to the truststore file</li>
 *   <li>javax.net.ssl.trustStorePassword - truststore password</li>
 *   <li>javax.net.ssl.trustStoreType - truststore type</li>
 * </ul>
 *
 * <h2>Tomcat Configuration Requirement</h2>
 * The HTTPS connector must be configured with {@code bindOnInit="false"} to enable
 * certificate reloading without full server restart.
 *
 * <h2>Reload Process</h2>
 * <ol>
 *   <li>Pause HTTPS connector to prevent new connections</li>
 *   <li>Wait for existing connections to close (with timeout)</li>
 *   <li>Stop HTTPS connector</li>
 *   <li>Reload certificates and create new SSL context</li>
 *   <li>Restart HTTPS connector with new certificates</li>
 * </ol>
 *
 * <h2>Calmness Period</h2>
 * Uses a 30-second calmness interval to avoid frequent reloads when certificate files
 * are updated multiple times in quick succession.
 *
 * @see SSLContextReloadListener
 */
@Component
public class SSLContextReloader {
    private Logger log = (Logger) LoggerFactory.getLogger(SSLContextReloader.class);

    /**
     * Helper class to keep information about a keystore/truststore file.
     */
    class StoreInfo {
        Path path;
        char[] password;
        String type;
        byte[] hash;
        long lastModified;

        /**
         * Constructs a StoreInfo by reading system properties.
         *
         * @param propertyBaseName the base name of the system property (e.g., "javax.net.ssl.keyStore")
         * @throws SystemPropertyNotFoundException if required system properties are not set
         */
        StoreInfo(String propertyBaseName) throws SystemPropertyNotFoundException {
            path = Path.of(getSystemProperty(propertyBaseName));
            password = getSystemProperty(propertyBaseName + "Password").toCharArray();
            type = getSystemProperty(propertyBaseName + "Type");
        }

        /**
         * Retrieves a system property value.
         *
         * @param name the system property name
         * @return the system property value
         * @throws SystemPropertyNotFoundException if the property is not set
         */
        private String getSystemProperty(@Nonnull String name) throws SystemPropertyNotFoundException {
            String value = System.getProperty(name);
            if (value == null)
                throw new SystemPropertyNotFoundException(name);

            return value;
        }

    }

    // TODO Should it be fetched from a configuration? It seems we have disabled protocols, not used one(s).
    public static final String PROTOCOL = "TLS";

    // Algorithm used to calculate hash of a store file.
    public static final String HASH_ALGORITHM = "MD5";

    private StoreInfo keyStore;
    private StoreInfo trustStore;
    private StoreInfo storesToWatch[];

    // Indicates whether certificate has been changed and reload is needed.
    private boolean certificateChanged = false;

    // Context created after reload of certificates.
    private SSLContext sslContext;

    // Listeners can be hooked to be notified about certificate change(s).
    private List<SSLContextReloadListener> listeners = new ArrayList<>();

    @Autowired
    Environment environment;
    private int httpsPort;

    /**
     * Initializes the SSL context reloader by loading keystore and truststore information
     * from system properties and configuring the HTTPS port.
     *
     * @return true if initialization succeeds, false otherwise
     */
    private boolean initialize() {
        try {
            // Initialize key/trust stores from standard system properties.
            keyStore = new StoreInfo("javax.net.ssl.keyStore");
            trustStore = new StoreInfo("javax.net.ssl.trustStore");
            storesToWatch = new StoreInfo[]{ keyStore, trustStore };

            String port = environment.getProperty("server.port", "8443");
            this.httpsPort = Integer.parseInt(port);

            log.info("SSLContextReloader initiated");

            return true;
        } catch (SystemPropertyNotFoundException | NumberFormatException e) {
            // Something went wrong report the issue.
            log.error("Cannot initiate SSL context reloader: {}", e.getMessage(), e);
            log.error("Certificate reload will not work!");

            return false;
        }
    }

    /**
     * Adds a listener to be notified when SSL context reload events occur.
     * The listener will receive callbacks when the SSL context is about to be reloaded
     * and when the reload process is completed.
     *
     * @param listener the SSLContextReloadListener to add
     */
    public void addListener(SSLContextReloadListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a previously registered SSL context reload listener.
     * The listener will no longer receive notifications about SSL context reload events.
     *
     * @param listener the SSLContextReloadListener to remove
     */
    public void removeListener(SSLContextReloadListener listener) {
        listeners.remove(listener);
    }

    public static final int OBSERVE_CERTIFICATE_RETRIES = 10;
    public static final int OBSERVE_CERTIFICATE_RETRY_WAIT_TIME = 1; // seconds
    private Thread observeCertificatesThread;

    /**
     * Starts the certificate monitoring thread after bean construction.
     * This method is automatically called by Spring after dependency injection.
     */
    @PostConstruct
    public void start() {
        if (!initialize()) {
            log.warn("Certificate on-the-fly reload not supported " +
                    "as some properties were not initialized; see former error(s)");
            return;
        }

        // Add certificate file observation. Allocate a new, independent thread for this.
        observeCertificatesThread = new Thread(() -> {
            int maxRetries = OBSERVE_CERTIFICATE_RETRIES;
            int retry = 1;
            while (retry++ <= maxRetries) {
                try {
                    observeCertificates();
                } catch (IOException e) {
                    log.error("Error occurred while observing certificate changes: {} ", e.getMessage(), e);
                    // break;
                } catch (GeneralSecurityException e) {
                    log.error("Error occurred while reloading certificate(s): {} ", e.getMessage(), e);
                } catch (InterruptedException e) {
                    log.error("Thread observing certificate changes has been terminated: {}", e.getMessage(), e);
                }

                try {
                    // Wait
                    Thread.sleep(OBSERVE_CERTIFICATE_RETRY_WAIT_TIME * 1000);
                } catch (InterruptedException e) {
                    // Ignore
                }
            }
        }, "Certificate watcher");

        // Don't prevent JVM from exiting.
        observeCertificatesThread.setDaemon(true);
        observeCertificatesThread.start();
    }

    /**
     * Performs the SSL certificate reload process, including reloading the SSL context
     * and resetting modification timestamps.
     */
    private void doReloadSSLCertificates() {
        try {
            log.info("Going to reload '{}' context...", PROTOCOL);
            // Just (re)load certificates and prepare a new SSLContext.
            reloadSSLContext();

            certificateChanged = false;

            // It's just after certificate reload. Remove modification times of stores
            // to indicate that they're fresh.
            for (StoreInfo store : storesToWatch) {
                store.lastModified = 0;
            }

            log.info("Reload of {} context done", PROTOCOL);
//            log.info(caCert);
        } catch (GeneralSecurityException | IOException | InterruptedException |
                 ReflectionException | MalformedObjectNameException | AttributeNotFoundException |
                 InstanceNotFoundException | MBeanException e) {
            log.error("Certificate reload failed: {}", e.getMessage(), e);
        }

    }

    /**
     * Registers a directory for file system monitoring.
     *
     * @param watcher the WatchService to register with
     * @param store the store information containing the file path
     * @throws IOException if the directory cannot be determined or registered
     */
    private void registerDir(WatchService watcher, @Nonnull StoreInfo store) throws IOException {
        Path dir = store.path.getParent();
        if (dir == null) {
            throw new IOException("Cannot determine directory of file '" + store.path + "'");
        }

        dir.register(watcher, ENTRY_MODIFY);
        log.info("Watching directory '{}'", dir);
    }

    /**
     * Checks if any store file is waiting for modification.
     *
     * @return true if any store has lastModified set to 0, false otherwise
     */
    private boolean isWaitingForStoreModification() {
        for (StoreInfo store : storesToWatch) {
            if (store.lastModified == 0) {
                log.info("Waiting for modification of '{}' ...", store.path);
                return true;
            }
        }

        return false;
    }

    /**
     * Monitors certificate files for changes using the file system WatchService.
     * When changes are detected, triggers the certificate reload process.
     *
     * @throws IOException if an I/O error occurs
     * @throws GeneralSecurityException if a security error occurs
     * @throws InterruptedException if the thread is interrupted
     */
    private void observeCertificates() throws IOException, GeneralSecurityException, InterruptedException {
        try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
            for (StoreInfo info : storesToWatch) {
                registerDir(watcher, info);
            }

            while (true) {
                // Wait for a file system event
                WatchKey key = watcher.take();

                boolean reloadCertificates = false;
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    // The event context is the relative Path to the watched directory
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;

                    Path file = ev.context();
                    Path dir = (Path) key.watchable();
                    Path fullPath = dir.resolve(file);

                    for (StoreInfo info : storesToWatch) {
                        if (fullPath.equals(info.path) && Files.exists(info.path)) {
                            byte[] hash = calculateFileHash(info.path);
                            if (info.hash == null || !MessageDigest.isEqual(info.hash, hash)) {
                                // File really modified.
                                log.info("File '{}' modified (count: {})", info.path, event.count());
                                // Don't use real modification time of a file. It might be in a past and causes
                                // some issues.
                                info.lastModified = System.currentTimeMillis();

                                // Don't start certificate reload until both keystore and truststore are modified.
                                if (!isWaitingForStoreModification()) {
                                    log.info("{}tarting certificate reload timer", reloadTimer != null ? "Re-s" : "S");
                                    certificateChanged = true;
                                    restartReloadTimer(info.lastModified);
                                }
                            }
                        }
                        else {
                            // Nothing to do; only a file attribute has been modified.
                        }
                    }
                }

                // Prepare for next modification(s)
                key.reset();
            }
        }
    }

//    private static MBeanServer getMBeanServer() {
//        // Try to find a platform MBeanServer first.
//        try {
//            MBeanServer platform = java.lang.management.ManagementFactory.getPlatformMBeanServer();
//            if (platform != null)
//                return platform;
//        } catch (Exception e) {
//            // Ignore.
//        }
//
//        // Fall back to MBeanServerFactory list.
//        try {
//            List<MBeanServer> servers = MBeanServerFactory.findMBeanServer(null);
//            if (servers != null && !servers.isEmpty()) {
//                // Prefer the first
//                return servers.get(0);
//            }
//        } catch (Exception ignored) {}
//
//        return null;
//    }

    /**
     * Finds the ThreadPool MBean for the specified port.
     *
     * @param mbs the MBeanServer to search
     * @param port the port number
     * @return the ObjectName of the ThreadPool MBean, or null if not found
     */
    private static ObjectName findThreadPoolMBean(MBeanServer mbs, int port) {
        // Common names used by Tomcat: ThreadPool,name="https-nio-8443" or "http-nio-8443", etc.
        String[] candidates = new String[] {
                String.format("Catalina:type=ThreadPool,name=\"https-nio-%d\"", port),
                String.format("Catalina:type=ThreadPool,name=\"https-nio2-%d\"", port),
                String.format("Catalina:type=ThreadPool,name=\"http-nio-%d\"", port),
                String.format("Catalina:type=ThreadPool,name=\"http-nio2-%d\"", port)
        };

        for (String s : candidates) {
            try {
                ObjectName on = new ObjectName(s);
                if (mbs.isRegistered(on)) return on;
            } catch (MalformedObjectNameException e) {
                // ignore and continue
            }
        }

        // Fallback: query all ThreadPool MBeans and find one whose name contains the port
        Set<ObjectName> names;
        try {
            names = mbs.queryNames(new ObjectName("Catalina:type=ThreadPool,*"), null);
        } catch (MalformedObjectNameException e) {
            return null;
        }

        for (ObjectName on : names) {
            String nameProp = on.getKeyProperty("name");
            if (nameProp != null && nameProp.contains(String.valueOf(port))) {
                return on;
            }
            // Some Tomcat setups put the port in the object name differently; check canonical name too
            if (on.toString().contains(String.valueOf(port))) {
                return on;
            }
        }

        return null;
    }

    /**
     * Safely parses an object to an integer.
     *
     * @param o the object to parse
     * @return the integer value, or -1 if parsing fails
     */
    private static int parseInt(Object o) {
        if (o instanceof Number) return ((Number) o).intValue();
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (Exception e) { return -1; }
    }

    /**
     * Pauses the HTTPS connector to prevent accepting new connections.
     *
     * @throws MalformedObjectNameException if the object name is malformed
     * @throws ReflectionException if an error occurs during reflection
     * @throws InstanceNotFoundException if the MBean instance is not found
     * @throws MBeanException if an error occurs in the MBean
     */
    public void pauseHttpsConnector() throws MalformedObjectNameException, ReflectionException, InstanceNotFoundException, MBeanException {
        String protocolHandlerName = "Catalina:type=ProtocolHandler,port=" + httpsPort;
        final ObjectName objectNameQuery = new ObjectName(protocolHandlerName);
        for (MBeanServer server :  MBeanServerFactory.findMBeanServer(null)) {
            for (ObjectName objectName : server.queryNames(objectNameQuery, null)) {
                log.info("Going to pause HTTPS connector '{}' not to accepts new connections...", objectName);
                server.invoke(objectName, "pause", null, null);
            }
        }
    }

    private static long POLL_INTERVAL_MS = 500;
    private static long TIMEOUT_MS = 60_000;

    /**
     * Waits until all active HTTPS connections are closed or timeout is reached.
     *
     * @throws MalformedObjectNameException if the object name is malformed
     * @throws ReflectionException if an error occurs during reflection
     * @throws AttributeNotFoundException if the attribute is not found
     * @throws InstanceNotFoundException if the MBean instance is not found
     * @throws MBeanException if an error occurs in the MBean
     * @throws InterruptedException if the thread is interrupted
     */
    private void waitUntilHttpsConnectionsClosed() throws MalformedObjectNameException, ReflectionException, AttributeNotFoundException, InstanceNotFoundException, MBeanException, InterruptedException {
        // Wait until all existing connection are closed.
        for (MBeanServer server :  MBeanServerFactory.findMBeanServer(null)) {
            ObjectName threadPool = findThreadPoolMBean(server, httpsPort);
            if (threadPool != null) {
                long start = System.currentTimeMillis();
                while (true) {
                    Object busyAttr = server.getAttribute(threadPool, "currentThreadsBusy");
                    int busy = parseInt(busyAttr);
                    if (busy <= 0) {
                        log.info("No active connection on port {}", httpsPort);
                        break;
                    }
                    log.debug("Busy threads: {}", busy);

                    long now = System.currentTimeMillis();
                    if (now - start > TIMEOUT_MS) {
                        log.warn("There is still {} active connections on port {}", busy, httpsPort);
                        log.warn("Timeout {}sec exceeded; connector on port {} will be restarted", TIMEOUT_MS, httpsPort);
                        break;
                    }

                    Thread.sleep(POLL_INTERVAL_MS);
                }
            }
        }
    }

    /**
     * Stops the HTTPS connector and waits until it reaches STOPPED state.
     * This will work only if connector is configured with bindOnInit="false" option.
     *
     * @throws MalformedObjectNameException if the object name is malformed
     * @throws ReflectionException if an error occurs during reflection
     * @throws InstanceNotFoundException if the MBean instance is not found
     * @throws MBeanException if an error occurs in the MBean
     * @throws InterruptedException if the thread is interrupted
     * @throws AttributeNotFoundException if the attribute is not found
     * @see <a href="https://serverfault.com/questions/328533/can-tomcat-reload-its-ssl-certificate-without-being-restarted">Tomcat SSL reload</a>
     */
    public void stopHttpsConnector() throws MalformedObjectNameException, ReflectionException, InstanceNotFoundException, MBeanException, InterruptedException, AttributeNotFoundException {
        String objectString = "*:type=Connector,port=" + httpsPort + ",*";
        ObjectName httpsConnectorName = new ObjectName(objectString);
        for (MBeanServer server: MBeanServerFactory.findMBeanServer(null)) {
            for (ObjectName objectName : server.queryNames(httpsConnectorName, null)) {
                log.info("Going to stop HTTPS connector '{}'...", objectName);
                server.invoke(objectName, "stop", null, null);
                log.info("Connector '{}' stopped", objectName);

                long start = System.currentTimeMillis();
                while (true) {
                    Object stateName = server.getAttribute(objectName, "stateName");
                    String state = stateName == null ? null : stateName.toString();
                    log.debug("State of connector '{}' is '{}'", objectName, stateName);
                    if ("STOPPED".equalsIgnoreCase(state)) {
                        break;
                    }

                    long now = System.currentTimeMillis();
                    if (now - start > TIMEOUT_MS) {
                        log.error("Waiting for connector '{}' to stop timed out ({} seconds)", objectName, TIMEOUT_MS);
                        break;
                    }

                    Thread.sleep(POLL_INTERVAL_MS);
                }

//                    // Polling sleep to reduce delay to safe minimum.
//                    // Use currentTimeMillis() over nanoTime() to avoid issues
//                    // with migrating threads across sleep() calls.
//                    long start = System.currentTimeMillis();
//                    // Maximum of 6 seconds, 3x time required on an idle system.
//                    long max_duration = 6000L;
//                    long duration = 0L;
//                    do {
//                        try {
//                            sleep(100);
//                        } catch (InterruptedException e) {
//                            Thread.currentThread().interrupt();
//                        }
//
//                        long now = System.currentTimeMillis();
//                        duration = (now - start);
//                    } while (duration < max_duration &&
//                            server.queryNames(httpsConnectorName, null).size() > 0);

                // Use below to get more accurate metrics.
//                    String message = "HTTPS connector stop took " + duration + "milliseconds";
//                    log.info(message);
            }
        }
    }

    /**
     * Starts the HTTPS connector after waiting for keystore and truststore files to be available.
     * This will work only if connector is configured with bindOnInit="false" option.
     *
     * @throws MalformedObjectNameException if the object name is malformed
     * @throws NoSuchAlgorithmException if the hash algorithm is not available
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the thread is interrupted
     * @throws ReflectionException if an error occurs during reflection
     * @throws InstanceNotFoundException if the MBean instance is not found
     * @throws MBeanException if an error occurs in the MBean
     * @see <a href="https://serverfault.com/questions/328533/can-tomcat-reload-its-ssl-certificate-without-being-restarted">Tomcat SSL reload</a>
     */
    public void startHttpsConnector() throws MalformedObjectNameException, NoSuchAlgorithmException, IOException, InterruptedException, ReflectionException, InstanceNotFoundException, MBeanException {
        String objectString = "*:type=Connector,port=" + httpsPort + ",*";
        ObjectName objectNameQuery = new ObjectName(objectString);
        for (final MBeanServer server: MBeanServerFactory.findMBeanServer(null)) {
            ObjectName objectName = (ObjectName) server.queryNames(objectNameQuery, null).toArray()[0];
            waitForFile(keyStore);
            waitForFile(trustStore);
            server.invoke(objectName, "start", null, null);
        }
    }

    private void waitForFile(StoreInfo store) throws InterruptedException, NoSuchAlgorithmException, IOException {
        // Sometimes keystore.jks becomes unavailable for an unknown reason...
        long waitTime = 100;
        Path path = store.path;
        long startTime = System.currentTimeMillis();
        if (!Files.exists(path)) {
            log.debug("{} doesn't exist; waiting...", store.path);
            do {
                sleep(waitTime);
            }
            // TODO Add max wait time
            while (!Files.exists(path));

            long endTime = System.currentTimeMillis();
            log.debug("{} exist; waited for {}ms...", store.path, endTime - startTime);
        }
        else {
            log.debug("{} exist", store.path);
        }

//        byte[] hash = computeHash(path);
//        log.debug("{}: MD5 {}", path, HexFormat.of().formatHex(hash));
//        log.debug("{}: inode {}", path, getFileInode(path));
    }

    /**
     * Reloads the SSL context by stopping the HTTPS connector, rebuilding the SSL context,
     * and restarting the connector.
     *
     * @return the newly created SSLContext
     * @throws GeneralSecurityException if a security error occurs
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the thread is interrupted
     * @throws ReflectionException if an error occurs during reflection
     * @throws MalformedObjectNameException if the object name is malformed
     * @throws AttributeNotFoundException if the attribute is not found
     * @throws InstanceNotFoundException if the MBean instance is not found
     * @throws MBeanException if an error occurs in the MBean
     */
    public SSLContext reloadSSLContext() throws GeneralSecurityException, IOException, InterruptedException, ReflectionException, MalformedObjectNameException, AttributeNotFoundException, InstanceNotFoundException, MBeanException {
        return reloadSSLContext(false);
    }

    /**
     * Reloads the SSL context with an option to force reload even if certificates haven't changed.
     *
     * @param force if true, forces reload even if certificates haven't changed
     * @return the newly created SSLContext, or existing context if reload is not needed
     * @throws ReflectionException if an error occurs during reflection
     * @throws MalformedObjectNameException if the object name is malformed
     * @throws InstanceNotFoundException if the MBean instance is not found
     * @throws MBeanException if an error occurs in the MBean
     * @throws AttributeNotFoundException if the attribute is not found
     * @throws InterruptedException if the thread is interrupted
     * @throws GeneralSecurityException if a security error occurs
     * @throws IOException if an I/O error occurs
     */
    public SSLContext reloadSSLContext(boolean force) throws ReflectionException, MalformedObjectNameException, InstanceNotFoundException, MBeanException, AttributeNotFoundException, InterruptedException, GeneralSecurityException, IOException {
        if (sslContext != null && !certificateChanged && !force) {
            return sslContext;
        }

        log.debug("Going to reload {} context...", PROTOCOL);

        // Stop HTTPS connector. This step performs two important actions:
        //   1. Prevents the connector from accepting new connections.
        //   2. Following start of the connector guarantees reload of certificates.
        pauseHttpsConnector();
        waitUntilHttpsConnectionsClosed();
        stopHttpsConnector();

        for (SSLContextReloadListener listener: listeners)
            listener.onContextWillReload();

        sslContext = buildSSLContext();
        // Set it as a default context for JVM.
        SSLContext.setDefault(sslContext);
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

        startHttpsConnector();

        String protocol = sslContext.getProtocol();
        log.debug("Certificates reloaded; a new {} context has been created.", protocol);

        // Notify listeners about the new context.
        for (SSLContextReloadListener listener: listeners)
            listener.onContextReloaded(sslContext);

        log.debug("Reload of {} context done", protocol);

        return sslContext;
    }



    /**
     * Loads a KeyStore from the file system using the provided store information.
     *
     * @param info the store information containing path, password, and type
     * @return the loaded KeyStore
     * @throws IOException if an I/O error occurs
     * @throws KeyStoreException if a keystore error occurs
     * @throws CertificateException if a certificate error occurs
     * @throws NoSuchAlgorithmException if the algorithm is not available
     */
    private KeyStore loadStore(StoreInfo info) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
        byte[] hash = calculateFileHash(info.path);
        log.debug("{}: MD5 {}", info.path, HexFormat.of().formatHex(hash));
        KeyStore store = KeyStore.getInstance(info.type);
        try (FileInputStream keyStoreStream = new FileInputStream(info.path.toFile())) {
            log.info("Loading file '{}'", info.path);
            store.load(keyStoreStream, info.password);
        }

        return store;
    }

//    private X509TrustManager tm;

    /**
     * Builds a new SSLContext by loading keystore and truststore and initializing key and trust managers.
     *
     * @return the newly built SSLContext
     * @throws IOException if an I/O error occurs
     * @throws GeneralSecurityException if a security error occurs
     * @throws InterruptedException if the thread is interrupted
     */
    private SSLContext buildSSLContext() throws IOException, GeneralSecurityException, InterruptedException {
        waitForFile(keyStore);
        KeyStore keyStore = loadStore(this.keyStore);
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, this.keyStore.password);

        waitForFile(trustStore);
        KeyStore trustStore = loadStore(this.trustStore);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

//        tm = new X509TrustManager() {
//            X509TrustManager delegate = (X509TrustManager) trustManagerFactory.getTrustManagers()[0];
//            @Override
//            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//                delegate.checkClientTrusted(chain, authType);
//            }
//
//            @Override
//            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//                try {
//                    delegate.checkServerTrusted(chain, authType);
//                } catch (CertificateException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//
//            @Override
//            public X509Certificate[] getAcceptedIssuers() {
//                X509Certificate[] cert = delegate.getAcceptedIssuers();
//                return cert;
//            }
//        };

        SSLContext context = SSLContext.getInstance(PROTOCOL);
        context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
//        context.init(keyManagerFactory.getKeyManagers(), new TrustManager[] {tm}, null);

//        showCAStarCert(tm);

        return context;
    }

    private String caCert;

//    /**
//     * Logs information about CA_star certificates from the trust manager.
//     *
//     * @param trustManager the X509TrustManager containing certificates
//     */
//    private void showCAStarCert(X509TrustManager trustManager) {
//        for (X509Certificate cert : trustManager.getAcceptedIssuers()) {
//            String subject = cert.getSubjectDN().getName();
//            String issuer = cert.getIssuerDN().getName();
//            if (subject.contains("CA_star")) {
//                caCert = "CA certificate: subject '" + subject + "', issuer '" + issuer + "', not before '" + cert.getNotBefore() + "', not after '" + cert.getNotAfter() + "'";
//                log.debug(caCert);
//            }
//        }
//
//
//    }

    /**
     * Calculates the MD5 hash of a file.
     *
     * @param file the path to the file
     * @return the MD5 hash as a byte array, or null if an error occurs
     * @throws NoSuchAlgorithmException if the MD5 algorithm is not available
     */
    private byte[] calculateFileHash(Path file) throws NoSuchAlgorithmException {
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(Files.readAllBytes(file));
            return md.digest();
        } catch (IOException e) {
            log.error("Cannot read file '{}': {}", file, e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves the inode number of a file (Unix systems only).
     *
     * @param path the path to the file
     * @return the inode number
     * @throws IOException if an I/O error occurs
     */
    private long getFileInode(Path path) throws IOException {
        Object ino = Files.getAttribute(path, "unix:ino");
        return (Long) ino;
    }

    // Certificate reload is triggered after modification of a certificate file. Standard certificate files are
    // keystore and truststore. Their locations are fetched from system properties
    // javax.net.ssl.keyStore and javax.net.ssl.trustStore, respectively. Whenever one of the files is modified, a time
    // counter is reset to this value and a countdown starts. When the counter reaches 0, certificate reload is
    // initiated.
    // TODO Introduce a new property?
    public static final long TRIGGER_RELOAD_CALMNESS_INTERVAL = 30_000; // milliseconds

    // How often certificate file modification calmness is checked.
    // TODO Introduce a new property?
    public static final long TRIGGER_RELOAD_CHECK_PERIOD = 1_000; // milliseconds

    public static final long UNSET = 0; // milliseconds

    // a time when a certificate file was modified for the last time.
    private long lastModified = UNSET;

    // Locks access to lastModified variable.
    private StampedLock reloadLock = new StampedLock();

    // Certificate reload timer thread.
    private Thread reloadTimer;


    /**
     * Starts a timer thread that monitors the calmness period and triggers certificate reload
     * when the period expires without further modifications.
     */
    private void startReloadTimer() {
        reloadTimer = new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(TRIGGER_RELOAD_CHECK_PERIOD);
                    long now = System.currentTimeMillis();

                    long lastModified;
                    long stamp = reloadLock.readLock();
                    try {
                        lastModified = this.lastModified;
                    }
                    finally {
                        reloadLock.unlockRead(stamp);
                    }

                    if (lastModified == UNSET) {
                        // Strange, should not get here...
                        String name = Thread.currentThread().getName();
                        log.error("Thread '{}' should not reach that point...", name);
                        break;
                    }

                    long calmnessInterval = now - lastModified; // milliseconds
                    long timeToReload = (TRIGGER_RELOAD_CALMNESS_INTERVAL - calmnessInterval) / 1000; // seconds
                    String message = "Certificate reload will be triggered in {} seconds";
                    log.debug(message, timeToReload);
                    if ((timeToReload % 10_000) == 0) {
                        // For info log level display once per 10 seconds
                        log.info(message, timeToReload);
                    }

                    if (calmnessInterval > TRIGGER_RELOAD_CALMNESS_INTERVAL) {
                        // Trigger certificate reload and break the loop to terminate the thread.
                        log.debug("Triggering certificate reload...");
                        doReloadSSLCertificates();
                        break;
                    }
                }
            } catch (InterruptedException e) {
                log.error("Trigger reload timer interrupted: '{}'", e.getMessage(), e);
            }
            finally {
                 setLastModified(UNSET);
                // Timer not needed any more. Set it to null to enable garbage collector to eat it and free
                // system resources it occupies.
                reloadTimer = null;
                log.info("Certificate reload timer stopped");
            }
        }, "CertificateReloadTimer");

        // Enable JVM to exit.
        reloadTimer.setDaemon(true);
        reloadTimer.start();
        log.info("Certificate reload timer started");
    }

    /**
     * Restarts the certificate reload timer with a new last modification time.
     * If no timer is currently running, a new timer thread is started. The timer
     * will trigger a certificate reload after the calmness interval has elapsed
     * since the specified modification time.
     *
     * @param lastModified the timestamp in milliseconds when the certificate file
     *                    was last modified, used to calculate when the reload
     *                    should be triggered
     */
    private void restartReloadTimer(long lastModified) {
        if (reloadTimer == null) {
            startReloadTimer();
        }

        setLastModified(lastModified);
    }


    /**
     * Sets the last modification time for certificate files in a thread-safe manner.
     * This method is used to track when certificate files were last modified to determine
     * when to trigger a certificate reload. The reload is scheduled to occur after a
     * calmness interval has passed since the last modification.
     *
     * @param lastModified the timestamp in milliseconds when the certificate file was last modified,
     *                    or UNSET (0) to clear the modification time
     */
    private void setLastModified(long lastModified) {
        long stamp = reloadLock.writeLock();
        try {
            if (lastModified != UNSET)
                log.debug("Certificate reload {}scheduled to '{}'",
                        this.lastModified == UNSET ? "" : "re",
                        new Date(lastModified + TRIGGER_RELOAD_CALMNESS_INTERVAL));
            this.lastModified = lastModified;
        }
        finally {
            reloadLock.unlockWrite(stamp);
        }
    }
}

