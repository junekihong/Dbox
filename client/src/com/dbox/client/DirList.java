package com.dbox.client;

import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class DirList extends Activity
{   
    ListView list;
    DirListAdapter adapter;
    
    private String mHost;
    private String mUsername;
    private String mPassword;
    private int mPort;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dirlist);
        
        Bundle bundle = getIntent().getExtras();
        
        String path = "", url = "";
        
        try
        {
	        mHost = bundle.getString("host");
	        mUsername = bundle.getString("username");
	        mPassword = bundle.getString("password");
	        url = bundle.getString("path");
	        path = new URL(url).getPath();
	        mPort = bundle.getInt("port");
        }
        catch (Exception e) {  }
        
        TextView t = (TextView) findViewById(R.id.path);
        t.setText(path);
        
        list = (ListView)findViewById(R.id.list);

        Resource[] ls = WebService.get(url,mPort,mUsername,mPassword);
        
        adapter = new DirListAdapter(this,ls);
        list.setAdapter(adapter);
        
        list.setOnItemClickListener
        (
        	new OnItemClickListener()
        	{
        		@Override
        		public void onItemClick(AdapterView<?> a, View v, int position, long id)
        		{
        			Bundle b = new Bundle();
        			b.putString("host",mHost);
        			b.putString("username",mUsername);
        			b.putString("password",mPassword);
        			b.putString("path", adapter.ls[position].url());
        			b.putInt("port",mPort);
        			
        			if (adapter.ls[position].isDirectory())
        			{	
	        			Intent i = new Intent(DirList.this,DirList.class);
	        			i.putExtras(b);
	        			startActivity(i); 
        			}
        			else
        			{
	        			Intent i = new Intent(DirList.this,DirList.class);
	        			i.putExtras(b);
	        			startActivity(i); 
        			}
        		}
        	 }
        );
    }
    
    @Override
    public void onDestroy()
    {
        list.setAdapter(null);
        super.onDestroy();
    }
}