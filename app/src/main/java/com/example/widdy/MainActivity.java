package com.example.widdy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        VideoView videoView = findViewById(R.id.splashScreen);
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.widdy_splashscreen);
        videoView.setVideoURI(videoUri);

        videoView.setOnPreparedListener(mp -> {
            mp.setVolume(0f, 0f);
            videoView.start();
        });


        videoView.setOnCompletionListener(mp -> {
        startActivity(new Intent(this, Login.class));
        finish();
    });
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }



}