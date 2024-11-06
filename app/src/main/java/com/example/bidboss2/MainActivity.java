package com.example.bidboss2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.bidboss2.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.bidboss2.ui.home.HomeFragment;


public class MainActivity extends AppCompatActivity {
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private Switch modeSwitch;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private TextView userName, userEmail;
    private ImageView profilePicture;
    private LinearLayout buyerLayout;
    private LinearLayout sellerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbarAndNavigation();
        initializeFirebase();
        setupNavigationItems();
        fetchUserData();
        setupModeSwitch();
    }

    private void setupToolbarAndNavigation() {
        setSupportActionBar(binding.appBarMain.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_BidHistory, R.id.nav_viewProfile, R.id.nav_EditProfile)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    private void initializeFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @SuppressLint("CutPasteId")
    private void setupNavigationItems() {
        NavigationView navigationView = binding.navView;
        View headerView = navigationView.getHeaderView(0);
        modeSwitch = headerView.findViewById(R.id.modeSwitch);
        userName = headerView.findViewById(R.id.userName);
        userEmail = headerView.findViewById(R.id.userEmail);
        profilePicture = headerView.findViewById(R.id.imageView);
        buyerLayout = findViewById(R.id.buyer_layout);
        sellerLayout = findViewById(R.id.seller_layout);

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            boolean handled = false;

            if (id == R.id.logout) {
                logoutUser();
                handled = true;
            } else {
                handled = NavigationUI.onNavDestinationSelected(menuItem,
                        Navigation.findNavController(this, R.id.nav_host_fragment_content_main));
            }

            if (handled) {
                DrawerLayout drawer = binding.drawerLayout;
                drawer.closeDrawers();
            }
            return handled;
        });
    }

    private void setupModeSwitch() {
        // Set initial mode to Buyer (off state)
        modeSwitch.setChecked(false);
        modeSwitch.setText("Buyer");
        // Listen for toggle changes
        modeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d("MainActivity", "Mode switch toggled: " + (isChecked ? "Seller" : "Buyer"));
            modeSwitch.setText(isChecked ? "Seller" : "Buyer");
            DrawerLayout drawer = binding.drawerLayout;
            drawer.closeDrawers();
            loadHomeFragment(isChecked);
        });

        // Initial load for Buyer mode
        loadHomeFragment(false);
    }

    private void loadHomeFragment(boolean isSellerMode) {
        Log.d("MainActivity", "Loading HomeFragment with isSellerMode: " + isSellerMode);

        // Create HomeFragment and pass the mode in a bundle
        HomeFragment homeFragment = new HomeFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isSellerMode", isSellerMode);
        homeFragment.setArguments(bundle);

        // Clear any previous fragment in the container
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        // Load HomeFragment into the container
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment_content_main, homeFragment);
        transaction.commit();
    }

    private void logoutUser() {
        auth.signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void fetchUserData() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(MainActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        db.collection("Users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("name");
                    String email = documentSnapshot.getString("email");

                    userName.setText(name != null ? name : "No Name Provided");
                    userEmail.setText(email != null ? email : "No Email Provided");
                } else {
                    Toast.makeText(MainActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Error fetching user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(MainActivity.this, "Failed to fetch user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }
}
