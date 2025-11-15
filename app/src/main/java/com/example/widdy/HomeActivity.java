package com.example.widdy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * HomeActivity
 * تعرض قائمة Wishlists على شكل بطاقات ديناميكية مع إمكانية التمرير.
 */
public class HomeActivity extends AppCompatActivity {

    private FrameLayout wishlistContainer; // Container للبطاقات

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // ربط الـ Container من XML
        wishlistContainer = findViewById(R.id.fragment_container);

        // إضافة البطاقات الافتراضية
        addWishlistCards();
    }

    /**
     * إضافة بطاقات افتراضية
     */
    private void addWishlistCards() {
        addWishlistCard("Birthday Wishes", "5 items", "#7DD3C0", 1);
        addWishlistCard("Holiday Haul", "12 items", "#C9B8E8", 2);
    }

    /**
     * إنشاء بطاقة واحدة وإضافتها للـ Container
     *
     * @param title      اسم القائمة
     * @param itemCount  عدد العناصر
     * @param bgColor    لون الخلفية (Hex)
     * @param wishlistId رقم تعريف القائمة
     */
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

        wishlistContainer.addView(cardView);
    }

    // ============================================
    // مثال مستقبلي: جلب البيانات من Database أو API
    // ============================================
    /*
    private void loadWishlistsFromDatabase() {
        List<Wishlist> wishlists = database.getAllWishlists();
        wishlistContainer.removeAllViews();
        for (Wishlist wishlist : wishlists) {
            addWishlistCard(
                    wishlist.getName(),
                    wishlist.getItemCount() + " items",
                    wishlist.getColor(),
                    wishlist.getId()
            );
        }
    }
    */
}
