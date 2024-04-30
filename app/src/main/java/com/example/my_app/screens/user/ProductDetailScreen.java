package com.example.my_app.screens.user;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

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
import com.example.my_app.models.Product;
import com.example.my_app.models.Rating;
import com.example.my_app.models.UserInfo;
import com.example.my_app.shared.GlobalVariable;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProductDetailScreen extends Fragment {
    FirebaseFirestore db;
    private BottomNavigationView bottomNavigationView;
    private ImageSlider imageSlider;
    private ImageView backButton, cartButton, addToCartBtn;
    private ShapeableImageView productDetailShopImage;
    private TextView productPrice, productName, productRatingDisplay, productDetailDescText,
            productDetailRatingBarText, productDetailTotalRatings, productDetailShopName;
    private RatingBar productRatingBar;

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

        productName.setText(product.getProductName());
        productPrice.setText("đ" + product.getPrice());
        if (product.getRatings() != null) {
            ratingNum = product.getRatings().size();
            for (Rating rating : product.getRatings()) {
                avgRating += rating.getRating();
            }
            avgRating = avgRating / ratingNum;
        }
        productRatingDisplay.setText(avgRating + " / 5");
        productDetailDescText.setText(product.getDesc());
        productRatingBar.setRating(avgRating);
        productDetailRatingBarText.setText(avgRating + "/5");
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
                bottomNavigationView.setVisibility(View.VISIBLE);
                requireActivity().getSupportFragmentManager().popBackStack("home_screen", FragmentManager.POP_BACK_STACK_INCLUSIVE);
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
}