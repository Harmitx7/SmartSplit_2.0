package com.smartsplit.app.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.smartsplit.app.R;
import com.smartsplit.app.core.MoneyFormatter;
import com.smartsplit.app.data.model.GroupSummary;
import com.smartsplit.app.databinding.FragmentProfileBinding;
import com.smartsplit.app.ui.auth.AuthActivity;
import com.smartsplit.app.ui.motion.IosMotion;
import com.smartsplit.app.ui.viewmodel.AuthViewModel;
import com.smartsplit.app.ui.viewmodel.GroupViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Profile screen with account details, app stats and sign-out action.
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private AuthViewModel authViewModel;
    private GroupViewModel groupViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        groupViewModel = new ViewModelProvider(requireActivity()).get(GroupViewModel.class);

        bindProfileInfo();
        bindDynamicStats();
        setupActions();
        setupMotion();
    }

    private void bindProfileInfo() {
        FirebaseUser user = authViewModel.getCurrentUser();
        if (user == null) {
            binding.tvProfileName.setText(getString(R.string.profile_guest));
            binding.tvProfileEmail.setText(getString(R.string.profile_guest_subtitle));
            return;
        }

        String email = user.getEmail() == null ? getString(R.string.profile_no_email) : user.getEmail();
        String displayName = user.getDisplayName();
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = deriveNameFromEmail(email);
        }

        binding.tvProfileName.setText(displayName);
        binding.tvProfileEmail.setText(email);
    }

    private void bindDynamicStats() {
        groupViewModel.getGroupSummaries().observe(getViewLifecycleOwner(), summaries -> {
            List<GroupSummary> safe = summaries != null ? summaries : new ArrayList<>();

            long totalSpend = 0L;
            long totalMembers = 0L;
            for (GroupSummary summary : safe) {
                if (summary == null) {
                    continue;
                }
                totalSpend += Math.max(0L, summary.totalSpendPaise);
                totalMembers += Math.max(0L, summary.memberCount);
            }

            animateText(binding.tvProfileGroupCount, String.valueOf(safe.size()));
            animateText(binding.tvProfileMemberCount, String.valueOf(totalMembers));
            animateText(binding.tvProfileTotalSpend, MoneyFormatter.formatPaise(totalSpend));
        });
    }

    private void setupActions() {
        binding.btnEditName.setOnClickListener(v -> showEditNameDialog());
        binding.btnSignOut.setOnClickListener(v -> signOutAndExit());
        IosMotion.applyPressFeedback(binding.btnEditName);
        IosMotion.applyPressFeedback(binding.btnSignOut);
    }

    private void setupMotion() {
        IosMotion.animateIn(binding.tvProfileTitle, 0);
        IosMotion.animateIn(binding.cardProfileHeader, 60);
        IosMotion.animateIn(binding.cardProfileStats, 120);
        IosMotion.animateIn(binding.btnEditName, 170);
        IosMotion.animateIn(binding.btnSignOut, 210);
    }

    private void showEditNameDialog() {
        FirebaseUser user = authViewModel.getCurrentUser();
        if (user == null) {
            Snackbar.make(binding.getRoot(), R.string.profile_login_required, Snackbar.LENGTH_SHORT).show();
            return;
        }

        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setText(binding.tvProfileName.getText());
        input.setSelection(input.getText() != null ? input.getText().length() : 0);
        input.setHint(R.string.profile_name_hint);
        int pad = (int) (16 * requireContext().getResources().getDisplayMetrics().density);
        input.setPadding(pad, pad, pad, pad);

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.profile_edit_name)
            .setView(input)
            .setPositiveButton(R.string.save, (dialog, which) -> {
                String newName = input.getText() == null ? "" : input.getText().toString().trim();
                if (newName.isEmpty()) {
                    Snackbar.make(binding.getRoot(), R.string.profile_name_empty, Snackbar.LENGTH_SHORT).show();
                    return;
                }

                UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build();

                user.updateProfile(request)
                    .addOnSuccessListener(unused -> animateText(binding.tvProfileName, newName))
                    .addOnFailureListener(e -> Snackbar.make(
                        binding.getRoot(),
                        getString(R.string.profile_update_failed, e.getMessage()),
                        Snackbar.LENGTH_LONG
                    ).show());
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void signOutAndExit() {
        authViewModel.signOut();
        Intent intent = new Intent(requireContext(), AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private String deriveNameFromEmail(String email) {
        int at = email.indexOf("@");
        String local = at > 0 ? email.substring(0, at) : email;
        if (local.isEmpty()) {
            return getString(R.string.profile_guest);
        }
        return local.substring(0, 1).toUpperCase(Locale.getDefault()) + local.substring(1);
    }

    private void animateText(View textView, String value) {
        textView.animate()
            .alpha(0f)
            .setDuration(110)
            .withEndAction(() -> {
                if (textView == binding.tvProfileGroupCount) {
                    binding.tvProfileGroupCount.setText(value);
                } else if (textView == binding.tvProfileMemberCount) {
                    binding.tvProfileMemberCount.setText(value);
                } else if (textView == binding.tvProfileTotalSpend) {
                    binding.tvProfileTotalSpend.setText(value);
                } else if (textView == binding.tvProfileName) {
                    binding.tvProfileName.setText(value);
                }
                textView.animate().alpha(1f).setDuration(180).start();
            })
            .start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
