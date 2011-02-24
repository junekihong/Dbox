package com.dbox.client;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DirListAdapter extends BaseAdapter
{    
    public Resource[] ls;
    private static LayoutInflater inflater = null;
    
    public DirListAdapter(Activity context, Resource[] ls)
    {
        this.ls = ls;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount()
    {
        return ls.length;
    }

    public Object getItem(int position)
    {
        return position;
    }

    public long getItemId(int position)
    {
        return position;
    }
    
    public static class ResourceHolder
    {
    	public TextView title;
    	public TextView metadata;
        public ImageView image;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        View resource = convertView;
        ResourceHolder holder;
        
        if ( convertView == null )
        {
            resource = inflater.inflate(R.layout.item, null);
            holder = new ResourceHolder();
            holder.title = (TextView)resource.findViewById(R.id.title);
            holder.metadata = (TextView)resource.findViewById(R.id.metadata);
            holder.image = (ImageView)resource.findViewById(R.id.image);
            resource.setTag(holder);
        }
        else
        {
            holder = (ResourceHolder) resource.getTag();
        }
        
        holder.title.setText(ls[position].name());
        holder.image.setTag(ls[position].name());
        
        if (ls[position].isDirectory())
        {
        	holder.image.setImageResource(R.drawable.dir);
        	holder.metadata.setText(ls[position].dateFormatted());
        }
        else
        {
        	holder.image.setImageResource(R.drawable.file);
        	holder.metadata.setText(ls[position].dateFormatted() + "  -  " + ls[position].type());
        }
        return resource;
    }
}