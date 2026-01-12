package ru.mmb.sportiduinomanager.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Debounce util.
 */
public final class DebounceUtil {
    /**
     * handlers map.
     */
    private static final Map<String, Handler> HANDLERS = new ConcurrentHashMap<>();
    /**
     * runnables map.
     */
    private static final Map<String, Runnable> RUNNABLES = new ConcurrentHashMap<>();

    private DebounceUtil() {

    }

    /**
     * callback definition.
     */
    @FunctionalInterface
    public interface DebounceCallback {
        /**
         * callback method.
         *
         * @param query - callback parameter
         */
        void execute(String query);
    }

    /**
     * Schedule execution of `callback` with parameter `query` in `delayMillis`
     * if there are no new calls to this method with the same `key`
     * during this time interval.
     *
     * @param key - key
     * @param query - query
     * @param delayMillis - timeout
     * @param callback - callback
     */
    public static void debounce(final String key, final String query, final int delayMillis,
                                final DebounceCallback callback) {
        cancelDebounce(key);

        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable runnable = () -> callback.execute(query);

        HANDLERS.put(key, handler);
        RUNNABLES.put(key, runnable);

        handler.postDelayed(runnable, delayMillis);
    }

    /**
     * cancel deferred execution if any exist with specified key.
     *
     * @param key - key
     */
    public static void cancelDebounce(final String key) {
        final Handler handler = HANDLERS.remove(key);
        final Runnable runnable = RUNNABLES.remove(key);

        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
}
