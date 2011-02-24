package com.dbox.client;

import java.util.Date;

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
