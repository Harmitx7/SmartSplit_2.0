package com.smartsplit.app.ui.dashboard;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.smartsplit.app.core.MoneyFormatter;
import com.smartsplit.app.data.model.GroupSummary;
import com.smartsplit.app.databinding.ItemGroupBinding;
import com.smartsplit.app.ui.utils.TouchPhysics;

import java.util.Locale;

/**
 * Adapter for dashboard group cards.
 */
public class GroupAdapter extends ListAdapter<GroupSummary, GroupAdapter.GroupViewHolder> {

    public interface OnGroupClickListener {
        void onGroupClick(GroupSummary group);
    }

    private final OnGroupClickListener listener;
    private int lastAnimatedPosition = -1;

    public GroupAdapter(OnGroupClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<GroupSummary> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<GroupSummary>() {
            @Override
            public boolean areItemsTheSame(@NonNull GroupSummary oldItem, @NonNull GroupSummary newItem) {
                return oldItem.groupId == newItem.groupId;
            }

            @Override
            public boolean areContentsTheSame(@NonNull GroupSummary oldItem, @NonNull GroupSummary newItem) {
                return oldItem.memberCount == newItem.memberCount
                    && oldItem.totalSpendPaise == newItem.totalSpendPaise
                    && safeEquals(oldItem.name, newItem.name)
                    && safeEquals(oldItem.icon, newItem.icon);
            }

            private boolean safeEquals(String a, String b) {
                return a == null ? b == null : a.equals(b);
            }
        };

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGroupBinding binding = ItemGroupBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false
        );
        return new GroupViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
        animateRow(holder, position);
    }

    private void animateRow(@NonNull GroupViewHolder holder, int position) {
        if (position <= lastAnimatedPosition) {
            holder.itemView.setAlpha(1f);
            holder.itemView.setTranslationY(0f);
            return;
        }

        holder.itemView.setAlpha(0f);
        holder.itemView.setTranslationY(26f);
        holder.itemView.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(Math.min(position, 6) * 22L)
            .setDuration(380)
            .start();
        lastAnimatedPosition = position;
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        private final ItemGroupBinding binding;

        GroupViewHolder(ItemGroupBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(GroupSummary group, OnGroupClickListener listener) {
            binding.tvGroupName.setText(group.name);
            binding.tvGroupIcon.setText(resolveIcon(group));
            binding.tvMemberCount.setText(formatMemberCount(group.memberCount));
            binding.tvTotalDebt.setText(MoneyFormatter.formatPaise(group.totalSpendPaise));
            
            // Apply 2026 Pro Max Spring Physics to the whole card
            TouchPhysics.addSpringPressEffect(binding.getRoot());
            
            binding.getRoot().setOnClickListener(v -> listener.onGroupClick(group));
        }

        private String formatMemberCount(long memberCount) {
            if (memberCount == 1L) {
                return "1 member";
            }
            return String.format(Locale.getDefault(), "%d members", memberCount);
        }

        private String resolveIcon(GroupSummary group) {
            String icon = group.icon == null ? "" : group.icon.trim();
            if (!icon.isEmpty()) {
                return firstCodePoints(icon, 1);
            }

            String name = group.name == null ? "" : group.name.trim();
            if (!name.isEmpty()) {
                return firstCodePoints(name.toUpperCase(Locale.getDefault()), 1);
            }

            return "G";
        }

        private String firstCodePoints(String value, int count) {
            int codePointCount = value.codePointCount(0, value.length());
            int end = value.offsetByCodePoints(0, Math.min(count, codePointCount));
            return value.substring(0, end);
        }
    }
}
