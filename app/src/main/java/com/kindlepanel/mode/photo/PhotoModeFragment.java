package com.kindlepanel.mode.photo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * 相册模式。
 * 负责从本地目录读取图片，并以顺序或随机方式轻量轮播。
 */
public class PhotoModeFragment extends Fragment {

    private static final String TAG = "PhotoMode";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private final Runnable photoRunnable = new Runnable() {
        @Override
        public void run() {
            showNextPhotoSafely();
            // 切换间隔设下限，防止误设为过快导致频繁解码。
            int delay = Math.max(settings.photoIntervalSeconds, 3) * 1000;
            handler.postDelayed(this, delay);
        }
    };

    private ImageView imageView;
    private TextView placeholderView;
    private AppSettings settings;
    private List<File> images = new ArrayList<>();
    private int currentIndex;
    private Bitmap currentBitmap;

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
        showNextPhotoSafely();
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

    @Override
    public void onDestroyView() {
        handler.removeCallbacks(photoRunnable);
        releaseCurrentBitmap();
        imageView = null;
        placeholderView = null;
        super.onDestroyView();
    }

    private void showNextPhotoSafely() {
        try {
            showNextPhoto();
        } catch (Throwable throwable) {
            // 旧设备上即使遇到坏图或内存压力，也优先保住主界面不退出。
            Log.e(TAG, "Failed to show photo", throwable);
            showPlaceholder(getString(R.string.photo_decode_error_hint, "图片不可用"));
        }
    }

    private void showNextPhoto() {
        if (images.isEmpty()) {
            showPlaceholder(getString(R.string.photo_empty_hint, settings.photoDirectory));
            return;
        }

        // 依次尝试可用图片，遇到单张坏图时直接跳过，不让整个模式失败。
        for (int attempt = 0; attempt < images.size(); attempt++) {
            File file = nextPhotoFile();
            try {
                Bitmap bitmap = decodeSampledBitmap(file);
                if (bitmap == null) {
                    Log.w(TAG, "Bitmap decode returned null for " + file.getAbsolutePath());
                    continue;
                }
                showBitmap(bitmap);
                return;
            } catch (OutOfMemoryError error) {
                Log.e(TAG, "Out of memory while decoding " + file.getAbsolutePath(), error);
                releaseCurrentBitmap();
            } catch (Throwable throwable) {
                Log.e(TAG, "Failed to decode " + file.getAbsolutePath(), throwable);
            }
        }

        showPlaceholder(getString(R.string.photo_decode_error_hint, settings.photoDirectory));
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
        // 旧版 Android / Fire OS 上避免使用 List.sort，改用兼容性更稳的 Collections.sort。
        Collections.sort(result, new Comparator<File>() {
            @Override
            public int compare(File left, File right) {
                return left.getName().toLowerCase(Locale.US)
                        .compareTo(right.getName().toLowerCase(Locale.US));
            }
        });
        Log.d(TAG, "Loaded " + result.size() + " images from " + directoryPath);
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
        // 先读尺寸再按目标区域缩放，降低大图加载时的内存占用。
        BitmapFactory.Options boundsOptions = new BitmapFactory.Options();
        boundsOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), boundsOptions);
        if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
            return null;
        }

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

    @NonNull
    private File nextPhotoFile() {
        if (settings.photoPlayMode == PhotoPlayMode.RANDOM) {
            return images.get(random.nextInt(images.size()));
        }
        File file = images.get(currentIndex);
        currentIndex = (currentIndex + 1) % images.size();
        return file;
    }

    private void showBitmap(@NonNull Bitmap bitmap) {
        if (placeholderView == null || imageView == null) {
            return;
        }
        placeholderView.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE);
        imageView.setAlpha(0f);
        releaseCurrentBitmap();
        currentBitmap = bitmap;
        imageView.setImageBitmap(bitmap);
        imageView.animate().alpha(1f).setDuration(180L).start();
    }

    private void showPlaceholder(@NonNull String message) {
        if (placeholderView == null || imageView == null) {
            return;
        }
        releaseCurrentBitmap();
        placeholderView.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.GONE);
        imageView.setImageDrawable(null);
        placeholderView.setText(message);
    }

    private void releaseCurrentBitmap() {
        if (currentBitmap != null && !currentBitmap.isRecycled()) {
            currentBitmap.recycle();
        }
        currentBitmap = null;
    }
}
