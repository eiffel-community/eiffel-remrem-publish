# Certificate Observation and Reload Logic

## Overview

The `SSLContextReloader` component provides automatic SSL certificate reloading without requiring application restart. This document describes the detailed logic of how certificate files are observed and reloaded.

## Architecture Components

### 1. File System Monitoring
- **Technology**: Java NIO `WatchService`
- **Monitored Files**: Keystore and truststore files
- **Detection Method**: Directory-level file modification events

### 2. Threading Model
- **Certificate Watcher Thread**: Daemon thread that monitors file system events
- **Certificate Reload Timer Thread**: Daemon thread that implements the calmness interval

### 3. Synchronization
- **StampedLock**: Thread-safe access to the `lastModified` timestamp
- **Atomic Operations**: Ensures consistency during concurrent access

## Detailed Flow

### Phase 1: Initialization

```
@PostConstruct start()
  ↓
initialize()
  ↓
Load keyStore and trustStore from system properties:
  - javax.net.ssl.keyStore
  - javax.net.ssl.keyStorePassword
  - javax.net.ssl.keyStoreType
  - javax.net.ssl.trustStore
  - javax.net.ssl.trustStorePassword
  - javax.net.ssl.trustStoreType
  ↓
Start "Certificate watcher" daemon thread
```

### Phase 2: File System Observation

```
observeCertificates()
  ↓
Create WatchService
  ↓
Register parent directories of keystore and truststore
  ↓
LOOP: Wait for file system events
  ↓
WatchKey.take() [BLOCKING]
  ↓
Process ENTRY_MODIFY events
  ↓
For each modified file:
  1. Check if it matches keystore or truststore path
  2. Verify file exists
  3. Calculate MD5 hash
  4. Compare with previous hash
  ↓
If hash changed:
  - Update StoreInfo.lastModified = System.currentTimeMillis()
  - Check if both keystore AND truststore are modified
  ↓
If both stores modified:
  - Set certificateChanged = true
  - Call restartReloadTimer()
```

### Phase 3: Calmness Period (Debouncing)

**Purpose**: Avoid frequent reloads when certificate files are updated multiple times in quick succession.

**Constants**:
- `TRIGGER_RELOAD_CALMNESS_INTERVAL` = 30,000 ms (30 seconds)
- `TRIGGER_RELOAD_CHECK_PERIOD` = 1,000 ms (1 second)

```
restartReloadTimer(lastModified)
  ↓
If reloadTimer == null:
  Start new "CertificateReloadTimer" daemon thread
  ↓
setLastModified(lastModified) [Thread-safe with StampedLock]
  ↓
Timer thread LOOP:
  Sleep 1 second
  ↓
  Read lastModified (with read lock)
  ↓
  Calculate: calmnessInterval = now - lastModified
  ↓
  If calmnessInterval > 30 seconds:
    Trigger doReloadSSLCertificates()
    Break loop (timer thread terminates)
  ↓
  Otherwise: Continue waiting
```

**Key Behavior**: If a certificate file is modified again during the 30-second calmness period, `setLastModified()` is called with a new timestamp, effectively resetting the countdown.

### Phase 4: Certificate Reload Process

```
doReloadSSLCertificates()
  ↓
reloadSSLContext()
  ↓
┌─────────────────────────────────────────┐
│ 1. Pause HTTPS Connector                │
│    pauseHttpsConnector()                │
│    - Invoke MBean: "pause" operation    │
│    - Prevents new connections           │
└─────────────────────────────────────────┘
  ↓
┌─────────────────────────────────────────┐
│ 2. Wait for Active Connections          │
│    waitUntilHttpsConnectionsClosed()    │
│    - Poll "currentThreadsBusy" MBean    │
│    - Wait until busy == 0               │
│    - Timeout: 60 seconds                │
│    - Poll interval: 500 ms              │
└─────────────────────────────────────────┘
  ↓
┌─────────────────────────────────────────┐
│ 3. Stop HTTPS Connector                 │
│    stopHttpsConnector()                 │
│    - Invoke MBean: "stop" operation     │
│    - Wait for stateName == "STOPPED"    │
│    - Timeout: 60 seconds                │
└─────────────────────────────────────────┘
  ↓
┌─────────────────────────────────────────┐
│ 4. Notify Listeners                     │
│    listener.onContextWillReload()       │
└─────────────────────────────────────────┘
  ↓
┌─────────────────────────────────────────┐
│ 5. Build New SSL Context                │
│    buildSSLContext()                    │
│    - waitForFile(keyStore)              │
│    - loadStore(keyStore)                │
│    - Create KeyManagerFactory           │
│    - waitForFile(trustStore)            │
│    - loadStore(trustStore)              │
│    - Create TrustManagerFactory         │
│    - Create SSLContext with TLS         │
│    - Initialize with managers           │
└─────────────────────────────────────────┘
  ↓
┌─────────────────────────────────────────┐
│ 6. Set as Default Context               │
│    SSLContext.setDefault(sslContext)    │
│    HttpsURLConnection.setDefault...()   │
└─────────────────────────────────────────┘
  ↓
┌─────────────────────────────────────────┐
│ 7. Start HTTPS Connector                │
│    startHttpsConnector()                │
│    - Invoke MBean: "start" operation    │
│    - Connector reads new certificates   │
└─────────────────────────────────────────┘
  ↓
┌─────────────────────────────────────────┐
│ 8. Notify Listeners                     │
│    listener.onContextReloaded(context)  │
└─────────────────────────────────────────┘
  ↓
Reset state:
  - certificateChanged = false
  - keyStore.lastModified = 0
  - trustStore.lastModified = 0
  - reloadTimer = null
```

## Key Design Decisions

### 1. Hash-Based Change Detection
**Why**: File modification timestamps can be unreliable (e.g., file copied from past).
**How**: Calculate MD5 hash of file content and compare with previous hash.

### 2. Both Stores Must Be Modified
**Why**: Ensures atomic update of both keystore and truststore.
**How**: `isWaitingForStoreModification()` checks if any store has `lastModified == 0`.

**Behavior**:
- After reload, both stores have `lastModified = 0`
- First file modification sets its `lastModified` to current time
- Second file modification triggers the reload timer
- This ensures both certificates are updated before reload

### 3. Calmness Period (30 seconds)
**Why**: Certificate updates often involve multiple file operations (write, rename, sync).
**How**: Timer resets on each modification, reload only happens after 30 seconds of no changes.

### 4. Graceful Connector Restart
**Why**: Avoid dropping active connections abruptly.
**How**: 
- Pause connector (no new connections)
- Wait for existing connections to close (60s timeout)
- Stop connector
- Reload certificates
- Start connector

### 5. Tomcat bindOnInit="false" Requirement
**Why**: Allows connector to be stopped and started without full server restart.
**How**: Connector doesn't bind to port during initialization, allowing dynamic rebinding.

## Error Handling

### Retry Mechanism
- **Certificate Watcher Thread**: Retries up to 10 times with 1-second intervals
- **File Availability**: `waitForFile()` polls until file exists (no timeout currently)

### Timeout Handling
- **Connection Drain**: 60-second timeout, proceeds with reload if exceeded
- **Connector Stop**: 60-second timeout, logs error if exceeded

### Exception Recovery
- Exceptions during reload are logged but don't crash the application
- Certificate watcher thread continues monitoring after errors

## Thread Safety

### StampedLock Usage
```java
// Write operation
long stamp = reloadLock.writeLock();
try {
    this.lastModified = lastModified;
} finally {
    reloadLock.unlockWrite(stamp);
}

// Read operation
long stamp = reloadLock.readLock();
try {
    lastModified = this.lastModified;
} finally {
    reloadLock.unlockRead(stamp);
}
```

### Daemon Threads
Both monitoring threads are daemon threads, allowing JVM to exit cleanly without explicit shutdown.

## Monitoring and Observability

### Log Levels

**INFO**:
- Certificate watcher started
- Directory being watched
- File modification detected
- Reload timer started/stopped
- Connector pause/stop/start operations
- Reload completion

**DEBUG**:
- File hash calculations
- Timer countdown
- Connector state transitions
- SSL context creation details

**ERROR**:
- Initialization failures
- File system errors
- Reload failures
- Timeout exceeded

### Listener Interface
```java
public interface SSLContextReloadListener {
    void onContextWillReload();
    void onContextReloaded(SSLContext context);
}
```

Applications can register listeners to:
- Update internal state before reload
- Refresh connections after reload
- Implement custom monitoring/alerting

## Configuration

### System Properties (Required)
```properties
javax.net.ssl.keyStore=/path/to/keystore.jks
javax.net.ssl.keyStorePassword=password
javax.net.ssl.keyStoreType=JKS
javax.net.ssl.trustStore=/path/to/truststore.jks
javax.net.ssl.trustStorePassword=password
javax.net.ssl.trustStoreType=JKS
```

### Spring Properties
```properties
server.port=8443
```

### Tomcat Connector Configuration
```xml
<Connector port="8443" 
           protocol="org.apache.coyote.http11.Http11NioProtocol"
           bindOnInit="false"
           ... />
```

## Limitations and Considerations

1. **Platform Dependency**: File system watching behavior may vary across operating systems
2. **Network File Systems**: May have delayed or unreliable modification events
3. **Large Files**: MD5 hash calculation reads entire file into memory
4. **Single Port**: Currently monitors only one HTTPS port (configurable via server.port)
5. **No Rollback**: If reload fails, the application may be in an inconsistent state

## Future Enhancements

1. Make calmness interval configurable
2. Add maximum wait time for file availability
3. Support multiple HTTPS ports
4. Implement rollback mechanism on reload failure
5. Add metrics/health check endpoints
6. Support for certificate rotation without both files changing
