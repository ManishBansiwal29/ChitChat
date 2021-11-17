package com.manish.chitchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private String messageRecieverId,messageRecieverName,messageRecieverImage;

    private TextView username, userLastseen;
    private CircleImageView userImage;
    private ImageButton privateMessageBtn;
    private EditText messageInputText;

    private String messageSenderId;

    private Toolbar chatToolbar;

    private DatabaseReference rootRef;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth=FirebaseAuth.getInstance();
        messageSenderId=mAuth.getCurrentUser().getUid();
        rootRef= FirebaseDatabase.getInstance().getReference();

        messageRecieverId=getIntent().getExtras().get("Visit_user_id").toString();
        messageRecieverName=getIntent().getExtras().get("Visit_user_name").toString();
        messageRecieverImage=getIntent().getExtras().get("Visit_user_image").toString();

        InitializeControllers();

        username.setText(messageRecieverName);
        Picasso.get().load(messageRecieverImage).placeholder(R.drawable.profile_image).into(userImage);

        privateMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

    }

    private void InitializeControllers() {



        chatToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);


        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView  = layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);

        username=findViewById(R.id.custom_profile_name);
        userImage=findViewById(R.id.custom_profile_image);
        userLastseen=findViewById(R.id.custom_user_lastseen);
        privateMessageBtn=findViewById(R.id.send_private_msg_btn);
        messageInputText=findViewById(R.id.input_msg);

        messageAdapter=new MessageAdapter(messagesList);
        userMessagesList=findViewById(R.id.private_msg_list);
        linearLayoutManager=new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

    }

    private void DisplayLastSeen(){
        rootRef.child("Users").child(messageSenderId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child("userState").hasChild("state")){
                    String state = dataSnapshot.child("userState").child("state").getValue().toString();

                    String date = dataSnapshot.child("userState").child("date").getValue().toString();

                    String time = dataSnapshot.child("userState").child("time").getValue().toString();

                    if (state.equals("online")){
                        userLastseen.setText("online");
                    }
                    else if (state.equals("offline")){
                        userLastseen.setText("Last seen: " + date+ "  "+ time);

                    }

                }
                else{
                    userLastseen.setText("offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        rootRef.child("Messages").child(messageSenderId).child(messageRecieverId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot , @Nullable String s) {

                Messages messages = dataSnapshot.getValue(Messages.class);
                messagesList.add(messages);

                messageAdapter.notifyDataSetChanged();

                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
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

    private void sendMessage()
    {
        String messageText =messageInputText.getText().toString();
        if (TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this , "Please Write a message..." , Toast.LENGTH_SHORT).show();
        }else
            {
                String messageSenderRef = "Messages/" + messageSenderId + "/" + messageRecieverId;
                String messageRecieverRef = "Messages/" + messageRecieverId + "/" + messageSenderId;


                DatabaseReference userMessageKeyRef = rootRef.child("Messages").child(messageSenderId).child(messageRecieverId).push();

                String messagePushId =userMessageKeyRef.getKey();

                Map messageTextBody = new HashMap();
                messageTextBody.put("message",messageText);
                messageTextBody.put("type","text");
                messageTextBody.put("from",messageSenderId);

                Map messageBodyDetails = new HashMap();
                messageBodyDetails.put(messageSenderRef + "/" +messagePushId,messageTextBody);
                messageBodyDetails.put(messageRecieverRef+"/"+messagePushId,messageTextBody);
                rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                   if (task.isSuccessful()){
                       Toast.makeText(ChatActivity.this , "sent" , Toast.LENGTH_SHORT).show();
                   }else{
                       Toast.makeText(ChatActivity.this , "error" , Toast.LENGTH_SHORT).show();

                   }
                   messageInputText.setText("");
                    }
                });

            }
    }
}
