package com.smartsplit.app.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.smartsplit.app.data.dao.ExpenseDao;
import com.smartsplit.app.data.dao.ExpenseSplitDao;
import com.smartsplit.app.data.dao.GroupDao;
import com.smartsplit.app.data.dao.MemberDao;
import com.smartsplit.app.data.db.AppDatabase;
import com.smartsplit.app.data.model.Expense;
import com.smartsplit.app.data.model.ExpenseSplit;
import com.smartsplit.app.data.model.Group;
import com.smartsplit.app.data.model.Member;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Single source of truth for all data in SmartSplit.
 * The Repository abstracts the data source from ViewModels.
 * All database writes happen on a background thread via ExecutorService.
 */
public class SmartSplitRepository {

    private final GroupDao groupDao;
    private final MemberDao memberDao;
    private final ExpenseDao expenseDao;
    private final ExpenseSplitDao expenseSplitDao;

    /**
     * Single-threaded executor for sequential DB writes.
     * Single thread avoids concurrency issues with SQLite.
     */
    private final ExecutorService dbWriteExecutor = Executors.newSingleThreadExecutor();

    public SmartSplitRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        groupDao = db.groupDao();
        memberDao = db.memberDao();
        expenseDao = db.expenseDao();
        expenseSplitDao = db.expenseSplitDao();
    }

    // ─── Group Operations ──────────────────────────────────────────────

    public LiveData<List<Group>> getAllGroups() {
        return groupDao.getAllGroups();
    }

    public LiveData<Group> getGroupById(long id) {
        return groupDao.getGroupById(id);
    }

    public void insertGroup(Group group) {
        dbWriteExecutor.execute(() -> groupDao.insertGroup(group));
    }

    public void deleteGroup(Group group) {
        dbWriteExecutor.execute(() -> groupDao.deleteGroup(group));
    }

    // ─── Member Operations ─────────────────────────────────────────────

    public LiveData<List<Member>> getMembersForGroup(long groupId) {
        return memberDao.getMembersForGroup(groupId);
    }

    public void insertMember(Member member) {
        dbWriteExecutor.execute(() -> memberDao.insertMember(member));
    }

    public void deleteMember(Member member) {
        dbWriteExecutor.execute(() -> memberDao.deleteMember(member));
    }

    // ─── Expense Operations ────────────────────────────────────────────

    public LiveData<List<Expense>> getExpensesForGroup(long groupId) {
        return expenseDao.getExpensesForGroup(groupId);
    }

    public LiveData<Long> getTotalSpendForGroup(long groupId) {
        return expenseDao.getTotalSpendForGroup(groupId);
    }

    /**
     * Inserts an expense AND its split lines atomically on a background thread.
     * This ensures the database is never in a partial state (expense with no splits).
     */
    public void insertExpenseWithSplits(Expense expense, List<ExpenseSplit> splits) {
        dbWriteExecutor.execute(() -> {
            long expenseId = expenseDao.insertExpense(expense);
            // Assign the generated expenseId to each split before inserting
            for (ExpenseSplit split : splits) {
                split.expenseId = expenseId;
            }
            expenseSplitDao.insertSplits(splits);
        });
    }

    // ─── Balance Calculation ───────────────────────────────────────────

    /**
     * Returns raw net-balance data from DB (synchronous, must be called on background thread).
     * Used by GroupViewModel to feed into BalanceEngine.
     */
    public List<ExpenseSplitDao.MemberBalance> getNetBalancesSync(long groupId) {
        return expenseSplitDao.getNetBalancesForGroup(groupId);
    }

    public List<Member> getMembersForGroupSync(long groupId) {
        return memberDao.getMembersForGroupSync(groupId);
    }
}
