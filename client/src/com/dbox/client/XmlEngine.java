package com.dbox.client;

import java.io.InputStream;
import java.io.StringReader;
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
	//public static Resource[] xmlToResource(String xml)
	public static Resource[] xmlToResource(InputStream is)
	{
		Resource[] result = null;
		int resultSize = 0;
		
		try
		{
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = db.parse(is);
			doc.getDocumentElement().normalize();
			NodeList nodeList = doc.getElementsByTagName("Resource");
			
			result = new Resource[nodeList.getLength()];
			
			System.out.println("Resource Size: " + nodeList.getLength());

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
						result[resultSize-1].setContent(e.getElementsByTagName("ResourceContent").item(0).getChildNodes().item(0).getNodeValue());
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
}
