package com.dbox.client;

import java.net.URL;
import java.util.Date;

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
        	HttpGet httpget = new HttpGet(host);
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
	
	public static Resource[] get ( String url )
	{
		Date k = new Date(2011,2,22);
        Resource a = new Resource("file1.txt","www","text/plain",k,10,false);
        Resource b = new Resource("file2.txt","www","text/plain",k,10,false);
        Resource c = new Resource("file3.txt","www","text/plain",k,10,false);
        Resource d = new Resource("file4.txt","www","application/octet-stream",k,10,false);
        Resource e = new Resource("foo","www","directory",k,10,true);
        Resource f = new Resource("bar","www","directory",k,10,true);
        Resource g = new Resource("42","www","directory",k,10,true);
        
        Resource[] ls = new Resource[7];
        ls[0] = e;
        ls[1] = f;
        ls[2] = g;
        ls[3] = d;
        ls[4] = a;
        ls[5] = b;
        ls[6] = c;
        
        return ls;
	}
}
