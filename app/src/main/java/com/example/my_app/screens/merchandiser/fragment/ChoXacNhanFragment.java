package com.example.my_app.screens.merchandiser.fragment;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.my_app.R;
import com.example.my_app.models.DSDetail;
import com.example.my_app.models.OrderDetail;
import com.example.my_app.models.Orders;
import com.example.my_app.models.Product;
import com.example.my_app.shared.GlobalVariable;
import com.example.my_app.view_adapter.MerOrderAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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


public class ChoXacNhanFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<Orders> orderList = new ArrayList<>();
//    List<DSDetail> orderDetailList = new ArrayList<>();
    MerOrderAdapter myAdapter;
    RecyclerView recyclerView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        myAdapter = new MerOrderAdapter(getContext(), orderList);
        View view = inflater.inflate(R.layout.fragment_cho_xac_nhan, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        getOrdersList();
        return view;


    }

    public void getOrdersList(){
        List<String> orderOfShop = new ArrayList<>();
        db.collection("order_detail").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<OrderDetail> temp = new ArrayList<>();
                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                for (int i = 0; i < list.size(); i++) {
                    DocumentSnapshot doc = list.get(i);
                    temp.add(doc.toObject(OrderDetail.class));
                    if (i == list.size() - 1) {
                        for(OrderDetail dt : temp){
                            db.collection("products").document(dt.getProductId()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                                    Product product = value.toObject(Product.class);
                                    if(product.getUid().equals(GlobalVariable.userInfo.getUid())){
                                        if(!orderOfShop.contains(dt.getOrderId())){
                                            orderOfShop.add(dt.getOrderId());
                                        }
                                    }
                                    if (temp.indexOf(dt) == temp.size()-1){
                                        if(orderOfShop.size()>0){
                                            db.collection("orders").whereIn("orderId", orderOfShop).addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                @Override
                                                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                    orderList.clear();
                                                    List<DocumentSnapshot> list = value.getDocuments();
                                                    for (int i = 0; i < list.size(); i++) {
                                                        DocumentSnapshot doc = list.get(i);
                                                        Orders orders = new Orders();
                                                        orders.setOrderId(doc.getData().get("orderId").toString());
                                                        orders.setUid(doc.getData().get("uid").toString());
                                                        orders.setAddress(doc.getData().get("address").toString());
                                                        Timestamp timestamp = doc.getTimestamp("createDate");
                                                        if (timestamp != null) {
                                                            Date date = timestamp.toDate();
                                                            orders.setCreateDate(date);
                                                        }
                                                        orderList.add(orders);
                                                        if(i == list.size()-1){
                                                            getOrderPending();
                                                        }
                                                    }

                                                }
                                            });

                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });

    }
    public void getOrderPending(){
        for (Orders order : orderList){
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
                    boolean flagEnd = orderList.indexOf(order) == orderList.size()-1;
                    if(!temp.get(0).getStatusId().equals("dr6Fty3ZHTjvQuelIhhT")){
                        orderList.remove(order);
                    }
                    if(flagEnd){
                        myAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

    }

}