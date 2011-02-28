package com.dbox.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;
import android.webkit.MimeTypeMap;


public class Resource implements Parcelable, Comparable<Resource>
{
	private String name;
	private String url;
	private String type;
	private Date date;
	private int size;
	private boolean isDirectory;
	private byte[] content;
	
	public Resource( String name, String url, String type, Date date, int size, boolean dir)
	{
		this.name = name;
		this.url = url;
		this.type = type;
		this.date = date;
		this.size = size;
		this.isDirectory = dir;
	}
	
	public Resource (Parcel in)
	{
		name = in.readString();
		url = in.readString();
		type = in.readString();
		date = new Date(in.readLong());
		size = in.readInt();
		isDirectory = (in.readInt() == 1) ? true : false;
	}
	
	public static final Parcelable.Creator<Resource> CREATOR = new Parcelable.Creator<Resource>()
	{
		public Resource createFromParcel(Parcel in)
		{
			return new Resource(in); 
		}

		public Resource[] newArray(int size)
		{
			return new Resource[size];
		}
	};
	
	public Resource(File file, String Url)
	{
		String name = file.getName();
		
		String ext;
		int x = name.lastIndexOf(".");
		if(x<0)
			ext = "";
		else
			ext = name.substring(x+1);
		
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
		this.url = Url;
		this.type = MimeTypeMap.getSingleton().getExtensionFromMimeType(ext);
		this.date = new Date(file.lastModified());
		this.size = (int) file.length();
		this.isDirectory = file.isDirectory();
		
		if (!file.isDirectory())
			this.content = bytes; //Base64.encode(bytes, Base64.DEFAULT);
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
	
	public byte[] content()
	{
		return content;
	}
	
	public void setContent(byte[] c)
	{
		content = c;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(name);
		dest.writeString(url);
		dest.writeString(type);
		dest.writeLong(date.getTime());
		dest.writeInt(size);
		
		if (isDirectory)
			dest.writeInt(1);
		else
			dest.writeInt(0);
	}

	@Override
	public int compareTo(Resource o)
	{
		if (isDirectory() && !o.isDirectory())
		{
			return -1;
		}
		else if (!isDirectory() && o.isDirectory())
		{
			return 1;
		}
		else
		{
			return name().compareToIgnoreCase(o.name());
		}
	}
}
