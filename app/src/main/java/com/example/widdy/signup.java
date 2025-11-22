package com.example.widdy;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class signup extends AppCompatActivity {

    private static final String TAG = "SignUp";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText fullName, email, password, confirmPassword;
    private Button signupBtn;
    private TextView loginText;
    private ImageView backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        fullName = findViewById(R.id.fullName);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        signupBtn = findViewById(R.id.signupBtn);
        loginText = findViewById(R.id.loginText);
        backBtn = findViewById(R.id.backBtn);

        backBtn.setOnClickListener(v -> finish());

        loginText.setOnClickListener(v -> {
            Intent i = new Intent(signup.this, Login.class);
            startActivity(i);
        });

        signupBtn.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {

        String name = fullName.getText().toString().trim();
        String mail = email.getText().toString().trim();
        String pass = password.getText().toString().trim();
        String confirm = confirmPassword.getText().toString().trim();

        if (name.isEmpty()) {
            fullName.setError("يرجى إدخال الاسم");
            fullName.requestFocus();
            return;
        }

        if (mail.isEmpty()) {
            email.setError("يرجى إدخال البريد الإلكتروني");
            email.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            email.setError("البريد الإلكتروني غير صالح");
            email.requestFocus();
            return;
        }

        if (pass.isEmpty() || pass.length() < 6) {
            password.setError("كلمة المرور يجب أن تكون 6 أحرف على الأقل");
            password.requestFocus();
            return;
        }

        if (!pass.equals(confirm)) {
            confirmPassword.setError("كلمات المرور غير متطابقة");
            confirmPassword.requestFocus();
            return;
        }

        signupBtn.setEnabled(false);
        Log.d(TAG, "Starting user registration...");

        mAuth.createUserWithEmailAndPassword(mail, pass).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();

                if (user != null) {
                    String uid = user.getUid();
                    Log.d(TAG, "Auth successful for UID: " + uid);

                    user.sendEmailVerification()
                            .addOnSuccessListener(unused -> {
                                Log.d(TAG, "Verification email sent successfully");

                                Map<String, Object> userData = new HashMap<>();
                                userData.put("name", name);
                                userData.put("email", mail);
                                userData.put("emailVerified", false);
                                userData.put("createdAt", System.currentTimeMillis());

                                db.collection("users")
                                        .document(uid)
                                        .set(userData)
                                        .addOnSuccessListener(unused2 -> {
                                            Log.d(TAG, "User data saved successfully");

                                            Toast.makeText(signup.this,
                                                    "تم إنشاء الحساب! تحقق من بريدك الإلكتروني للتفعيل",
                                                    Toast.LENGTH_LONG).show();

                                            mAuth.signOut();

                                            Intent intent = new Intent(signup.this, Login.class);
                                            intent.putExtra("email", mail);
                                            intent.putExtra("showVerificationMessage", true);
                                            startActivity(intent);
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Failed to save user data: " + e.getMessage());
                                            Toast.makeText(signup.this,
                                                    "حدث خطأ في حفظ البيانات: " + e.getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                            signupBtn.setEnabled(true);
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to send verification email: " + e.getMessage());
                                Toast.makeText(signup.this,
                                        "حدث خطأ في إرسال رسالة التحقق: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                                signupBtn.setEnabled(true);
                            });
                } else {
                    Log.e(TAG, "User is null after successful auth");
                    signupBtn.setEnabled(true);
                }

            } else {
                Log.e(TAG, "Auth failed: " + task.getException().getMessage());
                Toast.makeText(signup.this,
                        task.getException().getMessage(),
                        Toast.LENGTH_LONG).show();
                signupBtn.setEnabled(true);
            }
        });
    }
}