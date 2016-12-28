package com.gellert.sunshine.gcm;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by gellert on 2016. 08. 26..
 */
public class MyInstanceIDListenerService extends InstanceIDListenerService {
    private static final String TAG = "MyInstanceIDLS";

    @Override
    public void onTokenRefresh() {
        Intent intent = new Intent(this,RegistrationIntentService.class);
        startService(intent);
    }
}
