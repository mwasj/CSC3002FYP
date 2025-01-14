package com.example.myapplication.activities.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activities.base.BaseActivity;
import com.example.myapplication.constants.ServiceResponseCode;
import com.example.myapplication.constants.SessionConstants;
import com.example.myapplication.domain_objects.ServiceResponse;
import com.example.myapplication.domain_objects.User;
import com.example.myapplication.dtos.RegisterDTO;
import com.example.myapplication.factories.DialogFactory;
import com.example.myapplication.interfaces.WCFServiceCallback;
import com.example.myapplication.network_tasks.WcfPostServiceTask;
import com.example.myapplication.utilities.Pair;
import com.example.myapplication.utilities.Validators;
import com.google.gson.reflect.TypeToken;

import static java.util.Arrays.asList;

/**
 * Provides the user with all necessary functionality to register a new account with the system.
 * It also performs validation before calling the web service to ensure the required information is present and is in the correct format.
 **/
public class RegistrationActivity extends BaseActivity implements WCFServiceCallback<User, String>, View.OnClickListener, View.OnFocusChangeListener{

    private EditText userNameEditText;
    private EditText emailAddressEditText;
    private EditText passwordEditText;
    private EditText confirmedPasswordEditText;

    private Button registerButton;

    private ProgressBar progressBar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialise UI elements and setup all event handlers.
        progressBar = (ProgressBar) findViewById(R.id.RegistrationActivityProgressBar);

        userNameEditText = (EditText) findViewById(R.id.UserNameTextField);
        userNameEditText.setOnFocusChangeListener(this);

        emailAddressEditText = (EditText) findViewById(R.id.EmailTextField);
        emailAddressEditText.setOnFocusChangeListener(this);

        passwordEditText = (EditText) findViewById(R.id.RegistrationPasswordTextField);
        confirmedPasswordEditText = (EditText) findViewById(R.id.RegistrationConfirmPasswordTextField);

        registerButton = (Button) findViewById(R.id.RegisterNewUserButton);
        registerButton.setOnClickListener(this);

        findViewById(R.id.RegistrationActivityTermsTextView).setOnClickListener(this);
    }

    /**
     * Validate the information provided by the user and if successful,
     * call the web service to register a new account.
     **/
    public void Register() {

        //Perform validation before calling the web service.
        if(Validators.validatePasswords(passwordEditText, confirmedPasswordEditText) &&
                Validators.validateEmailAddress(emailAddressEditText) && Validators.validateUserName(userNameEditText))
        {
            // Validation successful, call the webservice to create a new account.
            progressBar.setVisibility(View.VISIBLE);
            registerButton.setEnabled(false);
            new WcfPostServiceTask<RegisterDTO>(this, getResources().getString(R.string.UserRegisterURL),
                    new RegisterDTO(passwordEditText.getText().toString(), confirmedPasswordEditText.getText().toString(),
                            new User(userNameEditText.getText().toString(),emailAddressEditText.getText().toString(), getAppManager().getRegistrationId())),
                    new TypeToken<ServiceResponse<User>>() {}.getType(),
                    asList(new Pair(SessionConstants.REMEMBER_ME, ""+false),
                           new Pair(SessionConstants.DEVICE_ID, getAppManager().getUniqueDeviceId()),
                           new Pair(SessionConstants.UUID, getAppManager().getUUID())), this).execute();

        }
    }

    /**
     * Called when a reply from the web service is received.
     **/
    @Override
    public void onServiceCallCompleted(ServiceResponse<User> serviceResponse, String session) {

        progressBar.setVisibility(View.GONE);

        if (serviceResponse.ServiceResponseCode == ServiceResponseCode.SUCCESS)
        {
            getAppManager().setSessionId(session);
            getAppManager().setUser(serviceResponse.Result);
            Toast.makeText(this, "Registered successfully!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
            finish();
        }
        else
        {
            registerButton.setEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.register, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.login_menu_option:
                openLoginActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private void openLoginActivity()
    {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.RegisterNewUserButton:
                Register();
                break;
            case R.id.RegistrationActivityTermsTextView:
                DialogFactory.getTermsAndConditionsDialog(this);
                break;
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        switch(view.getId())
        {
            case R.id.UserNameTextField:
                if(!hasFocus && !emailAddressEditText.getText().toString().isEmpty())
                    Validators.validateEmailAddress(emailAddressEditText);
                break;
            case R.id.EmailTextField:
              if(!hasFocus && !userNameEditText.getText().toString().isEmpty())
                    Validators.validateUserName(userNameEditText);
                break;
        }
    }
}