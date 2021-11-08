package com.belousov.whatschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ChatFragment extends AppCompatActivity {

    EditText textMessage;
    CardView sendMessage;
    ImageView backButton;
    Toolbar toolbar;
    TextView nameUser;
    ImageView imgUser;

    String timeEnteredMessage,
            receiverName, senderName,
            receiverUid, senderUid,
            senderRoom, receiverRoom,
            currentTime,
            uri, message;

    FirebaseAuth auth;
    FirebaseDatabase database;

    DatabaseReference databaseReference;

    RecyclerView recyclerView;

    Date date = new Date();

    Calendar calendar;
    SimpleDateFormat simpleDateFormat;

    MassagesAdapter messagesAdapter;
    ArrayList<Messages> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_fragment);

        textMessage = findViewById(R.id.text_message);
        sendMessage = findViewById(R.id.send_message);
        backButton = findViewById(R.id.back_button);
        //toolbar = findViewById(R.id.toolbar);
        nameUser = findViewById(R.id.name_user);
        imgUser = findViewById(R.id.img_user);

        messages = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        messagesAdapter = new MassagesAdapter(messages);
        recyclerView.setAdapter(messagesAdapter);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("hh:mm a");

        senderUid = auth.getUid();
        receiverUid = getIntent().getStringExtra("uid");
        receiverName = getIntent().getStringExtra("name");

        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        databaseReference = database.getReference().child("chats").child(senderRoom).child("messages");
        messagesAdapter = new MassagesAdapter(messages);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Messages message = snapshot1.getValue(Messages.class);
                    messages.add(message);
                }
                messagesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        backButton.setOnClickListener(back -> {
            finish();
        });

        nameUser.setText(receiverName);
        uri = getIntent().getStringExtra("img");

        Picasso.get().load(uri).into(imgUser);

        sendMessage.setOnClickListener(send -> {
            message = textMessage.getText().toString();

            if (message.isEmpty()) {
                textMessage.setError("Enter message");
            } else {
                currentTime = simpleDateFormat.format(calendar.getTime());
                Messages messages = new Messages(message, auth.getUid(), currentTime, date.getTime());
                database.getReference().child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .push()
                        .setValue(messages);
            }
            textMessage.setText("");
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        messagesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (messagesAdapter != null) {
            messagesAdapter.notifyDataSetChanged();
        }
    }
}