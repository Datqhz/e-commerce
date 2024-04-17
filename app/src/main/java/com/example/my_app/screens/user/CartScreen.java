package com.example.my_app.screens.user;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.my_app.R;
import com.example.my_app.models.Cart;
import com.example.my_app.models.CartDetail;
import com.example.my_app.models.Product;
import com.example.my_app.shared.GlobalVariable;
import com.example.my_app.view_adapter.CartAdapter;
import com.example.my_app.view_adapter.QuantityListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CartScreen extends Fragment implements QuantityListener {
    private FirebaseFirestore db;
    private BottomNavigationView bottomNavigationView;
    private ImageView cartScreenBackBtn, noProductImage;
    private boolean isFromHome;
    private RecyclerView productCartContainer;
    private CartAdapter cartAdapter;
    private ProgressBar progressBar;
    private TextView totalPrice;
    private TextView cartBuyBtn;
    List<CartDetail> cartDetailList = new ArrayList<>();
    List<Product> productList = new ArrayList<>();

    public CartScreen(BottomNavigationView bottomNavigationView, boolean isFromHome) {
        this.bottomNavigationView = bottomNavigationView;
        this.isFromHome = isFromHome;
    }

    public CartScreen() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), onBackPressedCartCallback);
        View view = inflater.inflate(R.layout.fragment_cart_screen, container, false);

        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.INVISIBLE);
        }

        setControl(view);
        setEvent();

        return view;
    }

    public void setControl(View view) {
        cartScreenBackBtn = view.findViewById(R.id.cart_screen_back_btn);
        productCartContainer = view.findViewById(R.id.cart_screen_list_item);
        progressBar = view.findViewById(R.id.cart_screen_progress_circle);
        noProductImage = view.findViewById(R.id.cart_screen_no_product_image);
        totalPrice = view.findViewById(R.id.cart_screen_total_price);
        cartBuyBtn = view.findViewById(R.id.cart_screen_buy_btn);
    }

    public void setEvent() {
        setProductItemCartView();

        cartScreenBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFromHome) {
                    bottomNavigationView.setVisibility(View.VISIBLE);
                    requireActivity().getSupportFragmentManager().popBackStack();
                } else {
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });
    }

    private void setProductItemCartView() {
        productCartContainer.setHasFixedSize(true);
        productCartContainer.setLayoutManager(new LinearLayoutManager(this.getContext()));

        db = FirebaseFirestore.getInstance();
        cartAdapter = new CartAdapter(this.getContext(), productList, cartDetailList, this, new CartAdapter.OnQuantityChangeListener() {
            @Override
            public void onAddQuantity(Product product, int quantity) {
                getCartDetails(new CartDetailsCallBack() {
                    @Override
                    public void onCartDetailsCallback(List<CartDetail> cartDetails) {
                        db.collection("cart_detail").whereEqualTo("cartId", cartDetails.get(0).getCartId())
                                .whereEqualTo("productId", product.getProductId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot querySnapshot) {
                                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                            CartDetail cartDetail = document.toObject(CartDetail.class);
                                            cartDetailList.remove(cartDetail);
                                            cartDetail.setQuantity(quantity + 1);
                                            cartDetailList.add(cartDetail);

                                            db.collection("cart_detail").document(document.getId()).set(cartDetail);
                                            cartAdapter.updateData(productList, cartDetailList);
                                        }

                                    }
                                });
                    }
                });
            }

            @Override
            public void onRemoveQuantity(Product product, int quantity) {
                getCartDetails(new CartDetailsCallBack() {
                    @Override
                    public void onCartDetailsCallback(List<CartDetail> cartDetails) {
                        db.collection("cart_detail").whereEqualTo("cartId", cartDetails.get(0).getCartId())
                                .whereEqualTo("productId", product.getProductId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot querySnapshot) {
                                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                            CartDetail cartDetail = document.toObject(CartDetail.class);
                                            cartDetailList.remove(cartDetail);
                                            if (cartDetail.getQuantity() != 1) {
                                                cartDetail.setQuantity(cartDetail.getQuantity() - 1);
                                                cartDetailList.add(cartDetail);
                                                db.collection("cart_detail").document(document.getId()).set(cartDetail);
                                                cartAdapter.updateData(productList, cartDetailList);
                                            } else {
                                                productList.remove(product);
                                                db.collection("cart_detail").document(document.getId()).delete();
                                                cartAdapter.updateData(productList, cartDetailList);
                                                noProductImage.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    }
                                });
                    }
                });
            }
        });
        productCartContainer.setAdapter(cartAdapter);

        getCartDetails(new CartDetailsCallBack() {
            @Override
            public void onCartDetailsCallback(List<CartDetail> cartDetails) {
                for (CartDetail cartDetail : cartDetails) {
                    db.collection("products").document(cartDetail.getProductId()).get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot product) {
                                    Product item = product.toObject(Product.class);
                                    productList.add(item);

                                    progressBar.setVisibility(View.GONE);
                                    noProductImage.setVisibility(View.GONE);

                                    cartAdapter.notifyDataSetChanged();
                                }
                            });
                }
            }
        });
    }

    private void getCartDetails(CartDetailsCallBack callback) {
        db.collection("carts").whereEqualTo("uid", GlobalVariable.getUserInfo().getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot carts) {
                        if (!carts.isEmpty()) {
                            for (DocumentSnapshot cart : carts.getDocuments()) {
                                Cart cartObject = cart.toObject(Cart.class);
                                db.collection("cart_detail").whereEqualTo("cartId", cartObject.getCartId()).get()
                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot cartItems) {
                                                if (!cartItems.isEmpty()) {
                                                    for (DocumentSnapshot cartItem : cartItems.getDocuments()) {
                                                        CartDetail cartDetail = cartItem.toObject(CartDetail.class);
                                                        cartDetailList.add(cartDetail);
                                                    }
                                                    progressBar.setVisibility(View.GONE);
                                                    noProductImage.setVisibility(View.GONE);

                                                    callback.onCartDetailsCallback(cartDetailList);
                                                } else {
                                                    progressBar.setVisibility(View.GONE);
                                                    noProductImage.setVisibility(View.VISIBLE);
                                                }
                                            }
                                        });
                            }

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CartScreen.this.getContext(), "Fail to get data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private final OnBackPressedCallback onBackPressedCartCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if (bottomNavigationView != null) {
                bottomNavigationView.setVisibility(View.VISIBLE);
            }
            requireActivity().getSupportFragmentManager().popBackStack();
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        onBackPressedCartCallback.remove();
    }

    @Override
    public void onQuantityChange(List<Product> selectedProduct, List<CartDetail> cartDetailList) {
        int total = 0;
        int paymentPrice = 0;
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

        for (Product item : selectedProduct) {
            try {
                Number number = formatter.parse(item.getPrice());
                int num = number.intValue();
                for (CartDetail cartDetail : cartDetailList) {
                    if (Objects.equals(cartDetail.getProductId(), item.getProductId())) {
                        paymentPrice = num * cartDetail.getQuantity();
                    }
                }
                total += paymentPrice;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        String formattedPrice = formatter.format(total);
        totalPrice.setText("đ" + formattedPrice);

        cartBuyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                Fragment paymentFragment = new PaymentScreen(bottomNavigationView);
                bundle.putSerializable("products", (Serializable) selectedProduct);
                bundle.putSerializable("cartDetails", (Serializable) cartDetailList);
                bundle.putSerializable("totalPayment", formattedPrice);
                paymentFragment.setArguments(bundle);

                fragmentTransaction.setReorderingAllowed(true)
                        .replace(R.id.cart_screen_container, paymentFragment)
                        .addToBackStack("payment_screen")
                        .commit();
            }
        });
    }

    public interface CartDetailsCallBack {
        void onCartDetailsCallback(List<CartDetail> cartDetails);
    }
}