package com.example.parkgmu;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.parkgmu.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private final int BLUE_MARKER = R.drawable.blue_gmu_marker;
    private final int GREEN_MARKER = R.drawable.green_marker;
    private final int YELLOW_MARKER = R.drawable.yellow_marker;
    private final LatLng GMU_POSITION = new LatLng(38.83229, -77.30859);
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private TextView reloadMap, profile, checkinCheckout;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private String currentUserMarkerId;
    private MyMarker userMarker;
    private Map<String, MyMarker> markerHashMap;
    private String selectedMarkerId = null;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        context = this;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        markerHashMap = new HashMap<>();

        // Set the drawable to the TextView
        reloadMap = findViewById(R.id.reload_map_btn);
        profile = findViewById(R.id.profile_btn);
        checkinCheckout = findViewById(R.id.checkin_checkout_btn);
        checkinCheckout.setEnabled(false);

        setCompoundDrawableToTextView(R.drawable.ic_restart_alt_24, reloadMap);
        setCompoundDrawableToTextView(R.drawable.ic_manage_accounts_24, profile);
        setCompoundDrawableToTextView(R.drawable.ic_person_pin_24, checkinCheckout);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if (user != null) {
                    intent = new Intent(MapsActivity.this, UserProfileActivity.class);
                } else {
                    intent = new Intent(MapsActivity.this, SignInActivity.class);
                }
                startActivity(intent);
                finish();
            }
        });
    }

    // Utility method to set drawable to TextView
    private void setCompoundDrawableToTextView(int drawableResId, TextView textView) {
        Drawable drawable = getResources().getDrawable(drawableResId);
        textView.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(GMU_POSITION, 14.0f));
        if (user != null) {
            getCurrentUserMarkerId();
        }
        else {
            String title = "GMU: " + GMU_POSITION;
            MyMarker gmuMarker = new MyMarker(GMU_POSITION, title, null, "GMU marker", null, context, BLUE_MARKER);
            addMarkerToMap(gmuMarker, false);
        }

        // Set an OnMarkerClickListener to handle marker clicks
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // Retrieve the corresponding MyMarker object based on the clicked marker's ID
                for (Map.Entry<String, MyMarker> entry : markerHashMap.entrySet()) {
                    MyMarker myMarker = entry.getValue();
                    if (myMarker.getMarker().equals(marker)) {
                        selectedMarkerId = entry.getKey();
                        Log.d("MarkerClicked", "Marker ID: " + selectedMarkerId + ", MyMarker details: " + myMarker);
                        break;
                    }
                }
                return false; // Return false to indicate that we haven't consumed the event
            }
        });

        // Set an OnMapClickListener to handle clicks on the map
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // Deselect the selected marker when clicking on the map
                selectedMarkerId = null;
                Log.d("MarkerClicked", "Marker ID: " + selectedMarkerId);
            }
        });
    }

    public void reloadMap(View view) {
        if (userMarker != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userMarker.getPosition(), 14.0f));
        }
        else {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(GMU_POSITION, 14.0f));
        }
    }

    // Check-In and Check-Out functionality handling
    public void handleCheckInCheckOut(View view) {
        if (currentUserMarkerId != null) {
            // Handle Check-Out
            checkOut();
            userMarker.getMarker().remove();
            currentUserMarkerId = null;
            userMarker = null;
            handleUserMarker();
        }
        else if (selectedMarkerId != null) {
            // Handle Check-In
            checkIn();
            currentUserMarkerId = selectedMarkerId;
            cleanUp();
            handleUserMarker();
        }
    }

    private void addMarkerToMap(MyMarker newMarker, boolean markerFocus) {
        if (mMap != null) {
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(newMarker.getPosition())
                    .title(newMarker.getTitle())
                    .icon(newMarker.getIcon()));

            newMarker.setMarker(marker);

            if (markerFocus) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newMarker.getPosition(), 14.0f));
            }
            else {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(GMU_POSITION, 14.0f));
            }
        }

    }

    private void handleUserMarker() {
        Boolean focusOnDefaultMarker;
        if (currentUserMarkerId != null) {
            checkinCheckout.setEnabled(true);
            checkinCheckout.setText("Check-Out");
            focusOnDefaultMarker = true;
            getCurrentUserMarker();
        } else {
            checkinCheckout.setEnabled(true);
            checkinCheckout.setText("Check-In");
            focusOnDefaultMarker = false;
            getAllMarkers();
        }
        String title = "GMU: " + GMU_POSITION;
        MyMarker gmuMarker = new MyMarker(GMU_POSITION, title, null, "gmuMarker", Timestamp.now(), context, BLUE_MARKER);
        addMarkerToMap(gmuMarker, focusOnDefaultMarker);
    }

    private void getCurrentUserMarkerId() {
        DocumentReference userRef = db.collection("users").document(user.getUid());
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    currentUserMarkerId = document.getString("myMarkerId");
                    handleUserMarker();
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    private void getCurrentUserMarker() {
        DocumentReference docRef = db.collection("markers").document(currentUserMarkerId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    GeoPoint geoPoint = document.getGeoPoint("position");
                    LatLng position = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                    String title = "My Parking: " + position;
                    String userId = document.getString("userId");
                    String markerId = currentUserMarkerId;
                    Timestamp parkingTime = document.getTimestamp("time");
                    userMarker = new MyMarker(position, title, userId, markerId, parkingTime, context, YELLOW_MARKER);
                    addMarkerToMap(userMarker, true);
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    private void getAllMarkers() {
        db.collection("markers")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Retrieve marker details from Firestore document
                            GeoPoint geoPoint = document.getGeoPoint("position");
                            LatLng position = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                            String title = "Open Parking: " + position;
                            String userId = document.getString("userId");
                            String markerId = document.getId();
                            Timestamp parkingTime = document.getTimestamp("time");

                            // Create MyMarker object for each retrieved marker
                            MyMarker marker = new MyMarker(position, title, userId, markerId, parkingTime, context, GREEN_MARKER);

                            // Add markers that are not associated with a user (open parking markers only)
                            if (userId == null) {
                                markerHashMap.put(document.getId(), marker);
                                addMarkerToMap(marker, false);
                            }
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    // Handle Check-Out
    private void checkOut() {
        // Handle Check-Out
        // Disassociate the user from the marker in Firestore db
        DocumentReference markerUserRef = db.collection("markers").document(currentUserMarkerId);
        markerUserRef
                .update("userId", null)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });

        // Disassociate the marker from the user in Firestore db
        DocumentReference userMarkerRef = db.collection("users").document(user.getUid());
        userMarkerRef
                .update("myMarkerId", null)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
    }

    // Handle Check-In
    private void checkIn() {
        // Handle Check-In
        // Associate the marker with the user in Firestore db
        DocumentReference markerUserRef = db.collection("markers").document(selectedMarkerId);
        markerUserRef
                .update("userId", user.getUid())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });

        // Associate the user with the marker in Firestore db
        DocumentReference userMarkerRef = db.collection("users").document(user.getUid());
        userMarkerRef
                .update("myMarkerId", selectedMarkerId)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
    }
    // Clean-up: Remove all markers from map
    private void cleanUp() {
        if (mMap != null) {
            for (MyMarker marker : markerHashMap.values()) {
                marker.getMarker().remove();
            }
        }
    }
}


