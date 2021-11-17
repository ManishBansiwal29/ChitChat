package com.manish.chitchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {

    private Button createAnAccountButton;
    private EditText userEmail,userPassword;
    private TextView alreadyHaveAnAccount;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference rootReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        rootReference= FirebaseDatabase.getInstance().getReference();

        InitializeField();

        alreadyHaveAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLoginActivity();
            }
        });

        createAnAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateAnAccount();
            }
        });
    }

    private void CreateAnAccount() {
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please Enter a Email",Toast.LENGTH_SHORT).show();

        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please Enter a Password",Toast.LENGTH_SHORT).show();

        }
        else{
            loadingBar.setTitle("Creating new Account");
            loadingBar.setMessage("Please wait while we are your account");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){

                                String deviceToken = FirebaseInstanceId.getInstance().getToken();


                                String currentUserId = mAuth.getCurrentUser().getUid();
                                rootReference.child("Users").child(currentUserId).setValue("");

                                rootReference.child("Users").child(currentUserId).child("device_token")
                                        .setValue(deviceToken);

                                SendUserToMainActivity();
                                Toast.makeText(RegisterActivity.this,"Account Created Succesfully",Toast.LENGTH_SHORT).show();
                                loadingBar.show();
                            }else{
                                String error = task.getException().toString();
                                Toast.makeText(RegisterActivity.this,"Error "+error,Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }

    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(loginIntent);
    }

    private void InitializeField() {
        createAnAccountButton=findViewById(R.id.register_btn);
        userEmail=findViewById(R.id.register_email);
        userPassword=findViewById(R.id.register_password_email);
        alreadyHaveAnAccount=findViewById(R.id.already_have_a_account_link);
        loadingBar=new ProgressDialog(this);
    }
}
