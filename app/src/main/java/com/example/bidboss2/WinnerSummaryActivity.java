package com.example.bidboss2;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class WinnerSummaryActivity extends AppCompatActivity {

    private Button deliveryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_winner_summary);

        deliveryButton = findViewById(R.id.deliveryButton);
        setupDeliveryOption();
    }

    private void setupDeliveryOption() {
        //Button deliveryButton = findViewById(R.id.deliveryButton); // Using built-in findViewById

        deliveryButton.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                        .update("balance", FieldValue.increment(-20))
                        .addOnSuccessListener(aVoid -> Log.d("Firebase", "Delivery fee deducted."));
            }
        });
    }
}
