package ebay_app.ebay_product_search.models;

import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class SearchFormModel {
    private String keyword;
    private String category;
    private HashSet<String> condition;
    private HashMap<String, String> priceRange;
    private HashMap<String, Boolean> shippingOption;

    private String distance = "-1";

    private String postalCode = "-1";

    public String toQueryParams() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("keyword="+keyword);
        stringBuilder.append("&category="+category);
        if(!shippingOption.isEmpty()){
            for (Map.Entry<String,Boolean> rule : shippingOption.entrySet()) {
                if(rule.getValue())
                    stringBuilder.append("&"+rule.getKey()+"=true");
            }
        }
        if(!condition.contains("unspecified")){
            stringBuilder.append("&unspecified=false");
        }
        if(!condition.contains("used")){
            stringBuilder.append("&used=false");
        }
        if(!condition.contains("new")){
            stringBuilder.append("&new=false");
        }
        if(!condition.isEmpty()) {
            for(String value: condition){
                stringBuilder.append("&"+value+"=true");
            }
        }

        if(!distance.equals("-1")){
            stringBuilder.append("&distance="+distance);
        } else {
            stringBuilder.append("&distance=1000");
        }

        //if postalCode is exist
        if(!postalCode.equals("-1")){
            stringBuilder.append("&buyerPostalCode="+postalCode);
        } else {
            stringBuilder.append("&buyerPostalCode=91007");
        }

        return stringBuilder.toString();
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setCondition(HashMap<String, Boolean> condition) {
        HashSet<String> conditionKeys = new HashSet<>();
        for (Map.Entry<String, Boolean> row: condition.entrySet()){
            if(row.getValue()){
                conditionKeys.add(row.getKey());
            }
        }
        this.condition = conditionKeys;
    }

    public void setShipping(HashMap<String, Boolean> shippingOption) {
        this.shippingOption = shippingOption;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public void setPostalCode(String postalCode) {
        Log.d("postalCode","SearchModel"+postalCode);
        this.postalCode = postalCode;
    }

    public void setPriceRange(HashMap<String, String> priceRange) {
        this.priceRange = priceRange;
    }

    public String getKeyword() {
        return keyword;
    }
}
