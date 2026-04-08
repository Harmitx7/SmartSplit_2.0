package com.smartsplit.app.data.sync;

/**
 * Sync seam for future cloud integration.
 */
public interface SyncGateway {
    void enqueueUpsert(SyncRecord record);
    void enqueueDelete(String entityType, String clientUuid, long deletedAt);
}
