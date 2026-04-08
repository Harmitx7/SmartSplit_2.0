package com.smartsplit.app.ui.nav;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/**
 * Custom View that draws the fluid bezier-cutout nav background.
 *
 * The top edge has a smooth bezier cradle scooped out at `cradleCenterX`.
 * Calling setCradleCenter() and invalidate() drives smooth wave glide animation.
 *
 * Coordinate note: the view is MATCH_PARENT inside FluidBottomNavigation.
 * Height h = full nav bar height. The cradle scoops DOWN from the top edge.
 */
public class FluidBackgroundView extends View {

    // Cradle proportions (relative to view width / height)
    private static final float CRADLE_HALF_WIDTH_RATIO = 0.14f;  // Half-width of the scoop
    private static final float CRADLE_DEPTH_RATIO      = 0.52f;  // How deep the scoop goes
    private static final float CP_SPREAD_RATIO         = 0.55f;  // Bezier control point spread
    private static final float CORNER_RADIUS_DP        = 28f;

    // Nav bar surface: #0A1F35 (surface_container) at full opacity — matches Crimson Night
    private static final int NAV_FILL_COLOR = 0xFF0D2240;

    private final Paint fillPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path  shape      = new Path();

    private float cradleCenterX = -1f; // -1 signals "not yet set"
    private float cornerRadiusPx;

    public FluidBackgroundView(Context context) { super(context); init(); }
    public FluidBackgroundView(Context context, AttributeSet a) { super(context, a); init(); }
    public FluidBackgroundView(Context context, AttributeSet a, int d) { super(context, a, d); init(); }

    private void init() {
        setLayerType(LAYER_TYPE_HARDWARE, null);

        fillPaint.setColor(NAV_FILL_COLOR);
        fillPaint.setStyle(Paint.Style.FILL);

        // Subtle top-edge crimson glow line
        glowPaint.setColor(Color.parseColor("#44D7263D"));
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeWidth(1.5f);
        glowPaint.setMaskFilter(new BlurMaskFilter(6f, BlurMaskFilter.Blur.NORMAL));

        cornerRadiusPx = CORNER_RADIUS_DP * getResources().getDisplayMetrics().density;
    }

    /** Animate the cradle position. Triggers invalidate(). */
    public void setCradleCenter(float x) {
        cradleCenterX = x;
        invalidate();
    }

    public float getCradleCenter() {
        return cradleCenterX;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        if (cradleCenterX < 0 && w > 0) {
            // Default: first quarter (tab 0 center for 4 tabs)
            cradleCenterX = w / 8f;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() == 0 || getHeight() == 0) return;

        final float W = getWidth();
        final float H = getHeight();
        final float cx = cradleCenterX;

        final float halfW  = W * CRADLE_HALF_WIDTH_RATIO;
        final float depth  = H * CRADLE_DEPTH_RATIO;
        final float spread = halfW * CP_SPREAD_RATIO;
        final float r      = cornerRadiusPx;

        final float left  = cx - halfW;
        final float right = cx + halfW;

        shape.reset();

        // ── Top-left rounded corner ──────────────────────────────────────────
        shape.moveTo(r, 0f);

        // ── Straight top edge ➜ left side of cradle ─────────────────────────
        shape.lineTo(left - spread, 0f);

        // ── LEFT bezier arm: smooth dive into cradle ─────────────────────────
        // Uses cubic Bezier: start flat, then curve steeply down
        shape.cubicTo(
            left - spread * 0.1f, 0f,         // CP1: stay flat a bit longer
            left,                  depth,       // CP2: pull sharply down
            cx - halfW * 0.25f,    depth        // End: bottom floor start
        );

        // ── Cradle floor arc ─────────────────────────────────────────────────
        shape.quadTo(
            cx, depth * 1.08f,                  // dip beneath depth at center
            cx + halfW * 0.25f, depth
        );

        // ── RIGHT bezier arm: smooth climb out of cradle ─────────────────────
        shape.cubicTo(
            right,                 depth,        // CP1
            right + spread * 0.1f, 0f,           // CP2: emerge flat
            right + spread,        0f            // End
        );

        // ── Straight top edge ➜ top-right corner ────────────────────────────
        shape.lineTo(W - r, 0f);

        // ── Top-right rounded corner ─────────────────────────────────────────
        shape.quadTo(W, 0f, W, r);

        // ── Right side + bottom + left side ──────────────────────────────────
        shape.lineTo(W, H);
        shape.lineTo(0f, H);
        shape.lineTo(0f, r);

        // ── Top-left corner close ─────────────────────────────────────────────
        shape.quadTo(0f, 0f, r, 0f);
        shape.close();

        canvas.drawPath(shape, fillPaint);
        canvas.drawPath(shape, glowPaint);
    }
}
