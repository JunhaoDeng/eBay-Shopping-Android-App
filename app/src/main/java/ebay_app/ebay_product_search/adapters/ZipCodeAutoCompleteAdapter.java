package ebay_app.ebay_product_search.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class ZipCodeAutoCompleteAdapter extends ArrayAdapter<String> {
    private List<String> zipCodeData;

    public ZipCodeAutoCompleteAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        zipCodeData = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return zipCodeData.size();
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return zipCodeData.get(position);
    }

    public void setData(List<String> newData) {
        zipCodeData.clear();
        zipCodeData.addAll(newData);
        notifyDataSetChanged();
    }

}