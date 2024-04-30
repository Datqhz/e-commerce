package com.example.my_app.view_adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.my_app.R;
import com.example.my_app.models.Category;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BuyerCategoryAdapter extends RecyclerView.Adapter<BuyerCategoryAdapter.ViewHolder> {
    List<Category> categories;
    Context context;

    public BuyerCategoryAdapter(List<Category> categories, Context context) {
        this.categories = categories;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.category_box, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Category category = this.categories.get(position);
        Picasso.get().load(category.getImageUrl()).into(holder.categoryImage);
        holder.categoryDescription.setText(category.getCategory());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, category.getCategory(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryImage;
        TextView categoryDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryImage = itemView.findViewById(R.id.categoryImage);
            categoryDescription = itemView.findViewById(R.id.categoryDescription);
        }
    }
}
