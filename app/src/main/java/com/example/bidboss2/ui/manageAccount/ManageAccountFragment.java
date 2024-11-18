package com.example.bidboss2.ui.manageAccount;

import android.os.Bundle;
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
        View rootView=inflater.inflate(R.layout.fragment_manageaccount,container,false);
        profilePicture = rootView.findViewById(R.id.edit_profilePicture);
        name = rootView.findViewById(R.id.edit_name);
        email = rootView.findViewById(R.id.edit_email);

        // Initialize Firebase Auth, Firestore, and Storage
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        fetchUserData();

        // Initialize views
        balanceTextView = rootView.findViewById(R.id.balanceTextView); // Update with correct ID
        depositButton = rootView.findViewById(R.id.depositButton);

        // Load the user's current balance
        loadUserBalance();

        // Set up the deposit button click listener
        depositButton.setOnClickListener(v -> addBalance(500.00));

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView profileImageView = view.findViewById(R.id.edit_profilePicture);
        balanceTextView = view.findViewById(R.id.balanceTextView);
        depositButton = view.findViewById(R.id.depositButton);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            loadUserBalance();
        }

        depositButton.setOnClickListener(v -> addBalance(500.00));
    }

    //Load the user balance initially
    private void loadUserBalance() {
        // Ensure Firebase Authentication and Firestore are initialized
        currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Reference to the user's document in Firestore
            DocumentReference userRef = db.collection("Users").document(userId);

            // Retrieve the user's document
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Retrieve the balance field
                    Double balance = documentSnapshot.getDouble("balance");

                    // Check for null and set the balance to 0 if not found
                    if (balance == null) {
                        balance = 0.0;
                    }

                    // Update the UI with the user's balance
                    balanceTextView.setText(String.format("Balance: $%.2f", balance));
                } else {
                    // Handle the case where the document does not exist
                    balanceTextView.setText("Balance: $0.00");
                    Log.e("ManageAccountFragment", "User document not found");
                }
            }).addOnFailureListener(e -> {
                // Handle any errors while retrieving the document
                balanceTextView.setText("Error loading balance");
                Log.e("ManageAccountFragment", "Error loading user balance", e);
            });
        } else {
            // Handle the case where no user is logged in
            balanceTextView.setText("No user logged in");
            Log.e("ManageAccountFragment", "No authenticated user found");
        }
    }

    //Add money to account
    private void addBalance(double v) {
        // Example amount to add, replace with actual input retrieval if needed
        double additionalBalance = 500;  // Example value

        // Get user document reference
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            DocumentReference userRef = db.collection("users").document(currentUser.getUid());

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
                        balanceTextView.setText(String.valueOf(updatedBalance));
                    }).addOnFailureListener(e -> {
                        // Handle failure in updating
                        Log.e("ManageAccountFragment", "Error updating balance", e);
                    });
                }
            }).addOnFailureListener(e -> {
                Log.e("ManageAccountFragment", "Error retrieving user data", e);
            });
        }
    }

    private void fetchUserData() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("Users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()) {
                    String Name = documentSnapshot.getString("name");
                    String Email = documentSnapshot.getString("email");
                    double balance = documentSnapshot.getDouble("balance");
                    // Populate fields
                    name.setText("Name: " + Name);
                    email.setText("Email: "+Email);
                    balanceTextView.setText("Balance: $" + balance);
                } else {
                    // Handle the case where the document doesn't exist
                }
            }
        });
    }
}
