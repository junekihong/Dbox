package com.dbox.client;

import android.app.Activity;
import android.app.ProgressDialog;
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
	    	
	    	if (result == LoggedIn.SUCCESS)
	    	{
	    		Intent i = new Intent(Login.this,DirList.class);
	    		startActivity(i);
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
   					String host = ((EditText) findViewById(R.id.host_input)).getText().toString();
   					String port = ((EditText) findViewById(R.id.port_input)).getText().toString();
   					String username = ((EditText) findViewById(R.id.username_input)).getText().toString();
   					String password = ((EditText) findViewById(R.id.password_input)).getText().toString();
    				
   					showLoginDialog();
   					
    				new LoginTask().execute(host,port,username,password);
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
}