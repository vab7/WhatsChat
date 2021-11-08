package com.belousov.whatschat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SaveProfile extends AppCompatActivity {

    // инициализируем все переменные с которыми будем работать

    ImageView userIMG;
    EditText enteredUserName;
    Button saveProfile;

    Uri uriIMG;
    User user;
    Map<String, Object> userData;
    UploadTask uploadTask;

    FirebaseAuth auth;
    FirebaseStorage storage;
    FirebaseDatabase database;
    FirebaseFirestore firestore;

    StorageReference storageReference;
    DatabaseReference databaseReference;
    DocumentReference documentReference;

    String userName, uriImgString;

    Bitmap bitmap;
    ByteArrayOutputStream byteArrayOutputStream;

    ProgressBar progressBar;

    final int PICK_IMG = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_profile);

        // instance - экземпляр (получаем экземпляры классов)
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // создаем папки для хранения данных пользователя
        storageReference = storage.getReference().child("Images").child(auth.getUid()).child("pick");
        documentReference = firestore.collection("Users").document(auth.getUid());
        databaseReference = database.getReference(auth.getUid());

        // находим элементы в xml
        userIMG = findViewById(R.id.user_img);
        enteredUserName = findViewById(R.id.user_name);
        saveProfile = findViewById(R.id.save_profile);
        progressBar = findViewById(R.id.progress_bar);

        // выбираем картинку пользователя
        userIMG.setOnClickListener(userIMG -> {
            getUserImg();
        });

        // отправляем данные пользователя на сервер
        saveProfile.setOnClickListener(save -> {
            userName = enteredUserName.getText().toString();

            if (userName.isEmpty())
                enteredUserName.setError("Enter User Your Name");
            else if (userIMG == null)
                getUserImg();
            else {
                saveProfile.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);

                try {
                    sendDataUser(); // метод отправки данных
                } catch (IOException e) {
                    e.printStackTrace();
                }

                progressBar.setVisibility(View.INVISIBLE);
                saveProfile.setVisibility(View.VISIBLE);

                // переходим в chat
                Intent intent = new Intent(
                        this,
                        ChatActivity.class
                );
                startActivity(intent);
                finish();
            }
        });

    }

    private void sendDataUser() throws IOException {
        user = new User(userName, auth.getUid()); // создали обьект user
        databaseReference.setValue(user); // отправляем данные в realtime database

        // класс предоставляет входной поток, использующий в качестве источника данных массив байтов
        byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriIMG);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

//         создаем задачу успеха и неудачи
        uploadTask = storageReference.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {

            // создаем объект пользователя (ключ, значение)
            userData = new HashMap<>();
            userData.put("name", userName);
            userData.put("image", uriIMG.toString());
            userData.put("uid", auth.getUid());
            userData.put("status", "Online");

            // отправляем данные пользователя на сервер firestore database
            documentReference.set(userData);
        }).addOnFailureListener(e -> {
            Toast.makeText(
                    this,
                    "Error",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    private void getUserImg() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMG) {
            uriIMG = data.getData();
            userIMG.setImageURI(uriIMG);
        }
    }
}