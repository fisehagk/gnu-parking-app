package com.example.parkgmu;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.parkgmu.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.time.LocalDateTime;

public class UserProfileActivity extends FragmentActivity implements OnMapReadyCallback {
    private final LatLng GMU_POSITION = new LatLng(38.83229, -77.30859);
    private final int YELLOW_MARKER = R.drawable.yellow_marker;
    private GoogleMap userMap;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;
    TextView signOutBtn, myParkingBtn, firstName, lastName, email;
    ImageView closeBtn, userAvatar;
    String userMarkerId;
    MyMarker userMarker;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        context = this;

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.user_map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        signOutBtn = findViewById(R.id.sign_out_btn);
        myParkingBtn = findViewById(R.id.user_parking_location_btn);
        myParkingBtn.setEnabled(false);
        firstName = findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);
        email = findViewById(R.id.user_email);
        closeBtn = findViewById(R.id.close_btn);
        userAvatar = findViewById(R.id.profile_img);

        //xml reference stopped working for some reason so setting some image references programmatically
        userAvatar.setImageResource(R.drawable.user_profile_avatar);
        closeBtn.setImageResource(R.drawable.ic_close);
        setCompoundDrawableToTextView(R.drawable.ic_logout, signOutBtn);


        // Set user information in the profile activity
        setUserInfo();

        // Go back to maps activity
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapsIntent = new Intent(UserProfileActivity.this, MapsActivity.class);

                //Start Maps activity
                startActivity(mapsIntent);
                finish();
            }
        });

        // Handle Sign Out button
        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent mapsIntent = new Intent(UserProfileActivity.this, MapsActivity.class);

                //Start Maps activity
                startActivity(mapsIntent);
                finish();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        userMap = googleMap;
        userMap.getUiSettings().setZoomControlsEnabled(true);
        userMap.animateCamera(CameraUpdateFactory.newLatLngZoom(GMU_POSITION, 14.0f));
    }

    public void goToMyCar(View view) {
        double destLatitude = userMarker.getPosition().latitude;
        double destLongitude = userMarker.getPosition().longitude;

        // Create a URI for the destination coordinates
        String uri = "http://maps.google.com/maps?saddr=" + "&daddr=" + destLatitude + "," + destLongitude;

        // Create an Intent with the ACTION_VIEW action and the URI
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps"); // Use Google Maps specifically

        // Check if the Google Maps app is installed
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent); // Start the intent
        } else {
            // If Google Maps is not installed, provide an alternative solution or notify the user
            // For instance, open the browser with Google Maps web URL
            Uri webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + destLatitude + "," + destLongitude);
            Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
            startActivity(webIntent);
        }

    }

    // Set current user profile info
    private void setUserInfo() {
        DocumentReference userRef = db.collection("users").document(user.getUid());
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    firstName.setText(document.getString("firstName"));
                    lastName.setText(document.getString("lastName"));
                    email.setText(document.getString("email"));
                    userMarkerId = document.getString("myMarkerId");
                    if (userMarkerId != null) {
                        myParkingBtn.setEnabled(true);
                        getCurrentUserMarker();
                    }
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    // Get the user associated marker
    private void getCurrentUserMarker() {
        DocumentReference docRef = db.collection("markers").document(userMarkerId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    GeoPoint geoPoint = document.getGeoPoint("position");
                    LatLng position = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                    String title = "My Parking: " + position;
                    String userId = document.getString("userId");
                    String markerId = userMarkerId;
                    Timestamp parkingTime = document.getTimestamp("time");
                    userMarker = new MyMarker(position, title, userId, markerId, parkingTime, context, YELLOW_MARKER);
                    if (userMap != null) {
                        Marker marker = userMap.addMarker(new MarkerOptions()
                                .position(userMarker.getPosition())
                                .title(userMarker.getTitle())
                                .icon(userMarker.getIcon()));

                        userMarker.setMarker(marker);
                        userMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userMarker.getPosition(), 14.0f));
                    }
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    // Utility method to set drawable to TextView
    private void setCompoundDrawableToTextView(int drawableResId, TextView textView) {
        Drawable drawable = getResources().getDrawable(drawableResId);
        textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
    }
}