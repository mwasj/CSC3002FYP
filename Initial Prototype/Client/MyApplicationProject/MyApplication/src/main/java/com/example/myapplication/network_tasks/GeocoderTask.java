package com.example.myapplication.network_tasks;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import com.example.myapplication.enums.MarkerType;
import com.example.myapplication.google_maps_utilities.BackupGeocoder;
import com.example.myapplication.google_maps_utilities.GeocoderParams;
import com.example.myapplication.interfaces.GeoCoderFinishedCallBack;
import com.example.myapplication.utilities.Utilities;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Michal on 04/02/14.
 */
public class GeocoderTask extends AsyncTask<GeocoderParams, Void, MarkerOptions> {

    private final String TAG = "GeocoderTask";
    private Context context;
    private GeoCoderFinishedCallBack listener;
    private MarkerOptions markerOptions;
    private MarkerType markerType;
    private double perimeter;

    public GeocoderTask(Context context, GeoCoderFinishedCallBack geoCoderFinishedCallBack, MarkerType markerType, double perimeter)
    {
        super();
        this.listener = geoCoderFinishedCallBack;
        this.context = context;
        this.markerType = markerType;
        this.perimeter = perimeter;
    }
    @Override
    protected MarkerOptions doInBackground(GeocoderParams... geocoderParams) {
        Geocoder geocoder = new Geocoder(this.context, Locale.getDefault());

        // Create a list to contain the result address
        List<Address> addresses = null;
        try {
            if(Geocoder.isPresent())
            {
                addresses = geocoderParams[0].getLocation() != null ? geocoder.getFromLocation(geocoderParams[0].getLocation().getLatitude(), geocoderParams[0].getLocation().getLongitude(), 1)
                        : geocoder.getFromLocationName(geocoderParams[0].getAddress(), 1);
            }
            else
            {
                Log.e(TAG, "Geocoder not present, attempting to retrieve address using Google Maps API.");

                if(geocoderParams[0].getAddress() != null)
                {
                    BackupGeocoder backupGeocoder = new BackupGeocoder();
                    JSONObject jsonAddress =  backupGeocoder.getJson(geocoderParams[0].getAddress());

                    if(jsonAddress != null)
                    {
                        Address address = backupGeocoder.getAddress(jsonAddress);

                        if(address != null)
                        {
                            addresses = new ArrayList<Address>();
                            addresses.add(address);
                        }
                    }
                }
            }
        } catch (IOException e1) {
            Log.e("LocationSampleActivity",
                    "IO Exception in getFromLocation()");
            e1.printStackTrace();
            return null;
        } catch (IllegalArgumentException e2) {
            e2.printStackTrace();
            return null;
        }

        // If the reverse geocode returned an address
        if (addresses != null && addresses.size() > 0)
        {
            // Get the first address
            Address address = addresses.get(0);

            String addressText = String.format(
                    "%s, %s, %s",
                    // If there's a street address, add it
                    address.getMaxAddressLineIndex() > 0 ?
                            address.getAddressLine(0) : "",
                    // Locality is usually a city
                    address.getLocality(),
                    // The country of the address
                    address.getCountryName());

            addressText = addressText.replace(",null", "").replace("null", "").replace(" , ", "");

            // Return the text
            this.markerOptions =  new MarkerOptions().position(new LatLng(address.getLatitude(), address.getLongitude()))
                    .title(addressText).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(!Utilities.isNetworkAvailable(this.context))
        {
            cancel(true);
            this.displayErrorDialog("Network unavailable, please check your internet connection and try again.");
        }
    }

    @Override
    protected void onPostExecute(MarkerOptions s) {
        super.onPostExecute(s);
        this.listener.onGeoCoderFinished(this.markerOptions, this.markerType, this.perimeter);
    }

    private void displayErrorDialog(String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        builder.setMessage(message)
                .setCancelable(false)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        listener.onGeoCoderFinished(null, null, null);
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }
}
