package com.example.bidboss2.ui.viewProfile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.bidboss2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class ViewProfileFragment extends Fragment {
    private ImageView profilePicture;
    private TextView name, email, phone, passportOrId, postalAddress,country;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView =inflater.inflate(R.layout.fragment_viewprofile,container,false);
        profilePicture = rootView.findViewById(R.id.edit_profilePicture);
        name = rootView.findViewById(R.id.edit_name);
        email = rootView.findViewById(R.id.edit_email);
        country = rootView.findViewById(R.id.countryMenu);
        phone = rootView.findViewById(R.id.edit_phoneNumber);
        passportOrId = rootView.findViewById(R.id.edit_passport);
        postalAddress = rootView.findViewById(R.id.edit_postalCode);

        // Initialize Firebase Auth, Firestore, and Storage
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        fetchUserData();
        return rootView;
    }

    private void fetchUserData() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("Users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()) {
                    String Name = documentSnapshot.getString("name");
                    String Email = documentSnapshot.getString("email");
                    String Country = documentSnapshot.getString("country");
                    String address = documentSnapshot.getString("postalAddress");
                    String Phone = documentSnapshot.getString("phone");
                    String passport = documentSnapshot.getString("passport");
                    String profilePic = documentSnapshot.getString("profilePhotoURL");

                    // Populate fields
                    name.setText("Name: " + Name);
                    email.setText("Email: "+Email);
                    phone.setText("Phone: "+Phone);
                    country.setText("Country: "+Country);
                    postalAddress.setText("Adress: "+address);
                    passportOrId.setText("Passport/ID: "+passport);
                }
            }
        });
    }


}