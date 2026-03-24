package com.kindlepanel.mode.web;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kindlepanel.R;
import com.kindlepanel.config.AppSettings;
import com.kindlepanel.config.SettingsRepository;

public class WebModeFragment extends Fragment {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (webView != null) {
                webView.reload();
                int delay = Math.max(settings.webRefreshSeconds, 30) * 1000;
                handler.postDelayed(this, delay);
            }
        }
    };

    private WebView webView;
    private TextView errorView;
    private AppSettings settings;
    private SettingsRepository settingsRepository;

    public static WebModeFragment newInstance() {
        return new WebModeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_web_mode, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        webView = view.findViewById(R.id.web_view);
        errorView = view.findViewById(R.id.text_web_error);
        settingsRepository = new SettingsRepository(requireContext());
        settings = settingsRepository.getSettings();
        setupWebView();
        loadConfiguredUrl();
    }

    @Override
    public void onStart() {
        super.onStart();
        handler.postDelayed(refreshRunnable, Math.max(settings.webRefreshSeconds, 30) * 1000L);
    }

    @Override
    public void onStop() {
        super.onStop();
        handler.removeCallbacks(refreshRunnable);
    }

    @Override
    public void onDestroyView() {
        handler.removeCallbacks(refreshRunnable);
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }
        super.onDestroyView();
    }

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportZoom(false);

        webView.setBackgroundColor(0xFF000000);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                errorView.setVisibility(View.GONE);
                persistCurrentUrl(url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                persistCurrentUrl(url);
            }

            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                super.doUpdateVisitedHistory(view, url, isReload);
                persistCurrentUrl(url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request,
                                        WebResourceError error) {
                if (request.isForMainFrame()) {
                    showError();
                }
            }

            @Override
            @SuppressWarnings("deprecation")
            public void onReceivedError(WebView view, int errorCode, String description,
                                        String failingUrl) {
                showError();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });
    }

    private void loadConfiguredUrl() {
        if (TextUtils.isEmpty(settings.webUrl)) {
            showError();
            return;
        }
        webView.loadUrl(settings.webUrl);
    }

    private void showError() {
        errorView.setVisibility(View.VISIBLE);
        errorView.setText(getString(R.string.web_error_hint));
    }

    private void persistCurrentUrl(@Nullable String url) {
        if (!TextUtils.isEmpty(url)) {
            settingsRepository.saveCurrentWebUrl(url);
        }
    }
}
