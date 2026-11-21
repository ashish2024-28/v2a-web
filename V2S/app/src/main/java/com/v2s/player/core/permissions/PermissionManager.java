package com.v2s.player.core.permissions;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import com.v2s.player.core.constants.AppConstants;

/**
 * Media permission helper.
 * Role: selects the correct Android permission set for the device API level and reports access state.
 */
public final class PermissionManager {
    /** Returns the media permissions required by this Android API level. */
    public static String[] requiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO};
        }
        return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    }

    /** Checks whether every required media permission is granted. */
    public static boolean hasMediaPermissions(Activity activity) {
        for (String permission : requiredPermissions()) {
            if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) return false;
        }
        return true;
    }

    /** Requests the current API level's media permissions. */
    public static void requestMediaPermissions(Activity activity) {
        activity.requestPermissions(requiredPermissions(), AppConstants.REQUEST_MEDIA_PERMISSIONS);
    }

    private PermissionManager() { }
}
