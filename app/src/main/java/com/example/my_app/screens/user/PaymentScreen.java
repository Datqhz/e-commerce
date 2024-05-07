package com.example.my_app.screens.user;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.my_app.models.PaymentMethodData;
import com.example.my_app.models.Product;
import com.example.my_app.shared.GlobalVariable;
import com.example.my_app.view_adapter.PaymentAdapter;
import com.example.my_app.view_model.CheckoutViewModel;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.android.gms.wallet.contract.TaskResultContracts;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class PaymentScreen extends Fragment {
    private CheckoutViewModel model;

    private FirebaseFirestore db;
    private ImageView backBtn;
    private BottomNavigationView bottomNavigationView;
    private List<Product> products;
    private List<CartDetail> cartDetails;
    private String totalPaymentPrice;
    private RecyclerView productPaymentContainer;
    private ProgressBar progressBar;
    private TextView paymentButton, paymentMethodName;
    private TextView totalPrice, totalPayment, totalPaymentFinal, userNumber, userName, userAddress;
    private LinearLayout addressContainer, paymentMethod;
    private String currentAddress = "address";

    final String[] paymentMethodSelected = {PaymentMethodData.CASH_ON_DELIVERY.getDisplayName()};

    public PaymentScreen() {
    }

    public PaymentScreen(BottomNavigationView bottomNavigationView) {
        this.bottomNavigationView = bottomNavigationView;
    }

    private final ActivityResultLauncher<Task<PaymentData>> paymentDataLauncher =
            registerForActivityResult(new TaskResultContracts.GetPaymentDataResult(), result -> {
                int statusCode = result.getStatus().getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes.SUCCESS:
                        handlePaymentSuccess(result.getResult());
                        break;
                    //case CommonStatusCodes.CANCELED: The user canceled
                    case AutoResolveHelper.RESULT_ERROR:
                        handleError(statusCode, result.getStatus().getStatusMessage());
                        break;
                    case CommonStatusCodes.INTERNAL_ERROR:
                        handleError(statusCode, "Unexpected non API" +
                                " exception when trying to deliver the task result to an activity!");
                        break;
                }
            });

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

        model = new ViewModelProvider(this).get(CheckoutViewModel.class);
        model.canUseGooglePay.observe(getViewLifecycleOwner(), this::setGooglePayAvailable);

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
        paymentMethod = view.findViewById(R.id.payment_screen_payment_method);
        paymentMethodName = view.findViewById(R.id.payment_screen_method_name);

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

        paymentMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Choose a payment method");

                final CharSequence[] items = {
                        PaymentMethodData.CASH_ON_DELIVERY.getDisplayName(),
                        PaymentMethodData.MOMO.getDisplayName(),
                        PaymentMethodData.QR_CODE.getDisplayName(),
                        PaymentMethodData.GPAY.getDisplayName()
                };

                int checkedItem = 0;
                if (Objects.equals(paymentMethodSelected[0], PaymentMethodData.MOMO.getDisplayName()))
                    checkedItem = 1;
                else if (Objects.equals(paymentMethodSelected[0], PaymentMethodData.QR_CODE.getDisplayName()))
                    checkedItem = 2;

                builder.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        paymentMethodSelected[0] = (String) items[which];
                    }
                });

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        paymentMethodName.setText(paymentMethodSelected[0]);
                    }
                });
                builder.setNegativeButton("Cancel", null);

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    private void setGooglePayAvailable(boolean available) {
        if (!available) {
            Toast.makeText(getContext(), R.string.google_pay_status_unavailable, Toast.LENGTH_LONG).show();
        }
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
                    if (Objects.equals(paymentMethodSelected[0], PaymentMethodData.CASH_ON_DELIVERY.getDisplayName()))
                        createOrderData();
                    else if (Objects.equals(paymentMethodSelected[0], PaymentMethodData.MOMO.getDisplayName())) {
                        System.out.println("Momo payment");
                    } else if (Objects.equals(paymentMethodSelected[0], PaymentMethodData.QR_CODE.getDisplayName())) {
                        System.out.println("VN Pay QR");
                    } else if (Objects.equals(paymentMethodSelected[0], PaymentMethodData.GPAY.getDisplayName())) {
                        requestPayment();
                    }
                }
            }
        });
    }

    private void createOrderData() {
        db = FirebaseFirestore.getInstance();
        DocumentReference newOrderRef = db.collection("orders").document();

        Orders data = new Orders();
        data.setOrderId(newOrderRef.getId());
        data.setCreateDate(new Date());
        data.setAddress(currentAddress);
        data.setUid(GlobalVariable.userInfo.getUid());
        data.setPaymentMethod(paymentMethodSelected[0]);

        newOrderRef.set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(PaymentScreen.this.getContext(), "Your payment was successful", Toast.LENGTH_SHORT).show();
                for (Product product : products) {
                    int quantity = 0;

                    for (CartDetail cartDetail : cartDetails) {
                        if (Objects.equals(product.getProductId(), cartDetail.getProductId())) {
                            quantity = cartDetail.getQuantity();
                        }
                    }

                    if (!Objects.equals(cartDetails.get(0).getCartId(), "anonymous")) {
                        removeProductInCart(product);
                    }

                    db.collection("products").document(product.getProductId()).update("quantity", product.getQuantity() - quantity);

                    OrderDetail newOrderDetail = new OrderDetail();
                    newOrderDetail.setOrderId(newOrderRef.getId());
                    newOrderDetail.setProductId(product.getProductId());
                    newOrderDetail.setPrice(product.getPrice());
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
                                if (address.isDefault()) {
                                    currentAddress = address.getAddress();
                                    userAddress.setText(currentAddress);
                                }
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

    public void requestPayment() {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

        int price = 0;
        try {
            Number num = formatter.parse(totalPaymentPrice);
            price = num.intValue();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        final Task<PaymentData> task = model.getLoadPaymentDataTask(String.valueOf(price));
        task.addOnCompleteListener(paymentDataLauncher::launch);
    }


    private void handlePaymentSuccess(PaymentData paymentData) {
        createOrderData();
        final String paymentInfo = paymentData.toJson();

        try {
            JSONObject paymentMethodData = new JSONObject(paymentInfo).getJSONObject("paymentMethodData");

            final JSONObject info = paymentMethodData.getJSONObject("info");
            final String billingName = info.getJSONObject("billingAddress").getString("name");
//            Toast.makeText(getContext(), getString(R.string.payments_show_name, billingName), Toast.LENGTH_LONG).show();

            Log.d("Google Pay token", paymentMethodData
                    .getJSONObject("tokenizationData")
                    .getString("token"));

        } catch (JSONException e) {
            Log.e("handlePaymentSuccess", "Error: " + e);
        }
    }

    private void handleError(int statusCode, @Nullable String message) {
        Log.e("loadPaymentData failed",
                String.format(Locale.getDefault(), "Error code: %d, Message: %s", statusCode, message));
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