package com.v2s.player;

import android.content.Intent;
import android.os.Bundle;
import com.v2s.player.home.HomeActivity;

/**
 * Launcher compatibility activity.
 * Role: keeps the launcher class stable while the real home implementation lives in the feature package.
 */
public final class MainActivity extends android.app.Activity {
    /** Redirects the launcher entry point to the production HomeActivity. */
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}
