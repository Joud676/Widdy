package com.example.widdy;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

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

    EditText occasionNameInput, notesInput;
    TextView dateText;
    ImageView imagePreview;
    LinearLayout imagePlaceholder;
    MaterialCardView dateCard, imageCard;
    Button createWishlistBtn;

    Uri selectedImageUri = null;
    FirebaseFirestore db;
    FirebaseAuth auth;

    private static final int IMAGE_PICK_CODE = 100;

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

        uploadImageToImgBB(name, date, notes, uid);
    }

    private void uploadImageToImgBB(String name, String date, String notes, String uid) {
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
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "فشل رفع الصورة", Toast.LENGTH_SHORT).show());
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "فشل رفع الصورة", Toast.LENGTH_SHORT).show());
                        }
                        return;
                    }

                    String respStr = response.body().string();
                    String imageUrl = extractImageUrl(respStr);

                    Map<String, Object> wishlist = new HashMap<>();
                    wishlist.put("name", name);
                    wishlist.put("date", date);
                    wishlist.put("notes", notes);
                    wishlist.put("imageUrl", imageUrl);
                    wishlist.put("userId", uid);
                    wishlist.put("createdAt", System.currentTimeMillis());

                    saveWishlistWithNumber(wishlist);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "حدث خطأ أثناء رفع الصورة", Toast.LENGTH_SHORT).show();
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

    private void saveWishlistWithNumber(Map<String, Object> wishlist) {
        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentReference counterRef = db.collection("metadata").document("wishlistCounter");

            Long current = transaction.get(counterRef).getLong("counter");
            if (current == null) current = 0L;
            long newNumber = current + 1;

            transaction.update(counterRef, "counter", newNumber);

            String docId = "wishlist" + newNumber;
            DocumentReference wishlistRef = db.collection("wishlists").document(docId);
            transaction.set(wishlistRef, wishlist);

            return null;
        }).addOnSuccessListener(unused -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "تم إنشاء القائمة بنجاح", Toast.LENGTH_SHORT).show());
            }
        }).addOnFailureListener(e -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "خطأ في إنشاء القائمة", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
