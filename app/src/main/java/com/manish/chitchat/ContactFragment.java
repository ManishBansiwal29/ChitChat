package com.manish.chitchat;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
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
public class ContactFragment extends Fragment {
    private View contactsView;
    private RecyclerView myContactsList;
    private DatabaseReference contactsRef,usersRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    public ContactFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater , ViewGroup container ,
                             Bundle savedInstanceState) {

        contactsView = inflater.inflate(R.layout.fragment_contact , container , false);

        myContactsList= contactsView.findViewById(R.id.contacts_list_recycler);
        myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        contactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        usersRef=FirebaseDatabase.getInstance().getReference().child("Users");

        return contactsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef,Contacts.class)
                .build();

        final FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder , int position , @NonNull Contacts model) {

                String usersId = getRef(position).getKey();
                usersRef.child(usersId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                   if (dataSnapshot.exists()){

                       if (dataSnapshot.child("userState").hasChild("state")){
                           String state = dataSnapshot.child("userState").child("state").getValue().toString();

                           String date = dataSnapshot.child("userState").child("date").getValue().toString();

                           String time = dataSnapshot.child("userState").child("time").getValue().toString();

                           if (state.equals("online")){
                               holder.onlineIcon.setVisibility(View.VISIBLE);
                           }
                           else if (state.equals("offline")){
                               holder.onlineIcon.setVisibility(View.INVISIBLE);

                           }

                       }
                       else{
                           holder.onlineIcon.setVisibility(View.INVISIBLE);

                       }


                       if (dataSnapshot.hasChild("image")){
                           String profileImage = dataSnapshot.child("image").getValue().toString();
                           String profileName = dataSnapshot.child("name").getValue().toString();
                           String profileStatus = dataSnapshot.child("status").getValue().toString();

                           holder.username.setText(profileName);
                           holder.userstatus.setText(profileStatus);
                           Picasso.get().load(profileImage).placeholder(R.drawable.profile_image).into(holder.userImage);
                       }else{
                           String profileName = dataSnapshot.child("name").getValue().toString();
                           String profileStatus = dataSnapshot.child("status").getValue().toString();

                           holder.username.setText(profileName);
                           holder.userstatus.setText(profileStatus);
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
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup , int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_display_layout,viewGroup,false);
                ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder;
            }
        };
        myContactsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder {

        private TextView username,userstatus;
        private ImageView onlineIcon;
        private CircleImageView userImage;
        public ContactsViewHolder(@NonNull View itemView) {

            super(itemView);
            username=itemView.findViewById(R.id.user_profile_name);
            userstatus=itemView.findViewById(R.id.user_status);
            userImage=itemView.findViewById(R.id.users_profile_image);

            onlineIcon=itemView.findViewById(R.id.user_online_status);
        }
    }

}
