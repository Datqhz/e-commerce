package com.example.my_app.screens.user;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import android.widget.ImageButton;

import com.example.my_app.R;
import com.example.my_app.models.DSDetail;
import com.example.my_app.models.Orders;
import com.example.my_app.shared.GlobalVariable;
import com.example.my_app.view_adapter.UserOrderAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class UserOrderManagement extends AppCompatActivity {

    TabLayout tlTabs;
    RecyclerView rvOrderList;
    UserOrderAdapter adapter;
    List<Orders> userOrders;
    ImageButton btnPrevious;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_order_management);
        setControl();
        setEvent();
        Intent intent = getIntent();
        int selectedTab = (int) intent.getSerializableExtra("tab");
        if(selectedTab == 0) {
            tlTabs.getTabAt(1).select();
        }
        tlTabs.getTabAt(selectedTab).select();
    }
    private void setControl(){
        tlTabs = findViewById(R.id.tlTabs);
        tlTabs.addTab(tlTabs.newTab().setText("Chờ xác nhận"));
        tlTabs.addTab(tlTabs.newTab().setText("Đang giao"));
        tlTabs.addTab(tlTabs.newTab().setText("Đã giao"));
        tlTabs.addTab(tlTabs.newTab().setText("Đã hủy"));
        rvOrderList = findViewById(R.id.rvOrderList);

        btnPrevious = findViewById(R.id.btnPrevious);
    }
    private void setEvent(){
        tlTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition() == 0){
                    getAllOrder("dr6Fty3ZHTjvQuelIhhT"); // cho xac nhan
                }else if(tab.getPosition() == 1){//đang giao
                    getAllOrder("6v7w0BrijLDtLKRYoyoN");
                }else if(tab.getPosition() == 2){ //da giao
                    getAllOrder("IWjbS9pxytwmbyxRXcNi");
                }else { //da huy
                    getAllOrder("rgNCIrNNoNaxothCyNe8");
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        userOrders = new ArrayList<>();
        adapter = new UserOrderAdapter(this, userOrders);
        rvOrderList.setAdapter(adapter);
        rvOrderList.setLayoutManager(new LinearLayoutManager(this));
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void getAllOrder(String dsId){
        db.collection("orders").whereEqualTo("uid", GlobalVariable.userInfo.getUid()).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                userOrders.clear();
                List<DocumentSnapshot> list = value.getDocuments();
                for (int i = 0; i < list.size(); i++) {
                    DocumentSnapshot doc = list.get(i);
                    Orders orders = new Orders();
                    orders.setOrderId(doc.getData().get("orderId").toString());
                    orders.setUid(doc.getData().get("uid").toString());
                    orders.setAddress(doc.getData().get("address").toString());
                    Timestamp timestamp = doc.getTimestamp("createDate");
                    if (timestamp != null) {
                        // Chuyển đổi Timestamp thành java.util.Date
                        Date date = timestamp.toDate();
                        orders.setCreateDate(date);
                        userOrders.add(orders);
                    }
                    if(i == list.size()-1){
                        getOrderWithDSId(dsId);
                    }
                }
            }
        });
    }
    public void getOrderWithDSId(String dsId){
        for (Orders order : userOrders){
            List<DSDetail> temp = new ArrayList<>();
            db.collection("ds_detail").whereEqualTo("orderId", order.getOrderId()).addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                    for( QueryDocumentSnapshot doc: value){
                        DSDetail detail = doc.toObject(DSDetail.class);
                        temp.add(detail);

                    }
                    Collections.sort(temp, new Comparator<DSDetail>() {
                        @Override
                        public int compare(DSDetail o1, DSDetail o2) {
                            return o2.getDateOfStatus().compareTo(o1.getDateOfStatus());
                        }
                    });
                    boolean flagEnd = userOrders.indexOf(order) == userOrders.size()-1;
                    if(!temp.get(0).getStatusId().equals(dsId)){
                        userOrders.remove(order);
                    }
                    if(flagEnd){
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }

    }
}