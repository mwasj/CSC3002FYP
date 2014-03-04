package com.example.myapplication.activities.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.interfaces.WCFImageRetrieved;
import com.example.myapplication.network_tasks.WcfPictureServiceTask;
import com.example.myapplication.utilities.DateTimeHelper;
import com.example.myapplication.utilities.Utilities;
import com.google.gson.reflect.TypeToken;

import java.text.DecimalFormat;

/**
 * This activity displays detailed summary of the current journey.
 **/
public class JourneySummaryActivity extends BaseActivity implements WCFImageRetrieved, View.OnClickListener{

    private TextView journeyIdTextView;
    private TextView journeyDriverTextView;
    private TextView journeyDateTextView;
    private TextView journeyTimeTextView;
    private TextView journeySeatsAvailableTextView;
    private TextView journeySmokersTextView;
    private TextView journeyPetsTextView;
    private TextView journeyFeeTextView;
    private TextView journeyVehicleTypeTextView;
    private TextView journeyPrivateTextView;

    private TableRow journeyDriverTableRow;

    private Journey journey;

    private ImageView driverIconImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_summary);

        // Initialise local variables.
        journey = gson .fromJson(getIntent().getStringExtra(IntentConstants.JOURNEY), new TypeToken<Journey>(){}.getType());

        // Initialise UI elements and setup event handlers.
        journeyIdTextView = (TextView) findViewById(R.id.JourneySummaryJourneyIdTextView);
        journeyDriverTextView = (TextView) findViewById(R.id.JourneySummaryJourneyDriverTextView);
        journeyDateTextView = (TextView) findViewById(R.id.JourneySummaryJourneyDateTextView);
        journeyTimeTextView = (TextView) findViewById(R.id.JourneySummaryJourneyTimeTextView);
        journeySeatsAvailableTextView = (TextView) findViewById(R.id.JourneySummaryJourneySeatsTextView);
        journeyPetsTextView = (TextView) findViewById(R.id.JourneySummaryJourneyPetsTextView);
        journeySmokersTextView = (TextView) findViewById(R.id.JourneySummaryJourneySmokersTextView);
        journeyFeeTextView = (TextView) findViewById(R.id.JourneySummaryJourneyFeeTextView);
        journeyVehicleTypeTextView = (TextView) findViewById(R.id.JourneySummaryJourneyVehicleTypeTextView);
        journeyPrivateTextView = (TextView) findViewById(R.id.JourneySummaryJourneyPrivateTextView);

        driverIconImageView = (ImageView) findViewById(R.id.JourneySummaryActivityDriverImageView);

        journeyDriverTableRow = (TableRow) findViewById(R.id.JourneySummaryActivityJourneyDriverTableRow);
        journeyDriverTableRow.setOnClickListener(this);

        // Fill in the details.
        fillJourneyDetails();

        // Retrieve picture of the driver.
        getDriverPicture();
    }

    private void fillJourneyDetails()
    {
        String[] vehicleTypes = getResources().getStringArray(R.array.vehicle_types);

        journeyIdTextView.setText(String.valueOf(journey.getJourneyId()));
        journeyDriverTextView.setText(journey.getDriver().getUserName());
        journeyDateTextView.setText(DateTimeHelper.getSimpleDate(journey.getDateAndTimeOfDeparture()));
        journeyTimeTextView.setText(DateTimeHelper.getSimpleTime(journey.getDateAndTimeOfDeparture()));
        journeySmokersTextView.setText(Utilities.translateBoolean(journey.isSmokersAllowed()));
        journeyPetsTextView.setText(Utilities.translateBoolean(journey.isPetsAllowed()));
        journeyPrivateTextView.setText(Utilities.translateBoolean(journey.isPrivate()));
        journeyVehicleTypeTextView.setText(vehicleTypes[journey.getVehicleType()]);
        journeySeatsAvailableTextView.setText(String.valueOf(journey.getAvailableSeats()));
        journeyFeeTextView.setText(("£"+new DecimalFormat("0.00").format(journey.getFee())) + (journey.getPreferredPaymentMethod() == null ? "" : ", " +journey.getPreferredPaymentMethod()));

    }

    private void getDriverPicture()
    {
        new WcfPictureServiceTask(appManager.getBitmapLruCache(), getResources().getString(R.string.GetProfilePictureURL),
                journey.getDriver().getUserId(), appManager.getAuthorisationHeaders(), this).execute();
    }

    @Override
    public void onImageRetrieved(Bitmap bitmap) {
        if(bitmap != null)
        {
            driverIconImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, bitmap.getWidth()/2, bitmap.getHeight()/2, false));
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.JourneySummaryActivityJourneyDriverTableRow:
                startActivity(new Intent(this, ProfileViewerActivity.class).putExtra(IntentConstants.USER, gson.toJson(journey.getDriver())));
                break;
        }
    }
}
