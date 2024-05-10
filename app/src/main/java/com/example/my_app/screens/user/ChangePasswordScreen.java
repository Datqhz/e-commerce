package com.example.my_app.screens.user;

import static com.google.firebase.appcheck.internal.util.Logger.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.my_app.R;
import com.example.my_app.screens.authenticate.LoginScreen;
import com.example.my_app.shared.GlobalVariable;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordScreen extends AppCompatActivity {

    ImageButton btnPrevious;
    Button btnSave;
    EditText edtOldPass, edtNewPass, edtRetype;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password_screen);
        setControl();
        setEvent();
    }

    private void setControl(){
        btnPrevious = findViewById(R.id.ChangePassword_btnPrevious);
        btnSave = findViewById(R.id.ChangePassword_btnSave);
        edtOldPass = findViewById(R.id.ChangePassword_edtOldPass);
        edtNewPass = findViewById(R.id.ChangePassword_edtNewPass);
        edtRetype = findViewById(R.id.ChangePassword_edtRetype);
    }
    private void setEvent(){
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean flag = true;
                if(edtOldPass.getText().toString().trim().equals("")){
                    edtOldPass.setError("Vui lòng nhập mật khẩu cũ!");
                    edtOldPass.setFocusable(true);
                    flag = false;
                }else {
                    if(edtOldPass.getText().toString().trim().length() < 6 ){
                        edtOldPass.setError("Độ dài mật khẩu từ 6 trở lên!");
                        edtOldPass.setFocusable(true);
                        flag = false;
                    }
                }
                if(edtNewPass.getText().toString().trim().equals("")){
                    edtNewPass.setError("Vui lòng nhập khẩu mới!");
                    edtNewPass.setFocusable(true);
                    flag = false;
                }else {
                    if(edtNewPass.getText().toString().trim().length() < 6 ){
                        edtNewPass.setError("Độ dài mật khẩu từ 6 trở lên!");
                        edtNewPass.setFocusable(true);
                        flag = false;
                    }
                }
                if(edtRetype.getText().toString().trim().equals("")){
                    edtRetype.setError("Vui lòng nhập lại mật khẩu mới!");
                    edtRetype.setFocusable(true);
                    flag = false;
                }else{
                    if(!edtNewPass.getText().toString().trim().equals(edtRetype.getText().toString().trim())){
                        edtRetype.setError("Mật khẩu nhập lại không khớp!");
                        edtRetype.setFocusable(true);
                        flag = false;
                    }
                }
                if(flag){
                    changePassword();
                }
            }
        });
    }

    public void changePassword(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider
                .getCredential(GlobalVariable.userInfo.getEmail(), edtOldPass.getText().toString().trim());

        user.reauthenticate(credential).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                user.updatePassword(edtNewPass.getText().toString().trim())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    finish();
                                    Toast.makeText(getApplicationContext(),"Cập nhật mật khẩu thành công", Toast.LENGTH_LONG).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(),"Cập nhật mật khẩu thất bại", Toast.LENGTH_LONG).show();
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        edtOldPass.setText("");
                        edtOldPass.setError("Mật khẩu cũ không đúng.");
                        edtOldPass.setFocusable(true);
                    }
                });
    }
}