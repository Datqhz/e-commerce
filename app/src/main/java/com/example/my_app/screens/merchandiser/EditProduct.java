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
import com.example.my_app.shared.GlobalVariable;
import com.example.my_app.view_adapter.ProductAdapterImage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class EditProduct extends AppCompatActivity {
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;
    private StorageReference storageReference;
    private Uri imageUri;
    private ArrayList<Uri> uriArrayList;
    private Bitmap bitmap;
    private Button btnSave, btnPickImages;
    private RecyclerView recyclerViewImages;
    private ProductAdapterImage recyclerProductImage;
    private ArrayList<String> listImageUrl;
    private ArrayList<String> listImageUrlDelete;
    private EditText edtProductName, edtDesc, edtPrice, edtQuantity, edtDisCount;
    private ArrayList<String> categoryArrayList;
    private ArrayList<String> productNameList;
    private ArrayAdapter<String> adapter;
    private Spinner spCategory;
    private String productId;
    private String uid;
    private int countNew = 0;
    private  boolean checkEdit = true;
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            if(o.getResultCode() == RESULT_OK){
                if(o.getData() != null && o.getData().getClipData() != null){
                    int count = o.getData().getClipData().getItemCount();
                    for(int i = 0; i < count; i++){
                        if(uriArrayList.size() < 8){
                            countNew = countNew + 1;
                            imageUri = o.getData().getClipData().getItemAt(i).getUri();

                            uriArrayList.add(imageUri);
                            checkEdit = false;
                        }else{
                            Toast.makeText(EditProduct.this, "Không thể chọn nhiều hơn 8 hình ảnh", Toast.LENGTH_SHORT).show();
                        }

                    }
                    recyclerProductImage.notifyDataSetChanged();
                } else if (o.getData().getData() != null) {
                    if(uriArrayList.size() < 8){
                        countNew = countNew + 1;
                        //imageUrl = o.getData().getData().getPath();
                        imageUri = o.getData().getData();
                        uriArrayList.add(imageUri);
                        checkEdit = false;
                    }else{
                        Toast.makeText(EditProduct.this, "Không thể chọn nhiều hơn 8 hình ảnh", Toast.LENGTH_SHORT).show();
                    }
                }
                recyclerProductImage.notifyDataSetChanged();
            }
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        setControl();
        getCategoryInfo();
        setEvent();
    }
    private void setControl(){
        btnSave = (Button) findViewById(R.id.btnSave);
        edtProductName = (EditText) findViewById(R.id.edtProductName);
        edtDesc = (EditText) findViewById(R.id.edtDesc);
        edtPrice = (EditText) findViewById(R.id.edtPrice);
        edtQuantity = (EditText) findViewById(R.id.edtQuantity);
        edtDisCount = (EditText) findViewById(R.id.edtDisCount);
        spCategory = (Spinner) findViewById(R.id.spCategory);

        btnPickImages = (Button) findViewById(R.id.btnPickImages);
        recyclerViewImages = (RecyclerView) findViewById(R.id.rvImages);
        uriArrayList = new ArrayList<Uri>();
        listImageUrl = new ArrayList<>();
        listImageUrlDelete = new ArrayList<>();
        recyclerProductImage = new ProductAdapterImage(EditProduct.this,uriArrayList, listImageUrlDelete,true);
        recyclerViewImages.setLayoutManager(new GridLayoutManager(EditProduct.this, 4));
        recyclerViewImages.setAdapter(recyclerProductImage);

        categoryArrayList = new ArrayList<>();
        productNameList = new ArrayList<>();
        adapter = new ArrayAdapter<>(EditProduct.this, android.R.layout.simple_spinner_item, categoryArrayList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
                });
    }
    private void getProductName() {
        firestore.collection("products").whereEqualTo("uid", GlobalVariable.userInfo.getUid())
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
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            edtProductName.setText(bundle.getString("productName"));
            edtDesc.setText(bundle.getString("desc"));
            edtPrice.setText(bundle.getString("price"));
            String tempp = bundle.getString("quantity");
            edtQuantity.setText(tempp);
            edtDisCount.setText(bundle.getString("disCount"));
            String temp = bundle.getString("category");
//                for(int i = 0; i < categoryArrayList.size();i++){
//                    if(categoryArrayList.get(i).equals(temp)){
//                        System.out.println("thành công" + temp);
//                    }
//                }
            spCategory.setSelection(categoryArrayList.indexOf(temp));

            uid = bundle.getString("uid");
            productId = bundle.getString("productId");

            listImageUrl = bundle.getStringArrayList("listImageUrl");

            listImageUrl.forEach(el->{
                try {
                    Uri uri =Uri.parse(el);
                    uriArrayList.add(uri);
                    //System.out.println("arraylisst" + uriArrayList);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
        }

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
                if(checkEdit){
                    if (!listImageUrlDelete.isEmpty()) {
                        for (int i = 0; i < listImageUrlDelete.size(); i++) {
                            listImageUrl.remove(listImageUrlDelete.get(i));
                        }

                        for (int i = 0; i < listImageUrlDelete.size(); i++) {
                            storageReference = storage.getReferenceFromUrl(listImageUrlDelete.get(i));
                            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    //Toast.makeText(v.getContext(), "Xóa hình ảnh thành công", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                    updateProductInfo(listImageUrl);
                }else {
                    editImageToStorage();
                }
            }
        });
    }
    private void uploadCategoryintoProduct(){
        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                parent.getItemIdAtPosition(position);
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
    private void editImageToStorage() {
        String productName = (edtProductName.getText().toString().replaceAll("\\s+", " ")).trim().toLowerCase();
        Boolean canNext = true;
        if(uriArrayList == null ){
            Toast.makeText(this, "Vui lòng chọn hình ảnh sản phẩm", Toast.LENGTH_SHORT).show();
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
        if(canNext) {
            final String randomName = UUID.randomUUID().toString();
            for (int i = uriArrayList.size() - countNew; i < uriArrayList.size(); i++) {
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
                    final StorageReference imageName = referenceImageFolder.child("imageNew" + i + ":" + randomName);
                    imageName.putBytes(bytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageName.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    if (!listImageUrlDelete.isEmpty()) {
                                        for (int i = 0; i < listImageUrlDelete.size(); i++) {
                                            listImageUrl.remove(listImageUrlDelete.get(i));
                                        }

                                        for (int i = 0; i < listImageUrlDelete.size(); i++) {
                                            storageReference = storage.getReferenceFromUrl(listImageUrlDelete.get(i));
                                            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    //Toast.makeText(v.getContext(), "Xóa hình ảnh thành công", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }

                                    listImageUrl.add(uri.toString());

                                    if (listImageUrl.size() == uriArrayList.size()) {
                                        updateProductInfo(listImageUrl);
                                        checkEdit = true;
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
    private void updateProductInfo(ArrayList<String> listImageUrl){
        Map<String, Object> edited = new HashMap<>();
        edited.put("productName", (edtProductName.getText().toString().replaceAll("\\s+", " ")).trim());
        edited.put("desc", edtDesc.getText().toString().trim());
        edited.put("price", edtPrice.getText().toString().trim());
        edited.put("quantity", Integer.parseInt(edtQuantity.getText().toString().trim()));
        edited.put("category", spCategory.getSelectedItem().toString());
        edited.put("listImageUrl", listImageUrl);
        edited.put("productId", productId);
        edited.put("uid", uid);
        edited.put("disCount", Integer.parseInt(edtDisCount.getText().toString().trim()));

        firestore.collection("products").document(productId).update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(EditProduct.this, "Cập nhật sản phẩm thành công", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditProduct.this, "Cập nhật sản phẩm thất bại", Toast.LENGTH_SHORT).show();
            }
        });

    }
}