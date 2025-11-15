package com.example.widdy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {
    // FirebaseAuth object
    private FirebaseAuth mAuth;

    // UI elements
    private EditText et_email, et_password;
    private Button btnLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        et_email = findViewById(R.id.email_Login);
        et_password = findViewById(R.id.password_Login);
        btnLogin = findViewById(R.id.btnLogin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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
            et_email.setError("الرجاء أدخال أيميل ");
            et_email.requestFocus();
            btnLogin.setEnabled(true);
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            et_email.setError("الرجاء أدخال أيميل صحيح ");
            et_email.requestFocus();
            btnLogin.setEnabled(true);
            return;
        }
        if(password.isEmpty() || password.length() < 6){ //you can also check the num. of chars.
            et_password.setError("الرجاء أدخال كلمة مرور مكونة من 6 خانات ");
            et_password.requestFocus();
            btnLogin.setEnabled(true);
            return;
        }
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                btnLogin.setEnabled(true);
                if (task.isSuccessful()){
                    //redirect to the mainpage
                    Toast.makeText(getApplicationContext(), "تم تسجيل الدخول بنجاح", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Login.this, HomePageActivity.class));
                    finish(); // Close login activity
                }
                else{
                    Toast.makeText(getApplicationContext(), task.getException().toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}