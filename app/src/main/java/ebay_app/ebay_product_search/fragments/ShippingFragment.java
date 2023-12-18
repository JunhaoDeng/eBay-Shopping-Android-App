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

import com.wssholmes.stark.circular_score.CircularScoreView;

import java.util.HashMap;

import edu.gyaneshm.ebay_product_search.R;
import ebay_app.ebay_product_search.data.ProductData;
import ebay_app.ebay_product_search.listeners.DataFetchingListener;
import ebay_app.ebay_product_search.models.ProductModel;
import ebay_app.ebay_product_search.models.SearchResultModel;
import ebay_app.ebay_product_search.shared.Utils;

public class ShippingFragment extends Fragment {
    private LinearLayout mProgressBarContainer;
    private ScrollView mMainContainer;
    private TextView mErrorTextView;
    private TextView mShippingView;

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
        View view = inflater.inflate(R.layout.shipping_fragment_layout, container, false);

        mProgressBarContainer = view.findViewById(R.id.progress_bar_container);
        mMainContainer = view.findViewById(R.id.main_container);
        mErrorTextView = view.findViewById(R.id.error);
        mShippingView = view.findViewById(R.id.shipping_info);

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

        setUpShippingSection();
        mMainContainer.setVisibility(View.VISIBLE);
    }

    private void setUpShippingSection() {
        boolean sectionValid = false;

        StringBuilder str = new StringBuilder();
        HashMap<String, String> itemSpecifics = searchResultItem.getShipping();

        for(HashMap.Entry<String, String> entry : itemSpecifics.entrySet()) {
            String key = entry.getKey();
            String[] words = key.split("(?=\\p{Upper})");
            String line = String.join(" ", words);
            str.append("&#8226; ");
            str.append("<b>"+Utils.capitalizeFirstCharacter(line)+"</b> : ");
            String value = entry.getValue().equalsIgnoreCase("true")?"Yes":entry.getValue();
            value = value.equalsIgnoreCase("false")?"No":value;

            str.append(Utils.capitalizeFirstCharacter(value));
            str.append("<br><br>");
            sectionValid = true;
        }

        if(sectionValid) {
            String finalStr = str.toString().trim();
            mShippingView.setText(Html.fromHtml(finalStr, Html.FROM_HTML_MODE_LEGACY));
        }
    }

}