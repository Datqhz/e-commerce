package com.example.my_app.screens.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.my_app.R;
import com.example.my_app.models.Category;
import com.example.my_app.models.Product;
import com.example.my_app.view_adapter.BuyerProductAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProductByCategory extends AppCompatActivity {
    private FirebaseFirestore db;
    private TextView productByCategoryTitle;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private BuyerProductAdapter adapter;
    private ImageView backBtn;
    private List<Product> products = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_by_category);

        Intent intent = getIntent();
        Category category = (Category) intent.getSerializableExtra("category");

        setControl();
        setEvent(category);
    }

    public void setControl() {
        db = FirebaseFirestore.getInstance();
        productByCategoryTitle = findViewById(R.id.product_by_category_title);
        recyclerView = findViewById(R.id.product_by_category_list_item);
        progressBar = findViewById(R.id.product_by_category_progress_circle);
        backBtn = findViewById(R.id.product_by_category_back_btn);
    };

    public void setEvent(Category category) {
        GridLayoutManager layoutManager=new GridLayoutManager(this,2);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new BuyerProductAdapter(products, ProductByCategory.this, new BuyerProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("product", product);

                ProductDetailScreen fragment = new ProductDetailScreen();
                fragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                        .replace(R.id.product_by_category_container, fragment)
                        .addToBackStack("")
                        .commit();
            }
        });
        recyclerView.setAdapter(adapter);

        productByCategoryTitle.setText(category.getCategory());
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        db.collection("products").whereEqualTo("categoryName", category.getCategory()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        if (!querySnapshot.isEmpty()) {
                            List<DocumentSnapshot> productsDocs = querySnapshot.getDocuments();
                            for (DocumentSnapshot product : productsDocs) {
                                Product item = product.toObject(Product.class);
                                products.add(item);
                            }
                            progressBar.setVisibility(View.INVISIBLE);
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    };
}