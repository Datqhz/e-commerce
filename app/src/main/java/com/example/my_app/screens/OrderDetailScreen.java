package com.example.my_app.screens;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.my_app.R;
import com.example.my_app.models.DSDetail;
import com.example.my_app.models.DeliveryStatus;
import com.example.my_app.models.OrderDetail;
import com.example.my_app.models.Orders;
import com.example.my_app.models.Product;
import com.example.my_app.view_adapter.ProductOrderAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class OrderDetailScreen extends AppCompatActivity {

    TextView edtStatus, edtshopName, edtTotal;
    RecyclerView listOrderDetail;
    private Orders order;
    ProductOrderAdapter adapter;
    private List<OrderDetail> orderDetailList = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ImageButton btnPrevious;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail_screen);
        setControl();
        setEvent();
    }
    private void setControl(){
        edtStatus = findViewById(R.id.statusName);
        edtshopName = findViewById(R.id.shopName1);
        edtTotal = findViewById(R.id.totalPrice1);
        btnPrevious = findViewById(R.id.btnPrevious);
        listOrderDetail = findViewById(R.id.allProduct);
        Intent intent = getIntent();
        order = (Orders) intent.getSerializableExtra("order");
        adapter = new ProductOrderAdapter(orderDetailList);
    }

    private void setEvent(){
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        listOrderDetail.setAdapter(adapter);
        listOrderDetail.setLayoutManager(new LinearLayoutManager(this));
        db.collection("order_detail").whereEqualTo("orderId",order.getOrderId()).addSnapshotListener(new EventListener<QuerySnapshot>(){
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                orderDetailList.clear();
                double total = 0;
                NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
                for(QueryDocumentSnapshot doc : value){
                    OrderDetail detail = doc.toObject(OrderDetail.class);
                    orderDetailList.add(detail);
                    try {
                        Number number = formatter.parse(detail.getPrice());
                        int num = number.intValue();
                        total+= detail.getQuantity()*num;
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    edtTotal.setText("₫"+formatter.format(total));
                }
                adapter.notifyDataSetChanged();
                //shop name
                db.collection("products").whereEqualTo("productId", orderDetailList.get(0).getProductId()).addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        for(QueryDocumentSnapshot doc : value){
                            Product product = doc.toObject(Product.class);
                            db.collection("users").document(product.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    edtshopName.setText(documentSnapshot.get("displayName").toString());
                                }
                            });
                        }
                    }
                });
            }
        });
        //order status(delivery)
        db.collection("ds_detail").whereEqualTo("orderId", order.getOrderId()).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                List<DSDetail> temp = new ArrayList<>();
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
                //status name
                db.collection("delivery_status").document(temp.get(0).getStatusId()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        assert value != null;
                        DeliveryStatus ds = value.toObject(DeliveryStatus.class);
                        edtStatus.setText("Đơn hàng "+ds.getStatusName());
                    }
                });
                //shop name

            }

        });


    }





}