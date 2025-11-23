package com.example.widdy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class EnterCodeActivity extends AppCompatActivity {

    private static final String TAG = "EnterCodeActivity";

    EditText codeInput;
    Button checkCodeButton;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_code);

        db = FirebaseFirestore.getInstance();

        codeInput = findViewById(R.id.codeInput);
        checkCodeButton = findViewById(R.id.btnContinue);

        checkCodeButton.setOnClickListener(v -> {
            String code = codeInput.getText().toString().trim();

            if (code.isEmpty()) {
                Toast.makeText(this, "أدخل كود القائمة", Toast.LENGTH_SHORT).show();
                return;
            }

            checkCodeButton.setEnabled(false);

            // 🛑 نبدأ المحاولة الأولى
            attemptSearchAsLong(code);
        });
    }

    // المحاولة رقم 1: البحث كرقم (Long)
    private void attemptSearchAsLong(String codeStr) {
        Log.d(TAG, "Attempt 1: Searching as LONG for: " + codeStr);

        long codeLong;
        try {
            codeLong = Long.parseLong(codeStr);
        } catch (NumberFormatException e) {
            // لو ما قبل يتحول رقم، نجرب نبحث كنص مباشرة
            attemptSearchAsString(codeStr);
            return;
        }

        db.collectionGroup("wishlists")
                .whereEqualTo("accessCode", codeLong)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Log.d(TAG, "FOUND as Long!");
                        processResult((QueryDocumentSnapshot) querySnapshot.getDocuments().get(0));
                    } else {
                        Log.d(TAG, "Not found as Long. Trying as String...");
                        // 🛑 إذا فشل الرقم، نجرب النص فوراً
                        attemptSearchAsString(codeStr);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching as Long", e);
                    attemptSearchAsString(codeStr);
                });
    }

    // المحاولة رقم 2: البحث كنص (String)
    private void attemptSearchAsString(String codeStr) {
        Log.d(TAG, "Attempt 2: Searching as STRING for: " + codeStr);

        db.collectionGroup("wishlists")
                .whereEqualTo("accessCode", codeStr)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Log.d(TAG, "FOUND as String!");
                        processResult((QueryDocumentSnapshot) querySnapshot.getDocuments().get(0));
                    } else {
                        // 🛑 إذا فشل الاثنين، الحل الأخير: طباعة التشخيص
                        Log.d(TAG, "Not found as String either.");
                        debugPrintAllWishlists();
                        Toast.makeText(this, "الكود غير صحيح (تأكد من الرقم)", Toast.LENGTH_SHORT).show();
                        checkCodeButton.setEnabled(true);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching as String", e);
                    Toast.makeText(this, "خطأ في الاتصال: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    checkCodeButton.setEnabled(true);
                });
    }

    // معالجة النتيجة ونقل المستخدم
    private void processResult(QueryDocumentSnapshot doc) {
        checkCodeButton.setEnabled(true);
        String wishlistId = doc.getId();
        String path = doc.getReference().getPath();
        String userId = extractUserIdFromPath(path);

        if (userId != null) {
            Log.d(TAG, "SUCCESS! Opening details for Owner: " + userId);
            Intent intent = new Intent(this, GuestGiftsActivity.class);
            intent.putExtra("ownerId", userId);
            intent.putExtra("wishlistId", wishlistId);
            intent.putExtra("guest", true);
            startActivity(intent);
//            finish();
        } else {
            Toast.makeText(this, "خطأ في هيكل البيانات", Toast.LENGTH_SHORT).show();
        }
    }

    // طباعة كل شيء في الكونسول عشان نعرف إيش المشكلة
    private void debugPrintAllWishlists() {
        Log.d(TAG, "DEBUGGING: Fetching ANY wishlist to see data types...");
        db.collectionGroup("wishlists").limit(3).get()
                .addOnSuccessListener(qs -> {
                    for (QueryDocumentSnapshot d : qs) {
                        Log.d(TAG, "Found Doc ID: " + d.getId());
                        Log.d(TAG, "Data: " + d.getData());
                        Object val = d.get("accessCode");
                        if (val != null) {
                            Log.d(TAG, "accessCode Type: " + val.getClass().getName());
                            Log.d(TAG, "accessCode Value: " + val);
                        } else {
                            Log.d(TAG, "accessCode is NULL!");
                        }
                    }
                });
    }

    private String extractUserIdFromPath(String path) {
        try {
            String[] parts = path.split("/");
            if (parts.length >= 2 && parts[0].equals("users")) {
                return parts[1];
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
}