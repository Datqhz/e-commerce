package com.example.my_app.screens.user;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.my_app.R;
import com.example.my_app.models.Address;
import com.example.my_app.shared.GlobalVariable;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddAddressScreen extends Fragment {
    private FirebaseFirestore db;
    private EditText addressText;
    private ImageView backBtn;
    private TextView addAddressBtn;

    public AddAddressScreen() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), onBackPressedAddAddressCallback);
        View view = inflater.inflate(R.layout.fragment_add_address_screen, container, false);

        setControl(view);
        setEvent(view);

        return view;
    }

    private void setControl(View view) {
        db = FirebaseFirestore.getInstance();
        addressText = view.findViewById(R.id.add_address_screen_edit_text);
        backBtn = view.findViewById(R.id.add_address_screen_back_btn);
        addAddressBtn = view.findViewById(R.id.add_address_screen_add_btn);
    }

    private void setEvent(View view) {
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        addAddressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleAddAddress(view);
            }
        });
    }

    private void handleAddAddress(View view) {
        DocumentReference newDocRef = db.collection("address").document();

        Address address = new Address();
        address.setAddressId(newDocRef.getId());
        address.setUid(GlobalVariable.getUserInfo().getUid());
        address.setAddress(addressText.getText().toString().trim());
        address.setDefault(false);

        newDocRef.set(address).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(view.getContext(), "Thêm địa chỉ thành công", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(view.getContext(), "Thêm địa chỉ thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private final OnBackPressedCallback onBackPressedAddAddressCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            requireActivity().getSupportFragmentManager().popBackStack();
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        onBackPressedAddAddressCallback.remove();
    }
}