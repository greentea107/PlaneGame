package com.example.planegame;

import android.graphics.Rect;

public class AppHelper {
    public static int widthSurface = 0;
    public static int heightSurface = 0;
    public static Rect bound = new Rect(); // 屏幕范围
    public static boolean isPause = false;
    public static boolean isRunning = false;

    public static final String SCORE_EVENT = "score";
    public static final String COLLI_EVENT = "collision";
    public static final String FIRE_EVENT = "firePower";
    public static final String BOMB_RESET_EVENT = "bomb_reset";
    public static final String BOMB_ADD_EVENT = "bomb_add";
}
