package com.example.project5;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.snackbar.Snackbar;

public class SubsPage extends AppCompatActivity {

    private static final String TAG = "SubbedPage";
    private View rootView;
    private Toast longToast;
    private Button unsubscribeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");

        setContentView(R.layout.subscribepage);

        rootView = findViewById(R.id.rootViewSubbedPage);

        unsubscribeButton = findViewById(R.id.unsubscribeButton);

        // Set to dark mode.
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES);
    }

    @Override
    protected void onStart() {

        super.onStart();
        Log.i(TAG, "onStart()");

        unsubscribeButton.setOnClickListener(view -> {

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

        unsubscribeButton.setOnClickListener(null);

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
