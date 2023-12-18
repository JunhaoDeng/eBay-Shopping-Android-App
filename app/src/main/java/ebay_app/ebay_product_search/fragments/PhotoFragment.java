package ebay_app.ebay_product_search.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ebay_app.ebay_product_search.EbayProductSearchApplication;
import ebay_app.ebay_product_search.listeners.DataFetchingListener;
import ebay_app.ebay_product_search.models.SearchResultModel;
import edu.gyaneshm.ebay_product_search.R;
import ebay_app.ebay_product_search.data.ProductData;
import ebay_app.ebay_product_search.models.ProductModel;

public class PhotoFragment extends Fragment {
    private LayoutInflater mLayoutInflator;

    private LinearLayout mProgressBarContainer;
    private ScrollView mMainContainer;
    private LinearLayout mGalleryLinearLayout;

    private ProductData mProductDataInstance;
    private ProductModel productDetail;

    private boolean isFetched = false;
    private TextView mErrorTextView;
    private SearchResultModel searchResultItem;
    private ArrayList<String> productLinks;
    private RequestQueue mRequestQueue = null;; // Volley 请求队列

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
        View view = inflater.inflate(R.layout.photo_fragment_layout, container, false);
        mLayoutInflator = inflater;

        mProgressBarContainer = view.findViewById(R.id.progress_bar_container);
        mMainContainer = view.findViewById(R.id.main_container);
        mGalleryLinearLayout = view.findViewById(R.id.image_gallery);

        mProductDataInstance = ProductData.getInstance();
        mProductDataInstance.registerCallback(dataFetchingListener);
        mErrorTextView = view.findViewById(R.id.error);

        initiate();

        return view;
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
        String title = productDetail.getTitle();
        String encodedTitle = Uri.encode(title);
        String url = "https://ebayexpressbackend-233.wl.r2.appspot.com/getProductPhotos?title=" + encodedTitle;
        Log.d("PhotoFragment", "URL: " + url);
        fetchProductPhotos(url);


        setUpImageGallery();


        mMainContainer.setVisibility(View.VISIBLE);
    }

    private void fetchProductPhotos(String url) {
        Log.d("PhotoFragment", "fetchProductPhotos: " + url);
        productLinks = new ArrayList<>(); // 初始化 productLinks 列表
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("PhotoFragment", "Response received: " + response.toString());
                        try {
                            JSONArray items = response.getJSONArray("items");
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject item = items.getJSONObject(i); // 获取单个 item 对象
                                String photoLink = item.getString("link"); // 提取 link 字段
                                Log.d("PhotoFragment", "Photo link: " + photoLink);
                                productLinks.add(photoLink);
                            }
                            setUpImageGallery();
                        } catch (JSONException e) {
                            Log.e("PhotoFragment", "JSON parsing error: ", e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("PhotoFragment", "Volley error: ", error);
                    }
                }
        );
        jsonObjectRequest.setTag("productPhotos");

        if (mRequestQueue == null) {
            Log.d("PhotoFragment", "Initializing request queue");
            mRequestQueue = Volley.newRequestQueue(EbayProductSearchApplication.getInstance().getApplicationContext());
        }
        mRequestQueue.add(jsonObjectRequest);
    }

    private void setUpImageGallery() {
        ArrayList<String> images = productLinks;
        if (images.size() == 0) {
            mMainContainer.setVisibility(View.GONE); // 如果没有图片，隐藏 ScrollView
            return;
        }

        for (int i = 0; i < images.size(); i++) {
            View view = mLayoutInflator.inflate(R.layout.product_image_gallery_item_for_photo, mGalleryLinearLayout, false);
            ImageView imageView = view.findViewById(R.id.image);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, // 宽度匹配父布局
                    LinearLayout.LayoutParams.MATCH_PARENT  // 高度匹配父布局
            );
            imageView.setLayoutParams(layoutParams);

            Glide.with(getContext())
                    .load(images.get(i))
                    .into(imageView);
            mGalleryLinearLayout.addView(view);
        }

        mMainContainer.setVisibility(View.VISIBLE); // 显示 ScrollView
    }
}