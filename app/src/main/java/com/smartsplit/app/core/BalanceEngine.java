package com.smartsplit.app.core;

import com.smartsplit.app.data.dao.ExpenseSplitDao;
import com.smartsplit.app.data.model.Member;
import com.smartsplit.app.data.model.SettlementTransaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * BALANCE ENGINE — Greedy Transaction Minimization
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 *
 * Algorithm: Greedy Debt Simplification (also called "Debt Cancellation")
 *
 * How it works:
 *   1. Compute net balance for each member: (amount they paid) - (amount they owe).
 *      • Positive net = they are OWED money (creditor).
 *      • Negative net = they OWE money (debtor).
 *   2. Use two max-heaps (priority queues):
 *      • One for largest creditors.
 *      • One for largest debtors.
 *   3. Greedily pair the largest creditor with the largest debtor.
 *      Match the amounts, produce a settlement, and push any remainder back.
 *   4. Repeat until all balances are zero.
 *
 * Complexity: O(n log n) where n = number of members.
 * Result: Minimum number of transactions required to settle the group.
 *
 * Example:
 *   3 members, net = [A: +300, B: -100, C: -200]
 *   → A gets from B: 100, A gets from C: 200 → 2 transactions
 *   (Without simplification it could be 3-4 transactions)
 */
public class BalanceEngine {

    /**
     * Computes the minimal set of settlement transactions for a group.
     *
     * @param rawBalances  List of MemberBalance from the database (member_id, net_balance_paise).
     *                     net_balance_paise > 0 means this member owes more than they paid (debtor).
     * @param memberMap    Map of member_id → Member (for display names).
     * @return List of minimal settlement transactions. Empty list if the group is already settled.
     */
    public static List<SettlementTransaction> minimize(
            List<ExpenseSplitDao.MemberBalance> rawBalances,
            Map<Long, Member> memberMap) {

        List<SettlementTransaction> result = new ArrayList<>();

        if (rawBalances == null || rawBalances.isEmpty()) {
            return result;
        }

        // Max-heap for creditors: those who are OWED money (negative net_balance = paid more than owed)
        // We store as positive amounts. Ordered by largest credit first.
        PriorityQueue<long[]> creditors = new PriorityQueue<>(
            (a, b) -> Long.compare(b[1], a[1])  // descending order
        );

        // Max-heap for debtors: those who OWE money (positive net_balance = owe more than paid)
        PriorityQueue<long[]> debtors = new PriorityQueue<>(
            (a, b) -> Long.compare(b[1], a[1])  // descending order
        );

        // Populate heaps
        // rawBalances.net_balance_paise > 0 → member owes (debtor)
        // rawBalances.net_balance_paise < 0 → member is owed (creditor)
        for (ExpenseSplitDao.MemberBalance mb : rawBalances) {
            if (mb.net_balance_paise > 0) {
                debtors.add(new long[]{mb.member_id, mb.net_balance_paise});
            } else if (mb.net_balance_paise < 0) {
                creditors.add(new long[]{mb.member_id, -mb.net_balance_paise});
            }
            // net_balance == 0: fully settled, skip
        }

        // Greedy pairing loop
        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            long[] creditor = creditors.poll();
            long[] debtor = debtors.poll();

            long creditAmount = creditor[1];
            long debtAmount = debtor[1];
            long settledAmount = Math.min(creditAmount, debtAmount);

            Member fromMember = memberMap.get(debtor[0]);
            Member toMember = memberMap.get(creditor[0]);

            // Guard: skip if member data is missing (data integrity issue)
            if (fromMember != null && toMember != null) {
                result.add(new SettlementTransaction(
                    debtor[0], creditor[0],
                    fromMember.name, toMember.name,
                    settledAmount
                ));
            }

            // Push remainder back if amounts didn't match exactly
            long remainder = creditAmount - debtAmount;
            if (remainder > 0) {
                creditors.add(new long[]{creditor[0], remainder});
            } else if (remainder < 0) {
                debtors.add(new long[]{debtor[0], -remainder});
            }
        }

        return result;
    }

    /**
     * Converts a list of Members into a Map for O(1) lookup by ID.
     */
    public static Map<Long, Member> buildMemberMap(List<Member> members) {
        Map<Long, Member> map = new HashMap<>();
        for (Member m : members) {
            map.put(m.id, m);
        }
        return map;
    }
}
