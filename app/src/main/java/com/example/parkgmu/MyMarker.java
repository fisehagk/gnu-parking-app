package com.example.parkgmu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.Timestamp;

import java.time.LocalDateTime;

public class MyMarker {
    private LatLng position;
    private String title;
    private String userId;
    private String markerId;
    private Timestamp timestamp;
    private BitmapDescriptor icon;
    private Marker marker;

    public MyMarker(LatLng position, String title, String userId, String markerId, Timestamp timestamp, Context context, int icon) {
        this.position = position;
        this.title = title;
        this.userId = userId;
        this.markerId = markerId;
        this.timestamp = timestamp;
        this.icon = getMarkerIconFromDrawableId(context, icon);
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMarkerId() { return markerId; }

    public void setMarkerId(String markerId) { this.markerId = markerId; }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public BitmapDescriptor getIcon() {
        return icon;
    }

    public void setIcon(Context context, int icon) {
        this.icon = getMarkerIconFromDrawableId(context, icon);
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    private BitmapDescriptor getMarkerIconFromDrawableId(Context context, int drawableResourceId) {
        Bitmap bitmap;

        // Convert the drawable resource ID to a Bitmap
        Drawable drawable = context.getResources().getDrawable(drawableResourceId);
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // Create a Canvas and draw the drawable onto the Bitmap
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        // Return the BitmapDescriptor for the Bitmap
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }



}
