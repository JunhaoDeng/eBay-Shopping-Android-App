package ebay_app.ebay_product_search.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

import ebay_app.ebay_product_search.shared.Utils;

public class SearchResultModel {
    private String topRated;
    private String image;
    private String title;
    private String zipCode;
    private String productId;
    private String condition;
    private Double price;
    private String shippingInfo;
    private HashMap<String, String> shipping = new HashMap<>();
    private String storeName;
    private String storeUrl;
    private String feedbackScore;
    private String positiveFeedbackPercent;
    private String feedbackRatingStar;
    private String globalShipping;
    private String handlingTime;

    public HashMap<String, String> getShipping() {
        return shipping;
    }

    public SearchResultModel(JSONObject data) {
        try {
            this.image = data.has("image") ? data.optString("image") : data.optString("galleryURL");
            this.title = data.optString("title");
            this.productId = data.has("productId") ? data.optString("productId") : data.optString("itemId");
            this.condition = data.optString("condition");
            this.price = data.has("price") ? data.optDouble("price") : data.optDouble("currentPrice");
            this.topRated = data.has("topRated") ? data.optString("topRated") : data.optString("topRatedListing");
            // 解析 shippingInfo
            this.shippingInfo = extractShippingInfo(data);

            // 处理 zipCode
            this.zipCode = data.optString("postalCode");

            JSONObject sellerInfo = data.optJSONObject("sellerInfo");
            JSONObject storeInfo = data.optJSONObject("storeInfo");
            JSONObject shippingInfo = data.optJSONObject("shippingInfo");

            this.storeName = extractFirstStringFromArray(storeInfo, "storeName");
            this.storeUrl = extractFirstStringFromArray(storeInfo, "storeURL");
            this.feedbackScore = extractFirstStringFromArray(sellerInfo, "feedbackScore");
            this.positiveFeedbackPercent = extractFirstStringFromArray(sellerInfo, "positiveFeedbackPercent");
            this.feedbackRatingStar = extractFirstStringFromArray(sellerInfo, "feedbackRatingStar");
            this.globalShipping = "No"; // Default value
            this.handlingTime = extractFirstStringFromArray(shippingInfo, "handlingTime");

            Log.d("SearchResultModel", "Store Name: " + storeName);
            Log.d("SearchResultModel", "Store URL: " + storeUrl);
            Log.d("SearchResultModel", "Feedback Score: " + feedbackScore);
            Log.d("SearchResultModel", "Positive Feedback Percent: " + positiveFeedbackPercent);
            Log.d("SearchResultModel", "Feedback Rating Star: " + feedbackRatingStar);
            Log.d("SearchResultModel", "Global Shipping: " + globalShipping);
            Log.d("SearchResultModel", "Handling Time: " + handlingTime);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private String extractFirstStringFromArray(JSONObject jsonObject, String key) {
        if (jsonObject != null && jsonObject.has(key)) {
            JSONArray jsonArray = jsonObject.optJSONArray(key);
            if (jsonArray != null && jsonArray.length() > 0) {
                return jsonArray.optString(0, null);
            }
        }
        return null;
    }

    private String extractShippingInfo(JSONObject data) {
        if (data.has("shippingInfo")) {
            Object shippingInfoObj = data.opt("shippingInfo");
            if (shippingInfoObj instanceof JSONObject) {
                JSONObject shippingInfoJSON = (JSONObject) shippingInfoObj;
                JSONArray shippingServiceCost = shippingInfoJSON.optJSONArray("shippingServiceCost");
                if (shippingServiceCost != null && shippingServiceCost.length() > 0) {
                    JSONObject cost = shippingServiceCost.optJSONObject(0);
                    return cost.optString("__value__", "0.0");
                }
            } else if (shippingInfoObj instanceof String) {
                return (String) shippingInfoObj;
            }
        }
        return "0.0";
    }

    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("topRated", topRated);
            obj.put("image", image);
            obj.put("title", title);
            obj.put("productId", productId);
            obj.put("condition", condition);
            obj.put("price", price);
            obj.put("shippingInfo", shippingInfo);
            obj.put("postalCode", zipCode);
        } catch (Exception ex) {
        }
        return obj;
    }

    public String getTopRated() {return topRated; }

    public String getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

    public String getProductId() {
        return productId;
    }

    public String getCondition() {
        return condition;
    }

    public Double getPrice() {
        return price;
    }

    public String getShippingInfo() { return shippingInfo; }

    public String getZipCode() {
        return zipCode;
    }

    // 新添加的 getter 方法
    public String getStoreName() { return storeName; }
    public String getStoreUrl() { return storeUrl; }
    public String getFeedbackScore() { return feedbackScore; }
    public String getPositiveFeedbackPercent() { return positiveFeedbackPercent; }
    public String getFeedbackRatingStar() { return feedbackRatingStar; }
    public String getGlobalShipping() { return globalShipping; }
    public String getHandlingTime() { return handlingTime; }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("shippingInfo: ");
        str.append(shippingInfo);
        str.append(", ");
        str.append("image: ");
        str.append(image);
        str.append(", ");
        str.append("title: ");
        str.append(title);
        str.append(", ");
        str.append("productId: ");
        str.append(productId);
        str.append(", ");
        str.append("price: ");
        str.append(price);
        str.append(", ");
        str.append("topRated: ");
        str.append(topRated);
        str.append(", ");
        str.append("postalCode: ");
        str.append(zipCode);
        return str.toString();
    }
}
