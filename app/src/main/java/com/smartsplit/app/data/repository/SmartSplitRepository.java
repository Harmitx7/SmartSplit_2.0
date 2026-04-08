package com.smartsplit.app.data.repository;

import android.app.Application;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.smartsplit.app.data.dao.ExpenseDao;
import com.smartsplit.app.data.dao.ExpenseSplitDao;
import com.smartsplit.app.data.dao.GroupDao;
import com.smartsplit.app.data.dao.MemberDao;
import com.smartsplit.app.data.db.AppDatabase;
import com.smartsplit.app.data.model.Expense;
import com.smartsplit.app.data.model.ExpenseSplit;
import com.smartsplit.app.data.model.Group;
import com.smartsplit.app.data.model.GroupSummary;
import com.smartsplit.app.data.model.Member;
import com.smartsplit.app.data.sync.NoOpSyncGateway;
import com.smartsplit.app.data.sync.SyncEntityMapper;
import com.smartsplit.app.data.sync.SyncGateway;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Single source of truth for local data with a sync-ready seam.
 */
public class SmartSplitRepository {

    private final GroupDao groupDao;
    private final MemberDao memberDao;
    private final ExpenseDao expenseDao;
    private final ExpenseSplitDao expenseSplitDao;
    private final SyncGateway syncGateway;

    private final ExecutorService dbWriteExecutor = Executors.newSingleThreadExecutor();

    public SmartSplitRepository(Application application) {
        this(application, new NoOpSyncGateway());
    }

    public SmartSplitRepository(Application application, SyncGateway syncGateway) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.groupDao = db.groupDao();
        this.memberDao = db.memberDao();
        this.expenseDao = db.expenseDao();
        this.expenseSplitDao = db.expenseSplitDao();
        this.syncGateway = syncGateway;
    }

    public LiveData<List<Group>> getAllGroups() {
        return groupDao.getAllGroups();
    }

    public LiveData<List<GroupSummary>> getGroupSummaries() {
        return groupDao.getGroupSummaries();
    }

    public LiveData<Group> getGroupById(long id) {
        return groupDao.getGroupById(id);
    }

    public void insertGroup(Group group) {
        insertGroup(group, null);
    }

    public void insertGroup(Group group, @Nullable Runnable onComplete) {
        dbWriteExecutor.execute(() -> {
            group.updatedAt = System.currentTimeMillis();
            groupDao.insertGroup(group);
            syncGateway.enqueueUpsert(SyncEntityMapper.toRecord(group));
            runCompletion(onComplete);
        });
    }

    public void deleteGroup(Group group) {
        dbWriteExecutor.execute(() -> {
            groupDao.deleteGroup(group);
            syncGateway.enqueueDelete(SyncEntityMapper.ENTITY_GROUP, group.clientUuid, System.currentTimeMillis());
        });
    }

    public LiveData<List<Member>> getMembersForGroup(long groupId) {
        return memberDao.getMembersForGroup(groupId);
    }

    public void insertMember(Member member) {
        insertMember(member, null);
    }

    public void insertMember(Member member, @Nullable Runnable onComplete) {
        dbWriteExecutor.execute(() -> {
            member.updatedAt = System.currentTimeMillis();
            memberDao.insertMember(member);
            syncGateway.enqueueUpsert(SyncEntityMapper.toRecord(member));
            runCompletion(onComplete);
        });
    }

    public void deleteMember(Member member) {
        dbWriteExecutor.execute(() -> {
            memberDao.deleteMember(member);
            syncGateway.enqueueDelete(SyncEntityMapper.ENTITY_MEMBER, member.clientUuid, System.currentTimeMillis());
        });
    }

    public LiveData<List<Expense>> getExpensesForGroup(long groupId) {
        return expenseDao.getExpensesForGroup(groupId);
    }

    public LiveData<Long> getTotalSpendForGroup(long groupId) {
        return expenseDao.getTotalSpendForGroup(groupId);
    }

    public void insertExpenseWithSplits(Expense expense, List<ExpenseSplit> splits) {
        insertExpenseWithSplits(expense, splits, null);
    }

    public void insertExpenseWithSplits(Expense expense, List<ExpenseSplit> splits, @Nullable Runnable onComplete) {
        dbWriteExecutor.execute(() -> {
            expense.updatedAt = System.currentTimeMillis();
            long expenseId = expenseDao.insertExpense(expense);
            expense.id = expenseId;
            syncGateway.enqueueUpsert(SyncEntityMapper.toRecord(expense));

            for (ExpenseSplit split : splits) {
                split.expenseId = expenseId;
                split.updatedAt = System.currentTimeMillis();
            }
            expenseSplitDao.insertSplits(splits);
            for (ExpenseSplit split : splits) {
                syncGateway.enqueueUpsert(SyncEntityMapper.toRecord(split));
            }

            runCompletion(onComplete);
        });
    }

    public List<ExpenseSplitDao.MemberBalance> getNetBalancesSync(long groupId) {
        return expenseSplitDao.getNetBalancesForGroup(groupId);
    }

    public List<Member> getMembersForGroupSync(long groupId) {
        return memberDao.getMembersForGroupSync(groupId);
    }

    private void runCompletion(@Nullable Runnable onComplete) {
        if (onComplete != null) {
            onComplete.run();
        }
    }
}
