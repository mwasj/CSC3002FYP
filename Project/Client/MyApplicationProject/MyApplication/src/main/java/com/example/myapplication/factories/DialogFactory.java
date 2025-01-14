package com.example.myapplication.factories;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.myapplication.R;
import com.example.myapplication.activities.activities.SearchResultDetailsActivity;
import com.example.myapplication.adapters.SearchResultsAdapter;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.domain_objects.Journey;
import com.example.myapplication.interfaces.Interfaces;
import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * Set of static methods to create and display alert dialogs.
 */
public class DialogFactory {

    /**
     * Displays the current activities' help dialog.
     * @param context - Context of the currently visible activity.
     * @param title - Title to be displayed on top of the alert dialog.
     * @param message - Message containing the help content.
     */
    public static void getHelpDialog(final Context context, String title, String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setCancelable(false)
                .setTitle(title)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Asks the user to input a new name for their journey template.
     * @param context - Context of the currently visible activity.
     * @param templateNameListener - Callback method to be invoked after the name has been entered.
     */
    public static void getJourneyTemplateNameDialog(final Context context, final Interfaces.TemplateNameListener templateNameListener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enter template name");

        // Set up the input
        final EditText input = new EditText(context);
        builder.setView(input);

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });


        final AlertDialog dialog = builder.create();
        dialog.show();

        Button theButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        theButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                input.setError(input.getText().toString().isEmpty() ? "Please enter template name." : null);

                if(!input.getText().toString().isEmpty())
                {
                    templateNameListener.NameEntered(input.getText().toString());
                    dialog.dismiss();
                }
            }
        });
    }

    /**
     * Displays a simple yes/no dialog.
     * @param context - Context of the currently visible activity.
     * @param title - Title to be displayed on top of the alert dialog.
     * @param message  - Message containing the help content.
     * @param listener - Callback method to be invoked after the positive (yes) button has been clicked.
     */
    public static void getYesNoDialog(Context context, String title, String message, final Interfaces.YesNoDialogPositiveButtonListener listener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title).setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.positiveButtonClicked();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Displays a pre-configured dialog containing search results retrieved from the web service.
     * @param context - Context of the currently visible activity.
     * @param searchResults - ArrayList of Journey objects containing the search results received from the server.
     */
    public static void getJourneySearchResultsDialog(final Context context, final ArrayList<Journey> searchResults)
    {
        Dialog searchResultsDialog = new Dialog(context, R.style.Theme_CustomDialog);
        searchResultsDialog.setCanceledOnTouchOutside(true);
        searchResultsDialog.setContentView(R.layout.dialog_search_results);
        ListView resultsListView = (ListView) searchResultsDialog.findViewById(R.id.SearchResultsDialogResultsListView);
        SearchResultsAdapter adapter = new SearchResultsAdapter(context, R.layout.listview_row_search_result, searchResults);
        resultsListView.setAdapter(adapter);

        resultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle bundle = new Bundle();
                bundle.putString(IntentConstants.JOURNEY, new Gson().toJson(searchResults.get(i)));
                context.startActivity(new Intent(context, SearchResultDetailsActivity.class).putExtras(bundle));
            }
        });

        searchResultsDialog.show();
    }

    /**
     * Displays the terms and conditions dialog at the time of registration.
     * @param context - Context of the currently visible activity.
     */
    public static void getTermsAndConditionsDialog(final Context context)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Terms & Conditions").setMessage("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. " +
                "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. " +
                "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. " +
                "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.")
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
