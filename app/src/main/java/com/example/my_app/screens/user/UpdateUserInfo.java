package com.example.my_app.screens.user;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.my_app.R;
import com.example.my_app.screens.authenticate.register.merchandiser.GetCCCDImg;
import com.example.my_app.shared.GlobalVariable;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class UpdateUserInfo extends AppCompatActivity {

    ImageView ivAvatar;
    Button btnSave;
    ImageButton btnPrevios;
    EditText edtDisplayName;
    FrameLayout flChooseImg;
    Uri newAvatar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user_info);
        setControl();
        setEvent();
    }
    private void setControl(){
        ivAvatar = findViewById(R.id.UpdateInfo_ivAvatar);
        btnPrevios = findViewById(R.id.UpdateInfo_btnPrevious);
        btnSave = findViewById(R.id.UpdateInfo_btnSave);
        edtDisplayName = findViewById(R.id.UpdateInfo_edtDisplayName);
        flChooseImg = findViewById(R.id.UpdateInfo_flChooseImg);
    }
    private void setEvent(){
        RequestOptions requestOptions = new RequestOptions().transform(new CircleCrop());
        Glide.with(this).load(GlobalVariable.userInfo.getAvatarLink())
                .transform(new BlurTransformation(12))
                .apply(requestOptions)
                .into(ivAvatar);
        edtDisplayName.setText(GlobalVariable.userInfo.getDisplayName());
        btnPrevios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(edtDisplayName.getText().toString().trim().isEmpty()){
                    edtDisplayName.setError("Vui lòng nhập tên hiển thị");
                    edtDisplayName.setFocusable(true);
                    return;
                }
                updateUserInfo();
            }
        });
        flChooseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageChooser();
            }
        });
    }
    void uploadAvatar(){
        try{
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), newAvatar);
            String uniqueString = UUID.randomUUID().toString();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageData = baos.toByteArray();
            StorageReference avatarRef = FirebaseStorage.getInstance().getReference().child("imageUsers/"+uniqueString+".jpg");
            avatarRef.putBytes(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    avatarRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            if(!GlobalVariable.userInfo.getAvatarLink().equals("https://firebasestorage.googleapis.com/v0/b/e-commerce-e6344.appspot.com/o/imageUsers%2Fdefault.png?alt=media&token=c1365a9b-f852-43ca-b9ed-23b8aeccc948")){
                                StorageReference oldRef = FirebaseStorage.getInstance().getReferenceFromUrl(GlobalVariable.userInfo.getAvatarLink());
                                oldRef.delete();
                            }
                            GlobalVariable.userInfo.setAvatarLink(uri.toString());
                            GlobalVariable.userInfo.setDisplayName(edtDisplayName.getText().toString().trim());
                            FirebaseFirestore.getInstance().collection("users").document(GlobalVariable.userInfo.getUid()).set(GlobalVariable.userInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    finish();
                                    Toast.makeText(getApplicationContext(), "Cập nhật thông tin cá nhân thành công!", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                }
            });
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    void updateUserInfo(){
        if(newAvatar!=null){
            uploadAvatar();
        }else {
            GlobalVariable.userInfo.setDisplayName(edtDisplayName.getText().toString().trim());
            FirebaseFirestore.getInstance().collection("users").document(GlobalVariable.userInfo.getUid()).set(GlobalVariable.userInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    finish();
                    Toast.makeText(getApplicationContext(), "Cập nhật thông tin cá nhân thành công!", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    void imageChooser() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), 200);
    }

    // this function is triggered when user
    // selects the image from the imageChooser
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == 200) {
                // Get the url of the image from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    newAvatar = selectedImageUri;
                    RequestOptions requestOptions = new RequestOptions().transform(new CircleCrop());
                    Glide.with(this).load(selectedImageUri)
                            .transform(new BlurTransformation(12))
                            .apply(requestOptions)
                            .into(ivAvatar);
                }
            }
        }
    }

}