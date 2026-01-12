package ru.mmb.sportiduinomanager.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.HashMap;
import java.util.Map;

public class DebounceUtil {
    @FunctionalInterface
    public interface DebounceCallback {
        void execute(String query);
    }

    private static final Map<String, Handler> handlers = new HashMap<>();
    private static final Map<String, Runnable> runnables = new HashMap<>();

    public static void debounce(String key, String query, int delayMillis, DebounceCallback callback) {
        cancelDebounce(key);

        Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable = () -> callback.execute(query);

        handlers.put(key, handler);
        runnables.put(key, runnable);

        handler.postDelayed(runnable, delayMillis);
    }

    public static void cancelDebounce(String key) {
        Handler handler = handlers.remove(key);
        Runnable runnable = runnables.remove(key);

        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
}
