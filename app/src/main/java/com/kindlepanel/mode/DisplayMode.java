package com.kindlepanel.mode;

import androidx.annotation.NonNull;

/**
 * 主界面支持的三种展示模式。
 */
public enum DisplayMode {
    DASHBOARD,
    WEB,
    PHOTO;

    @NonNull
    public static DisplayMode fromValue(String value) {
        // 配置异常时回退到看板模式，保证应用仍可启动。
        for (DisplayMode mode : values()) {
            if (mode.name().equalsIgnoreCase(value)) {
                return mode;
            }
        }
        return DASHBOARD;
    }
}
