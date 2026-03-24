package com.kindlepanel.power;

import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

/**
 * 集中处理前台常亮标记。
 */
public class ScreenAwakeController {

    private final Window window;

    public ScreenAwakeController(@NonNull Window window) {
        this.window = window;
    }

    public void apply(boolean keepScreenOn) {
        if (keepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }
}
