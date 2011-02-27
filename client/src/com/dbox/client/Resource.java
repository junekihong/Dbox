package com.dbox.client;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class Resource implements Parcelable
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
}
