package com.example.project5;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import java.util.Collections;

public class SignIn extends AppCompatActivity {

    private static final String TAG = "SignIn";
    private View rootView;
    private AdView bannerAdView;
    private FrameLayout bannerAdFrameLayout;
    private static final int RC_SIGN_IN = 0;
    private SignInButton googleSignInButton;
    private GoogleSignInClient mGoogleSignInClient;
    private Toast longToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");

        // Skip to needed page.
        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences("sharedPreferences", MODE_PRIVATE);
        GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (googleAccount != null && !sharedPrefs.getBoolean("disclaimed", false)) {

            Intent Activity = new Intent(this, Disclaimer.class);
            startActivity(Activity);
            return;
        } else if (googleAccount != null && !sharedPrefs.getBoolean("subbed", false)) {

            Intent Activity = new Intent(this, SubscribePage.class);
            startActivity(Activity);
            return;
        } else if (googleAccount != null) {

            Intent Activity = new Intent(this, SubsPage.class);
            startActivity(Activity);
            return;
        }

        setContentView(R.layout.signin);

        rootView = findViewById(R.id.rootViewSignIn);

        googleSignInButton = findViewById(R.id.googleSignInButton);
        // Set the color scheme for the Google sign-in button. Documentation found here:
        // developers.google.com/android/reference/com/google/android/gms/common/SignInButton.html#COLOR_DARK
        googleSignInButton.setColorScheme(0);

        RequestConfiguration configuration = new RequestConfiguration.Builder().setTestDeviceIds(Collections.singletonList("814BF63877CBD71E91F9D7241907F4FF")).build();
        MobileAds.setRequestConfiguration(configuration);

        bannerAdFrameLayout = findViewById(R.id.bannerAdFrameLayout);
        bannerAdView = new AdView(this);
        bannerAdView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
        bannerAdFrameLayout.addView(bannerAdView);
        loadBanner();

        // Configure Google Sign In.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Set to dark mode.
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES);
    }

    private void loadBanner() {

        AdRequest adRequest = new AdRequest.Builder().build();

        AdSize adSize = getAdSize();

        // Step 4 - Set the adaptive ad size on the ad view.
        bannerAdView.setAdSize(adSize);

        // Adjust bannerAdFrameLayout size here so viewPager doesn't jump when ad loads.
        bannerAdFrameLayout.getLayoutParams().height = adSize.getHeightInPixels(this);

        // Step 5 - Start loading the ad in the background.
        bannerAdView.loadAd(adRequest);
    }

    private AdSize getAdSize() {

        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = this.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    @Override
    protected void onStart() {

        super.onStart();
        Log.i(TAG, "onStart()");

        // Sign in using Google.
        googleSignInButton.setOnClickListener(view -> {

            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    protected void onRestart() {

        super.onRestart();
        Log.i(TAG, "onRestart()");
    }

    @Override
    protected void onResume() {

        super.onResume();
        Log.i(TAG, "onResume()");
    }

    @Override
    protected void onPause() {

        Log.i(TAG, "onPause()");
        super.onPause();
    }

    @Override
    protected void onStop() {

        Log.i(TAG, "onStop()");

        // Remove the listener.
        if (googleSignInButton != null) {

            googleSignInButton.setOnClickListener(null);
        }

        cancelToasts();

        super.onStop();
    }

    @Override
    public void onDestroy() {

        if (bannerAdView != null) {

            bannerAdView.removeAllViews();
            bannerAdView.destroy();
            bannerAdView = null;
        }

        super.onDestroy();
    }

    // Sign in using Google.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {

                // Google sign in was successful, authenticate with Firebase
                GoogleSignInAccount googleAccount = task.getResult(ApiException.class);
                if (googleAccount != null) {

                    Intent Activity = new Intent(this, Disclaimer.class);
                    startActivity(Activity);
                } else {

                    Log.w(TAG, "onActivityResult() -> account == null");
                    showMessageLong("Sign-in failed. Try again later.");
                }
            } catch (ApiException e) {

                // This will be called if user backed out of Google sign-in, so don't show an error message.
                Log.w(TAG, "Google sign-in failed: " + e);
            }
        }
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