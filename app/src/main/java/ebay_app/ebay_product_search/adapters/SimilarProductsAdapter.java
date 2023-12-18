package ebay_app.ebay_product_search.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import edu.gyaneshm.ebay_product_search.R;
import ebay_app.ebay_product_search.models.SimilarProductModel;

public class SimilarProductsAdapter extends RecyclerView.Adapter<SimilarProductsAdapter.SimilarProductViewHolder> {

    private Context context;
    private ArrayList<SimilarProductModel> similarProducts;

    public SimilarProductsAdapter(Context context, ArrayList<SimilarProductModel> similarProducts) {
        this.context = context;
        this.similarProducts = similarProducts;
    }

    @Override
    public SimilarProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.similar_item_card_view, parent, false);
        return new SimilarProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SimilarProductViewHolder holder, int position) {
        SimilarProductModel product = similarProducts.get(position);

        holder.titleTextView.setText(product.getTitle());
        holder.shippingTextView.setText(product.getShippingCost());
        holder.daysLeftTextView.setText(product.getTimeLeft());
        holder.priceTextView.setText(product.getPrice());

        Uri imageUri = Uri.parse(product.getImageUrl());
        Glide.with(context).load(imageUri).into(holder.productImageView);
    }

    @Override
    public int getItemCount() {
        return similarProducts.size();
    }

    public class SimilarProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImageView;
        TextView titleTextView;
        TextView shippingTextView;
        TextView daysLeftTextView;
        TextView priceTextView;

        public SimilarProductViewHolder(View itemView) {
            super(itemView);

            productImageView = itemView.findViewById(R.id.card_item_product_image);
            titleTextView = itemView.findViewById(R.id.card_item_product_title);
            shippingTextView = itemView.findViewById(R.id.card_item_product_shipping);
            daysLeftTextView = itemView.findViewById(R.id.card_item_product_time_left);
            priceTextView = itemView.findViewById(R.id.card_item_product_price);
        }
    }
}
