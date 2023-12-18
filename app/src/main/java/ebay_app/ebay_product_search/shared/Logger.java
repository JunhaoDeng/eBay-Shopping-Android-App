package ebay_app.ebay_product_search.shared;

import android.util.Log;

public class Logger {
    private static Logger mLogger = null;
    private final String TAG = "hw9_log";

    private Logger() {
    }

    public static Logger getInstance() {
        if (mLogger == null) {
            mLogger = new Logger();
        }
        return mLogger;
    }

    public void logError(String message) {
        Log.e(TAG, message);
    }

    public void logWarn(String message) {
        Log.w(TAG, message);
    }

    public void logDebug(String message) {
        Log.d(TAG, message);
    }
}
