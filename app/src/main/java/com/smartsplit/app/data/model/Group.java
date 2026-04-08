package com.smartsplit.app.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.smartsplit.app.data.sync.SyncState;

import java.util.UUID;

/**
 * Represents a spending group (for example: Goa Trip, Flatmates).
 */
@Entity(tableName = "groups")
public class Group {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "name")
    public String name;

    /** Emoji or text avatar for the group. */
    @ColumnInfo(name = "icon")
    public String icon;

    /** Unix timestamp in milliseconds. */
    @ColumnInfo(name = "created_at")
    public long createdAt;

    /** Unix timestamp in milliseconds. */
    @ColumnInfo(name = "updated_at", defaultValue = "0")
    public long updatedAt;

    @ColumnInfo(name = "created_by_uid")
    public String createdByUid;

    @ColumnInfo(name = "client_uuid", defaultValue = "''")
    public String clientUuid;

    @ColumnInfo(name = "remote_id")
    public String remoteId;

    @ColumnInfo(name = "sync_state", defaultValue = "'PENDING'")
    public String syncState;

    public Group() {
        // Room constructor.
    }

    @Ignore
    public Group(String name, String icon, long createdAt, String createdByUid) {
        this.name = name;
        this.icon = icon;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.createdByUid = createdByUid;
        this.clientUuid = UUID.randomUUID().toString();
        this.remoteId = null;
        this.syncState = SyncState.PENDING;
    }
}
