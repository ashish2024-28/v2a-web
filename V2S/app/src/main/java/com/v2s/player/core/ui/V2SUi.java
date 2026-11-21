package com.v2s.player.core.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.v2s.player.core.utils.ViewUtils;

/**
 * V2SUi is the shared visual component factory.
 * Role: keeps spacing, typography, cards, pills and controls consistent across every V2S screen.
 */
public final class V2SUi {
    public static final int BG = Color.rgb(5, 12, 18);
    public static final int SURFACE = Color.rgb(14, 25, 32);
    public static final int SURFACE_2 = Color.rgb(27, 39, 46);
    public static final int SURFACE_3 = Color.rgb(36, 49, 56);
    public static final int ORANGE = Color.rgb(255, 132, 0);
    public static final int TEXT = Color.rgb(245, 247, 248);
    public static final int MUTED = Color.rgb(165, 174, 180);
    public static final int DIVIDER = Color.rgb(47, 59, 66);

    /** Converts dp to pixels using the shared V2S density helper. */
    public static int dp(Context c, int value) { return ViewUtils.dp(c, value); }

    /** Creates a vertical LinearLayout with predictable V2S spacing. */
    public static LinearLayout column(Context c) { LinearLayout v = new LinearLayout(c); v.setOrientation(LinearLayout.VERTICAL); return v; }

    /** Creates a horizontal LinearLayout with center alignment. */
    public static LinearLayout row(Context c) { LinearLayout v = new LinearLayout(c); v.setOrientation(LinearLayout.HORIZONTAL); v.setGravity(Gravity.CENTER_VERTICAL); return v; }

    /** Creates a normal V2S text label. */
    public static TextView text(Context c, String value, float size, int color) { TextView v = new TextView(c); v.setText(value); v.setTextSize(size); v.setTextColor(color); v.setGravity(Gravity.CENTER_VERTICAL); return v; }

    /** Creates a bold V2S heading. */
    public static TextView heading(Context c, String value, float size) { TextView v = text(c, value, size, TEXT); v.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD); return v; }

    /** Applies a rounded surface background with optional border. */
    public static void surface(View view, int color, int radius, int strokeColor, int strokeWidth) { GradientDrawable g = new GradientDrawable(); g.setColor(color); g.setCornerRadius(dp(view.getContext(), radius)); if (strokeWidth > 0) g.setStroke(dp(view.getContext(), strokeWidth), strokeColor); view.setBackground(g); }

    /** Creates a compact icon-like text button used for search, overflow and view controls. */
    public static TextView iconButton(Context c, String glyph, String description) { TextView v = text(c, glyph, 22, TEXT); v.setGravity(Gravity.CENTER); v.setContentDescription(description); surface(v, SURFACE_2, 10, DIVIDER, 1); return v; }

    /** Creates an orange filled pill used for primary actions. */
    public static TextView orangeButton(Context c, String label, String description) { TextView v = text(c, label, 13, Color.WHITE); v.setGravity(Gravity.CENTER); v.setTypeface(null, android.graphics.Typeface.BOLD); v.setContentDescription(description); surface(v, ORANGE, 9, ORANGE, 1); return v; }

    /** Creates a dark outlined pill used for secondary actions. */
    public static TextView darkButton(Context c, String label, String description) { TextView v = text(c, label, 13, TEXT); v.setGravity(Gravity.CENTER); v.setContentDescription(description); surface(v, SURFACE_2, 9, DIVIDER, 1); return v; }

    /** Creates a divider matching the thin separators used throughout the reference screens. */
    public static View divider(Context c) { View v = new View(c); v.setBackgroundColor(DIVIDER); return v; }

    /** Creates a thumbnail image view with dimensions suited to media cards. */
    public static ImageView thumbnail(Context c, int width, int height) { ImageView image = new ImageView(c); image.setScaleType(ImageView.ScaleType.CENTER_CROP); image.setBackgroundColor(SURFACE_3); image.setClipToOutline(true); return image; }

    /** Adds standard content padding to a page container. */
    public static void pagePadding(View view) { int p = dp(view.getContext(), 10); view.setPadding(p, 4, p, p); }

    private V2SUi() { }
}
