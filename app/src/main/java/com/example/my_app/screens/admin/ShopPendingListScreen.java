package com.example.my_app.screens.admin;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.example.my_app.R;
import com.example.my_app.view_adapter.ShopPendingAdapter;
import com.example.my_app.dto.UserDTO;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ShopPendingListScreen extends AppCompatActivity {

    RecyclerView rvPendingList;
    List<UserDTO> shopPendingList;
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
                            UserDTO dto = doc.toObject(UserDTO.class);
                            dto.setId(doc.getId());
                            shopPendingList.add(dto);
                        }
                        shopPendingAdapter.notifyDataSetChanged();
                    }
                });

    }
}