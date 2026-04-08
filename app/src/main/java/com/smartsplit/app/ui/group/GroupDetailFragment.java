package com.smartsplit.app.ui.group;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.smartsplit.app.R;
import com.smartsplit.app.core.MoneyFormatter;
import com.smartsplit.app.data.model.Member;
import com.smartsplit.app.databinding.FragmentGroupDetailBinding;
import com.smartsplit.app.ui.motion.IosMotion;
import com.smartsplit.app.ui.viewmodel.GroupViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Group detail screen with members and expenses.
 */
public class GroupDetailFragment extends Fragment {

    private FragmentGroupDetailBinding binding;
    private GroupViewModel groupViewModel;
    private ExpenseAdapter expenseAdapter;

    private long groupId = -1L;
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

        if (getArguments() != null) {
            groupId = GroupDetailFragmentArgs.fromBundle(getArguments()).getGroupId();
        }

        if (groupId == -1L) {
            Snackbar.make(binding.getRoot(), "Invalid group", Snackbar.LENGTH_LONG).show();
            return;
        }

        setupRecyclerView();
        setupErrorObserver();
        observeData();
        setupClickListeners();
        setupMotion();
    }

    private void setupRecyclerView() {
        expenseAdapter = new ExpenseAdapter();
        binding.rvExpenses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvExpenses.setAdapter(expenseAdapter);
        IosMotion.applyListLayoutAnimation(binding.rvExpenses);
    }

    private void setupMotion() {
        IosMotion.animateIn(binding.btnBack, 0);
        IosMotion.animateIn(binding.tvGroupName, 30);
        IosMotion.animateIn(binding.tvGroupIcon, 60);
        IosMotion.animateIn(binding.btnAddMember, 110);
        if (binding.btnViewAnalytics != null) {
            IosMotion.animateIn(binding.btnViewAnalytics, 140);
        }
        if (binding.btnSimplifyDebts != null) {
            IosMotion.animateIn(binding.btnSimplifyDebts, 170);
        }
        IosMotion.animateIn(binding.rvExpenses, 210);
        IosMotion.animateIn(binding.fabAddExpense, 250);
    }

    private void setupErrorObserver() {
        groupViewModel.getOperationError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.trim().isEmpty()) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
                groupViewModel.clearOperationError();
            }
        });
    }

    private void observeData() {
        groupViewModel.getGroupById(groupId).observe(getViewLifecycleOwner(), group -> {
            if (group != null) {
                binding.tvGroupName.setText(group.name);
                binding.tvGroupIcon.setText(resolveGroupIcon(group.icon, group.name));
            }
        });

        groupViewModel.getMembersForGroup(groupId).observe(getViewLifecycleOwner(), members -> {
            currentMembers = members != null ? members : new ArrayList<>();
            binding.tvMemberCount.setText(String.valueOf(currentMembers.size()));

            if (currentMembers.isEmpty()) {
                binding.tvMemberNames.setText("No members yet - add some.");
            } else {
                String names = currentMembers.stream()
                    .map(m -> m.name)
                    .collect(Collectors.joining(", "));
                binding.tvMemberNames.setText(names);
            }
        });

        groupViewModel.getExpensesForGroup(groupId).observe(getViewLifecycleOwner(), expenses -> {
            if (expenses == null || expenses.isEmpty()) {
                binding.llNoExpenses.setVisibility(View.VISIBLE);
                binding.rvExpenses.setVisibility(View.GONE);
            } else {
                binding.llNoExpenses.setVisibility(View.GONE);
                binding.rvExpenses.setVisibility(View.VISIBLE);
                expenseAdapter.submitList(expenses, currentMembers);
                binding.rvExpenses.scheduleLayoutAnimation();
            }
        });

        groupViewModel.getTotalSpendForGroup(groupId).observe(getViewLifecycleOwner(), total -> {
            long totalPaise = total != null ? total : 0L;
            binding.tvTotalSpend.setText(MoneyFormatter.formatPaise(totalPaise));
        });
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        binding.btnAddMember.setOnClickListener(v -> showAddMemberDialog());
        IosMotion.applyPressFeedback(binding.btnBack);
        IosMotion.applyPressFeedback(binding.btnAddMember);
        IosMotion.applyPressFeedback(binding.fabAddExpense);

        binding.fabAddExpense.setOnClickListener(v -> {
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(80)
                .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(180)
                    .withEndAction(this::showAddExpenseDialog).start())
                .start();
        });

        if (binding.btnViewAnalytics != null) {
            IosMotion.applyPressFeedback(binding.btnViewAnalytics);
            binding.btnViewAnalytics.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putLong("groupId", groupId);
                NavHostFragment.findNavController(this)
                    .navigate(R.id.action_groupDetail_to_analytics, args);
            });
        }

        if (binding.btnSimplifyDebts != null) {
            IosMotion.applyPressFeedback(binding.btnSimplifyDebts);
            binding.btnSimplifyDebts.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putLong("groupId", groupId);
                NavHostFragment.findNavController(this)
                    .navigate(R.id.action_groupDetail_to_debtSimplification, args);
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
                EditText etName = dialogView.findViewById(R.id.et_member_name);
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
            Snackbar.make(binding.getRoot(), "Add members first.", Snackbar.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_expense, null);

        Spinner spinnerPaidBy = dialogView.findViewById(R.id.spinner_paid_by);
        List<String> memberNames = currentMembers.stream().map(m -> m.name).collect(Collectors.toList());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            requireContext(), android.R.layout.simple_spinner_item, memberNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPaidBy.setAdapter(adapter);

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Expense")
            .setView(dialogView)
            .setPositiveButton("Split Equally", (dialog, which) -> {
                EditText etTitle = dialogView.findViewById(R.id.et_expense_title);
                EditText etAmount = dialogView.findViewById(R.id.et_expense_amount);

                String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
                String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
                if (title.isEmpty() || amountStr.isEmpty()) {
                    Snackbar.make(binding.getRoot(), "Fill all fields", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double amountRupees = Double.parseDouble(amountStr);
                    long amountPaise = Math.round(amountRupees * 100);
                    if (amountPaise <= 0L) {
                        Snackbar.make(binding.getRoot(), "Amount must be greater than zero.", Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    int selectedIndex = spinnerPaidBy.getSelectedItemPosition();
                    if (selectedIndex < 0 || selectedIndex >= currentMembers.size()) {
                        Snackbar.make(binding.getRoot(), "Select who paid this expense.", Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    long paidByMemberId = currentMembers.get(selectedIndex).id;

                    List<Long> participantIds = currentMembers.stream()
                        .map(member -> member.id)
                        .collect(Collectors.toList());
                    if (participantIds.isEmpty()) {
                        Snackbar.make(binding.getRoot(), "Add members first.", Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    boolean added = groupViewModel.addExpenseEqual(
                        groupId,
                        title,
                        amountPaise,
                        paidByMemberId,
                        participantIds
                    );
                    if (!added) {
                        Snackbar.make(binding.getRoot(), "Could not add expense. Check values.", Snackbar.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Snackbar.make(binding.getRoot(), "Invalid amount", Snackbar.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Snackbar.make(binding.getRoot(), "Something went wrong while adding expense.", Snackbar.LENGTH_SHORT).show();
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

    private String resolveGroupIcon(String icon, String groupName) {
        String safeIcon = icon == null ? "" : icon.trim();
        if (!safeIcon.isEmpty()) {
            return firstCodePoints(safeIcon, 1);
        }

        String safeName = groupName == null ? "" : groupName.trim();
        if (!safeName.isEmpty()) {
            return firstCodePoints(safeName.toUpperCase(Locale.getDefault()), 1);
        }

        return "G";
    }

    private String firstCodePoints(String value, int count) {
        int codePointCount = value.codePointCount(0, value.length());
        int end = value.offsetByCodePoints(0, Math.min(count, codePointCount));
        return value.substring(0, end);
    }
}
