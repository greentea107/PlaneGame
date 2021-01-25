package com.example.planegame;

import android.app.Application;
import android.content.Context;

import com.jeremyliao.liveeventbus.LiveEventBus;

public class MyApp extends Application {
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
//        initLiveEvent();
    }

    private void initLiveEvent() {
        LiveEventBus
                .config()
                .enableLogger(BuildConfig.DEBUG)
                .lifecycleObserverAlwaysActive(false);
    }
}
