package com.manish.chitchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserId,current_state,senderUserId;
    private CircleImageView userProfileImage;
    private TextView profileUsername,profileUserstatus;
    private Button sendMessageRequestButton,declineMessageRequestButton;
    private DatabaseReference userRef,chatRequestRef,contactsRef,notificationRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth=FirebaseAuth.getInstance();
        senderUserId=mAuth.getCurrentUser().getUid();

        userRef= FirebaseDatabase.getInstance().getReference().child("Users");

        chatRequestRef=FirebaseDatabase.getInstance().getReference().child("Chat Requests");

        contactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");

        notificationRef=FirebaseDatabase.getInstance().getReference().child("Notifications");

        receiverUserId= getIntent().getExtras().get("visited_user_id").toString();
        Toast.makeText(this , receiverUserId , Toast.LENGTH_SHORT).show();

        userProfileImage=findViewById(R.id.visited_profile_image);
        profileUserstatus=findViewById(R.id.visited_profile_status);
        profileUsername=findViewById(R.id.visited_profile_name);
        sendMessageRequestButton=findViewById(R.id.send_message_request_btn);
        declineMessageRequestButton=findViewById(R.id.decline_message_request_btn);

        current_state="new";

        RetrieveUserInfo();

    }

    private void RetrieveUserInfo() {

        userRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && dataSnapshot.hasChild("image")) {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    profileUsername.setText(userName);
                    profileUserstatus.setText(userStatus);

                    ManageChatRequest();
                } else{
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    profileUsername.setText(userName);
                    profileUserstatus.setText(userStatus);

                   ManageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void ManageChatRequest()
    {

        chatRequestRef.child(senderUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(receiverUserId)){
                            String request_type = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();
                            if (request_type.equals("sent")){
                                current_state="request_sent";
                                sendMessageRequestButton.setText("Cancel Request");
                            }
                            else if (request_type.equals("recieved")){
                                current_state="request_recieved";
                                sendMessageRequestButton.setText("Accept Request");

                                declineMessageRequestButton.setVisibility(View.VISIBLE);
                                declineMessageRequestButton.setEnabled(true);

                                declineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CancelChatRequest();
                                    }
                                });
                            }
                        }
                        else{
                            contactsRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(receiverUserId)){
                                                current_state = "friends";
                                                sendMessageRequestButton.setText("Remove this user");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        if (!senderUserId.equals(receiverUserId))
        {
            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessageRequestButton.setEnabled(false);
                    if (current_state.equals("new")){
                        SendChatRequest();
                    }
                    if (current_state.equals("request_sent")){
                        CancelChatRequest();
                    }
                    if (current_state.equals("request_recieved")){
                        AcceptChatRequest();
                    }
                    if (current_state.equals("friends")){
                        RemoveSpecificContact();
                    }
                }
            });
        }else
            {
                sendMessageRequestButton.setVisibility(View.INVISIBLE);
            }
    }



    private void SendChatRequest()
    {
        chatRequestRef.child(senderUserId).child(receiverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            chatRequestRef.child(receiverUserId).child(senderUserId)
                                    .child("request_type").setValue("recieved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){

                                                HashMap<String,String> chatNotification = new HashMap<>();
                                                chatNotification.put("from",senderUserId);
                                                chatNotification.put("type","request");

                                                notificationRef.child(receiverUserId).push()
                                                        .setValue(chatNotification)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                           if (task.isSuccessful()){
                                                               sendMessageRequestButton.setEnabled(true);
                                                               current_state="request_sent";
                                                               sendMessageRequestButton.setText("Cancel Request");
                                                           }
                                                            }
                                                        });


                                            }
                                        }
                                    });

                        }
                    }
                });
    }

    private void CancelChatRequest() {
        chatRequestRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            chatRequestRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                sendMessageRequestButton.setEnabled(true);
                                                current_state="new";
                                                sendMessageRequestButton.setText("Send Request");

                                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                declineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }

                    }
                });
    }

    private void AcceptChatRequest()
    {
        contactsRef.child(senderUserId).child(receiverUserId)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            contactsRef.child(receiverUserId).child(senderUserId)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                chatRequestRef.child(senderUserId).child(receiverUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    chatRequestRef.child(receiverUserId).child(senderUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    sendMessageRequestButton.setEnabled(true);
                                                                                    current_state="friends";
                                                                                    sendMessageRequestButton.setText("Remove this user");

                                                                                    declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                                    declineMessageRequestButton.setEnabled(false);
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void RemoveSpecificContact()
    {
       contactsRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            contactsRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                sendMessageRequestButton.setEnabled(true);
                                                current_state="new";
                                                sendMessageRequestButton.setText("Send Request");

                                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                declineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }

                    }
                });
    }

}
