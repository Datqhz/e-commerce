package com.example.my_app.screens.merchandiser;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;



import android.content.Intent;
import android.graphics.Bitmap;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import android.widget.Spinner;
import android.widget.Toast;
import com.example.my_app.R;
import com.example.my_app.models.Product;
import com.example.my_app.shared.GlobalVariable;
import com.example.my_app.view_adapter.ProductAdapterImage;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
public class AddProduct extends AppCompatActivity {
    private String uid;
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;
    private StorageReference reference;
    private Uri imageUri;
    private ArrayList<Uri> uriArrayList;
    private Bitmap bitmap;
    private Button btnSave, btnPickImages;
    private RecyclerView rvAddImage;
    private ProductAdapterImage productAdapterImage;
    private ArrayList<String> listImageUrl;
    private EditText edtProductName, edtDesc, edtPrice, edtQuantity, edtDisCount;
    private ArrayList<String> categoryArrayList;
    private ArrayList<String> productNameList;
    private ArrayAdapter<String> adapter;
    private Spinner spCategory;
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            if(o.getResultCode() == RESULT_OK){
                if(o.getData() != null && o.getData().getClipData() != null){
                    int count = o.getData().getClipData().getItemCount();
                    for(int i = 0; i < count; i++){
                        if(uriArrayList.size() < 8){
                            imageUri = o.getData().getClipData().getItemAt(i).getUri();

                            uriArrayList.add(imageUri);
                        }else{
                            Toast.makeText(AddProduct.this, "Không thể chọn nhiều hơn 8 hình ảnh", Toast.LENGTH_SHORT).show();
                        }
                    }
                    productAdapterImage.notifyDataSetChanged();
                } else if (o.getData().getData() != null) {
                    if(uriArrayList.size() < 8){
                        //imageUrl = o.getData().getData().getPath();
                        imageUri = o.getData().getData();
                        uriArrayList.add(imageUri);
                    }else{
                        Toast.makeText(AddProduct.this, "Không thể chọn nhiều hơn 8 hình ảnh", Toast.LENGTH_SHORT).show();
                    }
                }
                productAdapterImage.notifyDataSetChanged();
            }
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        uid = GlobalVariable.userInfo.getUid();

        setControl();
        getCategoryInfo();
        getProductName();
        setEvent();
    }
    private void setControl(){
        btnSave = (Button) findViewById(R.id.btnSave);
        btnPickImages = (Button) findViewById(R.id.btnPickImages);
        rvAddImage =  findViewById(R.id.rvAddImage);
        uriArrayList = new ArrayList<>();
        listImageUrl = new ArrayList<>();
        rvAddImage.setLayoutManager(new GridLayoutManager(AddProduct.this, 4));
        productAdapterImage = new ProductAdapterImage(AddProduct.this,uriArrayList,null, false);
        rvAddImage.setAdapter(productAdapterImage);

        edtProductName = (EditText) findViewById(R.id.edtProductName);
        edtDesc = (EditText) findViewById(R.id.edtDesc);
        edtPrice = (EditText) findViewById(R.id.edtPrice);
        edtQuantity = (EditText) findViewById(R.id.edtQuantity);
        edtDisCount = (EditText) findViewById(R.id.edtDisCount);
        spCategory = (Spinner) findViewById(R.id.spCategory);


        categoryArrayList = new ArrayList<>();
        productNameList = new ArrayList<>();
        adapter = new ArrayAdapter<>(AddProduct.this, android.R.layout.simple_spinner_dropdown_item, categoryArrayList);
        spCategory.setAdapter(adapter);

    }
    private void getCategoryInfo() {
        firestore.collection("categories").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        QuerySnapshot doc = task.getResult();
                        for (DocumentSnapshot documentSnapshot: doc.getDocuments()){
                            categoryArrayList.add(documentSnapshot.get("category").toString());
                        }
                        adapter.notifyDataSetChanged();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddProduct.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void getProductName() {
        firestore.collection("products").whereEqualTo("uid",GlobalVariable.userInfo.getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error != null){
                            Log.e("firestore error", error.getMessage());
                            return;
                        }
                        productNameList.clear();
                        for(QueryDocumentSnapshot dc : value){
                            productNameList.add(dc.get("productName").toString());
                        }
                    }
                });
    }
    private void setEvent() {

        btnPickImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pickImageFromGallery();
            }
        });

        uploadCategoryintoProduct();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //uploadProductInfo();
                uploadImage();
            }
        });
    }
    private void uploadCategoryintoProduct(){
        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spCategory.getSelectedItem().toString().equals(view);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    private void pickImageFromGallery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activityResultLauncher.launch(Intent.createChooser(intent, "Chọn hình ảnh"));
    }
    private void uploadImage() {
        String productName = (edtProductName.getText().toString().replaceAll("\\s+", " ")).trim().toLowerCase();
        Boolean canNext = true;
        if(uriArrayList == null ){
            Toast.makeText(this, "Vui lòng chọn hình ảnh danh mục", Toast.LENGTH_SHORT).show();
            canNext = false;
        }
        if (productName.equals("")) {
            edtProductName.setError("Vui lòng nhập tên sản phẩm!");
            edtProductName.setFocusable(true);
            canNext = false;
        }else {
            for(int i=0;i<productNameList.size();i++){
                if(productName.equals(productNameList.get(i).toLowerCase())){
                    edtProductName.setError("Tên sản phẩm đã tồn tại!");
                    edtProductName.setFocusable(true);
                    canNext = false;
                }
            }
        }
        if (edtDesc.getText().toString().trim().equals("")) {
            edtDesc.setError("Vui lòng nhập mô tả sản phẩm!");
            edtDesc.setFocusable(true);
            canNext = false;
        }
        if (edtPrice.getText().toString().trim().equals("")) {
            edtPrice.setError("Vui lòng nhập giá sản phẩm!");
            edtPrice.setFocusable(true);
            canNext = false;
        }
        if (edtQuantity.getText().toString().trim().equals("")) {
            edtQuantity.setError("Vui lòng nhập số lượng sản phẩm!");
            edtQuantity.setFocusable(true);
            canNext = false;
        }
        if(edtDisCount.getText().toString().trim().equals("")){
            edtDisCount.setText("0");
        }
        if(Integer.parseInt(edtDisCount.getText().toString().trim()) > 100){
            edtDisCount.setError(" Mã giảm giá không hợp lệ, Vui lòng nhập lại mã giảm giá tối đa 100!");
            edtDisCount.setFocusable(true);
            canNext = false;
        }
        if (canNext) {
            final String randomName = UUID.randomUUID().toString();
            for (int i = 0; i < uriArrayList.size(); i++) {
                Uri tempImageUri = uriArrayList.get(i);
                byte[] bytes = new byte[0];
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), tempImageUri);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 40, byteArrayOutputStream);
                    bytes = byteArrayOutputStream.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //System.out.println("lisImageUri " + tempImageUri);
                if (bytes != null) {
                    StorageReference referenceImageFolder = storage.getReference().child("imagesProduct/");
                    final StorageReference imageName = referenceImageFolder.child("image" + i + ": " + randomName);
                    imageName.putBytes(bytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageName.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    listImageUrl.add(uri.toString());
                                    if (listImageUrl.size() == uriArrayList.size()) {
                                        uploadProductInfo(listImageUrl);
                                    }
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                }
            }
        }
    }
    private void uploadProductInfo(ArrayList<String> listImageUrl){
//        Product product = new Product((edtProductName.getText().toString().replaceAll("\\s+", " ")).trim(), listImageUrl,
//                edtDesc.getText().toString().trim(), edtPrice.getText().toString().trim()
//                , Integer.parseInt(edtQuantity.getText().toString().trim()), spCategory.getSelectedItem().toString(), "", uid);
        Product product = new Product();
        product.setProductName((edtProductName.getText().toString().replaceAll("\\s+", " ")).trim());
        product.setListImageUrl(listImageUrl);
        product.setDesc(edtDesc.getText().toString().trim());
        product.setPrice(edtPrice.getText().toString().trim());
        product.setQuantity(Integer.parseInt(edtQuantity.getText().toString().trim()));
        product.setCategoryName(spCategory.getSelectedItem().toString());
        product.setUid(uid);
        product.setDisCount(Integer.parseInt(edtDisCount.getText().toString().trim()));

        firestore.collection("products").add(product).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                product.setProductId(documentReference.getId());
                firestore.collection("products").document(product.getProductId())
                        .set(product, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(AddProduct.this, "Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AddProduct.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddProduct.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        uriArrayList.clear();
    }

}