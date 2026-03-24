package com.kindlepanel.weather;

import androidx.annotation.NonNull;

public class WeatherRepository {

    @NonNull
    public WeatherInfo loadPreview(@NonNull String city) {
        WeatherInfo info = new WeatherInfo();
        info.city = city;
        info.description = "天气模块待接入";
        info.currentTemp = 24;
        info.highTemp = 27;
        info.lowTemp = 19;
        info.conditionCode = "clear";
        return info;
    }
}
