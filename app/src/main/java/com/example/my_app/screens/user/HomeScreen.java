package com.example.my_app.screens.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.my_app.R;
import com.example.my_app.models.Category;
import com.example.my_app.models.Product;
import com.example.my_app.models.Rating;
import com.example.my_app.view_adapter.BuyerCategoryAdapter;
import com.example.my_app.view_adapter.BuyerProductAdapter;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class HomeScreen extends AppCompatActivity {

    private FirebaseFirestore db;
    private ProgressBar categoryProgressBar, productProgressBar;
    private BottomNavigationView bottomNavigationView;
    private ImageButton homeScreenCartBtn;
    private TextView searchBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_screen);

        setControl();
        setEvent();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void setControl() {
        categoryProgressBar = findViewById(R.id.categoryProgressBar);
        productProgressBar = findViewById(R.id.productProgressBar);
        homeScreenCartBtn = findViewById(R.id.home_screen_cart_button);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        searchBox = findViewById(R.id.home_screen_search_box);
    }

    public void setEvent() {
        searchBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeScreen.this, SearchByName.class);
                startActivity(intent);
            }
        });

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment selectedFragment = null;
                Intent selectedActivity = null;
                int id = menuItem.getItemId();
                if (id == R.id.navigation_home) {
                    selectedActivity = new Intent(HomeScreen.this, HomeScreen.class);
                    startActivity(selectedActivity);
                } else if (id == R.id.navigation_category) {
                    selectedFragment = new CategoryScreen();
                } else if (id == R.id.navigation_profile) {
                    selectedFragment = new ProfileScreen();
                }
                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.home_fragment_container, selectedFragment)
                            .addToBackStack("")
                            .commit();
                }
                return true;
            }
        });

        RecyclerView categoryRecyclerView = findViewById(R.id.categories);
        categoryRecyclerView.setHasFixedSize(true);

//        FlexboxLayoutManager categoryLayoutManager =
//                new FlexboxLayoutManager(this, FlexDirection.ROW, FlexWrap.WRAP);
//        categoryLayoutManager.setAlignItems(AlignItems.CENTER);
//        categoryLayoutManager.setJustifyContent(JustifyContent.CENTER);
//        categoryRecyclerView.setLayoutManager(categoryLayoutManager);
        LinearLayoutManager horizontalLayoutManager
                = new LinearLayoutManager(HomeScreen.this, LinearLayoutManager.HORIZONTAL, false);
        categoryRecyclerView.setLayoutManager(horizontalLayoutManager);
        RecyclerView productRecyclerView = findViewById(R.id.products);
        productRecyclerView.setHasFixedSize(true);

        GridLayoutManager productLayoutManager = new GridLayoutManager(this, 2);
        productRecyclerView.setLayoutManager(productLayoutManager);

        db = FirebaseFirestore.getInstance();

        List<Category> categories = new ArrayList<>();
        List<Product> products = new ArrayList<>();

        BuyerCategoryAdapter categoryAdapter = new BuyerCategoryAdapter(categories, HomeScreen.this,
                new BuyerCategoryAdapter.OnCategoryClickedListener() {
                    @Override
                    public void onCategoryClick(Category category) {
                        Intent intent = new Intent(HomeScreen.this, ProductByCategory.class);
                        intent.putExtra("category", category);
                        startActivity(intent);
                    }
                });
        categoryRecyclerView.setAdapter(categoryAdapter);

        BuyerProductAdapter productAdapter = new BuyerProductAdapter(products, HomeScreen.this,
                new BuyerProductAdapter.OnProductClickListener() {
                    @Override
                    public void onProductClick(Product product) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("product", product);

                        ProductDetailScreen fragment = new ProductDetailScreen(bottomNavigationView);
                        fragment.setArguments(bundle);

                        getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                                .replace(R.id.home_fragment_container, fragment)
                                .addToBackStack("home_screen")
                                .commit();
                    }
                });
        productRecyclerView.setAdapter(productAdapter);

        homeScreenCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CartScreen cartScreen = new CartScreen(bottomNavigationView, true);
                getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                        .replace(R.id.home_fragment_container, cartScreen)
                        .addToBackStack("home_screen")
                        .commit();
            }
        });

        db.collection("categories").get().
                addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            categoryProgressBar.setVisibility(View.GONE);
                            List<DocumentSnapshot> categoryList = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot category : categoryList) {
                                Category item = category.toObject(Category.class);
                                categories.add(item);
                            }
                            categoryAdapter.notifyDataSetChanged();
                        }
                        else {
                            Toast.makeText(HomeScreen.this, "No category data found in database", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HomeScreen.this, "Fail to get the category data", Toast.LENGTH_SHORT).show();
                    }
                });

        db.collection("products").get().
                addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            productProgressBar.setVisibility(View.GONE);
                            List<DocumentSnapshot> productList = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot product : productList) {
                                String productId = product.getId();
                                List<Rating> ratings = new ArrayList<>();
                                Product item = product.toObject(Product.class);

                                db.collection("products").document(productId).collection("ratings").get().
                                        addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot task) {
                                                if (!task.isEmpty()) {
                                                    List<DocumentSnapshot> ratingList = task.getDocuments();
                                                    for (DocumentSnapshot rating : ratingList) {
                                                        Rating itemRating = rating.toObject(Rating.class);
                                                        ratings.add(itemRating);
                                                    }
                                                    assert item != null;
                                                    item.setRatings(ratings);

                                                    products.add(item);

                                                    productAdapter.notifyDataSetChanged();
                                                } else {
                                                    products.add(item);

                                                    productAdapter.notifyDataSetChanged();

                                                    Log.d("RatingData", "Error getting documents: ");
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(HomeScreen.this, "No ratings data found in database", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(HomeScreen.this, "No product data found in database", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HomeScreen.this, "Fail to get the product data", Toast.LENGTH_SHORT).show();
                    }
                });

    }
}