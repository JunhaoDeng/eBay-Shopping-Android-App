package ebay_app.ebay_product_search.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ebay_app.ebay_product_search.SearchResultActivity;
import ebay_app.ebay_product_search.adapters.WishlistRecyclerViewAdapter;
import edu.gyaneshm.ebay_product_search.R;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import ebay_app.ebay_product_search.ProductDetailActivity;
import ebay_app.ebay_product_search.adapters.SearchItemRecyclerViewAdapter;
import ebay_app.ebay_product_search.data.ProductData;
import ebay_app.ebay_product_search.data.SearchResultData;
import ebay_app.ebay_product_search.listeners.ItemClickListener;
import ebay_app.ebay_product_search.models.SearchFormModel;
import ebay_app.ebay_product_search.models.SearchResultModel;
import ebay_app.ebay_product_search.listeners.DataFetchingListener;
import ebay_app.ebay_product_search.shared.Utils;
import edu.gyaneshm.ebay_product_search.R;


public class WishlistFragment extends Fragment {
    private RecyclerView recyclerView;
    private SearchItemRecyclerViewAdapter adapter;
    private ArrayList<SearchResultModel> wishlistItems = new ArrayList<>();
    private RequestQueue mRequestQueue;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView errorTextView;
    private LinearLayout mSearchResultsContainer;
    private TextView wishlistTotalItemsTextView;
    private TextView wishlistTotalPriceTextView;
    private double totalPrice;
    private LinearLayout mWishlistTotalContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wishlist_layout, container, false);

        recyclerView = view.findViewById(R.id.search_results_items);
        swipeRefreshLayout = view.findViewById(R.id.search_results_refresh);
        errorTextView = view.findViewById(R.id.search_results_error);
        mSearchResultsContainer = view.findViewById(R.id.search_results_container);
        mWishlistTotalContainer = view.findViewById(R.id.wishlist_total_container);

        wishlistTotalItemsTextView = view.findViewById(R.id.wishlist_total_items_textview);
        wishlistTotalPriceTextView = view.findViewById(R.id.wishlist_total_price_textview);

        setupRecyclerView();
        setupSwipeRefreshLayout();
        fetchWishlistItems();

        return view;
    }

    private void updateWishlistTotalViews() {
        int totalItems = wishlistItems.size();
        totalPrice = calculateTotalPrice(wishlistItems); // 计算总价

        String totalItemsText = getResources().getString(R.string.wishlist_total_items, totalItems);
        String totalPriceText = getResources().getString(R.string.wishlist_total_price, totalPrice);

        wishlistTotalItemsTextView.setText(totalItemsText);
        wishlistTotalPriceTextView.setText(totalPriceText);
    }

    // 计算总价
    private double calculateTotalPrice(ArrayList<SearchResultModel> wishlistItems) {
        double total = 0;
        for (SearchResultModel item : wishlistItems) {
            total += item.getPrice() + Double.parseDouble(item.getShippingInfo());
        }
        return total;
    }



    @Override
    public void onResume() {
        super.onResume();
        fetchWishlistItems();
    }


    private void setupRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter = new SearchItemRecyclerViewAdapter(getContext(), wishlistItems, item -> {
        });
        adapter.setWishlistUpdateListener(() -> fetchWishlistItems());
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(this::fetchWishlistItems);
    }

    public void fetchWishlistItems() {
        swipeRefreshLayout.setRefreshing(true);
        String url = "https://ebayexpressbackend-233.wl.r.appspot.com/mongodb/getWishList";
        mRequestQueue = Volley.newRequestQueue(getContext());

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    wishlistItems.clear();
                    parseResponse(response);
                    setUpSearchItemRecycleView();
                },
                error -> {
                    handleErrorResponse(error);
                    setUpSearchItemRecycleView();
                }
        );

        mRequestQueue.add(jsonArrayRequest);
    }

    private void parseResponse(JSONArray response) {
        try {
            for (int i = 0; i < response.length(); i++) {
                JSONObject result = response.getJSONObject(i);
                SearchResultModel item = new SearchResultModel(result);
                wishlistItems.add(item);
            }
            updateWishlistTotalViews();
        } catch (Exception e) {
            Log.e("WishlistFetchData", "Error parsing JSON array", e);
            errorTextView.setText("Error processing data.");
        }
    }

    private void handleErrorResponse(VolleyError error) {
        Log.e("FetchData", "Error during request: " + error.toString());
        if (error instanceof NetworkError) {
            errorTextView.setText(Utils.getString(R.string.not_connected_to_internet));
        } else {
            errorTextView.setText(Utils.getString(R.string.wish_list_no_item));
        }
    }

    private void setUpSearchItemRecycleView() {
        swipeRefreshLayout.setRefreshing(false);

        if (wishlistItems.isEmpty()) {
            errorTextView.setVisibility(View.VISIBLE);
            mSearchResultsContainer.setVisibility(View.GONE);
            mWishlistTotalContainer.setVisibility(View.GONE);
        } else {
            errorTextView.setVisibility(View.GONE);
            mSearchResultsContainer.setVisibility(View.VISIBLE);
            mWishlistTotalContainer.setVisibility(View.VISIBLE);
            updateWishlistTotalViews();
        }
        adapter.notifyDataSetChanged();
    }
}