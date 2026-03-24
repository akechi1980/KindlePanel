package com.kindlepanel.menu;

import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

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
        handler.removeCallbacks(hideRunnable);
        handler.postDelayed(hideRunnable, AUTO_HIDE_MS);
    }

    public interface Callback {
        void onTripleTapTriggered();

        void onMenuHideRequested();
    }
}
