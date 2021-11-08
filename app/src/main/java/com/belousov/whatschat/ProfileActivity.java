package com.belousov.whatschat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    EditText userName;
    Button saveProfile;
    ImageView userIMG;
    Toolbar toolbar;
    ImageView backButton;
    ProgressBar progressBar;

    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    FirebaseFirestore firestore;

    DatabaseReference databaseReference;
    StorageReference storageReference;
    DocumentReference documentReference;

    String uriIMGString, name;
    User user;
    Uri uri;
    Uri oldUri;

    final int PICK_IMG = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        firestore = FirebaseFirestore.getInstance();

        databaseReference = database.getReference(auth.getUid());
        storageReference = storage.getReference().child("Images").child(auth.getUid()).child("pick");
        documentReference = firestore.collection("Users").document(auth.getUid());

        userName = findViewById(R.id.user_name);
        saveProfile = findViewById(R.id.save_profile);
        userIMG = findViewById(R.id.user_img_profile);
        toolbar = findViewById(R.id.toolbar);
        backButton = findViewById(R.id.back_button);
        progressBar = findViewById(R.id.progress_bar);

        // метод назначает toolbar выполнять функции action bar
        setSupportActionBar(toolbar);

        // выходим из профиля
        backButton.setOnClickListener(back -> {
            Map<String, Object> userData = new HashMap<>();
            userData.put("name", name);
            userData.put("image", uriIMGString);
            userData.put("uid", auth.getUid());
            userData.put("status", "Online");

            documentReference.set(userData).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Update", Toast.LENGTH_SHORT).show();
            });

            finish();
        });

        // скачиваем картинку пользователя
        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            oldUri = uri;
            uriIMGString = uri.toString();
            Picasso.get().load(uriIMGString).into(userIMG);
        });

        // получаем имя пользователя
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                userName.setText(user.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        getApplicationContext(),
                        "Error",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // меняем картинку пользователя
        userIMG.setOnClickListener(user -> {
            getImgUser();
        });

        // сохраняем данные пользователя
        saveProfile.setOnClickListener(save -> {
            name = userName.getText().toString();

            if (name.isEmpty())
                userName.setError("Error");
            else {
                saveProfile.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);

                // отправляем имя пользователя
                User user = new User(name, auth.getUid());
                databaseReference.setValue(user);

                // меняем картинку пользователя
                if (uri != null) {
                    storageReference.putFile(uri);
                }

                saveProfile.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);

                Toast.makeText(
                        this,
                        "Updated",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

    }

    private void getImgUser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMG) {
            uri = data.getData();
            uriIMGString = uri.toString();
            userIMG.setImageURI(uri);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        documentReference.update("status", "Online");
    }

    @Override
    protected void onStop() {
        super.onStop();

        documentReference.update("status", "Offline");
    }

}