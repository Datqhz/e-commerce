package com.example.my_app.view_adapter;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.my_app.R;

public class ShopPendingViewHolder extends RecyclerView.ViewHolder {

    TextView tvShopName, tvSendDate;
    ImageButton btnInfo;

    public ShopPendingViewHolder(@NonNull View itemView) {
        super(itemView);
        tvShopName = itemView.findViewById(R.id.shopPending_tvName);
        tvSendDate = itemView.findViewById(R.id.shopPending_tvDate);
        btnInfo = itemView.findViewById(R.id.shopPending_btnInfo);
    }
}
