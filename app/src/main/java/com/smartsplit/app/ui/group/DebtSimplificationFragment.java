package com.smartsplit.app.ui.group;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.smartsplit.app.R;
import com.smartsplit.app.core.BalanceEngine;
import com.smartsplit.app.data.dao.ExpenseSplitDao;
import com.smartsplit.app.data.db.AppDatabase;
import com.smartsplit.app.data.model.Member;
import com.smartsplit.app.data.model.SettlementTransaction;
import com.smartsplit.app.databinding.FragmentDebtSimplificationBinding;
import com.smartsplit.app.ui.viewmodel.GroupViewModel;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * DebtSimplificationFragment
 *
 * Shows the output of BalanceEngine.minimize() — the minimal set of transactions
 * needed to fully settle a group's debts, rendered as a beautiful glassmorphic list.
 *
 * Design:
 *   - Hero section showing "X → Y transactions" reduction
 *   - RecyclerView of settlement cards (from→to→amount)  
 *   - Lottie success animation when group is already settled
 */
public class DebtSimplificationFragment extends Fragment {

    private FragmentDebtSimplificationBinding binding;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private SimplifiedTransactionAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDebtSimplificationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        long groupId = DebtSimplificationFragmentArgs.fromBundle(getArguments()).getGroupId();

        adapter = new SimplifiedTransactionAdapter();
        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTransactions.setAdapter(adapter);

        // Animate list in
        binding.rvTransactions.setLayoutAnimation(
            AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_slide_from_bottom)
        );

        binding.btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        loadAndSimplify(groupId);
    }

    private void loadAndSimplify(long groupId) {
        AppDatabase db = AppDatabase.getInstance(requireContext());

        executor.execute(() -> {
            // 1. Get raw net balances from DB
            List<ExpenseSplitDao.MemberBalance> rawBalances =
                db.expenseSplitDao().getNetBalancesForGroup(groupId);

            // 2. Build member map for display names
            List<Member> members = db.memberDao().getMembersForGroupSync(groupId);
            Map<Long, Member> memberMap = BalanceEngine.buildMemberMap(members);

            // 3. Run the greedy minimization algorithm
            List<SettlementTransaction> simplified = BalanceEngine.minimize(rawBalances, memberMap);

            // 4. Post results back to main thread
            requireActivity().runOnUiThread(() -> {
                if (simplified == null || simplified.isEmpty()) {
                    showAllClearState();
                } else {
                    showTransactions(simplified, rawBalances.size());
                }
            });
        });
    }

    private void showTransactions(List<SettlementTransaction> transactions, int originalCount) {
        binding.emptyState.setVisibility(View.GONE);
        binding.contentGroup.setVisibility(View.VISIBLE);

        // Hero stats
        String originalText = getString(R.string.original_transactions, originalCount);
        String simplifiedText = getString(R.string.simplified_to, transactions.size());
        binding.tvOriginalCount.setText(originalText);
        binding.tvSimplifiedCount.setText(simplifiedText);

        adapter.submitList(transactions);
        binding.rvTransactions.scheduleLayoutAnimation();
    }

    private void showAllClearState() {
        binding.contentGroup.setVisibility(View.GONE);
        binding.emptyState.setVisibility(View.VISIBLE);
        binding.lottieSuccess.setAnimation(R.raw.lottie_success);
        binding.lottieSuccess.playAnimation();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executor.shutdown();
        binding = null;
    }
}
