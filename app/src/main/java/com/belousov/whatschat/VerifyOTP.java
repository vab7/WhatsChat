package com.belousov.whatschat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class VerifyOTP extends AppCompatActivity {

    TextView changeNumber, countryCodeNumber;
    EditText enteredOTP, phoneNumber;
    Button verifyOTP;

    String otp;

    FirebaseAuth auth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        auth = FirebaseAuth.getInstance();

        changeNumber = findViewById(R.id.change_number);
        enteredOTP = findViewById(R.id.entered_OTP);
        verifyOTP = findViewById(R.id.verify_OTP);
        progressBar = findViewById(R.id.progress_bar);
        phoneNumber = findViewById(R.id.phone_number);
        countryCodeNumber = findViewById(R.id.country_code_number);

        // полученный номер телефона
        countryCodeNumber.setText(getIntent().getStringExtra("countryCodeNumber"));
        phoneNumber.setText(getIntent().getStringExtra("phoneNumber"));

        // изменить номер телефона
        changeNumber.setOnClickListener(changeNumber -> {
            Intent intent = new Intent(
                    this,
                    SendOTP.class
            );
            startActivity(intent);
            finish();
        });

        // проверяем OTP
        verifyOTP.setOnClickListener(verify -> {
            otp = enteredOTP.getText().toString();

            if (otp.isEmpty())
                enteredOTP.setError("Enter Your OTP");
            else {
                verifyOTP.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                PhoneAuthCredential credential = PhoneAuthProvider
                        .getCredential(
                                getIntent().getStringExtra("otp"),
                                otp
                        );
                auth.signInWithCredential(credential).addOnCompleteListener(auth -> {
                    if (auth.isSuccessful()) {
                        Intent intent = new Intent(
                                this,
                                SaveProfile.class
                        );
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                        verifyOTP.setVisibility(View.INVISIBLE);
                        Toast.makeText(
                                this,
                                "Error",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
            }

        });

        changeStatusBarColor(R.color.green);

    }

    private void changeStatusBarColor(int color) {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, color));
    }
}