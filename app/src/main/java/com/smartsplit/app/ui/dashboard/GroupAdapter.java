package com.smartsplit.app.ui.dashboard;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.smartsplit.app.data.model.Group;
import com.smartsplit.app.databinding.ItemGroupBinding;

/**
 * RecyclerView Adapter for the Dashboard group list.
 * Extends ListAdapter for efficient DiffUtil-based updates.
 */
public class GroupAdapter extends ListAdapter<Group, GroupAdapter.GroupViewHolder> {

    public interface OnGroupClickListener {
        void onGroupClick(Group group);
    }

    private final OnGroupClickListener listener;

    public GroupAdapter(OnGroupClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Group> DIFF_CALLBACK = new DiffUtil.ItemCallback<Group>() {
        @Override
        public boolean areItemsTheSame(@NonNull Group oldItem, @NonNull Group newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Group oldItem, @NonNull Group newItem) {
            return oldItem.name.equals(newItem.name) && oldItem.icon.equals(newItem.icon);
        }
    };

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGroupBinding binding = ItemGroupBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new GroupViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = getItem(position);
        holder.bind(group, listener);
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        private final ItemGroupBinding binding;

        GroupViewHolder(ItemGroupBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Group group, OnGroupClickListener listener) {
            binding.tvGroupName.setText(group.name);
            binding.tvMemberCount.setText("Tap to view expenses");
            binding.tvTotalDebt.setText("₹0");  // VERIFY: wire to actual total spend LiveData per group

            binding.getRoot().setOnClickListener(v -> listener.onGroupClick(group));
        }
    }
}
