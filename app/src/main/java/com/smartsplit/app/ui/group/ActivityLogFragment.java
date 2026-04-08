package com.smartsplit.app.ui.group;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.smartsplit.app.databinding.FragmentActivityLogBinding;
import com.smartsplit.app.ui.viewmodel.GroupViewModel;
import com.smartsplit.app.data.model.Expense;
import com.smartsplit.app.data.model.Member;

import java.util.ArrayList;
import java.util.List;

public class ActivityLogFragment extends Fragment {

    private FragmentActivityLogBinding binding;
    private GroupViewModel groupViewModel;
    private ExpenseAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentActivityLogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private List<Member> currentMembers = new ArrayList<>();
    private List<Expense> currentExpenses = new ArrayList<>();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        groupViewModel = new ViewModelProvider(requireActivity()).get(GroupViewModel.class);
        
        adapter = new ExpenseAdapter();
        binding.rvActivityLog.setAdapter(adapter);

        long groupId = -1;
        if (getArguments() != null) {
            groupId = ActivityLogFragmentArgs.fromBundle(getArguments()).getGroupId();
        }

        if (groupId != -1) {
            setupObservers(groupId);
        }
    }

    private void setupObservers(long groupId) {
        groupViewModel.getMembersForGroup(groupId).observe(getViewLifecycleOwner(), members -> {
            currentMembers = members != null ? members : new ArrayList<>();
            adapter.submitList(currentExpenses, currentMembers);
        });

        groupViewModel.getExpensesForGroup(groupId).observe(getViewLifecycleOwner(), expenses -> {
            currentExpenses = expenses != null ? expenses : new ArrayList<>();
            adapter.submitList(currentExpenses, currentMembers);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
