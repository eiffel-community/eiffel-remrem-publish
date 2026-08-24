package com.ericsson.eiffel.remrem.publish.helper;

import javax.net.ssl.SSLContext;

/**
 * Adapter class providing default empty implementations of {@link SSLContextReloadListener}.
 * <p>
 * This class allows subclasses to override only the methods they need, following the
 * adapter pattern. Useful when only one of the two callback methods is required.
 * </p>
 *
 * <h2>Usage Example</h2>
 * <pre>
 * contextReloader.addListener(new SSLContextReloadAdapter() {
 *     &#64;Override
 *     public void onContextReloaded(SSLContext sslContext) {
 *         // Handle only the reload completion event
 *         updateConnections(sslContext);
 *     }
 * });
 * </pre>
 *
 * @see SSLContextReloadListener
 * @see SSLContextReloader
 */
public class SSLContextReloadAdapter implements SSLContextReloadListener {
    @Override
    public void onContextWillReload() {

    }

    @Override
    public void onContextReloaded(SSLContext sslContext) {

    }
}
