package com.ericsson.eiffel.remrem.publish.helper;

import javax.net.ssl.SSLContext;

/**
 * Listener interface for receiving notifications about SSL context reload events.
 * <p>
 * Implementations can register with {@link SSLContextReloader} to be notified before
 * and after SSL certificates are reloaded, allowing applications to update connections
 * or perform cleanup operations.
 * </p>
 *
 * <h2>Usage Example</h2>
 * <pre>
 * sslContextReloader.addListener(new SSLContextReloadListener() {
 *     &#64;Override
 *     public void onContextWillReload() {
 *         // Prepare for reload (e.g., pause operations)
 *     }
 *     
 *     &#64;Override
 *     public void onContextReloaded(SSLContext sslContext) {
 *         // Update connections with new context
 *     }
 * });
 * </pre>
 *
 * @see SSLContextReloader
 * @see SSLContextReloadAdapter
 */
public interface SSLContextReloadListener {
    /**
     * Called before the SSL context reload begins.
     * Use this to prepare for the reload, such as pausing operations or logging.
     */
    void onContextWillReload();
    
    /**
     * Called after the SSL context has been successfully reloaded.
     * Use this to update connections or resume operations with the new context.
     *
     * @param sslContext the newly created SSLContext with updated certificates
     */
    void onContextReloaded(SSLContext sslContext);
}
