package com.cloud.agent.resource.computing;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class LibvirtStorageVolumeXMLParser {
	private static final Logger s_logger = Logger
			.getLogger(LibvirtStorageVolumeXMLParser.class);

	public LibvirtStorageVolumeDef parseStorageVolumeXML(String volXML) {
		DocumentBuilder builder;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(volXML));
			Document doc = builder.parse(is);

			Element rootElement = doc.getDocumentElement();

			String VolName = getTagValue("name", rootElement);
			Element target = (Element) rootElement.getElementsByTagName(
					"target").item(0);
			String format = getAttrValue("type", "format", target);
			Long capacity = Long
					.parseLong(getTagValue("capacity", rootElement));
			return new LibvirtStorageVolumeDef(VolName, capacity,
					LibvirtStorageVolumeDef.volFormat.getFormat(format), null,
					null);
		} catch (ParserConfigurationException e) {
			s_logger.debug(e.toString());
		} catch (SAXException e) {
			s_logger.debug(e.toString());
		} catch (IOException e) {
			s_logger.debug(e.toString());
		}
		return null;
	}

	private static String getTagValue(String tag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(tag).item(0)
				.getChildNodes();
		Node nValue = (Node) nlList.item(0);

		return nValue.getNodeValue();
	}

	private static String getAttrValue(String tag, String attr, Element eElement) {
		NodeList tagNode = eElement.getElementsByTagName(tag);
		if (tagNode.getLength() == 0) {
			return null;
		}
		Element node = (Element) tagNode.item(0);
		return node.getAttribute(attr);
	}

	public static void main(String[] args) {
		s_logger.addAppender(new org.apache.log4j.ConsoleAppender(
				new org.apache.log4j.PatternLayout(), "System.out"));
		String storagePool = "<pool type='dir'>" + "<name>test</name>"
				+ "<uuid>bf723c83-4b95-259c-7089-60776e61a11f</uuid>"
				+ "<capacity>20314165248</capacity>"
				+ "<allocation>1955450880</allocation>"
				+ "<available>18358714368</available>" + "<source>"
				+ "<host name='nfs1.lab.vmops.com'/>"
				+ "<dir path='/export/home/edison/kvm/primary'/>"
				+ "<format type='auto'/>" + "</source>" + "<target>"
				+ "<path>/media</path>" + "<permissions>" + "<mode>0700</mode>"
				+ "<owner>0</owner>" + "<group>0</group>" + "</permissions>"
				+ "</target>" + "</pool>";

		LibvirtStoragePoolXMLParser parser = new LibvirtStoragePoolXMLParser();
		LibvirtStoragePoolDef pool = parser.parseStoragePoolXML(storagePool);
		s_logger.debug(pool.toString());
	}
}
