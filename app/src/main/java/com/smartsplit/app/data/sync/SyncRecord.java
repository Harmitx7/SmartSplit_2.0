package com.smartsplit.app.data.sync;

import java.util.Map;

/**
 * Generic sync payload for a locally mutated entity.
 */
public class SyncRecord {

    public final String entityType;
    public final String clientUuid;
    public final String remoteId;
    public final long updatedAt;
    public final Map<String, Object> payload;

    public SyncRecord(String entityType, String clientUuid, String remoteId,
                      long updatedAt, Map<String, Object> payload) {
        this.entityType = entityType;
        this.clientUuid = clientUuid;
        this.remoteId = remoteId;
        this.updatedAt = updatedAt;
        this.payload = payload;
    }
}
