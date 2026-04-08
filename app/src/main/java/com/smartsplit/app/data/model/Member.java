package com.smartsplit.app.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.smartsplit.app.data.sync.SyncState;

import java.util.UUID;

/**
 * Represents a member in a group.
 */
@Entity(
    tableName = "members",
    foreignKeys = @ForeignKey(
        entity = Group.class,
        parentColumns = "id",
        childColumns = "group_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("group_id")}
)
public class Member {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "group_id")
    public long groupId;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "firebase_uid")
    public String firebaseUid;

    @ColumnInfo(name = "created_at", defaultValue = "0")
    public long createdAt;

    @ColumnInfo(name = "updated_at", defaultValue = "0")
    public long updatedAt;

    @ColumnInfo(name = "client_uuid", defaultValue = "''")
    public String clientUuid;

    @ColumnInfo(name = "remote_id")
    public String remoteId;

    @ColumnInfo(name = "sync_state", defaultValue = "'PENDING'")
    public String syncState;

    public Member() {
        // Room constructor.
    }

    @Ignore
    public Member(long groupId, String name, String firebaseUid) {
        long now = System.currentTimeMillis();
        this.groupId = groupId;
        this.name = name;
        this.firebaseUid = firebaseUid;
        this.createdAt = now;
        this.updatedAt = now;
        this.clientUuid = UUID.randomUUID().toString();
        this.remoteId = null;
        this.syncState = SyncState.PENDING;
    }
}
