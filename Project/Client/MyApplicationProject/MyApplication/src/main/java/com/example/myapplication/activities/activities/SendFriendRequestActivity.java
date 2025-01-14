package com.example.myapplication.activities.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.domain_objects.FriendRequest;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.interfaces.WCFImageRetrieved;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPictureServiceTask;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.google.gson.reflect.TypeToken;

/**
 * Provides user with all necessary functionality to send a new friend request to another user of the system.
 */
public class SendFriendRequestActivity extends BaseActivity implements WCFServiceCallback<Void, Void>, View.OnClickListener {

    private EditText messageEditText;

    private Button sendRequestButton;

    private ImageView profileIconImageView;

    private User targetUser;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_friend_request);

        //Initialise local variables.
        targetUser = getGson().fromJson(getIntent().getExtras().getString(IntentConstants.USER), new TypeToken<User>() {}.getType());

        //Initialise UI elements.
        messageEditText = (EditText) findViewById(R.id.SendFriendRequestActivityMessageEditText);
        sendRequestButton = (Button) findViewById(R.id.SendFriendRequestActivitySendButton);
        sendRequestButton.setOnClickListener(this);
        TextView headerTextView = (TextView) findViewById(R.id.SendFriendRequestActivityHeaderTextView);
        headerTextView.setText(headerTextView.getText().toString() + " " + targetUser.getFirstName() + " " + targetUser.getLastName() + " (" + targetUser.getUserName() + ")");
        profileIconImageView = (ImageView) findViewById(R.id.AlertDialogSendFriendRequestImageView);
        progressBar = (ProgressBar) findViewById(R.id.SendFriendRequestProgressBar);

        retrieveProfilePicture();
    }

    /**
     * Calls the web service with the required FriendRequest object.
     */
    private void sendFriendRequest()
    {
        sendRequestButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        sendRequestButton.setEnabled(false);
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setMessage(messageEditText.getText().toString());
        friendRequest.setToUser(targetUser);
        friendRequest.setFromUser(getAppManager().getUser());

        new WcfPostServiceTask<FriendRequest>(this,
                getResources().getString(R.string.SendFriendRequestURL), friendRequest,
                new TypeToken<ServiceResponse<Void>>() {}.getType(), getAppManager().getAuthorisationHeaders(), this).execute();
    }

    /**
     * Called after retrieving reply from the web service.
     *
     * @param serviceResponse - service response from the web service.
     * @param parameter
     */
    @Override
    public void onServiceCallCompleted(ServiceResponse<Void> serviceResponse, Void parameter)     {
        progressBar.setVisibility(View.GONE);
        if(serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            Toast.makeText(this, "Your friend request was sent successfully.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * Retrieves profile picture for the user whom the friend request is being sent to.
     */
    private void retrieveProfilePicture()
    {
        new WcfPictureServiceTask(getAppManager().getBitmapLruCache(), getResources().getString(R.string.GetProfilePictureURL),
                targetUser.getUserId(), getAppManager().getAuthorisationHeaders(), new WCFImageRetrieved() {
            @Override
            public void onImageRetrieved(Bitmap bitmap) {
                if(bitmap != null)
                {
                    profileIconImageView.setImageBitmap(bitmap);
                }
            }
        }).execute();
    }


    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.SendFriendRequestActivitySendButton:
                sendFriendRequest();
                break;
        }
    }
}
