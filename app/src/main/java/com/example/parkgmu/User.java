package com.example.parkgmu;

import com.google.android.gms.maps.model.LatLng;

public class User {
    private String firstName;
    private String lastName;
    private String email;
    private String myMarkerId;

    public User(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
    public User(String firstName, String lastName, String email, String markerId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.myMarkerId = markerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMyMarkerId() {
        return myMarkerId;
    }

    public void setMyMarkerId(String markerId) {
        this.myMarkerId = markerId;
    }
}
