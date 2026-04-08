package com.smartsplit.app.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Stores how much each member owes in a given expense.
 * For EQUAL splits, all shares are equal.
 * For CUSTOM splits, each member's share is stored individually.
 *
 * This is the normalized split line-item table.
 */
@Entity(
    tableName = "expense_splits",
    foreignKeys = {
        @ForeignKey(
            entity = Expense.class,
            parentColumns = "id",
            childColumns = "expense_id",
            onDelete = ForeignKey.CASCADE  // Deleting expense removes all its split records
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

    /** This member's share of the expense in paise */
    @ColumnInfo(name = "share_paise")
    public long sharePaise;

    public ExpenseSplit(long expenseId, long memberId, long sharePaise) {
        this.expenseId = expenseId;
        this.memberId = memberId;
        this.sharePaise = sharePaise;
    }
}
