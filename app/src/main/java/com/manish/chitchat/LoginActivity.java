package com.manish.chitchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton,phoneButton;
    private EditText userEmail,userPassword;
    private TextView needNewAccountLink,forgetPasswordLink;

    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth= FirebaseAuth.getInstance();

        usersRef= FirebaseDatabase.getInstance().getReference().child("Users");

        InitializeField();

        needNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToRegisterActivity();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUsersToLogin();
            }
        });

        phoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent phoneLoginActivity = new Intent(LoginActivity.this,PhoneLogin.class);
                startActivity(phoneLoginActivity);

            }
        });
    }



    private void InitializeField() {
        loginButton=findViewById(R.id.login_btn);
        phoneButton=findViewById(R.id.phone_login_btn);
        userEmail=findViewById(R.id.login_email);
        userPassword=findViewById(R.id.password_email);
        needNewAccountLink=findViewById(R.id.need_new_account);
        forgetPasswordLink=findViewById(R.id.forget_password_link);
        loadingBar=new ProgressDialog(this);

    }

    private void AllowUsersToLogin() {
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please Enter a Email",Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please Enter a Password",Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Sign In");
            loadingBar.setMessage("Please wait...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){

                        String currentUserId = mAuth.getCurrentUser().getUid();
                        String deviceToken = FirebaseInstanceId.getInstance().getToken();

                        usersRef.child(currentUserId).child("device_token")
                                .setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    SendUserToMainActivity();
                                    Toast.makeText(LoginActivity.this,"Logged in successfully",Toast.LENGTH_SHORT);
                                    loadingBar.dismiss();
                                }
                            }
                        });


                    }else{
                        String error = task.getException().toString();
                        Toast.makeText(LoginActivity.this,"Error "+error,Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    }
                }
            });

        }
    }


    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


    public void SendUserToRegisterActivity(){
        Intent MainIntent = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(MainIntent);
    }
}
