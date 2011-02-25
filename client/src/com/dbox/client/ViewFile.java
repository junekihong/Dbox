package com.dbox.client;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class ViewFile extends Activity
{   
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
	
	private class DeleteFileTask extends AsyncTask<String, Integer, Integer>
	{
		/**
		 * Executes task on a background thread.
		 */
		@Override
		protected Integer doInBackground(String... data)
		{
			WebService.delete(mUrl,mPort,mUsername,mPassword);
			return 1;
		}

		/**
		 * Called on the UI thread after doInBackground has finished.
		 */
		@Override
	    protected void onPostExecute(Integer result)
	    {
			onDeleteResponse();
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
	        mUsername = bundle.getString("username");
	        mPassword = bundle.getString("password");
	        mUrl = bundle.getString("path");
	        mPort = bundle.getInt("port");
	        
	        get();
        }
        catch (Exception e)
        {
        	e.printStackTrace();
        }
    }
    
    public void get()
    {
        showProgressDialog("Loading directory from server.");
        new DownloadFileTask().execute("");
    }
    
    public void delete()
    {
    	showProgressDialog("Deleting file.");
    	new DeleteFileTask().execute("");
    }
    
    public void onDeleteResponse()
    {
    	DirList.refreshOnResume = true;
    	finish();
    }
    
    public void onServerResponse()
    {
    	setContentView(R.layout.viewfile);
    	
    	Resource file = ls[0];
    	
    	if (file != null)
    	{
    		TextView title = (TextView) findViewById(R.id.title);
    		TextView name = (TextView) findViewById(R.id.name);
	    	TextView type = (TextView) findViewById(R.id.type);
	    	TextView size = (TextView) findViewById(R.id.filesize);
	    	TextView date = (TextView) findViewById(R.id.date);
	    	TextView content = (TextView) findViewById(R.id.content);
	    	
	    	title.setText("Viewing File: " + file.name());
	    	name.setText(file.name());
	    	type.setText(file.type());
	    	size.setText( ((double)file.size())/1000 + "kb");
	    	date.setText(file.dateFormatted());
	    	
	    	if (file.type().equals("text/plain"))  
	    		content.setText(file.content());
    	}
    	
    	hideProgressDialog();
    }
    
    /**
     * Show the login progress dialog.
     * @return void
     */
    public void showProgressDialog(String message)
    {
    	mProgressDialog = new ProgressDialog(this);
    	mProgressDialog.setCancelable(false);
    	mProgressDialog.setMessage(message);
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
    
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle item selection
        switch (item.getItemId())
        {
    	case R.id.refresh:
    		get();
    		return true;
    	case R.id.delete:
    		delete();
    		return true;
        }
        return false;
    }
}