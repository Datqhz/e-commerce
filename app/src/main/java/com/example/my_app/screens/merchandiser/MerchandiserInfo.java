package com.example.my_app.screens.merchandiser;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.my_app.R;
import com.example.my_app.models.Category;
import com.example.my_app.models.UserInfo;
import com.example.my_app.screens.admin.CategoryManagement;
import com.example.my_app.shared.GlobalVariable;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MerchandiserInfo extends AppCompatActivity {
    ImageView image;
    Button btnSave;
    EditText edtDisplayName;
    String imageUrlOld, imageUrlNew;
    ArrayList<String> userNameList;
    Uri imageUri, imageUriOld;
    String idUser;
    Bitmap bitmap;
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;
    private StorageReference storageReference;
    private Boolean checkUpdateImage = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchandiser_info);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        setControl();
        setEvent();
    }

    private void setControl() {
        image = findViewById(R.id.image);
        edtDisplayName = findViewById(R.id.edtDisplayName);
        btnSave = findViewById(R.id.btnSave);
        userNameList = new ArrayList<>();
    }
    private void setEvent() {
        imageUrlOld = GlobalVariable.userInfo.getAvatarLink();
        imageUriOld = Uri.parse(imageUrlOld);
        Picasso.get().load(imageUriOld).into(image);

        idUser = GlobalVariable.userInfo.getUid();
        edtDisplayName.setText(GlobalVariable.userInfo.getDisplayName());
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageFromGallery();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkUpdateImage){
                    uploadData(imageUrlOld);

                }else{
                    upLoadImage();
                }
            }
        });

    }
    private void getUserName() {
        firestore.collection("users")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("firestore error", error.getMessage());
                            return;
                        }
                        userNameList.clear();
                        for (QueryDocumentSnapshot dc : value) {
                            UserInfo userInfo = dc.toObject(UserInfo.class);
                            userNameList.add(userInfo.getDisplayName());
                        }
                    }
                });
    }
    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        resultLauncher.launch(intent);
    }
    ActivityResultLauncher<Intent>
            resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getData() != null) {
                        imageUri = data.getData();
                        image.setImageURI(imageUri);
                        checkUpdateImage = false;
                    }
                }
            }
    );
    private void upLoadImage(){
        String name = (edtDisplayName.getText().toString().replaceAll("\\s+", " ")).trim().toLowerCase();
        Boolean canNext = true;
        if(imageUri == null ){
            Toast.makeText(this, "Vui lòng chọn hình ảnh người dùng", Toast.LENGTH_SHORT).show();
            canNext = false;
        }
        if (name.equals("")) {
            edtDisplayName.setError("Vui lòng nhập tên người dùng!");
            edtDisplayName.setFocusable(true);
            canNext = false;
        }else{
            for(int i=0;i<userNameList.size();i++){
                if(name.equals(userNameList.get(i).toLowerCase())){
                    edtDisplayName.setError("Tên người dùng đã tồn tại!");
                    edtDisplayName.setFocusable(true);
                    canNext = false;
                }
            }
        }
        if (canNext) {
            if(imageUri!=null){
                final String randomName = UUID.randomUUID().toString();
                byte[] bytes = new byte[0];
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 40, byteArrayOutputStream);
                    bytes = byteArrayOutputStream.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                StorageReference ref = storageReference.child("imageUsers/" + randomName);
                ref.putBytes(bytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // getDownLoadUrl to store in string
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                if (uri != null) {
                                    imageUrlNew = uri.toString();

                                    String imageUrlDefault = "https://firebasestorage.googleapis.com/v0/b/e-commerce-e6344.appspot.com/o/imageUsers%2Fdefault.png?alt=media&token=c02eef84-cdbb-424c-ad2b-1670b9e95ab7";
                                    if(!imageUrlDefault.equals(imageUrlOld)){
                                        storageReference = storage.getReferenceFromUrl(imageUrlOld);
                                        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //Toast.makeText(v.getContext(), "Xóa hình ảnh thành công", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                    uploadData(imageUrlNew);
                                }
                                imageUri = null;
                                checkUpdateImage = true;

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MerchandiserInfo.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MerchandiserInfo.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void uploadData(String avatar){
        Map<String, Object> edited = new HashMap<>();
        edited.put("displayName", (edtDisplayName.getText().toString().replaceAll("\\s+", " ")).trim());
        GlobalVariable.userInfo.setAvatarLink(avatar);
        edited.put("avatarLink", avatar);
        edited.put("uid", idUser);
        firestore.collection("users").document(idUser).update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                finish();
                Toast.makeText(MerchandiserInfo.this, "Chỉnh sửa thông tin người dùng thành công", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MerchandiserInfo.this, "Chỉnh sửa thông tin người dùng thất bại", Toast.LENGTH_SHORT).show();
            }
        });

    }
}