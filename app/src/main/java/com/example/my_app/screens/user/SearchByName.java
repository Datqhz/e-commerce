package com.example.my_app.screens.user;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.my_app.R;
import com.example.my_app.models.Category;
import com.example.my_app.models.Product;
import com.example.my_app.screens.authenticate.register.merchandiser.GetCCCDImg;
import com.example.my_app.screens.authenticate.register.merchandiser.RegisterShop;
import com.example.my_app.view_adapter.BuyerProductAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchByName extends AppCompatActivity {
    RecyclerView recyclerView;
    ImageButton btnPrevious, btnSearch;
    EditText edtSearch;

    List<Product> searchResult = new ArrayList<>();
    BuyerProductAdapter adapter;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_by_name);
        // at last set adapter to recycler view.
        setControl();
        setEvent();
    }
    private void setControl(){
        recyclerView = findViewById(R.id.Search_Name_products);
        btnPrevious = findViewById(R.id.Search_Name_btnPrevious);
        btnSearch = findViewById(R.id.Search_Name_btnSearch);
        edtSearch = findViewById(R.id.Search_Name_edtSearch);
        db = FirebaseFirestore.getInstance();
    }
    private void setEvent(){
        GridLayoutManager layoutManager=new GridLayoutManager(this,2);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new BuyerProductAdapter(searchResult, SearchByName.this,
                new BuyerProductAdapter.OnProductClickListener() {
                    @Override
                    public void onProductClick(Product product) {
                        /// Nav to product detail
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("product", product);

                        ProductDetailScreen fragment = new ProductDetailScreen();
                        fragment.setArguments(bundle);
                        getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                                .replace(R.id.search_by_name_container, fragment)
                                .addToBackStack("")
                                .commit();
                    }
                });
        recyclerView.setAdapter(adapter);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!edtSearch.getText().toString().trim().isEmpty()){
                    searchProductByName(edtSearch.getText().toString().trim());
                }
            }
        });
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    public void searchProductByName(String regex){
        db.collection("products").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {;
                    List<DocumentSnapshot> productList = queryDocumentSnapshots.getDocuments();
                    System.out.println("doc length: " + productList.size());
                    for (DocumentSnapshot product : productList) {
                        Product item = product.toObject(Product.class);
                        if(item.getProductName().contains(regex)){
                            searchResult.add(item);
                        }
                    }
                    System.out.println("result: " + searchResult.size());
                    adapter.notifyDataSetChanged();
                }
                else {
                    Toast.makeText(SearchByName.this, "Không có sản phẩm nào phù hợp với mô tả", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}