package com.smartsplit.app.data.model;

import com.smartsplit.app.core.MoneyFormatter;

/**
 * One minimal settlement transaction produced by BalanceEngine.
 */
public class SettlementTransaction {

    public final long fromMemberId;
    public final long toMemberId;
    public final String fromName;
    public final String toName;
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

    public String getFormattedAmount() {
        return MoneyFormatter.formatPaise(amountPaise);
    }
}
