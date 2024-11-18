package com.example.bidboss2.ui.editProfile;

import static com.example.bidboss2.R.layout.*;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.bidboss2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1; // Code to identify the image picker
    private ImageView editProfilePicture;
    private EditText editName, editEmail, editPhone, editPassportOrId, editPostalAddress;
    private Spinner editCountry;
    private Button updateProfile;
    private Map<String, String> countryCodes;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Uri imageUri; // URI of the selected image

    public EditProfileFragment() {}

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_editprofile, container, false);
        editProfilePicture = rootView.findViewById(R.id.edit_profilePicture);
        editName = rootView.findViewById(R.id.edit_name);
        editEmail = rootView.findViewById(R.id.edit_email);
        editCountry = rootView.findViewById(R.id.countryMenu);
        editPhone = rootView.findViewById(R.id.edit_phoneNumber);
        editPassportOrId = rootView.findViewById(R.id.edit_passport);
        editPostalAddress = rootView.findViewById(R.id.edit_postalCode);
        updateProfile = rootView.findViewById(R.id.updateProfile);

        // Initialize Firebase Auth, Firestore, and Storage
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Initialize country codes
        initializeCountryCodes();

        // Set up country spinner with country list
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.countries_array, android.R.layout.simple_spinner_dropdown_item);
        editCountry.setAdapter(adapter);

        // Set phone number prefix on select country
        editCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCountry = editCountry.getSelectedItem().toString();
                String countryCode = countryCodes.getOrDefault(selectedCountry, "");
                editPhone.setText(countryCode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        // Set click listener to choose profile picture
        editProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage(); // Method to start image picker
            }
        });

        // Fetch user data from Firestore
        fetchUserData();

        // Update Profile Button
        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateFields()) {
                    uploadImageToFirebase(); // Call to upload the image
                }
            }

            private boolean validateFields() {
                String selectedCountry = editCountry.getSelectedItem().toString();
                if (editName.getText().toString().isEmpty() ||
                        editEmail.getText().toString().isEmpty() ||
                        editPassportOrId.getText().toString().isEmpty() ||
                        editPhone.getText().toString().isEmpty() ||
                        editPostalAddress.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Fill in all fields", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (!countryCodes.containsKey(selectedCountry)) {
                    Toast.makeText(getContext(), "Select a valid Country", Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;
            }
        });

        return rootView;
    }

    private void initializeCountryCodes() {
        countryCodes = new HashMap<>();
        countryCodes.put("Kenya", "+254");
        countryCodes.put("Burundi", "+257");
        countryCodes.put("Somalia", "+252");
        countryCodes.put("Uganda", "+256");
        countryCodes.put("Tanzania", "+255");
        countryCodes.put("Rwanda", "+250");
        countryCodes.put("Ethiopia", "+251");
        countryCodes.put("South Sudan", "+211");
    }

    private void fetchUserData() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("Users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("name");
                    String email = documentSnapshot.getString("email");
                    String country = documentSnapshot.getString("country");
                    String address = documentSnapshot.getString("postalAddress");
                    String phone = documentSnapshot.getString("phone");
                    String passport = documentSnapshot.getString("passport");
                    String profilePic = documentSnapshot.getString("profilePhotoURL");

                    // Populate fields
                    editName.setText(name);
                    editEmail.setText(email);
                    editPhone.setText(phone);
                    editPostalAddress.setText(address);
                    editPassportOrId.setText(passport);

                    // Load existing profile picture if available
                    if (profilePic != null) {
                        // You can use Picasso or Glide to load the image from the URL
                        //Glide.with(this).load(profilePic).circleCrop().into(editProfilePicture);
                    }

                    // Set selected country in spinner
                    if (country != null) {
                        int spinnerPosition = ((ArrayAdapter<String>) editCountry.getAdapter()).getPosition(country);
                        editCountry.setSelection(spinnerPosition);
                    } else {
                        editCountry.setSelection(0);
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Failed to fetch user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == -1 && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                // Set the image in the ImageView
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                editProfilePicture.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            String userId = auth.getCurrentUser().getUid();
            StorageReference storageRef = storage.getReference("profileImages/" + userId + ".png");

            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String profilePicUrl = uri.toString();
                            updateProfileInFirestore(profilePicUrl); // once uploaded, update Firestore with URL
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            updateProfileInFirestore(null); // No new image, just update other fields
        }
    }



    private void updateProfileInFirestore(String profilePhotoUrl) {
        String userId = auth.getCurrentUser().getUid();
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("name", editName.getText().toString());
        userUpdates.put("email", editEmail.getText().toString());
        userUpdates.put("phone", editPhone.getText().toString());
        userUpdates.put("passport", editPassportOrId.getText().toString());
        userUpdates.put("postalAddress", editPostalAddress.getText().toString());
        userUpdates.put("country", editCountry.getSelectedItem().toString());
        if (profilePhotoUrl != null) {
            userUpdates.put("profilePhotoURL", profilePhotoUrl);
        }

        db.collection("Users").document(userId).update(userUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Profile Update Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}