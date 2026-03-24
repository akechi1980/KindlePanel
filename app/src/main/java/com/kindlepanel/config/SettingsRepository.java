package com.kindlepanel.config;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.kindlepanel.mode.DisplayMode;
import com.kindlepanel.mode.photo.PhotoPlayMode;

public class SettingsRepository {

    private static final String PREFS_NAME = "kindle_panel_settings";
    private static final String KEY_DEFAULT_MODE = "default_mode";
    private static final String KEY_WEB_URL = "web_url";
    private static final String KEY_WEB_REFRESH_SECONDS = "web_refresh_seconds";
    private static final String KEY_WEATHER_CITY = "weather_city";
    private static final String KEY_WEATHER_REFRESH_MINUTES = "weather_refresh_minutes";
    private static final String KEY_PHOTO_DIRECTORY = "photo_directory";
    private static final String KEY_PHOTO_INTERVAL_SECONDS = "photo_interval_seconds";
    private static final String KEY_PHOTO_PLAY_MODE = "photo_play_mode";
    private static final String KEY_KEEP_SCREEN_ON = "keep_screen_on";
    private static final String KEY_CURRENT_WEB_URL = "current_web_url";

    private final SharedPreferences sharedPreferences;

    public SettingsRepository(@NonNull Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @NonNull
    public AppSettings getSettings() {
        AppSettings defaults = AppSettings.defaultSettings();
        AppSettings settings = new AppSettings();
        settings.defaultMode = DisplayMode.fromValue(sharedPreferences.getString(KEY_DEFAULT_MODE, defaults.defaultMode.name()));
        settings.webUrl = sharedPreferences.getString(KEY_WEB_URL, defaults.webUrl);
        settings.webRefreshSeconds = sharedPreferences.getInt(KEY_WEB_REFRESH_SECONDS, defaults.webRefreshSeconds);
        settings.weatherCity = sharedPreferences.getString(KEY_WEATHER_CITY, defaults.weatherCity);
        settings.weatherRefreshMinutes = sharedPreferences.getInt(KEY_WEATHER_REFRESH_MINUTES, defaults.weatherRefreshMinutes);
        settings.photoDirectory = sharedPreferences.getString(KEY_PHOTO_DIRECTORY, defaults.photoDirectory);
        settings.photoIntervalSeconds = sharedPreferences.getInt(KEY_PHOTO_INTERVAL_SECONDS, defaults.photoIntervalSeconds);
        settings.photoPlayMode = PhotoPlayMode.fromValue(sharedPreferences.getString(KEY_PHOTO_PLAY_MODE, defaults.photoPlayMode.name()));
        settings.keepScreenOn = sharedPreferences.getBoolean(KEY_KEEP_SCREEN_ON, defaults.keepScreenOn);
        return settings;
    }

    public void save(@NonNull AppSettings settings) {
        sharedPreferences.edit()
                .putString(KEY_DEFAULT_MODE, settings.defaultMode.name())
                .putString(KEY_WEB_URL, settings.webUrl)
                .putInt(KEY_WEB_REFRESH_SECONDS, settings.webRefreshSeconds)
                .putString(KEY_WEATHER_CITY, settings.weatherCity)
                .putInt(KEY_WEATHER_REFRESH_MINUTES, settings.weatherRefreshMinutes)
                .putString(KEY_PHOTO_DIRECTORY, settings.photoDirectory)
                .putInt(KEY_PHOTO_INTERVAL_SECONDS, settings.photoIntervalSeconds)
                .putString(KEY_PHOTO_PLAY_MODE, settings.photoPlayMode.name())
                .putBoolean(KEY_KEEP_SCREEN_ON, settings.keepScreenOn)
                .apply();
    }

    public void reset() {
        save(AppSettings.defaultSettings());
    }

    public void saveCurrentWebUrl(@NonNull String url) {
        sharedPreferences.edit()
                .putString(KEY_CURRENT_WEB_URL, url)
                .apply();
    }

    @NonNull
    public String getCurrentWebUrl() {
        return sharedPreferences.getString(KEY_CURRENT_WEB_URL, "");
    }
}
