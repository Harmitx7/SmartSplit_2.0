package com.smartsplit.app.ui.motion;

import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.PathInterpolator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smartsplit.app.R;

/**
 * iOS-inspired motion helpers for screen entrances and touch feedback.
 */
public final class IosMotion {

    private static final Interpolator EASE_OUT = new PathInterpolator(0.2f, 0f, 0f, 1f);
    private static final Interpolator EASE_IN = new PathInterpolator(0.4f, 0f, 1f, 1f);
    private static final Interpolator SPRING = new OvershootInterpolator(0.72f);

    private IosMotion() {
        // Utility class.
    }

    public static void animateIn(@NonNull View view, long delayMs) {
        if (view.getVisibility() != View.VISIBLE) {
            return;
        }
        view.setAlpha(0f);
        view.setTranslationY(dp(view, 20f));
        view.setScaleX(0.985f);
        view.setScaleY(0.985f);
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay(delayMs)
            .setDuration(540)
            .setInterpolator(SPRING)
            .start();
    }

    public static void animateNavBar(@NonNull View view) {
        view.setAlpha(0f);
        view.setTranslationY(dp(view, 26f));
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(460)
            .setStartDelay(140)
            .setInterpolator(EASE_OUT)
            .start();
    }

    public static void applyPressFeedback(@NonNull View view) {
        view.setOnTouchListener((v, event) -> {
            if (!v.isEnabled()) {
                return false;
            }
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                v.animate()
                    .scaleX(0.96f)
                    .scaleY(0.96f)
                    .setDuration(110)
                    .setInterpolator(EASE_OUT)
                    .start();
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                v.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(190)
                    .setInterpolator(EASE_IN)
                    .start();
            }
            return false;
        });
    }

    public static void applyListLayoutAnimation(@NonNull RecyclerView recyclerView) {
        recyclerView.setLayoutAnimation(
            AnimationUtils.loadLayoutAnimation(
                recyclerView.getContext(),
                R.anim.layout_slide_from_bottom
            )
        );
    }

    private static float dp(View view, float value) {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value,
            view.getResources().getDisplayMetrics()
        );
    }
}
