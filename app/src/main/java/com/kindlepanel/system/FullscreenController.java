package com.kindlepanel.system;

import android.os.Build;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;

/**
 * 统一处理沉浸式全屏。
 * 为兼容旧版系统，按系统版本分别走新旧两套写法。
 */
public final class FullscreenController {

    private FullscreenController() {
    }

    public static void apply(@NonNull Window window, @NonNull View decorView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);
        } else {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }
}
