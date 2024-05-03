package com.example.my_app.screens.merchandiser.fragment;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.my_app.R;
import com.example.my_app.models.DSDetail;
import com.example.my_app.models.OrderDetail;
import com.example.my_app.models.Orders;
import com.example.my_app.shared.GlobalVariable;
import com.example.my_app.view_adapter.MerOrderAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
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


public class DaHuyFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<Orders> orderList = new ArrayList<>();
    //    List<DSDetail> orderDetailList = new ArrayList<>();
    MerOrderAdapter myAdapter;
    RecyclerView recyclerView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myAdapter = new MerOrderAdapter(getContext(), orderList);
        View view = inflater.inflate(R.layout.fragment_da_huy, container, false);
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
                for (QueryDocumentSnapshot doc: queryDocumentSnapshots){
                    temp.add(doc.toObject(OrderDetail.class));
                }
                for(OrderDetail dt : temp){
                    db.collection("products").document(dt.getProductId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            String uid = documentSnapshot.getString("uid");
                            assert uid != null;
                            if(uid.equals(GlobalVariable.userInfo.getUid())){
                                if(!orderOfShop.contains(dt.getOrderId())){
                                    orderOfShop.add(dt.getOrderId());
                                    System.out.println("add order id" + dt.getOrderId());
                                }
                            }
                            if (temp.indexOf(dt) == temp.size()-1){
                                db.collection("orders").whereIn("orderId", orderOfShop).addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                        orderList.clear();
                                        for (QueryDocumentSnapshot doc: value){
                                            System.out.println(doc.getData());
                                            Orders orders = new Orders();
                                            orders.setOrderId(doc.getData().get("orderId").toString());
                                            orders.setUid(doc.getData().get("uid").toString());
                                            orders.setAddress(doc.getData().get("address").toString());
                                            Timestamp timestamp = doc.getTimestamp("createDate");
                                            if (timestamp != null) {
                                                // Chuyển đổi Timestamp thành java.util.Date
                                                Date date = timestamp.toDate();
                                                orders.setCreateDate(date);
//                    System.out.println("add");
                                                orderList.add(orders);
                                                System.out.println(orders.toString());
                                            }
                                        }
                                        myAdapter.notifyDataSetChanged();
                                        getOrderPending();
                                    }
                                });
                            }
                        }
                    });

                }
            }
        });
    }
    public void getOrderPending(){
        for (Orders order : orderList){
            System.out.println("get ds_detail order id: " + order.getOrderId());
            List<DSDetail> temp = new ArrayList<>();
            db.collection("ds_detail").whereEqualTo("orderId", order.getOrderId()).addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                    for( QueryDocumentSnapshot doc: value){
                        Log.d(TAG, "QueryDocumentSnapshot data: " + doc.getData());
                        DSDetail detail = doc.toObject(DSDetail.class);
                        temp.add(detail);
                        System.out.println(detail.toString());

                    }
                    Collections.sort(temp, new Comparator<DSDetail>() {
                        @Override
                        public int compare(DSDetail o1, DSDetail o2) {
                            return o2.getDateOfStatus().compareTo(o1.getDateOfStatus());
                        }
                    });
                    if(!temp.get(0).getStatusId().equals("rgNCIrNNoNaxothCyNe8")){ /////chỉnh lại id status
                        orderList.remove(order);
                    }
                }
            });
        }
        myAdapter.notifyDataSetChanged();
    }
}