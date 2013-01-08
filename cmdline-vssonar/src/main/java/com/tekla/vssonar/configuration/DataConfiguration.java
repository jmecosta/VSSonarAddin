/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tekla.vssonar.configuration;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author jocs
 */
public class DataConfiguration {

    private Map<String, PropertiesDefinition> configElements = new HashMap<String, PropertiesDefinition>();

    public void addNewSingleConfigurationElement(String id) {
        configElements.put(id, new PropertiesDefinition(id, true));
    }

    public void addNewComplexConfigurationElement(String id, String[] props) {

        PropertiesDefinition newprop = new PropertiesDefinition(id, false);
        for (String each : props) {
            newprop.addPropDefinitions(each);
        }
        configElements.put(id, newprop);
    }

    public Map<String, PropertiesDefinition> getConfigElems() {
        return configElements;
    }

    public String getStringParameter(String defaultval, String key) {

        if (configElements.containsKey(key)) {
            return configElements.get(key).getValue();
        }
        return defaultval;
    }

    public String getInStringParameter(String defaultval, String key, String insideKey) {

        try {
            return configElements.get(key).getProperties().get(insideKey);
        } catch (Exception ex) {
            return defaultval;
        }

    }

    public boolean getInBooleanParameter(boolean defaultval, String key, String keyIn) {
        String val = configElements.get(key).getProperties().get(keyIn);
        if (val.equalsIgnoreCase("true")) {
            return true;
        }
        return false;
    }

    public static void getPropsFromXMLFile(Map<String, PropertiesDefinition> configElements, String pathToFile) {

        try {

            File fXmlFile = new File(pathToFile);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            Iterator itmain = configElements.entrySet().iterator();
            while (itmain.hasNext()) {
                Map.Entry pairs = (Map.Entry) itmain.next();
                if (((PropertiesDefinition) pairs.getValue()).isSingle()) {
                    ((PropertiesDefinition) pairs.getValue()).setPropValue(doc.getElementsByTagName((String) pairs.getKey()).item(0).getTextContent());
                } else {
                    NodeList nList = doc.getElementsByTagName((String) pairs.getKey());
                    for (int temp = 0; temp < nList.getLength(); temp++) {

                        Node nNode = nList.item(temp);
                        if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                            Element eElement = (Element) nNode;
                            Iterator it = ((PropertiesDefinition) pairs.getValue()).getProperties().entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry pairs2 = (Map.Entry) it.next();
                                try {
                                    ((PropertiesDefinition) pairs.getValue()).getProperties().put((String) pairs2.getKey(), getTagValue((String) pairs2.getKey(), eElement));
                                } catch (java.lang.NullPointerException e) {
                                }
                            }
                        }
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getTagValue(String sTag, Element eElement) {
        String value = "";

        NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
        Node nValue = (Node) nlList.item(0);
        value = nValue.getNodeValue();


        return value;

    }
}
