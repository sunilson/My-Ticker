package com.sunilson.pro4;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.firebase.database.FirebaseDatabase;

/**
 * @author Linus Weiss
 */

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //Activate Disk Persistence of Firebase
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    /**
     * Returns current internet state
     *
     * @return True if internet is available, false if not
     */
    public boolean getInternetConnected() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
