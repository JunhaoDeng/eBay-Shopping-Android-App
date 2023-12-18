package ebay_app.ebay_product_search.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ebay_app.ebay_product_search.EbayProductSearchApplication;
import ebay_app.ebay_product_search.adapters.SimilarProductsAdapter;
import ebay_app.ebay_product_search.data.ProductData;
import ebay_app.ebay_product_search.listeners.DataFetchingListener;
import ebay_app.ebay_product_search.models.ProductModel;
import ebay_app.ebay_product_search.models.SearchResultModel;
import ebay_app.ebay_product_search.models.SimilarProductModel;
import edu.gyaneshm.ebay_product_search.R;

public class SimilarProductsFragment extends Fragment {
    private LayoutInflater mLayoutInflator;

    private LinearLayout mProgressBarContainer;
    private LinearLayout mSortingSection;
    private Spinner mCategorySortSpinner;
    private Spinner mSortOrderSpinner;
    private ScrollView mMainContainer;
    private RecyclerView mSearchItemsRecyclerView;
    private LinearLayout mSearchResultsContainer;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ProductData mProductDataInstance;
    private ProductModel productDetail;

    private boolean isFetched = false;
    private TextView mErrorTextView;
    private SearchResultModel searchResultItem;
    private ArrayList<SimilarProductModel> similarProductsList;
    private ArrayList<SimilarProductModel> similarProductsListOriginal;
    private RequestQueue mRequestQueue;

    private DataFetchingListener dataFetchingListener = new DataFetchingListener() {
        @Override
        public void dataSuccessFullyFetched() {
            if (!isFetched) {
                initiate();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.similar_product_activity_layout, container, false);
        mLayoutInflator = inflater;

        mProgressBarContainer = view.findViewById(R.id.progress_bar_container);

        mSortingSection = view.findViewById(R.id.sorting_section);
        mCategorySortSpinner = view.findViewById(R.id.category_sort_spinner);
        mSortOrderSpinner = view.findViewById(R.id.sort_order_spinner);

        mMainContainer = view.findViewById(R.id.main_container);
        mSearchItemsRecyclerView = view.findViewById(R.id.search_results_items);
        mSearchItemsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSwipeRefreshLayout = view.findViewById(R.id.similar_product_refresh);
        mSearchResultsContainer = view.findViewById(R.id.search_results_container);
        mSwipeRefreshLayout.setEnabled(false);

        mProductDataInstance = ProductData.getInstance();
        mProductDataInstance.registerCallback(dataFetchingListener);
        mErrorTextView = view.findViewById(R.id.error);

        initiate();
        initializeSortSpinner();

        return view;
    }

    private void initializeSortSpinner() {
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                getActivity(),
                R.array.category_sort,
                android.R.layout.simple_spinner_dropdown_item
        );
        mCategorySortSpinner.setAdapter(categoryAdapter);
        mCategorySortSpinner.setSelection(0);

        ArrayAdapter<CharSequence> sortOrderAdapter = ArrayAdapter.createFromResource(
                getActivity(),
                R.array.sort_order,
                android.R.layout.simple_spinner_dropdown_item
        );
        mSortOrderSpinner.setAdapter(sortOrderAdapter);
        mSortOrderSpinner.setSelection(0);

        mCategorySortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = mCategorySortSpinner.getSelectedItem().toString();
                if (selectedCategory.equals("Default")) {
                    mSortOrderSpinner.setEnabled(false);
                    if (similarProductsListOriginal != null) {
                        similarProductsList.clear();
                        similarProductsList.addAll(similarProductsListOriginal);
                        updateRecyclerView();
                    }
                } else {
                    mSortOrderSpinner.setEnabled(true);
                    sortAndDisplaySimilarProducts();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mSortOrderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortAndDisplaySimilarProducts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    private int extractDaysFromTimeLeft(String timeLeft) {
        try {
            Pattern pattern = Pattern.compile("(\\d+) Days Left");
            Matcher matcher = pattern.matcher(timeLeft);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        } catch (NumberFormatException e) {
            Log.e("SimilarProductsFragment", "Error parsing days from timeLeft: " + timeLeft, e);
        }
        return 0;
    }
    private void sortAndDisplaySimilarProducts() {
        if (similarProductsList == null || similarProductsList.isEmpty()) {
            return;
        }

        String selectedCategory = mCategorySortSpinner.getSelectedItem().toString();
        boolean isAscending = mSortOrderSpinner.getSelectedItem().toString().equals("Ascending");

        Collections.sort(similarProductsList, new Comparator<SimilarProductModel>() {
            @Override
            public int compare(SimilarProductModel o1, SimilarProductModel o2) {
                switch (selectedCategory) {
                    case "Name":
                        return compareValues(o1.getTitle(), o2.getTitle(), isAscending);
                    case "Price":
                        return compareValues(o1.getPrice(), o2.getPrice(), isAscending);
                    case "Days":
                        int days1 = extractDaysFromTimeLeft(o1.getTimeLeft());
                        int days2 = extractDaysFromTimeLeft(o2.getTimeLeft());
                        return compareValues(days1, days2, isAscending);
                    default:
                        return 0;
                }
            }
        });

        updateRecyclerView();
    }

    private <T extends Comparable<T>> int compareValues(T value1, T value2, boolean isAscending) {
        if (isAscending) {
            return value1.compareTo(value2);
        } else {
            return value2.compareTo(value1);
        }
    }

    private void initiate() {
        mProgressBarContainer.setVisibility(View.GONE);
        mMainContainer.setVisibility(View.GONE);
        mErrorTextView.setVisibility(View.GONE);

        isFetched = mProductDataInstance.isProductDetailFetched();
        if (isFetched) {
            checkError();
        } else {
            mProgressBarContainer.setVisibility(View.VISIBLE);
        }
    }

    private void checkError() {
        String errorMessage = mProductDataInstance.getProductDetailError();
        if (errorMessage == null) {
            setUpViews();
            return;
        }
        mErrorTextView.setVisibility(View.VISIBLE);
    }

    private void setUpViews() {
        productDetail = mProductDataInstance.getProductDetail();
        searchResultItem = mProductDataInstance.getItem();

        fetchSimilarProducts();

        mMainContainer.setVisibility(View.VISIBLE);
    }

    private void fetchSimilarProducts() {
        String productId = productDetail.getProductId(); // 获取产品ID
        String url = "https://ebayexpressbackend-233.wl.r.appspot.com/getSimilarItems?id=" + productId;
        Log.d("SimilarProductsFragment", "URL: " + url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("SimilarProductsFragment", "Response received: " + response.toString());
                        try {
                            JSONObject getSimilarItemsResponse = response.getJSONObject("getSimilarItemsResponse");
                            JSONArray items = getSimilarItemsResponse.getJSONObject("itemRecommendations").getJSONArray("item");
                            similarProductsList = new ArrayList<>();
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject itemObj = items.getJSONObject(i); // 获取单个 item 对象
                                SimilarProductModel item = new SimilarProductModel(itemObj);
                                similarProductsList.add(item); // 直接将 JSONObject 添加到列表
                                similarProductsListOriginal = new ArrayList<>(similarProductsList);
                                Log.d("SimilarProductsFragment", "item: " + item.toString());
                            }
                            // 数据获取和解析完成，更新RecyclerView
                            updateRecyclerView();
                            // 处理获取到的数据，例如更新UI
                        } catch (JSONException e) {
                            Log.e("SimilarProductsFragment", "JSON parsing error: ", e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("SimilarProductsFragment", "Volley error: ", error);
                    }
                }
        );
        jsonObjectRequest.setTag("similarProducts");

        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(EbayProductSearchApplication.getInstance().getApplicationContext());
        }
        mRequestQueue.add(jsonObjectRequest);
    }

    private void updateRecyclerView() {
        if (getActivity() == null || similarProductsList == null || similarProductsList.isEmpty()) {
            Log.e("SimilarProductsFragment", "Activity not attached or similarProductsList is empty.");
            return;
        }
        Log.d("SimilarProductsFragment", "Updating RecyclerView with " + similarProductsList.size() + " items.");

        if (mSearchItemsRecyclerView.getAdapter() != null) {
            mSearchItemsRecyclerView.getAdapter().notifyDataSetChanged();
        } else {
            SimilarProductsAdapter adapter = new SimilarProductsAdapter(getActivity(), similarProductsList);
            mSearchItemsRecyclerView.setAdapter(adapter);
        }
        mSearchItemsRecyclerView.setVisibility(View.VISIBLE);
        mSwipeRefreshLayout.setVisibility(View.VISIBLE);
        mSearchResultsContainer.setVisibility(View.VISIBLE);

    }
}