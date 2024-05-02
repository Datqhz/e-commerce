package com.example.my_app.screens.user;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.my_app.R;
import com.example.my_app.models.Category;
import com.example.my_app.view_adapter.BuyerCategoryAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class CategoryScreen extends Fragment {
    private FirebaseFirestore db;
    private BuyerCategoryAdapter adapter;
    private RecyclerView categoryList;
    private ImageView noProductFound;
    private ProgressBar progressBar;
    private List<Category> categories;

    public CategoryScreen() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), onBackPressedCategoryCallback);
        View view = inflater.inflate(R.layout.fragment_category_screen, container, false);

        setControl(view);
        setEvent(view);

        return view;
    }

    private void setControl(View view) {
        db = FirebaseFirestore.getInstance();
        categoryList = view.findViewById(R.id.category_screen_list_item);
        progressBar = view.findViewById(R.id.category_screen_progress_circle);
        noProductFound = view.findViewById(R.id.category_screen_no_category_image);
    }

    private void setEvent(View view) {
        categories = new ArrayList<>();
        getCategoriesData(view);

        GridLayoutManager categoryLayoutManager = new GridLayoutManager(view.getContext(), 4);
        categoryList.setLayoutManager(categoryLayoutManager);

        adapter = new BuyerCategoryAdapter(categories, view.getContext(), new BuyerCategoryAdapter.OnCategoryClickedListener() {
            @Override
            public void onCategoryClick(Category category) {
                Intent intent = new Intent(view.getContext(), ProductByCategory.class);
                intent.putExtra("category", category);
                startActivity(intent);
            }
        });

        categoryList.setAdapter(adapter);
    }

    private void getCategoriesData(View view) {
        db.collection("categories").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> categoriesDocs = queryDocumentSnapshots.getDocuments();

                            for (DocumentSnapshot category : categoriesDocs) {
                                Category item = category.toObject(Category.class);
                                categories.add(item);
                                progressBar.setVisibility(View.GONE);
                                noProductFound.setVisibility(View.GONE);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            progressBar.setVisibility(View.GONE);
                            noProductFound.setVisibility(View.VISIBLE);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(view.getContext(), "Fail to get categories data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private final OnBackPressedCallback onBackPressedCategoryCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            requireActivity().getSupportFragmentManager().popBackStack();
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}