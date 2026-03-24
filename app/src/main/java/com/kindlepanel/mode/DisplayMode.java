package com.kindlepanel.mode;

import androidx.annotation.NonNull;

public enum DisplayMode {
    DASHBOARD,
    WEB,
    PHOTO;

    @NonNull
    public static DisplayMode fromValue(String value) {
        for (DisplayMode mode : values()) {
            if (mode.name().equalsIgnoreCase(value)) {
                return mode;
            }
        }
        return DASHBOARD;
    }
}
