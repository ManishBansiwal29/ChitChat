package com.manish.chitchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.SuccessContinuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button updateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;

    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    private String currentUserId,downloadImageUrl;

    private static final int GalleryPick = 1;
    private Uri imageUri;

    private String retrievedImage;

    private StorageReference userProfileImagesRef;

    private ProgressDialog loadingBar;
    private Toolbar settingToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Image");

        Initialize();

        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSettings();
            }
        });

        RetrieveUserInformation();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent , GalleryPick);
            }
        });

    }


    private void updateSettings() {

        String setUserName = userName.getText().toString();
        String setUserStatus = userStatus.getText().toString();

        if (TextUtils.isEmpty(setUserName)) {
            Toast.makeText(this , "Please Write Your Username" , Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setUserStatus)) {
            Toast.makeText(this , "Please Write Your Status..." , Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid" , currentUserId);
            profileMap.put("name" , setUserName);
            profileMap.put("status" , setUserStatus);
            rootRef.child("Users").child(currentUserId).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        SendUserToMainActivity();
                        Toast.makeText(SettingsActivity.this , "Profile Updated..." , Toast.LENGTH_SHORT).show();
                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(SettingsActivity.this , "Error :" + message , Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    private void RetrieveUserInformation() {
        rootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && dataSnapshot.hasChild("name") && dataSnapshot.hasChild("image")) {
                    String retrievedUsername = dataSnapshot.child("name").getValue().toString();
                    String retrievedStatus = dataSnapshot.child("status").getValue().toString();
                    retrievedImage = dataSnapshot.child("image").getValue().toString();

                    userName.setText(retrievedUsername);
                    userStatus.setText(retrievedStatus);
                    Picasso.get().load(retrievedImage).into(userProfileImage);
                } else if ((dataSnapshot.exists()) && dataSnapshot.hasChild("name")) {
                    String retrievedUsername = dataSnapshot.child("name").getValue().toString();
                    String retrievedStatus = dataSnapshot.child("status").getValue().toString();

                    userName.setText(retrievedUsername);
                    userStatus.setText(retrievedStatus);
                } else {
                    Toast.makeText(SettingsActivity.this , "Please Update Your Name & Status.." , Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this , MainActivity.class);
        // mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        //finish();
    }

    private void Initialize() {
        updateAccountSettings = findViewById(R.id.update_settings_btn);
        userName = findViewById(R.id.set_user_name);
        userStatus = findViewById(R.id.set_profile_status);
        userProfileImage = findViewById(R.id.set_profile_image);
        loadingBar = new ProgressDialog(this);
        settingToolbar= findViewById(R.id.settings_toolbar);
        setSupportActionBar(settingToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");
    }

    @Override
    protected void onActivityResult(int requestCode , int resultCode , @Nullable Intent data) {
        super.onActivityResult(requestCode , resultCode , data);

        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1 , 1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait while we are updating your profile image");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();
                Uri resultUri = result.getUri();
                final StorageReference filePath = userProfileImagesRef.child(currentUserId + ".jpg");
                final UploadTask uploadTask = filePath.putFile(resultUri);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String error = e.toString();
                        Toast.makeText(SettingsActivity.this , error , Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(SettingsActivity.this , "Updated Succesfully" , Toast.LENGTH_SHORT).show();
                        Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()){
                                    throw task.getException();

                                }
                                loadingBar.dismiss();
                                downloadImageUrl = filePath.getDownloadUrl().toString();
                                return filePath.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()){
                                    downloadImageUrl=task.getResult().toString();
                                   // Toast.makeText(SettingsActivity.this , "Image saved to Database succesfully" , Toast.LENGTH_SHORT).show();
                                    rootRef.child("Users").child(currentUserId).child("image")
                                            .setValue(downloadImageUrl);
                                    loadingBar.dismiss();
                                }else{
                                    String error=task.getException().toString();
                                    Toast.makeText(SettingsActivity.this , error , Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });


            }
        }
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        Intent newMain = new Intent(SettingsActivity.this,MainActivity.class);
//        startActivity(newMain);
//    }
}

