package com.example.myapplication.activities.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.constants.GcmConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.dtos.ServiceResponse;
import com.example.myapplication.dtos.User;
import com.example.myapplication.experimental.WakeLocker;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WCFServiceTask;
import com.example.myapplication.R;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Created by Michal on 14/01/14.
 */
public class HomeActivity extends BaseActivity {

    private LinearLayout myJourneysLayout;
    private LinearLayout searchLayout;
    private LinearLayout notificationsLayout;
    private LinearLayout friendsLayout;

    private TextView generalNotificationsCountTextView;
    private TextView unreadMessagesCountTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        actionBar.setTitle(" Hi " + appData.getUser().UserName);
        myJourneysLayout = (LinearLayout) findViewById(R.id.ActivityHomeMyJourneysLayout);
        searchLayout = (LinearLayout) findViewById(R.id.ActivityHomeSearchLayout);
        notificationsLayout = (LinearLayout) findViewById(R.id.ActivityHomeNotificationsLayout);
        friendsLayout = (LinearLayout) findViewById(R.id.ActivityHomeFriendsLayout);

        generalNotificationsCountTextView = (TextView) findViewById(R.id.ActivityHomeNotificationCountTextView);
        unreadMessagesCountTextView =(TextView) findViewById(R.id.HomeActivityUnreadMessagesCountTextView);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GcmConstants.PROPERTY_ACTION_REFRESH);
        registerReceiver(GCMReceiver, intentFilter);
        setupUIEvents();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        super.exitApp(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout_menu_option:
                super.exitApp(true);
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        appData.setCurrentlyVisibleActivity(this.getLocalClassName());
        refreshInformation();
    }

    private void refreshInformation()
    {
        new WCFServiceTask<Integer>(getResources().getString(R.string.RefreshUserURL), appData.getUser().UserId,
                new TypeToken<ServiceResponse<User>>() {}.getType(),
                appData.getAuthorisationHeaders(), new WCFServiceCallback<User, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<User> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    userRefreshed(serviceResponse.Result);
                }
            }
        }).execute();

        new WCFServiceTask<Integer>(getResources().getString(R.string.GetUnreadNotificationsCountURL), appData.getUser().UserId,
                new TypeToken<ServiceResponse<Integer>>() {}.getType(),
                appData.getAuthorisationHeaders(), new WCFServiceCallback<Integer, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Integer> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    notificationCountRetrieved(serviceResponse.Result);
                }
            }
        }).execute();

        new WCFServiceTask<Integer>(getResources().getString(R.string.GetUnreadMessagesCountURL), appData.getUser().UserId,
                new TypeToken<ServiceResponse<Integer>>() {}.getType(),
                appData.getAuthorisationHeaders(), new WCFServiceCallback<Integer, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Integer> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    unreadMessagesCountRetrieved(serviceResponse.Result);
                }
            }
        }).execute();

        /*new WCFServiceTask<Integer>(getResources().getString(R.string.GetFriendsURL), appData.getUser().UserId,
                new TypeToken<ServiceResponse<ArrayList<User>>>() {}.getType(), appData.getAuthorisationHeaders(), new WCFServiceCallback<ArrayList<User>, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<ArrayList<User>> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    friendsRetrieved(serviceResponse.Result);
                }
            }
        }).execute();*/
    }

    private void setupUIEvents()
    {
        myJourneysLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MyJourneysActivity.class);
                startActivity(intent);
            }
        });

        searchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                startActivity(intent);
            }
        });

        notificationsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MyNotificationsActivity.class);
                startActivity(intent);
            }
        });

        friendsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), FriendsListActivity.class);
                startActivity(intent);
            }
        });
    }

    private final BroadcastReceiver GCMReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            WakeLocker.acquire(getApplicationContext());
            onResume();
            WakeLocker.release();
        }
    };

    private void notificationCountRetrieved(int count)
    {
        generalNotificationsCountTextView.setText(count + " " + getResources().getString(R.string.New) + (count == 0 ? "" : "!"));
        generalNotificationsCountTextView.setTypeface(null, count == 0 ? Typeface.NORMAL : Typeface.BOLD);
    }

    private void userRefreshed(User user)
    {
        appData.setUser(user);
    }

    private void unreadMessagesCountRetrieved(int count)
    {
        unreadMessagesCountTextView.setText(count + " Messages");
        unreadMessagesCountTextView.setTypeface(null, count == 0 ? Typeface.NORMAL : Typeface.BOLD);
    }

    private void friendsRetrieved(ArrayList<User> friends)
    {
        appData.setFriends(friends);
    }
}