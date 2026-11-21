package com.v2s.player.core.utils;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

/**
 * Small UI construction helpers.
 * Role: centralizes density conversion and reusable rounded backgrounds for programmatic screens.
 */
public final class ViewUtils {
    /** Converts dp units to physical pixels for the current device density. */
    public static int dp(Context context, int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    /** Applies a rounded background drawable to a view. */
    public static void roundedBackground(View view, int color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(view.getContext(), radiusDp));
        view.setBackground(drawable);
    }

    private ViewUtils() { }
}
