package com.example.myapplication.activities.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseMapActivity;
import com.example.myapplication.adapters.JourneyRequestAdapter;
import com.example.myapplication.adapters.PassengersAdapter;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.JourneyStatus;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.domain_objects.JourneyRequest;
import com.example.myapplication.domain_objects.Notification;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.dtos.JourneyUserDTO;
import com.example.myapplication.utilities.DialogCreator;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.notification_management.NotificationProcessor;
import com.example.myapplication.utilities.Utilities;
import com.google.android.gms.maps.MapFragment;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * This activity is used to display the details of the journey being passed in.
 * It also contains a control panel for the user to perform various operations on the journey such as cancelling, making changes, viewing passengers etc.
 * The options that are visible to the user depend on whether they are the passengers or the driver of this journey.
 * Passengers for example, cannot cancel the journey while drivers can or
 * Passengers can withdraw themselves from a journey while drivers cannot.
 **/
public class JourneyDetailsActivity extends BaseMapActivity implements View.OnClickListener{

    private Journey journey;

    private int newMessagesCount;
    private int newRequestsCount;

    private Button showRequestsButton;
    private Button showPassengersButton;
    private Button enterChatButton;
    private Button makeChangeButton;
    private Button cancelJourneyButton;
    private Button summaryButton;
    private Button withdrawFromJourneyButton;
    private Button rateDriverButton;

    TextView statusTextView;

    private final String TAG = "Journey Details Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_details);

        // Extract data from the bundle.
        Bundle extras = getIntent().getExtras();

        // Check to see if there is a pending notification to be marked as delivered inside the bundle.
        Notification notification = gson.fromJson(extras.getString(IntentConstants.NOTIFICATION), new TypeToken<Notification>() {}.getType());

        // If there is, go ahead and mark it as delivered.
        if(notification != null)
        {
            new NotificationProcessor().MarkDelivered(this, appManager, notification, new WCFServiceCallback<Boolean, Void>() {
                @Override
                public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
                    Log.i(TAG, "Notification successfully marked as delivered");
                }
            });
        }

        journey = gson.fromJson(extras.getString(IntentConstants.JOURNEY), new TypeToken<Journey>() {}.getType());

        newMessagesCount = extras.getInt(IntentConstants.NEW_JOURNEY_MESSAGES);
        newRequestsCount = extras.getInt(IntentConstants.NEW_JOURNEY_REQUESTS);

        // Initialise all UI elements and setup the event handlers.

        // The journey requests button.
        showRequestsButton = (Button) findViewById(R.id.MyJourneyDetailsActivityShowRequestsButton);
        showRequestsButton.setEnabled(journey.Driver.getUserId() == appManager.getUser().getUserId());
        showRequestsButton.setOnClickListener(this);
        showRequestsButton.setVisibility(journey.Driver.getUserId() == appManager.getUser().getUserId() ? View.VISIBLE : View.GONE);

        // The journey passengers button.
        showPassengersButton = (Button) findViewById(R.id.MyJourneyDetailsActivityShowPassengersButton);
        showPassengersButton.setOnClickListener(this);

        // The journey chat room button.
        enterChatButton = (Button) findViewById(R.id.MyJourneyDetailsActivityEnterChatButton);
        enterChatButton.setOnClickListener(this);

        // The make change to a journey button.
        makeChangeButton = (Button) findViewById(R.id.MyJourneyDetailsActivityMakeChangeButton);
        makeChangeButton.setEnabled(journey.Driver.getUserId() == appManager.getUser().getUserId() && journey.JourneyStatus == JourneyStatus.OK);
        makeChangeButton.setVisibility(journey.Driver.getUserId() == appManager.getUser().getUserId() ? View.VISIBLE : View.GONE);
        makeChangeButton.setOnClickListener(this);

        // The cancel journey button.
        cancelJourneyButton = (Button) findViewById(R.id.MyJourneyDetailsActivityCancelJourneyButton);
        cancelJourneyButton.setEnabled(journey.Driver.getUserId() == appManager.getUser().getUserId() && journey.JourneyStatus == JourneyStatus.OK);
        cancelJourneyButton.setVisibility(journey.Driver.getUserId() == appManager.getUser().getUserId() ? View.VISIBLE : View.GONE);
        cancelJourneyButton.setOnClickListener(this);

        // The withdraw from journey button.
        withdrawFromJourneyButton = (Button) findViewById(R.id.MyJourneyDetailsActivityWithdrawFromJourneyButton);
        withdrawFromJourneyButton.setEnabled(journey.Driver.getUserId() != appManager.getUser().getUserId() && journey.JourneyStatus == JourneyStatus.OK);
        withdrawFromJourneyButton.setVisibility(journey.Driver.getUserId() != appManager.getUser().getUserId() ? View.VISIBLE : View.GONE);
        withdrawFromJourneyButton.setOnClickListener(this);

        // The rate driver button.
        rateDriverButton = (Button) findViewById(R.id.MyJourneyDetailsActivityRateDriverButton);
        rateDriverButton.setEnabled(!(journey.Driver.getUserId() == appManager.getUser().getUserId()));
        rateDriverButton.setVisibility(!(journey.Driver.getUserId() == appManager.getUser().getUserId()) ? View.VISIBLE : View.GONE);
        rateDriverButton.setOnClickListener(this);

        // The journey summary button.
        summaryButton = (Button) findViewById(R.id.MyJourneyDetailsActivityShowSummaryButton);
        summaryButton.setOnClickListener(this);

        TextView headerTextView = (TextView) findViewById(R.id.JourneyDetailsActivityHeaderTextView);
        headerTextView.setText(Utilities.getJourneyHeader(journey.GeoAddresses));

        statusTextView = (TextView) findViewById(R.id.JourneyDetailsActivityStatusTextView);

        String statusText = "";

        switch(journey.JourneyStatus)
        {
            case JourneyStatus.OK:
                statusText = "OK";
                break;
            case JourneyStatus.Cancelled:
                statusText = "Cancelled";
                break;
            case JourneyStatus.Expired:
                statusText = "Expired";
                break;
        }

        statusTextView.setText(statusText);

        try {
            // Loading map
            initialiseMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        retrieveJourney();

        // Are there any new messages/requests? if so, make sure we mark them.
        showRequestsButton.setCompoundDrawablesWithIntrinsicBounds(null,
                newRequestsCount == 0 ? getResources().getDrawable(R.drawable.home_activity_notification) :
                        getResources().getDrawable(R.drawable.home_activity_notification_new), null, null);

        enterChatButton.setCompoundDrawablesWithIntrinsicBounds(null,
                newMessagesCount == 0 ? getResources().getDrawable(R.drawable.journey_chat) :
                        getResources().getDrawable(R.drawable.journey_chat_new_message), null, null);
    }


    /**
     * Used to retrieve the most up-to-date information about the current journey.
     **/
    private void retrieveJourney()
    {
        new WcfPostServiceTask<Integer>(this, getResources().getString(R.string.GetSingleJourneyURL), journey.getJourneyId(),
                new TypeToken<ServiceResponse<Journey>>() {}.getType(),
                appManager.getAuthorisationHeaders(), new WCFServiceCallback<Journey, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Journey> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    if(serviceResponse.Result != null)
                    {
                        journeyRetrieved(serviceResponse.Result);
                    }
                    else
                    {
                        errorCouldNotRetrieveJourney();
                    }
                }
            }
        }).execute();
    }

    private void errorCouldNotRetrieveJourney()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Error while retrieving current journey. Please try again later.")
                .setCancelable(false)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        finish();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void journeyRetrieved(Journey journey)
    {
        this.journey = journey;
        super.drawDrivingDirectionsOnMap(googleMap, journey.GeoAddresses);
    }

    private void initialiseMap() {

        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.FragmentJourneyDetailsMap)).getMap();

            if (googleMap == null) {
                Toast.makeText(this,
                        "Unable to initialise Google Maps, please check your network connection.", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void showRateDriverActivity()
    {
        startActivity(new Intent(this, RateDriverActivity.class).putExtra(IntentConstants.JOURNEY, gson.toJson(journey)));
    }

    private void showSummaryActivity()
    {
        startActivity(new Intent(this, JourneySummaryActivity.class).putExtra(IntentConstants.JOURNEY, gson.toJson(journey)));
    }

    private void showWithdrawQuestionDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Leave journey?");
        builder.setMessage("Are you sure you want to withdraw yourself from this journey? You will be removed from the list of passengers.");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                withdrawFromJourney();
                dialog.dismiss();
            }

        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showCancelJourneyDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Cancel journey?");
        builder.setMessage("Are you sure you want to cancel this journey?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                cancelJourney();
                dialog.dismiss();
            }

        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void enterChatRoom() {
        Bundle bundle = new Bundle();
        bundle.putInt(IntentConstants.JOURNEY, journey.getJourneyId());
        startActivity(new Intent(this, JourneyChatActivity.class).putExtras(bundle));
        newMessagesCount = 0;
    }


    private void retrieveJourneyRequests()
    {
        new WcfPostServiceTask<Integer>(this, getResources().getString(R.string.GetRequestsForJourneyURL),
                journey.getJourneyId(),
                new TypeToken<ServiceResponse<ArrayList<JourneyRequest>>>() {}.getType(),
                appManager.getAuthorisationHeaders(), new WCFServiceCallback<ArrayList<JourneyRequest>, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<ArrayList<JourneyRequest>> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    showRequests(serviceResponse.Result);
                }
            }
        }).execute();
    }

    private void showRequests(final ArrayList<JourneyRequest> requests)
    {
        // Show the journey requests dialog.
        final Dialog requestsDialog = new Dialog(this);
        requestsDialog.setContentView(R.layout.dialog_show_journey_requests);
        requestsDialog.setTitle("Requests");
        ListView requestsListView = (ListView) requestsDialog.findViewById(R.id.JourneyActivityRequestsListView);
        requestsListView.setVisibility(requests.size() == 0 ? View.GONE : View.VISIBLE);
        TextView noRequestsTextView = (TextView) requestsDialog.findViewById(R.id.JourneyActivityRequestsNoRequestsTextView);
        noRequestsTextView.setVisibility(requests.size() == 0 ? View.VISIBLE : View.GONE);

        if(requests.size() > 0)
        {
            JourneyRequestAdapter adapter = new JourneyRequestAdapter(appManager, this, R.layout.listview_row_journey_request, requests);
            requestsListView.setAdapter(adapter);
            requestsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    newRequestsCount = 0;
                    showRequestDialog(requests.get(i));
                    requestsDialog.dismiss();
                }
            });
        }

        requestsDialog.show();
    }

    private void showRequestDialog(JourneyRequest journeyRequest)
    {
        Bundle bundle = new Bundle();
        bundle.putString(IntentConstants.JOURNEY_REQUEST, gson.toJson(journeyRequest));
        startActivity(new Intent(this, JourneyRequestDialogActivity.class).putExtras(bundle));
    }

    private void showPassengers()
    {
        // Show the journey requests dialog.
        final Dialog passengersDialog = new Dialog(this);
        passengersDialog.setContentView(R.layout.dialog_show_passengers);
        passengersDialog.setTitle("Passengers");
        ListView passengersListView = (ListView) passengersDialog.findViewById(R.id.AlertDialogShowPassengersListView);
        passengersListView.setVisibility(journey.Participants.size() == 0 ? View.GONE : View.VISIBLE);
        TextView noPassengers = (TextView) passengersDialog.findViewById(R.id.AlertDialogShowPassengersNoPassengersTextView);
        noPassengers.setVisibility(journey.Participants.size() == 0 ? View.VISIBLE : View.GONE);

        if(journey.Participants.size() > 0)
        {
            PassengersAdapter adapter = new PassengersAdapter(appManager, this, R.layout.listview_row_journey_passengers, journey.Participants);
            passengersListView.setAdapter(adapter);
            passengersListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l)
                {
                    DialogCreator.ShowProfileOptionsDialog(passengersDialog.getContext(), journey.Participants.get(i));
                }
            });
        }

        passengersDialog.show();
    }

    private void withdrawFromJourney()
    {
        new WcfPostServiceTask<JourneyUserDTO>(this, getResources().getString(R.string.WithdrawFromJourneyURL),
                new JourneyUserDTO(journey.getJourneyId(), appManager.getUser().getUserId()),
                new TypeToken<ServiceResponse<Boolean>>() {}.getType(),
                appManager.getAuthorisationHeaders(), new WCFServiceCallback<Boolean, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    successfullyWithdrawnFromJourney();
                }
            }
        }).execute();
    }

    private void successfullyWithdrawnFromJourney()
    {
        Toast toast = Toast.makeText(this, "You have been successfully withdrawn from this journey.", Toast.LENGTH_LONG);
        toast.show();
        finish();
    }

    private void cancelJourney()
    {
        new WcfPostServiceTask<JourneyUserDTO>(this, getResources().getString(R.string.CancelJourneyURL),
                new JourneyUserDTO(journey.getJourneyId(), appManager.getUser().getUserId()),
                new TypeToken<ServiceResponse<Boolean>>() {}.getType(),
                appManager.getAuthorisationHeaders(), new WCFServiceCallback<Boolean, Void>() {
            @Override
            public void onServiceCallCompleted(ServiceResponse<Boolean> serviceResponse, Void parameter) {
                if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
                {
                    successfullyCancelledJourney();
                }
            }
        }).execute();
    }

    private void successfullyCancelledJourney()
    {
        Toast.makeText(this, "This journey has been successfully cancelled.", Toast.LENGTH_LONG).show();
        statusTextView.setText("Cancelled");
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.MyJourneyDetailsActivityShowRequestsButton:
                retrieveJourneyRequests();
                break;
            case R.id.MyJourneyDetailsActivityShowPassengersButton:
                showPassengers();
                break;
            case R.id.MyJourneyDetailsActivityEnterChatButton:
                enterChatRoom();
                break;
            case R.id.MyJourneyDetailsActivityMakeChangeButton:
                startEditor();
                break;
            case R.id.MyJourneyDetailsActivityWithdrawFromJourneyButton:
                showWithdrawQuestionDialog();
                break;
            case R.id.MyJourneyDetailsActivityCancelJourneyButton:
                showCancelJourneyDialog();
                break;
            case R.id.MyJourneyDetailsActivityRateDriverButton:
                showRateDriverActivity();
                break;
            case R.id.MyJourneyDetailsActivityShowSummaryButton:
                showSummaryActivity();
                break;
        }
    }

    private void startEditor()
    {
        Bundle bundle = new Bundle();
        bundle.putInt(IntentConstants.JOURNEY_CREATOR_MODE, IntentConstants.JOURNEY_CREATOR_MODE_EDITING);
        bundle.putString(IntentConstants.JOURNEY ,gson.toJson(journey));
        startActivity(new Intent(this, OfferJourneyStepOneActivity.class).putExtras(bundle));
    }
}
