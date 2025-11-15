package com.example.widdy;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateWishlistFragment extends Fragment {

    private static final String TAG = "CreateWishlist";
    private static final int IMAGE_PICK_CODE = 100;

    EditText occasionNameInput, notesInput;
    TextView dateText;
    ImageView imagePreview;
    LinearLayout imagePlaceholder;
    MaterialCardView dateCard, imageCard;
    Button createWishlistBtn;

    Uri selectedImageUri = null;
    FirebaseFirestore db;
    FirebaseAuth auth;

    public CreateWishlistFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_create_wishlist, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        occasionNameInput = view.findViewById(R.id.occasionNameInput);
        notesInput = view.findViewById(R.id.notesInput);
        dateText = view.findViewById(R.id.dateText);
        dateCard = view.findViewById(R.id.dateCard);

        imageCard = view.findViewById(R.id.imageCard);
        imagePlaceholder = view.findViewById(R.id.imagePlaceholder);
        imagePreview = view.findViewById(R.id.imagePreview);

        createWishlistBtn = view.findViewById(R.id.createWishlistBtn);

        dateCard.setOnClickListener(v -> openDatePicker());
        imageCard.setOnClickListener(v -> pickImage());
        createWishlistBtn.setOnClickListener(v -> saveWishlist());

        return view;
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
                imagePreview.setImageURI(selectedImageUri);
                imagePreview.setVisibility(View.VISIBLE);
                imagePlaceholder.setVisibility(View.GONE);
            }
        }
    }

    private void saveWishlist() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "يجب تسجيل الدخول أولاً", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "User not logged in");
            return;
        }

        String uid = currentUser.getUid();
        String name = occasionNameInput.getText().toString().trim();
        String date = dateText.getText().toString().trim();
        String notes = notesInput.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(getContext(), "اكتب اسم المناسبة", Toast.LENGTH_SHORT).show();
            return;
        }
        if (date.equals("اختر التاريخ")) {
            Toast.makeText(getContext(), "اختر التاريخ", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "اختر صورة", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Starting wishlist creation for user: " + uid);
        uploadImageToImgBB(name, date, notes, uid);
    }

    private void uploadImageToImgBB(String name, String date, String notes, String uid) {
        try {
            Log.d(TAG, "Starting image upload to ImgBB...");

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
                    .addFormDataPart("image", "image.jpg",
                            RequestBody.create(bytes, MediaType.parse("image/*")))
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.imgbb.com/1/upload?key=" + apiKey)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Image upload failed: " + e.getMessage());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "فشل رفع الصورة: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d(TAG, "Image upload response code: " + response.code());

                    if (!response.isSuccessful()) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "فشل رفع الصورة: " + response.code(), Toast.LENGTH_SHORT).show());
                        }
                        return;
                    }

                    String respStr = response.body().string();
                    Log.d(TAG, "Image upload response: " + respStr);
                    String imageUrl = extractImageUrl(respStr);

                    if (imageUrl.isEmpty()) {
                        Log.e(TAG, "Failed to extract image URL from response");
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "فشل استخراج رابط الصورة", Toast.LENGTH_SHORT).show());
                        }
                        return;
                    }

                    Log.d(TAG, "Image URL extracted: " + imageUrl);

                    Map<String, Object> wishlist = new HashMap<>();
                    wishlist.put("name", name);
                    wishlist.put("date", date);
                    wishlist.put("notes", notes);
                    wishlist.put("imageUrl", imageUrl);
                    wishlist.put("userId", uid);
                    wishlist.put("createdAt", System.currentTimeMillis());

                    saveWishlistToFirestore(wishlist);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception during image upload: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(getContext(), "حدث خطأ أثناء رفع الصورة: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void saveWishlistToFirestore(Map<String, Object> wishlist) {
        Log.d(TAG, "Starting to save wishlist to Firestore...");

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;
        String uid = currentUser.getUid();

        db.collection("wishlists")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    long count = querySnapshot.size();
                    String docId = "wishlist" + (count + 1);

                    Log.d(TAG, "Generated document ID: " + docId);

                    db.collection("wishlists")
                            .document(docId)
                            .set(wishlist)
                            .addOnSuccessListener(unused -> {
                                Log.d(TAG, "✓ Wishlist saved successfully!");

                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        Toast.makeText(getContext(), "تم إنشاء القائمة بنجاح ✓", Toast.LENGTH_SHORT).show();

                                        // Clear form
                                        occasionNameInput.setText("");
                                        notesInput.setText("");
                                        dateText.setText("اختر التاريخ");
                                        dateText.setTextColor(getResources().getColor(R.color.grey));
                                        imagePreview.setVisibility(View.GONE);
                                        imagePlaceholder.setVisibility(View.VISIBLE);
                                        selectedImageUri = null;
                                    });
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "✗ Failed to save wishlist: " + e.getMessage(), e);
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() ->
                                            Toast.makeText(getContext(), "خطأ في حفظ القائمة: " + e.getMessage(), Toast.LENGTH_LONG).show());
                                }
                            });

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to count existing wishlists: " + e.getMessage(), e);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "خطأ في إنشاء معرف القائمة", Toast.LENGTH_LONG).show());
                    }
                });
    }
}