package com.kindlepanel.weather;

import androidx.annotation.DrawableRes;

import com.kindlepanel.R;

public final class WeatherIconMapper {

    private WeatherIconMapper() {
    }

    @DrawableRes
    public static int map(String conditionCode) {
        if ("clear".equalsIgnoreCase(conditionCode)) {
            return R.drawable.ic_weather_clear;
        }
        return R.drawable.ic_weather_unknown;
    }
}
