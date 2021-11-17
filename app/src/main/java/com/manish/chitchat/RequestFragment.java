package com.manish.chitchat;


import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private View requestFragmentView;
    private RecyclerView myRequestList;

    private DatabaseReference chatRequestsRef,usersRef,contactsRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater , ViewGroup container ,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestFragmentView= inflater.inflate(R.layout.fragment_request , container , false);
        myRequestList=requestFragmentView.findViewById(R.id.chat_request_list);
        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();

        chatRequestsRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        usersRef=FirebaseDatabase.getInstance().getReference().child("Users");
        contactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");

        return requestFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatRequestsRef.child(currentUserId),Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,RequestViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder holder , int position , @NonNull Contacts model) {
                holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);

                final String listUserId=getRef(position).getKey();
                DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();
                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String type = dataSnapshot.getValue().toString();
                            if (type.equals("recieved")){
                                usersRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.hasChild("image")){

                                            final String requestUserimage = dataSnapshot.child("image").getValue().toString();

                                            Picasso.get().load(requestUserimage).placeholder(R.drawable.profile_image).into(holder.userImage);
                                        }
                                            final String requestUsername = dataSnapshot.child("name").getValue().toString();
                                            final String requestUserstatus = dataSnapshot.child("status").getValue().toString();

                                            holder.userName.setText(requestUsername);
                                            holder.userStatus.setText("wants to connects with you.");

                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence options[] = new CharSequence[]
                                                        {
                                                                "Accept",
                                                                "Cancel"
                                                        };
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle(requestUsername+"'s Chat Request");

                                                builder.setItems(options , new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog , int i) {
                                                        if (i==0){
                                                            contactsRef.child(currentUserId).child(listUserId).child("Contacts")
                                                                    .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){
                                                                        contactsRef.child(listUserId).child(currentUserId).child("Contacts")
                                                                                .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                chatRequestsRef.child(currentUserId).child(listUserId)
                                                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()){
                                                                                            chatRequestsRef.child(listUserId).child(currentUserId)
                                                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    if (task.isSuccessful()){
                                                                                                        Toast.makeText( getContext(), "New Contact Added" , Toast.LENGTH_SHORT).show();
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                        }
                                                                                    }
                                                                                });
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                        }
                                                        if (i==1){
                                                            contactsRef.child(listUserId).child(currentUserId).child("Contacts")
                                                                    .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    chatRequestsRef.child(currentUserId).child(listUserId)
                                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()){
                                                                                chatRequestsRef.child(listUserId).child(currentUserId)
                                                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()){
                                                                                            Toast.makeText( getContext(), "Request Deleted" , Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    }
                                                                                });
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                                builder.show();
                                            }

                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                            else if(type.equals("sent")){
                                  final Button request_sent_btn = holder.itemView.findViewById(R.id.request_accept_btn);
                                  request_sent_btn.setText("Request Sent");

                                  holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.INVISIBLE);


                                usersRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.hasChild("image")){

                                            final String requestUserimage = dataSnapshot.child("image").getValue().toString();

                                            Picasso.get().load(requestUserimage).placeholder(R.drawable.profile_image).into(holder.userImage);
                                        }
                                        final String requestUsername = dataSnapshot.child("name").getValue().toString();
                                        final String requestUserstatus = dataSnapshot.child("status").getValue().toString();

                                        holder.userName.setText(requestUsername);
                                        holder.userStatus.setText(" you have sent request to " + requestUsername);

                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence options[] = new CharSequence[]
                                                        {
                                                                "Cancel Chat Request"
                                                        };
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("Already Sent Request");

                                                builder.setItems(options , new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog , int i) {
                                                        if (i==0){
                                                            contactsRef.child(listUserId).child(currentUserId).child("Contacts")
                                                                    .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    chatRequestsRef.child(currentUserId).child(listUserId)
                                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()){
                                                                                chatRequestsRef.child(listUserId).child(currentUserId)
                                                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()){
                                                                                            Toast.makeText( getContext(), "You have canceled the request" , Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    }
                                                                                });
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                                builder.show();
                                            }

                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup , int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_display_layout,viewGroup,false);
                RequestViewHolder holder = new RequestViewHolder(view);
                return holder;
            }
        };
        myRequestList.setAdapter(adapter);
        adapter.startListening();

    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        private TextView userName,userStatus;
        private CircleImageView userImage;
        private Button acceptBtn,cancelBtn;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
            userImage=itemView.findViewById(R.id.users_profile_image);

            acceptBtn=itemView.findViewById(R.id.request_accept_btn);
            cancelBtn=itemView.findViewById(R.id.request_cancel_btn);
        }
    }
}
