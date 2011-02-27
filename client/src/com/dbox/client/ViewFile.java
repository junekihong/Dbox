package com.dbox.client;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ViewFile extends Activity
{   
	private Uri mLocalUri;
    private String mUrl;
    private String mUsername;
    private String mPassword;
    private int mPort;
    private Resource[] ls;
    private ProgressDialog mProgressDialog;
    private String mLocalPath;
    
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
	        
	        mUrl.replace(" ", "%20");
	        
	        get();
        }
        catch (Exception e)
        {
        	e.printStackTrace();
        }
    }
    
    public void get()
    {
        showProgressDialog("Loading file from server.");
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
	    	
	    	title.setText("Viewing File: " + file.name());
	    	name.setText(file.name());
	    	type.setText(file.type());
	    	size.setText( ((double)file.size())/1000 + "kb");
	    	date.setText(file.dateFormatted());
	    	
	    	try
	    	{
	    		mLocalPath = new URL(mUrl).getPath();
	    	}
	    	catch (Exception e) { }
	    	
	    	//System.out.println(file.url());
	    	System.out.println(mLocalPath);
	    	
	    	if (file.type().equals("text/plain"))
	    		writeFileToDisk(mLocalPath,file.content(),false);
	    	else
	    		writeFileToDisk(mLocalPath,file.content(),true);
	    	
	    	Button button = (Button) findViewById(R.id.viewButton);
	        button.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v) {
	                    Intent intent = new Intent(Intent.ACTION_VIEW);
	                    intent.setDataAndType(mLocalUri, ls[0].type());
	                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

	                    try {
	                        startActivity(intent);
	                    } 
	                    catch (ActivityNotFoundException e) {
	                        Toast.makeText(ViewFile.this, 
	                            "No Application Available to View PDF", 
	                            Toast.LENGTH_SHORT).show();
	                    }
	            }
	        });
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
    
    /**
     * Called when the menu button is pressed
     */
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        
        return true;
    }
    
    /**
     * Called when a menu item is selected
     */
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
    
    public boolean writeFileToDisk(String path, String data, boolean encoded)
    {
    	try
    	{
    		byte[] decoded;
    		
    		if (encoded)
    		{
    			decoded = Base64.decode(data, Base64.DEFAULT);	
    		}
    		else
    		{
    			decoded = data.getBytes();
    		}
    		
    		File root = Environment.getExternalStorageDirectory();

    		if (root.canWrite())
    		{
    			File f = new File(root, "downloads/" + path);
    			File x = new File(f.getParent());
    			x.mkdirs();
    			mLocalUri = Uri.fromFile(f);
    			FileOutputStream writer = new FileOutputStream(f);
    			writer.write(decoded);
    			
    			writer.close();
    			return true;
    		}
    	}
    	catch( Exception e )
    	{
    		e.printStackTrace();
    	}

    	return false;
    }
}