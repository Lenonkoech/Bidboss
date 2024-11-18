package com.example.bidboss2.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bidboss2.MainActivity;
import com.example.bidboss2.R;
import com.example.bidboss2.RealTimeBidActivity;

public class HomeFragment extends Fragment {
    private LinearLayout buyerLayout;
    private LinearLayout sellerLayout;
    private TextView buyer_description, price;
    private ImageView buyer_image;
    private Button bidButton;

    // Method to navigate to RealTimeBidActivity
    private void navigateToRealTimeBid() {
        Intent intent = new Intent(getActivity(), RealTimeBidActivity.class);
        startActivity(intent);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        buyerLayout = view.findViewById(R.id.buyer_layout);
        sellerLayout = view.findViewById(R.id.seller_layout);
        buyer_description = view.findViewById(R.id.buyer_description);
        price = view.findViewById(R.id.buyer_price);
        buyer_image = view.findViewById(R.id.buyer_image);
        bidButton = view.findViewById(R.id.bid_button);

        // Set up button click listener to navigate to RealTimeBidActivity
        bidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),RealTimeBidActivity.class);
                getActivity().finish();
            }
        });

        // Get the toggle state from arguments
        boolean isSellerMode = getArguments() != null && getArguments().getBoolean("isSellerMode", false);

        // Toggle layouts based on mode
        if (isSellerMode) {
            buyerLayout.setVisibility(View.GONE);
            sellerLayout.setVisibility(View.VISIBLE);
        } else {
            buyerLayout.setVisibility(View.VISIBLE);
            sellerLayout.setVisibility(View.GONE);
            bidButton.setVisibility(View.VISIBLE);
            buyer_image.setVisibility(View.VISIBLE);
            price.setVisibility(View.VISIBLE);
            buyer_description.setVisibility(View.VISIBLE);
        }

        return view;
    }
}
