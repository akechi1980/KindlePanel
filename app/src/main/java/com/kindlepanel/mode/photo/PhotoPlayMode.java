package com.kindlepanel.mode.photo;

import androidx.annotation.NonNull;

/**
 * 相册播放方式。
 */
public enum PhotoPlayMode {
    SEQUENTIAL,
    RANDOM;

    @NonNull
    public static PhotoPlayMode fromValue(String value) {
        // 无效配置时按顺序播放兜底。
        for (PhotoPlayMode mode : values()) {
            if (mode.name().equalsIgnoreCase(value)) {
                return mode;
            }
        }
        return SEQUENTIAL;
    }
}
