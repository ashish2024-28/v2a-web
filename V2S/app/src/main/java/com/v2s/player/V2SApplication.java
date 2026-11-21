package com.v2s.player;

import android.app.Application;

/**
 * V2S application entry point.
 * Role: owns process-wide application initialization that must run once.
 * It intentionally stays small so feature code does not become globally coupled.
 */
public final class V2SApplication extends Application {
    /** Initializes the V2S application process. */
    @Override public void onCreate() {
        super.onCreate();
    }
}
