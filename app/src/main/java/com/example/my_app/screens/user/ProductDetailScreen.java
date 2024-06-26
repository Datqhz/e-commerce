package com.example.my_app.screens.user;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.my_app.R;
import com.example.my_app.models.Cart;
import com.example.my_app.models.CartDetail;
import com.example.my_app.models.Orders;
import com.example.my_app.models.Product;
import com.example.my_app.models.Rating;
import com.example.my_app.models.UserInfo;
import com.example.my_app.shared.GlobalVariable;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ProductDetailScreen extends Fragment {
    FirebaseFirestore db;
    private BottomNavigationView bottomNavigationView;
    private ImageSlider imageSlider;
    private ImageView backButton, cartButton, addToCartBtn;
    private ShapeableImageView productDetailShopImage;
    private TextView productPrice, productName, productRatingDisplay, productDetailDescText,
            productDetailRatingBarText, productDetailTotalRatings, productDetailShopName, buyNowBtn;
    private RatingBar productRatingBar;
    boolean isToastShown = false;

    public ProductDetailScreen(BottomNavigationView bottomNavigationView) {
        this.bottomNavigationView = bottomNavigationView;
    }

    public ProductDetailScreen() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), onBackPressedProductDetailCallback);
        View view = inflater.inflate(R.layout.fragment_product_detail_screen, container, false);

        assert getArguments() != null;
        Product product = (Product) getArguments().getSerializable("product");

        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.INVISIBLE);
        }

        setControl(view, product);
        setEvent(view, product);

        return view;
    }

    public void setControl(View view, Product product) {
        float avgRating = 0;
        int ratingNum = 0;
        float myRating = 0;

        imageSlider = view.findViewById(R.id.product_detail_image_slider);
        backButton = view.findViewById(R.id.product_detail_back_button);
        cartButton = view.findViewById(R.id.product_detail_cart_button);
        productName = view.findViewById(R.id.product_detail_name);
        productPrice = view.findViewById(R.id.product_detail_price);
        productRatingDisplay = view.findViewById(R.id.product_detail_rating_display);
        productDetailDescText = view.findViewById(R.id.product_detail_desc_text);
        productRatingBar = view.findViewById(R.id.product_detail_rating_bar);
        productDetailRatingBarText = view.findViewById(R.id.product_detail_rating_bar_text);
        productDetailTotalRatings = view.findViewById(R.id.product_detail_total_ratings);
        productDetailShopImage = view.findViewById(R.id.product_detail_shop_image);
        productDetailShopName = view.findViewById(R.id.product_detail_shop_name);
        addToCartBtn = view.findViewById(R.id.product_detail_add_to_cart_btn);
        buyNowBtn = view.findViewById(R.id.product_detail_buy_now_btn);

        productName.setText(product.getProductName());
        productPrice.setText("đ" + product.getPrice());
        if (product.getRatings() != null) {
            ratingNum = product.getRatings().size();
            for (Rating rating : product.getRatings()) {
                if (Objects.equals(rating.getUid(), GlobalVariable.getUserInfo().getUid())) {
                    myRating = rating.getRating();
                }
                avgRating += rating.getRating();
            }
            avgRating = avgRating / ratingNum;
        }
        productRatingDisplay.setText(avgRating + " / 5");
        productDetailDescText.setText(product.getDesc());
        productRatingBar.setRating(myRating);
        productDetailRatingBarText.setText(myRating + "/5");
        productDetailTotalRatings.setText("(" + ratingNum + " đánh giá)");
    }

    public void setEvent(View view, Product product) {
        db = FirebaseFirestore.getInstance();

        List<String> productImageUrls = product.getListImageUrl();
        List<SlideModel> slideModels = new ArrayList<>();

        for (String url : productImageUrls) {
            slideModels.add(new SlideModel(url, ScaleTypes.FIT));
        }

        imageSlider.setImageList(slideModels, ScaleTypes.FIT);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomNavigationView != null) {
                    bottomNavigationView.setVisibility(View.VISIBLE);
                }
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        cartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CartScreen cartScreen = new CartScreen(bottomNavigationView, false);
                getParentFragmentManager().beginTransaction().setReorderingAllowed(true)
                        .replace(R.id.product_detail_container, cartScreen)
                        .addToBackStack("cart_screen")
                        .commit();
            }
        });

        setRatingToDatabase(product);

        buyNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Product> products = new ArrayList<>();
                List<CartDetail> cartDetails = new ArrayList<>();
                String price = product.getPrice();

                products.add(product);
                CartDetail cartDetail = new CartDetail();
                cartDetail.setCartId("anonymous");
                cartDetail.setProductId(product.getProductId());
                cartDetail.setQuantity(1);
                cartDetails.add(cartDetail);

                Bundle bundle = new Bundle();
                bundle.putSerializable("products", (Serializable) products);
                bundle.putSerializable("cartDetails", (Serializable) cartDetails);
                bundle.putSerializable("totalPayment", (Serializable) price);

                PaymentScreen paymentScreen = new PaymentScreen(bottomNavigationView);
                paymentScreen.setArguments(bundle);
                getParentFragmentManager().beginTransaction().setReorderingAllowed(true)
                        .replace(R.id.product_detail_container, paymentScreen)
                        .addToBackStack("")
                        .commit();
            }
        });

        db.collection("users").document(product.getUid()).get().
                addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        UserInfo userInfo = documentSnapshot.toObject(UserInfo.class);
                        Picasso.get().load(userInfo.getAvatarLink()).into(productDetailShopImage);
                        productDetailShopName.setText(userInfo.getDisplayName());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(view.getContext(), "No shop data found in database", Toast.LENGTH_SHORT).show();
                    }
                });

        addToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                                                            int temp = 0;
                                                            for (DocumentSnapshot cartItem : cartItems.getDocuments()) {
                                                                CartDetail cartDetail = cartItem.toObject(CartDetail.class);
                                                                if (Objects.equals(cartDetail.getProductId(), product.getProductId())) {
                                                                    temp += 1;
                                                                    cartDetail.setQuantity(cartDetail.getQuantity() + 1);
                                                                    db.collection("cart_detail").document(cartItem.getId()).set(cartDetail)
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void unused) {
                                                                                    Toast.makeText(ProductDetailScreen.this.getContext(),
                                                                                            "Added to cart successfully", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    Toast.makeText(ProductDetailScreen.this.getContext(),
                                                                                            "Added to cart fail", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                            if (temp == 0) {
                                                                addToCart(product.getProductId(), cartObject.getCartId());
                                                            }
                                                        } else {
                                                            addToCart(product.getProductId(), cartObject.getCartId());
                                                        }
                                                    }
                                                });
                                    }

                                }
                            }
                        });
            }
        });
    }

    private void addToCart(String productId, String cartId) {
        db = FirebaseFirestore.getInstance();

        CartDetail data = new CartDetail();
        data.setCartId(cartId);
        data.setProductId(productId);
        data.setQuantity(1);

        db.collection("cart_detail").add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(ProductDetailScreen.this.getContext(), "Added to cart successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProductDetailScreen.this.getContext(), "Added to cart fail", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void setRatingToDatabase(Product product) {
        productRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                final boolean[] isToastShown = {false};

                Rating ratingData = new Rating();
                ratingData.setUid(GlobalVariable.getUserInfo().getUid());
                ratingData.setRating(rating);
                ratingData.setComment("");

                db.collection("orders").whereEqualTo("uid", GlobalVariable.getUserInfo().getUid())
                        .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    List<DocumentSnapshot> orderDocs = queryDocumentSnapshots.getDocuments();
                                    final int[] queriesCompleted = {0};
                                    final int totalQueries = orderDocs.size();

                                    for (DocumentSnapshot order : orderDocs) {
                                        Orders orderItem = order.toObject(Orders.class);

                                        db.collection("order_detail")
                                                .whereEqualTo("orderId", orderItem.getOrderId())
                                                .whereEqualTo("productId", product.getProductId())
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        queriesCompleted[0]++;

                                                        if (task.isSuccessful()) {
                                                            QuerySnapshot queryDocumentSnapshots = task.getResult();
                                                            if (!queryDocumentSnapshots.isEmpty()) {
                                                                if (!isToastShown[0]) {
                                                                    ratingProduct(product, ratingData);
                                                                    isToastShown[0] = true;
                                                                }
                                                            }
                                                        } else {
                                                            Log.d("Firestore", "Error getting documents: ", task.getException());
                                                        }

                                                        if (queriesCompleted[0] == totalQueries && !isToastShown[0]) {
                                                            Toast.makeText(getContext(), "Bạn không thể đánh giá sản phẩm này", Toast.LENGTH_SHORT).show();
                                                            isToastShown[0] = true;
                                                            ratingBar.setRating(0);
                                                        }
                                                    }
                                                });
                                    }

                                } else {
                                    Toast.makeText(getContext(), "Bạn không thể đánh giá sản phẩm này", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    private void ratingProduct(Product product, Rating ratingData) {
        db.collection("products").document(product.getProductId())
                .collection("ratings")
                .document(GlobalVariable.getUserInfo().getUid())
                .set(ratingData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getContext(), "Đánh giá thành công", Toast.LENGTH_SHORT).show();
                        productDetailRatingBarText.setText(ratingData.getRating() + "/5");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Đánh giá thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private final OnBackPressedCallback onBackPressedProductDetailCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            bottomNavigationView.setVisibility(View.VISIBLE);
            requireActivity().getSupportFragmentManager().popBackStack();
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        onBackPressedProductDetailCallback.remove();
    }

    public interface RatingCallback {
        void onRatingChanged(boolean isValid);
    }
}