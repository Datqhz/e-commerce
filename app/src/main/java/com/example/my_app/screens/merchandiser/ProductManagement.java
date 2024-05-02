package com.example.my_app.screens.merchandiser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.my_app.R;
import com.example.my_app.models.Product;
import com.example.my_app.models.UserInfo;
import com.example.my_app.screens.admin.CategoryManagement;
import com.example.my_app.screens.admin.ShopPendingListScreen;
import com.example.my_app.screens.authenticate.MainActivity;
import com.example.my_app.shared.GlobalVariable;
import com.example.my_app.view_adapter.ProductAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class ProductManagement extends AppCompatActivity {
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;
    private RecyclerView recyclerViewProduct;
    private ProductAdapter productAdapter;
    private Product product;
    private ArrayList<Product> productArrayList;
    private SearchView searchView;

    private UserInfo userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_management);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setControl();
        getProductInfo();

    }

    private void setControl(){
        recyclerViewProduct = (RecyclerView) findViewById((R.id.rvDSSP));
        recyclerViewProduct.setHasFixedSize(true);
        recyclerViewProduct.setLayoutManager(new LinearLayoutManager(this));
        productArrayList = new ArrayList<>();
        productAdapter = new ProductAdapter(ProductManagement.this, productArrayList);
        recyclerViewProduct.setAdapter(productAdapter);

    }
    private void getProductInfo() {
        System.out.println("get data");
        firestore.collection("products").whereEqualTo("uid", GlobalVariable.userInfo.getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error != null){
                            Log.e("firestore error", error.getMessage());
                            return;
                        }
                        productArrayList.clear();
                        for(QueryDocumentSnapshot dc : value){
                            Product product = dc.toObject(Product.class);
                            productArrayList.add(product);
                        }
                        productAdapter.notifyDataSetChanged();
                    }
                });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_product_management, menu);
        //Lấy menu
        MenuItem menuItem = menu.findItem(R.id.app_bar_search);
        //Lấy search view ra
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                productAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                productAdapter.getFilter().filter(newText);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemid = item.getItemId();

        if(itemid == R.id.menu_item_addProduct){
            Intent intent = new Intent(ProductManagement.this, AddProduct.class);
            startActivity(intent);
            return true;
        }else if(itemid == R.id.menu_item_logout){
            FirebaseAuth auth = FirebaseAuth.getInstance();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Bạn có muốn đăng xuất không?")
                    .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            auth.signOut();
                            Intent intent = new Intent(ProductManagement.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                            dialog.dismiss();
                        }
                    });
            builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(ProductManagement.this, "Hủy", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        }else  if(itemid == R.id.menu_item_revenue){
            Intent intent = new Intent(ProductManagement.this, Revenue.class);
            startActivity(intent);
            return true;

        }
        return super.onOptionsItemSelected(item);
    }

}