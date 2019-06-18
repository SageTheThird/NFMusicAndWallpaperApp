package com.obcomdeveloper.realmusic.Utils;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

public class Ads {
    private static final String TAG = "Ads";


    public void initAdMob(Context context){

        MobileAds.initialize(context,
                "ca-app-pub-3940256099942544~3347511713");
    }

    public void setupBanner(final AdView adView){


        AdRequest adRequest = new AdRequest.Builder().build();

        adView.loadAd(adRequest);

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
                AdRequest adRequest = new AdRequest.Builder().build();
                adView.loadAd(adRequest);

            }
        });

    }

    public void setupInterstitial(Context context, String unit_ID, final InterstitialAd mInterstitialAd){
        //interstitial
        mInterstitialAd.setAdUnitId(unit_ID);
        //load it
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.d(TAG, "onAdLoaded: called");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                Log.d(TAG, "onAdFailedToLoad: called");
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
                Log.d(TAG, "onAdOpened: called");
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
                Log.d(TAG, "onAdClicked: called");
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
                Log.d(TAG, "onAdLeftApplication: called");
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the interstitial ad is closed.
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
                Log.d(TAG, "onAdClosed: called");
            }
        });


    }
    public void loadinterstitial(InterstitialAd interstitialAd){

        //display it
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else {
            Log.d(TAG, "The interstitial wasn't loaded yet.");
        }
    }
}
