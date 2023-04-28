package com.google.firebase.udacity.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.udacity.whatsapp.Adapter.ChatAdapter;
import com.google.firebase.udacity.whatsapp.Models.MessageModel;
import com.google.firebase.udacity.whatsapp.databinding.ActivityChatsDetailsBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class ChatsDetailsActivity extends AppCompatActivity {
    ActivityChatsDetailsBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        binding=ActivityChatsDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();



       final String senderId =auth.getUid();
        String recieverId=getIntent().getStringExtra("userId");
        String userName=getIntent().getStringExtra("userName");
        String profilePic=getIntent().getStringExtra("profilePic");
        binding.userName.setText(userName);
        Picasso.get().load(profilePic).placeholder(R.drawable.ic_user).into(binding.profileImage);



        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(ChatsDetailsActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        final ArrayList<MessageModel>messageModels=new ArrayList<>();
        final ChatAdapter chatAdapter= new ChatAdapter(messageModels,this,recieverId);
        binding.chatsRecyclerVierw.setAdapter(chatAdapter);


        LinearLayoutManager layoutManager= new LinearLayoutManager(this);
        binding.chatsRecyclerVierw.setLayoutManager(layoutManager);



        final String senderRoom = senderId+recieverId;
        final String recevierRoom = recieverId+senderId;

        database.getReference().child("chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageModels.clear();
                        for (DataSnapshot snapshot1: snapshot.getChildren()){
                            MessageModel model = snapshot1.getValue(MessageModel.class);
                            model.setMessageId(snapshot1.getKey());
                            messageModels.add(model);

                        }
                        chatAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (binding.etMessage.getText().toString().isEmpty()){
                    binding.etMessage.setError("Enter your message ");
                    return;
                }

             String message =    binding.etMessage.getText().toString();
             final  MessageModel model = new MessageModel(senderId,message);
             model.setTimeStamp(new Date().getTime());
             binding.etMessage.setText("");
             database.getReference().child("chats")
                     .child(senderRoom)
                     .push()
                     .setValue(model)
                     .addOnSuccessListener(new OnSuccessListener<Void>() {
                         @Override
                         public void onSuccess(Void unused) {
                             database.getReference().child("chats")
                                     .child(recevierRoom)
                                     .push()
                                     .setValue(model)
                                     .addOnSuccessListener(new OnSuccessListener<Void>() {
                                         @Override
                                         public void onSuccess(Void unused) {

                                         }
                                     });

                         }
                     });

            }
        });


    }
}