/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tekla.vssonar.sensors;

import com.tekla.vssonar.configuration.Configuration;
import com.tekla.vssonar.configuration.DataConfiguration;
import com.tekla.vssonar.configuration.PropertiesDefinition;
import com.tekla.vssonar.utils.CmdExecutor;
import com.tekla.vssonar.utils.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.sonar.wsclient.services.Violation;

/**
 * {@inheritDoc}
 */
public abstract class ToolSensor {

    public static final String ERROR = "error";    
    public static final String SENSOR_EXEC_CMD  = "executable";
    public static final String SENSOR_EXEC_ARGS  = "args";
    public static final String SENSOR_EXEC_RULE_KEY  = "rulekey";
    public static final String SENSOR_EXEC_OUTPUT  = "output";
    public static final String SENSOR_EXEC_LANGUAGE  = "language";
    
    private final String[] configStr = {SENSOR_EXEC_CMD, SENSOR_EXEC_RULE_KEY, SENSOR_EXEC_ARGS, SENSOR_EXEC_OUTPUT, SENSOR_EXEC_LANGUAGE};    
    private DataConfiguration toolConfig = new DataConfiguration();    
    private List<Violation> violations = new ArrayList<Violation>();
    private final CmdExecutor executor;    
    private final Configuration configuration;
    private final String sensorKey;
        
    public ToolSensor(String key, String file, CmdExecutor exec, Configuration pluginconfig) {   
        sensorKey = key;
        executor = exec;
        configuration = pluginconfig;
        toolConfig.addNewComplexConfigurationElement(sensorKey, configStr);
                
        // read xml
        String configFile = Utils.loadResourceAsStream(file).toString();
        Map<String, PropertiesDefinition> configElements = toolConfig.getConfigElems();
        DataConfiguration.getPropsFromXMLFile(configElements, configFile);        
        String userConfigurationFile = configuration.getUserConfigurationFile();
        if(!userConfigurationFile.equals("")){   
            // this will overwrite config settings
            DataConfiguration.getPropsFromXMLFile(toolConfig.getConfigElems(), userConfigurationFile);
        }
    }
    
    public List<Violation> getViolations() {
        return violations;    
    }

    protected List<String> packBasicExecutorCmd(String file) {
        List<String> cmdArray = new ArrayList<String>();
        cmdArray.add(getToolConfig().getInStringParameter(ERROR, getSensorKey(), getSensorExeccmd()).toString());
        String arguments = getToolConfig().getInStringParameter(ERROR, getSensorKey(), getSensorExecargs());
        for (String elem : arguments.split("\\s+")) {
            if(!elem.isEmpty()) {
                cmdArray.add(elem);
            }
        }
        cmdArray.add(file);
        return cmdArray;
    }
    
    protected boolean shouldExecute() {      
        if (getConfiguration().getprojectLanguage().equals(getSensorExeclanguage())) {
            return true;
        }
        return false;
    }
    
    protected String getSensorKey() {
        return sensorKey;
    }
    
    public void execTool(String fileToCheck) throws IOException, InterruptedException {              
    }
    
    public DataConfiguration getToolConfig()  {              
        return toolConfig;
    }    
    public String getSensorExecargs()  {              
        return SENSOR_EXEC_ARGS;
    }    
    public String getSensorExecoutput()  {              
        return SENSOR_EXEC_OUTPUT;
    }  
    public String getSensorExeclanguage()  {              
        return toolConfig.getInStringParameter("error", sensorKey, SENSOR_EXEC_LANGUAGE);
    } 
    public String getSensorExeccmd()  {              
        return SENSOR_EXEC_CMD;
    }     
    public CmdExecutor getExecutor()  {              
        return executor;
    }     
    public Configuration getConfiguration()  {              
        return configuration;
    }        
}
