package com.smartsplit.app.ui.group;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smartsplit.app.R;
import com.smartsplit.app.core.MoneyFormatter;
import com.smartsplit.app.data.model.Expense;
import com.smartsplit.app.data.model.Member;
import com.smartsplit.app.databinding.ItemExpenseBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Adapter for expenses list in GroupDetail.
 */
public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenses = new ArrayList<>();
    private final Map<Long, String> memberNameMap = new HashMap<>();
    private int lastAnimatedPosition = -1;

    public void submitList(List<Expense> newExpenses, List<Member> members) {
        this.expenses = newExpenses != null ? newExpenses : new ArrayList<>();
        memberNameMap.clear();
        if (members != null) {
            for (Member member : members) {
                memberNameMap.put(member.id, member.name);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemExpenseBinding binding = ItemExpenseBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ExpenseViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        holder.bind(expenses.get(position), memberNameMap);
        animateRow(holder, position);
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    private void animateRow(@NonNull ExpenseViewHolder holder, int position) {
        if (position <= lastAnimatedPosition) {
            holder.itemView.setAlpha(1f);
            holder.itemView.setTranslationY(0f);
            return;
        }

        holder.itemView.setAlpha(0f);
        holder.itemView.setTranslationY(20f);
        holder.itemView.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(Math.min(position, 7) * 18L)
            .setDuration(340)
            .start();
        lastAnimatedPosition = position;
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private final ItemExpenseBinding binding;

        ExpenseViewHolder(ItemExpenseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Expense expense, Map<Long, String> memberNameMap) {
            binding.tvExpenseTitle.setText(expense.title);
            binding.tvExpenseAmount.setText(MoneyFormatter.formatPaise(expense.amountPaise));
            binding.tvCategoryEmoji.setText(resolveBadge(expense));

            String payerName = memberNameMap.getOrDefault(expense.paidByMemberId, "Unknown");
            binding.tvPaidBy.setText(binding.getRoot().getContext().getString(R.string.paid_by, payerName));

            long createdAt = expense.createdAt > 0L ? expense.createdAt : System.currentTimeMillis();
            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                createdAt,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE
            );
            binding.tvExpenseDate.setText(relativeTime);
        }

        private String resolveBadge(Expense expense) {
            String title = expense.title == null ? "" : expense.title.trim();
            if (!title.isEmpty()) {
                return title.substring(0, 1).toUpperCase(Locale.getDefault());
            }

            String splitType = expense.splitType == null ? "" : expense.splitType.trim();
            if ("CUSTOM".equalsIgnoreCase(splitType)) {
                return "C";
            }

            if ("EQUAL".equalsIgnoreCase(splitType)) {
                return "E";
            }

            return "S";
        }
    }
}
