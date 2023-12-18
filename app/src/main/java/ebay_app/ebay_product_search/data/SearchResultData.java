package ebay_app.ebay_product_search.data;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import ebay_app.ebay_product_search.EbayProductSearchApplication;
import edu.gyaneshm.ebay_product_search.R;
import ebay_app.ebay_product_search.models.SearchFormModel;
import ebay_app.ebay_product_search.models.SearchResultModel;
import ebay_app.ebay_product_search.listeners.DataFetchingListener;
import ebay_app.ebay_product_search.shared.Utils;

public class SearchResultData {
    private static SearchResultData mSearchResultData = null;

    private SearchFormModel searchFormData;

    private boolean dataFetched = false;
    private String errorMessage = null;
    private ArrayList<SearchResultModel> searchResults = new ArrayList<>();

    private ArrayList<DataFetchingListener> mCallbacks = new ArrayList<>();

    private RequestQueue mRequestQueue = null;

    private SearchResultData() {
    }

    public static SearchResultData getInstance() {
        if (mSearchResultData == null) {
            mSearchResultData = new SearchResultData();
        }
        return mSearchResultData;
    }

    public void setSearchFormData(SearchFormModel searchFormData) {
        this.searchFormData = searchFormData;
        errorMessage = null;
        dataFetched = false;
        searchResults.clear();
    }

//    public void fetchData(AppCompatActivity activityContext) {
//        String baseUrl = AppConfig.getApiEndPoint(activityContext) + "/searchProductResults?";
//        String requestUrl = baseUrl + searchFormData.toQueryParams();
//        Log.d("LoadSearchResults", "URL: " + requestUrl);
//
//        dataFetched = false;
//
//        JsonArrayRequest request = new JsonArrayRequest(
//                Request.Method.GET,
//                requestUrl,
//                null,
//                this::handleResponse,
//                this::handleError
//        );
//        request.setTag("SearchResultsRequest");
//
//        initializeRequestQueue(activityContext);
//        mRequestQueue.add(request);
//    }
//
//    private void handleResponse(JSONArray response) {
//        Log.d("LoadSearchResults", "Response received");
//        resetErrorMessage();
//        parseSearchResults(response);
//        dataFetched = true;
//        notifyDataFetchedListeners();
//    }
//
//    private void handleError(VolleyError error) {
//        Log.e("LoadSearchResults", "Error: " + error.toString());
//        setErrorMessageBasedOnError(error);
//        dataFetched = true;
//        notifyDataFetchedListeners();
//    }
//
//    // ... [rest of the class methods remain unchanged]
//
//    private void initializeRequestQueue(AppCompatActivity context) {
//        if (mRequestQueue == null) {
//            mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
//        }
//    }
//
//    private void resetErrorMessage() {
//        errorMessage = null;
//    }
//
//    private void setErrorMessageBasedOnError(VolleyError error) {
//        if (error instanceof NetworkError) {
//            errorMessage = Utils.getString(R.string.not_connected_to_internet);
//        } else if (error.networkResponse != null && error.networkResponse.data != null) {
//            errorMessage = new String(error.networkResponse.data, StandardCharsets.UTF_8);
//        } else {
//            errorMessage = Utils.getString(R.string.search_results_no_records);
//        }
//    }
//
//    private void parseSearchResults(JSONArray response) {
//        try {
//            if (response.length() == 0) {
//                errorMessage = "No data found.";
//                Log.d("LoadSearchResults", "No data found");
//                return;
//            }
//
//            for (int i = 0; i < response.length(); i++) {
//                JSONObject result = response.getJSONObject(i);
//                SearchResultModel searchResult = new SearchResultModel(result);
//                searchResults.add(searchResult);
//                Log.d("SearchResult", "Item " + i + ": " + searchResult.toString());
//            }
//        } catch (JSONException e) {
//            errorMessage = "Error in processing data.";
//            Log.e("LoadSearchResults", "Error processing JSON", e);
//        }
//    }
//
//    private void notifyDataFetchedListeners() {
//        for (DataFetchingListener listener : mCallbacks) {
////            listener.dataSuccessfullyFetched();
//        }
//    }

    public void fetchData(AppCompatActivity activity) {
        String apiEndPoint = AppConfig.getApiEndPoint(activity) + "/getProductSearchResult?";
        String finalUrl = apiEndPoint + searchFormData.toQueryParams();
        Log.i("Fetch URL", finalUrl);
        dataFetched = false;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                finalUrl,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.i("FetchData", "Received response");
                        errorMessage = null;
                        try {
                            if (response.length() == 0) {
                                errorMessage = "No Records.";
                                Log.i("FetchData", "No records found in response");
                            } else {
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject result = response.getJSONObject(i);
                                    SearchResultModel item = new SearchResultModel(result);
                                    searchResults.add(item);
                                    Log.i("SearchResult", "Item " + i + ": " + item.toString());
                                }
                            }
                        } catch (Exception e) {
                            errorMessage = "Error processing data.";
                            Log.e("FetchData", "Error parsing JSON array", e);
                        }
                        dataFetched = true;
                        sendNotification();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("FetchData", "Error during request: " + error.toString());
                        if (error instanceof NetworkError) {
                            errorMessage = Utils.getString(R.string.not_connected_to_internet);
                        } else if (error != null && error.networkResponse != null && error.networkResponse.data != null) {
                            errorMessage = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                        } else {
                            errorMessage = Utils.getString(R.string.search_results_no_records);
                        }
                        dataFetched = true;
                        sendNotification();
                    }
                }
        );
        jsonArrayRequest.setTag("results");

        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(EbayProductSearchApplication.getInstance().getApplicationContext());
        }
        mRequestQueue.add(jsonArrayRequest);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public ArrayList<SearchResultModel> getSearchResults() {
        return searchResults;
    }

    public SearchFormModel getSearchFormData() {
        return searchFormData;
    }

    public boolean isDataFetched() {
        return dataFetched;
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

    public void cancelRequest() {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll("results");
        }
    }
}
