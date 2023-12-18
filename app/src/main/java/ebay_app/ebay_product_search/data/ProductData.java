package ebay_app.ebay_product_search.data;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import ebay_app.ebay_product_search.EbayProductSearchApplication;
import edu.gyaneshm.ebay_product_search.R;
import ebay_app.ebay_product_search.listeners.DataFetchingListener;
import ebay_app.ebay_product_search.models.ProductModel;
import ebay_app.ebay_product_search.models.SearchResultModel;
import ebay_app.ebay_product_search.shared.Utils;

public class ProductData {
    private static ProductData instance;

    private static SearchResultModel item;
    private RequestQueue mRequestQueue;

    private boolean productDetailFetched;

    private String productDetailError;

    private ProductModel mProductDetail;

    private ArrayList<DataFetchingListener> mCallbacks = new ArrayList<>();

    private ProductData() {
    }

    public static ProductData getInstance() {
        if (instance == null) {
            instance = new ProductData();
        }
        return instance;
    }

    public void fetchProductDetail(AppCompatActivity activity) {
        String apiEndPoint = AppConfig.getApiEndPoint(activity) + "/getProductDetails?";
        String finalUrl = apiEndPoint+"id="+item.getProductId();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                finalUrl,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mProductDetail = new ProductModel(response);
                        productDetailFetched = true;
                        sendNotification();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof NetworkError) {
                            productDetailError = Utils.getString(R.string.not_connected_to_internet);
                        } else if (error != null && error.networkResponse != null && error.networkResponse.data != null) {
                            productDetailError = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                        } else {
                            productDetailError = Utils.getString(R.string.no_response_from_server);
                        }
                        Utils.showToast(Utils.getString(R.string.error_while_fetching_product_details), productDetailError);
                        productDetailFetched = true;
                        sendNotification();
                    }
                }
        );
        jsonObjectRequest.setTag("productDetail");

        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(EbayProductSearchApplication.getInstance().getApplicationContext());
        }
        mRequestQueue.add(jsonObjectRequest);
    }

    public void setItem(SearchResultModel item) {
        this.item = item;
        productDetailFetched = false;
        productDetailError = null;
    }

    public SearchResultModel getItem() {
        return item;
    }

    public ProductModel getProductDetail() {
        return mProductDetail;
    }

    public boolean isProductDetailFetched() {
        return productDetailFetched;
    }

    public String getProductDetailError() {
        return productDetailError;
    }

    public void registerCallback(DataFetchingListener callback) {
        mCallbacks.add(callback);
    }

    public void unregisterCallback(DataFetchingListener callback) {
        mCallbacks.remove(callback);
    }

    private void sendNotification() {
        for (int i = 0; i < mCallbacks.size(); i++) {
            mCallbacks.get(i).dataSuccessFullyFetched();
        }
    }
}
