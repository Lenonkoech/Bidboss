package com.example.bidboss2;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class NonWinnerSummaryActivity {
    // NonWinnerSummaryActivity.java
    private void refundWithDeduction() {
        double deductionRate = 0.025;
        long initialBid = 100; // example starting bid, adjust accordingly

        long refundAmount = initialBid - (long)(initialBid * deductionRate);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                    .update("balance", FieldValue.increment(refundAmount))
                    .addOnSuccessListener(aVoid -> Log.d("Firebase", "Refunded with deduction."));
        }
    }
}
