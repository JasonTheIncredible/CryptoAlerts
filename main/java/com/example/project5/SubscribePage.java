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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SubscribePage extends AppCompatActivity {

    private static final String TAG = "SubscriptionPage";
    private View rootView;
    private AdView bannerAdView;
    private FrameLayout bannerAdFrameLayout;
    private EditText firstUser, secondUser, thirdUser, firstKeywords, secondKeywords, thirdKeywords;
    private Button confirmationButton;
    private Toast longToast;
    private BillingClient billingClient;
    private String firstUserString, firstKeywordsString, secondUserString, secondKeywordsString, thirdUserString, thirdKeywordsString;
    private final Integer maxNumberOfKeywordsPerUsername = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");

        setContentView(R.layout.subscribepage);

        rootView = findViewById(R.id.rootViewSubscriptionPage);

        RequestConfiguration configuration = new RequestConfiguration.Builder().setTestDeviceIds(Collections.singletonList("814BF63877CBD71E91F9D7241907F4FF")).build();
        MobileAds.setRequestConfiguration(configuration);

        bannerAdFrameLayout = findViewById(R.id.bannerAdFrameLayout);
        bannerAdView = new AdView(this);
        bannerAdView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
        bannerAdFrameLayout.addView(bannerAdView);
        loadBanner();

        firstUser = findViewById(R.id.firstUser);
        secondUser = findViewById(R.id.secondUser);
        thirdUser = findViewById(R.id.thirdUser);
        firstKeywords = findViewById(R.id.firstKeywords);
        secondKeywords = findViewById(R.id.secondKeywords);
        thirdKeywords = findViewById(R.id.thirdKeywords);
        confirmationButton = findViewById(R.id.confirmationButton);

        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener((billingResult, list) -> {

                            // Will be called when purchase has been updated.
                            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {

                                for (Purchase purchase : list) {

                                    verifySubPurchase(purchase);
                                }
                            }
                        }
                ).build();

        //start the connection after initializing the billing client
        establishConnection();

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

    void establishConnection() {

        billingClient.startConnection(new BillingClientStateListener() {

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

                    // The BillingClient is ready. You can query purchases here.
                    showProducts();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {

                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                establishConnection();
            }
        });
    }

    void showProducts() {

        List<String> skuList = new ArrayList<>();
        skuList.add("addtexthere");
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
        billingClient.querySkuDetailsAsync(params.build(), (billingResult, skuDetailsList) -> {

            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {

                // Process the result.
                for (SkuDetails skuDetails : skuDetailsList) {

                    if (skuDetails.getSku().equals("addtexthere")) {

                        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
                        if (acct != null) {

                            if (acct.getEmail() != null) {

                                confirmationButton.setOnClickListener(view -> {

                                    firstUserString = firstUser.getText().toString().trim();
                                    firstKeywordsString = firstKeywords.getText().toString().trim();
                                    secondUserString = secondUser.getText().toString().trim();
                                    secondKeywordsString = secondKeywords.getText().toString().trim();
                                    thirdUserString = thirdUser.getText().toString().trim();
                                    thirdKeywordsString = thirdKeywords.getText().toString().trim();

                                    // Add warnings to make sure all the information is filled correctly.
                                    if (firstUserString.equals("") && !firstKeywordsString.equals("")) {

                                        firstUser.requestFocus();
                                        showMessageLong("Please assign a user to the keyword(s) below");
                                    }

                                    if (firstKeywordsString.equals("") && !firstUserString.equals("")) {

                                        firstKeywords.requestFocus();
                                        showMessageLong("Please assign keyword(s) to the user above");
                                    }

                                    if (secondUserString.equals("") && !secondKeywordsString.equals("")) {

                                        secondUser.requestFocus();
                                        showMessageLong("Please assign a user to the keyword(s) below");
                                    }

                                    if (secondKeywordsString.equals("") && !secondUserString.equals("")) {

                                        secondKeywords.requestFocus();
                                        showMessageLong("Please assign keyword(s) to the user above");
                                    }

                                    if (thirdUserString.equals("") && !thirdKeywordsString.equals("")) {

                                        thirdUser.requestFocus();
                                        showMessageLong("Please assign a user to the keyword(s) below");
                                    }

                                    if (thirdKeywordsString.equals("") && !thirdUserString.equals("")) {

                                        thirdKeywords.requestFocus();
                                        showMessageLong("Please assign keyword(s) to the user above");
                                    }

                                    if ((firstUserString.contains("@") && firstUserString.length() > 16) || (!firstUserString.contains("@") && firstUserString.length() > 15)) {

                                        firstUser.requestFocus();
                                        showMessageLong("Twitter usernames cannot be longer than 15 characters");
                                    }

                                    if ((secondUserString.contains("@") && secondUserString.length() > 16) || (!secondUserString.contains("@") && secondUserString.length() > 15)) {

                                        secondUser.requestFocus();
                                        showMessageLong("Twitter usernames cannot be longer than 15 characters");
                                    }

                                    if ((thirdUserString.contains("@") && thirdUserString.length() > 16) || (!thirdUserString.contains("@") && thirdUserString.length() > 15)) {

                                        thirdUser.requestFocus();
                                        showMessageLong("Twitter usernames are not longer than 15 characters");
                                    }

                                    if (firstKeywordsString.length() > 100) {

                                        firstKeywords.requestFocus();
                                        showMessageLong("Please limit your keywords' length to 100 characters");
                                    }

                                    if (secondKeywordsString.length() > 100) {

                                        secondKeywords.requestFocus();
                                        showMessageLong("Please limit your keywords' length to 100 characters");
                                    }

                                    if (thirdKeywordsString.length() > 100) {

                                        thirdKeywords.requestFocus();
                                        showMessageLong("Please limit your keywords' length to 100 characters");
                                    }

                                    if (firstUserString.equals("") && secondUserString.equals("") && thirdUserString.equals("")) {

                                        showMessageLong("Please fill out the username information above");
                                    }

                                    // Have this here instead of top to check length of xKeywordsString before doing this work.
                                    List<String> firstKeywordsList = Arrays.asList(firstKeywordsString.split(","));
                                    List<String> secondKeywordsList = Arrays.asList(secondKeywordsString.split(","));
                                    List<String> thirdKeywordsList = Arrays.asList(thirdKeywordsString.split(","));

                                    if (firstKeywordsList.size() > maxNumberOfKeywordsPerUsername) {

                                        firstKeywords.requestFocus();
                                        showMessageLong("Please limit the number of keywords per username to " + maxNumberOfKeywordsPerUsername);
                                    }

                                    if (secondKeywordsList.size() > maxNumberOfKeywordsPerUsername) {

                                        secondKeywords.requestFocus();
                                        showMessageLong("Please limit the number of keywords per username to " + maxNumberOfKeywordsPerUsername);
                                    }

                                    if (thirdKeywordsList.size() > maxNumberOfKeywordsPerUsername) {

                                        thirdKeywords.requestFocus();
                                        showMessageLong("Please limit the number of keywords per username to " + maxNumberOfKeywordsPerUsername);
                                    }

                                    launchPurchaseFlow(skuDetails);
                                });
                            } else {

                                Log.w(TAG, "verifyPurchase() -> acct.getEmail() == null");
                                showMessageLong("Google email account is null. Restart app and try again.");
                            }
                        } else {

                            Log.w(TAG, "verifyPurchase() -> acct == null");
                            showMessageLong("Google account is null. Restart app and try again.");
                        }
                    }
                }
            }
        });
    }

    void launchPurchaseFlow(SkuDetails skuDetails) {

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build();

        billingClient.launchBillingFlow(SubscribePage.this, billingFlowParams);
    }

    @Override
    protected void onStart() {

        super.onStart();
        Log.i(TAG, "onStart()");
    }

    @Override
    protected void onResume() {

        super.onResume();

        billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS, (billingResult, list) -> {

                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

                        for (Purchase purchase : list) {

                            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged()) {

                                verifySubPurchase(purchase);
                            }
                        }
                    }
                }
        );
    }

    void verifySubPurchase(Purchase purchases) {

        AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams
                .newBuilder()
                .setPurchaseToken(purchases.getPurchaseToken())
                .build();

        billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {

            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

                SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences("sharedPreferences", MODE_PRIVATE);
                sharedPrefs.edit().putBoolean("subbed", true).apply();
                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
                if (acct != null) {

                    if (acct.getEmail() != null) {

                        // Firebase does not allow ".", so replace them with ",".
                        String userEmailFirebase = acct.getEmail().replace(".", ",");
                        if (!firstKeywordsString.equals("")) {

                            FirebaseDatabase.getInstance().getReference().child("Users").child(userEmailFirebase).child(firstUserString).setValue("keywordsString", firstKeywordsString);
                        }

                        if (!secondKeywordsString.equals("")) {

                            FirebaseDatabase.getInstance().getReference().child("Users").child(userEmailFirebase).child(secondUserString).setValue("keywordsString", secondKeywordsString);
                        }

                        if (!thirdKeywordsString.equals("")) {

                            FirebaseDatabase.getInstance().getReference().child("Users").child(userEmailFirebase).child(thirdUserString).setValue("keywordsString", thirdKeywordsString);
                        }

                        // Send token to Firebase so notifications can be received.
                        Project5MessagingService project5MessagingService = new Project5MessagingService();
                        project5MessagingService.updateTokenInFirebaseFromSharedPrefs();

                        Intent Activity = new Intent(this, SubsPage.class);
                        startActivity(Activity);
                    } else {

                        Log.w(TAG, "verifyPurchase() -> acct.getEmail() == null");
                        showMessageLong("Google email account is null. Restart app and try again.");
                    }
                } else {

                    Log.w(TAG, "verifyPurchase() -> acct == null");
                    showMessageLong("Google account is null. Restart app and try again.");
                }
            }
        });

        Log.d(TAG, "Purchase Token: " + purchases.getPurchaseToken());
        Log.d(TAG, "Purchase Time: " + purchases.getPurchaseTime());
        Log.d(TAG, "Purchase OrderID: " + purchases.getOrderId());
    }

    @Override
    protected void onDestroy() {

        confirmationButton.setOnClickListener(null);

        super.onDestroy();
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