package com.kindlepanel.config;

import com.kindlepanel.mode.DisplayMode;
import com.kindlepanel.mode.photo.PhotoPlayMode;

public class AppSettings {

    public DisplayMode defaultMode;
    public String webUrl;
    public int webRefreshSeconds;
    public String weatherCity;
    public int weatherRefreshMinutes;
    public String photoDirectory;
    public int photoIntervalSeconds;
    public PhotoPlayMode photoPlayMode;
    public boolean keepScreenOn;

    public static AppSettings defaultSettings() {
        AppSettings settings = new AppSettings();
        settings.defaultMode = DisplayMode.DASHBOARD;
        settings.webUrl = "http://192.168.0.200:3000/d/kindle-hd8-overview/83869f0?orgId=1&from=now-1h&to=now&timezone=browser&refresh=30s";
        settings.webRefreshSeconds = 300;
        settings.weatherCity = "杭州";
        settings.weatherRefreshMinutes = 30;
        settings.photoDirectory = "/sdcard/Pictures";
        settings.photoIntervalSeconds = 10;
        settings.photoPlayMode = PhotoPlayMode.SEQUENTIAL;
        settings.keepScreenOn = true;
        return settings;
    }
}
