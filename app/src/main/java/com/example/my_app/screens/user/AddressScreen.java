package com.example.my_app.screens.user;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.my_app.R;
import com.example.my_app.models.Address;
import com.example.my_app.shared.GlobalVariable;
import com.example.my_app.view_adapter.AddressAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AddressScreen extends Fragment {
    private FirebaseFirestore db;
    private ImageView backBtn;
    private ProgressBar progressBar;
    private List<Address> addresses;
    private AddressAdapter adapter;
    private RecyclerView addressList;
    private ConstraintLayout addAddressBtn;

    public AddressScreen() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), onBackPressedAddressCallback);
        View view = inflater.inflate(R.layout.fragment_address_screen, container, false);

        setControl(view);
        setEvent(view);

        return view;
    }

    private void setControl(View view) {
        db = FirebaseFirestore.getInstance();

        addAddressBtn = view.findViewById(R.id.address_screen_add_btn_container);
        backBtn = view.findViewById(R.id.address_screen_back_btn);
        progressBar = view.findViewById(R.id.address_screen_progress_circle);
        addressList = view.findViewById(R.id.address_screen_list_item);
    }

    private void setEvent(View view) {
        addresses = new ArrayList<>();

        GridLayoutManager addressLayoutManager = new GridLayoutManager(view.getContext(), 1);
        addressList.setLayoutManager(addressLayoutManager);

        adapter = new AddressAdapter(addresses, view.getContext(), new AddressAdapter.OnAddressClickedListener() {
            @Override
            public void onAddressClick(Address address) {
                setDefaultAddress(view, address);
            }

            @Override
            public void onAddressDelete(Address address) {
                deleteAddress(view, address);
            }
        });

        addressList.setAdapter(adapter);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        addAddressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                AddAddressScreen addAddressFragment = new AddAddressScreen();
                fragmentTransaction.setReorderingAllowed(true)
                        .replace(R.id.address_screen_container, addAddressFragment)
                        .addToBackStack("")
                        .commit();
            }
        });
    }

    private void getAddressData(View view) {
        db.collection("address").whereEqualTo("uid", GlobalVariable.getUserInfo().getUid())
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> addressDocs = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot addressDoc : addressDocs) {
                                Address address = addressDoc.toObject(Address.class);
                                addresses.add(address);
                                progressBar.setVisibility(View.GONE);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(view.getContext(), "Fail to get address data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setDefaultAddress(View view, Address address) {
        addresses = new ArrayList<>();
        address.setDefault(true);

        db.collection("address")
                .document(address.getAddressId())
                .set(address).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(view.getContext(), "Đổi đia chỉ thành công", Toast.LENGTH_SHORT).show();

                        String uid = GlobalVariable.getUserInfo().getUid();
                        String excludeAddressId = address.getAddressId();

                        db.collection("address").whereEqualTo("uid", uid)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @SuppressLint("NotifyDataSetChanged")
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                String addressId = document.getString("addressId");
                                                if (!addressId.equals(excludeAddressId)) {
                                                    document.getReference().update("default", false);
                                                }
                                            }
                                            adapter.notifyDataSetChanged();
                                        } else {
                                            Toast.makeText(view.getContext(), "Fail to get address data", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(view.getContext(), "Fail to get address data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteAddress(View view, Address address) {
        if (address.isDefault()) {
            db.collection("address").document(address.getAddressId()).delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            db.collection("address").whereEqualTo("uid", GlobalVariable.getUserInfo().getUid())
                                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @SuppressLint("NotifyDataSetChanged")
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            if (!queryDocumentSnapshots.isEmpty()) {
                                                List<DocumentSnapshot> addressDocs = queryDocumentSnapshots.getDocuments();
                                                List<Address> data = new ArrayList<>();
                                                for (DocumentSnapshot addressDoc : addressDocs) {
                                                    Address address = addressDoc.toObject(Address.class);
                                                    data.add(address);
                                                }
                                                Address temp = data.get(0);
                                                temp.setDefault(true);
                                                db.collection("address").document(temp.getAddressId()).set(temp);
                                                addresses.remove(address);
                                                adapter.notifyDataSetChanged();
                                                Toast.makeText(view.getContext(), "Địa chỉ đã xóa thành công", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(view.getContext(), "Địa chỉ đã xóa thành công", Toast.LENGTH_SHORT).show();
                                                addresses.remove(address);
                                                adapter.notifyDataSetChanged();
                                            }
                                        }
                                    });
                        }
                    });
        } else {
            db.collection("address").document(address.getAddressId()).delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onSuccess(Void unused) {
                            addresses.remove(address);
                            Toast.makeText(view.getContext(), "Địa chỉ đã xóa thành công", Toast.LENGTH_SHORT).show();
                            adapter.notifyDataSetChanged();
                        }
                    });
        }
    }

    private final OnBackPressedCallback onBackPressedAddressCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            requireActivity().getSupportFragmentManager().popBackStack();
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        onBackPressedAddressCallback.remove();
    }

    @Override
    public void onResume() {
        super.onResume();
        getAddressData(AddressScreen.this.getView());
    }
}