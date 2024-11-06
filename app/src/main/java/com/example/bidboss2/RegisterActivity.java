package com.example.bidboss2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {
    private EditText editTextName, editTextEmail, editTextPassword, editTextRetypePassword;
    private CheckBox termsAndConditions;
    private Button signUp;
    private ProgressBar progressBar;
    private TextView login;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore; // Firestore instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance(); // Initialize Firestore
        checkIfUserLoggedIn();

        initializeUIElements();

        // Login link onClickListener
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        });

        // Signup button onClickListener
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    private void checkIfUserLoggedIn() {
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
        }
    }

    private void initializeUIElements() {
        editTextName = findViewById(R.id.etFullName);
        editTextEmail = findViewById(R.id.etEmailAddress);
        editTextPassword = findViewById(R.id.etPassword);
        editTextRetypePassword = findViewById(R.id.etRetypePassword);
        termsAndConditions = findViewById(R.id.termsCheckbox);
        signUp = findViewById(R.id.btnSignUp);
        progressBar = findViewById(R.id.progressBar);
        login = findViewById(R.id.btnSignInLink);
    }

    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String retypePassword = editTextRetypePassword.getText().toString().trim();
        String name = editTextName.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);

        // Validate input fields
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(password) || TextUtils.isEmpty(retypePassword)) {
            showToast("All fields are required!");
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (!termsAndConditions.isChecked()) {
            showToast("Please agree to the terms and conditions");
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (!password.equals(retypePassword)) {
            showToast("Passwords do not match");
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (password.length() < 6) {
            showToast("Password must be at least 6 characters");
            progressBar.setVisibility(View.GONE);
            return;
        }

        // Create user in Firebase
        createUserInFirebase(email, password, name);
    }

    private void createUserInFirebase(String email, String password, String name) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            saveUserToFirestore(name, email, firebaseUser.getUid());
                        }
                    } else {
                        showToast("Authentication failed.");
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    // Save user information to Firestore
    private void saveUserToFirestore(String name, String email, String userId) {
        String defaultPhotoUrl = ""; // Use a valid image resource or URL if needed
        Users users = new Users(name, email, defaultPhotoUrl);

        // Save user data to Firestore
        mFirestore.collection("Users").document(userId)
                .set(users)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showToast("Account Created");
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finish();
                    } else {
                        showToast("Failed to save user info");
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void showToast(String message) {
        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    // User model
    public static class Users {
        public String name;
        public String email;
        public String profilePhotoURL = "@drawable/user_image.xml";

        public Users() { }

        public Users(String name, String email, String profilePhotoURL) {
            this.name = name;
            this.email = email;
            this.profilePhotoURL = profilePhotoURL;
        }
    }
}