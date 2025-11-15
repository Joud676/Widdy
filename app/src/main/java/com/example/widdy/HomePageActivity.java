package com.example.widdy;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class HomePageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new CreateWishlistFragment())
                .commit();
    }

    public void openHome(View view) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
    }

    public void openWishlists(View view) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new AllWishlistsFragment())
                .commit();
    }

    public void openCreateWishlists(View view) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new CreateWishlistFragment())
                .commit();
    }

    public void openAddGift(View view) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new AddGiftFragment())
                .commit();
    }

    public void openProfile(View view) {
    }
}
