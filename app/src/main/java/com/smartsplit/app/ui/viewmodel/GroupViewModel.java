package com.smartsplit.app.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.smartsplit.app.core.BalanceEngine;
import com.smartsplit.app.core.SplitValidation;
import com.smartsplit.app.data.dao.ExpenseSplitDao;
import com.smartsplit.app.data.model.Expense;
import com.smartsplit.app.data.model.ExpenseSplit;
import com.smartsplit.app.data.model.Group;
import com.smartsplit.app.data.model.GroupSummary;
import com.smartsplit.app.data.model.Member;
import com.smartsplit.app.data.model.SettlementTransaction;
import com.smartsplit.app.data.repository.SmartSplitRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel for group, balances, and expense flows.
 */
public class GroupViewModel extends AndroidViewModel {

    private final SmartSplitRepository repository;
    private final ExecutorService settlementExecutor = Executors.newSingleThreadExecutor();
    private final Map<Long, MutableLiveData<List<SettlementTransaction>>> settlementsByGroup = new ConcurrentHashMap<>();
    private final MutableLiveData<String> operationError = new MutableLiveData<>();

    public GroupViewModel(@NonNull Application application) {
        super(application);
        repository = new SmartSplitRepository(application);
    }

    public LiveData<List<Group>> getAllGroups() {
        return repository.getAllGroups();
    }

    public LiveData<List<GroupSummary>> getGroupSummaries() {
        return repository.getGroupSummaries();
    }

    public LiveData<Group> getGroupById(long groupId) {
        return repository.getGroupById(groupId);
    }

    public LiveData<List<Member>> getMembersForGroup(long groupId) {
        return repository.getMembersForGroup(groupId);
    }

    public LiveData<List<Expense>> getExpensesForGroup(long groupId) {
        return repository.getExpensesForGroup(groupId);
    }

    public LiveData<Long> getTotalSpendForGroup(long groupId) {
        return repository.getTotalSpendForGroup(groupId);
    }

    public LiveData<String> getOperationError() {
        return operationError;
    }

    public void clearOperationError() {
        operationError.setValue(null);
    }

    public LiveData<List<SettlementTransaction>> getSettlementsForGroup(long groupId) {
        MutableLiveData<List<SettlementTransaction>> target = getOrCreateSettlementsLiveData(groupId);
        if (target.getValue() == null) {
            target.setValue(new ArrayList<>());
        }
        return target;
    }

    public void createGroup(String name, String icon, String createdByUid) {
        Group group = new Group(name, icon, System.currentTimeMillis(), createdByUid);
        repository.insertGroup(group);
    }

    public void deleteGroup(Group group) {
        repository.deleteGroup(group);
    }

    public void addMember(long groupId, String memberName) {
        Member member = new Member(groupId, memberName, null);
        repository.insertMember(member, () -> computeSettlementsForGroup(groupId));
    }

    public boolean addExpenseEqual(long groupId, String title, long amountPaise,
                                   long paidByMemberId, List<Long> participantIds) {
        SplitValidation.ValidationResult validation =
            SplitValidation.validateEqualSplit(amountPaise, paidByMemberId, participantIds);
        if (!validation.valid) {
            operationError.postValue(validation.errorMessage);
            return false;
        }

        Expense expense = new Expense(
            groupId,
            title,
            amountPaise,
            paidByMemberId,
            System.currentTimeMillis(),
            "EQUAL"
        );

        List<ExpenseSplit> splits = SplitValidation.toExpenseSplits(validation.normalizedShares);
        repository.insertExpenseWithSplits(expense, splits, () -> computeSettlementsForGroup(groupId));
        return true;
    }

    public boolean addExpenseCustom(long groupId, String title, long amountPaise,
                                    long paidByMemberId, Map<Long, Long> memberSharesMap) {
        SplitValidation.ValidationResult validation =
            SplitValidation.validateCustomSplit(amountPaise, paidByMemberId, memberSharesMap);
        if (!validation.valid) {
            operationError.postValue(validation.errorMessage);
            return false;
        }

        Expense expense = new Expense(
            groupId,
            title,
            amountPaise,
            paidByMemberId,
            System.currentTimeMillis(),
            "CUSTOM"
        );

        List<ExpenseSplit> splits = SplitValidation.toExpenseSplits(validation.normalizedShares);
        repository.insertExpenseWithSplits(expense, splits, () -> computeSettlementsForGroup(groupId));
        return true;
    }

    public void computeSettlementsForGroup(long groupId) {
        MutableLiveData<List<SettlementTransaction>> target = getOrCreateSettlementsLiveData(groupId);
        settlementExecutor.execute(() -> {
            List<ExpenseSplitDao.MemberBalance> rawBalances = repository.getNetBalancesSync(groupId);
            List<Member> members = repository.getMembersForGroupSync(groupId);
            Map<Long, Member> memberMap = BalanceEngine.buildMemberMap(members);
            List<SettlementTransaction> computed = BalanceEngine.minimize(rawBalances, memberMap);
            target.postValue(computed);
        });
    }

    private MutableLiveData<List<SettlementTransaction>> getOrCreateSettlementsLiveData(long groupId) {
        MutableLiveData<List<SettlementTransaction>> existing = settlementsByGroup.get(groupId);
        if (existing != null) {
            return existing;
        }
        MutableLiveData<List<SettlementTransaction>> created = new MutableLiveData<>(new ArrayList<>());
        settlementsByGroup.put(groupId, created);
        return created;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        settlementExecutor.shutdown();
    }
}
