package com.example.project5;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Disclaimer extends AppCompatActivity {

    private static final String TAG = "Disclaimer";
    private View rootView;
    private ScrollView disclaimerScrollView;
    private Button proceedButton;
    private ViewTreeObserver.OnScrollChangedListener viewTreeObserver;
    private Toast longToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");

        setContentView(R.layout.disclaimer);

        rootView = findViewById(R.id.rootViewDisclaimer);

        disclaimerScrollView = findViewById(R.id.disclaimerScrollView);
        proceedButton = findViewById(R.id.proceedButton);

        // Sets proceedButton to enabled when user scrolls to bottom of disclaimerScrollView.
        viewTreeObserver = (ViewTreeObserver.OnScrollChangedListener) () -> {

            if (!disclaimerScrollView.canScrollVertically(1)) {
                // Bottom of scroll view.
                proceedButton.setEnabled(true);
            }
        };

        // Set to dark mode.
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES);
    }

    @Override
    protected void onStart() {

        super.onStart();
        Log.i(TAG, "onStart()");

        // Sets proceedButton to enabled when user scrolls to bottom of disclaimerScrollView.
        disclaimerScrollView.getViewTreeObserver().addOnScrollChangedListener(viewTreeObserver);

        // Shows message to users if they try to click on disabled proceedButton.
        proceedButton.setOnClickListener(v -> {

            if (!proceedButton.isActivated()) {

                showMessageLong("Scroll to bottom of disclaimer to proceed.");
            } else {

                // If user has a Google account, get email one way. Else, get email another way.
                GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(this);
                String email;
                if (googleAccount != null) {

                    email = googleAccount.getEmail();
                    if (email != null) {

                        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences("sharedPreferences", MODE_PRIVATE);
                        sharedPrefs.edit().putBoolean("disclaimed", true).apply();

                        // Firebase does not allow ".", so replace them with ",".
                        String userEmailFirebase = email.replace(".", ",");
                        // Check if user has an account saved in Firebase from a previous subscription (i.e. they logged out and are logging back in).
                        FirebaseDatabase.getInstance().getReference().orderByChild("Users").equalTo(userEmailFirebase).addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.getChildrenCount() > 0) {

                                    sharedPrefs.edit().putBoolean("subbed", true).apply();

                                    // Send token to Firebase so notifications can be received.
                                    Project5MessagingService project5MessagingService = new Project5MessagingService();
                                    project5MessagingService.updateTokenInFirebaseFromSharedPrefs();

                                    Intent Activity = new Intent(getBaseContext(), SubsPage.class);
                                    startActivity(Activity);
                                } else {

                                    Intent Activity = new Intent(getBaseContext(), SubscribePage.class);
                                    startActivity(Activity);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                showMessageLong("Error checking database for existing account. Try again later.");
                            }
                        });
                    } else {

                        showMessageLong("Could not retrieve account information. Try again later.");
                    }
                } else {

                    showMessageLong("Could not retrieve account information. Try again later.");
                }
            }
        });
    }

    @Override
    protected void onStop() {

        Log.i(TAG, "onStop()");

        disclaimerScrollView.getViewTreeObserver().removeOnScrollChangedListener(viewTreeObserver);

        proceedButton.setOnClickListener(null);

        super.onStop();
    }

    private void cancelToasts() {

        if (longToast != null) {

            longToast.cancel();
        }
    }

    private void showMessageLong(String message) {

        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {

            Snackbar snackBar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
            View snackBarView = snackBar.getView();
            TextView snackTextView = snackBarView.findViewById(com.google.android.material.R.id.snackbar_text);
            snackTextView.setMaxLines(10);
            snackBar.show();
        } else {

            cancelToasts();
            longToast = Toast.makeText(this, message, Toast.LENGTH_LONG);
            longToast.setGravity(Gravity.CENTER, 0, 0);
            longToast.show();
        }
    }
}