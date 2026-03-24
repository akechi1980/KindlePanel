package com.kindlepanel;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.kindlepanel.config.AppSettings;
import com.kindlepanel.config.SettingsRepository;
import com.kindlepanel.menu.TripleTapMenuController;
import com.kindlepanel.mode.DisplayMode;
import com.kindlepanel.mode.dashboard.DashboardFragment;
import com.kindlepanel.mode.photo.PhotoModeFragment;
import com.kindlepanel.mode.web.WebModeFragment;
import com.kindlepanel.power.ScreenAwakeController;
import com.kindlepanel.settings.SettingsActivity;
import com.kindlepanel.system.FullscreenController;

/**
 * 主界面容器。
 * 负责承载三种展示模式，并协调三击浮层、常亮和全屏行为。
 */
public class MainActivity extends AppCompatActivity implements TripleTapMenuController.Callback {

    private static final int REQUEST_SETTINGS = 1001;

    private FrameLayout modeContainer;
    private LinearLayout overlayMenu;
    private LinearLayout overlayModeBar;
    private SettingsRepository settingsRepository;
    private ScreenAwakeController screenAwakeController;
    private TripleTapMenuController tripleTapMenuController;
    private DisplayMode currentMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settingsRepository = new SettingsRepository(this);
        screenAwakeController = new ScreenAwakeController(getWindow());
        tripleTapMenuController = new TripleTapMenuController(this);

        modeContainer = findViewById(R.id.mode_container);
        overlayMenu = findViewById(R.id.overlay_menu);
        overlayModeBar = findViewById(R.id.overlay_mode_bar);

        Button exitButton = findViewById(R.id.button_exit);
        Button settingsButton = findViewById(R.id.button_settings);
        Button dashboardModeButton = findViewById(R.id.button_mode_dashboard);
        Button webModeButton = findViewById(R.id.button_mode_web);
        Button photoModeButton = findViewById(R.id.button_mode_photo);

        exitButton.setOnClickListener(v -> finishAffinity());
        settingsButton.setOnClickListener(v -> {
            hideOverlayControls();
            startActivityForResult(new Intent(this, SettingsActivity.class), REQUEST_SETTINGS);
        });
        dashboardModeButton.setOnClickListener(v -> switchMode(DisplayMode.DASHBOARD));
        webModeButton.setOnClickListener(v -> switchMode(DisplayMode.WEB));
        photoModeButton.setOnClickListener(v -> switchMode(DisplayMode.PHOTO));

        AppSettings settings = settingsRepository.getSettings();
        currentMode = settings.defaultMode;
        // 首次启动按已保存设定恢复运行状态。
        applySettings(settings);
        showMode(currentMode);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FullscreenController.apply(getWindow(), getWindow().getDecorView());
    }

    @Override
    protected void onStart() {
        super.onStart();
        applySettings(settingsRepository.getSettings());
    }

    @Override
    protected void onStop() {
        super.onStop();
        screenAwakeController.apply(false);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        tripleTapMenuController.onTouchEvent(ev, isOverlayVisible());
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onTripleTapTriggered() {
        if (isOverlayVisible()) {
            hideOverlayControls();
        } else {
            showOverlayControls();
        }
    }

    @Override
    public void onMenuHideRequested() {
        hideOverlayControls();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SETTINGS && resultCode == RESULT_OK) {
            AppSettings settings = settingsRepository.getSettings();
            currentMode = settings.defaultMode;
            applySettings(settings);
            showMode(currentMode);
        }
    }

    private void applySettings(@NonNull AppSettings settings) {
        // 常亮控制统一收口到单独控制器，避免逻辑散落。
        screenAwakeController.apply(settings.keepScreenOn);
    }

    private void showMode(@NonNull DisplayMode mode) {
        currentMode = mode;
        Fragment fragment;
        switch (mode) {
            case WEB:
                fragment = WebModeFragment.newInstance();
                break;
            case PHOTO:
                fragment = PhotoModeFragment.newInstance();
                break;
            case DASHBOARD:
            default:
                fragment = DashboardFragment.newInstance();
                break;
        }

        // 三种模式彼此独立，主界面只负责切换，不耦合内部实现。
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mode_container, fragment, mode.name())
                .commitAllowingStateLoss();

        updateModeButtons();
    }

    private void showOverlayControls() {
        overlayMenu.setVisibility(View.VISIBLE);
        overlayModeBar.setVisibility(View.VISIBLE);
        overlayMenu.setAlpha(0f);
        overlayModeBar.setAlpha(0f);
        // 浮层动画保持极轻，兼顾旧设备展示稳定性。
        overlayMenu.animate().alpha(1f).setDuration(120L).start();
        overlayModeBar.animate().alpha(1f).setDuration(120L).start();
        updateModeButtons();
        tripleTapMenuController.onMenuShown();
    }

    private void hideOverlayControls() {
        overlayMenu.animate()
                .alpha(0f)
                .setDuration(120L)
                .withEndAction(() -> overlayMenu.setVisibility(View.GONE))
                .start();
        overlayModeBar.animate()
                .alpha(0f)
                .setDuration(120L)
                .withEndAction(() -> overlayModeBar.setVisibility(View.GONE))
                .start();
        tripleTapMenuController.onMenuHidden();
    }

    private void switchMode(@NonNull DisplayMode targetMode) {
        // 模式切换只影响当前会话，不直接改默认启动模式。
        showMode(targetMode);
        hideOverlayControls();
    }

    private void updateModeButtons() {
        updateModeButtonState(findViewById(R.id.button_mode_dashboard), currentMode == DisplayMode.DASHBOARD);
        updateModeButtonState(findViewById(R.id.button_mode_web), currentMode == DisplayMode.WEB);
        updateModeButtonState(findViewById(R.id.button_mode_photo), currentMode == DisplayMode.PHOTO);
    }

    private void updateModeButtonState(@NonNull Button button, boolean active) {
        button.setAlpha(active ? 1f : 0.74f);
        button.setEnabled(!active);
    }

    private boolean isOverlayVisible() {
        return overlayMenu.getVisibility() == View.VISIBLE || overlayModeBar.getVisibility() == View.VISIBLE;
    }
}
