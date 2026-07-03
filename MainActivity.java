package org.orthodox.prayerrope;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final int TOTAL_KNOTS = 33;
    private static final String JESUS_PRAYER =
            "Lord Jesus Christ, Son of God, have mercy on me, a sinner.";

    private static final String[] REFLECTIONS = {
            "First third — stillness: \"Be still, and know that I am God.\" (Psalm 46:10)",
            "Second third — repentance: the publican's prayer teaches humility before God.",
            "Final third — mercy: for yourself, and for all whom you carry in your heart."
    };

    // Byzantine palette
    private static final int COLOR_BG_TOP = Color.parseColor("#4A1010");
    private static final int COLOR_BG_BOTTOM = Color.parseColor("#210606");
    private static final int COLOR_GOLD = Color.parseColor("#D4AF37");
    private static final int COLOR_GOLD_DIM = Color.parseColor("#8C7228");
    private static final int COLOR_CREAM = Color.parseColor("#F3E9D2");
    private static final int COLOR_CARD = Color.parseColor("#33110F");

    private int knotsPrayed = 0;
    private TextView counterText;
    private RopeView ropeView;
    private TextView prayerText;
    private TextView reflectionText;
    private Button prayButton;
    private Button resetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        // Edge-to-edge: let the background run under the status bar.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(COLOR_BG_TOP);
            getWindow().setNavigationBarColor(COLOR_BG_BOTTOM);
        }

        ViewGroup.LayoutParams matchParent = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackground(verticalGradient(COLOR_BG_TOP, COLOR_BG_BOTTOM));
        root.setPadding(56, 140, 56, 80);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setLayoutParams(matchParent);
        root.setMinimumHeight(getResources().getDisplayMetrics().heightPixels);

        // --- Ornamental cross ---
        TextView cross = new TextView(this);
        cross.setText("\u2626"); // ☦ Orthodox cross
        cross.setTextSize(48);
        cross.setTextColor(COLOR_GOLD);
        cross.setGravity(Gravity.CENTER);
        root.addView(cross);

        TextView title = new TextView(this);
        title.setText("Komboskini");
        title.setTextSize(32);
        title.setTypeface(Typeface.SERIF, Typeface.BOLD);
        title.setTextColor(COLOR_GOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 12, 0, 0);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("A Prayer Rope for the Jesus Prayer");
        subtitle.setTextSize(14);
        subtitle.setTypeface(Typeface.SERIF, Typeface.ITALIC);
        subtitle.setTextColor(COLOR_GOLD_DIM);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 8, 0, 8);
        root.addView(subtitle);

        root.addView(divider());

        counterText = new TextView(this);
        counterText.setTextSize(18);
        counterText.setTypeface(Typeface.SERIF, Typeface.BOLD);
        counterText.setTextColor(COLOR_CREAM);
        counterText.setGravity(Gravity.CENTER);
        counterText.setPadding(0, 24, 0, 8);
        root.addView(counterText);

        ropeView = new RopeView(this);
        int ropeHeightPx = (int) (320 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams ropeParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ropeHeightPx);
        ropeParams.setMargins(0, 8, 0, 8);
        ropeView.setLayoutParams(ropeParams);
        root.addView(ropeView);

        // --- Prayer card ---
        prayerText = new TextView(this);
        prayerText.setTextSize(18);
        prayerText.setTypeface(Typeface.SERIF);
        prayerText.setTextColor(COLOR_CREAM);
        prayerText.setGravity(Gravity.CENTER);
        prayerText.setLineSpacing(6, 1.1f);
        prayerText.setPadding(32, 32, 32, 32);
        prayerText.setBackground(cardBackground());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 24);
        prayerText.setLayoutParams(cardParams);
        root.addView(prayerText);

        reflectionText = new TextView(this);
        reflectionText.setTextSize(14);
        reflectionText.setTypeface(Typeface.SERIF, Typeface.ITALIC);
        reflectionText.setTextColor(COLOR_GOLD_DIM);
        reflectionText.setGravity(Gravity.CENTER);
        reflectionText.setLineSpacing(4, 1.1f);
        reflectionText.setPadding(16, 8, 16, 32);
        LinearLayout.LayoutParams reflParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        reflectionText.setLayoutParams(reflParams);
        root.addView(reflectionText);

        prayButton = new Button(this);
        prayButton.setText("Climb to the next knot");
        prayButton.setAllCaps(false);
        prayButton.setTextSize(17);
        prayButton.setTypeface(Typeface.SERIF, Typeface.BOLD);
        prayButton.setTextColor(COLOR_BG_BOTTOM);
        prayButton.setBackground(goldButtonBackground());
        prayButton.setPadding(32, 28, 32, 28);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnParams.setMargins(0, 8, 0, 16);
        prayButton.setLayoutParams(btnParams);
        prayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prayKnot();
            }
        });
        root.addView(prayButton);

        resetButton = new Button(this);
        resetButton.setText("Begin the rope again");
        resetButton.setAllCaps(false);
        resetButton.setTextSize(15);
        resetButton.setTypeface(Typeface.SERIF);
        resetButton.setTextColor(COLOR_GOLD);
        resetButton.setBackground(outlineButtonBackground());
        resetButton.setPadding(32, 24, 32, 24);
        resetButton.setVisibility(View.GONE);
        LinearLayout.LayoutParams resetParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        resetButton.setLayoutParams(resetParams);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetRope();
            }
        });
        root.addView(resetButton);

        ScrollView scroll = new ScrollView(this);
        scroll.setLayoutParams(matchParent);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(COLOR_BG_BOTTOM);
        scroll.addView(root);
        setContentView(scroll);

        resetRope();
    }

    // ---------- drawables ----------

    private GradientDrawable verticalGradient(int top, int bottom) {
        return new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{top, bottom});
    }

    private GradientDrawable cardBackground() {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(COLOR_CARD);
        gd.setCornerRadius(24);
        gd.setStroke(2, COLOR_GOLD_DIM);
        return gd;
    }

    private GradientDrawable goldButtonBackground() {
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{COLOR_GOLD, Color.parseColor("#E8CB6E"), COLOR_GOLD});
        gd.setCornerRadius(16);
        return gd;
    }

    private GradientDrawable outlineButtonBackground() {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.TRANSPARENT);
        gd.setCornerRadius(16);
        gd.setStroke(2, COLOR_GOLD);
        return gd;
    }

    private View divider() {
        View line = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(240, 2);
        params.setMargins(0, 16, 0, 0);
        line.setLayoutParams(params);
        line.setBackgroundColor(COLOR_GOLD_DIM);
        return line;
    }

    // ---------- game logic ----------

    private void prayKnot() {
        if (knotsPrayed >= TOTAL_KNOTS) {
            return;
        }
        knotsPrayed++;
        prayerText.setText(JESUS_PRAYER);
        updateDisplay();
        vibrateLightly();

        if (knotsPrayed % 11 == 0) {
            int section = (knotsPrayed / 11) - 1;
            reflectionText.setText(REFLECTIONS[section]);
        }

        if (knotsPrayed == TOTAL_KNOTS) {
            prayerText.setText("You have completed one circuit of the rope.\n\n"
                    + "\u201CPray without ceasing.\u201D (1 Thessalonians 5:17)");
            reflectionText.setText("The rope has no end \u2014 as prayer, offered humbly, "
                    + "has no end. Glory to God.");
            prayButton.setVisibility(View.GONE);
            resetButton.setVisibility(View.VISIBLE);
        }
    }

    private void resetRope() {
        knotsPrayed = 0;
        prayerText.setText("Tap \u201CClimb to the next knot\u201D to begin.");
        reflectionText.setText("");
        prayButton.setVisibility(View.VISIBLE);
        resetButton.setVisibility(View.GONE);
        updateDisplay();
    }

    private void updateDisplay() {
        counterText.setText("Knot " + knotsPrayed + " of " + TOTAL_KNOTS);
        ropeView.setKnotsPrayed(knotsPrayed);
    }

    private void vibrateLightly() {
        try {
            Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (v == null) return;
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                v.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(20);
            }
        } catch (Exception ignored) {
            // Vibration is a nicety, never let it crash the prayer.
        }
    }

    // =====================================================================
    // RopeView — draws the 33-knot prayer rope as a snaking three-row path
    // (11 knots per row, echoing the rope's traditional thirds) and
    // animates a small monk figure climbing knot-by-knot as the player prays.
    // =====================================================================
    public static class RopeView extends View {

        private static final int TOTAL_KNOTS = 33;
        private static final int KNOTS_PER_ROW = 11;
        private static final int ROWS = 3;

        private static final int COLOR_ROPE = Color.parseColor("#8C7228");
        private static final int COLOR_KNOT_FILLED = Color.parseColor("#D4AF37");
        private static final int COLOR_KNOT_HOLLOW = Color.parseColor("#5A4A22");
        private static final int COLOR_KNOT_STROKE = Color.parseColor("#F3E9D2");
        private static final int COLOR_MARKER = Color.parseColor("#F3E9D2");
        private static final int COLOR_ROBE = Color.parseColor("#3B1F16");
        private static final int COLOR_ROBE_DARK = Color.parseColor("#241209");
        private static final int COLOR_SKIN = Color.parseColor("#C68A5B");
        private static final int COLOR_CROSS = Color.parseColor("#D4AF37");

        private final Paint ropePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint knotFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint knotHollowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint knotStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint robePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint robeDarkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint skinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint crossPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // points[0] = starting cross/anchor, points[1..33] = knot centers
        private final PointF[] points = new PointF[TOTAL_KNOTS + 1];
        private int knotsPrayed = 0;
        private final PointF monkPos = new PointF();
        private ValueAnimator climbAnimator;

        public RopeView(Context context) {
            super(context);
            init();
        }

        private void init() {
            ropePaint.setColor(COLOR_ROPE);
            ropePaint.setStyle(Paint.Style.STROKE);
            ropePaint.setStrokeWidth(10f);
            ropePaint.setStrokeCap(Paint.Cap.ROUND);

            knotFillPaint.setColor(COLOR_KNOT_FILLED);
            knotFillPaint.setStyle(Paint.Style.FILL);

            knotHollowPaint.setColor(COLOR_KNOT_HOLLOW);
            knotHollowPaint.setStyle(Paint.Style.FILL);

            knotStrokePaint.setColor(COLOR_KNOT_STROKE);
            knotStrokePaint.setStyle(Paint.Style.STROKE);
            knotStrokePaint.setStrokeWidth(2.5f);

            markerPaint.setColor(COLOR_MARKER);
            markerPaint.setStyle(Paint.Style.FILL);

            robePaint.setColor(COLOR_ROBE);
            robePaint.setStyle(Paint.Style.FILL);

            robeDarkPaint.setColor(COLOR_ROBE_DARK);
            robeDarkPaint.setStyle(Paint.Style.FILL);

            skinPaint.setColor(COLOR_SKIN);
            skinPaint.setStyle(Paint.Style.FILL);

            crossPaint.setColor(COLOR_CROSS);
            crossPaint.setStyle(Paint.Style.STROKE);
            crossPaint.setStrokeWidth(4f);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            layoutPoints(w, h);
            monkPos.set(points[knotsPrayed].x, points[knotsPrayed].y);
            invalidate();
        }

        private void layoutPoints(int w, int h) {
            float marginX = w * 0.12f;
            float marginY = h * 0.10f;
            float usableW = w - 2 * marginX;
            float rowHeight = (h - 2 * marginY) / (ROWS - 1);

            // Anchor (the rope's starting cross) sits below the first row.
            points[0] = new PointF(marginX, h - marginY + rowHeight * 0.35f);

            int knot = 1;
            for (int row = 0; row < ROWS; row++) {
                // Row 0 is nearest the bottom; the monk climbs upward.
                float y = (h - marginY) - row * rowHeight;
                boolean leftToRight = (row % 2 == 0);
                for (int col = 0; col < KNOTS_PER_ROW && knot <= TOTAL_KNOTS; col++, knot++) {
                    float t = col / (float) (KNOTS_PER_ROW - 1);
                    float x = leftToRight ? marginX + t * usableW : (w - marginX) - t * usableW;
                    points[knot] = new PointF(x, y);
                }
            }
        }

        /** Call after each prayed knot (or on reset with 0) to animate the monk's climb. */
        public void setKnotsPrayed(int newCount) {
            this.knotsPrayed = Math.max(0, Math.min(TOTAL_KNOTS, newCount));
            if (points[0] == null) {
                // Not laid out yet; position will be set in onSizeChanged.
                return;
            }
            final PointF target = points[knotsPrayed];
            final PointF start = new PointF(monkPos.x, monkPos.y);

            if (climbAnimator != null) {
                climbAnimator.cancel();
            }
            climbAnimator = ValueAnimator.ofFloat(0f, 1f);
            climbAnimator.setDuration(400);
            climbAnimator.setInterpolator(new DecelerateInterpolator());
            climbAnimator.addUpdateListener(animation -> {
                float f = (float) animation.getAnimatedValue();
                monkPos.x = start.x + (target.x - start.x) * f;
                monkPos.y = start.y + (target.y - start.y) * f;
                invalidate();
            });
            climbAnimator.start();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (points[0] == null) return;

            // Rope path, anchor through every knot.
            Path path = new Path();
            path.moveTo(points[0].x, points[0].y);
            for (int i = 1; i <= TOTAL_KNOTS; i++) {
                path.lineTo(points[i].x, points[i].y);
            }
            canvas.drawPath(path, ropePaint);

            // Starting cross (the tassel/cross at the foot of a real prayer rope).
            float cx = points[0].x, cy = points[0].y;
            float armLen = 16f;
            canvas.drawLine(cx, cy - armLen, cx, cy + armLen, crossPaint);
            canvas.drawLine(cx - armLen * 0.6f, cy - armLen * 0.3f, cx + armLen * 0.6f, cy - armLen * 0.3f, crossPaint);

            // Knots.
            float radius = 15f;
            for (int i = 1; i <= TOTAL_KNOTS; i++) {
                PointF p = points[i];
                boolean prayed = i <= knotsPrayed;
                canvas.drawCircle(p.x, p.y, radius, prayed ? knotFillPaint : knotHollowPaint);
                canvas.drawCircle(p.x, p.y, radius, knotStrokePaint);
                if (i % KNOTS_PER_ROW == 0 && i != TOTAL_KNOTS) {
                    // Section marker bead, slightly larger, between rows.
                    canvas.drawCircle(p.x, p.y, radius * 1.5f, markerPaint);
                    canvas.drawCircle(p.x, p.y, radius, prayed ? knotFillPaint : knotHollowPaint);
                    canvas.drawCircle(p.x, p.y, radius, knotStrokePaint);
                }
            }

            drawMonk(canvas, monkPos.x, monkPos.y);
        }

        private void drawMonk(Canvas canvas, float x, float y) {
            float bodyW = 26f;
            float bodyH = 40f;
            float headR = 11f;

            float feetY = y - 6f;
            float headCy = feetY - bodyH - headR;

            // Cassock (robe): a simple trapezoid, wider at the hem.
            Path robe = new Path();
            robe.moveTo(x - bodyW * 0.28f, feetY - bodyH);
            robe.lineTo(x + bodyW * 0.28f, feetY - bodyH);
            robe.lineTo(x + bodyW * 0.5f, feetY);
            robe.lineTo(x - bodyW * 0.5f, feetY);
            robe.close();
            canvas.drawPath(robe, robePaint);

            // Sash shadow line down the middle for a bit of form.
            canvas.drawLine(x, feetY - bodyH, x, feetY, robeDarkPaint);

            // Head.
            canvas.drawCircle(x, headCy, headR, skinPaint);

            // Klobuk / hood: a dark dome over the head and down the sides.
            Path hood = new Path();
            hood.moveTo(x - headR * 1.15f, headCy);
            hood.quadTo(x, headCy - headR * 2.1f, x + headR * 1.15f, headCy);
            hood.lineTo(x + headR * 0.9f, headCy + headR * 0.3f);
            hood.quadTo(x, headCy - headR * 0.6f, x - headR * 0.9f, headCy + headR * 0.3f);
            hood.close();
            canvas.drawPath(hood, robeDarkPaint);

            // Small gold cross on the chest.
            float ccx = x, ccy = feetY - bodyH * 0.55f;
            canvas.drawLine(ccx, ccy - 7f, ccx, ccy + 7f, crossPaint);
            canvas.drawLine(ccx - 4.5f, ccy - 2.5f, ccx + 4.5f, ccy - 2.5f, crossPaint);
        }

        @Override
        protected int getSuggestedMinimumHeight() {
            return (int) (280 * getResources().getDisplayMetrics().density);
        }
    }
}