package com.dbox.client;

import java.io.ByteArrayInputStream;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XmlEngine
{
	private static byte[] startTag = "<ResourceContent>".getBytes();
	private static byte[] closeTag = "</ResourceContent>".getBytes();
	
	public static Resource[] xmlToResource(byte[] data)
	{
		// search the data for the bytes in startTag and closeTag
		int startTagIndex = indexOf(data,startTag);
		int closeTagIndex = indexOf(data,closeTag);
		
		// initialize data structures
		byte[] xml = null;
		byte[] base64data = null;
		Resource[] result = null;
		int resultSize = 0;
		
		// if the bytes in startTag and closeTag were found...
		if (startTagIndex != -1 && closeTagIndex != -1 && startTagIndex < closeTagIndex)
		{
			// set startTagIndex to point to the end of the <ResourceContent> tag
			startTagIndex += startTag.length;
			
			// calculate the size of the base64 data 
			int base64size = closeTagIndex - startTagIndex;
			
			// create a byte[] to hold the base64 data
			base64data = new byte[base64size];
			
			// create a byte[] to hold the xml data
			xml = new byte[data.length-base64size];
			
			// copy the base64 bytes from 'data' to 'base64data'
			System.arraycopy(data, startTagIndex, base64data, 0, base64size);
			
			// copy the first part of the xml bytes from data[] to xml[]
			System.arraycopy(data, 0, xml, 0, startTagIndex);
			
			// copy the second part of the xml bytes from data[] to xml[]
			System.arraycopy(data, closeTagIndex, xml, startTagIndex, data.length-closeTagIndex);
		}
		else
		{
			// data is entirely xml
			xml = data;
		}
		
		// deallocate data
		data = null;
		
		try
		{
			// parse the xml
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = db.parse(new InputSource(new ByteArrayInputStream(xml)));
			
			// deallocate
			xml = null;
			db = null;
			
			// find all nodes with tag <Resource>
			doc.getDocumentElement().normalize();
			NodeList nodeList = doc.getElementsByTagName("Resource");
			
			// initialize the resource array (one entry for each resource node)
			result = new Resource[nodeList.getLength()];

			// iterate thru the resource nodes...
			for (int s = 0; s < nodeList.getLength() ; s++)
			{
				// declare vars
				Node n;
				NamedNodeMap attr;
				Element e;
				String resourceName, resourceURL, resourceType, resourceCategory;
				Date resourceDate;
				int resourceSize, year, month, day, hour, min, sec;
				boolean isDirectory;
				
				n = nodeList.item(s);
				attr = n.getAttributes();
				resourceCategory = attr.getNamedItem("category").getNodeValue();
				
				if (resourceCategory.equals("directory"))
					isDirectory = true;
				else
					isDirectory = false;

				if (n.getNodeType() == Node.ELEMENT_NODE)
				{
					e = (Element) n;

					resourceName = e.getElementsByTagName("ResourceName").item(0).getChildNodes().item(0).getNodeValue();
					
					if (e.getElementsByTagName("ResourceURL").getLength() > 0)
						resourceURL = e.getElementsByTagName("ResourceURL").item(0).getChildNodes().item(0).getNodeValue();
					else
						resourceURL = "";
					
					resourceType = e.getElementsByTagName("ResourceType").item(0).getChildNodes().item(0).getNodeValue();
					resourceSize = Integer.parseInt(e.getElementsByTagName("ResourceSize").item(0).getChildNodes().item(0).getNodeValue());
					year = Integer.parseInt(e.getElementsByTagName("year").item(0).getChildNodes().item(0).getNodeValue());
					month = Integer.parseInt(e.getElementsByTagName("month").item(0).getChildNodes().item(0).getNodeValue());
					day = Integer.parseInt(e.getElementsByTagName("day").item(0).getChildNodes().item(0).getNodeValue());
					hour = Integer.parseInt(e.getElementsByTagName("hour").item(0).getChildNodes().item(0).getNodeValue());
					min = Integer.parseInt(e.getElementsByTagName("min").item(0).getChildNodes().item(0).getNodeValue());
					sec = Integer.parseInt(e.getElementsByTagName("sec").item(0).getChildNodes().item(0).getNodeValue());
					resourceDate = new Date(year,month,day,hour,min,sec);
					
					result[resultSize++] = new Resource(resourceName,resourceURL,resourceType,resourceDate,resourceSize,isDirectory);
					
					if (e.getElementsByTagName("ResourceContent").getLength() > 0)
					{
						result[resultSize-1].setContent(base64data);
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static String resourceToXml(Resource x)
	{
		StringBuilder sb = new StringBuilder();
		
		if (x.isDirectory())
			sb.append("<Resource category=\"directory\">");
		else
			sb.append("<Resource category=\"file\">");
		
		sb.append("<ResourceName>" + x.name() + "</ResourceName>");
		sb.append("<ResourceSize>" + x.name() + "</ResourceSize>");
		sb.append("<ResourceURL>" + x.url() + "</ResourceURL>");
		sb.append("<ResourceDate>");
		sb.append("<year>" + x.date().getYear() + "</year>");
		sb.append("<month>" + x.date().getMonth() + "</month>");
		sb.append("<day>" + x.date().getDate() + "</day>");
		sb.append("<hour>" + x.date().getHours() + "</hour>");
		sb.append("<min>" + x.date().getMinutes() + "</min>");
		sb.append("<sec>" + x.date().getSeconds() + "</sec>");
		sb.append("</ResourceDate>");
		sb.append("<ResourceType>" + x.type() + "</ResourceType>");
		
		return sb.toString();
	}
	
	/**
	 * Finds the first occurrence of pattern[] in data[] using the KMP algorithm. 
	 * See: http://en.wikipedia.org/wiki/Knuth%E2%80%93Morris%E2%80%93Pratt_algorithm
	 * @param data the data to search
	 * @param pattern the pattern to search for
	 * @return the index of the first occurent of pattern[] in data[], or -1 if the pattern is not found.
	 */
	public static int indexOf(byte[] data, byte[] pattern)
	{
		if (data.length == 0 || pattern.length == 0)
			return -1;
		
		int[] partialMatch = computePartialMatch(pattern);

		int a = 0, b;

		for (b = 0; b < data.length; b++)
		{
			while (a > 0 && pattern[a] != data[b])
				a = partialMatch[a - 1];
			
			if (pattern[a] == data[b])
				a++;
			
			if (a == pattern.length)
				return b - pattern.length + 1;
		}
		
		return -1;
	}

	/**
	 * Computes the partial match (failure function) for the given pattern.
	 * See: http://en.wikipedia.org/wiki/Knuth%E2%80%93Morris%E2%80%93Pratt_algorithm
	 * @param pattern the search pattern
	 * @return the partial match table
	 */
	private static int[] computePartialMatch(byte[] pattern)
	{
		int[] partialMatch = new int[pattern.length];
	
		int a = 0, b;
		
		for (b = 1; b < pattern.length; b++)
		{
			while (a > 0 && pattern[a] != pattern[b])
				a = partialMatch[a - 1];
			
			if (pattern[a] == pattern[b])
				a++;

			partialMatch[b] = a;
		}
	
		return partialMatch;
	}
}
