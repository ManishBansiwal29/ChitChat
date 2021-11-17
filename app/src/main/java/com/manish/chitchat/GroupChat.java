package com.manish.chitchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChat extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton sendMessageBtn;
    private EditText userInputMessage;
    private ScrollView mScrollView;
    private TextView displayTextMessages;

    private String currentGroupName,currentUserId,currentUserName,currentDate,currentTime;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef,groupRef,groupMessageRefKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();

        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();

        usersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        groupRef=FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);

        InitializeFields();

        GetUserInfo();

        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessageInfoToDatabase();
                userInputMessage.setText("");
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        groupRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot , @Nullable String s) {
                if (dataSnapshot.exists())
                {
                    DisplayMessage(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot , @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot , @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void InitializeFields() {
        mToolbar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);
        sendMessageBtn=findViewById(R.id.send_message_btn);
        userInputMessage=findViewById(R.id.input_group_message);
        mScrollView=findViewById(R.id.myScrollView);
        displayTextMessages=findViewById(R.id.groupchat_text_display);
    }

    private void GetUserInfo()
    {
        usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void SendMessageInfoToDatabase()
    {
        String message = userInputMessage.getText().toString();
        String messageKey = groupRef.push().getKey();
        if (TextUtils.isEmpty(message))
        {
            Toast.makeText(this , "message is empty" , Toast.LENGTH_SHORT).show();
        }else
            {
                Calendar calForDate = Calendar.getInstance();
                SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
                currentDate = currentDateFormat.format(calForDate.getTime());

                Calendar calForTime = Calendar.getInstance();
                SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
                currentTime = currentTimeFormat.format(calForTime.getTime());


                HashMap<String, Object> groupMessageKey = new HashMap<>();
                groupRef.updateChildren(groupMessageKey);
                groupMessageRefKey=groupRef.child(messageKey);

                HashMap<String,Object> groupMessagaMap = new HashMap<>();
                groupMessagaMap.put("name",currentUserName);
                groupMessagaMap.put("message",message);
                groupMessagaMap.put("Date",currentDate);
                groupMessagaMap.put("Time",currentTime);
                groupMessageRefKey.updateChildren(groupMessagaMap);

            }
    }

    private void DisplayMessage(DataSnapshot dataSnapshot)
    {
        Iterator iterator = dataSnapshot.getChildren().iterator();

        while (iterator.hasNext())
        {
         String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
         String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();
         String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
         String chatName = (String) ((DataSnapshot)iterator.next()).getValue();

         displayTextMessages.append(chatName + " :\n" + chatMessage +"\n"+chatTime+"   "+ chatDate+"\n\n\n");

         mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

        }
    }

}
