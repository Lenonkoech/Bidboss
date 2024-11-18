package com.example.bidboss2.ui.manageAccount;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bidboss2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;

public class ManageAccountFragment extends Fragment {

    private static final double ADDITIONAL_BALANCE = 500.00; // Constant for the amount to add
    private TextView balanceTextView;
    private ImageView profilePicture;
    private TextView name, email;
    private Button depositButton;
    private FirebaseUser currentUser;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_manageaccount, container, false);
        profilePicture = rootView.findViewById(R.id.edit_profilePicture);
        name = rootView.findViewById(R.id.edit_name);
        email = rootView.findViewById(R.id.edit_email);

        // Initialize Firebase Auth, Firestore, and Storage
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Load user data and balance
        loadUserDataAndBalance();

        // Initialize views
        balanceTextView = rootView.findViewById(R.id.balanceTextView);
        depositButton = rootView.findViewById(R.id.depositButton);

        // Set up the deposit button click listener
        depositButton.setOnClickListener(v -> addBalance(ADDITIONAL_BALANCE));

        return rootView;
    }

    private void loadUserDataAndBalance() {
        currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userRef = db.collection("Users").document(userId);

            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        String nameStr = documentSnapshot.getString("name");
                        String emailStr = documentSnapshot.getString("email");
                        double balance = documentSnapshot.getDouble("balance") != null ?
                                documentSnapshot.getDouble("balance") : 0.0;

                        // Populate fields
                        name.setText(!TextUtils.isEmpty(nameStr) ? "Name: " + nameStr : "Name not available");
                        email.setText(!TextUtils.isEmpty(emailStr) ? "Email: " + emailStr : "Email not available");
                        balanceTextView.setText(String.format("Balance: $%.2f", balance));
                    } else {
                        Log.e("ManageAccountFragment", "User document not found");
                        balanceTextView.setText("Balance: $0.00");
                    }
                } else {
                    Log.e("ManageAccountFragment", "Error fetching user data", task.getException());
                }
            });
        } else {
            balanceTextView.setText("No user logged in");
            Log.e("ManageAccountFragment", "No authenticated user found");
        }
    }

    private void addBalance(double additionalBalance) {
        currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            DocumentReference userRef = db.collection("Users").document(currentUser.getUid());

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    double currentBalance = documentSnapshot.getDouble("balance") != null
                            ? documentSnapshot.getDouble("balance") : 0;
                    double updatedBalance = currentBalance + additionalBalance;

                    // Update Firestore with new balance
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("balance", updatedBalance);

                    userRef.set(updates, SetOptions.merge()).addOnSuccessListener(aVoid -> {
                        // Successfully updated balance
                        balanceTextView.setText(String.format("Balance: $%.2f", updatedBalance));
                    }).addOnFailureListener(e -> {
                        // Handle failure in updating
                        Log.e("ManageAccountFragment", "Error updating balance", e);
                    });
                } else {
                    Log.e("ManageAccountFragment", "User document not found during balance update");
                }
            }).addOnFailureListener(e -> {
                Log.e("ManageAccountFragment", "Error retrieving user data", e);
            });
        }
    }
}