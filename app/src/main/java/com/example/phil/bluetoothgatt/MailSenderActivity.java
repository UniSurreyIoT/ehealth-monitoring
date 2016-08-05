/**
 * Created by Phil on 16/01/2016.
 * Simple activity to make use of built in email program
 */

package com.example.phil.bluetoothgatt;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class MailSenderActivity extends Activity {



    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        //Get filename to email
        Bundle bundle = getIntent().getExtras();
        String filename = bundle.getString("EmailFile");

        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        String titleString = getApplicationContext().getString(R.string.email_title);

        emailIntent.setType("text/plain");

        //Check if should be using default email address
        if(EmailPreferences.getUseDefaultEmailAddress(getApplicationContext())){
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{EmailPreferences.getDefaultEmailAddress(getApplicationContext())});
        }

        //Add title and file to email
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, titleString);
        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + filename));
        startActivity(emailIntent);
        finish();
    }

}
