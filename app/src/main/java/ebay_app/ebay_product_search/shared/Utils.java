package ebay_app.ebay_product_search.shared;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import ebay_app.ebay_product_search.EbayProductSearchApplication;
import edu.gyaneshm.ebay_product_search.R;

public class Utils {
    public static String formatPriceToString(Double number) {
        if (number == null) {
            return "";
        }
        if (Math.floor(number) == number) {
            return String.format(Locale.getDefault(), "%.1f", number);
        } else {
            return String.format(Locale.getDefault(), "%.2f", number);
        }
    }

    public static String truncateString(String str, int len) {
        if (str.length() > len) {
            str = str.substring(0, len) + getString(R.string.horizontal_ellipsis);
        }
        return str;
    }

    public static String getString(int id) {
        return EbayProductSearchApplication.getInstance().getApplicationContext().getString(id);
    }

    public static void showToast(int id) {
        showToast(getString(id));
    }

    public static void showToast(String... messages) {
        StringBuilder str = new StringBuilder();
        for (String s :
                messages) {
            str.append(s);
            str.append(" ");
        }
        showToast(str.toString().trim());
    }

    public static void showToast(String message) {
//        Toast.makeText(
//                EbayProductSearchApplication.getInstance(),
//                message,
//                Toast.LENGTH_SHORT
//        ).show();
        Context context = EbayProductSearchApplication.getInstance().getApplicationContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.custom_toast, null);

        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(message);

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    public static String doubleToString(Double n) {
        return String.valueOf((int) Math.round(Math.floor(n)));
    }

    public static int doubleToInt(Double n) {
        return (int) Math.round(Math.floor(n));
    }

    public static String optString(JSONObject json, String key) {
        if (json.isNull(key))
            return null;
        else
            return json.optString(key, null);
    }

    public static Double optDouble(JSONObject json, String key) {
        if (json.isNull(key))
            return null;
        else
            return json.optDouble(key);
    }

    public static Boolean optBoolean(JSONObject json, String key) {
        if (json.isNull(key))
            return false;
        else
            return json.optBoolean(key, false);
    }

    public static JSONArray optJSONArray(JSONObject json, String key) {
        if (json.isNull(key))
            return new JSONArray();
        else
            return json.optJSONArray(key);
    }

    public static String capitalizeFirstCharacter(String str) {
        if (str == null) {
            return null;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static HashMap<String, String> toMap(JSONObject jsonobj)  throws JSONException {
        HashMap<String, String> map = new HashMap<String, String>();
        Iterator<String> keys = jsonobj.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            String value = jsonobj.getString(key);
            map.put(key, value);
        }
        return map;
    }

    public static Double parsePrice(String priceString) {
        if (priceString == null || priceString.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(priceString.replaceAll("[^\\d.]", ""));
        } catch (NumberFormatException e) {
            // Log the exception or handle it as needed
            return null;
        }
    }
}
