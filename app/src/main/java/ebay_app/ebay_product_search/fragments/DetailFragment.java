package ebay_app.ebay_product_search.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;

import edu.gyaneshm.ebay_product_search.R;
import ebay_app.ebay_product_search.data.ProductData;
import ebay_app.ebay_product_search.listeners.DataFetchingListener;
import ebay_app.ebay_product_search.models.ProductModel;
import ebay_app.ebay_product_search.models.SearchResultModel;
import ebay_app.ebay_product_search.shared.Utils;

public class DetailFragment extends Fragment {
    private LayoutInflater mLayoutInflator;

    private LinearLayout mProgressBarContainer;
    private ScrollView mMainContainer;
    private LinearLayout mGalleryLinearLayout;

    private TextView mProductTileTextView;
    private TextView mProductPriceTextView;
    private TextView mShippingTextView;

    private LinearLayout mHighlightsSectionLinearLayout;
    private LinearLayout mHighLightsSectionPriceLayout;
    private TextView mHighLightsSectionDescriptionTextView;
    private LinearLayout mHighLightsSectionBrandLayout;
    private TextView mHighLightsSectionBrandTextView;

    private LinearLayout mSpecificationsSectionLinearLayout;
    private TextView mSpecificationsSectionSpecificationTextView;

    private TextView mErrorTextView;

    private ProductData mProductDataInstance;
    private ProductModel productDetail;
    private SearchResultModel searchResultItem;

    private boolean isFetched = false;

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
        View view = inflater.inflate(R.layout.detail_fragment_layout, container, false);
        mLayoutInflator = inflater;

        mProgressBarContainer = view.findViewById(R.id.progress_bar_container);
        mMainContainer = view.findViewById(R.id.main_container);
        mGalleryLinearLayout = view.findViewById(R.id.image_gallery);

        mProductTileTextView = view.findViewById(R.id.product_title);
        mProductPriceTextView = view.findViewById(R.id.product_price);
        mShippingTextView = view.findViewById(R.id.shipping);

        mHighlightsSectionLinearLayout = view.findViewById(R.id.highlight_section);
        mHighLightsSectionPriceLayout = view.findViewById(R.id.highlight_section_price_layout);
        mHighLightsSectionDescriptionTextView = view.findViewById(R.id.highlight_section_description_text);
        mHighLightsSectionBrandLayout = view.findViewById(R.id.highlight_section_brand_layout);
        mHighLightsSectionBrandTextView = view.findViewById(R.id.highlight_section_brand_text);

        mSpecificationsSectionLinearLayout = view.findViewById(R.id.specifications_section);
        mSpecificationsSectionSpecificationTextView = view.findViewById(R.id.specifications);

        mErrorTextView = view.findViewById(R.id.error);

        mProductDataInstance = ProductData.getInstance();
        mProductDataInstance.registerCallback(dataFetchingListener);
        initiate();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mProductDataInstance.unregisterCallback(dataFetchingListener);
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

        setUpImageGallery();
        setUpTitleAndPrice();
        setUpHighlightsSection();
        setUpSpecificationsSection();

        mMainContainer.setVisibility(View.VISIBLE);
    }

    private void setUpImageGallery() {
        ArrayList<String> images = productDetail.getProductImages();
        if (images.size() == 0) {
            mGalleryLinearLayout.setVisibility(View.GONE);
            return;
        }

        for (int i = 0; i < images.size(); i++) {
            View view = mLayoutInflator.inflate(R.layout.product_image_gallery_item, mGalleryLinearLayout, false);
            ImageView imageView = view.findViewById(R.id.image);

            // 设置ImageView的宽度和高度
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
    }

    private void setUpTitleAndPrice() {
        mProductTileTextView.setText(productDetail.getTitle());

        Double price = productDetail.getPrice();
        if (price != null) {
            mProductPriceTextView.setText(getString(R.string.price, Utils.formatPriceToString(price)));
        } else {
            mProductPriceTextView.setVisibility(View.GONE);
        }

        Double shipping = Double.parseDouble(searchResultItem.getShippingInfo());
        if (shipping == 0) {
            mShippingTextView.setText("with Free Shipping");
        } else {
            mShippingTextView.setText("with $"+ shipping.toString()+ " Shipping");
        }
    }

    private void setUpHighlightsSection() {
        boolean sectionValid = false;
        String description = productDetail.getPrice().toString();
        mHighLightsSectionPriceLayout.setVisibility(View.VISIBLE);
        mHighLightsSectionDescriptionTextView.setText("$"+description);

        HashMap<String, String> itemSpecifics = productDetail.getItemSpecifics();
        if (itemSpecifics.containsKey("Brand")) {
            mHighLightsSectionBrandTextView.setText(itemSpecifics.get("Brand"));
            mHighLightsSectionBrandLayout.setVisibility(View.VISIBLE);
            sectionValid = true;
        }

        if(sectionValid) {
            mHighlightsSectionLinearLayout.setVisibility(View.VISIBLE);
        }
    }

    private void setUpSpecificationsSection() {
        boolean sectionValid = false;

        StringBuilder str = new StringBuilder();
        HashMap<String, String> itemSpecifics = productDetail.getItemSpecifics();

        int count = itemSpecifics.size();
        for(HashMap.Entry<String, String> entry : itemSpecifics.entrySet()) {
            if(count==0) break;
            str.append("&#8226; ");
            str.append(Utils.capitalizeFirstCharacter(entry.getValue()));
            str.append("<br>");
            sectionValid = true;
        }

        if(sectionValid) {
            String finalStr = str.toString().trim();
            mSpecificationsSectionSpecificationTextView.setText(Html.fromHtml(finalStr, Html.FROM_HTML_MODE_LEGACY));
            mSpecificationsSectionLinearLayout.setVisibility(View.VISIBLE);
        }
    }
}
