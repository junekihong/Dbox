package com.dbox.client;

import java.net.URL;

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

	/** Task to register a new user, logging in when completed. */
	private class RegisterTask extends AsyncTask<String, Integer, LoggedIn>
	{
		/**
		 * Executes task on a background thread.
		 */
		@Override
		protected LoggedIn doInBackground(String... data)
		{
			return WebService.register(mHost,mPort,mUsername,mPassword);
		}

		/**
		 * Called on the UI thread after doInBackground has finished.
		 */
		@Override
	    protected void onPostExecute(LoggedIn result)
	    {
	    	hideProgressDialog();
	    	
	    	switch (result)
	    	{
	    		case SUCCESS:
	    			try
					{
						URL url = new URL(mHost);
						String path = "http://" + url.getHost() + ":" + mPort + "/" + mUsername + "/";
						
		    			Bundle b = new Bundle();
		    			b.putString("host", mHost);
		    			b.putInt("port", mPort);
		    			b.putString("username", mUsername);
		    			b.putString("password", mPassword);
		    			b.putString("path", path);
		    			
		    			Intent i = new Intent(Login.this,DirList.class);
		    			i.putExtras(b);
			    		startActivity(i);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
	    			
		    		break;
	    		case FAILURE:
	    			showErrorDialog("Registration Failed: The user already exists");
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
        
        Bundle b = getIntent().getExtras();
        
        if (b != null)
        {
        	String url = b.getString("path");
        	mPort = b.getInt("port");
        	
        	if (url != null)
        	{
        		String h = "";
        		
        		try { h = new URL(url).getHost(); } catch (Exception e) {}
        		
	        	EditText host = (EditText) findViewById(R.id.host_input);
	        	host.setText(h);
	        	
	        	EditText port = (EditText) findViewById(R.id.port_input);
	        	port.setText("" + mPort);
        	}
        }
        
        Button login = (Button) findViewById(R.id.login_button);
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
							URL url = new URL(mHost);
							String path = "";
							Intent i;
							
							path = "http://" + url.getHost() + ":" + mPort + "/" + mUsername + "/";
							i = new Intent(Login.this,DirList.class);
							
			    			Bundle b = new Bundle();
			    			b.putString("host", mHost);
			    			b.putInt("port", mPort);
			    			b.putString("username", mUsername);
			    			b.putString("password", mPassword);
			    			b.putString("path", path);
			    			
			    			i.putExtras(b);
				    		startActivity(i);
						}
						catch (Exception e)
						{
							showErrorDialog("An error occured parsing your login credentials. Ensure that they are valid and try again.");
						} 
   					}
    			}
    		}
        );
        
        //Registration: Uses the same inputs as Login, but first sends a registration request to the
        // server to create a new user. If the registration is successful, proceed to log in.
        Button register = (Button) findViewById(R.id.register_button);
        register.setOnClickListener
        (
        	new OnClickListener()
    		{
    			public void onClick(View v)
   				{
    				if (checkInput())
   					{  						
   						showProgressDialog("Attempting register with server.");
   						new RegisterTask().execute("");
   					}
    			}
    		}
        );
    }
    
    public boolean checkInput()
    {
    	boolean valid = true;
    	
		String host = ((EditText) findViewById(R.id.host_input)).getText().toString();
		String port = ((EditText) findViewById(R.id.port_input)).getText().toString();
		String username = ((EditText) findViewById(R.id.username_input)).getText().toString();
		String password = ((EditText) findViewById(R.id.password_input)).getText().toString();
		
		// check for input errors
		if (host.equals(""))
		{
			showErrorDialog("You entered an invalid host.");
			valid = false;
		}
		else if (port.equals(""))
		{
			showErrorDialog("You entered an invalid port.");
			valid = false;
		}
		else if (username.equals(""))
		{
			showErrorDialog("You entered an invalid username.");
			valid = false;
		}
		else if (password.equals(""))
		{
			showErrorDialog("You entered an invalid password.");
			valid = false;
		}
		else
		{
			if (!host.contains("http://") && !host.contains("https://"))
				host = "http://" + host;
			
			mHost = host;
			mPort = Integer.parseInt(port);
			mUsername = username;
			mPassword = password;
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