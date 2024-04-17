package com.example.my_app.view_adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.my_app.R;
import com.example.my_app.models.ShopPending;
import com.example.my_app.screens.admin.ShopInfoScreen;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ShopPendingAdapter extends RecyclerView.Adapter<ShopPendingViewHolder> implements Filterable {

    List<ShopPending> shopList;
    List<ShopPending> shopListOld;
    public ShopPendingAdapter(List<ShopPending> shopList) {
        this.shopList = shopList;
        this.shopListOld = shopList;
    }

    @NonNull
    @Override
    public ShopPendingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shop_pending, parent,false);
        return new ShopPendingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopPendingViewHolder holder, int position) {
        ShopPending shop = shopList.get(position);
        holder.tvShopName.setText(shop.getDisplayName());
        holder.tvSendDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(shop.getCreateDate()));
        holder.btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ShopInfoScreen.class);
                intent.putExtra("shop", shop);
                view.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return shopList.size();
    }
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String strSearch = constraint.toString();
                if (strSearch.isEmpty()){
                    shopList = shopListOld;
                }else {
                    ArrayList<ShopPending> list = new ArrayList<>();
                    for (ShopPending shopPending : shopListOld){
                        if (shopPending.getDisplayName().toLowerCase().contains(strSearch.toLowerCase())){
                            list.add(shopPending);
                        }
                    }
                    shopList = list;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = shopList;

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                shopList = (List<ShopPending>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}
