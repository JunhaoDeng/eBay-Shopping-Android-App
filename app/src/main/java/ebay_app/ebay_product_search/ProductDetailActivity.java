package ebay_app.ebay_product_search;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Iterator;

import ebay_app.ebay_product_search.adapters.ViewPageAdapter;
import ebay_app.ebay_product_search.data.AppConfig;
import ebay_app.ebay_product_search.data.ProductData;
import ebay_app.ebay_product_search.fragments.DetailFragment;
import ebay_app.ebay_product_search.fragments.PhotoFragment;
import ebay_app.ebay_product_search.fragments.SellerFragment;
import ebay_app.ebay_product_search.fragments.ShippingFragment;
import ebay_app.ebay_product_search.fragments.SimilarProductsFragment;
import ebay_app.ebay_product_search.models.ProductModel;
import ebay_app.ebay_product_search.models.SearchResultModel;
import ebay_app.ebay_product_search.shared.Utils;
import edu.gyaneshm.ebay_product_search.R;

public class ProductDetailActivity extends AppCompatActivity {
    private ActionBar mActionBar;
    private TabLayout mTabLayout;
    private ViewPageAdapter mViewPageAdapter;
    private ProductData mProductDataInstance;
    private SearchResultModel item;

    private String mProductDetailId;

    private ImageView mCartPlusIcon;
    private ImageView mCartRemoveIcon;

    private RequestQueue mRequestQueue = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_activity_layout);

        mProductDataInstance = ProductData.getInstance();
        item = mProductDataInstance.getItem();

        mProductDetailId = item.getProductId();
        mCartPlusIcon = findViewById(R.id.cart_plus_icon_circle);
        mCartRemoveIcon = findViewById(R.id.cart_remove_icon_circle);

        mActionBar = getSupportActionBar();
        setUpActionBar();

        ViewPager viewPager = findViewById(R.id.product_container);
        setUpViewPager();
        viewPager.setAdapter(mViewPageAdapter);
        viewPager.setOffscreenPageLimit(2);

        mTabLayout = findViewById(R.id.product_tabs);
        mTabLayout.setupWithViewPager(viewPager);
        setTabIcons();
        initiateDataFetching();

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.getIcon().setState(new int[] {android.R.attr.state_selected});
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getIcon().setState(new int[] {-android.R.attr.state_selected});
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        checkWishListStatusAndUpdateIcon();
        mCartPlusIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToWishList();
            }
        });

        // 设置从心愿单移除的点击监听器
        mCartRemoveIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFromWishList();
            }
        });
    }

    private void checkWishListStatusAndUpdateIcon() {
        String itemId = Uri.encode(mProductDetailId);
        String url = "https://ebayexpressbackend-233.wl.r.appspot.com/mongodb/searchWishList2/" + itemId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("WishListCheck", "Response received for item ID: " + itemId);
                        // 解析响应
                        try {
                            JSONObject responseObject = new JSONObject(response);
                            boolean isPresent = responseObject.optBoolean("isPresent", false);
                            Log.d("WishListCheck", "Item is present in wish list: " + isPresent + " for item ID: " + itemId);
                            if (isPresent) {
                                mCartPlusIcon.setVisibility(View.INVISIBLE);
                                mCartRemoveIcon.setVisibility(View.VISIBLE);
                            } else {
                                mCartPlusIcon.setVisibility(View.VISIBLE);
                                mCartRemoveIcon.setVisibility(View.INVISIBLE);
                            }
                        } catch (Exception e) {
                            Log.e("WishListCheck", "JSON parsing error: ", e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("WishListCheck", "Network error for item ID: " + itemId, error);
            }
        });

        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(EbayProductSearchApplication.getInstance().getApplicationContext());
        }
        mRequestQueue.add(stringRequest);
        Log.d("WishListCheck", "Request queued for item ID: " + itemId);
    }

    private void addToWishList() {
        JSONObject itemJson = item.toJSONObject();
        String queryParams = "";
        Iterator<String> keys = itemJson.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            String value = itemJson.optString(key, "");
            value = Uri.encode(value);
            queryParams += key + "=" + value;
            if (keys.hasNext()) {
                queryParams += "&";
            }
        }

        String url = "https://ebayexpressbackend-233.wl.r.appspot.com/mongodb/addWishList2?" + queryParams;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mCartPlusIcon.setVisibility(View.INVISIBLE); // 隐藏图标
                        Log.d("WishList", "Added to wish list: " + response);
                        mCartRemoveIcon.setVisibility(View.VISIBLE); // 显示 cart_remove 图标
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("WishList", "Failed to add to wish list: " + error.toString());
            }
        });
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(EbayProductSearchApplication.getInstance().getApplicationContext());
        }
        mRequestQueue.add(stringRequest);
        String toastMessage = getToastMessage(item.getTitle(), true);
        Utils.showToast(toastMessage);
    }

    private void removeFromWishList() {
        String itemId = Uri.encode(item.getProductId());
        String url = "https://ebayexpressbackend-233.wl.r.appspot.com/mongodb/deleteWishList/" + itemId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mCartRemoveIcon.setVisibility(View.INVISIBLE); // 隐藏图标
                        Log.d("WishList", "Removed from wish list: " + response);
                        mCartPlusIcon.setVisibility(View.VISIBLE); // 显示 cart_plus 图标
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("WishList", "Failed to remove from wish list: " + error.toString());
            }
        });

        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(EbayProductSearchApplication.getInstance().getApplicationContext());
        }
        mRequestQueue.add(stringRequest);
        String toastMessage = getToastMessage(item.getTitle(), false);
        Utils.showToast(toastMessage);
    }

    private String getToastMessage(String title, boolean added) {
        String titleSubstring = title.length() > 10 ? title.substring(0, 10) + "..." : title;
        String actionString = added ? "added to" : "removed from";
        return titleSubstring + " " + actionString + " wishlist";
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setUpActionBar() {
        mActionBar.setTitle(item.getTitle());
        mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.product_data_menu, menu);
        MenuItem redirectItem = menu.getItem(0);
        redirectItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_link:
                shareFacebookPost();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void setUpViewPager() {
        mViewPageAdapter = new ViewPageAdapter(this, getSupportFragmentManager());
        mViewPageAdapter.addFragment(new DetailFragment(), R.string.product_tab_title_product);
        mViewPageAdapter.addFragment(new SellerFragment(), R.string.product_tab_title_shipping);
        mViewPageAdapter.addFragment(new PhotoFragment(), R.string.product_tab_title_photos);
        mViewPageAdapter.addFragment(new SimilarProductsFragment(), R.string.product_tab_title_similar);
    }

    private void initiateDataFetching() {
        if (!mProductDataInstance.isProductDetailFetched()) {
            mProductDataInstance.fetchProductDetail(this);
        }
    }

    private void shareFacebookPost() {
        if (!mProductDataInstance.isProductDetailFetched()) {
            Utils.showToast(R.string.error_facebook_share_data_not_fetched);
            return;
        }
        if (mProductDataInstance.getProductDetailError() != null) {
            Utils.showToast(R.string.error_facebook_share_data_not_found);
            return;
        }
        ProductModel product = mProductDataInstance.getProductDetail();
        String productUrl = product.getProductUrl();
        if (productUrl == null) {
            productUrl = "https://www.ebay.com/";
        }

        String encodedUrl = Uri.encode(productUrl);

        String facebookShareUrl = "http://www.facebook.com/sharer.php?u=" + encodedUrl;

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(facebookShareUrl));
        startActivity(browserIntent);
    }

    private void setTabIcons() {
        int[] icons = new int[]{
                R.drawable.information_variant_selector,
                R.drawable.truck_delivery_selector,
                R.drawable.google_photos_selector,
                R.drawable.equal_simillar_selector
        };
        for (int i = 0; i < icons.length; i++) {
            mTabLayout.getTabAt(i).setIcon(icons[i]);
        }
    }
}
