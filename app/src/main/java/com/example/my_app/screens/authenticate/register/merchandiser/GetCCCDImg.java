package com.example.my_app.screens.authenticate.register.merchandiser;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.my_app.R;
import com.example.my_app.models.UserInfo;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GetCCCDImg extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1888;

    UserInfo userInfo;
    Bitmap frontImg, behindImg;
    List<String> imgLink = new ArrayList<>();
    TextView tvPrevious, tvFront, tvBehind;
    Button btnRegisterShop;
    ImageView ivFront, ivBehind;
    FrameLayout flFront, flBehind;
    ImageButton btnRemoveFront, btnRemoveBehind;
    int currentTake;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_cccdimg);
        Intent intent = getIntent();
        userInfo = (UserInfo) intent.getSerializableExtra("user_info");
        setControl();
        setEvent();
    }
    private void setControl(){
        tvPrevious = (TextView) findViewById(R.id.tvPrevious);
        tvFront = (TextView) findViewById(R.id.tvFront);
        tvBehind = (TextView) findViewById(R.id.tvBehind);
        btnRegisterShop = (Button) findViewById(R.id.btnRegisterShop);
        ivFront = (ImageView) findViewById(R.id.ivFront);
        ivBehind = (ImageView) findViewById(R.id.ivBehind);
        flFront = (FrameLayout) findViewById(R.id.flFront);
        flBehind = (FrameLayout) findViewById(R.id.flBehind);
        btnRemoveBehind = (ImageButton) findViewById(R.id.btnRemoveBehind);
        btnRemoveFront = (ImageButton) findViewById(R.id.btnRemoveFront);
    }
    private void setEvent(){
        tvPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        btnRemoveFront.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ivFront.setImageResource(R.drawable.image);
                btnRemoveFront.setVisibility(View.GONE);
                frontImg = null;
                tvFront.setVisibility(View.VISIBLE);
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) ivFront.getLayoutParams();
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                marginParams.setMargins((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, displayMetrics),
                        0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, displayMetrics), 0);
//                ivFront.setLayoutParams(marginParams);
                ivFront.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
        });
        btnRemoveBehind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ivBehind.setImageResource(R.drawable.image);
                btnRemoveBehind.setVisibility(View.GONE);
                behindImg = null;
                tvBehind.setVisibility(View.VISIBLE);
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) ivBehind.getLayoutParams();
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                marginParams.setMargins((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, displayMetrics),
                        0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, displayMetrics), 0);
//                ivBehind.setLayoutParams(marginParams);
                ivBehind.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
        });
        ivFront.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentTake = 0;
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });
        ivBehind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentTake = 1;
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });
        btnRegisterShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(frontImg == null || behindImg == null){
                    Toast.makeText(GetCCCDImg.this, "Vui lòng chọn 2 ảnh của 2 mặt căn cước công dân trước khi gửi đơn đăng ký bán hàng", Toast.LENGTH_LONG).show();
                }else {

                    List<Bitmap> list = new ArrayList<>();
                    list.add(frontImg);
                    list.add(behindImg);
                    uploadImage(list);

                }
            }
        });
    }
    private void uploadImage(List<Bitmap> imgs){
        imgLink.clear();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        for(int i = 0; i<imgs.size();i++){
            String uniqueString = UUID.randomUUID().toString();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imgs.get(i).compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageData = baos.toByteArray();
            StorageReference cccdRef = storage.getReference().child("cccd/"+uniqueString+i+".jpg");
            imgLink.add("cccd/"+uniqueString+i+".jpg");
            UploadTask uploadTask = cccdRef.putBytes(imageData);
            int finalI = i;
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                if(finalI == imgs.size()-1){
                    System.out.println(imgLink);
                    userInfo.setCCCDImg(imgLink);
                    FirebaseFirestore.getInstance().collection("shopPendings").add(userInfo).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(GetCCCDImg.this, "Đơn đăng ký bán hàng đã được gửi cho admin, một email xác thực tài khoản sẽ được gửi đến bạn khi admin xác nhận đơn đăng ký.\nVui lòng xác thực email để hoàn tất đăng ký.", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });

                }
            }).addOnFailureListener(exception -> {
                Toast.makeText(GetCCCDImg.this, "Xảy ra sự cố trong quá trình lưu trữ hình ảnh", Toast.LENGTH_LONG).show();
            });
        }

    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Match the request 'pic id with requestCode
        if (requestCode == CAMERA_REQUEST) {
            // BitMap is data structure of image file which store the image in memory
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            // Set the image in imageview for display
            if(currentTake == 0){
                ivFront.setImageBitmap(photo);
                frontImg = photo;
                btnRemoveFront.setVisibility(View.VISIBLE);
                tvFront.setVisibility(View.GONE);
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) ivFront.getLayoutParams();
                marginParams.setMargins(0, 0, 0, 0);
//                ivFront.setLayoutParams(marginParams);
//                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) ivBehind.getLayoutParams();
//                layoutParams.setMargins(0, 0, 0, 0);
//                ivFront.setLayoutParams(layoutParams);
                ivFront.setScaleType(ImageView.ScaleType.FIT_XY);
            }else {
                ivBehind.setImageBitmap(photo);
                behindImg = photo;
                btnRemoveBehind.setVisibility(View.VISIBLE);
                tvBehind.setVisibility(View.GONE);
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) ivBehind.getLayoutParams();
                marginParams.setMargins(0, 0, 0, 0);
////                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) ivBehind.getLayoutParams();
////                layoutParams.setMargins(0, 0, 0, 0);
//                ivBehind.setLayoutParams(marginParams);
                ivBehind.setScaleType(ImageView.ScaleType.FIT_XY);
            }

        }
    }
}