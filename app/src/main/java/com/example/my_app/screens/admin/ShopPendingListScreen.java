package com.example.my_app.screens.admin;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

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
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.my_app.R;
import com.example.my_app.models.ShopPending;
import com.example.my_app.screens.authenticate.MainActivity;
import com.example.my_app.view_adapter.ShopPendingAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ShopPendingListScreen extends AppCompatActivity {

    private SearchView searchView;
    RecyclerView rvPendingList;
    List<ShopPending> shopPendingList;
    ShopPendingAdapter shopPendingAdapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_pending_list_screen);
        setControl();
//        setEvent();
    }
    private  void setControl(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        rvPendingList = findViewById(R.id.shopPendingList_rvPendingList);
        shopPendingList = new ArrayList<>();
        getData();
        shopPendingAdapter = new ShopPendingAdapter(shopPendingList);
        rvPendingList.setAdapter(shopPendingAdapter);
        rvPendingList.setLayoutManager(new LinearLayoutManager(this));

    }
    private void getData(){
        db.collection("shopPendings")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            return;
                        }
                        shopPendingList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Log.d(TAG, "DocumentSnapshot data: " + doc.getData());
                            ShopPending shop = doc.toObject(ShopPending.class);
                            shopPendingList.add(shop);
                        }
                        shopPendingAdapter.notifyDataSetChanged();
                    }
                });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_admin, menu);
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
                shopPendingAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                shopPendingAdapter.getFilter().filter(newText);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemid = item.getItemId();

        if(itemid == R.id.menu_item_category){
            Intent intent = new Intent(ShopPendingListScreen.this, CategoryManagement.class);
            startActivity(intent);
            return true;
        } else if (itemid == R.id.menu_item_logout) {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Bạn có muốn đăng xuất không?")
                    .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            auth.signOut();
                            Intent intent = new Intent(ShopPendingListScreen.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                            dialog.dismiss();
                        }
                    });
            builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(ShopPendingListScreen.this, "Hủy", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}