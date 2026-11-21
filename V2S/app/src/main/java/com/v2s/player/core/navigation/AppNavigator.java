package com.v2s.player.core.navigation;

import android.app.Activity;
import android.content.Intent;
import com.v2s.player.player.PlayerActivity;
import com.v2s.player.data.model.MediaItem;

/**
 * Central navigation helper.
 * Role: keeps Activity-to-Activity navigation out of feature business logic.
 */
public final class AppNavigator {
    /** Opens PlayerActivity for the selected media item. */
    public static void openPlayer(Activity activity, MediaItem item) {
        Intent intent = new Intent(activity, PlayerActivity.class);
        intent.setData(item.uri);
        intent.putExtra(PlayerActivity.EXTRA_TITLE, item.title);
        intent.putExtra(PlayerActivity.EXTRA_IS_VIDEO, item.video);
        activity.startActivity(intent);
    }

    private AppNavigator() { }
}
