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
 * Represents a single expense entry in a group.
 */
@Entity(
    tableName = "expenses",
    foreignKeys = {
        @ForeignKey(
            entity = Group.class,
            parentColumns = "id",
            childColumns = "group_id",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = Member.class,
            parentColumns = "id",
            childColumns = "paid_by_member_id",
            onDelete = ForeignKey.RESTRICT
        )
    },
    indices = {@Index("group_id"), @Index("paid_by_member_id")}
)
public class Expense {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "group_id")
    public long groupId;

    @ColumnInfo(name = "title")
    public String title;

    /** Amount in paise (smallest unit). */
    @ColumnInfo(name = "amount_paise")
    public long amountPaise;

    @ColumnInfo(name = "paid_by_member_id")
    public long paidByMemberId;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at", defaultValue = "0")
    public long updatedAt;

    @ColumnInfo(name = "split_type")
    public String splitType;

    @ColumnInfo(name = "client_uuid", defaultValue = "''")
    public String clientUuid;

    @ColumnInfo(name = "remote_id")
    public String remoteId;

    @ColumnInfo(name = "sync_state", defaultValue = "'PENDING'")
    public String syncState;

    public Expense() {
        // Room constructor.
    }

    @Ignore
    public Expense(long groupId, String title, long amountPaise,
                   long paidByMemberId, long createdAt, String splitType) {
        this.groupId = groupId;
        this.title = title;
        this.amountPaise = amountPaise;
        this.paidByMemberId = paidByMemberId;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.splitType = splitType;
        this.clientUuid = UUID.randomUUID().toString();
        this.remoteId = null;
        this.syncState = SyncState.PENDING;
    }
}
