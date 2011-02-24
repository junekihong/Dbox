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

/**
 * Login Activity
 */

public class Login extends Activity
{
	/**
	 * Login response codes.
	 */
	public static enum LoggedIn { SUCCESS, FAILURE, ERROR };
	
	/**
	 * Login progress dialog
	 */
	private ProgressDialog mLoginProgress;
	
	/**
	 * Login progress dialog
	 */
	private AlertDialog mErrorDialog;
	
	/**
	 * Asynchronous Login Task
	 */
	private class LoginTask extends AsyncTask<String, Integer, LoggedIn>
	{
		/**
		 * Executes task on a background thread.
		 */
		@Override
		protected LoggedIn doInBackground(String... data)
		{
			return WebService.login(data[0], Integer.parseInt(data[1]), data[2], data[3]);
		}

		/**
		 * Called on the UI thread after doInBackground has finished.
		 */
		@Override
	    protected void onPostExecute(LoggedIn result)
	    {
	    	hideLoginDialog();
	    	
	    	switch (result)
	    	{
	    		case SUCCESS:
	    			Intent i = new Intent(Login.this,DirList.class);
		    		startActivity(i);
		    		break;
	    		case FAILURE:
	    			showErrorDialog("Invalid Login Credentials");
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
        setContentView(R.layout.login);
        
        Button login = (Button) findViewById(R.id.login_button);
        login.setOnClickListener
        (
        	new OnClickListener()
    		{
    			public void onClick(View v)
   				{
    				// get the user input
   					String host = ((EditText) findViewById(R.id.host_input)).getText().toString();
   					String port = ((EditText) findViewById(R.id.port_input)).getText().toString();
   					String username = ((EditText) findViewById(R.id.username_input)).getText().toString();
   					String password = ((EditText) findViewById(R.id.password_input)).getText().toString();
   					
   					// check for input errors
   					if (host.equals(""))
   					{
   						showErrorDialog("You entered an invalid host.");
   					}
   					else if (port.equals(""))
   					{
   						showErrorDialog("You entered an invalid port.");
   					}
   					else if (username.equals(""))
   					{
   						showErrorDialog("You entered an invalid username.");
   					}
   					else if (password.equals(""))
   					{
   						showErrorDialog("You entered an invalid password.");
   					}
   					else
   					{
   						showLoginDialog();
   						new LoginTask().execute(host,port,username,password);
   					}
    			}
    		}
        );
    }
    
    /**
     * Show the login progress dialog.
     * @return void
     */
    public void showLoginDialog()
    {
    	mLoginProgress = new ProgressDialog(this);
    	mLoginProgress.setCancelable(false);
    	mLoginProgress.setMessage("Attempting logon to server.");
		mLoginProgress.show();
    }
    
    /**
     * Hide the login progress dialog.
     * @return void
     */
    public void hideLoginDialog()
    {
    	mLoginProgress.dismiss();
    	mLoginProgress = null;
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
    
    /**
     * Hide the error dialog.
     * @return void
     */
    public void hideErrorDialog()
    {
    	mErrorDialog.dismiss();
    	mErrorDialog = null;
    }
}