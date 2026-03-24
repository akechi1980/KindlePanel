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
        // 默认网页地址使用通用示例，避免把个人内网地址写入公开仓库。
        settings.webUrl = "http://192.168.1.100:3000";
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
