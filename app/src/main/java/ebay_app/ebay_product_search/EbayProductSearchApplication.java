package ebay_app.ebay_product_search;

import android.app.Application;

public class EbayProductSearchApplication extends Application {
    public static EbayProductSearchApplication ebayProductSearchApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        ebayProductSearchApplication = this;
    }

    public static EbayProductSearchApplication getInstance() {
        return ebayProductSearchApplication;
    }
}
