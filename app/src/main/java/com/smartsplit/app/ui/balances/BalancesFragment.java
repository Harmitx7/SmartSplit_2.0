package com.smartsplit.app.ui.balances;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.smartsplit.app.databinding.FragmentBalancesBinding;
import com.smartsplit.app.data.model.SettlementTransaction;
import com.smartsplit.app.ui.viewmodel.GroupViewModel;

import java.util.List;

/**
 * Balances Fragment — shows the minimal settlement transactions for all groups.
 *
 * NOTE: In this initial implementation, it computes balances across ALL groups.
 * A future enhancement would allow the user to select a specific group.
 * VERIFY: Confirm UX requirement — per-group or global balances view.
 */
public class BalancesFragment extends Fragment {

    private FragmentBalancesBinding binding;
    private GroupViewModel groupViewModel;
    private SettlementAdapter settlementAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBalancesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        groupViewModel = new ViewModelProvider(requireActivity()).get(GroupViewModel.class);

        setupRecyclerView();
        observeSettlements();
    }

    private void setupRecyclerView() {
        settlementAdapter = new SettlementAdapter();
        binding.rvSettlements.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSettlements.setAdapter(settlementAdapter);
    }

    private void observeSettlements() {
        groupViewModel.getSettlements().observe(getViewLifecycleOwner(), settlements -> {
            if (settlements == null || settlements.isEmpty()) {
                binding.llEmptyState.setVisibility(View.VISIBLE);
                binding.rvSettlements.setVisibility(View.GONE);
            } else {
                binding.llEmptyState.setVisibility(View.GONE);
                binding.rvSettlements.setVisibility(View.VISIBLE);
                settlementAdapter.submitList(settlements);
            }
        });

        // Observe all groups and trigger balance computation for each
        groupViewModel.getAllGroups().observe(getViewLifecycleOwner(), groups -> {
            if (groups != null && !groups.isEmpty()) {
                // VERIFY: Currently triggers for first group only. Extend to aggregate across groups.
                groupViewModel.computeSettlements(groups.get(0).id);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
