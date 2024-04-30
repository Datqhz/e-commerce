package com.example.my_app.view_adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.my_app.R;
import com.example.my_app.models.CartDetail;
import com.example.my_app.models.Product;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.ViewHolder> {
    Context context;
    List<Product> products;
    List<CartDetail> cartDetails;

    public PaymentAdapter(Context context, List<Product> products, List<CartDetail> cartDetails) {
        this.context = context;
        this.products = products;
        this.cartDetails = cartDetails;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_horizontal_only_view_box, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int quantity = 0;
        Product product = products.get(position);
        for (CartDetail cartDetail : cartDetails) {
            if (Objects.equals(product.getProductId(), cartDetail.getProductId())) {
                quantity = cartDetail.getQuantity();
            }
        }
        Picasso.get().load(product.getListImageUrl().get(0)).into(holder.productImage);
        holder.productName.setText(product.getProductName());
        holder.productDescription.setText(product.getDesc());
        holder.productPrice.setText("đ" + product.getPrice());
        holder.productQuantity.setText("x" + quantity);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productDescription, productPrice, productQuantity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            productImage = itemView.findViewById(R.id.product_horizontal_only_view_image);
            productName = itemView.findViewById(R.id.product_horizontal_only_view_name);
            productDescription = itemView.findViewById(R.id.product_horizontal_only_view_description);
            productPrice = itemView.findViewById(R.id.product_horizontal_only_view_price);
            productQuantity = itemView.findViewById(R.id.product_horizontal_only_view_quantity);
        }
    }
}
