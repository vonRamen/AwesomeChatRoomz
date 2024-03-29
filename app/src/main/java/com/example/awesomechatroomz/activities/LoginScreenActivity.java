package com.example.awesomechatroomz.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.awesomechatroomz.R;
import com.example.awesomechatroomz.components.LoginComponent;
import com.example.awesomechatroomz.domain.FacebookLoginMethod;
import com.example.awesomechatroomz.domain.GoogleLoginMethod;
import com.example.awesomechatroomz.repository.LoginManager;
import com.example.awesomechatroomz.interfaces.AsyncTaskCallback;
import com.example.awesomechatroomz.models.User;
import com.example.awesomechatroomz.room.SavedInstancesDatabase;
import com.facebook.CallbackManager;
import com.facebook.FacebookException;
import com.google.android.gms.common.api.ApiException;

import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;


public class LoginScreenActivity extends AppCompatActivity implements HasAndroidInjector {
    private static final String TAG = "LoginScreenActivity";

    LoginComponent comp;
    @Inject
    DispatchingAndroidInjector<Object> activityDispatchingAndroidInjector;
    @Inject
    FacebookLoginMethod facebookLogin;
    @Inject
    GoogleLoginMethod googleLoginMethod;

    @Inject
    LoginManager loginManager;


    @Inject
    SavedInstancesDatabase d;

    private List<CallbackManager> callbackManagers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        //this.callbackManagers = new List<CallbackManager>();
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_login_screen);

        Log.i(TAG, "onCreate: in LoginScreenActivity. Change so that login works again.");
        facebookLogin.prepare(this);
        googleLoginMethod.prepare(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        User user = null;

        //Look for Facebook results.
        try {
            user = facebookLogin.onActivityResult(requestCode, resultCode, data);
        } catch(FacebookException e) {
            displayConnectionError("Facebook Connect", "Could not connect to Facebook. Are you connected to the internet?");
            Log.d(TAG, "onActivityResult: FacebookException happened. Check if you're connected to the internet.\n"+e);
        }

        //If the user is not logged in using Facebook, Google is tried.
        if(user==null) {
            try {
                user = googleLoginMethod.onActivityResult(requestCode, resultCode, data);
            } catch (ApiException e) {
                displayConnectionError("Google Connect", "Could not connect to Google. Are you connected to the internet?");
                Log.d(TAG, "onActivityResult: FacebookException happened. Check if you're connected to the internet.\n"+e);
            }
        }

        //If user is found, he is logged in through the system.
        if(user!=null) {
            login(user);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void login(User user) {

        loginManager.updateUserInformation(user, new AsyncTaskCallback() {
            @Override
            public void OnPreExecute() {
                findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                super.OnPreExecute();
            }

            @Override
            public void onPostExecute() {
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                Intent chatMenu = new Intent(LoginScreenActivity.this, ChatMenuActivity.class);
                startActivity(chatMenu);
            }
        });
    }

    private void displayConnectionError(String title, String message) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).show();
    }

    public AndroidInjector androidInjector() {
        return activityDispatchingAndroidInjector;
    }
}
