package com.dbox.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class DirList extends Activity
{   
    ListView list;
    DirListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dirlist);
        
        list = (ListView)findViewById(R.id.list);

        Resource[] ls = WebService.get("/noah/");
        
        adapter = new DirListAdapter(this,ls);
        list.setAdapter(adapter);
        
        list.setOnItemClickListener
        (
        	new OnItemClickListener()
        	{
        		@Override
        		public void onItemClick(AdapterView<?> a, View v, int position, long id)
        		{
        			System.out.println(adapter.ls[position].name());
        			System.out.println(adapter.ls[position].url());
        			Intent i = new Intent(DirList.this,DirList.class);
        			startActivity(i);        			
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