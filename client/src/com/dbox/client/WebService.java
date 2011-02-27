package com.dbox.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.BasicHttpEntity;
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

			// Check that login was successful.
			if (response.getStatusLine().getStatusCode() == 200)
			{
				InputStream responseStream = entity.getContent();
				
				/*
				ByteArrayOutputStream responseData = new ByteArrayOutputStream();
				int ch;

				// read the server response
				while ((ch = responseStream.read()) != -1)
				{
					responseData.write(ch);
				}
				
				// HttpEntity instance is no longer needed, so we signal that resources 
				// should be deallocated
				if (entity != null)
				{
					entity.consumeContent();
				}
				
				String xml = new String(responseData.toByteArray());
				responseData = null;
				*/
				
				return XmlEngine.xmlToResource(responseStream);
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
	
	public static boolean delete ( String url, int port, String username, String password )
	{
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
			HttpDelete delete = new HttpDelete(url);
			HttpResponse response = httpclient.execute(delete);
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
				
				return true;
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
		
		return false;
	}
	public static boolean put ( String url, int port, String username, String password, String body)
	{
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
			HttpPut httpput = new HttpPut(url);
			BasicHttpEntity x = new BasicHttpEntity();
			x.setContent(new ByteArrayInputStream(body.getBytes("UTF-8")));
			httpput.setEntity(x);
			
			HttpResponse response = httpclient.execute(httpput);
			HttpEntity entity = response.getEntity();

			System.out.println("UPLOAD STATUS CODE:");
			System.out.println(response.getStatusLine().getStatusCode());

			// Check that the upload was successful.
			if (response.getStatusLine().getStatusCode() == 200)
			{
				return true;
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
		
		return false;
	}
}
