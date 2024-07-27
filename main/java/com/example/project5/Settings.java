package com.example.project5;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.FirebaseDatabase;

public class Settings extends AppCompatActivity {

    private static final String TAG = "Settings";
    private View rootView;
    private Toast longToast;
    private SwitchCompat receiveNotificationsSwitch;
    private Button logOutButton, deleteAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");

        setContentView(R.layout.settings);

        rootView = findViewById(R.id.rootViewSettings);

        receiveNotificationsSwitch = findViewById(R.id.receiveNotificationsSwitch);
        logOutButton = findViewById(R.id.logOutButton);
        deleteAccountButton = findViewById(R.id.unsubscribeButton);

        // Set to dark mode.
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES);
    }

    @Override
    protected void onStart() {

        super.onStart();
        Log.i(TAG, "onStart()");

        receiveNotificationsSwitch.setOnCheckedChangeListener((compoundButton, isOn) -> {

            if (isOn) {

                Do this.
            } else {

                Do that.
            }

        });

        logOutButton.setOnClickListener(view -> {

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Log out")
                    .setMessage("Really log out? You will no longer receive notifications.")
                    .setCancelable(true)
                    .setPositiveButton("Confirm", (dialog, id) -> {
                        // If user has a Google account, get email one way. Else, get email another way.
                        GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(this);
                        String email;
                        if (googleAccount != null) {

                            email = googleAccount.getEmail();
                            if (email != null) {

                                // Clear all sharedPrefs once user logs out.
                                SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences("sharedPreferences", MODE_PRIVATE);
                                sharedPrefs.edit().clear().apply();

                                // Firebase does not allow ".", so replace them with ",".
                                String userEmailFirebase = email.replace(".", ",");
                                FirebaseDatabase.getInstance().getReference().child("Users").child(userEmailFirebase).child("Token").removeValue();

                                GoogleSignInOptions gso = new GoogleSignInOptions.
                                        Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                                        build();

                                GoogleSignInClient googleSignInClient=GoogleSignIn.getClient(this, gso);
                                googleSignInClient.signOut();

                                Intent Activity = new Intent(this, SignIn.class);
                                startActivity(Activity);
                            } else {

                                showMessageLong("Could not retrieve account information. Try again later.");
                            }
                        } else {

                            showMessageLong("Could not retrieve account information. Try again later.");
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, id) -> {
                        // CANCEL
                    });

            // Create the AlertDialog object.
            builder.create();
        });

        deleteAccountButton.setOnClickListener(view -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Unsubscribe")
                    .setMessage("Really unsubscribe? Your payment will no longer be active.")
                    .setCancelable(true)
                    .setPositiveButton("Confirm", (dialog, id) -> {
                        // CONFIRM
                        need this
                    })
                    .setNegativeButton("Cancel", (dialog, id) -> {
                        // CANCEL
                    });

            // Create the AlertDialog object.
            builder.create();
        });
    }

    @Override
    protected void onStop() {

        logOutButton.setOnClickListener(null);

        deleteAccountButton.setOnClickListener(null);

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
