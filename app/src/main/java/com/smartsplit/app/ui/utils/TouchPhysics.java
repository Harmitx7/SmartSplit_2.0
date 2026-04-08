package com.smartsplit.app.ui.utils;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;

import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

public class TouchPhysics {

    /**
     * Applies a premium iOS-style spring scale effect to a view.
     * When touched, it compresses slightly. Upon release, it springs back to original size.
     */
    @SuppressLint("ClickableViewAccessibility")
    public static void addSpringPressEffect(View view) {
        // Create spring animations for Scale X and Scale Y
        SpringAnimation scaleXAnimation = new SpringAnimation(view, DynamicAnimation.SCALE_X, 1f);
        SpringAnimation scaleYAnimation = new SpringAnimation(view, DynamicAnimation.SCALE_Y, 1f);

        // Configure the spring force (bouncy and responsive)
        SpringForce force = new SpringForce(1f)
                .setStiffness(SpringForce.STIFFNESS_LOW)
                .setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);

        scaleXAnimation.setSpring(force);
        scaleYAnimation.setSpring(force);

        view.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    // Compress to 92% of original size
                    scaleXAnimation.animateToFinalPosition(0.92f);
                    scaleYAnimation.animateToFinalPosition(0.92f);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Spring back to 100%
                    scaleXAnimation.animateToFinalPosition(1f);
                    scaleYAnimation.animateToFinalPosition(1f);
                    break;
            }
            // Return false to allow click listeners to still function
            return false;
        });
    }
}
