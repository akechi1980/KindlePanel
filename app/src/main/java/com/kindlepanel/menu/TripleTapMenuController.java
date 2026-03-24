package com.kindlepanel.menu;

import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

/**
 * 负责识别空白区域三击手势，并控制浮层自动隐藏时机。
 */
public class TripleTapMenuController {

    private static final long TRIPLE_TAP_WINDOW_MS = 800L;
    private static final long AUTO_HIDE_MS = 4000L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Callback callback;
    private int tapCount;
    private long lastTapTimestamp;
    private final Runnable hideRunnable = new Runnable() {
        @Override
        public void run() {
            callback.onMenuHideRequested();
        }
    };

    public TripleTapMenuController(@NonNull Callback callback) {
        this.callback = callback;
    }

    public void onTouchEvent(@NonNull MotionEvent event, boolean menuVisible) {
        if (event.getActionMasked() != MotionEvent.ACTION_DOWN) {
            return;
        }

        // 使用短时间窗口累计点击次数，避免引入更重的手势系统。
        long now = System.currentTimeMillis();
        if (now - lastTapTimestamp <= TRIPLE_TAP_WINDOW_MS) {
            tapCount++;
        } else {
            tapCount = 1;
        }
        lastTapTimestamp = now;

        if (tapCount >= 3) {
            tapCount = 0;
            callback.onTripleTapTriggered();
            return;
        }

        if (menuVisible) {
            scheduleAutoHide();
        }
    }

    public void onMenuShown() {
        scheduleAutoHide();
    }

    public void onMenuHidden() {
        handler.removeCallbacks(hideRunnable);
    }

    private void scheduleAutoHide() {
        // 每次交互都重置隐藏计时，让浮层只在短暂停留时可见。
        handler.removeCallbacks(hideRunnable);
        handler.postDelayed(hideRunnable, AUTO_HIDE_MS);
    }

    public interface Callback {
        void onTripleTapTriggered();

        void onMenuHideRequested();
    }
}
