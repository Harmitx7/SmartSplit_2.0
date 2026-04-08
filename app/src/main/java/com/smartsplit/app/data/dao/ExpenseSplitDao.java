package com.smartsplit.app.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.smartsplit.app.data.model.ExpenseSplit;

import java.util.List;

/**
 * Data Access Object for ExpenseSplit operations.
 * Primarily used by the BalanceEngine to compute net balances.
 */
@Dao
public interface ExpenseSplitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSplits(List<ExpenseSplit> splits);

    @Query("SELECT * FROM expense_splits WHERE expense_id = :expenseId")
    List<ExpenseSplit> getSplitsForExpense(long expenseId);

    /**
     * Key query for balance calculation:
     * Gets the sum of (what a member was charged) minus (what they paid)
     * across all expenses in a group. A positive result means they OWE money.
     *
     * NOTE: This does NOT simplify multi-party debts — that is done by BalanceEngine.
     */
    @Query("SELECT es.member_id, " +
           "SUM(es.share_paise) - COALESCE(SUM(CASE WHEN e.paid_by_member_id = es.member_id THEN e.amount_paise ELSE 0 END), 0) AS net_balance_paise " +
           "FROM expense_splits es " +
           "JOIN expenses e ON es.expense_id = e.id " +
           "WHERE e.group_id = :groupId " +
           "GROUP BY es.member_id")
    List<MemberBalance> getNetBalancesForGroup(long groupId);

    /** Simple POJO to receive the balance query result */
    class MemberBalance {
        public long member_id;
        public long net_balance_paise;
    }
}
