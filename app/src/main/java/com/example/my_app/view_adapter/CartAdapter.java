package com.example.my_app.view_adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.my_app.R;
import com.example.my_app.models.CartDetail;
import com.example.my_app.models.Product;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    Context context;
    List<Product> list;
    List<Product> list_0 = new ArrayList<>();
    List<CartDetail> cartDetailList;
    QuantityListener quantityListener;
    private OnQuantityChangeListener onQuantityChangeListener;

    public CartAdapter(Context context, List<Product> list, List<CartDetail> cartDetailList,
                       QuantityListener quantityListener, OnQuantityChangeListener onQuantityChangeListener) {
        this.context = context;
        this.list = list;
        this.cartDetailList = cartDetailList;
        this.quantityListener = quantityListener;
        this.onQuantityChangeListener = onQuantityChangeListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product_horizontal_box, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (list != null && !list.isEmpty()) {
            final int[] quantity = {0};

            Product product = this.list.get(position);
            for (CartDetail cartDetailItem : cartDetailList) {
                if (Objects.equals(product.getProductId(), cartDetailItem.getProductId())) {
                    quantity[0] = cartDetailItem.getQuantity();
                }
            }
            Picasso.get().load(product.getListImageUrl().get(0)).into(holder.productImage);
            holder.productName.setText(product.getProductName());
            holder.productDescription.setText(product.getDesc());
            holder.productPrice.setText("đ" + product.getPrice());
            holder.productQuantity.setText(String.valueOf(quantity[0]));
            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.checkBox.isChecked()) {
                        list_0.add(list.get(holder.getBindingAdapterPosition()));
                    } else {
                        list_0.remove(list.get(holder.getBindingAdapterPosition()));
                    }
                    quantityListener.onQuantityChange(list_0, cartDetailList);
                }
            });
            holder.productPlus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.productQuantity.setText(String.valueOf(quantity[0]));
                    onQuantityChangeListener.onAddQuantity(product, quantity[0]);
                }
            });

            holder.productMinus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.productQuantity.setText(String.valueOf(quantity[0]));
                    onQuantityChangeListener.onRemoveQuantity(product, quantity[0]);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        ImageView productImage, productMinus, productPlus;
        TextView productName, productDescription, productPrice, productQuantity;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            checkBox = itemView.findViewById(R.id.product_horizontal_checkbox);
            productImage = itemView.findViewById(R.id.product_horizontal_image);
            productMinus = itemView.findViewById(R.id.product_horizontal_minus_quantity);
            productPlus = itemView.findViewById(R.id.product_horizontal_plus_quantity);
            productName = itemView.findViewById(R.id.product_horizontal_name);
            productDescription = itemView.findViewById(R.id.product_horizontal_description);
            productPrice = itemView.findViewById(R.id.product_horizontal_price);
            productQuantity = itemView.findViewById(R.id.product_horizontal_quantity);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Product> newProductList, List<CartDetail> newCartDetailList) {
        this.list = newProductList;
        this.cartDetailList = newCartDetailList;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateTotalPrice(List<Product> newProductList, List<CartDetail> newCartDetailList) {
        quantityListener.onQuantityChange(newProductList, newCartDetailList);
        notifyDataSetChanged();
    }

    public interface OnQuantityChangeListener {
        void onAddQuantity(Product product, int quantity);
        void onRemoveQuantity(Product product, int quantity);
    }
}
