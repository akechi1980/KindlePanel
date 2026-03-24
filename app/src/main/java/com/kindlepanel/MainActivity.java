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

public class MainActivity extends AppCompatActivity implements TripleTapMenuController.Callback {

    private static final int REQUEST_SETTINGS = 1001;

    private FrameLayout modeContainer;
    private LinearLayout overlayMenu;
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

        Button exitButton = findViewById(R.id.button_exit);
        Button settingsButton = findViewById(R.id.button_settings);

        exitButton.setOnClickListener(v -> finishAffinity());
        settingsButton.setOnClickListener(v -> {
            hideOverlayMenu();
            startActivityForResult(new Intent(this, SettingsActivity.class), REQUEST_SETTINGS);
        });

        AppSettings settings = settingsRepository.getSettings();
        currentMode = settings.defaultMode;
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
        tripleTapMenuController.onTouchEvent(ev, overlayMenu.getVisibility() == View.VISIBLE);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onTripleTapTriggered() {
        if (overlayMenu.getVisibility() == View.VISIBLE) {
            hideOverlayMenu();
        } else {
            showOverlayMenu();
        }
    }

    @Override
    public void onMenuHideRequested() {
        hideOverlayMenu();
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
        screenAwakeController.apply(settings.keepScreenOn);
    }

    private void showMode(@NonNull DisplayMode mode) {
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

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mode_container, fragment, mode.name())
                .commitAllowingStateLoss();
    }

    private void showOverlayMenu() {
        overlayMenu.setVisibility(View.VISIBLE);
        overlayMenu.setAlpha(0f);
        overlayMenu.animate().alpha(1f).setDuration(120L).start();
        tripleTapMenuController.onMenuShown();
    }

    private void hideOverlayMenu() {
        overlayMenu.animate()
                .alpha(0f)
                .setDuration(120L)
                .withEndAction(() -> overlayMenu.setVisibility(View.GONE))
                .start();
        tripleTapMenuController.onMenuHidden();
    }
}
