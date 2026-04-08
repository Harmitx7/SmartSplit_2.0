package com.smartsplit.app.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Represents a single expense within a group.
 * e.g., "Pizza – ₹500 – paid by Rahul"
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
            onDelete = ForeignKey.RESTRICT  // Cannot delete a member who paid an expense
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

    /** Amount in the smallest currency unit (e.g., paisa) to avoid floating point errors */
    @ColumnInfo(name = "amount_paise")
    public long amountPaise;

    /** Member ID of who paid for this expense */
    @ColumnInfo(name = "paid_by_member_id")
    public long paidByMemberId;

    /** Unix timestamp (ms) of the expense */
    @ColumnInfo(name = "created_at")
    public long createdAt;

    /** "EQUAL" or "CUSTOM" — determines how splits are calculated */
    @ColumnInfo(name = "split_type")
    public String splitType;

    public Expense(long groupId, String title, long amountPaise,
                   long paidByMemberId, long createdAt, String splitType) {
        this.groupId = groupId;
        this.title = title;
        this.amountPaise = amountPaise;
        this.paidByMemberId = paidByMemberId;
        this.createdAt = createdAt;
        this.splitType = splitType;
    }
}
