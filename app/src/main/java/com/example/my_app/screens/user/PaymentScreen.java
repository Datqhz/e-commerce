package com.example.my_app.screens.user;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.my_app.R;
import com.example.my_app.models.Address;
import com.example.my_app.models.Cart;
import com.example.my_app.models.CartDetail;
import com.example.my_app.models.OrderDetail;
import com.example.my_app.models.Orders;
import com.example.my_app.models.Product;
import com.example.my_app.shared.GlobalVariable;
import com.example.my_app.view_adapter.PaymentAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class PaymentScreen extends Fragment {
    private FirebaseFirestore db;
    private ImageView backBtn;
    private BottomNavigationView bottomNavigationView;
    private List<Product> products;
    private List<CartDetail> cartDetails;
    private String totalPaymentPrice;
    private RecyclerView productPaymentContainer;
    private ProgressBar progressBar;
    private TextView paymentButton;
    private TextView totalPrice, totalPayment, totalPaymentFinal, userNumber, userName, userAddress;
    private LinearLayout addressContainer;

    public PaymentScreen() {
    }

    public PaymentScreen(BottomNavigationView bottomNavigationView) {
        this.bottomNavigationView = bottomNavigationView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), onBackPressedPaymentCallback);
        View view = inflater.inflate(R.layout.fragment_payment_screen, container, false);

        if (getArguments() != null) {
            products = (List<Product>) getArguments().getSerializable("products");
            cartDetails = (List<CartDetail>) getArguments().getSerializable("cartDetails");
            totalPaymentPrice = (String) getArguments().getSerializable("totalPayment");

            setControl(view);
            setEvent(products, cartDetails);
        }

        return view;
    }

    private void setControl(View view) {
        backBtn = view.findViewById(R.id.payment_screen_back_btn);
        productPaymentContainer = view.findViewById(R.id.payment_screen_list_item);
        progressBar = view.findViewById(R.id.payment_screen_progress_circle);
        paymentButton = view.findViewById(R.id.payment_screen_buy_btn);
        totalPrice = view.findViewById(R.id.payment_screen_total_price);
        totalPayment = view.findViewById(R.id.payment_screen_total_payment);
        totalPaymentFinal = view.findViewById(R.id.payment_screen_total_payment_final);
        userName = view.findViewById(R.id.payment_screen_name);
        userNumber = view.findViewById(R.id.payment_screen_phone_number);
        userAddress = view.findViewById(R.id.payment_screen_address);
        addressContainer = view.findViewById(R.id.payment_screen_address_container);

        totalPrice.setText("đ" + totalPaymentPrice);
        totalPayment.setText("đ" + totalPaymentPrice);
        totalPaymentFinal.setText("đ" + totalPaymentPrice);
        userName.setText(GlobalVariable.getUserInfo().getDisplayName());
        userNumber.setText(GlobalVariable.getUserInfo().getPhone());
    }

    private void setEvent(List<Product> products, List<CartDetail> cartDetails) {
        setOrderItemView(products, cartDetails);
        makePayment();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void setOrderItemView(List<Product> products, List<CartDetail> cartDetails) {
        productPaymentContainer.setHasFixedSize(true);
        productPaymentContainer.setLayoutManager(new LinearLayoutManager(this.getContext()));

        PaymentAdapter paymentAdapter = new PaymentAdapter(this.getContext(), products, cartDetails);
        productPaymentContainer.setAdapter(paymentAdapter);
        progressBar.setVisibility(View.GONE);
    }

    private void makePayment() {
        paymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = 0;
                boolean isValid = false;
                for (Product product : products) {
                    for (CartDetail cartDetail : cartDetails) {
                        if (Objects.equals(product.getProductId(), cartDetail.getProductId())) {
                            quantity = cartDetail.getQuantity();
                        }
                    }
                    if (product.getQuantity() - quantity >= 0) {
                        isValid = true;
                    } else {
                        isValid = false;
                        db = FirebaseFirestore.getInstance();
                        removeProductInCart(product);

                        Toast.makeText(PaymentScreen.this.getContext(), product.getProductName() + " out of stock", Toast.LENGTH_SHORT).show();

                        bottomNavigationView.setVisibility(View.VISIBLE);
                        requireActivity().getSupportFragmentManager().popBackStack("home_screen", FragmentManager.POP_BACK_STACK_INCLUSIVE);

                        break;
                    }
                }
                if (isValid) {

                    db = FirebaseFirestore.getInstance();
                    DocumentReference newOrderRef = db.collection("orders").document();

                    Orders data = new Orders();
                    data.setOrderId(newOrderRef.getId());
                    data.setCreateDate(new Date());
                    data.setAddress("address");
                    data.setUid(GlobalVariable.userInfo.getUid());

                    newOrderRef.set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(PaymentScreen.this.getContext(), "Your payment was successful", Toast.LENGTH_SHORT).show();
                            for (Product product : products) {
                                int paymentPrice = 0;
                                int quantity = 0;
                                String paymentPriceString = "";
                                for (CartDetail cartDetail : cartDetails) {
                                    NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
                                    if (Objects.equals(product.getProductId(), cartDetail.getProductId())) {
                                        quantity = cartDetail.getQuantity();
                                    }
                                    try {
                                        Number number = formatter.parse(product.getPrice());
                                        int num = number.intValue();
                                        paymentPrice = num * quantity;
                                        paymentPriceString = formatter.format(paymentPrice);
                                    } catch (ParseException e) {
                                        throw new RuntimeException(e);
                                    }
                                }

                                if (!Objects.equals(cartDetails.get(0).getCartId(), "anonymous")) {
                                    removeProductInCart(product);
                                }

                                db.collection("products").document(product.getProductId()).update("quantity", product.getQuantity() - quantity);

                                OrderDetail newOrderDetail = new OrderDetail();
                                newOrderDetail.setOrderId(newOrderRef.getId());
                                newOrderDetail.setProductId(product.getProductId());
                                newOrderDetail.setPrice(paymentPriceString);
                                newOrderDetail.setQuantity(quantity);
                                db.collection("order_detail").add(newOrderDetail).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(PaymentScreen.this.getContext(), "Error create order detail", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                bottomNavigationView.setVisibility(View.VISIBLE);
                                requireActivity().getSupportFragmentManager().popBackStack("home_screen", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            }
                            db.collection("delivery_status").whereEqualTo("statusName", "Chờ xác nhận").get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    String statusId = document.getId();

                                                    Map<String, Object> statusDetailData = new HashMap<>();
                                                    statusDetailData.put("statusId", statusId);
                                                    statusDetailData.put("orderId", newOrderRef.getId());
                                                    statusDetailData.put("dateOfStatus", new Date());

                                                    db.collection("ds_detail").add(statusDetailData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            Log.d("ds_detail", "Added successfully");
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.w("ds_detail", "Error adding document");
                                                        }
                                                    });
                                                }
                                            } else {
                                                Toast.makeText(PaymentScreen.this.getContext(), "Error getting status document", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    });
                }
            }
        });
    }

    private void removeProductInCart(Product product) {
        db.collection("carts").whereEqualTo("uid", GlobalVariable.getUserInfo().getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot carts) {
                        if (!carts.isEmpty()) {
                            for (DocumentSnapshot cart : carts.getDocuments()) {
                                Cart cartObject = cart.toObject(Cart.class);
                                db.collection("cart_detail").whereEqualTo("cartId", cartObject.getCartId())
                                        .whereEqualTo("productId", product.getProductId()).get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        document.getReference().delete();
                                                    }
                                                } else {
                                                    Toast.makeText(PaymentScreen.this.getContext(),
                                                            "Something went wrong", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                            }

                        }
                    }
                });
    }

    private void handleAddressLogic() {
        db = FirebaseFirestore.getInstance();

        db.collection("address").whereEqualTo("uid", GlobalVariable.getUserInfo().getUid())
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> addressDocs = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot document : addressDocs) {
                                Address address = document.toObject(Address.class);
                                if (address.isDefault())
                                    userAddress.setText(address.getAddress());
                            }
                        }
                    }
                });

        addressContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                AddressScreen addressFragment = new AddressScreen();
                fragmentTransaction.setReorderingAllowed(true)
                        .replace(R.id.payment_screen_container, addressFragment)
                        .addToBackStack("")
                        .commit();
            }
        });
    }

    private final OnBackPressedCallback onBackPressedPaymentCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            requireActivity().getSupportFragmentManager().popBackStack();
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        handleAddressLogic();
    }
}