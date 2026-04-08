package com.smartsplit.app.ui.balances;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.smartsplit.app.R;
import com.smartsplit.app.data.model.Group;
import com.smartsplit.app.data.model.SettlementTransaction;
import com.smartsplit.app.databinding.FragmentBalancesBinding;
import com.smartsplit.app.ui.motion.IosMotion;
import com.smartsplit.app.ui.viewmodel.GroupViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Balances screen with explicit per-group selection.
 */
public class BalancesFragment extends Fragment {

    private FragmentBalancesBinding binding;
    private GroupViewModel groupViewModel;
    private SettlementAdapter settlementAdapter;
    private ArrayAdapter<String> groupSelectorAdapter;

    private final List<Group> currentGroups = new ArrayList<>();
    private long selectedGroupId = -1L;
    private long observedGroupId = -1L;
    private boolean suppressSelectionEvent = false;
    private LiveData<List<SettlementTransaction>> selectedSettlementSource;

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
        setupGroupSelector();
        observeGroups();
        setupMotion();
    }

    private void setupRecyclerView() {
        settlementAdapter = new SettlementAdapter();
        binding.rvSettlements.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSettlements.setAdapter(settlementAdapter);
        IosMotion.applyListLayoutAnimation(binding.rvSettlements);
    }

    private void setupMotion() {
        IosMotion.animateIn(binding.spinnerGroupSelector, 40);
        IosMotion.animateIn(binding.rvSettlements, 110);
    }

    private void setupGroupSelector() {
        groupSelectorAdapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            new ArrayList<>()
        );
        groupSelectorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerGroupSelector.setAdapter(groupSelectorAdapter);

        binding.spinnerGroupSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (suppressSelectionEvent || position < 0 || position >= currentGroups.size()) {
                    return;
                }
                Group selected = currentGroups.get(position);
                if (selectedGroupId != selected.id) {
                    selectedGroupId = selected.id;
                    bindSettlementsForSelectedGroup();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No-op.
            }
        });
    }

    private void observeGroups() {
        groupViewModel.getAllGroups().observe(getViewLifecycleOwner(), groups -> {
            currentGroups.clear();
            if (groups != null) {
                currentGroups.addAll(groups);
            }

            updateGroupSelectorItems();

            if (currentGroups.isEmpty()) {
                selectedGroupId = -1L;
                observedGroupId = -1L;
                if (selectedSettlementSource != null) {
                    selectedSettlementSource.removeObservers(getViewLifecycleOwner());
                    selectedSettlementSource = null;
                }
                settlementAdapter.submitList(new ArrayList<>());
                showNoGroupsState();
                return;
            }

            binding.spinnerGroupSelector.setEnabled(true);
            int selectionIndex = indexOfGroup(selectedGroupId);
            if (selectionIndex == -1) {
                selectionIndex = 0; // most recent group because DAO orders DESC
                selectedGroupId = currentGroups.get(0).id;
            }

            suppressSelectionEvent = true;
            binding.spinnerGroupSelector.setSelection(selectionIndex, false);
            suppressSelectionEvent = false;

            bindSettlementsForSelectedGroup();
        });
    }

    private void bindSettlementsForSelectedGroup() {
        if (selectedGroupId == -1L) {
            return;
        }

        if (observedGroupId != selectedGroupId) {
            if (selectedSettlementSource != null) {
                selectedSettlementSource.removeObservers(getViewLifecycleOwner());
            }

            selectedSettlementSource = groupViewModel.getSettlementsForGroup(selectedGroupId);
            selectedSettlementSource.observe(getViewLifecycleOwner(), this::renderSettlements);
            observedGroupId = selectedGroupId;
        }

        groupViewModel.computeSettlementsForGroup(selectedGroupId);
    }

    private void renderSettlements(List<SettlementTransaction> settlements) {
        if (settlements == null || settlements.isEmpty()) {
            showAllSettledState();
            settlementAdapter.submitList(new ArrayList<>());
            return;
        }

        binding.llEmptyState.setVisibility(View.GONE);
        binding.rvSettlements.setVisibility(View.VISIBLE);
        settlementAdapter.submitList(settlements);
        binding.rvSettlements.scheduleLayoutAnimation();
    }

    private void updateGroupSelectorItems() {
        List<String> labels = new ArrayList<>();
        for (Group group : currentGroups) {
            labels.add(group.name);
        }

        groupSelectorAdapter.clear();
        groupSelectorAdapter.addAll(labels);
        groupSelectorAdapter.notifyDataSetChanged();
    }

    private int indexOfGroup(long groupId) {
        for (int i = 0; i < currentGroups.size(); i++) {
            if (currentGroups.get(i).id == groupId) {
                return i;
            }
        }
        return -1;
    }

    private void showNoGroupsState() {
        binding.spinnerGroupSelector.setEnabled(false);
        binding.llEmptyState.setVisibility(View.VISIBLE);
        binding.rvSettlements.setVisibility(View.GONE);
        binding.tvEmptyTitle.setText(R.string.no_groups);
        binding.tvEmptySubtitle.setText(R.string.no_groups_subtitle);
        IosMotion.animateIn(binding.llEmptyState, 40);
    }

    private void showAllSettledState() {
        binding.llEmptyState.setVisibility(View.VISIBLE);
        binding.rvSettlements.setVisibility(View.GONE);
        binding.tvEmptyTitle.setText(R.string.all_settled);
        binding.tvEmptySubtitle.setText(R.string.all_settled_subtitle);
        IosMotion.animateIn(binding.llEmptyState, 40);
    }

    @Override
    public void onDestroyView() {
        if (selectedSettlementSource != null) {
            selectedSettlementSource.removeObservers(getViewLifecycleOwner());
            selectedSettlementSource = null;
        }
        binding = null;
        super.onDestroyView();
    }
}
