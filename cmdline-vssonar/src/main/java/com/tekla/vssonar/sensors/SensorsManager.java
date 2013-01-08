/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tekla.vssonar.sensors;

import com.tekla.vssonar.configuration.Configuration;
import com.tekla.vssonar.utils.CmdExecutor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sonar.wsclient.services.Violation;

/**
 *
 * @author jocs
 */
public class SensorsManager {
    private List<ToolSensor> l;
    
    public SensorsManager(CmdExecutor executor, Configuration pluginconfig) {
        createExtensions(executor, pluginconfig);    
    }
    
    public List<Violation> executeSensors(String fileToCheck) {
        List<Violation> violations =  new ArrayList<Violation>();
        for(ToolSensor sensor : l) {

                try {
                    sensor.execTool(fileToCheck);
                    violations.addAll(sensor.getViolations());
                } catch (IOException ex) {
                    Logger.getLogger(SensorsManager.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(SensorsManager.class.getName()).log(Level.SEVERE, null, ex);
                }
        }                
        return violations;
    }    
        
  /**
   * {@inheritDoc}
   */
  private void createExtensions(CmdExecutor executor, Configuration pluginconfig) {
    l = new ArrayList<ToolSensor>();
    addANewSensor(l, new CpplintSensor(executor, pluginconfig));
    addANewSensor(l, new CppCheckSensor(executor, pluginconfig));
    addANewSensor(l, new VeraSensor(executor, pluginconfig));
    addANewSensor(l, new RatsSensor(executor, pluginconfig));
  }
  
  
  /**
   * {@inheritDoc}
   */
  private  void addANewSensor(List<ToolSensor> l, ToolSensor sensor) {
      if(sensor.shouldExecute()) {
        l.add(sensor);       
      }
  }  
  

  
}
