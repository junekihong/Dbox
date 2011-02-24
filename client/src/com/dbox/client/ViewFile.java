package com.dbox.client;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

public class ViewFile extends Activity
{   
    private String mHost;
    private String mUrl;
    private String mUsername;
    private String mPassword;
    private int mPort;
    private Resource[] ls;
    private ProgressDialog mProgressDialog;
    
	private class DownloadFileTask extends AsyncTask<String, Integer, Integer>
	{
		/**
		 * Executes task on a background thread.
		 */
		@Override
		protected Integer doInBackground(String... data)
		{
			ls = WebService.get(mUrl,mPort,mUsername,mPassword);
			return 1;
		}

		/**
		 * Called on the UI thread after doInBackground has finished.
		 */
		@Override
	    protected void onPostExecute(Integer result)
	    {
			onServerResponse();
	    }
	}

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.dirlist);
        
        Bundle bundle = getIntent().getExtras();
        
        try
        {
	        mHost = bundle.getString("host");
	        mUsername = bundle.getString("username");
	        mPassword = bundle.getString("password");
	        mUrl = bundle.getString("path");
	        mPort = bundle.getInt("port");
	        
	        showProgressDialog();
	        
	        new DownloadFileTask().execute("");
        }
        catch (Exception e)
        {
        	e.printStackTrace();
        }
    }
    
    public void onServerResponse()
    {
    	setContentView(R.layout.viewfile);
    	
    	Resource file = ls[0];
    	
    	TextView name = (TextView) findViewById(R.id.name);
    	TextView type = (TextView) findViewById(R.id.type);
    	TextView size = (TextView) findViewById(R.id.size);
    	TextView date = (TextView) findViewById(R.id.date);
    	TextView content = (TextView) findViewById(R.id.content);
    	
    	name.setText(file.name());
    	type.setText(file.type());
    	size.setText(file.size());
    	date.setText(file.dateFormatted());
    	if (file.type().equals("text/plain"))  
    	content.setText(file.content());
    	
    	hideProgressDialog();
    }
    
    /**
     * Show the login progress dialog.
     * @return void
     */
    public void showProgressDialog()
    {
    	mProgressDialog = new ProgressDialog(this);
    	mProgressDialog.setCancelable(false);
    	mProgressDialog.setMessage("Loading directory from server.");
		mProgressDialog.show();
    }
    
    /**
     * Hide the login progress dialog.
     * @return void
     */
    public void hideProgressDialog()
    {
    	mProgressDialog.dismiss();
    	mProgressDialog = null;
    }
}