package com.example.widdy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class HomePageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .addToBackStack(null)
                .commit();
    }

    public void openHome(View view) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .addToBackStack(null)
                .commit();
    }



    public void openCreateWishlists(View view) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new CreateWishlistFragment())
                .addToBackStack(null)
                .commit();
    }

    public void openAddGift(View view, String wishlistDocId) {
        AddGiftFragment fragment = new AddGiftFragment();
        Bundle args = new Bundle();
        args.putString("wishlistId", wishlistDocId);
        fragment.setArguments(args);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }


    public void openProfile(View view) {
    }

    public void openWishlists(View view) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new AllWishlistsFragment())
                .addToBackStack(null)
                .commit();
    }

    public void openWishlistDetails(String wishlistDocId) {
        WishlistDetailsFragment fragment = new WishlistDetailsFragment();
        Bundle args = new Bundle();
        args.putString("wishlistDocId", wishlistDocId);
        fragment.setArguments(args);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void openEditWishlist(String userId, String wishlistDocId) {
        EditWishlistFragment fragment = EditWishlistFragment.newInstance(userId, wishlistDocId);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }


}