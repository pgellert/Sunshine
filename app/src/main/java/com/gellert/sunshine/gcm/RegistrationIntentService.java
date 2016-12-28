package com.gellert.sunshine.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.gellert.sunshine.MainActivity;
import com.gellert.sunshine.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

/**
 * Created by gellert on 2016. 08. 26..
 */
public class RegistrationIntentService extends IntentService {
    private static final String TAG = "RegIntentService";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        try{
            synchronized (TAG){
                InstanceID instanceID = InstanceID.getInstance(this);

                String senderId = getString(R.string.gcm_defaultSenderId);
                Log.i(TAG,"Sender ID: " + senderId);
                if ( senderId.length() != 0 ) {
                    String token = instanceID.getToken(senderId,
                            GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                    sendRegistrationToServer(token);
                }

                sp.edit().putBoolean(MainActivity.SENT_TOKEN_TO_SERVER, true).apply();
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            sp.edit().putBoolean(MainActivity.SENT_TOKEN_TO_SERVER, false).apply();
        }
    }

    private void sendRegistrationToServer(String token) {
        Log.i(TAG, "GCM Registration Token: " + token);
    }
}
