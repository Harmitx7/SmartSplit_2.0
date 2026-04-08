package com.smartsplit.app.ui.group;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.smartsplit.app.R;
import com.smartsplit.app.data.model.Expense;
import com.smartsplit.app.data.model.Member;
import com.smartsplit.app.databinding.FragmentGroupDetailBinding;
import com.smartsplit.app.ui.viewmodel.GroupViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Group Detail Fragment — shows group info, members, and expenses.
 * Allows adding members and expenses.
 */
public class GroupDetailFragment extends Fragment {

    private FragmentGroupDetailBinding binding;
    private GroupViewModel groupViewModel;
    private ExpenseAdapter expenseAdapter;

    private long groupId = -1;
    private List<Member> currentMembers = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGroupDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        groupViewModel = new ViewModelProvider(requireActivity()).get(GroupViewModel.class);

        // Get groupId from Safe Args
        if (getArguments() != null) {
            groupId = GroupDetailFragmentArgs.fromBundle(getArguments()).getGroupId();
        }

        if (groupId == -1) {
            Snackbar.make(binding.getRoot(), "Invalid group", Snackbar.LENGTH_LONG).show();
            return;
        }

        setupRecyclerView();
        observeData();
        setupClickListeners();
    }

    private void setupRecyclerView() {
        expenseAdapter = new ExpenseAdapter();
        binding.rvExpenses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvExpenses.setAdapter(expenseAdapter);
    }

    private void observeData() {
        // Observe group details
        groupViewModel.getGroupById(groupId).observe(getViewLifecycleOwner(), group -> {
            if (group != null) {
                binding.tvGroupName.setText(group.name);
                binding.tvGroupIcon.setText(group.icon != null ? group.icon : "🏠");
            }
        });

        // Observe members
        groupViewModel.getMembersForGroup(groupId).observe(getViewLifecycleOwner(), members -> {
            currentMembers = members != null ? members : new ArrayList<>();
            binding.tvMemberCount.setText(String.valueOf(currentMembers.size()));

            if (currentMembers.isEmpty()) {
                binding.tvMemberNames.setText("No members yet — add some!");
            } else {
                String names = currentMembers.stream()
                    .map(m -> m.name)
                    .collect(Collectors.joining(", "));
                binding.tvMemberNames.setText(names);
            }
        });

        // Observe expenses
        groupViewModel.getExpensesForGroup(groupId).observe(getViewLifecycleOwner(), expenses -> {
            if (expenses == null || expenses.isEmpty()) {
                binding.llNoExpenses.setVisibility(View.VISIBLE);
                binding.rvExpenses.setVisibility(View.GONE);
            } else {
                binding.llNoExpenses.setVisibility(View.GONE);
                binding.rvExpenses.setVisibility(View.VISIBLE);
                expenseAdapter.submitList(expenses, currentMembers);
            }
        });

        // Observe total spend
        groupViewModel.getTotalSpendForGroup(groupId).observe(getViewLifecycleOwner(), total -> {
            long totalPaise = total != null ? total : 0L;
            binding.tvTotalSpend.setText(String.format("₹%.2f", totalPaise / 100.0));
        });
    }

    private void setupClickListeners() {
        binding.btnAddMember.setOnClickListener(v -> showAddMemberDialog());

        // FAB with 0.98 scale press animation (Ethereal Ledger design spec)
        binding.fabAddExpense.setOnClickListener(v -> {
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(80)
                .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(200)
                    .withEndAction(this::showAddExpenseDialog).start()).start();
        });

        // Analytics button
        if (binding.btnViewAnalytics != null) {
            binding.btnViewAnalytics.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putLong("groupId", groupId);
                NavHostFragment.findNavController(this).navigate(R.id.action_groupDetail_to_analytics, args);
            });
        }

        // Simplify Debts button
        if (binding.btnSimplifyDebts != null) {
            binding.btnSimplifyDebts.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putLong("groupId", groupId);
                NavHostFragment.findNavController(this).navigate(R.id.action_groupDetail_to_debtSimplification, args);
            });
        }
    }

    private void showAddMemberDialog() {
        View dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_member, null);

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Member")
            .setView(dialogView)
            .setPositiveButton("Add", (dialog, which) -> {
                TextInputEditText etName = dialogView.findViewById(R.id.et_member_name);
                String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                if (!name.isEmpty()) {
                    groupViewModel.addMember(groupId, name);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showAddExpenseDialog() {
        if (currentMembers.isEmpty()) {
            Snackbar.make(binding.getRoot(), "Add members first!", Snackbar.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_expense, null);

        // Populate the "Who paid?" spinner with member names
        Spinner spinnerPaidBy = dialogView.findViewById(R.id.spinner_paid_by);
        List<String> memberNames = currentMembers.stream()
            .map(m -> m.name)
            .collect(Collectors.toList());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            requireContext(), android.R.layout.simple_spinner_item, memberNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPaidBy.setAdapter(adapter);

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Expense")
            .setView(dialogView)
            .setPositiveButton("Split Equally", (dialog, which) -> {
                TextInputEditText etTitle = dialogView.findViewById(R.id.et_expense_title);
                TextInputEditText etAmount = dialogView.findViewById(R.id.et_expense_amount);

                String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
                String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";

                if (title.isEmpty() || amountStr.isEmpty()) {
                    Snackbar.make(binding.getRoot(), "Fill all fields", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double amountRupees = Double.parseDouble(amountStr);
                    long amountPaise = Math.round(amountRupees * 100);

                    int selectedIndex = spinnerPaidBy.getSelectedItemPosition();
                    long paidByMemberId = currentMembers.get(selectedIndex).id;

                    // All members participate in equal split
                    List<Long> participantIds = currentMembers.stream()
                        .map(m -> m.id)
                        .collect(Collectors.toList());

                    groupViewModel.addExpenseEqual(
                        groupId, title, amountPaise, paidByMemberId, participantIds);

                    // Recompute settlements after adding expense
                    groupViewModel.computeSettlements(groupId);

                } catch (NumberFormatException e) {
                    Snackbar.make(binding.getRoot(), "Invalid amount", Snackbar.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
