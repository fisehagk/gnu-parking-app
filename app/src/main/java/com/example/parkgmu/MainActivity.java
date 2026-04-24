package com.example.parkgmu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private WebView webView;
    Button signUpBtn, signInBtn, goToMapBtn;

    @Override
    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            //System.out.println("Currently signed in user: " + currentUser.getEmail());
            Intent mapIntent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(mapIntent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase and use of Firebase Emulator
        FirebaseApp.initializeApp(this);

        setContentView(R.layout.activity_main);

        signUpBtn = findViewById(R.id.sign_up_btn);
        signInBtn = findViewById(R.id.sign_in_btn);
        goToMapBtn = findViewById(R.id.map_btn);

        webView = findViewById(R.id.webview);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.setInitialScale(1);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                view.setInitialScale(1);
                view.getSettings().setLoadWithOverviewMode(true);
                view.getSettings().setUseWideViewPort(true);
            }
        });
        webView.loadUrl("file:///android_res/raw/background_img2.gif");

        // Start SignIn activity
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = new Intent(MainActivity.this, SignInActivity.class);

                //Start SignIn activity
                startActivity(signInIntent);
                finish();
            }
        });

        // Start SignIn activity
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUpIntent = new Intent(MainActivity.this, SignUpActivity.class);

                //Start SignIn activity
                startActivity(signUpIntent);
                finish();
            }
        });
        // Start Maps activity
        goToMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(mapIntent);
                finish();
            }
        });
    }
}
