package com.example.widdy;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private ImageView profileImage;
    private TextView userName, userEmail, wishlistCount, giftCount;
    private LinearLayout editNameLayout, editEmailLayout;
    private View loadingLayout, contentLayout;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        initViews(view);
        loadUserData();
    }

    private void initViews(View view) {
        loadingLayout = view.findViewById(R.id.loadingLayout);
        contentLayout = view.findViewById(R.id.contentLayout);

        profileImage = view.findViewById(R.id.profileImage);
        userName = view.findViewById(R.id.userName);
        userEmail = view.findViewById(R.id.userEmail);
        wishlistCount = view.findViewById(R.id.wishlistCount);
        giftCount = view.findViewById(R.id.giftCount);

        editNameLayout = view.findViewById(R.id.editNameLayout);
        editEmailLayout = view.findViewById(R.id.editEmailLayout);

        editNameLayout.setOnClickListener(v -> showEditNameDialog());

        editEmailLayout.setOnClickListener(v -> showEditEmailDialog());

        showLoading();
    }

    private void loadUserData() {
        if (userId == null) {
            hideLoading();
            Toast.makeText(getContext(), "خطأ في تحميل البيانات", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        String email = doc.getString("email");

                        userName.setText(name != null ? name : "مستخدم");
                        userEmail.setText(email != null ? email : "");
                        profileImage.setImageResource(R.drawable.default_profile);


                        loadWishlistCount();
                    } else {
                        hideLoading();
                        Toast.makeText(getContext(), "بيانات المستخدم غير موجودة", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    Log.e(TAG, "Error loading user data: " + e.getMessage());
                    Toast.makeText(getContext(), "خطأ في تحميل البيانات", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadWishlistCount() {
        db.collection("users")
                .document(userId)
                .collection("wishlists")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();
                    wishlistCount.setText(String.valueOf(count));

                    loadTotalGiftCount(querySnapshot);
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    Log.e(TAG, "Error loading wishlist count: " + e.getMessage());
                });
    }

    private void loadTotalGiftCount(QuerySnapshot wishlists) {
        if (wishlists.isEmpty()) {
            giftCount.setText("0");
            hideLoading();
            return;
        }

        int[] totalGifts = {0};
        int[] processedLists = {0};

        for (var wishlist : wishlists) {
            db.collection("users")
                    .document(userId)
                    .collection("wishlists")
                    .document(wishlist.getId())
                    .collection("gifts")
                    .get()
                    .addOnSuccessListener(gifts -> {
                        totalGifts[0] += gifts.size();
                        processedLists[0]++;

                        // لما نخلص كل القوائم
                        if (processedLists[0] == wishlists.size()) {
                            giftCount.setText(String.valueOf(totalGifts[0]));
                            hideLoading();
                        }
                    })
                    .addOnFailureListener(e -> {
                        processedLists[0]++;
                        if (processedLists[0] == wishlists.size()) {
                            giftCount.setText(String.valueOf(totalGifts[0]));
                            hideLoading();
                        }
                    });
        }
    }

    private void showEditNameDialog() {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_edit_text, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        EditText editText = dialogView.findViewById(R.id.editText);
        android.widget.Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        android.widget.Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        dialogTitle.setText("تعديل الاسم");
        editText.setText(userName.getText().toString());
        editText.setHint("أدخل الاسم الجديد");

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String newName = editText.getText().toString().trim();

            if (newName.isEmpty()) {
                editText.setError("الرجاء إدخال اسم");
                return;
            }

            dialog.dismiss();
            updateUserName(newName);
        });

        dialog.show();
    }

    private void showEditEmailDialog() {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_edit_text, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        EditText editText = dialogView.findViewById(R.id.editText);
        android.widget.Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        android.widget.Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        dialogTitle.setText("تعديل البريد الإلكتروني");
        editText.setText(userEmail.getText().toString());
        editText.setHint("أدخل البريد الجديد");
        editText.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String newEmail = editText.getText().toString().trim();

            if (newEmail.isEmpty()) {
                editText.setError("الرجاء إدخال البريد الإلكتروني");
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                editText.setError("الرجاء إدخال بريد إلكتروني صحيح");
                return;
            }

            dialog.dismiss();
            updateUserEmail(newEmail);
        });

        dialog.show();
    }

    private void updateUserName(String newName) {
        showLoading();

        db.collection("users")
                .document(userId)
                .update("name", newName)
                .addOnSuccessListener(unused -> {
                    userName.setText(newName);
                    hideLoading();
                    Toast.makeText(getContext(), "تم تحديث الاسم بنجاح", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    Log.e(TAG, "Error updating name: " + e.getMessage());
                    Toast.makeText(getContext(), "حدث خطأ أثناء التحديث", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserEmail(String newEmail) {
        showLoading();

        db.collection("users")
                .document(userId)
                .update("email", newEmail)
                .addOnSuccessListener(unused -> {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        user.updateEmail(newEmail)
                                .addOnSuccessListener(unused2 -> {
                                    userEmail.setText(newEmail);
                                    hideLoading();
                                    Toast.makeText(getContext(), "تم تحديث البريد الإلكتروني بنجاح", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    hideLoading();
                                    Log.e(TAG, "Error updating email in Auth: " + e.getMessage());
                                    Toast.makeText(getContext(), "حدث خطأ، قد تحتاج لتسجيل الدخول مرة أخرى", Toast.LENGTH_LONG).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    Log.e(TAG, "Error updating email in Firestore: " + e.getMessage());
                    Toast.makeText(getContext(), "حدث خطأ أثناء التحديث", Toast.LENGTH_SHORT).show();
                });
    }

    private void showLoading() {
        if (loadingLayout != null) loadingLayout.setVisibility(View.VISIBLE);
        if (contentLayout != null) contentLayout.setVisibility(View.GONE);
    }

    private void hideLoading() {
        if (loadingLayout != null) loadingLayout.setVisibility(View.GONE);
        if (contentLayout != null) contentLayout.setVisibility(View.VISIBLE);
    }
}