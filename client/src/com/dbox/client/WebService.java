package com.dbox.client;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class WebService
{
	public static Login.LoggedIn login ( String host, int port, String username, String password )
	{
		Login.LoggedIn loggedIn;
		
        try
        {
        	// Parse out the domain from the given URL. 
            URL url = new URL(host);
            String domain = url.getHost();
            
            // Create a connection to the server.
        	DefaultHttpClient httpclient = new DefaultHttpClient();
        	httpclient.getCredentialsProvider().setCredentials
        	(
                new AuthScope(domain, port),
                new UsernamePasswordCredentials(username, password)
        	);
        	
        	// Send the request and receive the response.
        	HttpGet httpget = new HttpGet("http://" + domain + ":" + port + "/" + username + "/");
        	HttpResponse response = httpclient.execute(httpget);
        	HttpEntity entity = response.getEntity();
        	
        	System.out.println("LOGIN STATUS CODE:");
        	System.out.println(response.getStatusLine().getStatusCode());
        	
        	// Check that login was successful.
        	if (response.getStatusLine().getStatusCode() == 200)
        	{
        		loggedIn = Login.LoggedIn.SUCCESS;
        	}
        	else
        	{
        		loggedIn = Login.LoggedIn.FAILURE;
        	}
	        
        	// HttpEntity instance is no longer needed, so we signal that resources 
        	// should be deallocated
	        if (entity != null)
	        {
	            entity.consumeContent();
	        }

	        // HttpClient instance is no longer needed, so we shut down the connection manager
	        // to ensure the immediate deallocation of all system resources
	        httpclient.getConnectionManager().shutdown();
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        	loggedIn = Login.LoggedIn.ERROR;
        }
        
		return loggedIn;
	}
	
	public static Resource[] get ( String url, int port, String username, String password )
	{
		/*
		String xml = "<ResourceList><Resource category=\"dir\"><ResourceName>bar.txt</ResourceName><ResourceSize>10</ResourceSize><ResourceURL>http://acm.jhu.edu:42080/foo//bar.txt</ResourceURL><ResourceDate><year>2011</year><month>2</month><day>11</day><hour>15</hour><min>1</min><sec>29</sec></ResourceDate><ResourceType>text/plain</ResourceType></Resource><Resource category=\"file\"><ResourceName>42</ResourceName><ResourceSize>3</ResourceSize><ResourceURL>http://acm.jhu.edu:42080/foo//42</ResourceURL><ResourceDate><year>2011</year><month>2</month><day>11</day><hour>15</hour><min>1</min><sec>29</sec></ResourceDate><ResourceType>application/octet-stream</ResourceType></Resource><Resource category=\"file\"><ResourceName>21</ResourceName><ResourceSize>0</ResourceSize><ResourceURL>http://acm.jhu.edu:42080/foo//21</ResourceURL><ResourceDate><year>2011</year><month>2</month><day>11</day><hour>15</hour><min>1</min><sec>29</sec></ResourceDate><ResourceType>application/octet-stream</ResourceType></Resource></ResourceList>";
		
		Resource[] ls = XmlEngine.xmlToResource(xml);
		
		System.out.println("LS Size: " + ls.length);
		
		return ls;
		*/
		
		try
		{
			// Parse out the domain from the given URL. 
			String domain = new URL(url).getHost();

			// Create a connection to the server.
			DefaultHttpClient httpclient = new DefaultHttpClient();
			httpclient.getCredentialsProvider().setCredentials
			(
				new AuthScope(domain, port),
				new UsernamePasswordCredentials(username, password)
			);

			// Send the request and receive the response.
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();

			System.out.println("LOGIN STATUS CODE:");
			System.out.println(response.getStatusLine().getStatusCode());

			// Check that login was successful.
			if (response.getStatusLine().getStatusCode() == 200)
			{
				InputStream responseStream = entity.getContent();
				ByteArrayOutputStream responseData = new ByteArrayOutputStream();
				int ch;

				// read the server response
				while ((ch = responseStream.read()) != -1)
				{
					responseData.write(ch);
				}
				
				return XmlEngine.xmlToResource(new String(responseData.toByteArray()));
			}

			// HttpEntity instance is no longer needed, so we signal that resources 
			// should be deallocated
			if (entity != null)
			{
				entity.consumeContent();
			}

			// HttpClient instance is no longer needed, so we shut down the connection manager
			// to ensure the immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
}
