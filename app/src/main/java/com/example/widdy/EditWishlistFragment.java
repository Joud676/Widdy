package com.example.widdy;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.widdy.model.WishlistModel;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditWishlistFragment extends Fragment {

    private static final String ARG_USER_ID = "userId";
    private static final String ARG_WISHLIST_ID = "wishlistId";
    private static final int IMAGE_PICK_CODE = 300;

    private String userId;
    private String wishlistDocId;
    private Uri selectedImageUri = null;
    private String currentImageUrl = "";

    private ImageView coverImage, backButton;
    private LinearLayout imagePlaceholder;
    private EditText wishlistNameInput, notesInput;
    private TextView dateText;
    private Button saveButton;
    private MaterialCardView dateCard, imageCard;

    private FirebaseFirestore db;

    public EditWishlistFragment() {}

    public static EditWishlistFragment newInstance(String userId, String wishlistDocId) {
        EditWishlistFragment fragment = new EditWishlistFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        args.putString(ARG_WISHLIST_ID, wishlistDocId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);
            wishlistDocId = getArguments().getString(ARG_WISHLIST_ID);
        }
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_wishlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        coverImage = view.findViewById(R.id.imagePreview);
        imagePlaceholder = view.findViewById(R.id.imagePlaceholder);
        wishlistNameInput = view.findViewById(R.id.occasionNameInput);
        notesInput = view.findViewById(R.id.notesInput);
        dateText = view.findViewById(R.id.dateText);
        dateCard = view.findViewById(R.id.dateCard);
        imageCard = view.findViewById(R.id.imageCard);
        saveButton = view.findViewById(R.id.editWishlistBtn);
        backButton = view.findViewById(R.id.backButton);

        loadWishlistData();

        dateCard.setOnClickListener(v -> openDatePicker());
        imageCard.setOnClickListener(v -> pickImage());
        saveButton.setOnClickListener(v -> saveWishlistChanges());
        backButton.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });
    }

    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(
                getContext(),
                (view, year, month, day) -> {
                    String date = day + "/" + (month + 1) + "/" + year;
                    dateText.setText(date);
                    dateText.setTextColor(getResources().getColor(R.color.black));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.show();
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_CODE && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                coverImage.setImageURI(selectedImageUri);
                coverImage.setVisibility(View.VISIBLE);
                imagePlaceholder.setVisibility(View.GONE);
            }
        }
    }

    private void loadWishlistData() {
        if (userId == null || wishlistDocId == null) return;

        db.collection("users")
                .document(userId)
                .collection("wishlists")
                .document(wishlistDocId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        WishlistModel wishlist = doc.toObject(WishlistModel.class);
                        if (wishlist != null) {
                            wishlistNameInput.setText(wishlist.getName());
                            dateText.setText(wishlist.getDate());
                            notesInput.setText(wishlist.getNotes());
                            currentImageUrl = wishlist.getImageUrl();

                            if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                                Glide.with(this)
                                        .load(currentImageUrl)
                                        .centerCrop()
                                        .into(coverImage);
                                coverImage.setVisibility(View.VISIBLE);
                                imagePlaceholder.setVisibility(View.GONE);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "خطأ في تحميل بيانات القائمة", Toast.LENGTH_SHORT).show());
    }

    private void saveWishlistChanges() {
        String newName = wishlistNameInput.getText().toString().trim();
        String newDate = dateText.getText().toString().trim();
        String newNotes = notesInput.getText().toString().trim();

        if (newName.isEmpty() || newDate.isEmpty()) {
            Toast.makeText(getContext(), "الرجاء ملء جميع الحقول", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userId == null || wishlistDocId == null) return;

        saveButton.setEnabled(false);
        saveButton.setText("جاري الحفظ...");

        if (selectedImageUri != null) {
            uploadNewImage(newName, newDate, newNotes);
        } else {
            updateWishlist(newName, newDate, newNotes, currentImageUrl);
        }
    }

    private void uploadNewImage(String newName, String newDate, String newNotes) {
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(selectedImageUri);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[4096];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] bytes = buffer.toByteArray();

            String apiKey = "843245c4456926b93571bc842e628ae4";
            OkHttpClient client = new OkHttpClient();

            RequestBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", "wishlist.jpg",
                            RequestBody.create(bytes, MediaType.parse("image/*")))
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.imgbb.com/1/upload?key=" + apiKey)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, java.io.IOException e) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "فشل رفع الصورة", Toast.LENGTH_SHORT).show();
                            resetButton();
                        });
                    }
                }

                @Override
                public void onResponse(Call call, Response response) {
                    if (!response.isSuccessful()) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "فشل رفع الصورة", Toast.LENGTH_SHORT).show();
                                resetButton();
                            });
                        }
                        return;
                    }

                    try {
                        String respStr = response.body().string();
                        String imageUrl = extractImageUrl(respStr);

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> updateWishlist(newName, newDate, newNotes, imageUrl));
                        }
                    } catch (Exception e) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "خطأ في قراءة الرد", Toast.LENGTH_SHORT).show();
                                resetButton();
                            });
                        }
                    }
                }
            });

        } catch (Exception e) {
            Toast.makeText(getContext(), "حدث خطأ", Toast.LENGTH_SHORT).show();
            resetButton();
        }
    }

    private String extractImageUrl(String json) {
        int start = json.indexOf("\"url\":\"") + 7;
        int end = json.indexOf("\"", start);
        if (start > 6 && end > start) {
            return json.substring(start, end).replace("\\/", "/");
        }
        return "";
    }

    private void updateWishlist(String newName, String newDate, String newNotes, String imageUrl) {
        DocumentReference docRef = db.collection("users")
                .document(userId)
                .collection("wishlists")
                .document(wishlistDocId);

        docRef.update("name", newName, "date", newDate, "notes", newNotes, "imageUrl", imageUrl)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "تم تحديث القائمة بنجاح", Toast.LENGTH_SHORT).show();
                    resetButton();
                    if (getActivity() != null) getActivity().onBackPressed();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "فشل تحديث القائمة", Toast.LENGTH_SHORT).show();
                    resetButton();
                });
    }

    private void resetButton() {
        if (saveButton != null) {
            saveButton.setEnabled(true);
            saveButton.setText("حفظ التعديلات");
        }
    }
}