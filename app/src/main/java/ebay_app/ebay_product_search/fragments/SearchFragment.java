package ebay_app.ebay_product_search.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;

import ebay_app.ebay_product_search.EbayProductSearchApplication;
import ebay_app.ebay_product_search.adapters.ZipCodeAutoCompleteAdapter;
import edu.gyaneshm.ebay_product_search.R;
import ebay_app.ebay_product_search.SearchResultActivity;
import ebay_app.ebay_product_search.data.SearchResultData;
import ebay_app.ebay_product_search.models.SearchFormModel;
import ebay_app.ebay_product_search.shared.Utils;

import android.widget.CompoundButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;

public class SearchFragment extends Fragment {
    private EditText mKeywordEditText;
    private TextView mKeywordErrorTextView;
    private Spinner mSortSpinner;

    private CheckBox mConditionNewCheckbox;
    private CheckBox mConditionUsedCheckbox;
    private CheckBox mConditionUnspecifiedCheckbox;

    private CheckBox mShippingLocalCheckbox;
    private CheckBox mShippingFreeCheckbox;
    private CheckBox mEnableNearbySearch;

    private TextView mSearchNearbyMilesHeader;
    private EditText mSearchNearbyMiles;

    private TextView mSearchNearbyRadioGroupHeader;
    private RadioGroup radioGroup;
    private RadioButton radioCurrentLocation;
    private RadioButton radioZipcode;


    private AutoCompleteTextView mEditTextZipcode;
    private TextView mEditTextZipcodeError;

    private Button mSearchButton;
    private Button mClearButton;
    private View mSearchButtonsLayout;

    private RequestQueue mRequestQueue = null;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment_layout, container, false);

        mKeywordEditText = view.findViewById(R.id.search_keyword);
        mKeywordErrorTextView = view.findViewById(R.id.search_keyword_error);

        mSortSpinner = view.findViewById(R.id.search_sort);
        initializeSortSpinner();

        mConditionNewCheckbox = view.findViewById(R.id.search_condition_new);
        mConditionUsedCheckbox = view.findViewById(R.id.search_condition_used);
        mConditionUnspecifiedCheckbox = view.findViewById(R.id.search_condition_unspecified);

        mShippingLocalCheckbox = view.findViewById(R.id.search_shipping_local);
        mShippingFreeCheckbox = view.findViewById(R.id.search_shipping_free);

        mEnableNearbySearch = view.findViewById(R.id.search_enable_nearby_search);
        mSearchNearbyMilesHeader = view.findViewById(R.id.search_nearby_search_miles_header);
        mSearchNearbyMiles = view.findViewById(R.id.search_nearby_search_miles);

        mSearchNearbyRadioGroupHeader = view.findViewById(R.id.search_nearby_radio_group_header);
        radioGroup = view.findViewById(R.id.search_nearby_radio_group); // 你的RadioGroup的ID
        radioCurrentLocation = view.findViewById(R.id.radio_current_location);
        radioZipcode = view.findViewById(R.id.radio_zipcode);
        mEditTextZipcode = view.findViewById(R.id.editTextZipcode);
        mEditTextZipcodeError = view.findViewById(R.id.editTextZipcodeError);

        mSearchButtonsLayout = view.findViewById(R.id.search_buttons_layout);
        mSearchButtonsLayout.setVisibility(View.VISIBLE);

        mSearchButton = view.findViewById(R.id.search_button);
        mClearButton = view.findViewById(R.id.clear_button);

        mSearchButton.setVisibility(View.VISIBLE);
        mClearButton.setVisibility(View.VISIBLE);

        mEnableNearbySearch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mSearchNearbyRadioGroupHeader.setVisibility(View.VISIBLE);
                    radioGroup.setVisibility(View.VISIBLE);
                    mSearchNearbyMilesHeader.setVisibility(View.VISIBLE);
                    mSearchNearbyMiles.setVisibility(View.VISIBLE);
                    mEditTextZipcode.setVisibility(View.VISIBLE);

                    radioCurrentLocation.setChecked(true);
                } else {
                    mSearchNearbyRadioGroupHeader.setVisibility(View.GONE);
                    radioGroup.setVisibility(View.GONE);
                    mSearchNearbyMilesHeader.setVisibility(View.GONE);
                    mSearchNearbyMiles.setVisibility(View.GONE);
                    mEditTextZipcode.setVisibility(View.GONE);
                }
            }
        });

        if (mEnableNearbySearch.isChecked()) {
            mSearchNearbyRadioGroupHeader.setVisibility(View.VISIBLE);
            radioGroup.setVisibility(View.VISIBLE);
            mSearchNearbyMilesHeader.setVisibility(View.VISIBLE);
            mSearchNearbyMiles.setVisibility(View.VISIBLE);
            mEditTextZipcode.setVisibility(View.VISIBLE);
        } else {
            mSearchNearbyRadioGroupHeader.setVisibility(View.GONE);
            radioGroup.setVisibility(View.GONE);
            mSearchNearbyMilesHeader.setVisibility(View.GONE);
            mSearchNearbyMiles.setVisibility(View.GONE);
            mEditTextZipcode.setVisibility(View.GONE);
        }

        setupAutoCompleteForZipCode();

        setUpSearchButton();
        setUpClearButton();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_current_location:
                        // 如果选择了"Current Location"，则禁用 mEditTextZipcode
                        mEditTextZipcode.setEnabled(false);
                        mEditTextZipcode.setText(""); // 清空文本
                        break;
                    case R.id.radio_zipcode:
                        // 如果选择了"Zipcode"，则启用 mEditTextZipcode
                        mEditTextZipcode.setEnabled(true);
                        break;
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll("zipcode");
            mRequestQueue.cancelAll("current_zipcode");
        }
    }

    private void initializeSortSpinner() {
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                getActivity(),
                R.array.search_categories,
                android.R.layout.simple_spinner_dropdown_item
        );
        mSortSpinner.setAdapter(categoryAdapter);
        mSortSpinner.setSelection(0);
    }

    private void setUpSearchButton() {
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean valid = true;
                String keyword = mKeywordEditText.getText().toString();
                if (
                        keyword.length() == 0 ||
                                keyword.matches("^ +$")
                ) {
                    valid = false;
                    mKeywordErrorTextView.setVisibility(View.VISIBLE);
                } else {
                    mKeywordErrorTextView.setVisibility(View.GONE);
                }
                String zipcode = mEditTextZipcode.getText().toString();
                if (mEnableNearbySearch.isChecked() && radioZipcode.isChecked() && !zipcode.matches("^\\d{5}$")) {
                    valid = false;
                    mEditTextZipcodeError.setVisibility(View.VISIBLE);
                } else {
                    mEditTextZipcodeError.setVisibility(View.GONE);
                }

                if (!valid) {
                    Utils.showToast(R.string.please_fix_all_fields_with_error);
                    return;
                }
                else {
                    launchSearchResultsActivity();
                }
            }
        });

    }

    private void setUpClearButton() {
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mKeywordEditText.setText("");
                mKeywordErrorTextView.setVisibility(View.GONE);

                mSortSpinner.setSelection(0);

                mConditionNewCheckbox.setChecked(false);
                mConditionUsedCheckbox.setChecked(false);
                mConditionUnspecifiedCheckbox.setChecked(false);

                mShippingLocalCheckbox.setChecked(false);
                mShippingFreeCheckbox.setChecked(false);
                mEnableNearbySearch.setChecked(false);

                mSearchNearbyMiles.setText(""); // Clear the nearby search miles
                mSearchNearbyMilesHeader.setVisibility(View.GONE);
                mSearchNearbyMiles.setVisibility(View.GONE);
                mEditTextZipcode.setText(""); // Clear the zipcode
                mEditTextZipcodeError.setVisibility(View.GONE);
            }
        });
    }

    private void launchSearchResultsActivity() {
        SearchFormModel searchFormData = new SearchFormModel();

        searchFormData.setKeyword(mKeywordEditText.getText().toString());

        int categoryPosition = mSortSpinner.getSelectedItemPosition();
        searchFormData.setCategory(getResources().getStringArray(R.array.search_categories_id)[categoryPosition]);

        HashMap<String, Boolean> condition = new HashMap<>();
        condition.put("new", mConditionNewCheckbox.isChecked());
        condition.put("used", mConditionUsedCheckbox.isChecked());
        condition.put("unspecified", mConditionUnspecifiedCheckbox.isChecked());
        searchFormData.setCondition(condition);

        HashMap<String, Boolean> shippingOption = new HashMap<>();
        shippingOption.put("local", mShippingLocalCheckbox.isChecked());
        shippingOption.put("free", mShippingFreeCheckbox.isChecked());
        searchFormData.setShipping(shippingOption);

        if (mEnableNearbySearch.isChecked()) {
            String distanceStr = mSearchNearbyMiles.getText().toString();
            if (distanceStr.isEmpty()) {
                distanceStr = "10";
            }
            searchFormData.setDistance(distanceStr);
            if (radioCurrentLocation.isChecked()) {
                fetchPostalCode(searchFormData);
            } else {
                searchFormData.setPostalCode(mEditTextZipcode.getText().toString());
            }
        }


        SearchResultData searchResultData = SearchResultData.getInstance();
        searchResultData.setSearchFormData(searchFormData);

        Intent intent = new Intent(getContext(), SearchResultActivity.class);
        startActivity(intent);
    }

    private void fetchPostalCode(final SearchFormModel searchFormData) {
        String ipAddress = getLocalIpAddress();
        if (ipAddress != null) {
            String apiUrl = "https://ipinfo.io/" + ipAddress + "?token=2d803fdf8cd403";
            Log.d("PostalCode", "Local IP address: " + ipAddress);
        } else {
            Log.e("PostalCode", "Local IP address is null.");
        }
        Log.d("PostalCode", "Fetching postal code...");
        String apiUrl = "https://ipinfo.io/" + ipAddress + "?token=2d803fdf8cd403"; // 构建请求的 URL

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, apiUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonResponse) {
                try {
                    Log.d("PostalCode", "Postal code fetched successfully.");

                    String postalCode = jsonResponse.optString("postal");

                    if (postalCode != null) {
                        searchFormData.setPostalCode(postalCode);

                        // 添加日志以查看 postalCode
                        Log.d("PostalCode", "Postal Code: " + postalCode);

                    } else {
                        // Handle the case where postalCode is null
                        Log.e("PostalCode", "Postal Code is null in the JSON response.");
                    }
                } catch (Exception e) {
                    // Handle other exceptions
                    Log.e("PostalCode", "Error in onResponse", e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // 处理错误
                error.printStackTrace();

                // 添加日志以查看错误信息
                Log.e("VolleyError", "Volley Error: " + error.getMessage());
            }
        });

        // 将请求添加到队列中以执行
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(EbayProductSearchApplication.getInstance().getApplicationContext());
        }
        mRequestQueue.add(jsonObjectRequest);
    }
    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress.getAddress().length == 4) {
                        String ipAddress = inetAddress.getHostAddress();
                        return ipAddress;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void fetchZipCodes(String query) {
        String url = "https://ebayexpressbackend-233.wl.r.appspot.com/getAPIResponse?curr=" + query;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray postalCodesArray = response.getJSONArray("postalCodes");
                            List<String> postalCodesList = new ArrayList<>();
                            for (int i = 0; i < postalCodesArray.length(); i++) {
                                JSONObject postalCodeObject = postalCodesArray.getJSONObject(i);
                                String postalCode = postalCodeObject.getString("postalCode");
                                postalCodesList.add(postalCode);
                            }
                            ((ZipCodeAutoCompleteAdapter) mEditTextZipcode.getAdapter()).setData(postalCodesList);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // 处理网络请求错误
                error.printStackTrace();
            }
        });

        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getContext());
        }
        mRequestQueue.add(jsonObjectRequest);
    }

    private void setupAutoCompleteForZipCode() {
        ZipCodeAutoCompleteAdapter adapter = new ZipCodeAutoCompleteAdapter(getContext(), android.R.layout.simple_dropdown_item_1line);
        mEditTextZipcode.setAdapter(adapter);
        mEditTextZipcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 0 && s.length() <= 4) {
                    fetchZipCodes(s.toString());
                } else {
                    adapter.clear();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}
