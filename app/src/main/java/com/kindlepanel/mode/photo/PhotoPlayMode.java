package com.kindlepanel.mode.photo;

import androidx.annotation.NonNull;

public enum PhotoPlayMode {
    SEQUENTIAL,
    RANDOM;

    @NonNull
    public static PhotoPlayMode fromValue(String value) {
        for (PhotoPlayMode mode : values()) {
            if (mode.name().equalsIgnoreCase(value)) {
                return mode;
            }
        }
        return SEQUENTIAL;
    }
}
