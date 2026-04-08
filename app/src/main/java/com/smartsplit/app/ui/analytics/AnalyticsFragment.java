package com.smartsplit.app.ui.analytics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.smartsplit.app.R;
import com.smartsplit.app.core.MoneyFormatter;
import com.smartsplit.app.data.db.AppDatabase;
import com.smartsplit.app.data.model.Group;
import com.smartsplit.app.databinding.FragmentAnalyticsBinding;
import com.smartsplit.app.ui.motion.IosMotion;
import com.smartsplit.app.ui.viewmodel.GroupViewModel;
import com.smartsplit.app.ui.utils.TouchPhysics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Analytics screen with category pie chart and spend summary.
 */
public class AnalyticsFragment extends Fragment {

    private FragmentAnalyticsBinding binding;
    private AnalyticsViewModel viewModel;
    private GroupViewModel groupViewModel;
    private long loadedGroupId = -1L;

    private static final int[] CATEGORY_COLORS = {
        Color.parseColor("#D7263D"), // Crimson Red (Primary)
        Color.parseColor("#013A63"), // Deep Navy
        Color.parseColor("#4361EE"), // Royal Blue
        Color.parseColor("#FFD700"), // Accent Gold
        Color.parseColor("#3F37C9")  // Deep Slate
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAnalyticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AnalyticsViewModel.class);
        groupViewModel = new ViewModelProvider(requireActivity()).get(GroupViewModel.class);
        setupPieChart();
        setupBackButton();
        setupMotion();

        viewModel.getAnalyticsData().observe(getViewLifecycleOwner(), data -> {
            if (data == null || data.categoryTotals.isEmpty()) {
                showEmptyState(getString(R.string.no_expenses_analytics));
            } else {
                showChartData(data);
            }
        });

        final long requestedGroupId = resolveRequestedGroupId();
        groupViewModel.getAllGroups().observe(getViewLifecycleOwner(), groups -> {
            if (groups == null || groups.isEmpty()) {
                showEmptyState(getString(R.string.no_groups_subtitle));
                return;
            }

            if (requestedGroupId > 0L && !containsGroup(groups, requestedGroupId)) {
                showEmptyState(getString(R.string.analytics_invalid_group));
                return;
            }

            long targetGroupId = requestedGroupId > 0L ? requestedGroupId : groups.get(0).id;
            loadGroup(targetGroupId);
        });
    }

    private void setupPieChart() {
        PieChart chart = binding.pieChart;
        chart.setBackgroundColor(Color.TRANSPARENT);
        chart.setHoleColor(Color.TRANSPARENT);
        chart.setTransparentCircleColor(Color.TRANSPARENT);
        chart.setHoleRadius(65f);
        chart.setTransparentCircleRadius(0f);
        chart.setDrawCenterText(true);
        chart.setCenterText("SPEND\nDISTRIBUTION");
        chart.setCenterTextColor(Color.WHITE);
        chart.setCenterTextSize(14f);
        chart.setCenterTextTypeface(null); // Will default to bold via style if possible
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setEntryLabelColor(Color.WHITE);
        chart.setEntryLabelTextSize(11f);
        chart.setRotationEnabled(true);
        chart.setHighlightPerTapEnabled(true);
        
        // Disable slice labels to keep the donut clean if desired, 
        // but we'll keep them for clarity with percent formatter
        chart.setDrawEntryLabels(true);
    }

    private void showChartData(AnalyticsViewModel.AnalyticsData data) {
        if (data.totalSpendPaise <= 0L) {
            showEmptyState(getString(R.string.no_expenses_analytics));
            return;
        }

        binding.emptyState.setVisibility(View.GONE);
        binding.contentGroup.setVisibility(View.VISIBLE);

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        int colorIndex = 0;
        for (Map.Entry<String, Long> entry : data.categoryTotals.entrySet()) {
            float percent = (float) entry.getValue() / (float) data.totalSpendPaise * 100f;
            entries.add(new PieEntry(percent, entry.getKey()));
            colors.add(CATEGORY_COLORS[colorIndex % CATEGORY_COLORS.length]);
            colorIndex++;
        }

        PieDataSet dataSet = new PieDataSet(entries, "Spending");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(4f);
        dataSet.setSelectionShift(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new PercentFormatter(binding.pieChart));

        PieData pieData = new PieData(dataSet);
        binding.pieChart.setData(pieData);
        binding.pieChart.animateY(1400, Easing.EaseOutBack);
        binding.pieChart.invalidate();

        // Reveal effect
        binding.pieChart.setScaleX(0.9f);
        binding.pieChart.setScaleY(0.9f);
        binding.pieChart.setAlpha(0f);
        binding.pieChart.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(600)
            .start();

        binding.tvTotalSpend.setText(MoneyFormatter.formatPaise(data.totalSpendPaise));
        binding.tvTopCategory.setText(getTopCategory(data.categoryTotals));

        // Staggered reveal for summary cards
        binding.cardTotalSpend.setAlpha(0f);
        binding.cardTotalSpend.setTranslationY(60f);
        binding.cardTotalSpend.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(400)
            .setInterpolator(new android.view.animation.DecelerateInterpolator())
            .start();
    }

    private void showEmptyState(String message) {
        binding.contentGroup.setVisibility(View.GONE);
        binding.emptyState.setVisibility(View.VISIBLE);
        binding.tvEmptyMessage.setText(message);
        IosMotion.animateIn(binding.emptyState, 60);
    }

    private void setupBackButton() {
        binding.btnBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        TouchPhysics.addSpringPressEffect(binding.btnBack);
    }

    private void setupMotion() {
        IosMotion.animateIn(binding.header, 0);
        IosMotion.animateIn(binding.contentGroup, 80);
        
        // Apply 2026 Pro Max Spring Physics
        TouchPhysics.addSpringPressEffect(binding.cardTotalSpend);
    }

    private void loadGroup(long groupId) {
        if (groupId <= 0L || loadedGroupId == groupId) {
            return;
        }
        loadedGroupId = groupId;
        AppDatabase db = AppDatabase.getInstance(requireContext());
        viewModel.loadForGroup(db, groupId);
    }

    private long resolveRequestedGroupId() {
        Bundle args = getArguments();
        if (args == null) {
            return -1L;
        }
        return AnalyticsFragmentArgs.fromBundle(args).getGroupId();
    }

    private boolean containsGroup(List<Group> groups, long groupId) {
        for (Group group : groups) {
            if (group != null && group.id == groupId) {
                return true;
            }
        }
        return false;
    }

    private String getTopCategory(Map<String, Long> categoryTotals) {
        String winner = getString(R.string.category_other);
        long max = Long.MIN_VALUE;
        for (Map.Entry<String, Long> entry : categoryTotals.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                winner = entry.getKey();
            }
        }
        return winner;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
