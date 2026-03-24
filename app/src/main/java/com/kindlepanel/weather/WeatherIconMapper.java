package com.kindlepanel.weather;

import androidx.annotation.DrawableRes;

import com.kindlepanel.R;

/**
 * 将内部天气类型映射到统一风格的本地图标资源。
 */
public final class WeatherIconMapper {

    private WeatherIconMapper() {
    }

    @DrawableRes
    public static int map(String conditionCode) {
        // 未识别到类型时统一回退为通用图标，避免界面空白。
        if ("clear_day".equalsIgnoreCase(conditionCode)) {
            return R.drawable.ic_weather_clear;
        }
        if ("clear_night".equalsIgnoreCase(conditionCode)) {
            return R.drawable.ic_weather_clear_night;
        }
        if ("cloudy".equalsIgnoreCase(conditionCode)) {
            return R.drawable.ic_weather_cloudy;
        }
        if ("fog".equalsIgnoreCase(conditionCode)) {
            return R.drawable.ic_weather_fog;
        }
        if ("rain".equalsIgnoreCase(conditionCode)) {
            return R.drawable.ic_weather_rain;
        }
        if ("snow".equalsIgnoreCase(conditionCode)) {
            return R.drawable.ic_weather_snow;
        }
        if ("storm".equalsIgnoreCase(conditionCode)) {
            return R.drawable.ic_weather_storm;
        }
        return R.drawable.ic_weather_unknown;
    }
}
