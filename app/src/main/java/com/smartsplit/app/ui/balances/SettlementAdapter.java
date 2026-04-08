package com.smartsplit.app.ui.balances;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.smartsplit.app.data.model.SettlementTransaction;
import com.smartsplit.app.databinding.ItemSettlementBinding;

/**
 * Adapter for the Settlement transactions list in BalancesFragment.
 */
public class SettlementAdapter extends ListAdapter<SettlementTransaction, SettlementAdapter.SettlementViewHolder> {

    public SettlementAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<SettlementTransaction> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<SettlementTransaction>() {
            @Override
            public boolean areItemsTheSame(@NonNull SettlementTransaction o, @NonNull SettlementTransaction n) {
                return o.fromMemberId == n.fromMemberId && o.toMemberId == n.toMemberId;
            }

            @Override
            public boolean areContentsTheSame(@NonNull SettlementTransaction o, @NonNull SettlementTransaction n) {
                return o.amountPaise == n.amountPaise;
            }
        };

    @NonNull
    @Override
    public SettlementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSettlementBinding binding = ItemSettlementBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new SettlementViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SettlementViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class SettlementViewHolder extends RecyclerView.ViewHolder {
        private final ItemSettlementBinding binding;

        SettlementViewHolder(ItemSettlementBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SettlementTransaction transaction) {
            binding.tvFromName.setText(transaction.fromName);
            binding.tvToName.setText(transaction.toName);
            binding.tvAmount.setText(transaction.getFormattedAmount());
        }
    }
}
