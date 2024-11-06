package com.example.bidboss2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnCreateAccount;
    private TextView tvForgotPassword;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Check if user is already logged in
        if (mAuth.getCurrentUser() != null) {
            // User is already logged in, navigate to MainActivity
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish(); // Close LoginActivity
            return;
        }

        // Initialize UI components
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        progressBar=findViewById(R.id.progressBar);

        // Link to register view
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
                finish();
            }
        });

        // Login button onClickListener
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                // Check if email or password is empty
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(LoginActivity.this, "Blank Email!!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, "Blank Password!!!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Sign in the user
                progressBar.setVisibility(View.VISIBLE); // Show progress bar
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Login successful, navigate to MainActivity
                                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();

                                    // Fetch user data from Firestore if needed
                                    if (firebaseUser != null) {
                                        fetchUserData(firebaseUser.getUid());
                                    }
                                } else {
                                    // If login fails, display a message to the user
                                    Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                                progressBar.setVisibility(View.GONE); // Hide progress bar after completion
                            }
                        });
            }
        });
    }

    // Fetch user data from Firestore
    private void fetchUserData(String userId) {
        mFirestore.collection("Users").document(userId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()) {
                                // You can retrieve user info here if necessary
                                String name = documentSnapshot.getString("name");
                                String email = documentSnapshot.getString("email");

                                // Optional: Pass user data to MainActivity if needed
                                Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                mainIntent.putExtra("name", name);
                                mainIntent.putExtra("email", email);
                                startActivity(mainIntent);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Error fetching user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}