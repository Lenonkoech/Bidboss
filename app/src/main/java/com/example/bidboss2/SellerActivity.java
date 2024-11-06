package com.example.bidboss2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bidboss2.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;

public class SellerActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imageView;
    private EditText descriptionEditText;
    private EditText priceEditText;
    private EditText auctionEndDateEditText;
    private Button uploadButton;

    private Uri imageUri;

    private FirebaseStorage storage;
    private DatabaseReference databaseReference;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller);

        imageView = findViewById(R.id.image_view);
        descriptionEditText = findViewById(R.id.description_edit_text);
        priceEditText = findViewById(R.id.price_edit_text);
        auctionEndDateEditText = findViewById(R.id.auction_end_date_edit_text);
        uploadButton = findViewById(R.id.upload_button);

        storage = FirebaseStorage.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("bidboss2");

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadProduct();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
    }

    private void uploadProduct() {
        if (imageUri != null) {
            final StorageReference fileReference = storage.getReference("uploads").child("image_" + System.currentTimeMillis());
            fileReference.putFile(imageUri)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                fileReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            String imageUrl = task.getResult().toString();
                                            String description = descriptionEditText.getText().toString().trim();
                                            String price = priceEditText.getText().toString().trim();
                                            String auctionEndDate = auctionEndDateEditText.getText().toString().trim();

                                            String auctionId = databaseReference.push().getKey();Auction auction = new Auction(auctionId, description, price, imageUrl, auctionEndDate);

                                            databaseReference.child(auctionId).setValue(auction).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(SellerActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(SellerActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(SellerActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }
}