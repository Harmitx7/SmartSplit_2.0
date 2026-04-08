package com.smartsplit.app.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.smartsplit.app.R;
import com.smartsplit.app.core.MoneyFormatter;
import com.smartsplit.app.databinding.FragmentDashboardBinding;
import com.smartsplit.app.data.model.GroupSummary;
import com.smartsplit.app.ui.motion.IosMotion;
import com.smartsplit.app.ui.viewmodel.AuthViewModel;
import com.smartsplit.app.ui.viewmodel.GroupViewModel;
import com.smartsplit.app.ui.utils.TouchPhysics;

import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard fragment showing all groups.
 */
public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private GroupViewModel groupViewModel;
    private AuthViewModel authViewModel;
    private GroupAdapter groupAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        groupViewModel = new ViewModelProvider(requireActivity()).get(GroupViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        setupRecyclerView();
        observeGroups();
        setupFab();
        updateGreeting();
        setupMotion();
    }

    private void setupRecyclerView() {
        groupAdapter = new GroupAdapter(group -> {
            Bundle args = new Bundle();
            args.putLong("groupId", group.groupId);
            Navigation.findNavController(requireView())
                .navigate(R.id.action_dashboard_to_groupDetail, args);
        });

        binding.rvGroups.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvGroups.setAdapter(groupAdapter);
        IosMotion.applyListLayoutAnimation(binding.rvGroups);
    }

    private void observeGroups() {
        groupViewModel.getGroupSummaries().observe(getViewLifecycleOwner(), summaries -> {
            List<GroupSummary> safeSummaries = summaries != null ? summaries : new ArrayList<>();
            groupAdapter.submitList(safeSummaries);

            boolean showEmpty = safeSummaries.isEmpty();
            binding.llEmpty.setVisibility(showEmpty ? View.VISIBLE : View.GONE);
            binding.rvGroups.setVisibility(showEmpty ? View.GONE : View.VISIBLE);
            if (!showEmpty) {
                binding.rvGroups.scheduleLayoutAnimation();
            }

            long totalSpendPaise = 0L;
            for (GroupSummary summary : safeSummaries) {
                if (summary != null) {
                    totalSpendPaise += Math.max(0L, summary.totalSpendPaise);
                }
            }

            binding.tvDashboardGroupCount.setText(String.valueOf(safeSummaries.size()));
            binding.tvDashboardTotalSpend.setText(MoneyFormatter.formatPaise(totalSpendPaise));
        });
    }

    private void setupFab() {
        binding.btnCreateGroup.setOnClickListener(v -> showCreateGroupDialog());
        binding.fabAdd.setOnClickListener(v -> showCreateGroupDialog());
    }

    private void setupMotion() {
        IosMotion.animateIn(binding.tvDashboardTitle, 0);
        IosMotion.animateIn(binding.tvDashboardSubtitle, 40);
        IosMotion.animateIn(binding.cardSummary, 80);
        IosMotion.animateIn(binding.rowGroupsHeader, 130);
        IosMotion.animateIn(binding.rvGroups, 170);
        IosMotion.animateIn(binding.fabAdd, 240);
        
        // Apply 2026 Pro Max Spring Physics
        TouchPhysics.addSpringPressEffect(binding.cardSummary);
        TouchPhysics.addSpringPressEffect(binding.btnCreateGroup);
        TouchPhysics.addSpringPressEffect(binding.fabAdd);
    }

    private void updateGreeting() {
        FirebaseUser user = authViewModel.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            // Header currently has static text in XML.
        }
    }

    private void showCreateGroupDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_group, null);

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Create Group")
            .setView(dialogView)
            .setPositiveButton("Create", (dialog, which) -> {
                TextInputEditText etName = dialogView.findViewById(R.id.et_group_name);
                TextInputEditText etIcon = dialogView.findViewById(R.id.et_group_icon);

                String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                String icon = etIcon.getText() != null ? etIcon.getText().toString().trim() : "";

                if (!name.isEmpty()) {
                    FirebaseUser user = authViewModel.getCurrentUser();
                    String uid = user != null ? user.getUid() : "local";
                    groupViewModel.createGroup(name, icon.isEmpty() ? "G" : icon, uid);
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
