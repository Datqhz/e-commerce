package com.example.my_app.screens.user;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.my_app.R;
import com.example.my_app.screens.authenticate.MainActivity;
import com.example.my_app.shared.GlobalVariable;
import com.google.firebase.auth.FirebaseAuth;


public class ProfileScreen extends Fragment {

    ImageView ivAvatar;
    LinearLayout llMyOrder, llUpdateInfo, llChangePassword, llOrderPending, llOrderDelivery, llOrderComplete, llOrderCancel, llLogout;
    TextView tvDisplayName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), onBackPressedProfileCallback);

        View view =inflater.inflate(R.layout.fragment_profile_screen, container, false);
        setControl(view);
        setEvent(view);
        return view;
    }


    private void setControl(View view){
        ivAvatar = view.findViewById(R.id.Profile_ivAvatar);
        llMyOrder = view.findViewById(R.id.Profile_llMyOrder);
        llUpdateInfo = view.findViewById(R.id.Profile_llUpdateInfo);
        llChangePassword = view.findViewById(R.id.Profile_llChangePassword);
        llOrderPending= view.findViewById(R.id.Profile_llOrderPending);
        llOrderDelivery= view.findViewById(R.id.Profile_llOrderDelivery);
        llOrderComplete= view.findViewById(R.id.Profile_llOrderComplete);
        llOrderCancel= view.findViewById(R.id.Profile_llOrderCancel);
        tvDisplayName = view.findViewById(R.id.Profile_tvDisplayName);
        llLogout = view.findViewById(R.id.Profile_llLogout);
    }
    private void setEvent(View view){
        RequestOptions requestOptions = new RequestOptions().transform(new CircleCrop());
        Glide.with(this).load(GlobalVariable.userInfo.getAvatarLink()).apply(requestOptions).into(ivAvatar);
        tvDisplayName.setText(GlobalVariable.userInfo.getDisplayName());
        llMyOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), UserOrderManagement.class);
                intent.putExtra("tab", 2);
                startActivity(intent);
            }
        });
        llUpdateInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), UpdateUserInfo.class);
                startActivity(intent);
            }
        });
        llChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ChangePasswordScreen.class);
                startActivity(intent);
            }
        });
        llOrderPending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), UserOrderManagement.class);
                intent.putExtra("tab", 0);
                startActivity(intent);
            }
        });
        llOrderDelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), UserOrderManagement.class);
                intent.putExtra("tab", 1);
                startActivity(intent);
            }
        });
        llOrderComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), UserOrderManagement.class);
                intent.putExtra("tab", 2);
                startActivity(intent);
            }
        });
        llOrderCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), UserOrderManagement.class);
                intent.putExtra("tab", 3);
                startActivity(intent);
            }
        });
        llLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        RequestOptions requestOptions = new RequestOptions().transform(new CircleCrop());
        Glide.with(this).load(GlobalVariable.userInfo.getAvatarLink()).apply(requestOptions).into(ivAvatar);
        tvDisplayName.setText(GlobalVariable.userInfo.getDisplayName());
    }
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == 1) {
//            if (resultCode == Activity.RESULT_OK) {
//                // The data has been updated, reload the data
//                loadData();
//            }
//        }

    private final OnBackPressedCallback onBackPressedProfileCallback = new OnBackPressedCallback(true) {
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