package com.example.parkgmu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    TextView signUpLink;
    EditText emailInput, passwordInput;
    Button signInBtn;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.progress_bar);
        signUpLink = findViewById(R.id.sign_in_link);
        emailInput = findViewById(R.id.sign_up_email);
        emailInput.setCompoundDrawablePadding(20);
        passwordInput = findViewById(R.id.sign_up_pw);
        passwordInput.setCompoundDrawablePadding(20);
        signInBtn = findViewById(R.id.sign_up_actv_btn);

        // Go to Sign Up page
        signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUpIntent = new Intent(SignInActivity.this, SignUpActivity.class);

                //Start SignIn activity
                startActivity(signUpIntent);
                finish();
            }
        });

        // Account sign in
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String email, password;

                email = String.valueOf(emailInput.getText());
                password = String.valueOf(passwordInput.getText());

                // Check for empty fields
                if (email.isEmpty() || password.isEmpty()) {
                    Snackbar.make(findViewById(android.R.id.content), "Please provide all required information.", Snackbar.LENGTH_LONG).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user.isEmailVerified()) {
                                        Snackbar.make(findViewById(android.R.id.content), "Sign In successful.", Snackbar.LENGTH_LONG).show();

                                    Intent mapIntent = new Intent(SignInActivity.this, MapsActivity.class);
                                    startActivity(mapIntent);
                                    finish();
                                    }
                                    else {
                                        // Prompt the user to resend verification code
                                        promptUserForAction(user);

                                        FirebaseAuth.getInstance().signOut();
                                    }
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

    // Alert to prompt user action
    private void promptUserForAction(FirebaseUser user) {
        // Create an AlertDialog Builder instance
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set the title and message for the dialog
        builder.setTitle("Email Verification");
        builder.setMessage("You have not verified your email. Resend verification link to your email?");

        // Set positive button and its action
        builder.setPositiveButton("Resend Link", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Send email verification link to the user's email
                user.sendEmailVerification();
                Snackbar.make(findViewById(android.R.id.content), "Verification link sent.", Snackbar.LENGTH_LONG).show();
            }
        });

        // Set negative button and its action
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Create and display the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}