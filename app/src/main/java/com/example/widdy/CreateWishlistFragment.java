package com.example.widdy;

import static android.content.ContentValues.TAG;

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
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateWishlistFragment extends Fragment {

    private static final int IMAGE_PICK_CODE = 100;

    EditText occasionNameInput, notesInput;
    TextView dateText;
    ImageView imagePreview, backButton;
    LinearLayout imagePlaceholder;
    MaterialCardView dateCard, imageCard;
    Button createWishlistBtn;

    Uri selectedImageUri = null;
    FirebaseFirestore db;
    FirebaseAuth auth;

    private String fromPage = "home";

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
        backButton = view.findViewById(R.id.backButton);

        if (getArguments() != null) {
            fromPage = getArguments().getString("fromPage", "home");
        }

        dateCard.setOnClickListener(v -> openDatePicker());
        imageCard.setOnClickListener(v -> pickImage());
        createWishlistBtn.setOnClickListener(v -> saveWishlist());
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

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

    private long generateAccessCode() {
        Random random = new Random();
        return 100000L + random.nextInt(900000);
    }

    private void generateUniqueAccessCode(OnCodeGeneratedListener listener) {
        long code = generateAccessCode();
        db.collection("users")
                .document(auth.getCurrentUser().getUid())
                .collection("wishlists")
                .document(String.valueOf(code))
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        listener.onCodeGenerated(code);
                    } else {
                        generateUniqueAccessCode(listener);
                    }
                })
                .addOnFailureListener(e -> listener.onCodeGenerated(code));
    }

    interface OnCodeGeneratedListener {
        void onCodeGenerated(long code);
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

        if (name.isEmpty() || date.equals("اختر التاريخ") || selectedImageUri == null) {
            Toast.makeText(getContext(), "اكمل جميع البيانات", Toast.LENGTH_SHORT).show();
            return;
        }

        createWishlistBtn.setText("جاري الحفظ...");
        createWishlistBtn.setEnabled(false);

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

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
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
                                Toast.makeText(getContext(), "فشل رفع الصورة: " + response.code(), Toast.LENGTH_SHORT).show();
                                resetButton();
                            });
                        }
                        return;
                    }

                    try {
                        String respStr = response.body().string();
                        String imageUrl = extractImageUrl(respStr);

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() ->
                                    generateUniqueAccessCode(accessCode -> {
                                        Map<String, Object> wishlist = new HashMap<>();
                                        wishlist.put("name", name);
                                        wishlist.put("date", date);
                                        wishlist.put("notes", notes);
                                        wishlist.put("imageUrl", imageUrl);
                                        wishlist.put("userId", uid);
                                        wishlist.put("accessCode", accessCode);
                                        wishlist.put("itemCount", 0);
                                        wishlist.put("createdAt", System.currentTimeMillis());
                                        saveWishlistToFirestore(wishlist, String.valueOf(accessCode));
                                    })
                            );
                        }
                    } catch (Exception e) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "فشل قراءة الرد", Toast.LENGTH_SHORT).show();
                                resetButton();
                            });
                        }
                    }
                }
            });

        } catch (Exception e) {
            Toast.makeText(getContext(), "حدث خطأ أثناء رفع الصورة", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
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

    private void saveWishlistToFirestore(Map<String, Object> wishlist, String docId) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            resetButton();
            return;
        }

        String uid = currentUser.getUid();

        db.collection("users")
                .document(uid)
                .collection("wishlists")
                .document(docId)
                .set(wishlist)
                .addOnSuccessListener(unused -> {
                    resetButton();
                    Toast.makeText(getContext(), "تم إنشاء القائمة بنجاح ✓", Toast.LENGTH_SHORT).show();

                    if (getActivity() instanceof HomePageActivity) {
                        ((HomePageActivity) getActivity()).openWishlistDetails(docId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Save failed: " + e.getMessage());
                    Toast.makeText(getContext(), "خطأ في الحفظ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    resetButton();
                });
    }

    private void resetButton() {
        if (createWishlistBtn != null && getActivity() != null) {
            createWishlistBtn.setText("إنشاء القائمة");
            createWishlistBtn.setEnabled(true);
        }
    }
}