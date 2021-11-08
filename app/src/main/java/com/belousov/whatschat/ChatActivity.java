package com.belousov.whatschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChatActivity extends AppCompatActivity {

    TabLayout tabLayout;
    TabItem chat, call, status;
    ViewPager viewPager;
    PagerAdapter pagerAdapter;
    Toolbar toolbar;

    FirebaseAuth auth;
    FirebaseFirestore firestore;
    DocumentReference documentReference;

    Drawable drawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        tabLayout = findViewById(R.id.tabLayout);
        chat = findViewById(R.id.chats);
        call = findViewById(R.id.calls);
        status = findViewById(R.id.status);
        toolbar = findViewById(R.id.toolbar);
        viewPager = findViewById(R.id.view_pager);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        documentReference = firestore.collection("Users").document(auth.getUid());

        // метод назначает toolbar выполнять функции action bar
        setSupportActionBar(toolbar);

        // меняем иконку menu
        drawable = ContextCompat.getDrawable(
                getApplicationContext(),
                R.drawable.ic_baseline_more_vert_24
        );
        toolbar.setOverflowIcon(drawable);

        // adapter
        pagerAdapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);

        // меняем tab при нажатии
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

                if (tab.getPosition() == 0 || tab.getPosition() == 1 || tab.getPosition() == 2) {
                    pagerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.profile:
                Intent intent = new Intent(
                        this,
                        ProfileActivity.class
                );
                startActivity(intent);
                break;
            case R.id.settings:
                Toast.makeText(
                        this,
                        "Settings is clicked",
                        Toast.LENGTH_SHORT
                ).show();
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);

        return true;
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