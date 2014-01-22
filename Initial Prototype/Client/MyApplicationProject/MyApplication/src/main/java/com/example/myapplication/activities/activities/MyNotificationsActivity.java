package com.example.myapplication.activities.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.adapters.NotificationsAdapter;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.dtos.LoadRangeDTO;
import com.example.myapplication.dtos.Notification;
import com.example.myapplication.dtos.ServiceResponse;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WCFServiceTask;
import com.example.myapplication.R;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Created by Michal on 06/01/14.
 */
public class MyNotificationsActivity extends BaseActivity implements WCFServiceCallback<ArrayList<Notification>, Void> {

    private ListView mainListView;
    private ArrayList<Notification> notifications;
    private NotificationsAdapter notificationsAdapter;
    private ProgressBar progressBar;
    private Button loadMoreButton;
    private int currentScrollPosition;
    private Boolean loadMoreData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_notifications);
        progressBar = (ProgressBar) findViewById(R.id.ActivityMyNotificationsProgressBar);
        loadMoreButton = (Button) findViewById(R.id.ActivityMyNotificationsLoadMoreButton);
        loadMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentScrollPosition = mainListView.getFirstVisiblePosition();
                loadMoreData = true;
                getNotifications();
            }
        });
        mainListView = (ListView) findViewById(R.id.MyNotificationsActivityMainListView);
        mainListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastItem = firstVisibleItem + visibleItemCount;
                if(lastItem == totalItemCount && totalItemCount > 0) {
                    // Last item is fully visible.
                    loadMoreButton.setVisibility(View.VISIBLE);
                    mainListView.setPadding(0, 0, 0, loadMoreButton.getHeight());
                }
                else
                {
                    loadMoreButton.setVisibility(View.GONE);
                    mainListView.setPadding(0, 0, 0, 0);
                }
            }
        });
        notifications = new ArrayList<Notification>();
        notificationsAdapter = new NotificationsAdapter(this, R.layout.activity_notifications, notifications);
        mainListView.setAdapter(notificationsAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        getNotifications();
    }

    private void getNotifications()
    {
        progressBar.setVisibility(View.VISIBLE);
        new WCFServiceTask<LoadRangeDTO>(getResources().getString(R.string.GetAllNotificationsURL),
                new LoadRangeDTO(appData.getUser().UserId, mainListView.getCount(),appData.getItemsPerCall(), loadMoreData),
                new TypeToken<ServiceResponse<ArrayList<Notification>>>() {}.getType(),
                appData.getAuthorisationHeaders(), this).execute();
    }
    @Override
    public void onServiceCallCompleted(final ServiceResponse<ArrayList<Notification>> serviceResponse, Void parameter) {
        super.checkIfAuthorised(serviceResponse.ServiceResponseCode);
        TextView noRequestsTextView = (TextView) findViewById(R.id.MyNotificationsActivityNoNotificationsTextView);
        progressBar.setVisibility(View.GONE);
        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            if(serviceResponse.Result.size() == 0 && mainListView.getCount() == 0)
            {
                noRequestsTextView.setVisibility(View.VISIBLE);
                mainListView.setVisibility(View.GONE);
            }
            else
            {
                if(serviceResponse.Result.size() < appData.getItemsPerCall())
                {
                    loadMoreButton.setText("No more data to load");
                    loadMoreButton.setEnabled(false);
                }

                noRequestsTextView.setVisibility(View.GONE);
                mainListView.setVisibility(View.VISIBLE);

                final CharSequence options[] = new CharSequence[] {"Take action", "Archive"};
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                if(loadMoreData)
                {
                    notifications.addAll(notifications.size() == 0 ? 0 : notifications.size(), serviceResponse.Result);
                    loadMoreData = false;
                }
                else
                {
                    notifications.clear();
                    notifications.addAll(serviceResponse.Result);
                }

                notificationsAdapter.notifyDataSetInvalidated();
                mainListView.setSelectionFromTop(currentScrollPosition, 0);
                mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        markNotificationAsRead(notifications.get(i).NotificationId);
                        builder.setTitle("Selected notification");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        builder.show();
                    }
                });
            }

            /*ArrayList<Integer> ids = new ArrayList<Integer>();

            for (JourneyRequest request : serviceResponse.Result)
            {
                ids.add(request.JourneyId);
            }

            new WCFServiceTask<ArrayList<Integer>, ArrayList<Journey>>(getResources().getString(R.string.GetManyJourneysURL), ids,
                    new TypeToken<ServiceResponse<ArrayList<Journey>>>() {}.getType(),
                    appData.getAuthorisationHeaders(),null, new WCFServiceCallback<ArrayList<Journey>, String>() {

                @Override
                public void onServiceCallCompleted(ServiceResponse<ArrayList<Journey>> filteredJourneys, String parameter) {
                    for(JourneyRequest request : serviceResponse.Result)
                    {
                        for(Journey journey : filteredJourneys.Result)
                        {
                            if(request.JourneyId == journey.JourneyId)
                            {
                                request.Journey = journey;
                            }
                        }
                    }



                }
            }).execute();*/
        }
    }

    private void markNotificationAsRead(int id)
    {
        new WCFServiceTask<Integer>(getResources().getString(R.string.MarkNotificationAsReadURL),
                id,
                new TypeToken<ServiceResponse<Boolean>>() {}.getType(),
                appData.getAuthorisationHeaders(), new WCFServiceCallback<Boolean, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {

            }
        }).execute();
    }
}