package com.kindlepanel.weather;

import androidx.annotation.NonNull;

/**
 * 将天气接口返回的天气码映射为中文描述和内部图标类型。
 */
public final class WeatherCodeMapper {

    private WeatherCodeMapper() {
    }

    @NonNull
    public static String toDescription(int weatherCode) {
        // 当前按 Open-Meteo / WMO 代码做简化映射。
        switch (weatherCode) {
            case 0:
                return "晴朗";
            case 1:
                return "晴间少云";
            case 2:
                return "局部多云";
            case 3:
                return "阴天";
            case 45:
                return "有雾";
            case 48:
                return "冻雾";
            case 51:
            case 53:
            case 55:
                return "毛毛雨";
            case 56:
            case 57:
                return "冻毛雨";
            case 61:
            case 63:
            case 65:
                return "下雨";
            case 66:
            case 67:
                return "冻雨";
            case 71:
            case 73:
            case 75:
            case 77:
                return "降雪";
            case 80:
            case 81:
            case 82:
                return "阵雨";
            case 85:
            case 86:
                return "阵雪";
            case 95:
                return "雷暴";
            case 96:
            case 99:
                return "雷暴伴冰雹";
            default:
                return "天气未知";
        }
    }

    @NonNull
    public static String toConditionCode(int weatherCode, boolean isDay) {
        // 图标类型保持少量枚举，便于统一暗色风格。
        if (weatherCode == 0) {
            return isDay ? "clear_day" : "clear_night";
        }
        if (weatherCode == 1 || weatherCode == 2 || weatherCode == 3) {
            return "cloudy";
        }
        if (weatherCode == 45 || weatherCode == 48) {
            return "fog";
        }
        if ((weatherCode >= 51 && weatherCode <= 67) || (weatherCode >= 80 && weatherCode <= 82)) {
            return "rain";
        }
        if ((weatherCode >= 71 && weatherCode <= 77) || weatherCode == 85 || weatherCode == 86) {
            return "snow";
        }
        if (weatherCode >= 95) {
            return "storm";
        }
        return "cloudy";
    }
}
