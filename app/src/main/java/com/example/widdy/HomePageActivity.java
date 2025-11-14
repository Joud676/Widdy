package com.example.widdy;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class HomePageActivity extends AppCompatActivity {

    ImageView icon_home, icon_gift, icon_profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        icon_home = findViewById(R.id.icon_home);
        icon_gift = findViewById(R.id.icon_gift);
        icon_profile = findViewById(R.id.icon_profile);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();

        icon_home.setOnClickListener(v -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        });

        icon_gift.setOnClickListener(v -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    //.replace(R.id.fragment_container, new WishlistFragment())
                    .commit();
        });

        icon_profile.setOnClickListener(v -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    //.replace(R.id.fragment_container, new ProfileFragment())
                    .commit();
        });
    }
}
