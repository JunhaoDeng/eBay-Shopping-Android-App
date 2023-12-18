package ebay_app.ebay_product_search.models;

import android.util.Log;

import org.json.JSONObject;

public class SimilarProductModel {
    private String imageUrl;
    private String title;
    private String shippingCost;
    private String timeLeft;
    private String price;

    public SimilarProductModel(JSONObject jsonObject) {
        try {
            this.imageUrl = jsonObject.optString("imageURL");
            Log.d("SimilarProductModel", "Image URL: " + imageUrl);

            this.title = jsonObject.optString("title");
            Log.d("SimilarProductModel", "Title: " + title);

            JSONObject shippingCostObj = jsonObject.optJSONObject("shippingCost");

            this.shippingCost = shippingCostObj.optString("__value__");
            Log.d("SimilarProductModel", "Shipping Cost: " + shippingCost);

            String time = jsonObject.optString("timeLeft");
            if (time.contains("P") && time.contains("D")) {
                this.timeLeft = time.substring(time.indexOf("P") + 1, time.indexOf("D")) + " Days Left";
            } else {
                this.timeLeft = time; // 或者处理为默认值
            }
            Log.d("SimilarProductModel", "Time Left: " + timeLeft);

            JSONObject priceObj = jsonObject.optJSONObject("buyItNowPrice");
            if (priceObj != null) {
                this.price = priceObj.optString("__value__");
            } else {
                this.price = "N/A";
            }
            Log.d("SimilarProductModel", "Price: " + price);

        } catch (Exception ex) {
            Log.e("SimilarProductModel", "Error parsing JSON object", ex);
        }
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getShippingCost() {
        return shippingCost;
    }

    public String getTimeLeft() {
        return timeLeft;
    }

    public String getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "SimilarProductModel{" +
                "imageUrl='" + imageUrl + '\'' +
                ", title='" + title + '\'' +
                ", shippingCost='" + shippingCost + '\'' +
                ", timeLeft='" + timeLeft + '\'' +
                ", price='" + price + '\'' +
                '}';
    }
}
