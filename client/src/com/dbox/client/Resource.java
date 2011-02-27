package com.dbox.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Date;

import android.util.Base64;
import android.webkit.MimeTypeMap;


public class Resource
{
	private String name;
	private String url;
	private String type;
	private Date date;
	private int size;
	private boolean isDirectory;
	private String content;
	
	public Resource( String name, String url, String type, Date date, int size, boolean dir)
	{
		this.name = name;
		this.url = url;
		this.type = type;
		this.date = date;
		this.size = size;
		this.isDirectory = dir;
	}
	
	public Resource(File file)
	{
		String name = file.getName();
		
		String ext;
		int x = name.lastIndexOf(".");
		if(x<0)
			ext = "";
		else
			ext = name.substring(x);
		
		FileInputStream stream = null;
		FileChannel channel = null;
		byte[] bytes = null;
		
		try 
		{
	        stream = new FileInputStream(file);
	        channel = stream.getChannel();
	        int size = (int) channel.size();
	        MappedByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, size);
	        bytes = new byte[size];
	        buffer.get(bytes);

	    } 
		catch (IOException e) 
	    {
	        e.printStackTrace();
	    }
		finally 
		{
			try 
			{
	            if (stream != null) 
	            {
	                stream.close();
	            }
	            if (channel != null) 
	            {
	                channel.close();
	            }
	        } 
			catch (IOException e) 
	        {
	            e.printStackTrace();
	        }
	    }
		
		this.name = name;
		this.url = file.getAbsolutePath();
		this.type = MimeTypeMap.getSingleton().getExtensionFromMimeType(ext);
		this.date = new Date(file.lastModified());
		this.size = (int) file.length();
		this.isDirectory = file.isDirectory();
		this.content = Base64.encodeToString(bytes, Base64.DEFAULT);
	}
	
	
	public String name()
	{
		return name;
	}
	
	public String url()
	{
		return url;
	}
	
	public String type()
	{
		return type;
	}
	
	public Date date()
	{
		return date;
	}
	
	public String dateFormatted()
	{
		return date.getMonth() + "/" + date.getDate() + "/" + date.getYear();
	}
	
	public int size()
	{
		return size;
	}
	
	public boolean isDirectory()
	{
		return isDirectory;
	}
	
	public String content()
	{
		return content;
	}
	
	public void setContent(String c)
	{
		content = c;
	}
}
