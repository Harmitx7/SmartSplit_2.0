package com.smartsplit.app.data.sync;

/**
 * v1.1 implementation placeholder. Intentionally does nothing.
 */
public class NoOpSyncGateway implements SyncGateway {
    @Override
    public void enqueueUpsert(SyncRecord record) {
        // No-op in local-first mode.
    }

    @Override
    public void enqueueDelete(String entityType, String clientUuid, long deletedAt) {
        // No-op in local-first mode.
    }
}
