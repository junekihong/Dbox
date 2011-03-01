package com.dbox.client;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Uploading Activity
 */
public class Upload extends Activity
{
	public static boolean killMe = false;
	
	private ListView listView;
	private DirListAdapter adapter;
    private String mUrl;
    private String mPath;
    private String mUsername;
    private String mPassword;
    private int mPort;
    private Resource thing;
    private Resource[] resources;
    private ProgressDialog mProgressDialog;
    private boolean isRoot=false;
    
	/**
	 * Asynchronous Upload Task
	 */
	private class UploadFileTask extends AsyncTask<String, Integer, Boolean>
	{
		/**
		 * Executes task on a background thread.
		 */
		@Override
		protected Boolean doInBackground(String... data)
		{
			String mBody = XmlEngine.resourceToXml(thing);			
			try {
				return WebService.put(mUrl,mPort,mUsername,mPassword,mBody);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return false;
		}
	
		/**
		 * Called on the UI thread after doInBackground has finished.
		 */
		@Override
	    protected void onPostExecute(Boolean result)
	    {
	    	onUploadResponse();
	    }
	}
	
	/**
	 * Asynchronous Upload Task
	 */
	private class LsTask extends AsyncTask<String, Integer, Boolean>
	{
		/**
		 * Executes task on a background thread.
		 */
		@Override
		protected Boolean doInBackground(String... data)
		{
			File Uploads = new File(mPath);
			
			if (!Uploads.exists())
				Uploads.mkdir();
			
			File[] list= Uploads.listFiles();
			
			
			
			if (!isRoot)
			{
				resources= new Resource[list.length+1];
				File parent = new File(Uploads.getParent());
				resources[list.length] = new Resource("..",Uploads.getParent(),"",new Date(parent.lastModified()),0,true);
			}
			else
			{
				resources= new Resource[list.length];
			}
			
			for(int i = 0; i < list.length; i++)
			{
				try 
				{
					resources[i] = new Resource(list[i], new URL(mUrl).getPath().substring(1));
				} 
				catch (MalformedURLException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return true;
		}
	
		/**
		 * Called on the UI thread after doInBackground has finished.
		 */
		@Override
	    protected void onPostExecute(Boolean result)
	    {
	    	buildList();
	    }
	}
	
	/** 
	 * Called when the Activity is first created
	 * 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upload);
		
		Bundle bundle = getIntent().getExtras();
		
		try
		{
			mUrl = bundle.getString("uploadPath");
			mPath = bundle.getString("path");
			mPort = bundle.getInt("port");
			mUsername = bundle.getString("username");
	        mPassword = bundle.getString("password");
	        isRoot = bundle.getBoolean("isRoot");
	        
	        TextView t = (TextView) findViewById(R.id.path);
	        t.setText(mPath);
			
	        showProgressDialog("Reading from " + mPath);
	        new LsTask().execute("");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void buildList()
	{
		Arrays.sort(resources);
		
		adapter = new DirListAdapter(Upload.this,resources);
		listView=(ListView)findViewById(R.id.list);
		listView.setAdapter(adapter);
		
		registerForContextMenu(listView);
		
		listView.setOnItemClickListener
		(
			new OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> a, View v, int position, long id)
				{
					thing = resources[position];
					
					if (thing.isDirectory())
					{
						if (thing.name().equals(".."))
						{
							finish();
						}
						else
						{
					    	File uploads = new File(mPath + "/" + thing.name());
							Bundle b = new Bundle();
							b.putString("uploadPath", mUrl);
							b.putString("path", uploads.getAbsolutePath());
							b.putString("username",mUsername);
							b.putString("password", mPassword);
							b.putBoolean("isRoot", false);
							b.putInt("port",mPort);
							Intent i = new Intent(Upload.this, Upload.class);
							i.putExtras(b);
							startActivity(i);
						}
					}
					else
					{
						put();
					}
				}
			}
		);
		
		hideProgressDialog();
	}

	public void put()
	{
		
		showProgressDialog("Uploading to server.");
		new UploadFileTask().execute("");
	}

	public void onUploadResponse()
	{
		hideProgressDialog();
		
		//later: say "would you like to upload another file?"
		
		new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle("Upload Complete")
        .setMessage("Would you like to upload another file?")
        .setPositiveButton("Yes", null)
        .setNegativeButton("No",new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
            	DirList.refreshOnResume=true;
        		killMe=true;		
        		finish();  
            }

        })
        .show();
	}
	
	/**
	 * Show the Upload progress dialog
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
	 * Hide the Upload progress dialog
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
		inflater.inflate(R.menu.upload, menu);	        
		return true;
	}
	    
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle item selection
        switch (item.getItemId())
        {
    	case R.id.return_to_server:
    		DirList.refreshOnResume=true;
    		if (!isRoot)
    			killMe = true;
    		
    		finish();
    		return true;
        }
        return false;
    }

    public void onWindowFocusChanged(boolean hasFocus)
    {
    	if (killMe)
    	{
    		if (isRoot)
    			killMe = false;
    		
    		finish();
    	}
    }
}