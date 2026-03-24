package com.kindlepanel.mode.dashboard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kindlepanel.R;

public class FlipDigitView extends FrameLayout {

    private TextView currentDigitView;
    private TextView incomingDigitView;
    private char currentDigit = '\0';

    public FlipDigitView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public FlipDigitView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FlipDigitView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(@NonNull Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_flip_digit, this, true);
        currentDigitView = findViewById(R.id.text_digit_current);
        incomingDigitView = findViewById(R.id.text_digit_incoming);
    }

    public void setDigit(char digit, boolean animate) {
        if (currentDigit == digit) {
            return;
        }

        if (currentDigit == '\0' || !animate) {
            currentDigit = digit;
            currentDigitView.setText(String.valueOf(digit));
            incomingDigitView.setText("");
            incomingDigitView.setAlpha(0f);
            return;
        }

        currentDigit = digit;
        incomingDigitView.animate().cancel();
        currentDigitView.animate().cancel();

        incomingDigitView.setText(String.valueOf(digit));
        incomingDigitView.setTranslationY(getHeight() * 0.18f);
        incomingDigitView.setScaleY(1.08f);
        incomingDigitView.setAlpha(0f);

        currentDigitView.animate()
                .translationY(-getHeight() * 0.12f)
                .scaleY(0.92f)
                .alpha(0f)
                .setDuration(140L)
                .start();

        incomingDigitView.animate()
                .translationY(0f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(160L)
                .withEndAction(() -> {
                    currentDigitView.setText(String.valueOf(digit));
                    currentDigitView.setTranslationY(0f);
                    currentDigitView.setScaleY(1f);
                    currentDigitView.setAlpha(1f);
                    incomingDigitView.setText("");
                    incomingDigitView.setAlpha(0f);
                    incomingDigitView.setTranslationY(0f);
                    incomingDigitView.setScaleY(1f);
                })
                .start();
    }
}
