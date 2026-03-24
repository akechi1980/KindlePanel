package com.kindlepanel.mode.photo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class PhotoModeFragment extends Fragment {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private final Runnable photoRunnable = new Runnable() {
        @Override
        public void run() {
            showNextPhoto();
            int delay = Math.max(settings.photoIntervalSeconds, 3) * 1000;
            handler.postDelayed(this, delay);
        }
    };

    private ImageView imageView;
    private TextView placeholderView;
    private AppSettings settings;
    private List<File> images = new ArrayList<>();
    private int currentIndex;

    public static PhotoModeFragment newInstance() {
        return new PhotoModeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photo_mode, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageView = view.findViewById(R.id.image_photo);
        placeholderView = view.findViewById(R.id.text_photo_placeholder);
        settings = new SettingsRepository(requireContext()).getSettings();
        images = loadImages(settings.photoDirectory);
        showNextPhoto();
    }

    @Override
    public void onStart() {
        super.onStart();
        handler.post(photoRunnable);
    }

    @Override
    public void onStop() {
        super.onStop();
        handler.removeCallbacks(photoRunnable);
    }

    private void showNextPhoto() {
        if (images.isEmpty()) {
            placeholderView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            placeholderView.setText(getString(R.string.photo_empty_hint, settings.photoDirectory));
            return;
        }

        placeholderView.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE);

        File file;
        if (settings.photoPlayMode == PhotoPlayMode.RANDOM) {
            file = images.get(random.nextInt(images.size()));
        } else {
            file = images.get(currentIndex);
            currentIndex = (currentIndex + 1) % images.size();
        }

        imageView.setAlpha(0f);
        Bitmap bitmap = decodeSampledBitmap(file);
        if (bitmap == null) {
            placeholderView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            placeholderView.setText(getString(R.string.photo_decode_error_hint, file.getName()));
            return;
        }
        imageView.setImageBitmap(bitmap);
        imageView.animate().alpha(1f).setDuration(180L).start();
    }

    @NonNull
    private List<File> loadImages(@Nullable String directoryPath) {
        if (TextUtils.isEmpty(directoryPath)) {
            return Collections.emptyList();
        }

        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            return Collections.emptyList();
        }

        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }

        List<File> result = new ArrayList<>();
        for (File file : files) {
            if (file.isFile() && isSupportedImage(file.getName())) {
                result.add(file);
            }
        }
        result.sort((left, right) -> left.getName().toLowerCase(Locale.US)
                .compareTo(right.getName().toLowerCase(Locale.US)));
        return result;
    }

    private boolean isSupportedImage(@NonNull String name) {
        String lower = name.toLowerCase(Locale.US);
        return lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".png")
                || lower.endsWith(".webp")
                || lower.endsWith(".bmp");
    }

    @Nullable
    private Bitmap decodeSampledBitmap(@NonNull File file) {
        BitmapFactory.Options boundsOptions = new BitmapFactory.Options();
        boundsOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), boundsOptions);

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        decodeOptions.inSampleSize = calculateInSampleSize(boundsOptions.outWidth, boundsOptions.outHeight);
        return BitmapFactory.decodeFile(file.getAbsolutePath(), decodeOptions);
    }

    private int calculateInSampleSize(int width, int height) {
        int reqWidth = imageView.getWidth() > 0 ? imageView.getWidth() : 1280;
        int reqHeight = imageView.getHeight() > 0 ? imageView.getHeight() : 800;
        int inSampleSize = 1;
        while ((width / inSampleSize) > reqWidth * 2 || (height / inSampleSize) > reqHeight * 2) {
            inSampleSize *= 2;
        }
        return Math.max(inSampleSize, 1);
    }
}
