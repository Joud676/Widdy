package com.example.widdy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.widdy.model.GiftModel;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddGiftFragment extends Fragment {

    private static final String TAG = "AddGiftFragment";

    private ImageView backButton, imagePreview;
    private EditText giftNameInput, expectedPriceInput, shortDescriptionInput, productLinkInput;
    private TextView locationText;
    private MaterialCardView imageCard, locationCard;
    private LinearLayout imagePlaceholder;
    private Spinner prioritySpinner;
    private Button saveGiftBtn;

    private Uri selectedImageUri = null;
    private String selectedLocation = "";
    private String wishlistId;
    private String userId;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        imagePreview.setImageURI(selectedImageUri);
                        imagePreview.setVisibility(View.VISIBLE);
                        imagePlaceholder.setVisibility(View.GONE);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> pickLocationLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    double lat = result.getData().getDoubleExtra("lat", 0);
                    double lng = result.getData().getDoubleExtra("lng", 0);
                    selectedLocation = lat + "," + lng;
                    locationText.setText("تم اختيار الموقع");
                    locationText.setTextColor(getResources().getColor(R.color.black));
                }
            });

    public AddGiftFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();
        }

        if (getArguments() != null) {
            wishlistId = getArguments().getString("wishlistId");
        }

        if (wishlistId == null || wishlistId.isEmpty()) {
            Toast.makeText(getContext(), "خطأ: لم يتم تحديد القائمة", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) getActivity().onBackPressed();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_gift, container, false);
        initViews(view);
        setupListeners();
        return view;
    }

    private void initViews(View view) {
        backButton = view.findViewById(R.id.backButton);
        giftNameInput = view.findViewById(R.id.giftNameInput);
        imageCard = view.findViewById(R.id.imageCard);
        imagePlaceholder = view.findViewById(R.id.imagePlaceholder);
        imagePreview = view.findViewById(R.id.imagePreview);
        expectedPriceInput = view.findViewById(R.id.expectedPriceInput);
        shortDescriptionInput = view.findViewById(R.id.shortDescriptionInput);
        locationCard = view.findViewById(R.id.locationCard);
        locationText = view.findViewById(R.id.locationText);
        productLinkInput = view.findViewById(R.id.productLinkInput);
        prioritySpinner = view.findViewById(R.id.prioritySpinner);
        saveGiftBtn = view.findViewById(R.id.saveGiftBtn);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });
        imageCard.setOnClickListener(v -> pickImage());
        locationCard.setOnClickListener(v -> openLocationPicker());
        saveGiftBtn.setOnClickListener(v -> saveGift());
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    private void openLocationPicker() {
        Intent intent = new Intent(getContext(), PickLocationActivity.class);
        pickLocationLauncher.launch(intent);
    }

    private void saveGift() {
        String name = giftNameInput.getText().toString().trim();
        String price = expectedPriceInput.getText().toString().trim();
        String description = shortDescriptionInput.getText().toString().trim();
        String link = productLinkInput.getText().toString().trim();
        String priority = prioritySpinner.getSelectedItem().toString();

        if (name.isEmpty()) {
            Toast.makeText(getContext(), "أدخل اسم الهدية", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "اختر صورة الهدية", Toast.LENGTH_SHORT).show();
            return;
        }

        if (price.isEmpty()) {
            Toast.makeText(getContext(), "أدخل السعر المتوقع", Toast.LENGTH_SHORT).show();
            return;
        }

        saveGiftBtn.setEnabled(false);
        saveGiftBtn.setText("جاري الحفظ...");

        uploadImageToImgBB(name, price, description, link, priority);
    }

    private void uploadImageToImgBB(String name, String price, String description, String link, String priority) {
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
                    .addFormDataPart("image", "gift.jpg",
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
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "فشل رفع الصورة", Toast.LENGTH_SHORT).show();
                            saveGiftBtn.setEnabled(true);
                            saveGiftBtn.setText("إضافة الهدية");
                        });
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "فشل رفع الصورة", Toast.LENGTH_SHORT).show();
                                saveGiftBtn.setEnabled(true);
                                saveGiftBtn.setText("إضافة الهدية");
                            });
                        }
                        return;
                    }

                    String respStr = response.body().string();
                    String imageUrl = extractImageUrl(respStr);

                    if (imageUrl.isEmpty()) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "فشل استخراج رابط الصورة", Toast.LENGTH_SHORT).show();
                                saveGiftBtn.setEnabled(true);
                                saveGiftBtn.setText("إضافة الهدية");
                            });
                        }
                        return;
                    }

                    saveGiftToFirestore(name, imageUrl, price, description, link, priority);
                }
            });

        } catch (Exception e) {
            Toast.makeText(getContext(), "حدث خطأ", Toast.LENGTH_SHORT).show();
            saveGiftBtn.setEnabled(true);
            saveGiftBtn.setText("إضافة الهدية");
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

    private void saveGiftToFirestore(String name, String imageUrl, String price,
                                     String description, String link, String priority) {

        db.collection("users")
                .document(userId)
                .collection("wishlists")
                .document(wishlistId)
                .collection("gifts")
                .get()
                .addOnSuccessListener(snapshots -> {

                    int maxNumber = 0;
                    Pattern pattern = Pattern.compile("^gift(\\d+)$");

                    for (int i = 0; i < snapshots.size(); i++) {
                        String docId = snapshots.getDocuments().get(i).getId();
                        Matcher matcher = pattern.matcher(docId);
                        if (matcher.matches()) {
                            int number = Integer.parseInt(matcher.group(1));
                            if (number > maxNumber) maxNumber = number;
                        }
                    }

                    int newNumber = maxNumber + 1;
                    String giftId = "gift" + newNumber;

                    GiftModel gift = new GiftModel(
                            giftId,
                            name,
                            imageUrl,
                            price,
                            description,
                            selectedLocation,
                            link,
                            priority,
                            System.currentTimeMillis()
                    );

                    Map<String, Object> giftData = new HashMap<>();
                    giftData.put("ID", gift.getID());
                    giftData.put("name", gift.getName());
                    giftData.put("imageUrl", gift.getImageUrl());
                    giftData.put("expectedPrice", gift.getExpectedPrice());
                    giftData.put("description", gift.getDescription());
                    giftData.put("storeLocation", gift.getStoreLocation());
                    giftData.put("productLink", gift.getProductLink());
                    giftData.put("priority", gift.getPriority());
                    giftData.put("createdAt", gift.getCreatedAt());
                    giftData.put("isReserved", gift.isReserved());
                    giftData.put("reservedBy", gift.getReservedBy());

                    db.collection("users")
                            .document(userId)
                            .collection("wishlists")
                            .document(wishlistId)
                            .collection("gifts")
                            .document(giftId)
                            .set(giftData)
                            .addOnSuccessListener(unused -> {
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        Toast.makeText(getContext(), "تم إضافة الهدية بنجاح ✓", Toast.LENGTH_SHORT).show();
                                        getActivity().setResult(getActivity().RESULT_OK);
                                        getActivity().onBackPressed();
                                    });
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        Toast.makeText(getContext(), "خطأ في الحفظ", Toast.LENGTH_SHORT).show();
                                        saveGiftBtn.setEnabled(true);
                                        saveGiftBtn.setText("إضافة الهدية");
                                    });
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "خطأ في الحفظ", Toast.LENGTH_SHORT).show();
                            saveGiftBtn.setEnabled(true);
                            saveGiftBtn.setText("إضافة الهدية");
                        });
                    }
                });
    }
}
