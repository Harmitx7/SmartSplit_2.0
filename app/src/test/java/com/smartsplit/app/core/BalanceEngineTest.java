package com.smartsplit.app.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.smartsplit.app.data.dao.ExpenseSplitDao;
import com.smartsplit.app.data.model.Member;
import com.smartsplit.app.data.model.SettlementTransaction;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BalanceEngineTest {

    @Test
    public void minimize_whenAlreadySettled_returnsEmptyList() {
        List<ExpenseSplitDao.MemberBalance> rawBalances = new ArrayList<>();
        rawBalances.add(balance(1L, 0L));

        List<SettlementTransaction> result = BalanceEngine.minimize(rawBalances, new HashMap<>());

        assertTrue(result.isEmpty());
    }

    @Test
    public void minimize_withSingleDebtorAndCreditor_returnsOneTransaction() {
        List<ExpenseSplitDao.MemberBalance> rawBalances = new ArrayList<>();
        rawBalances.add(balance(1L, 500L));
        rawBalances.add(balance(2L, -500L));

        Map<Long, Member> members = memberMap(
            member(1L, "Alice"),
            member(2L, "Bob")
        );

        List<SettlementTransaction> result = BalanceEngine.minimize(rawBalances, members);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).fromMemberId);
        assertEquals(2L, result.get(0).toMemberId);
        assertEquals(500L, result.get(0).amountPaise);
    }

    @Test
    public void minimize_withUnevenRemainderBalances_settlesToExactTotal() {
        List<ExpenseSplitDao.MemberBalance> rawBalances = new ArrayList<>();
        rawBalances.add(balance(1L, 101L));
        rawBalances.add(balance(2L, 99L));
        rawBalances.add(balance(3L, -200L));

        Map<Long, Member> members = memberMap(
            member(1L, "A"),
            member(2L, "B"),
            member(3L, "C")
        );

        List<SettlementTransaction> result = BalanceEngine.minimize(rawBalances, members);

        assertEquals(2, result.size());
        long settled = 0L;
        for (SettlementTransaction transaction : result) {
            settled += transaction.amountPaise;
        }
        assertEquals(200L, settled);
    }

    @Test
    public void minimize_withMissingMemberMapEntries_skipsInvalidTransactions() {
        List<ExpenseSplitDao.MemberBalance> rawBalances = new ArrayList<>();
        rawBalances.add(balance(1L, 100L));
        rawBalances.add(balance(2L, -100L));

        Map<Long, Member> members = memberMap(member(1L, "Alice"));

        List<SettlementTransaction> result = BalanceEngine.minimize(rawBalances, members);

        assertTrue(result.isEmpty());
    }

    private static ExpenseSplitDao.MemberBalance balance(long memberId, long netBalancePaise) {
        ExpenseSplitDao.MemberBalance balance = new ExpenseSplitDao.MemberBalance();
        balance.member_id = memberId;
        balance.net_balance_paise = netBalancePaise;
        return balance;
    }

    private static Member member(long id, String name) {
        Member member = new Member(1L, name, null);
        member.id = id;
        return member;
    }

    private static Map<Long, Member> memberMap(Member... members) {
        Map<Long, Member> map = new HashMap<>();
        for (Member member : members) {
            map.put(member.id, member);
        }
        return map;
    }
}
