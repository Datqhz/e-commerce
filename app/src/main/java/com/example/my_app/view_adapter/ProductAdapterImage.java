package com.example.my_app.view_adapter;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.my_app.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProductAdapterImage extends RecyclerView.Adapter<ProductAdapterImage.ViewHolder> {
    private Context context;
    private ArrayList<Uri> uriArrayList;
    private ArrayList<String> listImageUrlDelete;
    private Boolean check;

    public ProductAdapterImage(Context context, ArrayList<Uri> uriArrayList, ArrayList<String> listImageUrlDelete, Boolean check) {
        this.context = context;
        this.uriArrayList = uriArrayList;
        this.listImageUrlDelete = listImageUrlDelete;
        this.check = check;
    }

    @NonNull
    @Override
    public ProductAdapterImage.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_image_product, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductAdapterImage.ViewHolder holder, int position) {
        //holder.imageView.setImageURI(uriArrayList.get(position));
        Picasso.get().load(uriArrayList.get(position)).into(holder.imageView);

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = uriArrayList.get(holder.getAbsoluteAdapterPosition()).toString();
                uriArrayList.remove(uriArrayList.get(holder.getAbsoluteAdapterPosition()));
                if(check == true) {
                    listImageUrlDelete.add(temp);
                }
                notifyItemRemoved(holder.getAbsoluteAdapterPosition());
                notifyItemRangeChanged(holder.getAbsoluteAdapterPosition(), getItemCount());
            }
        });
    }

    @Override
    public int getItemCount() {
        return uriArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView, delete;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.image);
            delete = itemView.findViewById(R.id.delete);
        }
    }
}
