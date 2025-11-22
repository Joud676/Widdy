package com.example.widdy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private static final String TAG = "Login";

    private FirebaseAuth mAuth;
    private EditText et_email, et_password;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            Log.d(TAG, "User already logged in and verified, redirecting to home");
            startActivity(new Intent(Login.this, HomePageActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        et_email = findViewById(R.id.email_Login);
        et_password = findViewById(R.id.password_Login);
        btnLogin = findViewById(R.id.btnLogin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        if (intent.hasExtra("email")) {
            et_email.setText(intent.getStringExtra("email"));
        }
        if (intent.getBooleanExtra("showVerificationMessage", false)) {
            Toast.makeText(this, "تحقق من بريدك الإلكتروني لتفعيل الحساب", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            Log.d(TAG, "User already logged in and verified in onStart");
            startActivity(new Intent(Login.this, HomePageActivity.class));
            finish();
        }
    }

    public void sinupPage(View view){
        Intent intent = new Intent(this, signup.class);
        startActivity(intent);
    }

    public void loginMethod(View view) {
        String email = et_email.getText().toString().trim();
        String password = et_password.getText().toString().trim();
        btnLogin.setEnabled(false);

        if(email.isEmpty()){
            et_email.setError("الرجاء أدخال أيميل");
            et_email.requestFocus();
            btnLogin.setEnabled(true);
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            et_email.setError("الرجاء أدخال أيميل صحيح");
            et_email.requestFocus();
            btnLogin.setEnabled(true);
            return;
        }
        if(password.isEmpty() || password.length() < 6){
            et_password.setError("الرجاء أدخال كلمة مرور مكونة من 6 خانات");
            et_password.requestFocus();
            btnLogin.setEnabled(true);
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){
                    FirebaseUser user = mAuth.getCurrentUser();

                    if (user != null) {
                        if (user.isEmailVerified()) {
                            Log.d(TAG, "Email verified, login successful");
                            Toast.makeText(getApplicationContext(), "تم تسجيل الدخول بنجاح", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Login.this, HomePageActivity.class));
                            finish();
                        } else {
                            Log.d(TAG, "Email not verified");
                            mAuth.signOut();
                            showVerificationDialog(user);
                            btnLogin.setEnabled(true);
                        }
                    } else {
                        btnLogin.setEnabled(true);
                    }
                }
                else{
                    Log.e(TAG, "Login failed: " + task.getException().getMessage());
                    Toast.makeText(getApplicationContext(), "البريد الإلكتروني أو كلمة المرور غير صحيحة", Toast.LENGTH_LONG).show();
                    btnLogin.setEnabled(true);
                }
            }
        });
    }

    private void showVerificationDialog(FirebaseUser user) {
        new AlertDialog.Builder(this)
                .setTitle("تفعيل البريد الإلكتروني")
                .setMessage("يجب تفعيل بريدك الإلكتروني أولاً. هل تريد إعادة إرسال رسالة التفعيل؟")
                .setPositiveButton("إعادة الإرسال", (dialog, which) -> {
                    user.sendEmailVerification()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(Login.this, "تم إرسال رسالة التفعيل. تحقق من بريدك الإلكتروني", Toast.LENGTH_LONG).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(Login.this, "حدث خطأ: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }
}