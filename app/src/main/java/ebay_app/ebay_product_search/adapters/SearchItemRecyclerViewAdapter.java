package ebay_app.ebay_product_search.adapters;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Iterator;

import ebay_app.ebay_product_search.EbayProductSearchApplication;
import edu.gyaneshm.ebay_product_search.R;
import ebay_app.ebay_product_search.listeners.ItemClickListener;
import ebay_app.ebay_product_search.models.SearchResultModel;
import ebay_app.ebay_product_search.shared.Utils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class SearchItemRecyclerViewAdapter extends RecyclerView.Adapter<SearchItemRecyclerViewAdapter.ItemViewHolder> {

    private Context mContext;
    private ArrayList<SearchResultModel> mSearchResults;
    private ItemClickListener mItemClickListener;
    private RequestQueue mRequestQueue = null;

    private WishlistUpdateListener wishlistUpdateListener;
    private boolean showToastOnWishlistUpdate = true;

    public void setWishlistUpdateListener(WishlistUpdateListener listener) {
        this.wishlistUpdateListener = listener;
    }

    public SearchItemRecyclerViewAdapter(Context context, ArrayList<SearchResultModel> searchResults, ItemClickListener itemClickListener) {
        mContext = context;
        mSearchResults = searchResults;
        mItemClickListener = itemClickListener;
    }


    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.search_item_card_view, null);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, final int position) {
        SearchResultModel searchResult = mSearchResults.get(position);
        updateWishListIcon(holder, searchResult);
        setTextViewContent(holder.mTitleTextView, searchResult.getTitle(), R.string.n_a);
        setTextViewContent(holder.mPriceTextView, formatPrice(searchResult.getPrice()), R.string.n_a);
        setTextViewContent(holder.mConditionTextView, searchResult.getCondition(), R.string.n_a);
        setShippingText(holder.mShippingTextView, searchResult.getShippingInfo());
        setImage(holder.mItemImageView, searchResult.getImage());
        holder.mZipCodeTextView.setText(formatZipCode(searchResult.getZipCode()));
    }

    private void updateWishListIcon(ItemViewHolder holder, SearchResultModel searchResult) {
        checkWishListStatus(searchResult, holder.mCardItemProductCartIcon, holder.mCardItemProductCartIconRemove);
    }

    private void setTextViewContent(TextView textView, String content, int defaultStringId) {
        textView.setText(content != null ? content : mContext.getString(defaultStringId));
    }

    private String formatPrice(Double price) {
        return price != null ? mContext.getString(R.string.price, Utils.formatPriceToString(price)) : null;
    }

    private void setShippingText(TextView textView, String shippingInfo) {
        double shippingCost = Double.parseDouble(shippingInfo);
        String text = shippingCost == 0 ? "FREE" : "Ships for $" + shippingCost;
        textView.setText(Html.fromHtml(text));
    }

    private void setImage(ImageView imageView, String imageUrl) {
        if (imageUrl == null || imageUrl.equals("") || imageUrl.equalsIgnoreCase("https://thumbs1.ebaystatic.com/pict/04040_0.jpg")) {
            Glide.with(mContext).clear(imageView);
            imageView.setImageDrawable(mContext.getDrawable(R.drawable.ferry));
        } else {
            Glide.with(mContext).load(imageUrl).into(imageView);
        }
    }

    private String formatZipCode(String zipCode) {
        return "Zip: " + (zipCode != null ? zipCode : "N/A");
    }


    public void checkWishListStatus(final SearchResultModel itemData, final ImageView cartIcon, final ImageView cartIconRemove) {
        String itemId = Uri.encode(itemData.getProductId());
        String url = "https://ebayexpressbackend-233.wl.r.appspot.com/mongodb/searchWishList2/" + itemId;
        Log.d("WishListCheck", "url: " + url);

        Log.d("WishListCheck", "Checking wish list status for item ID: " + itemId);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("WishListCheck", "Response received for item ID: " + itemId);
                        // 解析响应
                        try {
                            JSONObject responseObject = new JSONObject(response);
                            Log.d("WishListCheck", "Parsed JSON: " + responseObject.toString()+" for item ID: " + itemId);
                            boolean isPresent = responseObject.optBoolean("isPresent", false);
                            Log.d("WishListCheck", "Item is present in wish list: " + isPresent + " for item ID: " + itemId);
                            if (isPresent) {
                                cartIcon.setVisibility(View.INVISIBLE); // 隐藏 cart_plus
                                Log.d("WishListCheck", cartIcon.toString() + " is set to invisible for item ID: " + itemId);
                                cartIconRemove.setVisibility(View.VISIBLE); // 显示 cart_remove
                            } else {
                                cartIcon.setVisibility(View.VISIBLE); // 显示 cart_plus
                                Log.d("WishListCheck", cartIcon.toString() + " is set to visible for item ID: " + itemId);
                                cartIconRemove.setVisibility(View.INVISIBLE); // 隐藏 cart_remove
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

        // Disable caching for this request
        stringRequest.setShouldCache(false);

        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(EbayProductSearchApplication.getInstance().getApplicationContext());
        }
        mRequestQueue.add(stringRequest);
        Log.d("WishListCheck", "Request queued for item ID: " + itemId);
    }

    @Override
    public int getItemCount() {
        return mSearchResults.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView mTitleTextView;
        TextView mPriceTextView;
        TextView mZipCodeTextView;
        TextView mShippingTextView;
        TextView mConditionTextView;
        ImageView mItemImageView;
        ImageView mCardItemProductCartIcon;

        ImageView mCardItemProductCartIconRemove;


        ItemViewHolder(View itemView) {
            super(itemView);

            mTitleTextView = itemView.findViewById(R.id.card_item_product_title);
            mPriceTextView = itemView.findViewById(R.id.card_item_product_price);
            mZipCodeTextView = itemView.findViewById(R.id.card_item_product_zipcode);
            mShippingTextView = itemView.findViewById(R.id.card_item_product_shipping);
            mConditionTextView = itemView.findViewById(R.id.card_item_product_condition);
            mItemImageView = itemView.findViewById(R.id.card_item_product_image);
            mConditionTextView = itemView.findViewById(R.id.card_item_product_condition);
            mCardItemProductCartIcon = itemView.findViewById(R.id.card_item_product_cart_icon);
            mCardItemProductCartIconRemove = itemView.findViewById(R.id.card_item_product_cart_icon_remove);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mItemClickListener.onItemClicked(getAdapterPosition());
                }
            });

            mCardItemProductCartIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION) {
                        SearchResultModel item = mSearchResults.get(position);
                        addToWishList(item, mCardItemProductCartIcon, mCardItemProductCartIconRemove);
                    }
                }
            });

            mCardItemProductCartIconRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION) {
                        SearchResultModel item = mSearchResults.get(position);
                        removeFromWishList(item, mCardItemProductCartIconRemove, mCardItemProductCartIcon);
//                        showToastOnWishlistUpdate = false;
                    }
                    if (wishlistUpdateListener != null) {
                        wishlistUpdateListener.onWishlistUpdate();
                    }
                }
            });

        }
    }

    public void addToWishList(SearchResultModel itemData, ImageView cartIcon, ImageView cartIconRemove){
        JSONObject itemJson = itemData.toJSONObject();
        String queryParams = "";
        Iterator<String> keys = itemJson.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            String value = itemJson.optString(key, "");
            // 对参数进行编码以确保它们在URL中传输时不会有问题
            value = Uri.encode(value);
            queryParams += key + "=" + value;
            if (keys.hasNext()) {
                queryParams += "&"; // 添加参数分隔符
            }
        }

        RequestQueue queue = Volley.newRequestQueue(mContext);
        String url = "https://ebayexpressbackend-233.wl.r.appspot.com/mongodb/addWishList2?" + queryParams;
        Log.d("WishList", "url: " + url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        cartIcon.setVisibility(View.INVISIBLE); // 隐藏图标
                        Log.d("WishList", "Added to wish list: " + response);
                        cartIconRemove.setVisibility(View.VISIBLE); // 显示 cart_remove 图标
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
        String toastMessage = getToastMessage(itemData.getTitle(), true);
        Utils.showToast(toastMessage);
    }

    public void removeFromWishList(SearchResultModel itemData, ImageView cartIconRemove, ImageView cartIcon) {
        String itemId = Uri.encode(itemData.getProductId());
        String url = "https://ebayexpressbackend-233.wl.r.appspot.com/mongodb/deleteWishList/" + itemId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        cartIconRemove.setVisibility(View.INVISIBLE); // 隐藏图标
                        Log.d("WishList", "Removed from wish list: " + response);
                        cartIcon.setVisibility(View.VISIBLE); // 显示 cart_plus 图标
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
        String toastMessage = getToastMessage(itemData.getTitle(), false);
        Utils.showToast(toastMessage);
    }


    private String getToastMessage(String title, boolean added) {
        String titleSubstring = title.length() > 10 ? title.substring(0, 10) + "..." : title;
        String actionString = added ? "added to" : "removed from";
        return titleSubstring + " " + actionString + " wishlist";
    }

    private void showCustomToast(Context context, String title, boolean added) {
        Toast toast = new Toast(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View layout = inflater.inflate(R.layout.custom_toast_layout,
                (ViewGroup) ((Activity)context).findViewById(R.id.custom_toast_container));

        ImageView imageView = layout.findViewById(R.id.custom_toast_icon);
        imageView.setImageResource(R.drawable.shopping_icon_launcher); // 替换为你的应用图标资源ID

        String titleSubstring = title.length() > 10 ? title.substring(0, 10) + "..." : title;

        String actionString = added ? "added to" : "removed from";
        TextView text = layout.findViewById(R.id.custom_toast_message);
        text.setText(titleSubstring + " " + actionString + " wishlist");

        toast.setView(layout);

        toast.setDuration(Toast.LENGTH_SHORT);

        toast.show();
    }

    public interface WishlistUpdateListener {
        void onWishlistUpdate();
    }



}
