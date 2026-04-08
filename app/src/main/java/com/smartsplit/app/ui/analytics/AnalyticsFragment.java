package com.smartsplit.app.ui.analytics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.airbnb.lottie.LottieAnimationView;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.smartsplit.app.R;
import com.smartsplit.app.data.db.AppDatabase;
import com.smartsplit.app.databinding.FragmentAnalyticsBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AnalyticsFragment — displays a PieChart breakdown of group spending by category.
 *
 * Design: Ethereal Ledger glassmorphism
 *   - Deep space #0c0e12 background
 *   - PieChart styled with brand colors (primary/secondary/tertiary/error)
 *   - Empty state driven by Lottie animation
 *   - Glass stat cards for Total Spend and Top Spender
 */
public class AnalyticsFragment extends Fragment {

    private FragmentAnalyticsBinding binding;
    private AnalyticsViewModel viewModel;

    // Category color palette — Ethereal Ledger brand colors
    private static final int[] CATEGORY_COLORS = {
        Color.parseColor("#F382FF"),  // primary: Food
        Color.parseColor("#3ADFFA"),  // secondary: Bills
        Color.parseColor("#C4FFCD"),  // tertiary: Transport
        Color.parseColor("#FF6E84"),  // error: Entertainment
        Color.parseColor("#AAABB0"),  // on_surface_variant: Other
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

        // Get groupId from arguments
        long groupId = AnalyticsFragmentArgs.fromBundle(getArguments()).getGroupId();

        setupPieChart();
        setupBackButton();

        // Load data
        AppDatabase db = AppDatabase.getInstance(requireContext());
        viewModel.loadForGroup(db, groupId == -1 ? 1L : groupId);

        viewModel.getAnalyticsData().observe(getViewLifecycleOwner(), data -> {
            if (data == null || data.categoryTotals.isEmpty()) {
                showEmptyState();
            } else {
                showChartData(data);
            }
        });
    }

    // ── PieChart Setup ────────────────────────────────────────────────────

    private void setupPieChart() {
        PieChart chart = binding.pieChart;

        // Transparent deep-space background
        chart.setBackgroundColor(Color.TRANSPARENT);
        chart.setHoleColor(Color.TRANSPARENT);
        chart.setTransparentCircleColor(Color.parseColor("#0C0E12"));
        chart.setTransparentCircleAlpha(30);
        chart.setHoleRadius(62f);
        chart.setTransparentCircleRadius(67f);

        // Chart appearance
        chart.setDrawCenterText(true);
        chart.setCenterText("Spend\nBreakdown");
        chart.setCenterTextColor(Color.parseColor("#F6F6FC"));
        chart.setCenterTextSize(14f);

        // Hide description & legend default style → custom legend below
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setEntryLabelColor(Color.parseColor("#F6F6FC"));
        chart.setEntryLabelTextSize(11f);

        // Rotation animation
        chart.setRotationEnabled(true);
        chart.setHighlightPerTapEnabled(true);
    }

    private void showChartData(AnalyticsViewModel.AnalyticsData data) {
        binding.emptyState.setVisibility(View.GONE);
        binding.contentGroup.setVisibility(View.VISIBLE);

        // Build PieEntries
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        int colorIdx = 0;
        for (Map.Entry<String, Long> entry : data.categoryTotals.entrySet()) {
            float percent = (float) entry.getValue() / data.totalSpendPaise * 100f;
            entries.add(new PieEntry(percent, entry.getKey()));
            colors.add(CATEGORY_COLORS[colorIdx % CATEGORY_COLORS.length]);
            colorIdx++;
        }

        PieDataSet dataSet = new PieDataSet(entries, "Spending");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(8f);
        dataSet.setValueTextColor(Color.parseColor("#F6F6FC"));
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new PercentFormatter(binding.pieChart));

        PieData pieData = new PieData(dataSet);
        binding.pieChart.setData(pieData);

        // Animate in with spin + fade
        binding.pieChart.animateY(900, Easing.EaseInOutCubic);
        binding.pieChart.invalidate();

        // Update stat cards
        double totalDollars = data.totalSpendPaise / 100.0;
        binding.tvTotalSpend.setText(String.format("$%.2f", totalDollars));

        // Animate stat cards sliding up
        binding.cardTotalSpend.setAlpha(0f);
        binding.cardTotalSpend.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(300).start();
        binding.cardTotalSpend.setTranslationY(40f);
    }

    private void showEmptyState() {
        binding.contentGroup.setVisibility(View.GONE);
        binding.emptyState.setVisibility(View.VISIBLE);
        // Lottie will autoplay from XML attribute
    }

    private void setupBackButton() {
        binding.btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
