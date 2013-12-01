package com.example.myapplication.Experimental;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.DomainObjects.CarShare;
import com.example.myapplication.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Michal on 29/11/13.
 */
public class MyCarSharesAdapter extends ArrayAdapter<CarShare> {

    private Context context;
    private int layoutResourceId;
    private int userId;
    private CarShare CarShares[] = null;

    public MyCarSharesAdapter(int user, Context context, int resource, CarShare[] carShares) {
        super(context, resource, carShares);
        this.layoutResourceId = resource;
        this.context = context;
        this.CarShares = carShares;
        userId = user;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        CarShareHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new CarShareHolder();
            holder.imgIcon = (ImageView) row.findViewById(R.id.imgIcon);
            holder.FromTo = (TextView) row.findViewById(R.id.CarShareFromToTextView);
            holder.DepartureDate = (TextView)row.findViewById(R.id.CarShareDepartureDateTextView);
            row.setTag(holder);
        }
        else
        {
            holder = (CarShareHolder)row.getTag();
        }

        CarShare carShare = CarShares[position];


        holder.FromTo.setText(carShare.DepartureCity + " -> " + carShare.DestinationCity);
        Date date = WCFDateTimeParser.parseWCFDateTimeString(carShare.DateOfDeparture);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(WCFDateTimeParser.parseWCFDateTimeString(carShare.DateOfDeparture));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String formatted = format.format(calendar.getTime());
        holder.DepartureDate.setText(formatted);
        holder.imgIcon.setImageResource(R.drawable.taxi);
        if(carShare.UserId == userId)
        {
            holder.imgIcon.setImageResource(R.drawable.steering_wheel);
        }

        return row;
    }

    static class CarShareHolder
    {
        ImageView imgIcon;
        TextView FromTo;
        TextView DepartureDate;
    }
}
