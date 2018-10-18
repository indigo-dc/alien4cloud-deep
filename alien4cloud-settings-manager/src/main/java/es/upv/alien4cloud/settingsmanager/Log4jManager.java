package es.upv.alien4cloud.settingsmanager;

import java.io.IOException;
import java.io.InputStream;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class Log4jManager {
	

	public static final String CONFIG_TEMPLATE_PATH = "config/log4j2.xml";

	  private Element root;
	  private Document doc;
	  
		public Log4jManager() throws IOException, ParserConfigurationException, SAXException{
		    InputStream inConfigTemplate = ConfigManager.class.getClassLoader().getResourceAsStream(CONFIG_TEMPLATE_PATH);

		    DocumentBuilderFactory factory =
		    		DocumentBuilderFactory.newInstance();
    		DocumentBuilder builder = factory.newDocumentBuilder();
    		StringBuilder xmlStringBuilder = new StringBuilder();
    		doc = builder.parse(inConfigTemplate);
    		root = doc.getDocumentElement();
		}
		
		public void setFullPathLogs(String fullPathLogs) throws ValueNotFoundException {
			if (!fullPathLogs.endsWith("/"))
				fullPathLogs += "/";
			NodeList property = root.getElementsByTagName("Properties");
			if (property != null && property.getLength() > 0) {
				NodeList properties = property.item(0).getChildNodes();
				if (properties != null && properties.getLength() > 0) {
					boolean found = false;
					for (int idx=0; idx<properties.getLength() && !found; ++idx) {
						NamedNodeMap at = properties.item(idx).getAttributes();
						if (at != null) {
							for (int idxAt=0; idxAt<at.getLength() && !found; ++idxAt) {
								if (at.item(idxAt).getNodeName().equalsIgnoreCase("name") &&
										at.item(idxAt).getNodeValue().equalsIgnoreCase("deployment_path"))
								{
									found = true;
									String oldPath = properties.item(idx).getTextContent();
									properties.item(idx).setTextContent(fullPathLogs + oldPath);
								}
							}
						}
					}
					if (!found)
						throw new ValueNotFoundException(
						          String.format("Can't find the properties.property node with name %s in the %s template", "deployment_path", CONFIG_TEMPLATE_PATH));
				} else
					throw new ValueNotFoundException(
					          String.format("Can't find the properties.property node in the %s template", CONFIG_TEMPLATE_PATH));
			} else
				throw new ValueNotFoundException(
				          String.format("Can't find the properties node in the %s template", CONFIG_TEMPLATE_PATH));
		}
		
		public void writeConfig(String outFileFullPath) throws TransformerException {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(outFileFullPath));
			transformer.transform(source, result);
		}
	  
	
}
