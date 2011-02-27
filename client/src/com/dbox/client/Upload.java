package com.dbox.client;

import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;




/**
 * Uploading Activity
 */

public class Upload extends Activity
{
	private ListView listView;
	private DirListAdapter adapter;
    private String mUrl;
    private String mUsername;
    private String mPassword;
    private int mPort;
    private Resource thing;
    private Resource[] resources;
    private ProgressDialog mProgressDialog;
    
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
			return WebService.put(mUrl,mPort,mUsername,mPassword,mBody);
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
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dirlist);
		
		Bundle bundle = getIntent().getExtras();
		
		try
		{
			mUrl = bundle.getString("path");
			
			
			buildList();
			//put();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void put()
	{
		
		showProgressDialog("Uploading to server.");
		new UploadFileTask().execute("");
	}

	public void buildList()
	{
		
		File f = Environment.getExternalStorageDirectory();
		File Uploads = new File(f,"Uploads/");
		if(!Uploads.exists())
		{
			Uploads.mkdir();
		}
		File[] list= Uploads.listFiles();
		
		
		int listLength =list.length;
		if(listLength==0)
		{
			System.out.println("LIST IS EMPTY");
		}
		resources= new Resource[listLength];
		
		for(int i = 0; i < list.length-1; i++)
		{
			resources[i] = new Resource(list[i]);
		}
		
		adapter = new DirListAdapter(this,resources);
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
					put();
				}
			}
		);
	}

	public void onUploadResponse()
	{
		hideProgressDialog();
		
		//later: say "would you like to upload another file?"
		
		finish();
	}
	
	
	/**
	 * Show the login progress dialog
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
	 * Hide the login progress dialog
	 * @return void
	 */
	public void hideProgressDialog()
	{
		mProgressDialog.dismiss();
		mProgressDialog = null;
	}
	
	
	
	
	
	
	
	
	
}
	
	

