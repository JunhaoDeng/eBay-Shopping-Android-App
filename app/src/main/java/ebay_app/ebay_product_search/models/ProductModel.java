package ebay_app.ebay_product_search.models;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import ebay_app.ebay_product_search.shared.Utils;

public class ProductModel {
    private String title;
    private String productId;
    private String productUrl;
    private ArrayList<String> productImages = new ArrayList<>();
    private Double price;
    private String location;
    private String seller;
    private String returnPolicy;
    private HashMap<String, String> itemSpecifics = new HashMap<>();

    private String refund;
    private String returnsWithin;
    private String returnsAccepted;
    private String shippingCostPaidBy;

    public ProductModel(JSONObject data) {
        try {
            title = data.optString("Title");
            productUrl = data.optString("eBay Link");
            productId = Uri.parse(productUrl).getLastPathSegment();

            JSONArray photos = data.optJSONArray("Photo");
            if (photos != null) {
                for (int i = 0; i < photos.length(); i++) {
                    productImages.add(photos.optString(i));
                }
            }

            price = Utils.parsePrice(data.optString("Price"));
            location = data.optString("Location");
            seller = data.optString("Seller");
            returnPolicy = data.optString("Return Policy(US)");

            JSONObject specifics = data.optJSONObject("ItemSpecifics");
            if (specifics != null && specifics.has("NameValueList")) {
                JSONArray nameValueList = specifics.optJSONArray("NameValueList");
                for (int i = 0; i < nameValueList.length(); i++) {
                    JSONObject nameValue = nameValueList.optJSONObject(i);
                    if (nameValue != null) {
                        String name = nameValue.optString("Name");
                        JSONArray values = nameValue.optJSONArray("Value");
                        if (values != null && values.length() > 0) {
                            String firstValue = values.optString(0);
                            itemSpecifics.put(name, firstValue);
                        }
                    }
                }
            }

            JSONObject returnPolicyObj = data.optJSONObject("Return Policy");
            if (returnPolicyObj != null) {
                refund = returnPolicyObj.optString("Refund", "null");
                returnsWithin = returnPolicyObj.optString("ReturnsWithin", "null");
                returnsAccepted = returnPolicyObj.optString("ReturnsAccepted", "null");
                shippingCostPaidBy = returnPolicyObj.optString("ShippingCostPaidBy", "null");

                // Log 输出
                Log.d("ProductModel", "Refund: " + refund);
                Log.d("ProductModel", "ReturnsWithin: " + returnsWithin);
                Log.d("ProductModel", "ReturnsAccepted: " + returnsAccepted);
                Log.d("ProductModel", "ShippingCostPaidBy: " + shippingCostPaidBy);
            }
        } catch (Exception ex) {
        }
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public ArrayList<String> getProductImages() {
        return productImages;
    }

    public Double getPrice() {
        return price;
    }

    public String getLocation() {
        return location;
    }

    public String getSeller() {
        return seller;
    }

    public String getReturnPolicy() {
        return returnPolicy;
    }

    public HashMap<String, String> getItemSpecifics() {
        return itemSpecifics;
    }

    // 新增的 Getters
    public String getRefund() {
        return refund;
    }

    public String getReturnsWithin() {
        return returnsWithin;
    }

    public String getReturnsAccepted() {
        return returnsAccepted;
    }

    public String getShippingCostPaidBy() {
        return shippingCostPaidBy;
    }

    @Override
    public String toString() {
        return "ProductModel{" +
                "title='" + title + '\'' +
                ", productId='" + productId + '\'' +
                ", productUrl='" + productUrl + '\'' +
                ", productImages=" + productImages +
                ", price=" + price +
                ", location='" + location + '\'' +
                ", seller='" + seller + '\'' +
                ", returnPolicy='" + returnPolicy + '\'' +
                ", itemSpecifics=" + itemSpecifics +
                '}';
    }
}

