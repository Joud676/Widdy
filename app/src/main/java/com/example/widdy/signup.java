package com.example.widdy;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

public class signup extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText fullName, email, password, confirmPassword;
    private Button signupBtn;
    private TextView loginText;
    private ImageView backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        fullName = findViewById(R.id.fullName);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        signupBtn = findViewById(R.id.signupBtn);
        loginText = findViewById(R.id.loginText);
        backBtn = findViewById(R.id.backBtn);

        // back button
        backBtn.setOnClickListener(v -> finish());

        // go to login page
        loginText.setOnClickListener(v -> {
            Intent i = new Intent(signup.this, login.class);
            startActivity(i);
        });

        // sign up button
        signupBtn.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {

        String name = fullName.getText().toString().trim();
        String mail = email.getText().toString().trim();
        String pass = password.getText().toString().trim();
        String confirm = confirmPassword.getText().toString().trim();

        // input verification
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

        // Firebase Authentication
        mAuth.createUserWithEmailAndPassword(mail, pass).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                // نجاح
                Toast.makeText(signup.this, "تم إنشاء الحساب بنجاح", Toast.LENGTH_LONG).show();

                // انتقال إلى الـ Home Page
                Intent intent = new Intent(signup.this, HomePageActivity.class);
                startActivity(intent);
                finish();
            } else {
                // فشل
                Toast.makeText(signup.this,
                        task.getException().getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}