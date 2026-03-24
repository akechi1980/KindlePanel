package com.kindlepanel.config;

import com.kindlepanel.mode.DisplayMode;
import com.kindlepanel.mode.photo.PhotoPlayMode;

/**
 * 应用运行所需的轻量配置对象。
 * 这里仅保存简单字段，便于 SharedPreferences 直接读写。
 */
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
        // 默认值保持保守，优先让旧平板开箱即可进入看板模式。
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
