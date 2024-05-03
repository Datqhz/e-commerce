package com.example.my_app.view_adapter;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.my_app.R;
import com.example.my_app.models.OrderDetail;
import com.example.my_app.models.Product;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ProductOrderAdapter extends RecyclerView.Adapter<ProductOrderAdapter.MyViewHolder> {

    List<OrderDetail> orderDetailList;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    public ProductOrderAdapter(List<OrderDetail> orderDetailList) {
        this.orderDetailList = orderDetailList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_detail_order, parent, false);
        return new ProductOrderAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        OrderDetail orderDetail = orderDetailList.get(position);
        db.collection("products").whereEqualTo("productId", orderDetail.getProductId()).addSnapshotListener(new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
            for(QueryDocumentSnapshot doc : value){
                Product product = doc.toObject(Product.class);
                holder.productName1.setText(product.getProductName());
                Picasso.get().load(product.getListImageUrl().get(0)).into(holder.imgProduct1);
            }


        }
    });
        String quant = String.valueOf(orderDetail.getQuantity());
        holder.price.setText("₫" + orderDetail.getPrice());
        holder.quantity.setText("x"+ quant);
    }

    @Override
    public int getItemCount() {
        return orderDetailList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView categoryName1, productName1, quantity, price;
        ImageView imgProduct1;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName1 = itemView.findViewById(R.id.shopName1);
            productName1 = itemView.findViewById(R.id.productName1);
            quantity = itemView.findViewById(R.id.quantity1);
            price = itemView.findViewById(R.id.price1);
            imgProduct1 = itemView.findViewById(R.id.imgProduct1);
        }
    }
}
