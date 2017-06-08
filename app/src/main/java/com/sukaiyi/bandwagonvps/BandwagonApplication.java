package com.sukaiyi.bandwagonvps;

import android.app.Application;

import com.sukaiyi.bandwagonvps.utils.CrashHandler;

/**
 * Created by sukaiyi on 2017/06/07.
 */

public class BandwagonApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
    }
}