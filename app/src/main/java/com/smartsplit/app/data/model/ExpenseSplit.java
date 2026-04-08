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
 * Stores each member's share for an expense.
 */
@Entity(
    tableName = "expense_splits",
    foreignKeys = {
        @ForeignKey(
            entity = Expense.class,
            parentColumns = "id",
            childColumns = "expense_id",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = Member.class,
            parentColumns = "id",
            childColumns = "member_id",
            onDelete = ForeignKey.RESTRICT
        )
    },
    indices = {@Index("expense_id"), @Index("member_id")}
)
public class ExpenseSplit {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "expense_id")
    public long expenseId;

    @ColumnInfo(name = "member_id")
    public long memberId;

    @ColumnInfo(name = "share_paise")
    public long sharePaise;

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

    public ExpenseSplit() {
        // Room constructor.
    }

    @Ignore
    public ExpenseSplit(long expenseId, long memberId, long sharePaise) {
        long now = System.currentTimeMillis();
        this.expenseId = expenseId;
        this.memberId = memberId;
        this.sharePaise = sharePaise;
        this.createdAt = now;
        this.updatedAt = now;
        this.clientUuid = UUID.randomUUID().toString();
        this.remoteId = null;
        this.syncState = SyncState.PENDING;
    }
}
