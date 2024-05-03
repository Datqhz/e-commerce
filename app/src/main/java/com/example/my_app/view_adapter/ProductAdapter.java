package com.example.my_app.view_adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.my_app.R;
import com.example.my_app.models.Product;
import com.example.my_app.screens.merchandiser.EditProduct;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.MyViewHolder> implements Filterable {
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Context context;
    private ArrayList<Product> productArrayList;
    private ArrayList<Product> productArrayListOld;

    public ProductAdapter(Context context, ArrayList<Product> productArrayList) {
        this.context = context;
        this.productArrayList = productArrayList;
        this.productArrayListOld = productArrayList;
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public ProductAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflater layout
        View view = LayoutInflater.from(context).inflate(R.layout.recyclerview_product, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductAdapter.MyViewHolder holder, int position) {
        // get position
        Product sp = productArrayList.get(position);
        // set text into text view, image into image view
        holder.tvProductName.setText(sp.getProductName());
        holder.tvCategory.setText(sp.getCategoryName());
        holder.tvPrice.setText(sp.getPrice()+" đ");
        holder.tvQuantity.setText(sp.getQuantity() + " Sản phẩm");

        ArrayList<String> listImageUrl = sp.getListImageUrl();
        if (listImageUrl != null && listImageUrl.size() > 0) {
            String imageUrl = listImageUrl.get(0);
            Picasso.get().load(imageUrl).into(holder.imageView);
        }

        holder.ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditProduct.class);
                intent.putStringArrayListExtra("listImageUrl", listImageUrl);
                intent.putExtra("productName", productArrayList.get(holder.getAbsoluteAdapterPosition()).getProductName());
                intent.putExtra("desc", productArrayList.get(holder.getAbsoluteAdapterPosition()).getDesc());
                intent.putExtra("categoryName", productArrayList.get(holder.getAbsoluteAdapterPosition()).getCategoryName());
                intent.putExtra("price", productArrayList.get(holder.getAbsoluteAdapterPosition()).getPrice());
                intent.putExtra("quantity", Integer.toString(productArrayList.get(holder.getAbsoluteAdapterPosition()).getQuantity()));
                intent.putExtra("productId", productArrayList.get(holder.getAbsoluteAdapterPosition()).getProductId());
                intent.putExtra("uid", productArrayList.get(holder.getAbsoluteAdapterPosition()).getUid());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        holder.ivdDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setMessage("Bạn có muốn xóa sản phẩm này không?")
                        .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int index = holder.getAbsoluteAdapterPosition();
                                firestore.collection("products").document(productArrayList.get(index)
                                        .getProductId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            for(int i = 0; i < listImageUrl.size();i++){
                                                storageReference = storage.getReferenceFromUrl(listImageUrl.get(i));
                                                storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        //Toast.makeText(v.getContext(), "Xóa hình ảnh thành công", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                            notifyDataSetChanged();
                                            Toast.makeText(v.getContext(), "Xóa sản phẩm thành công", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                dialog.dismiss();
                            }
                        });
                builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(v.getContext(), "Hủy", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {

        return productArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        // intialize objects
        private ImageView imageView, ivEdit, ivdDelete;
        private TextView tvProductName, tvCategory, tvPrice, tvQuantity;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            imageView = itemView.findViewById(R.id.imageView);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivdDelete = itemView.findViewById(R.id.ivDelete);
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String strSearch = constraint.toString();
                if (strSearch.isEmpty()){
                    productArrayList = productArrayListOld;
                }else {
                    ArrayList<Product> list = new ArrayList<>();
                    for (Product product : productArrayListOld){
                        if (product.getProductName().toLowerCase().contains(strSearch.toLowerCase())){
                            list.add(product);
                        }
                    }
                    productArrayList = list;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = productArrayList;

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                productArrayList = (ArrayList<Product>) results.values;
                notifyDataSetChanged();
            }
        };
    }

}

