package com.example.bidboss2;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class RealTimeBidActivity extends AppCompatActivity {

    private TextView balanceTextView, countdownTextView, bidAmountTextView;
    private Button incrementBidButton, placeBidButton;
    private ImageView productImageView;
    private int bidAmount = 0;
    private final int BID_DURATION = 2 * 60 * 1000; // Duration in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_bid);

        balanceTextView = findViewById(R.id.balanceTextView);
        countdownTextView = findViewById(R.id.countdownTextView);
        bidAmountTextView = findViewById(R.id.bidAmountTextView);
        incrementBidButton = findViewById(R.id.incrementBidButton);
        placeBidButton = findViewById(R.id.placeBidButton);
        productImageView = findViewById(R.id.productImageView);

        loadUserBalance();
        loadProductImage();
        setupBidControls();
        startBidCountdown();
    }

    private void setupBidControls() {
        incrementBidButton.setOnClickListener(v -> {
            bidAmount += 5;
            bidAmountTextView.setText("Bid: $" + bidAmount);
        });

        placeBidButton.setOnClickListener(v -> placeBid());
    }

    private void startBidCountdown() {
        new CountDownTimer(BID_DURATION, 1000) {
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                int minutes = (int) ((millisUntilFinished / (1000 * 60)) % 60);
                countdownTextView.setText(String.format("%02d:%02d", minutes, seconds));
            }

            public void onFinish() {
                checkBidResult();
            }
        }.start();
    }

    private void placeBid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Map<String, Object> bidData = new HashMap<>();
            bidData.put("amount", bidAmount);
            bidData.put("bidderId", user.getUid());

            FirebaseFirestore.getInstance().collection("products").document("currentProductId")
                    .set(bidData, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> Log.d("Firebase", "Bid placed successfully."))
                    .addOnFailureListener(e -> Log.w("Firebase", "Error placing bid", e));
        }
    }

    private void checkBidResult() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("bids").document("currentProductId")
                .get().addOnSuccessListener(snapshot -> {
                    String highestBidderId = snapshot.getString("bidderId");
                    if (userId.equals(highestBidderId)) {
                        showWinnerPage();
                    } else {
                        showNonWinnerPage();
                    }
                });
    }

    private void loadProductImage() {
        // Load product image logic here
    }

    private void loadUserBalance() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                    .addSnapshotListener((snapshot, e) -> {
                        if (snapshot != null && snapshot.exists()) {
                            long balance = snapshot.getLong("balance");
                            balanceTextView.setText("Balance: $" + balance);
                        }
                    });
        }
    }

    private void showWinnerPage() {
        Intent intent = new Intent(this, WinnerSummaryActivity.class);
        startActivity(intent);
        finish();
    }

    private void showNonWinnerPage() {
        Intent intent = new Intent(this, NonWinnerSummaryActivity.class);
        startActivity(intent);
        finish();
    }
}
