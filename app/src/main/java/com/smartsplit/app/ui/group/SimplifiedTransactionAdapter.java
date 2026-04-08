package com.smartsplit.app.ui.group;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.smartsplit.app.core.MoneyFormatter;
import com.smartsplit.app.data.model.SettlementTransaction;
import com.smartsplit.app.databinding.ItemSimplifiedTransactionBinding;

import java.util.Locale;

/**
 * Adapter for simplified debt transactions.
 */
public class SimplifiedTransactionAdapter
    extends ListAdapter<SettlementTransaction, SimplifiedTransactionAdapter.ViewHolder> {

    private int lastAnimatedPosition = -1;

    private static final DiffUtil.ItemCallback<SettlementTransaction> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<SettlementTransaction>() {
            @Override
            public boolean areItemsTheSame(@NonNull SettlementTransaction oldItem,
                                           @NonNull SettlementTransaction newItem) {
                return oldItem.fromMemberId == newItem.fromMemberId
                    && oldItem.toMemberId == newItem.toMemberId;
            }

            @Override
            public boolean areContentsTheSame(@NonNull SettlementTransaction oldItem,
                                              @NonNull SettlementTransaction newItem) {
                return oldItem.amountPaise == newItem.amountPaise
                    && oldItem.fromName.equals(newItem.fromName)
                    && oldItem.toName.equals(newItem.toName);
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
        animateRow(holder, position);
    }

    private void animateRow(@NonNull ViewHolder holder, int position) {
        if (position <= lastAnimatedPosition) {
            holder.itemView.setAlpha(1f);
            holder.itemView.setTranslationY(0f);
            return;
        }

        holder.itemView.setAlpha(0f);
        holder.itemView.setTranslationY(24f);
        holder.itemView.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(Math.min(position, 8) * 20L)
            .setDuration(360)
            .start();
        lastAnimatedPosition = position;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSimplifiedTransactionBinding binding;

        ViewHolder(ItemSimplifiedTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SettlementTransaction transaction) {
            binding.tvFromName.setText(transaction.fromName);
            binding.tvToName.setText(transaction.toName);
            binding.tvAmount.setText(MoneyFormatter.formatPaise(transaction.amountPaise));
            binding.tvFromInitial.setText(extractInitial(transaction.fromName));
            binding.tvToInitial.setText(extractInitial(transaction.toName));

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

        private String extractInitial(String name) {
            if (name == null) {
                return "?";
            }
            String trimmed = name.trim();
            if (trimmed.isEmpty()) {
                return "?";
            }
            return trimmed.substring(0, 1).toUpperCase(Locale.getDefault());
        }
    }
}
