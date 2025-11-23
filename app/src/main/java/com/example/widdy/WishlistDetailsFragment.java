package com.example.widdy;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.widdy.model.GiftModel;
import com.example.widdy.model.WishlistModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class WishlistDetailsFragment extends Fragment {

    private static final String TAG = "WishlistDetails";

    private ImageView backButton, wishlistCoverImage, deleteWishlistButton, editWishlistButton;
    private TextView wishlistName, wishlistDate, accessCode, giftCount;
    private RecyclerView giftsRecyclerView;
    private LinearLayout emptyState;
    private com.google.android.material.floatingactionbutton.FloatingActionButton addGiftButton;
    private View contentLayout;
    private View loadingLayout;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String userId;
    private String wishlistDocId;
    private ArrayList<GiftModel> giftsList = new ArrayList<>();
    private GiftAdapter giftAdapter;

    public WishlistDetailsFragment() {}

    public static WishlistDetailsFragment newInstance(String wishlistDocId) {
        WishlistDetailsFragment fragment = new WishlistDetailsFragment();
        Bundle args = new Bundle();
        args.putString("wishlistDocId", wishlistDocId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wishlist_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();
        }

        if (getArguments() != null) {
            wishlistDocId = getArguments().getString("wishlistDocId");
        }

        Log.d(TAG, "wishlistDocId: " + wishlistDocId);
        Log.d(TAG, "userId: " + userId);

        if (userId == null || wishlistDocId == null || wishlistDocId.isEmpty()) {
            Toast.makeText(getContext(), "خطأ: لم يتم تحديد القائمة", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) getActivity().onBackPressed();
            return;
        }

        initViews(view);
        loadWishlistDetails();

        backButton.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        addGiftButton.setOnClickListener(v -> {
            if (getActivity() instanceof HomePageActivity) {
                ((HomePageActivity) getActivity()).openAddGift(v, wishlistDocId);
            }
        });

        editWishlistButton.setOnClickListener(v -> {
            if (getActivity() instanceof HomePageActivity) {
                ((HomePageActivity) getActivity()).openEditWishlist(userId, wishlistDocId);
            }
        });

        deleteWishlistButton.setOnClickListener(v -> showDeleteDialog());
    }

    private void initViews(View view) {
        contentLayout = view.findViewById(R.id.contentLayout);
        loadingLayout = view.findViewById(R.id.loadingLayout);

        backButton = view.findViewById(R.id.backButton);
        wishlistCoverImage = view.findViewById(R.id.wishlistCoverImage);
        wishlistName = view.findViewById(R.id.wishlistName);
        wishlistDate = view.findViewById(R.id.wishlistDate);
        accessCode = view.findViewById(R.id.accessCode);
        giftCount = view.findViewById(R.id.giftCount);
        giftsRecyclerView = view.findViewById(R.id.giftsRecyclerView);
        emptyState = view.findViewById(R.id.emptyState);
        addGiftButton = view.findViewById(R.id.addGiftButton);
        editWishlistButton = view.findViewById(R.id.editWishlistButton);
        deleteWishlistButton = view.findViewById(R.id.deleteWishlistButton);

        giftsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        giftAdapter = new GiftAdapter(giftsList, this::onEditGift, this::onDeleteGift);
        giftsRecyclerView.setAdapter(giftAdapter);

        showLoading();
    }

    private void loadWishlistDetails() {
        Log.d(TAG, "Loading wishlist from: users/" + userId + "/wishlists/" + wishlistDocId);

        db.collection("users")
                .document(userId)
                .collection("wishlists")
                .document(wishlistDocId)
                .get()
                .addOnSuccessListener(doc -> {
                    Log.d(TAG, "Document exists: " + doc.exists());

                    if (doc.exists()) {
                        WishlistModel wishlist = doc.toObject(WishlistModel.class);
                        if (wishlist != null) {
                            Log.d(TAG, "Wishlist loaded: " + wishlist.getName());

                            wishlistName.setText(wishlist.getName());
                            wishlistDate.setText(wishlist.getDate());
                            accessCode.setText(String.valueOf(wishlist.getAccessCode()));

                            if (wishlist.getImageUrl() != null && !wishlist.getImageUrl().isEmpty()) {
                                Glide.with(this)
                                        .load(wishlist.getImageUrl())
                                        .centerCrop()
                                        .into(wishlistCoverImage);
                            }

                            loadGifts();
                        } else {
                            hideLoading();
                            Log.e(TAG, "Wishlist object is null");
                            Toast.makeText(getContext(), "خطأ في قراءة بيانات القائمة", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        hideLoading();
                        Log.e(TAG, "Document does not exist");
                        Toast.makeText(getContext(), "القائمة غير موجودة", Toast.LENGTH_SHORT).show();
                        if (getActivity() != null) getActivity().onBackPressed();
                    }
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    Log.e(TAG, "Error loading wishlist: " + e.getMessage());
                    Toast.makeText(getContext(), "خطأ في تحميل القائمة: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadGifts() {
        db.collection("users")
                .document(userId)
                .collection("wishlists")
                .document(wishlistDocId)
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
                    updateWishlistItemCount(giftsList.size());

                    hideLoading();
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    Log.e(TAG, "Error loading gifts: " + e.getMessage());
                    Toast.makeText(getContext(), "خطأ في تحميل الهدايا", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateWishlistItemCount(int count) {
        db.collection("users")
                .document(userId)
                .collection("wishlists")
                .document(wishlistDocId)
                .update("itemCount", count)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update count: " + e.getMessage()));
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

    private void onEditGift(GiftModel gift) {
        Toast.makeText(getContext(), "تعديل: " + gift.getName(), Toast.LENGTH_SHORT).show();
    }

    private void onDeleteGift(GiftModel gift) {
        Toast.makeText(getContext(), "حذف: " + gift.getName(), Toast.LENGTH_SHORT).show();
    }

    private void showDeleteDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_confirm_delete, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            deleteWishlist();
        });

        dialog.show();
    }

    private void deleteWishlist() {
        db.collection("users")
                .document(userId)
                .collection("wishlists")
                .document(wishlistDocId)
                .collection("gifts")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (var doc : querySnapshot.getDocuments()) {
                        doc.getReference().delete();
                    }

                    db.collection("users")
                            .document(userId)
                            .collection("wishlists")
                            .document(wishlistDocId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "تم حذف القائمة", Toast.LENGTH_SHORT).show();
                                if (getActivity() != null) getActivity().onBackPressed();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Delete failed: " + e.getMessage());
                                Toast.makeText(getContext(), "فشل حذف القائمة", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Delete gifts failed: " + e.getMessage());
                    Toast.makeText(getContext(), "خطأ في حذف الهدايا", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (wishlistDocId != null) {
            loadGifts();
        }
    }
}