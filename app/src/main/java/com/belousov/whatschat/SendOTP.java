package com.belousov.whatschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class SendOTP extends AppCompatActivity {

    EditText enteredPhoneNumber;
    Button sendOTP;
    CountryCodePicker countryCodePicker;
    TextView countryCodeNumber;

    String countryCode, phoneNumber;

    FirebaseAuth auth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_otp);

        auth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progress_bar);

        enteredPhoneNumber = findViewById(R.id.entered_phone_number);
        sendOTP = findViewById(R.id.send_OTP);
        countryCodePicker = findViewById(R.id.country_code_picker);
        countryCodeNumber = findViewById(R.id.country_code_number);

        // автоматически определяет телефонный код страны
        countryCode = countryCodePicker.getSelectedCountryCodeWithPlus();
        countryCodeNumber.setText(countryCode);

        // меняем телефонный код страны
        countryCodePicker.setOnCountryChangeListener(() -> {
            countryCode = countryCodePicker.getSelectedCountryCodeWithPlus();
            countryCodeNumber.setText(countryCode);
        });

        // отправляем данные на сервер
        sendOTP.setOnClickListener(sendOTP -> {
            // получаем введенный номер телефона
            phoneNumber = enteredPhoneNumber.getText().toString();

            if (phoneNumber.isEmpty())
                enteredPhoneNumber.setError("Please Enter Your Phone Number");
            else if (phoneNumber.length() < 10)
                enteredPhoneNumber.setError("Please Enter Correct Number");
            else {
                sendOTP.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                phoneNumber = countryCode + phoneNumber;

                // отправка OTP
                PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phoneNumber)
                        .setActivity(this)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setCallbacks(
                                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                    @Override
                                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                                    }

                                    @Override
                                    public void onVerificationFailed(@NonNull FirebaseException e) {

                                    }

                                    @Override
                                    public void onCodeSent(@NonNull String code, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        sendOTP.setVisibility(View.VISIBLE);
                                        Intent intent = new Intent(
                                                getApplicationContext(),
                                                VerifyOTP.class
                                        );
                                        intent.putExtra("otp", code);
                                        intent.putExtra("countryCodeNumber", countryCode);
                                        intent.putExtra("phoneNumber", phoneNumber.substring(2));
                                        startActivity(intent);
                                    }
                                }
                        )
                        .build();
                PhoneAuthProvider.verifyPhoneNumber(options);
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

    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null) {
            Intent intent = new Intent(
                    this,
                    ChatActivity.class
            );
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}