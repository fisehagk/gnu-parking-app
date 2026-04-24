package com.example.parkgmu;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    EditText firstNameInput, lastNameInput, emailInput, passwordInput, passwordConfirm;
    TextView signInLink;
    Button signUpBtn;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase Auth & Firestore db
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        progressBar = findViewById(R.id.progress_bar);
        firstNameInput = findViewById(R.id.sign_up_first_name);
        firstNameInput.setCompoundDrawablePadding(20);
        lastNameInput = findViewById(R.id.sign_up_last_name);
        lastNameInput.setCompoundDrawablePadding(20);
        emailInput = findViewById(R.id.sign_up_email);
        emailInput.setCompoundDrawablePadding(20);
        passwordInput = findViewById(R.id.sign_up_pw);
        passwordInput.setCompoundDrawablePadding(20);
        passwordConfirm = findViewById(R.id.sign_up_pw_confirm);
        passwordConfirm.setCompoundDrawablePadding(20);
        signInLink = findViewById(R.id.sign_in_link);
        signUpBtn = findViewById(R.id.sign_up_actv_btn);

        // Go to Sign In page
        signInLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = new Intent(SignUpActivity.this, SignInActivity.class);

                //Start SignIn activity
                startActivity(signInIntent);
                finish();
            }
        });

        // Account sign in
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String firstName, lastName, email, password, confirmPw;

                firstName = String.valueOf(firstNameInput.getText());
                lastName = String.valueOf(lastNameInput.getText());
                email = String.valueOf(emailInput.getText());
                password = String.valueOf(passwordInput.getText());
                confirmPw = String.valueOf(passwordConfirm.getText());

                // Check for empty fields
                if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(email) ||
                        TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPw)) {
                    Snackbar.make(findViewById(android.R.id.content), "Please provide all required information.", Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (!password.equals(confirmPw)) {
                    Snackbar.make(findViewById(android.R.id.content), "Passwords don't match. Enter matching password.", Snackbar.LENGTH_LONG).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    //FirebaseUser user = mAuth.getCurrentUser();
                                    Snackbar.make(findViewById(android.R.id.content), "Your account has been created.", Snackbar.LENGTH_LONG).show();

                                    FirebaseUser user = mAuth.getCurrentUser();

                                    // Add new user to the Firestore database
                                    addUserToFirestore(firstName, lastName, email, null, user);

                                    user.sendEmailVerification()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        String message = "Verification email sent to " + user.getEmail() + ". Please go to your email and follow the link.";
                                                        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();

                                                        Intent signInIntent = new Intent(SignUpActivity.this, SignInActivity.class);

                                                        //Start SignIn activity
                                                        startActivity(signInIntent);
                                                        finish();
                                                    }
                                                }
                                            });
                                } else {
                                    Exception exception = task.getException();
                                    // If sign in fails, display a message to the user.
                                    Snackbar.make(findViewById(android.R.id.content), exception.getMessage(), Snackbar.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

    }

    // Add new user data to Firestore database
    private void addUserToFirestore(String firstName, String lastName, String email, String myMarkerId, FirebaseUser user) {
        // Create a User object
        User newUser = new User(firstName, lastName, email, myMarkerId);

        // creating a collection reference
        // for Firebase Firestore database.
        db.collection("users").document(user.getUid())
                .set(newUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }
}