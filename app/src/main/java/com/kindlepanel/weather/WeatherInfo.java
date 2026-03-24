package com.kindlepanel.weather;

/**
 * 看板天气展示所需的最小数据结构。
 */
public class WeatherInfo {
    public String city;
    public String description;
    public int currentTemp;
    public int highTemp;
    public int lowTemp;
    public String conditionCode;
    public long updatedAtMillis;
    public boolean fromCache;
}
