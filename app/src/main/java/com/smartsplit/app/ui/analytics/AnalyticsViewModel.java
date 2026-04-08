package com.smartsplit.app.ui.analytics;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.smartsplit.app.data.dao.ExpenseDao;
import com.smartsplit.app.data.db.AppDatabase;
import com.smartsplit.app.data.model.Expense;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Application;

/**
 * AnalyticsViewModel — provides aggregated data for the Analytics screen.
 *
 * Data pipeline:
 *   Room DB → getExpensesForGroupSync() → compute category map → LiveData<Map<String, Long>>
 *
 * All DB reads happen on a background executor; results posted back to main thread via MediatorLiveData.
 */
public class AnalyticsViewModel extends ViewModel {

    // ── Inner data container ──────────────────────────────────────────────
    public static class AnalyticsData {
        /** Category name → total amount in paise */
        public Map<String, Long> categoryTotals = new HashMap<>();
        /** Member name → total amount paid in paise */
        public Map<String, Long> memberSpend = new HashMap<>();
        /** Total group spend in paise */
        public long totalSpendPaise;
    }

    private final MediatorLiveData<AnalyticsData> analyticsData = new MediatorLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Call this once from the Fragment to kick off data loading.
     * Runs DB read on background thread, emits result to LiveData on completion.
     */
    public void loadForGroup(AppDatabase db, long groupId) {
        executor.execute(() -> {
            List<Expense> expenses = db.expenseDao().getExpensesForGroupSync(groupId);
            AnalyticsData result = buildAnalytics(expenses);
            analyticsData.postValue(result);
        });
    }

    public LiveData<AnalyticsData> getAnalyticsData() {
        return analyticsData;
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private AnalyticsData buildAnalytics(List<Expense> expenses) {
        AnalyticsData data = new AnalyticsData();
        if (expenses == null || expenses.isEmpty()) return data;

        for (Expense e : expenses) {
            // Categorize by keyword matching on title
            String category = inferCategory(e.title);
            Long current = data.categoryTotals.getOrDefault(category, 0L);
            data.categoryTotals.put(category, current + e.amountPaise);
            data.totalSpendPaise += e.amountPaise;
        }

        return data;
    }

    /**
     * Simple keyword-based category inference.
     * In a production app, the user would be asked to assign a category at creation time.
     */
    private String inferCategory(String title) {
        if (title == null) return "Other";
        String lower = title.toLowerCase();
        if (lower.contains("food") || lower.contains("pizza") || lower.contains("grocery")
                || lower.contains("dinner") || lower.contains("lunch") || lower.contains("restaurant")
                || lower.contains("cafe") || lower.contains("breakfast")) {
            return "Food";
        }
        if (lower.contains("electricity") || lower.contains("water") || lower.contains("gas")
                || lower.contains("wifi") || lower.contains("internet") || lower.contains("bill")) {
            return "Bills";
        }
        if (lower.contains("uber") || lower.contains("transport") || lower.contains("bus")
                || lower.contains("metro") || lower.contains("taxi") || lower.contains("fuel")) {
            return "Transport";
        }
        if (lower.contains("movie") || lower.contains("concert") || lower.contains("game")
                || lower.contains("netflix") || lower.contains("spotify") || lower.contains("entertainment")) {
            return "Entertainment";
        }
        return "Other";
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
