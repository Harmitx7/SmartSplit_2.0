package com.smartsplit.app.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.smartsplit.app.R;
import com.smartsplit.app.databinding.ActivityMainBinding;
import com.smartsplit.app.ui.auth.AuthActivity;
import com.smartsplit.app.ui.nav.FluidBottomNavigation;

import java.util.ArrayList;
import java.util.List;

/**
 * Main container activity.
 * Hosts the Navigation Component with the Pro Max Fluid Drop Navigation Bar.
 * All screens (Dashboard, Balances, Analytics, Profile) are Fragments within this activity.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    // Ordered to match bottom_nav_menu.xml tab order
    private static final int[] MENU_IDS = {
        R.id.dashboardFragment,
        R.id.balancesFragment,
        R.id.analyticsFragment,
        R.id.profileFragment
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Guard: if user somehow lands here unauthenticated, redirect to auth
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        // ── NavController ─────────────────────────────────────────────────────
        NavHostFragment navHostFragment = (NavHostFragment)
            getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment == null) return;
        navController = navHostFragment.getNavController();

        // ── Build tab list matching menu order ─────────────────────────────────
        List<FluidBottomNavigation.TabItem> tabs = new ArrayList<>();
        tabs.add(new FluidBottomNavigation.TabItem(R.drawable.ic_nav_groups,  getString(R.string.nav_groups),   R.id.dashboardFragment));
        tabs.add(new FluidBottomNavigation.TabItem(R.drawable.ic_nav_balances, getString(R.string.nav_balances), R.id.balancesFragment));
        tabs.add(new FluidBottomNavigation.TabItem(R.drawable.ic_nav_analytics, getString(R.string.nav_analytics), R.id.analyticsFragment));
        tabs.add(new FluidBottomNavigation.TabItem(R.drawable.ic_nav_profile, getString(R.string.nav_profile),  R.id.profileFragment));

        binding.fluidBottomNav.setTabs(tabs);

        // ── Tab → NavController bridge ─────────────────────────────────────────
        binding.fluidBottomNav.setOnTabSelectedListener((tabIndex, menuItemId) -> {
            // Navigate, clearing back stack to avoid deep stacking on tab switch
            navController.navigate(menuItemId,
                null,
                new androidx.navigation.NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setRestoreState(true)
                    .setPopUpTo(navController.getGraph().getStartDestinationId(),
                        false, true)
                    .build()
            );
        });

        // ── NavController → FluidNav sync (hardware back / deep links) ───────
        navController.addOnDestinationChangedListener(
            (controller, destination, arguments) -> syncFluidNav(destination)
        );

        // FluidBottomNavigation handles its own entrance animation internally.
        // (No external animateNavBar call needed — it would conflict with indicator positioning.)
    }

    /**
     * Keeps the fluid nav indicator in sync when the back stack changes
     * without the user tapping a tab.
     */
    private void syncFluidNav(NavDestination destination) {
        int destId = destination.getId();
        for (int i = 0; i < MENU_IDS.length; i++) {
            if (MENU_IDS[i] == destId) {
                binding.fluidBottomNav.setSelectedTab(i);
                return;
            }
        }
    }
}
