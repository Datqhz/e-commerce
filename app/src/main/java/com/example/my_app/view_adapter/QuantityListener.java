package com.example.my_app.view_adapter;

import com.example.my_app.models.CartDetail;
import com.example.my_app.models.Product;

import java.util.List;

public interface QuantityListener {
    void onQuantityChange(List<Product> selectedProduct, List<CartDetail> cartDetailList);
}
