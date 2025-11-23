package com.example.widdy;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.widdy.model.GiftModel;
import com.example.widdy.model.WishlistModel;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class GuestGiftsActivity extends AppCompatActivity {

    private static final String TAG = "GuestGiftsActivity";

    private ImageView backButton, wishlistCoverImage;
    private TextView wishlistName, wishlistDate, giftCount;
    private RecyclerView giftsRecyclerView;
    private LinearLayout emptyState;
    private View contentLayout;
    private View loadingLayout;

    private FirebaseFirestore db;
    private String ownerUserId;
    private String wishlistId;
    private ArrayList<GiftModel> giftsList = new ArrayList<>();
    private GiftReserveAdapter giftAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_gifts);

        db = FirebaseFirestore.getInstance();

        // استلام البيانات من EnterCodeActivity
        ownerUserId = getIntent().getStringExtra("ownerId");
        wishlistId = getIntent().getStringExtra("wishlistId");

        Log.d(TAG, "ownerId: " + ownerUserId);
        Log.d(TAG, "wishlistId: " + wishlistId);

        if (ownerUserId == null || wishlistId == null) {
            Toast.makeText(this, "خطأ: بيانات القائمة غير صحيحة", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadWishlistDetails();

        backButton.setOnClickListener(v -> finish());
    }

    private void initViews() {
        contentLayout = findViewById(R.id.contentLayout);
        loadingLayout = findViewById(R.id.loadingLayout);

        backButton = findViewById(R.id.backButton);
        wishlistCoverImage = findViewById(R.id.wishlistCoverImage);
        wishlistName = findViewById(R.id.wishlistName);
        wishlistDate = findViewById(R.id.wishlistDate);
        giftCount = findViewById(R.id.giftCount);
        giftsRecyclerView = findViewById(R.id.giftsRecyclerView);
        emptyState = findViewById(R.id.emptyState);

        giftsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        giftAdapter = new GiftReserveAdapter(giftsList, this::onReserveClick);
        giftsRecyclerView.setAdapter(giftAdapter);

        showLoading();
    }

    private void loadWishlistDetails() {
        Log.d(TAG, "Loading wishlist from: users/" + ownerUserId + "/wishlists/" + wishlistId);

        db.collection("users")
                .document(ownerUserId)
                .collection("wishlists")
                .document(wishlistId)
                .get()
                .addOnSuccessListener(doc -> {
                    Log.d(TAG, "Document exists: " + doc.exists());

                    if (doc.exists()) {
                        WishlistModel wishlist = doc.toObject(WishlistModel.class);
                        if (wishlist != null) {
                            Log.d(TAG, "Wishlist loaded: " + wishlist.getName());

                            wishlistName.setText(wishlist.getName());
                            wishlistDate.setText(wishlist.getDate());

                            if (wishlist.getImageUrl() != null && !wishlist.getImageUrl().isEmpty()) {
                                Glide.with(this)
                                        .load(wishlist.getImageUrl())
                                        .centerCrop()
                                        .placeholder(R.drawable.welcome_widdy)
                                        .error(R.drawable.welcome_widdy)
                                        .into(wishlistCoverImage);
                            } else {
                                wishlistCoverImage.setImageResource(R.drawable.welcome_widdy);
                            }

                            loadGifts();
                        } else {
                            hideLoading();
                            Log.e(TAG, "Wishlist object is null");
                            Toast.makeText(this, "خطأ في قراءة بيانات القائمة", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        hideLoading();
                        Log.e(TAG, "Document does not exist");
                        Toast.makeText(this, "القائمة غير موجودة", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    Log.e(TAG, "Error loading wishlist: " + e.getMessage());
                    Toast.makeText(this, "خطأ في تحميل القائمة: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void loadGifts() {
        db.collection("users")
                .document(ownerUserId)
                .collection("wishlists")
                .document(wishlistId)
                .collection("gifts")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    giftsList.clear();

                    if (snapshots.isEmpty()) {
                        showEmptyState();
                    } else {
                        hideEmptyState();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            GiftModel gift = doc.toObject(GiftModel.class);
                            giftsList.add(gift);
                        }
                    }

                    giftCount.setText(giftsList.size() + " هدية");
                    giftAdapter.notifyDataSetChanged();

                    hideLoading();
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    Log.e(TAG, "Error loading gifts: " + e.getMessage());
                    Toast.makeText(this, "خطأ في تحميل الهدايا", Toast.LENGTH_SHORT).show();
                });
    }

    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        giftsRecyclerView.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        emptyState.setVisibility(View.GONE);
        giftsRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showLoading() {
        if (contentLayout != null) contentLayout.setVisibility(View.GONE);
        if (loadingLayout != null) loadingLayout.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        if (loadingLayout != null) loadingLayout.setVisibility(View.GONE);
        if (contentLayout != null) contentLayout.setVisibility(View.VISIBLE);
    }

    private void onReserveClick(GiftModel gift) {
        if (gift.isReserved()) {
            showAlreadyReservedDialog(gift);
        } else {
            showReserveConfirmDialog(gift);
        }
    }

    private void showAlreadyReservedDialog(GiftModel gift) {
        new AlertDialog.Builder(this)
                .setTitle("هدية محجوزة")
                .setMessage("هذه الهدية محجوزة مسبقاً من قبل شخص آخر")
                .setPositiveButton("حسناً", null)
                .show();
    }

    private void showReserveConfirmDialog(GiftModel gift) {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_reserve_gift, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        TextView giftNameText = dialogView.findViewById(R.id.giftNameText);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        giftNameText.setText(gift.getName());

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            reserveGift(gift);
        });

        dialog.show();
    }

    private void reserveGift(GiftModel gift) {
        // عرض loading
        showLoading();

        DocumentReference docRef = db.collection("users")
                .document(ownerUserId)
                .collection("wishlists")
                .document(wishlistId)
                .collection("gifts")
                .document(gift.getID());

        docRef.update(
                "isReserved", true,
                "reservedBy", "ضيف"
        ).addOnSuccessListener(unused -> {
            Log.d(TAG, "Gift reserved successfully");

            // تحديث الكائن المحلي
            gift.setReserved(true);
            gift.setReservedBy("ضيف");

            // إعادة تحميل القائمة لإظهار التحديث
            loadGifts();

            Toast.makeText(this, "تم حجز الهدية بنجاح ✅", Toast.LENGTH_SHORT).show();

        }).addOnFailureListener(e -> {
            hideLoading();
            Log.e(TAG, "Reserve failed: " + e.getMessage());
            Toast.makeText(this, "حدث خطأ أثناء الحجز، حاول مرة أخرى", Toast.LENGTH_SHORT).show();
        });
    }
}