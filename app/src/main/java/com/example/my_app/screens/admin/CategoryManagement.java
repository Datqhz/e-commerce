package com.example.my_app.screens.admin;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import com.example.my_app.screens.merchandiser.AddProduct;
import com.example.my_app.view_adapter.CategoryAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class CategoryManagement extends AppCompatActivity {
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;
    private StorageReference storageReference;
    private Uri imageUri, imageUriOld;
    private Bitmap bitmap;
    private Button btnSave, btnEdit;
    private EditText edtCategory;
    private ImageView imageCategory;
    private String imageUrlNew, imageUrlOld;
    private RecyclerView rvCategory;
    private ArrayList<Category> categoryArrayList;
    private ArrayList<String> categoryNameList;
    private CategoryAdapter categoryAdapter;
    private String categoryNameOld;
    private String categoryId;
    private Boolean isCreate = true;
    private Boolean checkChangeImage = true;
    private Boolean checkUpdateOrDelete = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_management);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        setControl();
        getCategoryInfo();
        setEvent();
    }
    private void setControl() {
        btnSave = (Button) findViewById(R.id.btnSave);
        btnEdit = (Button) findViewById(R.id.btnEdit);
        btnEdit.setEnabled(false);
        btnEdit.setTextColor(Color.GRAY);
        imageCategory = (ImageView) findViewById(R.id.imageCategory);
        edtCategory = (EditText) findViewById(R.id.edtCategory);
        rvCategory = (RecyclerView) findViewById((R.id.rvCategory));
        rvCategory.setHasFixedSize(true);
        rvCategory.setLayoutManager(new LinearLayoutManager(this));
        categoryArrayList = new ArrayList<>();
        categoryNameList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(CategoryManagement.this, categoryArrayList, new CategoryAdapter.GetData() {
            @Override
            public void onItemClick(Category category) {
                categoryNameOld = category.getCategory().toString();
                edtCategory.setText(categoryNameOld);
                System.out.println("0 " + edtCategory.getText().toString());
                imageUrlOld = category.getImageUrl();
                categoryId = category.getCategoryId();
                imageUriOld = Uri.parse(imageUrlOld);
                Picasso.get().load(imageUriOld).into(imageCategory);

                isCreate = false;
                btnSave.setEnabled(false);
                btnSave.setTextColor(Color.GRAY);
                btnEdit.setEnabled(true);
                btnEdit.setTextColor(Color.WHITE);
            }
        }, checkUpdateOrDelete);
        rvCategory.setAdapter(categoryAdapter);
    }
    private void getCategoryInfo() {
        firestore.collection("categories")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("firestore error", error.getMessage());
                            return;
                        }
                        categoryArrayList.clear();
                        categoryNameList.clear();
                        for (QueryDocumentSnapshot dc : value) {
                            Category category = dc.toObject(Category.class);
                            categoryArrayList.add(category);
                            categoryNameList.add(category.getCategory());
                        }
                        categoryAdapter.notifyDataSetChanged();
                    }
                });
    }
    private void setEvent() {
        imageCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageFromGallery();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String category = (edtCategory.getText().toString().replaceAll("\\s+", " ")).trim().toLowerCase();
                Boolean canNext = true;
                if(imageUri == null ){
                    Toast.makeText(CategoryManagement.this, "Vui lòng chọn hình ảnh danh mục", Toast.LENGTH_SHORT).show();
                    canNext = false;
                }
                if (category.equals("")) {
                    edtCategory.setError("Vui lòng nhập tên danh mục!");
                    edtCategory.setFocusable(true);
                    canNext = false;
                }else{
                    for(int i=0;i<categoryNameList.size();i++){
                        if(category.equals(categoryNameList.get(i).toLowerCase())){
                            edtCategory.setError("Tên danh mục đã tồn tại!");
                            edtCategory.setFocusable(true);
                            canNext = false;
                        }
                    }
                }
                if (canNext) {
                    uploadImage();
                }
            }
        });
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String category = (edtCategory.getText().toString().replaceAll("\\s+", " ")).trim().toLowerCase();
                Boolean canNext = true;
                if(imageUriOld == null ){
                    Toast.makeText(CategoryManagement.this, "Vui lòng chọn hình ảnh danh mục", Toast.LENGTH_SHORT).show();
                    canNext = false;
                }
                if (category.equals("")) {
                    edtCategory.setError("Vui lòng nhập tên danh mục!");
                    edtCategory.setFocusable(true);
                    canNext = false;
                }else if(!category.equals(categoryNameOld.toLowerCase())){
                    for(int i=0;i<categoryNameList.size();i++){
                        if(category.equals(categoryNameList.get(i).toLowerCase())){
                            edtCategory.setError("Tên danh mục đã tồn tại!");
                            edtCategory.setFocusable(true);
                            canNext = false;
                        }
                    }
                }
                if (canNext) {
                    if (checkChangeImage) {
                        updateCategory(imageUrlOld);
                    } else {
                        uploadImage();
                    }
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
                        imageCategory.setImageURI(imageUri);
                        checkChangeImage = false;
                    }
                }
            }
    );
    private void uploadImage() {

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
                StorageReference ref = storageReference.child("imagesCategory/" + randomName);
                ref.putBytes(bytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // getDownLoadUrl to store in string
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                if (uri != null) {
                                    imageUrlNew = uri.toString();
                                    if (!isCreate) {
                                        storageReference = storage.getReferenceFromUrl(imageUrlOld);
                                        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //Toast.makeText(CategoryManagement.this, "Xóa hình ảnh cũ thành công", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        updateCategory(imageUrlNew);

                                    } else {
                                        uploadCategoryInfo(imageUrlNew);
                                    }
                                    imageUri = null;
                                    checkChangeImage = true;
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(CategoryManagement.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CategoryManagement.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

    }
    private void uploadCategoryInfo(String imageUrlNew) {
        DocumentReference documentReference = firestore.collection("categories").document();
        Category category = new Category((edtCategory.getText().toString().replaceAll("\\s+", " ")).trim(), imageUrlNew, "");
        documentReference.set(category, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    categoryId = documentReference.getId();
                    category.setCategoryId(categoryId);
                    documentReference.set(category, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(CategoryManagement.this, "Thêm danh mục thành công", Toast.LENGTH_SHORT).show();
                                edtCategory.setText("");
                                imageCategory.setImageResource(R.drawable.icon_image_add);
                                isCreate = true;
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CategoryManagement.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CategoryManagement.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void updateCategory(String imageUrl) {

        Map<String, Object> edited = new HashMap<>();
        edited.put("category", (edtCategory.getText().toString().replaceAll("\\s+", " ")).trim());
        edited.put("imageUrl", imageUrl);
        edited.put("categoryId", categoryId);
        firestore.collection("categories").document(categoryId).update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(CategoryManagement.this, "Cập nhật danh mục thành công", Toast.LENGTH_SHORT).show();
                edtCategory.setText("");
                imageCategory.setImageResource(R.drawable.dot_border_img_category);
                isCreate = true;
                checkUpdateOrDelete = true;
            btnSave.setEnabled(true);
                btnSave.setTextColor(Color.WHITE);
                btnEdit.setEnabled(false);
                btnEdit.setTextColor(Color.GRAY);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CategoryManagement.this, "Cập nhật danh mục thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
