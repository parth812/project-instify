package com.instify.android.app;

/**
 * Created by Abhish3k on 1/8/2017.
 */

import android.content.Intent;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.facebook.stetho.Stetho;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crash.FirebaseCrash;
import com.instify.android.BuildConfig;
import com.instify.android.helpers.PreferenceManager;
import com.instify.android.helpers.SQLiteHandler;
import com.instify.android.ux.IntroActivity;
import com.squareup.leakcanary.LeakCanary;
import com.thefinestartist.Base;
import timber.log.Timber;

public class AppController extends MultiDexApplication {
  public static final String TAG = AppController.class.getSimpleName();

  private static AppController mInstance;
  public FirebaseAnalytics mFirebaseAnalytics;
  private RequestQueue mRequestQueue;
  private PreferenceManager mPrefs;

  static {
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
  }

  public static synchronized AppController getInstance() {
    return mInstance;
  }

  @Override public void onCreate() {
    super.onCreate();
    mInstance = this;
    FirebaseApp.initializeApp(this);
    // Fixes crash reported on Firebase, issue : https://github.com/TheFinestArtist/FinestWebView-Android/issues/79
    Base.initialize(this);
    // Check Build Config for debugging libraries
    if (BuildConfig.DEBUG) {
      // Plant Tiber debug tree
      Timber.plant(new Timber.DebugTree());
      // Set Crash Reporting to false
      FirebaseCrash.setCrashCollectionEnabled(false);
      // Initialise Leak Canary
      if (LeakCanary.isInAnalyzerProcess(this)) {
        // This process is dedicated to LeakCanary for heap analysis.
        // You should not init your app in this process.
        return;
      }
      LeakCanary.install(this);
      // Initialise Stetho
      Stetho.initializeWithDefaults(this);
    } else {
      // Obtain the FirebaseAnalytics
      mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
      // Set Analytics collection to true
      mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
      // Set Crash Reporting to true
      FirebaseCrash.setCrashCollectionEnabled(true);
    }
  }

  public PreferenceManager getPrefManager() {
    if (mPrefs == null) {
      mPrefs = new PreferenceManager(this);
    }

    return mPrefs;
  }

  public RequestQueue getRequestQueue() {
    if (mRequestQueue == null) {
      mRequestQueue = Volley.newRequestQueue(getApplicationContext());
    }

    return mRequestQueue;
  }

  public <T> void addToRequestQueue(Request<T> req, String tag) {
    req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
    getRequestQueue().add(req);
  }

  public <T> void addToRequestQueue(Request<T> req) {
    req.setTag(TAG);
    getRequestQueue().add(req);
  }

  public void cancelPendingRequests(Object tag) {
    if (mRequestQueue != null) {
      mRequestQueue.cancelAll(tag);
    }
  }

  public void logoutUser() {
    mPrefs.clear();
    // SignOut from Firebase
    FirebaseAuth.getInstance().signOut();
    // Delete database
    SQLiteHandler sqLiteHandler = new SQLiteHandler(this);
    sqLiteHandler.deleteUsers();
    // Set First Run to true
    AppController.getInstance().getPrefManager().setIsFirstRun(true);
    // Launch the intro activity
    Intent intent = new Intent(this, IntroActivity.class);
    // Closing all the Activities & Add new Flag to start new Activity
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
  }
}
