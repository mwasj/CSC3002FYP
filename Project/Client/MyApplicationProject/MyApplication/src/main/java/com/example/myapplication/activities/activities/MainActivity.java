package com.example.myapplication.activities.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.constants.WcfConstants;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.interfaces.GCMRegistrationCallback;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.GCMRegistrationTask;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.gson.reflect.TypeToken;
import java.util.UUID;

/**
 * Main Activity starts the application. Contacts the web service to see if user has established a permanent session earlier and attempts to auto-login.
 * It also performs the necessary checks for the availability of Google Play services.
 */
public class MainActivity extends BaseActivity implements WCFServiceCallback<User, Void>, GCMRegistrationCallback {

    private final String TAG = "Main Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(getActionBar() != null)
        {
            getActionBar().setHomeButtonEnabled(false);
            getActionBar().setDisplayHomeAsUpEnabled(false);
        }

        WcfConstants.DEV_MODE =true;
        // Check device for Play Services APK.
        if (!checkPlayServices()) {
            // If this check succeeds, proceed with normal processing.
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setMessage("For optimal experience, please ensure that Google Play is installed and that your google account is set up on your device. You will not be able to receive notifications and instant messages in real time until Google Play is installed.")
                    .setCancelable(false)
                    .setTitle("Google Play is not available on this device.")
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            getAppManager().setRegistrationId(null);
                            performAutoLogin();
                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();
        }
        else //Google Play services available.
        {
            if(getAppManager().getRegistrationId() == null)
            {
                Log.i(TAG, "GCM Registration Id is empty, attempting to register device.");
                new GCMRegistrationTask(this, this).execute();
            }
            else
            {
                Log.i(TAG, "Current GCM registration id: "+ getAppManager().getRegistrationId());
                performAutoLogin();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    /**
     * Calls the WCF service and attempts to perform auto-login of the current user.
     * If auto-login succeeds, user is automatically transferred to their home screen.
     * Otherwise manual login activity is started to allow the user to log in by providing their username and password.
     **/
    private void performAutoLogin()
    {
        // Generate new random UUID for the duration of this session.
        getAppManager().setUUID(UUID.randomUUID().toString());
        Log.i(TAG, "New UUID generated, " + getAppManager().getUUID());

        new WcfPostServiceTask<Void>(this, getResources().getString(R.string.UserAutoLoginURL), null, new TypeToken<ServiceResponse<User>>() {}.getType(),
                getAppManager().getAuthorisationHeaders(), this).execute();
    }

    /***
     * WcfPostServiceTask callback function invoked when a response from the WCF service is retrieved.
     * If auto-login was successful, transfer the current user directly to their home page,
     * otherwise display the login page and ask the user to log in manually.
     * @param serviceResponse - Service Response received from the server after trying to log the user in automatically.
     * @param v - empty parameter.
     */
    @Override
    public void onServiceCallCompleted(ServiceResponse<User> serviceResponse, Void v)
    {
        getAppManager().setUser(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS ? serviceResponse.Result : null);

        startActivity(new Intent(this, serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS ? HomeActivity.class : LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        
        finish();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices()
    {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS)
        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        getPLAY_SERVICES_RESOLUTION_REQUEST()).show();
            }
            return false;
        }
        return true;
    }

    /**
     * Called by the GCMRegistration task on completion of the GCM registration process.
     * @param registrationId - Contains the Registration Id returned from the GCM service.
     */
    @Override
    public void onGCMRegistrationCompleted(String registrationId)
    {
        getAppManager().setRegistrationId(registrationId);
        if(registrationId == null)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setMessage("Could not register with Google Cloud Messaging. Instant Messenger features will not be available. The app will try to re-register at next launch.")
                    .setCancelable(false)
                    .setTitle("GCM unavailable.")
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            performAutoLogin();
                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();
        }
        else
        {
            Log.i(TAG, "GCM Registration completed, the new registration id is: " + registrationId);
            performAutoLogin();
        }
    }
}
