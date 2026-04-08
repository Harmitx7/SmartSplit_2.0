package com.smartsplit.app.data.model;

/**
 * A plain data class (NOT a Room entity) representing one settlement transaction.
 * Produced by the BalanceEngine as the minimal set of payments to settle a group.
 */
public class SettlementTransaction {

    /** The member ID who owes money */
    public final long fromMemberId;

    /** The member ID who is owed money */
    public final long toMemberId;

    /** Name of who owes (for display) */
    public final String fromName;

    /** Name of who is owed (for display) */
    public final String toName;

    /** Amount in paise */
    public final long amountPaise;

    public SettlementTransaction(long fromMemberId, long toMemberId,
                                  String fromName, String toName,
                                  long amountPaise) {
        this.fromMemberId = fromMemberId;
        this.toMemberId = toMemberId;
        this.fromName = fromName;
        this.toName = toName;
        this.amountPaise = amountPaise;
    }

    /** Returns amount as a formatted rupee string, e.g., "₹50.00" */
    public String getFormattedAmount() {
        return String.format("₹%.2f", amountPaise / 100.0);
    }
}
