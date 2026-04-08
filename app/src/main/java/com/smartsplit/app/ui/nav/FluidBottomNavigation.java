package com.smartsplit.app.ui.nav;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import com.smartsplit.app.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Pro Max Fluid Drop Navigation Bar.
 *
 * Architecture:
 *   Layer 0 (bottom) — FluidBackgroundView: custom bezier-cutout canvas shape
 *   Layer 1 (middle) — tabsContainer LinearLayout: icon+label for each inactive tab
 *   Layer 2 (top)    — floatingIndicator FrameLayout: crimson circle that spring-drops
 *
 * The indicator overflows ABOVE the nav bar top edge (requires all parents to have
 * android:clipChildren="false").
 *
 * Coordinate system note:
 *   All TranslationX/TranslationY values are relative to the FluidBottomNavigation itself.
 *   getTabCenterX() walks up the view hierarchy to get correct screen-relative values.
 */
public class FluidBottomNavigation extends FrameLayout {

    /** Callback wired to NavController from MainActivity. */
    public interface OnTabSelectedListener {
        void onTabSelected(int tabIndex, int menuItemId);
    }

    /** Tab data passed in from activity. */
    public static class TabItem {
        public final int iconResId;
        public final String label;
        public final int menuItemId;

        public TabItem(int iconResId, String label, int menuItemId) {
            this.iconResId = iconResId;
            this.label = label;
            this.menuItemId = menuItemId;
        }
    }

    // ─── Constants ────────────────────────────────────────────────────────────

    /** dp size of the floating crimson circle. */
    private static final int INDICATOR_SIZE_DP = 54;
    /**
     * How far the top of the indicator sits above the nav bar's top edge (in dp).
     * Positive = above. This makes it "float" into the bezier cradle.
     */
    private static final int INDICATOR_LIFT_DP = 20;

    private static final PathInterpolator EXPO_EASE_OUT = new PathInterpolator(0.16f, 1f, 0.3f, 1f);

    // ─── Views ────────────────────────────────────────────────────────────────

    private FluidBackgroundView backgroundView;
    private FrameLayout floatingIndicator;
    private ImageView indicatorIconView;
    private LinearLayout tabsContainer;

    // ─── State ────────────────────────────────────────────────────────────────

    private final List<TabItem> tabs = new ArrayList<>();
    private final List<View> tabViews = new ArrayList<>();
    private int selectedIndex = 0;
    private boolean initialLayoutDone = false;
    private OnTabSelectedListener listener;

    // ─── Constructors ─────────────────────────────────────────────────────────

    public FluidBottomNavigation(@NonNull Context context) {
        super(context);
        init();
    }

    public FluidBottomNavigation(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FluidBottomNavigation(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    // ─── Initialization ───────────────────────────────────────────────────────

    private void init() {
        // CRITICAL: Allow the floating indicator to draw outside this view's bounds
        setClipChildren(false);
        setClipToPadding(false);

        // Layer 0: Bezier cutout background
        backgroundView = new FluidBackgroundView(getContext());
        addView(backgroundView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Layer 1: Inactive tab row (icons + labels)
        tabsContainer = new LinearLayout(getContext());
        tabsContainer.setOrientation(LinearLayout.HORIZONTAL);
        tabsContainer.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        // Pad the bottom slightly so labels breathe and top is occupied by the indicator cradle
        tabsContainer.setPadding(0, dp(14), 0, dp(10));
        addView(tabsContainer, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Layer 2: Floating indicator circle
        buildFloatingIndicator();
    }

    private void buildFloatingIndicator() {
        int sizePx = dp(INDICATOR_SIZE_DP);

        floatingIndicator = new FrameLayout(getContext());
        // Use ContextCompat to avoid deprecated getDrawable() and handle null safely
        android.graphics.drawable.Drawable bg = ContextCompat.getDrawable(getContext(), R.drawable.bg_fluid_indicator);
        if (bg != null) floatingIndicator.setBackground(bg);
        floatingIndicator.setElevation(dp(12));

        // White icon inside the circle — use SRC_IN so vector paths tint correctly
        indicatorIconView = new ImageView(getContext());
        indicatorIconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        indicatorIconView.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        int iconSizePx = dp(26); // Fixed 26dp icon inside 54dp circle
        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(iconSizePx, iconSizePx, Gravity.CENTER);
        floatingIndicator.addView(indicatorIconView, iconParams);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(sizePx, sizePx);
        // gravity START + TOP → we use TranslationX and TranslationY to position it
        params.gravity = Gravity.TOP | Gravity.START;
        addView(floatingIndicator, params);

        // Hidden until first layout
        floatingIndicator.setAlpha(0f);
        floatingIndicator.setScaleX(0.5f);
        floatingIndicator.setScaleY(0.5f);
    }

    // ─── Public API ───────────────────────────────────────────────────────────

    public void setTabs(List<TabItem> items) {
        if (items == null || items.isEmpty()) return;
        tabs.clear();
        tabs.addAll(items);
        tabsContainer.removeAllViews();
        tabViews.clear();
        // Clamp selectedIndex in case it's stale from a previous setTabs call
        if (selectedIndex >= tabs.size()) selectedIndex = 0;
        buildTabViews();
        indicatorIconView.setImageResource(tabs.get(selectedIndex).iconResId);
    }

    public void setOnTabSelectedListener(OnTabSelectedListener l) {
        this.listener = l;
    }

    /** Called from MainActivity's navController.addOnDestinationChangedListener. */
    public void setSelectedTab(int index) {
        if (index < 0 || index >= tabs.size() || index == selectedIndex) return;
        int prev = selectedIndex;
        selectedIndex = index;
        updateTabAppearances(prev, index);
        if (initialLayoutDone) animateIndicatorToTab();
    }

    // ─── Tab Views ────────────────────────────────────────────────────────────

    private void buildTabViews() {
        for (int i = 0; i < tabs.size(); i++) {
            View v = buildSingleTab(tabs.get(i), i);
            tabsContainer.addView(v, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
            tabViews.add(v);
        }
    }

    private View buildSingleTab(TabItem tab, int index) {
        LinearLayout root = new LinearLayout(getContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setClickable(true);
        root.setFocusable(true);
        root.setBackground(null);

        ImageView icon = new ImageView(getContext());
        icon.setImageResource(tab.iconResId);
        icon.setContentDescription(tab.label);
        int iconSizePx = dp(22);
        root.addView(icon, new LinearLayout.LayoutParams(iconSizePx, iconSizePx));

        TextView label = new TextView(getContext());
        label.setText(tab.label);
        label.setTextSize(10f);
        label.setGravity(Gravity.CENTER);
        label.setMaxLines(1);
        label.setLetterSpacing(0.05f);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(3);
        root.addView(label, lp);

        // Set initial visual state — use SRC_IN for correct vector tinting
        boolean isSelected = (index == selectedIndex);
        icon.setColorFilter(Color.parseColor("#94A3B8"), PorterDuff.Mode.SRC_IN);
        icon.setAlpha(isSelected ? 0f : 1f);
        label.setTextColor(Color.parseColor("#94A3B8"));
        label.setAlpha(isSelected ? 0f : 1f);

        root.setOnClickListener(v -> onTabTapped(index));
        applyPressFeedback(root);
        return root;
    }

    // ─── Appearance Updates ───────────────────────────────────────────────────

    private void updateTabAppearances(int prevIndex, int newIndex) {
        // Fade previous tab icon + label back in — guard against null children
        if (prevIndex >= 0 && prevIndex < tabViews.size()) {
            View prevView = tabViews.get(prevIndex);
            if (prevView instanceof LinearLayout) {
                LinearLayout prev = (LinearLayout) prevView;
                View child0 = prev.getChildAt(0);
                View child1 = prev.getChildAt(1);
                if (child0 instanceof ImageView) {
                    ((ImageView) child0).setColorFilter(Color.parseColor("#94A3B8"), PorterDuff.Mode.SRC_IN);
                    child0.animate().alpha(1f).setDuration(200).setInterpolator(EXPO_EASE_OUT).start();
                }
                if (child1 != null) {
                    child1.animate().alpha(1f).setDuration(200).setInterpolator(EXPO_EASE_OUT).start();
                }
            }
        }
        // Fade new tab out — indicator takes over visually
        if (newIndex >= 0 && newIndex < tabViews.size()) {
            View nextView = tabViews.get(newIndex);
            if (nextView instanceof LinearLayout) {
                LinearLayout next = (LinearLayout) nextView;
                View child0 = next.getChildAt(0);
                View child1 = next.getChildAt(1);
                if (child0 != null) child0.animate().alpha(0f).setDuration(130).start();
                if (child1 != null) child1.animate().alpha(0f).setDuration(130).start();
            }
            if (newIndex < tabs.size()) {
                indicatorIconView.setImageResource(tabs.get(newIndex).iconResId);
            }
        }
    }

    // ─── Layout & Initial Positioning ────────────────────────────────────────

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!initialLayoutDone && getWidth() > 0) {
            // Wait one more frame to guarantee all children have measured positions
            getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    getViewTreeObserver().removeOnPreDrawListener(this);
                    performFirstLayout();
                    return true;
                }
            });
        }
    }

    private void performFirstLayout() {
        if (initialLayoutDone) return;
        initialLayoutDone = true;

        float cx = getTabCenterX(selectedIndex);
        backgroundView.setCradleCenter(cx);

        float tx = cx - dp(INDICATOR_SIZE_DP) / 2f;
        float ty = getIndicatorTargetY();

        floatingIndicator.setTranslationX(tx);
        floatingIndicator.setTranslationY(ty - dp(70));
        floatingIndicator.setAlpha(0f);

        // Phase 1: entire nav bar slides up from below
        setTranslationY(dp(80));
        setAlpha(0f);
        animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(480)
            .setStartDelay(50)
            .setInterpolator(EXPO_EASE_OUT)
            .withEndAction(() -> {
                // Phase 2: indicator drops in after bar is settled
                floatingIndicator.animate()
                    .translationY(ty)
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(520)
                    .setStartDelay(0)
                    .setInterpolator(EXPO_EASE_OUT)
                    .start();
            })
            .start();
    }

    // ─── Tab Click → Animation ────────────────────────────────────────────────

    private void onTabTapped(int index) {
        if (index == selectedIndex || index < 0 || index >= tabs.size()) return;
        int prev = selectedIndex;
        selectedIndex = index;
        updateTabAppearances(prev, index);
        animateIndicatorToTab();
        if (listener != null) listener.onTabSelected(index, tabs.get(index).menuItemId);
    }

    /**
     * Full fluid transition sequence:
     *   1. Cradle wave slides horizontally (ValueAnimator)
     *   2. Indicator pops up + squishes (quick ease-out)
     *   3. Indicator sweeps horizontally to new X
     *   4. Indicator spring-drops into new cradle (SpringAnimation)
     */
    private void animateIndicatorToTab() {
        final float targetX = getTabCenterX(selectedIndex) - dp(INDICATOR_SIZE_DP) / 2f;
        final float targetY = getIndicatorTargetY();

        // 1. Slide the bezier wave
        float fromCradle = backgroundView.getCradleCenter();
        float toCradle = getTabCenterX(selectedIndex);
        ValueAnimator cradleAnim = ValueAnimator.ofFloat(fromCradle, toCradle);
        cradleAnim.setDuration(360);
        cradleAnim.setInterpolator(EXPO_EASE_OUT);
        cradleAnim.addUpdateListener(a ->
            backgroundView.setCradleCenter((float) a.getAnimatedValue())
        );
        cradleAnim.start();

        // 2. Pop indicator upward + squish
        floatingIndicator.animate()
            .translationY(targetY - dp(46))
            .scaleX(0.72f)
            .scaleY(0.72f)
            .setDuration(160)
            .setInterpolator(EXPO_EASE_OUT)
            .withEndAction(() -> {
                // 3. Sweep horizontally
                floatingIndicator.animate()
                    .translationX(targetX)
                    .setDuration(240)
                    .setInterpolator(EXPO_EASE_OUT)
                    .withEndAction(() -> springDrop(targetY))
                    .start();
            })
            .start();
    }

    /** Physics-based drop into the cradle with a satisfying bounce. */
    private void springDrop(float targetY) {
        SpringAnimation sy = new SpringAnimation(floatingIndicator, DynamicAnimation.TRANSLATION_Y, targetY);
        sy.getSpring().setStiffness(SpringForce.STIFFNESS_MEDIUM).setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);
        sy.start();

        SpringAnimation sx = new SpringAnimation(floatingIndicator, DynamicAnimation.SCALE_X, 1f);
        sx.getSpring().setStiffness(SpringForce.STIFFNESS_LOW).setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);
        sx.start();

        SpringAnimation sy2 = new SpringAnimation(floatingIndicator, DynamicAnimation.SCALE_Y, 1f);
        sy2.getSpring().setStiffness(SpringForce.STIFFNESS_LOW).setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);
        sy2.start();
    }

    // ─── Coordinate Helpers ───────────────────────────────────────────────────

    /**
     * Returns the X center of tab[index] in this view's coordinate space.
     * Walks tabsContainer children since they lay out relative to tabsContainer.
     */
    private float getTabCenterX(int index) {
        if (tabViews.isEmpty() || index < 0 || index >= tabViews.size()) {
            return getWidth() / (float) Math.max(1, tabs.size()) * 0.5f;
        }
        View tab = tabViews.get(index);
        // tab.getLeft() is relative to tabsContainer, which starts at x=0 inside this view
        return tab.getLeft() + tab.getWidth() / 2f;
    }

    /**
     * How far the indicator's TOP edge is from this view's top edge.
     * Negative = above the nav bar → floats up into the bezier cradle.
     * Net visual position: indicator center sits LIFT_DP above nav bar top.
     */
    private float getIndicatorTargetY() {
        return -(dp(INDICATOR_LIFT_DP) + dp(INDICATOR_SIZE_DP) / 2f);
    }

    // ─── Touch Feedback ───────────────────────────────────────────────────────

    private void applyPressFeedback(View v) {
        v.setOnTouchListener((view, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    view.animate().scaleX(0.87f).scaleY(0.87f).setDuration(90)
                        .setInterpolator(EXPO_EASE_OUT).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    view.animate().scaleX(1f).scaleY(1f).setDuration(180)
                        .setInterpolator(EXPO_EASE_OUT).start();
                    break;
            }
            return false;
        });
    }

    // ─── Utilities ────────────────────────────────────────────────────────────

    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }
    private int dp(float v) { return Math.round(v * getResources().getDisplayMetrics().density); }
}
