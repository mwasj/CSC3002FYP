package com.example.myapplication.activities.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseMapActivity;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.domain_objects.GeoAddress;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.domain_objects.MarkerType;
import com.example.myapplication.experimental.GeocoderParams;
import com.example.myapplication.experimental.WaypointHolder;
import com.example.myapplication.network_tasks.GeocoderTask;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Created by Michal on 22/01/14.
 */
public class OfferJourneyStepOneActivity extends BaseMapActivity {

    private RelativeLayout departureRelativeLayout;
    private RelativeLayout destinationRelativeLayout;
    private RelativeLayout waypointRelativeLayout;

    private Button departureGPSButton;
    private Button destinationGPSButton;
    private Button stepTwoButton;

    private TextView departureTextView;
    private TextView destinationTextView;
    private TextView viaTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_journey_step_one);

        // Initialise variables.
        actionBar.hide();
        this.wayPoints = new ArrayList<WaypointHolder>();

        //Initialise UI elements.
        this.stepTwoButton = (Button) this.findViewById(R.id.OfferJourneyStepOneActivityStepTwoButton);
        this.departureGPSButton = (Button) this.findViewById(R.id.OfferJourneyStepOneActivityDepartureGPSButton);
        this.destinationGPSButton = (Button) this.findViewById(R.id.OfferJourneyStepOneActivityDestinationGPSButton);
        this.departureRelativeLayout = (RelativeLayout) this.findViewById(R.id.OfferJourneyStepOneActivityDepartureRelativeLayout);
        this.destinationRelativeLayout = (RelativeLayout) this.findViewById(R.id.OfferJourneyStepOneActivityDestinationRelativeLayout);
        this.waypointRelativeLayout = (RelativeLayout) this.findViewById(R.id.OfferJourneyStepOneActivityViaRelativeLayout);
        this.departureTextView = (TextView) this.findViewById(R.id.OfferJourneyStepOneActivityDepartureTextView);
        this.destinationTextView = (TextView) this.findViewById(R.id.OfferJourneyStepOneActivityDestinationTextView);
        this.viaTextView = (TextView) this.findViewById(R.id.OfferJourneyStepOneActivityViaTextView);
        // Setting up event handlers.
        this.setupEventHandlers();

        try {
            // Loading map
            initialiseMap();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(googleMap != null)
            centerMapOnMyLocation();
    }

    private void initialiseMap() {

        if (googleMap == null) {
            this.googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.OfferJourneyStepOneActivityMap)).getMap();

            if (googleMap == null) {
                Toast.makeText(this,
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void showWaypointOnMap(WaypointHolder waypointHolder, MarkerOptions markerOptions)
    {
        if(markerOptions != null)
        {
            waypointHolder.googleMapMarker = googleMap.addMarker(new MarkerOptions()
                    .position(markerOptions.getPosition())
                    .title(markerOptions.getTitle())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            waypointHolder.googleMapMarker.showInfoWindow();
        }

        animateCamera();
    }

    private void setupEventHandlers()
    {
        this.stepTwoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildJourneyAndProceed();
            }
        });

        this.departureRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddressSelectionDialog(MarkerType.Departure, departureMarker);
            }
        });

        this.destinationRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddressSelectionDialog(MarkerType.Destination, destinationMarker);
            }
        });

        this.departureGPSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentAddress(MarkerType.Departure, locationClient.getLastLocation());
            }
        });

        this.destinationGPSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentAddress(MarkerType.Destination, locationClient.getLastLocation());
            }
        });

        this.waypointRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showWayPointAddressDialog();
            }
        });
    }

    private void showWayPointAddressDialog()
    {
        final Dialog wayPointDialog = new Dialog(this);
        wayPointDialog.setCanceledOnTouchOutside(true);
        wayPointDialog.setContentView(R.layout.alert_dialog_waypoint_selector);
        wayPointDialog.setTitle("Journey waypoints");

        final EditText firstWayPointEditText  = (EditText) wayPointDialog.findViewById(R.id.AlertDialogWaypointSelectorFirstWaypointEditText);
        final EditText secondWayPointEditText  = (EditText) wayPointDialog.findViewById(R.id.AlertDialogWaypointSelectorSecondWaypointEditText);
        final EditText thirdWayPointEditText  = (EditText) wayPointDialog.findViewById(R.id.AlertDialogWaypointSelectorThirdWaypointEditText);
        final EditText fourthWayPointEditText  = (EditText) wayPointDialog.findViewById(R.id.AlertDialogWaypointSelectorFourthWaypointEditText);
        final EditText fifthWayPointEditText  = (EditText) wayPointDialog.findViewById(R.id.AlertDialogWaypointSelectorFifthWaypointEditText);
        final EditText sixthWayPointEditText  = (EditText) wayPointDialog.findViewById(R.id.AlertDialogWaypointSelectorSixthWaypointEditText);

        for(WaypointHolder waypointHolder : this.wayPoints)
        {
            if(waypointHolder.geoAddress.Order == 1 && waypointHolder.googleMapMarker != null)
            {

                firstWayPointEditText.setText(waypointHolder.googleMapMarker.getTitle());
            }

            if(waypointHolder.geoAddress.Order == 2 && waypointHolder.googleMapMarker != null)
            {
                secondWayPointEditText.setText(waypointHolder.googleMapMarker.getTitle());
            }

            if(waypointHolder.geoAddress.Order == 3 && waypointHolder.googleMapMarker != null)
            {
                thirdWayPointEditText.setText(waypointHolder.googleMapMarker.getTitle());
            }

            if(waypointHolder.geoAddress.Order == 4 && waypointHolder.googleMapMarker != null)
            {
                fourthWayPointEditText.setText(waypointHolder.googleMapMarker.getTitle());
            }

            if(waypointHolder.geoAddress.Order == 5 && waypointHolder.googleMapMarker != null)
            {
                fifthWayPointEditText.setText(waypointHolder.googleMapMarker.getTitle());
            }

            if(waypointHolder.geoAddress.Order == 6 && waypointHolder.googleMapMarker != null)
            {
                sixthWayPointEditText.setText(waypointHolder.googleMapMarker.getTitle());
            }
        }


        Button okButton = (Button) wayPointDialog.findViewById(R.id.AlertDialogWaypointSelectorOkButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                for(WaypointHolder waypointHolder : wayPoints)
                {
                    waypointHolder.removeMarker();
                }

                wayPoints.clear();
                viaTextView.setText("Add new waypoint (optional)");
                int wayPointCounter = 1;
                if(!firstWayPointEditText.getText().toString().isEmpty())
                {
                    addressDialogClosed(MarkerType.Waypoint, firstWayPointEditText.getText().toString(), wayPointCounter);
                    viaTextView.setText("");
                    wayPointCounter += 1;
                }

                if(!secondWayPointEditText.getText().toString().isEmpty())
                {
                    addressDialogClosed(MarkerType.Waypoint, secondWayPointEditText.getText().toString(), wayPointCounter);
                    viaTextView.setText("");
                    wayPointCounter += 1;
                }

                if(!thirdWayPointEditText.getText().toString().isEmpty())
                {
                    addressDialogClosed(MarkerType.Waypoint, thirdWayPointEditText.getText().toString(), wayPointCounter);
                    viaTextView.setText("");
                    wayPointCounter += 1;
                }

                if(!fourthWayPointEditText.getText().toString().isEmpty())
                {
                    addressDialogClosed(MarkerType.Waypoint, fourthWayPointEditText.getText().toString(), wayPointCounter);
                    viaTextView.setText("");
                    wayPointCounter += 1;
                }

                if(!fifthWayPointEditText.getText().toString().isEmpty())
                {
                    addressDialogClosed(MarkerType.Waypoint, fifthWayPointEditText.getText().toString(), wayPointCounter);
                    viaTextView.setText("");
                    wayPointCounter += 1;
                }

                if(!sixthWayPointEditText.getText().toString().isEmpty())
                {
                    addressDialogClosed(MarkerType.Waypoint, sixthWayPointEditText.getText().toString(), wayPointCounter);
                    viaTextView.setText("");
                }
                wayPointDialog.dismiss();
                drawDrivingDirectionsOnMap();
            }
        });

        wayPointDialog.show();
    }

    private void showAddressSelectionDialog(final MarkerType markerType, Marker marker)
    {
        final Dialog addressSelectorDialog = new Dialog(this);
        addressSelectorDialog.setCanceledOnTouchOutside(true);
        addressSelectorDialog.setContentView(R.layout.alert_dialog_offer_journey_address_selector);
        addressSelectorDialog.setTitle(markerType == MarkerType.Departure ? "Enter departure address" : "Enter destination address");
        final  EditText addressEditText = (EditText) addressSelectorDialog.findViewById(R.id.AlertDialogOfferJourneyAddressSelectorAddressEditText);
        addressEditText.setText(marker == null ? "" : marker.getTitle());
        Button okButton = (Button) addressSelectorDialog.findViewById(R.id.AlertDialogOfferJourneyAddressSelectorOkButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String addressText = addressEditText.getText().toString();
                if(addressText != null && !addressText.isEmpty())
                {
                    addressDialogClosed(markerType, addressText, 0);
                    addressSelectorDialog.dismiss();

                }
            }
        });

        addressSelectorDialog.show();
    }

    private void addressDialogClosed(MarkerType markerType, String address, double order)
    {
        new GeocoderTask(this, this, markerType, order).execute(new GeocoderParams(address, null));
    }

    private void buildJourneyAndProceed()
    {
        if(this.departureMarker == null || this.destinationMarker == null)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("You must specify departure and destination points.")
                    .setCancelable(false)
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();
            return;
        }

        final Journey journey = new Journey();
        journey.GeoAddresses = new ArrayList<GeoAddress>();
        journey.GeoAddresses.add(new GeoAddress(departureMarker.getPosition().latitude, departureMarker.getPosition().longitude, departureMarker.getTitle(), 0));

        for(WaypointHolder waypointHolder : this.wayPoints)
        {
            if(waypointHolder.googleMapMarker != null)
            {
                journey.GeoAddresses.add(waypointHolder.geoAddress);
            }
        }

        journey.GeoAddresses.add(new GeoAddress(destinationMarker.getPosition().latitude, destinationMarker.getPosition().longitude, destinationMarker.getTitle(), this.wayPoints.size()+1));
        Log.i("This journey has the following geoaddresses", "\n");
        for(GeoAddress geoAddress : journey.GeoAddresses)
        {
            Log.i("GeoAddress " + geoAddress.Order, " "+geoAddress.Latitude + " " + geoAddress.Longitude + " " + geoAddress.AddressLine);
        }

        this.alrightImDone(journey);
    }

    private void alrightImDone(final Journey journey)
    {
        googleMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(Bitmap bitmap) {
                int h = getApplicationContext().getResources().getDisplayMetrics().heightPixels / 3;
                int w = (int) (h * bitmap.getWidth() / ((double) bitmap.getHeight()));

                bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 50, bs);
                Bundle bundle = new Bundle();
                bundle.putString(IntentConstants.JOURNEY, gson.toJson(journey));
                Intent intent = new Intent(getApplicationContext(), OfferJourneyStepTwoActivity.class);
                intent.putExtras(bundle);
                intent.putExtra(IntentConstants.MINIMAP, bs.toByteArray());
                startActivity(intent);
            }
        });
    }

    @Override
    public void onGeoCoderFinished(MarkerOptions address, MarkerType markerType, double perimeter)
    {
        super.onGeoCoderFinished(address, markerType, perimeter);

        if(address != null)
        {
            addressEntered(markerType, address, perimeter);
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Could not retrieve current location. Please enter it manually.")
                    .setCancelable(false)
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void addressEntered(MarkerType markerType, MarkerOptions markerOptions, Double perimeter)
    {
        if(markerType == MarkerType.Departure)
        {
            showDeparturePoint(markerOptions, perimeter);
            this.departureTextView.setText(markerOptions.getTitle());
        }
        else if(markerType == MarkerType.Destination)
        {
            showDestinationPoint(markerOptions, perimeter);
            this.destinationTextView.setText(markerOptions.getTitle());
        }else
        {
            WaypointHolder waypointHolder = new WaypointHolder();
            waypointHolder.geoAddress = new GeoAddress(markerOptions.getPosition().latitude, markerOptions.getPosition().longitude, markerOptions.getTitle(), perimeter.intValue());
            this.wayPoints.add(waypointHolder);
            this.showWaypointOnMap(waypointHolder, markerOptions);
            this.viaTextView.setText(this.viaTextView.getText().toString() + markerOptions.getTitle() + ", ");
        }

        this.drawDrivingDirectionsOnMap();
    }

    private void drawDrivingDirectionsOnMap()
    {
        if(departureMarker != null && destinationMarker != null)
        {
            ArrayList<GeoAddress> geoAddresses = new ArrayList<GeoAddress>();
            geoAddresses.add(new GeoAddress(departureMarker.getPosition().latitude, departureMarker.getPosition().longitude, departureMarker.getTitle(), 0));
            geoAddresses.add(new GeoAddress(destinationMarker.getPosition().latitude, destinationMarker.getPosition().longitude, destinationMarker.getTitle(), 1));
            if(wayPoints.size() > 0)
            {
                for(WaypointHolder waypointHolder : this.wayPoints)
                {
                    if(waypointHolder.geoAddress != null)
                    {
                        geoAddresses.add(waypointHolder.geoAddress);
                    }
                }
            }
            drawDrivingDirectionsOnMap(googleMap, geoAddresses);
        }
    }
}
