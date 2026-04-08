package com.smartsplit.app.ui.group;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smartsplit.app.data.model.Expense;
import com.smartsplit.app.data.model.Member;
import com.smartsplit.app.databinding.ItemExpenseBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter for the Expenses RecyclerView in GroupDetailFragment.
 * Does NOT extend ListAdapter because it needs both expenses + members context.
 */
public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenses = new ArrayList<>();
    private Map<Long, String> memberNameMap = new HashMap<>();

    public void submitList(List<Expense> newExpenses, List<Member> members) {
        this.expenses = newExpenses != null ? newExpenses : new ArrayList<>();
        this.memberNameMap.clear();
        if (members != null) {
            for (Member m : members) {
                memberNameMap.put(m.id, m.name);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemExpenseBinding binding = ItemExpenseBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new ExpenseViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        holder.bind(expenses.get(position), memberNameMap);
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private final ItemExpenseBinding binding;

        ExpenseViewHolder(ItemExpenseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Expense expense, Map<Long, String> memberNameMap) {
            binding.tvExpenseTitle.setText(expense.title);
            binding.tvExpenseAmount.setText(
                String.format("₹%.2f", expense.amountPaise / 100.0));

            String payerName = memberNameMap.getOrDefault(expense.paidByMemberId, "Unknown");
            binding.tvPaidBy.setText("Paid by " + payerName);
        }
    }
}
