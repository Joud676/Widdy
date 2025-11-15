package com.example.widdy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private LinearLayout wishlistContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // ربط الـ Container
        wishlistContainer = findViewById(R.id.wishlistContainer);

        // إضافة البطاقات
        addWishlistCards();
    }

    private void addWishlistCards() {
        // البطاقة الأولى - Birthday Wishes (Teal)
        addWishlistCard("Birthday Wishes", "5 items", "#7DD3C0", 1);

        // البطاقة الثانية - Holiday Haul (Purple)
        addWishlistCard("Holiday Haul", "12 items", "#C9B8E8", 2);
    }

    private void addWishlistCard(String title, String itemCount, String bgColor, int wishlistId) {
        View cardView = LayoutInflater.from(this)
                .inflate(R.layout.activity_wishlist_card_item, wishlistContainer, false);

        cardView.setBackgroundColor(android.graphics.Color.parseColor(bgColor));

        TextView tvName = cardView.findViewById(R.id.tvWishlistName);
        TextView tvCount = cardView.findViewById(R.id.tvItemCount);
        Button btnView = cardView.findViewById(R.id.btnView);

        tvName.setText(title);
        tvCount.setText(itemCount);

        btnView.setOnClickListener(v -> {
            Toast.makeText(this, "Opening " + title, Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(this, WishlistDetailsActivity.class);
            // intent.putExtra("wishlist_id", wishlistId);
            // startActivity(intent);
        });

        // إضافة البطاقة للـ Container
        wishlistContainer.addView(cardView);
    }
}


// ============================================
// إذا عندك بيانات من Database أو API:
// ============================================

// في الـ Activity:
//private void loadWishlistsFromDatabase() {
//    // احصل على البيانات من Database
//    List<Wishlist> wishlists = database.getAllWishlists();
//
//    // امسح البطاقات القديمة
//    wishlistContainer.removeAllViews();
//
//    // أضف البطاقات الجديدة
//    for (Wishlist wishlist : wishlists) {
//        addWishlistCard(
//                wishlist.getName(),
//                wishlist.getItemCount() + " items",
//                wishlist.getColor(),
//                wishlist.getId()
//        );
//    }
//}