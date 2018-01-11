package com.sunilson.pro4;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.multidex.MultiDexApplication;

/**
 * @author Linus Weiss
 */

public class BaseApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        //Activate Disk Persistence of Firebase
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
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
