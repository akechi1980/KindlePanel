package com.kindlepanel.settings;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.kindlepanel.R;
import com.kindlepanel.config.AppSettings;
import com.kindlepanel.config.SettingsRepository;
import com.kindlepanel.mode.DisplayMode;
import com.kindlepanel.mode.photo.PhotoPlayMode;

/**
 * 应用设定页。
 * 负责编辑并保存轻量配置，不承载复杂业务逻辑。
 */
public class SettingsActivity extends AppCompatActivity {

    private Spinner modeSpinner;
    private EditText webUrlEdit;
    private EditText webRefreshEdit;
    private EditText weatherCityEdit;
    private EditText weatherRefreshEdit;
    private EditText photoDirectoryEdit;
    private EditText photoIntervalEdit;
    private Spinner photoPlayModeSpinner;
    private SwitchCompat keepScreenOnSwitch;
    private SettingsRepository settingsRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settingsRepository = new SettingsRepository(this);

        modeSpinner = findViewById(R.id.spinner_default_mode);
        webUrlEdit = findViewById(R.id.edit_web_url);
        webRefreshEdit = findViewById(R.id.edit_web_refresh);
        weatherCityEdit = findViewById(R.id.edit_weather_city);
        weatherRefreshEdit = findViewById(R.id.edit_weather_refresh);
        photoDirectoryEdit = findViewById(R.id.edit_photo_directory);
        photoIntervalEdit = findViewById(R.id.edit_photo_interval);
        photoPlayModeSpinner = findViewById(R.id.spinner_photo_mode);
        keepScreenOnSwitch = findViewById(R.id.switch_keep_screen_on);

        setupSpinners();
        bind(settingsRepository.getSettings());

        Button resetButton = findViewById(R.id.button_reset);
        Button saveButton = findViewById(R.id.button_save);

        resetButton.setOnClickListener(v -> bind(AppSettings.defaultSettings()));
        saveButton.setOnClickListener(v -> {
            settingsRepository.save(readSettingsFromViews());
            setResult(RESULT_OK);
            finish();
        });
    }

    private void setupSpinners() {
        // 下拉项全部来自本地中文资源，避免硬编码分散。
        ArrayAdapter<CharSequence> modeAdapter = ArrayAdapter.createFromResource(
                this, R.array.default_mode_labels, android.R.layout.simple_spinner_item);
        modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modeSpinner.setAdapter(modeAdapter);

        ArrayAdapter<CharSequence> playModeAdapter = ArrayAdapter.createFromResource(
                this, R.array.photo_play_mode_labels, android.R.layout.simple_spinner_item);
        playModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        photoPlayModeSpinner.setAdapter(playModeAdapter);
    }

    private void bind(@NonNull AppSettings settings) {
        modeSpinner.setSelection(toModeIndex(settings.defaultMode));
        webUrlEdit.setText(settings.webUrl);
        webRefreshEdit.setText(String.valueOf(settings.webRefreshSeconds));
        weatherCityEdit.setText(settings.weatherCity);
        weatherRefreshEdit.setText(String.valueOf(settings.weatherRefreshMinutes));
        photoDirectoryEdit.setText(settings.photoDirectory);
        photoIntervalEdit.setText(String.valueOf(settings.photoIntervalSeconds));
        photoPlayModeSpinner.setSelection(toPlayModeIndex(settings.photoPlayMode));
        keepScreenOnSwitch.setChecked(settings.keepScreenOn);
    }

    @NonNull
    private AppSettings readSettingsFromViews() {
        AppSettings defaults = AppSettings.defaultSettings();
        AppSettings settings = new AppSettings();
        settings.defaultMode = fromModeIndex(modeSpinner.getSelectedItemPosition());
        settings.webUrl = fallback(webUrlEdit.getText().toString(), defaults.webUrl);
        settings.webRefreshSeconds = parseInt(webRefreshEdit.getText().toString(), defaults.webRefreshSeconds);
        settings.weatherCity = fallback(weatherCityEdit.getText().toString(), defaults.weatherCity);
        settings.weatherRefreshMinutes = parseInt(weatherRefreshEdit.getText().toString(), defaults.weatherRefreshMinutes);
        settings.photoDirectory = fallback(photoDirectoryEdit.getText().toString(), defaults.photoDirectory);
        settings.photoIntervalSeconds = parseInt(photoIntervalEdit.getText().toString(), defaults.photoIntervalSeconds);
        settings.photoPlayMode = fromPlayModeIndex(photoPlayModeSpinner.getSelectedItemPosition());
        settings.keepScreenOn = keepScreenOnSwitch.isChecked();
        return settings;
    }

    private int parseInt(String rawValue, int defaultValue) {
        try {
            return Integer.parseInt(rawValue.trim());
        } catch (Exception ignored) {
            // 输入非法时回退默认值，优先保证设定页稳定可保存。
            return defaultValue;
        }
    }

    @NonNull
    private String fallback(String value, @NonNull String defaultValue) {
        return TextUtils.isEmpty(value) ? defaultValue : value.trim();
    }

    private int toModeIndex(@NonNull DisplayMode mode) {
        switch (mode) {
            case WEB:
                return 1;
            case PHOTO:
                return 2;
            case DASHBOARD:
            default:
                return 0;
        }
    }

    @NonNull
    private DisplayMode fromModeIndex(int index) {
        switch (index) {
            case 1:
                return DisplayMode.WEB;
            case 2:
                return DisplayMode.PHOTO;
            case 0:
            default:
                return DisplayMode.DASHBOARD;
        }
    }

    private int toPlayModeIndex(@NonNull PhotoPlayMode mode) {
        return mode == PhotoPlayMode.RANDOM ? 1 : 0;
    }

    @NonNull
    private PhotoPlayMode fromPlayModeIndex(int index) {
        return index == 1 ? PhotoPlayMode.RANDOM : PhotoPlayMode.SEQUENTIAL;
    }
}
