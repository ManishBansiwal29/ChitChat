package com.manish.chitchat;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
public class ChatFragment extends Fragment {

    private View privateChatsView;
    private RecyclerView chatsListRec;
    private FirebaseAuth mAuth;
    private DatabaseReference chatsRef,usersRef;

    private String currentUserId;





    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater , ViewGroup container ,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       privateChatsView = inflater.inflate(R.layout.fragment_chat , container , false);
       chatsListRec = privateChatsView.findViewById(R.id.chats_list_fragment);
       chatsListRec.setLayoutManager(new LinearLayoutManager(getContext()));

       mAuth=FirebaseAuth.getInstance();
       currentUserId=mAuth.getCurrentUser().getUid();
       usersRef=FirebaseDatabase.getInstance().getReference().child("Users");

       chatsRef= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);


       return privateChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatsRef,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ChatsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder , int position , @NonNull Contacts model) {

                final String usersIds = getRef(position).getKey();
                final String[] retImage = {"default_image"};
                usersRef.child(usersIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       if (dataSnapshot.exists())
                       {
                           if (dataSnapshot.hasChild("image")){
                               retImage[0] = dataSnapshot.child("image").getValue().toString();

                               Picasso.get().load(retImage[0]).placeholder(R.drawable.profile_image).into(holder.profileImage);
                           }

                           final String retName = dataSnapshot.child("name").getValue().toString();
                           String retStatus = dataSnapshot.child("status").getValue().toString();

                           holder.userName.setText(retName);

                           if (dataSnapshot.child("userState").hasChild("state")){
                               String state = dataSnapshot.child("userState").child("state").getValue().toString();

                               String date = dataSnapshot.child("userState").child("date").getValue().toString();

                               String time = dataSnapshot.child("userState").child("time").getValue().toString();

                               if (state.equals("online")){
                                   holder.userStatus.setText("online");
                               }
                               else if (state.equals("offline")){
                                   holder.userStatus.setText("Last seen: " + date+ "  "+ time);

                               }

                           }
                           else{
                               holder.userStatus.setText("offline");
                           }



                           holder.itemView.setOnClickListener(new View.OnClickListener() {
                               @Override
                               public void onClick(View v)
                               {
                                   Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                   chatIntent.putExtra("Visit_user_id",usersIds);
                                   chatIntent.putExtra("Visit_user_name",retName);
                                   chatIntent.putExtra("Visit_user_image", retImage[0]);

                                   startActivity(chatIntent);
                               }
                           });
                       }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent , int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout,parent,false);
                return new ChatsViewHolder(view);
            }
        };
        chatsListRec.setAdapter(adapter);
        adapter.startListening();
    }

    private static class ChatsViewHolder extends RecyclerView.ViewHolder {

        private TextView userName,userStatus;
        private CircleImageView profileImage;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage=itemView.findViewById(R.id.users_profile_image);
            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
        }
    }
}
