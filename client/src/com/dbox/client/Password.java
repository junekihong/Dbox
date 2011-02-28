package com.dbox.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Login Activity
 */

public class Password extends Activity
{
	/**
	 * Login response codes.
	 */
	public static enum UpdatePassword { SUCCESS, FAILURE, ERROR };
	
	/**
	 * Login progress dialog
	 */
	private ProgressDialog mProgress;
	
	/**
	 * Login progress dialog
	 */
	private AlertDialog mErrorDialog;
	
	/**
	 * User Input: Server Host
	 */
	private String mHost;
	
	/**
	 * User Input: Server Port
	 */
	private int mPort;
	
	/**
	 * User Input: Username
	 */
	private String mUsername;
	
	/**
	 * User Input: Password 
	 */
	private String mPassword;
	
	private String mNewPassword;

	/** Task to register a new user, logging in when completed. */
	private class UpdatePasswordTask extends AsyncTask<String, Integer, UpdatePassword>
	{
		/**
		 * Executes task on a background thread.
		 */
		@Override
		protected UpdatePassword doInBackground(String... data)
		{
			return WebService.update_password(mHost,mPort,mUsername,mPassword,mNewPassword);
		}

		/**
		 * Called on the UI thread after doInBackground has finished.
		 */
		@Override
	    protected void onPostExecute(UpdatePassword result)
	    {
	    	hideProgressDialog();
	    	
	    	switch (result)
	    	{
	    		case SUCCESS:
	    			try
					{
	    				Toast.makeText(Password.this, "Password has been updated. Please login with your new credentials.", Toast.LENGTH_LONG).show();
	    				
	    				Intent i = new Intent(Password.this,Login.class);
	    				i.putExtra("port", mPort);
	    				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    				startActivity(i);
	    				finish();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
	    			
		    		break;
	    		case FAILURE:
	    			showErrorDialog("Password update failed.");
	    			break;
	    		case ERROR:
	    			showErrorDialog("Could not connect to the host on the specified port.");
	    			break;
	    	}
	    }
	}

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password);
        
        Bundle b = getIntent().getExtras();
        
        mHost = b.getString("host");
        mPort = b.getInt("port");
        mUsername = b.getString("username");
        mPassword = b.getString("password");
        
        Button login = (Button) findViewById(R.id.update_password);
        login.setOnClickListener
        (
        	new OnClickListener()
    		{
    			public void onClick(View v)
   				{
    				if (checkInput())
    				{
						try
						{
							showProgressDialog("Updating Password");
							new UpdatePasswordTask().execute("");
						}
						catch (Exception e)
						{
							showErrorDialog("An error occured parsing your login credentials. Ensure that they are valid and try again.");
						} 
   					}
    			}
    		}
        );
    }
    
    public boolean checkInput()
    {
    	boolean valid = true;
    	
		String password = ((EditText) findViewById(R.id.password_input)).getText().toString();
		String confirm = ((EditText) findViewById(R.id.confirm_input)).getText().toString();
		
		// check for input errors
		if (password.equals(""))
		{
			showErrorDialog("Cannot submit an empty password.");
			valid = false;
		}
		else if (!password.equals(confirm))
		{
			showErrorDialog("Passwords do not match!");
			valid = false;
		}
		else
		{
			mNewPassword = password;
		}
		
		return valid;
    }
    
    /**
     * Show the registration progress dialog.
     * @return void
     */
    public void showProgressDialog(String message)
    {
    	mProgress = new ProgressDialog(this);
    	mProgress.setCancelable(false);
    	mProgress.setMessage(message);
		mProgress.show();
    }
    
    public void hideProgressDialog()
    {
    	mProgress.dismiss();
    }
    
    /**
     * Show an error dialog.
     * @return void
     */
    public void showErrorDialog(String error)
    {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	
    	builder
    		.setTitle("Error")
    		.setMessage(error)
    		.setCancelable(false)
    		.setPositiveButton
    		(
    			"OK",
    			new DialogInterface.OnClickListener()
    			{
    	           public void onClick(DialogInterface dialog, int id)
    	           {
    	                // nada to do...
    	           }
    	       }
    		);
    	mErrorDialog = builder.create();
    	mErrorDialog.show();
    }
}