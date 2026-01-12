package ru.mmb.sportiduinomanager.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.HashMap;
import java.util.Map;

/**
 * Debounce util.
 */
public class DebounceUtil {
    /**
     * callback definition.
     */
    @FunctionalInterface
    public interface DebounceCallback {
        void execute(String query);
    }

    private static final Map<String, Handler> handlers = new HashMap<>();
    private static final Map<String, Runnable> runnables = new HashMap<>();

    /**
     * Schedule execution of `callback` with parameter `query` in `delayMillis` if there are no new
     * calls to this method with the same `key` during this time interval.
     *
     * @param key - key
     * @param query - query
     * @param delayMillis - timeout
     * @param callback - callback
     */
    public static void debounce(String key, String query, int delayMillis, DebounceCallback callback) {
        cancelDebounce(key);

        Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable = () -> callback.execute(query);

        handlers.put(key, handler);
        runnables.put(key, runnable);

        handler.postDelayed(runnable, delayMillis);
    }

    /**
     * cancel deferred execution if any exist with specified key.
     *
     * @param key - key
     */
    public static void cancelDebounce(String key) {
        Handler handler = handlers.remove(key);
        Runnable runnable = runnables.remove(key);

        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
}
