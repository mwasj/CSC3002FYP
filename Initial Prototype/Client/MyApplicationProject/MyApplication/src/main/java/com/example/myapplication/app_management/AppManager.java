package com.example.myapplication.app_management;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;
import android.util.LruCache;

import com.example.myapplication.R;
import com.example.myapplication.activities.activities.LoginActivity;
import com.example.myapplication.constants.SessionConstants;
import com.example.myapplication.constants.SharedPreferencesConstants;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.utilities.Pair;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Created by Michal on 01/01/14.
 */
public class AppManager extends Application {


    private User user;
    private String uuid;
    private String sessionId;
    private String registrationId;
    private String uniqueDeviceId;
    private SharedPreferences sharedPreferences;
    private WifiManager wifiManager;
    private WifiManager.WifiLock wifiLock;
    private ArrayList<Integer> notificationIds;
    private LruCache<String, Bitmap> bitmapLruCache;

    public void addNotificationId(Integer id)
    {
        if(this.notificationIds == null)
        {
            this.notificationIds = new ArrayList<Integer>();
        }

        this.notificationIds.add(id);
    }

    public Boolean hasAppBeenKilled()
    {
        return this.user == null && this.getSessionId().endsWith("0");
    }

    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;

        if(this.sharedPreferences == null)
        {
            this.sharedPreferences = this.retrieveSharedPreferences();
        }

        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        editor.putString(SharedPreferencesConstants.PROPERTY_SESSION_ID, sessionId);
        editor.commit();
    }

    public void login(User u)
    {
        if(this.wifiLock == null)
        {
            this.retrieveWifiLock();
        }

        this.wifiLock.acquire();

        this.setUser(u);
    }

    public void setUser(User u)
    {
        this.user = u;

        if(this.sharedPreferences == null)
        {
            this.sharedPreferences = this.retrieveSharedPreferences();
        }

        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        editor.putString(SharedPreferencesConstants.PROPERTY_USER, u == null ? "" : new Gson().toJson(user));
        editor.commit();
    }

    public LruCache<String, Bitmap> getBitmapLruCache()
    {
        if(this.bitmapLruCache == null)
        {
            this.bitmapLruCache = this.createBitmapLruCache();
        }

        return this.bitmapLruCache;
    }

    public User getUser()
    {
        if(this.user == null)
        {
            if(this.sharedPreferences == null)
            {
                this.sharedPreferences = this.retrieveSharedPreferences();
            }

            this.user = new Gson().fromJson(sharedPreferences.getString(SharedPreferencesConstants.PROPERTY_USER, ""), new TypeToken<User>() {}.getType());
        }

        return this.user;
    }

    public void setUUID(String u)
    {
        this.uuid = u;
    }

    public String getUUID()
    {
        return this.uuid;
    }

    public String getSessionId()
    {
        if(this.sessionId == null)
        {
            this.sessionId = this.retrieveSessionId();
        }

        return this.sessionId;
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    public String getRegistrationId()
    {
        if(this.registrationId == null)
        {
            this.registrationId = this.retrieveRegistrationId();
        }

        return this.registrationId;
    }

    public void setRegistrationId(String registrationId)
    {
        this.registrationId = registrationId;

        if(this.sharedPreferences == null)
        {
            this.sharedPreferences = this.retrieveSharedPreferences();
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferencesConstants.PROPERTY_REG_ID, registrationId);
        editor.putInt(SharedPreferencesConstants.PROPERTY_APP_VERSION, this.getAppVersion());
        editor.commit();
    }
    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    public int getAppVersion() {
        try {
            PackageInfo packageInfo = this.getPackageManager()
                    .getPackageInfo(this.getPackageName(), 0);

            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            e.printStackTrace();
            throw new RuntimeException("Could not get package name: " + e);
        }catch (NullPointerException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public String getUniqueDeviceId()
    {
        if(this.uniqueDeviceId == null)
        {
            this.uniqueDeviceId = this.retrieveUniqueDeviceId();
        }

        return this.uniqueDeviceId;
    }

    public List<Pair> getAuthorisationHeaders()
    {
        return asList(new Pair(SessionConstants.DEVICE_ID, this.getUniqueDeviceId()),
                new Pair(SessionConstants.SESSION_ID, this.getSessionId()),
                new Pair(SessionConstants.UUID, this.getUUID()));
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void logout(final boolean force, boolean setOfflineOnServer)
    {
        if(setOfflineOnServer)
        {
            new WcfPostServiceTask<Boolean>(this, getResources().getString(R.string.UserLogoutURL),
                    force, new TypeToken<ServiceResponse<Boolean>>(){}.getType(), this.getAuthorisationHeaders(), new WCFServiceCallback<Boolean, Void>() {
                @Override
                public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
                }
            }).execute();
        }

        if(getSessionId().endsWith("0") || force)
        {
            setUser(null);
            setSessionId("");
            clearNotifications();
        }

        if(force)
        {

            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            if(this.wifiLock== null)
            {
                this.retrieveWifiLock();
            }

            if(this.wifiLock.isHeld())
            {
                this.wifiLock.release();
            }

        }
    }

    private void clearNotifications()
    {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(getApplicationContext().NOTIFICATION_SERVICE);


        if(this.notificationIds != null)
        {
            for(Integer id : this.notificationIds)
            {
                Log.i(this.getClass().getSimpleName(), "Notification: " + id + " is being cleared");
                notificationManager.cancel(id);
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.sharedPreferences = this.retrieveSharedPreferences();
        this.registrationId = this.retrieveRegistrationId();
        this.sessionId = this.retrieveSessionId();
        this.uniqueDeviceId = this.retrieveUniqueDeviceId();
        this.wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        this.wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "LockTag");
        this.notificationIds = new ArrayList<Integer>();
        this.bitmapLruCache = this.createBitmapLruCache();
    }

    private String retrieveUniqueDeviceId()
    {
        return ""+ Build.BOARD.length()%10+ Build.BRAND.length()%10 +
                Build.CPU_ABI.length()%10 + Build.DEVICE.length()%10 +
                Build.DISPLAY.length()%10 + Build.HOST.length()%10 +
                Build.ID.length()%10 + Build.MANUFACTURER.length()%10 +
                Build.MODEL.length()%10 + Build.PRODUCT.length()%10 +
                Build.TAGS.length()%10 + Build.TYPE.length()%10 +
                Build.USER.length()%10 ;
    }

    private String retrieveSessionId()
    {
        if(this.sharedPreferences == null)
        {
            this.sharedPreferences = this.retrieveSharedPreferences();
        }

        String session = sharedPreferences.getString(SessionConstants.SESSION_ID, "");

        this.sessionId = session == null ? "" : session;

        return this.sessionId;
    }

    private String retrieveRegistrationId()
    {
        if(this.sharedPreferences == null)
        {
            this.sharedPreferences = this.retrieveSharedPreferences();
        }

        String registrationId = sharedPreferences.getString(SharedPreferencesConstants.PROPERTY_REG_ID, "");

        this.registrationId = registrationId == null ? "0" : registrationId;

        return this.registrationId;
    }

    private SharedPreferences retrieveSharedPreferences()
    {
        return getSharedPreferences(SharedPreferencesConstants.GLOBAL_APP_DATA, Context.MODE_PRIVATE);
    }

    private void retrieveWifiLock()
    {
        this.wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        this.wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "LockTag");
    }

    private LruCache<String, Bitmap> createBitmapLruCache()
    {
        return new LruCache<String, Bitmap>((int)(Runtime.getRuntime().maxMemory() / 1024) / 4);
    }
}
