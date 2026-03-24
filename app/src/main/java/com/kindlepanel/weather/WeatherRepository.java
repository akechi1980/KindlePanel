package com.kindlepanel.weather;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 天气数据仓库。
 * 当前采用 Open-Meteo 公共接口，并在本地做简单缓存兜底。
 */
public class WeatherRepository {

    private static final String PREFS_NAME = "weather_cache";
    private static final String KEY_CITY_QUERY = "cached_city_query";
    private static final String KEY_CITY = "cached_city";
    private static final String KEY_DESCRIPTION = "cached_description";
    private static final String KEY_CURRENT_TEMP = "cached_current_temp";
    private static final String KEY_HIGH_TEMP = "cached_high_temp";
    private static final String KEY_LOW_TEMP = "cached_low_temp";
    private static final String KEY_CONDITION_CODE = "cached_condition_code";
    private static final String KEY_UPDATED_AT = "cached_updated_at";

    private static final String GEOCODING_URL = "https://geocoding-api.open-meteo.com/v1/search";
    private static final String FORECAST_URL = "https://api.open-meteo.com/v1/forecast";
    private static final ExecutorService NETWORK_EXECUTOR = Executors.newSingleThreadExecutor();

    private final SharedPreferences sharedPreferences;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public WeatherRepository(@NonNull Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void requestWeather(@NonNull String city, @NonNull Callback callback) {
        final String normalizedCity = city.trim();
        NETWORK_EXECUTOR.execute(() -> {
            WeatherInfo cachedInfo = loadCached(normalizedCity);
            try {
                // 网络成功时优先刷新缓存，并回到主线程更新界面。
                WeatherInfo weatherInfo = fetchWeather(normalizedCity);
                weatherInfo.fromCache = false;
                weatherInfo.updatedAtMillis = System.currentTimeMillis();
                saveCache(normalizedCity, weatherInfo);
                mainHandler.post(() -> callback.onWeatherLoaded(weatherInfo));
            } catch (Exception ignored) {
                // 网络失败时尽量回退缓存，避免页面直接空白。
                if (cachedInfo != null) {
                    cachedInfo.fromCache = true;
                    mainHandler.post(() -> callback.onWeatherLoaded(cachedInfo));
                } else {
                    mainHandler.post(callback::onWeatherUnavailable);
                }
            }
        });
    }

    @Nullable
    public WeatherInfo loadCached(@NonNull String city) {
        String cachedQuery = sharedPreferences.getString(KEY_CITY_QUERY, "");
        if (TextUtils.isEmpty(cachedQuery) || !cachedQuery.equalsIgnoreCase(city.trim())) {
            return null;
        }

        String cachedCity = sharedPreferences.getString(KEY_CITY, "");
        String description = sharedPreferences.getString(KEY_DESCRIPTION, "");
        String conditionCode = sharedPreferences.getString(KEY_CONDITION_CODE, "");
        if (TextUtils.isEmpty(description) || TextUtils.isEmpty(conditionCode)) {
            return null;
        }

        WeatherInfo info = new WeatherInfo();
        info.city = cachedCity;
        info.description = description;
        info.currentTemp = sharedPreferences.getInt(KEY_CURRENT_TEMP, 0);
        info.highTemp = sharedPreferences.getInt(KEY_HIGH_TEMP, 0);
        info.lowTemp = sharedPreferences.getInt(KEY_LOW_TEMP, 0);
        info.conditionCode = conditionCode;
        info.updatedAtMillis = sharedPreferences.getLong(KEY_UPDATED_AT, 0L);
        info.fromCache = true;
        return info;
    }

    @NonNull
    public WeatherInfo loadPreview(@NonNull String city) {
        WeatherInfo cachedInfo = loadCached(city);
        if (cachedInfo != null) {
            return cachedInfo;
        }

        // 首次加载前先返回占位信息，避免界面闪动。
        WeatherInfo info = new WeatherInfo();
        info.city = city;
        info.description = "天气加载中";
        info.currentTemp = 0;
        info.highTemp = 0;
        info.lowTemp = 0;
        info.conditionCode = "cloudy";
        info.updatedAtMillis = 0L;
        info.fromCache = false;
        return info;
    }

    @NonNull
    private WeatherInfo fetchWeather(@NonNull String city) throws Exception {
        GeoLocation geoLocation = requestGeoLocation(city);
        // 只请求当前温度和当天高低温，控制字段最小化。
        String forecastUrl = Uri.parse(FORECAST_URL).buildUpon()
                .appendQueryParameter("latitude", String.valueOf(geoLocation.latitude))
                .appendQueryParameter("longitude", String.valueOf(geoLocation.longitude))
                .appendQueryParameter("current", "temperature_2m,weather_code,is_day")
                .appendQueryParameter("daily", "weather_code,temperature_2m_max,temperature_2m_min")
                .appendQueryParameter("forecast_days", "1")
                .appendQueryParameter("timezone", "auto")
                .build()
                .toString();

        JSONObject root = new JSONObject(readText(forecastUrl));
        JSONObject current = root.getJSONObject("current");
        JSONObject daily = root.getJSONObject("daily");

        int weatherCode = current.getInt("weather_code");
        boolean isDay = current.optInt("is_day", 1) == 1;

        WeatherInfo info = new WeatherInfo();
        info.city = geoLocation.displayName;
        info.description = WeatherCodeMapper.toDescription(weatherCode);
        info.currentTemp = Math.round((float) current.getDouble("temperature_2m"));
        info.highTemp = Math.round((float) daily.getJSONArray("temperature_2m_max").getDouble(0));
        info.lowTemp = Math.round((float) daily.getJSONArray("temperature_2m_min").getDouble(0));
        info.conditionCode = WeatherCodeMapper.toConditionCode(weatherCode, isDay);
        return info;
    }

    @NonNull
    private GeoLocation requestGeoLocation(@NonNull String city) throws Exception {
        // 当前保守策略是取第一个城市匹配结果。
        String geocodingUrl = Uri.parse(GEOCODING_URL).buildUpon()
                .appendQueryParameter("name", city)
                .appendQueryParameter("count", "1")
                .appendQueryParameter("language", "zh")
                .appendQueryParameter("format", "json")
                .build()
                .toString();

        JSONObject root = new JSONObject(readText(geocodingUrl));
        JSONArray results = root.optJSONArray("results");
        if (results == null || results.length() == 0) {
            throw new IllegalStateException("No weather location found.");
        }

        JSONObject first = results.getJSONObject(0);
        GeoLocation geoLocation = new GeoLocation();
        geoLocation.latitude = first.getDouble("latitude");
        geoLocation.longitude = first.getDouble("longitude");
        geoLocation.displayName = first.optString("name", city);
        return geoLocation;
    }

    @NonNull
    private String readText(@NonNull String urlString) throws Exception {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            connection = (HttpURLConnection) new URL(urlString).openConnection();
            // 超时保持较短，避免差网络下长时间拖住界面刷新节奏。
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setUseCaches(false);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.connect();

            int code = connection.getResponseCode();
            if (code < 200 || code >= 300) {
                throw new IllegalStateException("Weather request failed: " + code);
            }

            inputStream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void saveCache(@NonNull String cityQuery, @NonNull WeatherInfo info) {
        // 缓存只保留展示所需字段，避免引入更重的本地存储方案。
        sharedPreferences.edit()
                .putString(KEY_CITY_QUERY, cityQuery)
                .putString(KEY_CITY, info.city)
                .putString(KEY_DESCRIPTION, info.description)
                .putInt(KEY_CURRENT_TEMP, info.currentTemp)
                .putInt(KEY_HIGH_TEMP, info.highTemp)
                .putInt(KEY_LOW_TEMP, info.lowTemp)
                .putString(KEY_CONDITION_CODE, info.conditionCode)
                .putLong(KEY_UPDATED_AT, info.updatedAtMillis)
                .apply();
    }

    public interface Callback {
        void onWeatherLoaded(@NonNull WeatherInfo weatherInfo);

        void onWeatherUnavailable();
    }

    /**
     * 简化后的地理坐标结果，只保存天气请求所需字段。
     */
    private static class GeoLocation {
        double latitude;
        double longitude;
        String displayName;
    }
}
