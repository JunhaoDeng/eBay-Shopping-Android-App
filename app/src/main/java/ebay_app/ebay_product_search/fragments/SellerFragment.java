package ebay_app.ebay_product_search.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.text.method.LinkMovementMethod;

import org.w3c.dom.Text;

import java.util.HashMap;

import ebay_app.ebay_product_search.data.ProductData;
import ebay_app.ebay_product_search.listeners.DataFetchingListener;
import ebay_app.ebay_product_search.models.ProductModel;
import ebay_app.ebay_product_search.models.SearchResultModel;
import ebay_app.ebay_product_search.shared.Utils;
import edu.gyaneshm.ebay_product_search.R;

public class SellerFragment extends Fragment {
    private LinearLayout mProgressBarContainer;
    private ProductData mProductDataInstance;
    private ProductModel productDetail;
    private SearchResultModel searchResultItem;
    private ScrollView mMainContainer;
    private TextView sellerView;
    private TextView returnView;

    // Added member variables
    private LinearLayout soldBySectionStoreNameLayout;
    private TextView soldBySectionStoreNameText;
    private LinearLayout soldBySectionFeedbackScore;
    private TextView soldBySectionFeedbackScoreText;
    private LinearLayout soldBySectionPopularityLayout;
    private com.wssholmes.stark.circular_score.CircularScoreView soldBySectionPopularityCircle;
    private LinearLayout soldBySectionFeedbackStarLayout;
    private TextView soldBySectionFeedbackStarCircleText;
    private TextView soldBySectionFeedbackStarCircleOutlineText;

    private LinearLayout shippingInfoSectionShippingCost;
    private TextView shippingInfoSectionShippingCostText;
    private LinearLayout shippingInfoSectionGlobalShipping;
    private TextView shippingInfoSectionGlobalShippingText;
    private LinearLayout shippingInfoSectionHandlingTime;
    private TextView shippingInfoSectionHandlingTimeText;

    private LinearLayout returnPolicySectionPolicyLayout;
    private TextView returnPolicySectionPolicyText;
    private LinearLayout returnPolicySectionReturnWithinLayout;
    private TextView returnPolicySectionReturnWithinText;
    private LinearLayout returnPolicySectionRefundModeLayout;
    private TextView returnPolicySectionRefundModeText;
    private LinearLayout returnPolicySectionShippedByLayout;
    private TextView returnPolicySectionShippedByText;

    private TextView mErrorTextView;
    private boolean isFetched = false;

    private DataFetchingListener dataFetchingListener = new DataFetchingListener() {
        @Override
        public void dataSuccessFullyFetched() {
            if (!isFetched) {
                initiate();
            }
        }
    };

    public SellerFragment() {
    }

    public static SellerFragment newInstance(String param1, String param2) {
        SellerFragment fragment = new SellerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.seller_fragment_layout, container, false);
        mMainContainer = view.findViewById(R.id.main_container);
        mProgressBarContainer = view.findViewById(R.id.progress_bar_container);
        mErrorTextView = view.findViewById(R.id.error);
        soldBySectionPopularityLayout = view.findViewById(R.id.sold_by_section_popularity);
        soldBySectionPopularityCircle = view.findViewById(R.id.sold_by_section_popularity_circle);
        soldBySectionFeedbackStarLayout = view.findViewById(R.id.sold_by_section_feedback_star);
        soldBySectionFeedbackStarCircleText = view.findViewById(R.id.sold_by_section_feedback_star_circle_text);
        soldBySectionFeedbackStarCircleOutlineText = view.findViewById(R.id.sold_by_section_feedback_star_circle_outline_text);
        soldBySectionStoreNameLayout = view.findViewById(R.id.sold_by_section_store_name);
        soldBySectionStoreNameText = view.findViewById(R.id.sold_by_section_store_name_text);
        soldBySectionFeedbackScore = view.findViewById(R.id.sold_by_section_feedback_score);
        soldBySectionFeedbackScoreText = view.findViewById(R.id.sold_by_section_feedback_score_text);

        soldBySectionStoreNameText.setSingleLine();
        soldBySectionStoreNameText.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        soldBySectionStoreNameText.setMarqueeRepeatLimit(-1);

        soldBySectionStoreNameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.requestFocus();
                soldBySectionStoreNameText.setSelected(true);
            }
        });

        shippingInfoSectionShippingCost = view.findViewById(R.id.shipping_info_section_shipping_cost);
        shippingInfoSectionShippingCostText = view.findViewById(R.id.shipping_info_section_shipping_cost_text);
        shippingInfoSectionGlobalShipping = view.findViewById(R.id.shipping_info_section_global_shipping);
        shippingInfoSectionGlobalShippingText = view.findViewById(R.id.shipping_info_section_global_shipping_text);
        shippingInfoSectionHandlingTime = view.findViewById(R.id.shipping_info_section_Handling_time);
        shippingInfoSectionHandlingTimeText = view.findViewById(R.id.shipping_info_section_handling_time_text);

        initializeReturnPolicyViews(view);

        mProductDataInstance = ProductData.getInstance();
        mProductDataInstance.registerCallback(dataFetchingListener);
        initiate();
        return view;
    }

    private void initializeReturnPolicyViews(View view) {
        returnPolicySectionPolicyLayout = view.findViewById(R.id.return_policy_section_policy);
        returnPolicySectionPolicyText = view.findViewById(R.id.return_policy_section_policy_text);
        returnPolicySectionReturnWithinLayout = view.findViewById(R.id.return_policy_section_return_within);
        returnPolicySectionReturnWithinText = view.findViewById(R.id.return_policy_section_return_within_text);
        returnPolicySectionRefundModeLayout = view.findViewById(R.id.return_policy_section_refund_mode);
        returnPolicySectionRefundModeText = view.findViewById(R.id.return_policy_section_refund_mode_text);
        returnPolicySectionShippedByLayout = view.findViewById(R.id.return_policy_section_shipped_by);
        returnPolicySectionShippedByText = view.findViewById(R.id.return_policy_section_shipped_by_text);
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

        setUpSoldBySection();
        setUpShippingInfoSection();
        setUpReturnPolicySection();
        mMainContainer.setVisibility(View.VISIBLE);
    }

    private void setUpSoldBySection() {
        String storeName = searchResultItem.getStoreName();
        String storeUrl = searchResultItem.getStoreUrl();
        Log.d("storeName", storeName);
        Log.d("storeUrl", storeUrl);
        Double popularity = Double.parseDouble(searchResultItem.getPositiveFeedbackPercent());

        if (popularity != null) {
            soldBySectionPopularityCircle.setScore(popularity.intValue());
            soldBySectionPopularityLayout.setVisibility(View.VISIBLE);
        } else {
            soldBySectionPopularityLayout.setVisibility(View.GONE);
        }

        if (storeName != null && storeUrl != null) {
            String linkedText = String.format("<a href='%s'>%s</a>", storeUrl, storeName);
            soldBySectionStoreNameText.setText(Html.fromHtml(linkedText, Html.FROM_HTML_MODE_COMPACT));
            soldBySectionStoreNameText.setMovementMethod(LinkMovementMethod.getInstance());
            soldBySectionStoreNameLayout.setVisibility(View.VISIBLE);
        } else {
            soldBySectionStoreNameLayout.setVisibility(View.GONE);
        }

        if (searchResultItem.getFeedbackScore() != null) {
            soldBySectionFeedbackScore.setVisibility(View.VISIBLE);
            soldBySectionFeedbackScoreText.setText(searchResultItem.getFeedbackScore());
        } else {
            soldBySectionFeedbackScore.setVisibility(View.GONE);
        }

        if (searchResultItem.getFeedbackScore() != null) {
            int feedbackScore = Integer.parseInt(searchResultItem.getFeedbackScore());
            Drawable starDrawable;
            int starColor;
            boolean isStarFilled = feedbackScore >= 10000;
            starDrawable = ContextCompat.getDrawable(getContext(),
                    isStarFilled ? R.drawable.star_circle : R.drawable.star_circle_outline);

            if (feedbackScore >= 500000) {
                starColor = ContextCompat.getColor(getContext(), R.color.silver);
            } else if (feedbackScore >= 100000) {
                starColor = ContextCompat.getColor(getContext(), R.color.red);
            } else if (feedbackScore >= 50000) {
                starColor = ContextCompat.getColor(getContext(), R.color.purple);
            } else if (feedbackScore >= 25000) {
                starColor = ContextCompat.getColor(getContext(), R.color.turquoise);
            } else if (feedbackScore >= 10000) {
                starColor = ContextCompat.getColor(getContext(), R.color.yellow);
            } else if (feedbackScore >= 5000) {
                starColor = ContextCompat.getColor(getContext(), R.color.green);
            } else if (feedbackScore >= 1000) {
                starColor = ContextCompat.getColor(getContext(), R.color.red);
            } else if (feedbackScore >= 500) {
                starColor = ContextCompat.getColor(getContext(), R.color.purple);
            } else if (feedbackScore >= 100) {
                starColor = ContextCompat.getColor(getContext(), R.color.turquoise);
            } else if (feedbackScore >= 50) {
                starColor = ContextCompat.getColor(getContext(), R.color.blue);
            } else {
                starColor = ContextCompat.getColor(getContext(), R.color.yellow);
            }

            starDrawable.mutate();
            DrawableCompat.setTint(starDrawable, starColor);
            soldBySectionFeedbackStarCircleText.setCompoundDrawablesWithIntrinsicBounds(starDrawable, null, null, null);

            soldBySectionFeedbackStarCircleText.setVisibility(View.VISIBLE);

            soldBySectionFeedbackStarLayout.setVisibility(View.VISIBLE);
        } else {
            soldBySectionFeedbackStarLayout.setVisibility(View.GONE);
        }
    }

    private void setUpShippingInfoSection() {
        // 获取运输信息
        Double shipping = Double.parseDouble(searchResultItem.getShippingInfo());
        String shippingCost = "";
        if (shipping == 0) {
            shippingCost = "Free";
        } else {
            shippingCost = "Ships for $"+ shipping.toString()+"";
        }
        String globalShipping = searchResultItem.getGlobalShipping();
        String handlingTime = searchResultItem.getHandlingTime();

        if (shippingCost != null) {
            shippingInfoSectionShippingCostText.setText(shippingCost);
            shippingInfoSectionShippingCost.setVisibility(View.VISIBLE);
        } else {
            shippingInfoSectionShippingCost.setVisibility(View.GONE);
        }

        if (globalShipping != null) {
            shippingInfoSectionGlobalShippingText.setText(globalShipping);
            shippingInfoSectionGlobalShipping.setVisibility(View.VISIBLE);
        } else {
            shippingInfoSectionGlobalShipping.setVisibility(View.GONE);
        }

        if (handlingTime != null) {
            shippingInfoSectionHandlingTimeText.setText(handlingTime);
            shippingInfoSectionHandlingTime.setVisibility(View.VISIBLE);
        } else {
            shippingInfoSectionHandlingTime.setVisibility(View.GONE);
        }
    }

    private void setUpReturnPolicySection() {
        productDetail = mProductDataInstance.getProductDetail();

        String policy = productDetail.getReturnsAccepted();
        String returnWithin = productDetail.getReturnsWithin();
        String refundMode = productDetail.getRefund();
        String shippedBy = productDetail.getShippingCostPaidBy();

        setTextAndVisibility(returnPolicySectionPolicyText, returnPolicySectionPolicyLayout, policy);
        setTextAndVisibility(returnPolicySectionReturnWithinText, returnPolicySectionReturnWithinLayout, returnWithin);
        setTextAndVisibility(returnPolicySectionRefundModeText, returnPolicySectionRefundModeLayout, refundMode);
        setTextAndVisibility(returnPolicySectionShippedByText, returnPolicySectionShippedByLayout, shippedBy);
    }

    private void setTextAndVisibility(TextView textView, LinearLayout layout, String text) {
        if (text != null && !text.equals("null")) {
            textView.setText(text);
            layout.setVisibility(View.VISIBLE);
        } else {
            layout.setVisibility(View.GONE);
        }
    }



    private void setUpReturnSection() {
        boolean sectionValid = false;

        StringBuilder str = new StringBuilder();
        HashMap<String, String> itemSpecifics = productDetail.getItemSpecifics();

        int count = 5;
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
            returnView.setText(Html.fromHtml(finalStr, Html.FROM_HTML_MODE_LEGACY));
        }
    }

    private void setUpSellerSection() {
        boolean sectionValid = false;

        StringBuilder str = new StringBuilder();
        HashMap<String, String> itemSpecifics = productDetail.getItemSpecifics();


        int count = 5;
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
            sellerView.setText(Html.fromHtml(finalStr, Html.FROM_HTML_MODE_LEGACY));
        }
    }
}
