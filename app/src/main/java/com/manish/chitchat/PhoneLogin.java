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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import org.w3c.dom.Text;

import java.util.concurrent.TimeUnit;

public class PhoneLogin extends AppCompatActivity {
//    private Button sendVerificationBtn, verifyButton;
//    private EditText inputPhoneNumber, inputVerificationCode;
//
//    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
//
//    private String mVerificationId;
//    private PhoneAuthProvider.ForceResendingToken mResendToken;
//    private FirebaseAuth mAuth;
//    private ProgressDialog loadingBar;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_phone_login);
//
//        mAuth = FirebaseAuth.getInstance();
//
//        sendVerificationBtn = findViewById(R.id.send_verification_code);
//        verifyButton = findViewById(R.id.verify_btn);
//        inputPhoneNumber = findViewById(R.id.phone_number_input);
//        inputVerificationCode = findViewById(R.id.phone_verification_input);
//        loadingBar = new ProgressDialog(PhoneLogin.this);
//
//        sendVerificationBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                String phoneNumber = inputPhoneNumber.getText().toString();
//                if (TextUtils.isEmpty(phoneNumber)) {
//                    Toast.makeText(PhoneLogin.this , "Enter Your Phone Number..." , Toast.LENGTH_LONG).show();
//                } else {
//                    loadingBar.setTitle("Phone Verification");
//                    loadingBar.setMessage("Please Wait WhileWe Are Authenticating");
//                    loadingBar.setCanceledOnTouchOutside(false);
//                    loadingBar.show();
//                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
//                            phoneNumber ,        // Phone number to verify
//                            300 ,                 // Timeout duration
//                            TimeUnit.SECONDS ,   // Unit of timeout
//                            PhoneLogin.this ,               // Activity (for callback binding)
//                            callbacks);        // OnVerificationStateChangedCallbacks
//                }
//            }
//        });
//
//        verifyButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                sendVerificationBtn.setVisibility(View.INVISIBLE);
//                inputPhoneNumber.setVisibility(View.INVISIBLE);
//
//                String verificationCode = inputVerificationCode.getText().toString();
//
//                if (TextUtils.isEmpty(verificationCode)){
//                    Toast.makeText(PhoneLogin.this , "Please Write Code First..." , Toast.LENGTH_SHORT).show();
//                }else
//                {
//                    loadingBar.setTitle("Code Verification");
//                    loadingBar.setMessage("Please Wait While We Are Verifying");
//                    loadingBar.setCanceledOnTouchOutside(false);
//                    loadingBar.show();
//
//                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
//                    signInWithPhoneAuthCredential(credential);
//
//                }
//            }
//        });
//
//        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//            @Override
//            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
//                signInWithPhoneAuthCredential(phoneAuthCredential);
//            }
//
//            @Override
//            public void onVerificationFailed(@NonNull FirebaseException e) {
//                loadingBar.dismiss();
//                Toast.makeText(PhoneLogin.this , "Please Enter with Your County Code" , Toast.LENGTH_LONG).show();
//
//                sendVerificationBtn.setVisibility(View.VISIBLE);
//                inputPhoneNumber.setVisibility(View.VISIBLE);
//
//                verifyButton.setVisibility(View.INVISIBLE);
//                inputVerificationCode.setVisibility(View.INVISIBLE);
//            }
//
//            @Override
//            public void onCodeSent(@NonNull String verificationId ,
//                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
//
//                Toast.makeText(PhoneLogin.this , "Code Sent" , Toast.LENGTH_SHORT).show();
//                sendVerificationBtn.setVisibility(View.INVISIBLE);
//                inputPhoneNumber.setVisibility(View.INVISIBLE);
//
//                verifyButton.setVisibility(View.VISIBLE);
//                inputVerificationCode.setVisibility(View.VISIBLE);
//
//                mVerificationId = verificationId;
//                mResendToken = token;
//
//                loadingBar.dismiss();
//
//
//
//
//            }
//        };
//    }
//
//
//    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            loadingBar.dismiss();
//                            Toast.makeText(PhoneLogin.this , "Congratulations You Are Logged in Succesfully" , Toast.LENGTH_SHORT).show();
//                            SendUserToMainActivity();
//                        } else {
//                            String error = task.getException().toString();
//                            Toast.makeText(PhoneLogin.this , "Error :" + error , Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//    }
//
//    private void SendUserToMainActivity() {
//        Intent intent = new Intent(PhoneLogin.this, MainActivity.class);
//        startActivity(intent);
//    }
//
//}


    private EditText phone_Number_Input, verification_code_input;
    private Button sendVerificationCodeBtn, verifyButton;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private FirebaseAuth mAuth;

    private ProgressDialog loadingBar;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);


        mAuth = FirebaseAuth.getInstance();


        phone_Number_Input = findViewById(R.id.phone_number_input);
        verification_code_input =findViewById(R.id.phone_verification_input);
        sendVerificationCodeBtn =  findViewById(R.id.send_verification_code);
        verifyButton =  findViewById(R.id.verify_btn);
        loadingBar = new ProgressDialog(this);


        sendVerificationCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String phoneNumber = phone_Number_Input.getText().toString();

                if (TextUtils.isEmpty(phoneNumber))
                {
                    Toast.makeText(PhoneLogin.this, "Please enter your phone number first...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("Please wait, while we are authenticating using your phone...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();


                    PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, PhoneLogin.this, callbacks);
                }
            }
        });



        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                phone_Number_Input.setVisibility(View.INVISIBLE);
                sendVerificationCodeBtn.setVisibility(View.INVISIBLE);


                String verificationCode =verification_code_input.getText().toString();

                if (TextUtils.isEmpty(verificationCode))
                {
                    Toast.makeText(PhoneLogin.this, "Please write verification code first...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.setTitle("Verification Code");
                    loadingBar.setMessage("Please wait, while we are verifying verification code...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });


        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential)
            {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e)
            {
                Toast.makeText(PhoneLogin.this, "Invalid Phone Number, Please enter correct phone number with your country code...", Toast.LENGTH_LONG).show();
                loadingBar.dismiss();

                phone_Number_Input.setVisibility(View.VISIBLE);
                sendVerificationCodeBtn.setVisibility(View.VISIBLE);

               verification_code_input.setVisibility(View.INVISIBLE);
                verifyButton.setVisibility(View.INVISIBLE);
            }

            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token)
            {
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;


                Toast.makeText(PhoneLogin.this, "Code has been sent, please check and verify...", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();

                phone_Number_Input.setVisibility(View.INVISIBLE);
                sendVerificationCodeBtn.setVisibility(View.INVISIBLE);

               verification_code_input.setVisibility(View.VISIBLE);
                verifyButton.setVisibility(View.VISIBLE);
            }
        };
    }



    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential)
    {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLogin.this, "Congratulations, you're logged in Successfully.", Toast.LENGTH_SHORT).show();
                            SendUserToMainActivity();
                        }
                        else
                        {
                            String message = task.getException().toString();
                            Toast.makeText(PhoneLogin.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
    }




    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(PhoneLogin.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
