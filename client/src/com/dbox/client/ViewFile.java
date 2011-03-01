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
    private Resource file;
    private ProgressDialog mProgressDialog;
    private String mLocalPath;
    private URL mUrlObj;
    
	private class DownloadFileTask extends AsyncTask<String, Integer, Integer>
	{
		/**
		 * Executes task on a background thread.
		 */
		@Override
		protected Integer doInBackground(String... data)
		{
			try
			{
				file = WebService.get(mUrl,mPort,mUsername,mPassword)[0];
			} 
			catch (HttpException e)
			{
				return -2;
			}
			catch (Exception e)
			{
				return -1;
			}
			return 1;
		}

		/**
		 * Called on the UI thread after doInBackground has finished.
		 */
		@Override
	    protected void onPostExecute(Integer result)
	    {
			if ( result == 1 )
			{
				onServerResponse();
			}
			else
			{
				if (result == -2)
					printMessage("Could not connect to the host on the specified port.");
				else
					printMessage("Invalid login credentials. Please re-enter your username and password.");
				
				openLoginScreen();
			}
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
			try
			{
				WebService.delete(mUrl,mPort,mUsername,mPassword);
			}
			catch (HttpException e)
			{
				return -2;
			}
			catch (Exception e)
			{
				return -1;
			}
			return 1;
		}

		/**
		 * Called on the UI thread after doInBackground has finished.
		 */
		@Override
	    protected void onPostExecute(Integer result)
	    {
			if ( result == 1 )
			{
				onDeleteResponse();
			}
			else
			{
				if (result == -2)
					printMessage("Could not connect to the host on the specified port.");
				else
					printMessage("Invalid login credentials. Please re-enter your username and password.");
				
				openLoginScreen();
			}
	    }
	}

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewfile);
        
        Bundle bundle = getIntent().getExtras();
        
        try
        {
	        mUsername = bundle.getString("username");
	        mPassword = bundle.getString("password");
	        mUrl = bundle.getString("path").replace(" ", "%20");;
	        mPort = bundle.getInt("port");
	        
	        mUrlObj = new URL(mUrl);
	        mLocalPath = mUrlObj.getPath();
	        
	        File root = Environment.getExternalStorageDirectory();
	        File local = new File(root,"Downloads/" + mLocalPath);
	        
	        mLocalUri = Uri.fromFile(local);
	        
	        if (local.exists())
	        {
	        	file = bundle.getParcelable("resource");
	        	
	        	buildDisplay(true);
	        }
	        else
	        {
	        	get();
	        }
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
    
    public void buildDisplay(boolean showCachedMessage)
    {
    	Button refresh = (Button) findViewById(R.id.refreshButton);
    	refresh.setVisibility(View.VISIBLE);
    	refresh.setOnClickListener
    	(
    			new View.OnClickListener()
    			{
					@Override
					public void onClick(View v)
					{
						get();
					}
				}
    	);
    	
    	TextView refreshText = (TextView) findViewById(R.id.refreshText);
    	
    	if (showCachedMessage)
    		refreshText.setVisibility(View.VISIBLE);
    	else
    		refreshText.setVisibility(View.GONE);
    	
    	setText(file.name(), file.type(), file.size(), file.dateFormatted());
    	
    	Button button = (Button) findViewById(R.id.viewButton);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    
                    if (file.type().contains("text/"))
                    	intent.setDataAndType(mLocalUri, "text/plain");
                    else
                    	intent.setDataAndType(mLocalUri, file.type());
                    
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    try
                    {
                        startActivity(intent);
                    } 
                    catch (ActivityNotFoundException e)
                    {
                        Toast.makeText(ViewFile.this,"Could not find an application to view " + file.type() + " files.", Toast.LENGTH_LONG).show();
                    }
            }
        });
    }
    
    public void setText(String n, String t, int s, String d)
    {
   		TextView title = (TextView) findViewById(R.id.title);
		TextView name = (TextView) findViewById(R.id.name);
    	TextView type = (TextView) findViewById(R.id.type);
    	TextView size = (TextView) findViewById(R.id.filesize);
    	TextView date = (TextView) findViewById(R.id.date);
    	
    	title.setText("Viewing File: " + n);
    	name.setText(n);
    	type.setText(t);
    	size.setText( ((double) s)/1000 + "kb");
    	date.setText(d);
    }
    
    public void onServerResponse()
    {
    	if (file != null)
    	{
    		setText(file.name(), file.type(), file.size(), file.dateFormatted());
	    	
	    	if (file.type().equals("text/plain"))
	    		writeFileToDisk(mLocalPath,file.content(),false);
	    	else
	    		writeFileToDisk(mLocalPath,file.content(),true);
    	}
    	
    	buildDisplay(false);
    	
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
        
        menu.setGroupVisible(R.id.upload_group, false);
        
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
    	case R.id.logout:
    		openLoginScreen();
    		return true;
    	case R.id.home:
    		home();
    		return true;
    	case R.id.password:
    		openChangePasswordScreen();
    		return true;
        }
        return false;
    }
    
    /**
     * Write file to disk.  
     * @param path the file path
     * @param data the file data
     * @param encoded true if data[] has been encoded as base64, false otherwise 
     * @return true if the file was written successfully, false otherwise
     */
    public boolean writeFileToDisk(String path, byte[] data, boolean encoded)
    {
    	try
    	{
    		byte[] decoded = data;
    		
    		if (encoded)
    			decoded = Base64.decode(data, Base64.DEFAULT);	
    		
    		data = null;
    		
    		File root = Environment.getExternalStorageDirectory();

    		if (root.canWrite())
    		{
    			File f = new File(root, "Downloads/" + path);
    			File x = new File(f.getParent());
    			x.mkdirs();
    			x = null;
    			FileOutputStream writer = new FileOutputStream(f);
    			writer.write(decoded);
    			writer.close();
    			writer = null;
    			f = null;
    			root = null;
    			return true;
    		}
    	}
    	catch( Exception e )
    	{
    		e.printStackTrace();
    	}

    	return false;
    }
    
    public void printMessage(String message)
    {
    	Toast.makeText(this,message, Toast.LENGTH_LONG).show();
    }
    
    public void home()
    {
    	DirList.finishUnlessHome = true;
    	finish();
    }
    
    public void openChangePasswordScreen()
    {
		Intent i = new Intent(this,Password.class);
		Bundle b = new Bundle();
		b.putString("username",mUsername);
		b.putString("password",mPassword);
		b.putString("host","http://" + mUrlObj.getHost());
		b.putInt("port", mPort);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.putExtras(b);
		startActivity(i);
		finish();
    }
    
    public void openLoginScreen()
    {
		Intent i = new Intent(ViewFile.this,Login.class);
		i.putExtra("port", mPort);
		i.putExtra("path", mUrl);
		i.putExtra("isDir", false);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
		finish();
    }
}