package com.example.my_app.view_adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.my_app.R;
import com.example.my_app.screens.admin.ShopInfoScreen;
import com.example.my_app.dto.UserDTO;

import java.text.SimpleDateFormat;
import java.util.List;

public class ShopPendingAdapter extends RecyclerView.Adapter<ShopPendingViewHolder> {

    List<UserDTO> shopList;
    public ShopPendingAdapter(List<UserDTO> shopList) {
        this.shopList = shopList;
    }

    @NonNull
    @Override
    public ShopPendingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shop_pending, parent,false);
        return new ShopPendingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopPendingViewHolder holder, int position) {
        UserDTO shop = shopList.get(position);
        holder.tvShopName.setText(shop.getDisplayName());
        holder.tvSendDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(shop.getCreateDate()));
        holder.btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ShopInfoScreen.class);
                intent.putExtra("userDTO", shop);
                view.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return shopList.size();
    }
}
