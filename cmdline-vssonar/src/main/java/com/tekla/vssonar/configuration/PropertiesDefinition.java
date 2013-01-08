/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tekla.vssonar.configuration;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jocs
 */
public class PropertiesDefinition {
    private boolean singleData = true;
    private final String name;
    private String value;
    private Map<String, String> properties = new HashMap<String, String>();          
    
    public Map<String, String> getProperties () {
        return properties;
    }
    
    public PropertiesDefinition(String nameIn, boolean isSingle) {
        name = nameIn;
        value = "";
        singleData = isSingle;
    }
    
    public void setPropValue(String val) {
        value = val;        
    }
    
    public void addPropDefinitions(String def) {
        properties.put(def, "");    
    }
   
    public boolean isSingle() {
        return singleData;
    }
    
    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
    
}
