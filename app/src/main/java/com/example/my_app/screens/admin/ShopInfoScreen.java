package com.example.my_app.screens.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.my_app.R;
import com.example.my_app.models.ShopPending;
import com.example.my_app.models.UserInfo;
import com.example.my_app.service.SendEmailTask;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;

public class ShopInfoScreen extends AppCompatActivity {
    TextView tvShopName, tvCCCD, tvPhoneNumber, tvEmail, tvSubmitDate;
    ImageView ivFrontView, ivBehindView;
    Button btnDecline, btnAccept;
    ImageButton btnPrevious;
    ShopPending shop;
    StorageReference storageRef= FirebaseStorage.getInstance().getReference();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_info_screen);
        setControl();
        setEvent();
    }
    private void setControl(){
        shop = (ShopPending) getIntent().getSerializableExtra("shop");
        tvShopName = findViewById(R.id.shopInfoScreen_tvShopName);
        tvCCCD = findViewById(R.id.shopInfoScreen_tvCCCD);
        tvPhoneNumber = findViewById(R.id.shopInfoScreen_tvPhoneNumber);
        tvEmail = findViewById(R.id.shopInfoScreen_tvEmail);
        tvSubmitDate = findViewById(R.id.shopInfoScreen_tvSubmitDate);
        ivFrontView = findViewById(R.id.shopInfoScreen_ivFrontView);
        ivBehindView = findViewById(R.id.shopInfoScreen_ivBehindView);
        btnDecline = findViewById(R.id.shopInfoScreen_btnDecline);
        btnAccept = findViewById(R.id.shopInfoScreen_btnAccept);
        btnPrevious = findViewById(R.id.shopInfoScreen_btnPrevious);
    }
    private void setEvent(){
        tvShopName.setText("Tên shop: "+shop.getDisplayName());
        tvCCCD.setText("CCCD: "+shop.getCccd());
        tvPhoneNumber.setText("SDT: "+shop.getPhone());
        tvEmail.setText("Email: "+shop.getEmail());
        tvSubmitDate.setText("Ngày submit: "+new SimpleDateFormat("dd/MM/yyyy").format(shop.getCreateDate()));
        for(String path: shop.getCCCDImg()){
            storageRef.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    if(path.substring(path.length()-5).startsWith("0")){

                        Glide.with(getApplicationContext()).load(uri).into(ivFrontView);
                    }else {
                        Glide.with(getApplicationContext()).load(uri).into(ivBehindView);
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        }
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        btnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    FirebaseFirestore.getInstance()
                            .collection("shopPendings")
                            .document(shop.getShopId())
                            .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(getApplicationContext(),
                                    "Đã từ chối đơn đăng ký bán hàng của shop " + shop.getDisplayName(),
                                    Toast.LENGTH_LONG).show();
                                StorageReference ref = FirebaseStorage.getInstance().getReference();
                                for(String path: shop.getCCCDImg()){
                                    ref.child(path).delete();
                                }
                                new SendEmailTask("Từ chối mở tài khoản bán hàng",
                                        "Đơn đăng ký bán hàng đã bị admin từ chối mở tài khoản.",
                                        shop.getEmail()).execute();
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(),
                                    "Xảy ra lỗi trong quá trình từ chối!",
                                    Toast.LENGTH_LONG).show();
                        }
                    });

            }
        });
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseApp tempApp = FirebaseApp.initializeApp(ShopInfoScreen.this);
                FirebaseAuth tempAuth = FirebaseAuth.getInstance(tempApp);
                tempAuth.createUserWithEmailAndPassword(shop.getEmail(), shop.getPassword())
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // User created, now send the verification email
                                FirebaseUser user = task.getResult().getUser();
                                user.sendEmailVerification()
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                System.out.println("sent email verify success");
                                                Toast.makeText(getApplicationContext(),
                                                        "Đơn đăng ký của shop "+ shop.getDisplayName() + " đã được duyệt.",
                                                        Toast.LENGTH_LONG).show();
                                                UserInfo tempUser = new UserInfo();
                                                tempUser.mapFromShopPendingToUser(shop);
                                                tempUser.setUid(user.getUid());
                                                tempUser.setAvatarLink("https://firebasestorage.googleapis.com/v0/b/e-commerce-e6344.appspot.com/o/imageUsers%2Fdefault.png?alt=media&token=c1365a9b-f852-43ca-b9ed-23b8aeccc948");
                                                FirebaseFirestore.getInstance().collection("users")
                                                        .document(user.getUid()).set(tempUser)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        FirebaseFirestore.getInstance()
                                                                .collection("shopPendings")
                                                                .document(shop.getShopId())
                                                                .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                tempApp.delete();
                                                                finish();
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(getApplicationContext(),
                                                                        "Xảy ra lỗi trong quá trình xử lý thông tin",
                                                                        Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                                    }
                                                });
                                            }

                                        });

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                tempAuth.createUserWithEmailAndPassword(shop.getPhone()+"@gmail.com", shop.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        FirebaseUser user = task.getResult().getUser();
                                        System.out.println("create user with phone success");
                                        new SendEmailTask("Đơn đăng ký đã đươợc duyệt!",
                                                "Đơn đăng ký bán hàng đã được admin chấp nhận.\nTên đăng nhập của bạn là "+shop.getPhone(),
                                                shop.getEmail()).execute();
                                        Toast.makeText(getApplicationContext(),
                                                "Đơn đăng ký của shop "+ shop.getDisplayName() + " đã được duyệt.",
                                                Toast.LENGTH_LONG).show();
                                        UserInfo tempUser = new UserInfo();
                                        tempUser.mapFromShopPendingToUser(shop);
                                        FirebaseFirestore.getInstance()
                                                .collection("users")
                                                .document(user.getUid()).set(tempUser)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                FirebaseFirestore.getInstance()
                                                        .collection("shopPendings")
                                                        .document(shop.getShopId()).delete()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        tempApp.delete();
                                                        finish();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getApplicationContext(),
                                                                "Xảy ra lỗi trong quá trình xử lý thông tin",
                                                                Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            }
                                        });

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        System.out.println("create user with phone fail");
                                        new SendEmailTask("Đơn đăng ký đã bị hủy!",
                                                "Đơn đăng ký bán hàng đã bị hủy.\nLý do: Email và số điện thoại của bạn đã được sử dụng để đăng ký tài khoản.",
                                                shop.getEmail()).execute();
                                        Toast.makeText(getApplicationContext(),
                                                "Đơn đăng ký của shop "+ shop.getDisplayName() + " đã bị hủy.\n Mã lỗi: Email và SDT đã được sử dụng.",
                                                Toast.LENGTH_LONG).show();
                                        tempApp.delete();
                                    }
                                });
                            }
                        });

            }
        });
    }
}