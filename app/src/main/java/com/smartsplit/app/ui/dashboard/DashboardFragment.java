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
import com.smartsplit.app.databinding.FragmentDashboardBinding;
import com.smartsplit.app.ui.viewmodel.AuthViewModel;
import com.smartsplit.app.ui.viewmodel.GroupViewModel;

/**
 * Dashboard fragment — shows all the user's groups.
 * Primary entry point after login.
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
    }

    private void setupRecyclerView() {
        groupAdapter = new GroupAdapter(group -> {
            // Navigate to GroupDetail passing the group's ID
            Bundle args = new Bundle();
            args.putLong("groupId", group.id);
            Navigation.findNavController(requireView()).navigate(R.id.action_dashboard_to_groupDetail, args);
        });

        binding.rvGroups.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvGroups.setAdapter(groupAdapter);
    }

    private void observeGroups() {
        groupViewModel.getAllGroups().observe(getViewLifecycleOwner(), groups -> {
            groupAdapter.submitList(groups);
            if (groups != null && groups.isEmpty()) {
                binding.llEmpty.setVisibility(View.VISIBLE);
                binding.rvGroups.setVisibility(View.GONE);
            } else {
                binding.llEmpty.setVisibility(View.GONE);
                binding.rvGroups.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupFab() {
        binding.btnCreateGroup.setOnClickListener(v -> showCreateGroupDialog());
        binding.fabAdd.setOnClickListener(v -> showCreateGroupDialog());
    }

    private void updateGreeting() {
        FirebaseUser user = authViewModel.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            String email = user.getEmail();
            String displayName = email.contains("@") ? email.substring(0, email.indexOf("@")) : email;
            // Removed tvUserEmail as it's no longer in the glassmorphism layout
        }
    }

    private void showCreateGroupDialog() {
        View dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_create_group, null);

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Create Group")
            .setView(dialogView)
            .setPositiveButton("Create", (dialog, which) -> {
                TextInputEditText etName = dialogView.findViewById(R.id.et_group_name);
                TextInputEditText etIcon = dialogView.findViewById(R.id.et_group_icon);

                String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                String icon = etIcon.getText() != null ? etIcon.getText().toString().trim() : "🏠";

                if (!name.isEmpty()) {
                    FirebaseUser user = authViewModel.getCurrentUser();
                    String uid = user != null ? user.getUid() : "local";
                    groupViewModel.createGroup(name, icon.isEmpty() ? "🏠" : icon, uid);
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
