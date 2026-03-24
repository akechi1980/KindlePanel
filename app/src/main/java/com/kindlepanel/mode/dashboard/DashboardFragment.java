package com.kindlepanel.mode.dashboard;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kindlepanel.R;
import com.kindlepanel.config.AppSettings;
import com.kindlepanel.config.SettingsRepository;
import com.kindlepanel.weather.WeatherIconMapper;
import com.kindlepanel.weather.WeatherInfo;
import com.kindlepanel.weather.WeatherRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 本地看板模式。
 * 左侧显示日期和翻页时钟，右侧显示简化天气信息。
 */
public class DashboardFragment extends Fragment {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable clockRunnable = new Runnable() {
        @Override
        public void run() {
            updateClock();
            handler.postDelayed(this, 1000L);
        }
    };
    private final Runnable weatherRunnable = new Runnable() {
        @Override
        public void run() {
            requestWeatherRefresh();
            // 刷新频率设下限，避免旧设备或弱网络环境下请求过密。
            int refreshMinutes = Math.max(settingsRepository.getSettings().weatherRefreshMinutes, 10);
            handler.postDelayed(this, refreshMinutes * 60L * 1000L);
        }
    };

    private TextView dateView;
    private TextView weekView;
    private TextView colonView;
    private TextView cityView;
    private TextView descView;
    private TextView tempView;
    private TextView rangeView;
    private ImageView weatherIconView;
    private FlipDigitView hourTensView;
    private FlipDigitView hourOnesView;
    private FlipDigitView minuteTensView;
    private FlipDigitView minuteOnesView;
    private String lastRenderedTime = "";
    private SettingsRepository settingsRepository;
    private WeatherRepository weatherRepository;

    public static DashboardFragment newInstance() {
        return new DashboardFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dateView = view.findViewById(R.id.text_date);
        weekView = view.findViewById(R.id.text_week);
        colonView = view.findViewById(R.id.text_time_colon);
        cityView = view.findViewById(R.id.text_city);
        descView = view.findViewById(R.id.text_weather_desc);
        tempView = view.findViewById(R.id.text_current_temp);
        rangeView = view.findViewById(R.id.text_temp_range);
        weatherIconView = view.findViewById(R.id.image_weather_icon);
        hourTensView = view.findViewById(R.id.digit_hour_tens);
        hourOnesView = view.findViewById(R.id.digit_hour_ones);
        minuteTensView = view.findViewById(R.id.digit_minute_tens);
        minuteOnesView = view.findViewById(R.id.digit_minute_ones);
        settingsRepository = new SettingsRepository(requireContext());
        weatherRepository = new WeatherRepository(requireContext());

        updateClock();
        updateWeather();
    }

    @Override
    public void onStart() {
        super.onStart();
        handler.post(clockRunnable);
        handler.post(weatherRunnable);
    }

    @Override
    public void onStop() {
        super.onStop();
        handler.removeCallbacks(clockRunnable);
        handler.removeCallbacks(weatherRunnable);
    }

    private void updateClock() {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        SimpleDateFormat weekFormat = new SimpleDateFormat("EEEE", Locale.CHINA);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);

        dateView.setText(dateFormat.format(now));
        weekView.setText(weekFormat.format(now));

        String newTime = timeFormat.format(now);
        // 冒号只做轻量闪烁，保留节奏感但不做重型动画。
        colonView.animate().cancel();
        colonView.setAlpha(now.getSeconds() % 2 == 0 ? 1f : 0.6f);

        if (!newTime.equals(lastRenderedTime)) {
            bindTimeDigits(newTime, !lastRenderedTime.isEmpty());
            lastRenderedTime = newTime;
        }
    }

    private void bindTimeDigits(@NonNull String timeValue, boolean animate) {
        if (timeValue.length() < 5) {
            return;
        }

        hourTensView.setDigit(timeValue.charAt(0), animate);
        hourOnesView.setDigit(timeValue.charAt(1), animate);
        minuteTensView.setDigit(timeValue.charAt(3), animate);
        minuteOnesView.setDigit(timeValue.charAt(4), animate);
    }

    private void updateWeather() {
        AppSettings settings = settingsRepository.getSettings();
        // 先显示缓存或占位内容，让页面首帧更稳定。
        bindWeather(weatherRepository.loadPreview(settings.weatherCity));
    }

    private void requestWeatherRefresh() {
        AppSettings settings = settingsRepository.getSettings();
        weatherRepository.requestWeather(settings.weatherCity, new WeatherRepository.Callback() {
            @Override
            public void onWeatherLoaded(@NonNull WeatherInfo weatherInfo) {
                if (!isAdded()) {
                    return;
                }
                bindWeather(weatherInfo);
            }

            @Override
            public void onWeatherUnavailable() {
                if (!isAdded()) {
                    return;
                }
                WeatherInfo preview = weatherRepository.loadPreview(settings.weatherCity);
                // 失败时保留可展示内容，只替换为明确中文提示。
                preview.description = "天气获取失败";
                bindWeather(preview);
            }
        });
    }

    private void bindWeather(@NonNull WeatherInfo info) {
        weatherIconView.setImageResource(WeatherIconMapper.map(info.conditionCode));
        cityView.setText(info.city);
        descView.setText(info.description);
        tempView.setText(getString(R.string.weather_temp_format, info.currentTemp));
        rangeView.setText(getString(R.string.weather_range_format, info.highTemp, info.lowTemp));
    }
}
