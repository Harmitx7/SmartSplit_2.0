package com.smartsplit.app.ui.group;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.smartsplit.app.data.model.SettlementTransaction;
import com.smartsplit.app.databinding.ItemSimplifiedTransactionBinding;

import java.util.Locale;

/**
 * RecyclerView adapter for simplified debt transactions.
 * Each row shows: [FromName] → [amount] → [ToName]
 * Styled to match Ethereal Ledger glassmorphic card design.
 */
public class SimplifiedTransactionAdapter
        extends ListAdapter<SettlementTransaction, SimplifiedTransactionAdapter.ViewHolder> {

    private static final DiffUtil.ItemCallback<SettlementTransaction> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<SettlementTransaction>() {
            @Override
            public boolean areItemsTheSame(@NonNull SettlementTransaction a, @NonNull SettlementTransaction b) {
                return a.fromMemberId == b.fromMemberId && a.toMemberId == b.toMemberId;
            }

            @Override
            public boolean areContentsTheSame(@NonNull SettlementTransaction a, @NonNull SettlementTransaction b) {
                return a.amountPaise == b.amountPaise
                    && a.fromName.equals(b.fromName)
                    && a.toName.equals(b.toName);
            }
        };

    public SimplifiedTransactionAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSimplifiedTransactionBinding binding = ItemSimplifiedTransactionBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSimplifiedTransactionBinding binding;

        ViewHolder(ItemSimplifiedTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SettlementTransaction tx) {
            binding.tvFromName.setText(tx.fromName);
            binding.tvToName.setText(tx.toName);

            // Convert paise → dollars for display
            double amount = tx.amountPaise / 100.0;
            binding.tvAmount.setText(String.format(Locale.US, "$%.2f", amount));

            // Press animation (0.98 scale — Ethereal Ledger design spec)
            binding.getRoot().setOnClickListener(v -> {
                v.animate()
                    .scaleX(0.97f)
                    .scaleY(0.97f)
                    .setDuration(80)
                    .withEndAction(() -> v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(160)
                        .start())
                    .start();
            });
        }
    }
}
