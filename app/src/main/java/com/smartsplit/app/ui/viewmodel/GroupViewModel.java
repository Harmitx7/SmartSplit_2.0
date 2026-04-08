package com.smartsplit.app.ui.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.smartsplit.app.core.BalanceEngine;
import com.smartsplit.app.data.dao.ExpenseSplitDao;
import com.smartsplit.app.data.model.Expense;
import com.smartsplit.app.data.model.ExpenseSplit;
import com.smartsplit.app.data.model.Group;
import com.smartsplit.app.data.model.Member;
import com.smartsplit.app.data.model.SettlementTransaction;
import com.smartsplit.app.data.repository.SmartSplitRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * ViewModel for Group-related screens (GroupDetail, Balances).
 * Owns the group state and coordinates between the repository and UI.
 */
public class GroupViewModel extends AndroidViewModel {

    private final SmartSplitRepository repository;

    private final MutableLiveData<List<SettlementTransaction>> settlements = new MutableLiveData<>();

    public GroupViewModel(Application application) {
        super(application);
        repository = new SmartSplitRepository(application);
    }

    public LiveData<List<Group>> getAllGroups() {
        return repository.getAllGroups();
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

    /** Exposes the computed settlements list for the BalancesFragment to observe */
    public LiveData<List<SettlementTransaction>> getSettlements() {
        return settlements;
    }

    // ─── Write Operations ──────────────────────────────────────────────

    public void createGroup(String name, String icon, String createdByUid) {
        Group group = new Group(name, icon, System.currentTimeMillis(), createdByUid);
        repository.insertGroup(group);
    }

    public void deleteGroup(Group group) {
        repository.deleteGroup(group);
    }

    public void addMember(long groupId, String memberName) {
        Member member = new Member(groupId, memberName, null);
        repository.insertMember(member);
    }

    /**
     * Adds a new expense and automatically splits it among participants.
     *
     * @param groupId     The group this expense belongs to.
     * @param title       The expense description.
     * @param amountPaise Total amount in smallest unit (paise).
     * @param paidByMemberId The member who paid.
     * @param participantIds List of member IDs splitting this expense.
     * @param splitType   "EQUAL" or "CUSTOM".
     * @param customShares Optional map of memberId → share amount for CUSTOM splits.
     */
    public void addExpenseEqual(long groupId, String title, long amountPaise,
                                long paidByMemberId, List<Long> participantIds) {
        if (participantIds == null || participantIds.isEmpty()) return;

        Expense expense = new Expense(groupId, title, amountPaise,
                paidByMemberId, System.currentTimeMillis(), "EQUAL");

        // Distribute evenly: handle remainder cents by giving them to the first participant
        long share = amountPaise / participantIds.size();
        long remainder = amountPaise % participantIds.size();

        List<ExpenseSplit> splits = new ArrayList<>();
        for (int i = 0; i < participantIds.size(); i++) {
            long thisShare = (i == 0) ? share + remainder : share;
            splits.add(new ExpenseSplit(0L, participantIds.get(i), thisShare));
        }

        repository.insertExpenseWithSplits(expense, splits);
    }

    public void addExpenseCustom(long groupId, String title, long amountPaise,
                                  long paidByMemberId, Map<Long, Long> memberSharesMap) {
        Expense expense = new Expense(groupId, title, amountPaise,
                paidByMemberId, System.currentTimeMillis(), "CUSTOM");

        List<ExpenseSplit> splits = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : memberSharesMap.entrySet()) {
            splits.add(new ExpenseSplit(0L, entry.getKey(), entry.getValue()));
        }

        repository.insertExpenseWithSplits(expense, splits);
    }

    /**
     * Computes the minimal settlements for a group on a background thread,
     * then posts the result to the settlements LiveData for the UI to observe.
     */
    public void computeSettlements(long groupId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ExpenseSplitDao.MemberBalance> rawBalances = repository.getNetBalancesSync(groupId);
            List<Member> members = repository.getMembersForGroupSync(groupId);
            Map<Long, Member> memberMap = BalanceEngine.buildMemberMap(members);
            List<SettlementTransaction> computed = BalanceEngine.minimize(rawBalances, memberMap);
            settlements.postValue(computed);
        });
    }
}
