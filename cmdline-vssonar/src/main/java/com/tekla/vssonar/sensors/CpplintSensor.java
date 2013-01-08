/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tekla.vssonar.sensors;

import com.tekla.vssonar.configuration.Configuration;
import com.tekla.vssonar.utils.CmdExecutor;
import com.tekla.vssonar.utils.Utils;
import java.util.ArrayList;
import java.util.List;
import org.sonar.wsclient.services.Violation;

/**
 *
 * @author jocs
 */
public class CpplintSensor extends ToolSensor {

    static final String KEY = "cpplint";
    static final String CONFIGFILE = "/cpplint.xml";

    public CpplintSensor(CmdExecutor executor, Configuration pluginconfig) {
        super(KEY, CONFIGFILE, executor, pluginconfig);
    }

    @Override
    public void execTool(String file) {

        List<String> cmdArray = new ArrayList<String>();
        cmdArray.add(getToolConfig().getInStringParameter("error", KEY, getSensorExeccmd()));
        String arguments = getToolConfig().getInStringParameter("error", KEY, getSensorExecargs());


        for (String elem : arguments.split("\\s+")) {
            if (elem.contains("cpplint.py") && getConfiguration().getUserConfigurationFile().equals("")) {
                cmdArray.add(Utils.loadResourceAsStream("/cpplint.py").toString());
            } else {
                cmdArray.add(elem);
            }
        }


        cmdArray.add(file);

        String[] args = cmdArray.toArray(new String[cmdArray.size()]);
        List<String> toolOutput = getExecutor().executeCmd(args, getToolConfig().getInStringParameter("error", KEY, getSensorExecoutput()), null);
        getViolations().addAll(parseOutputForViolations(toolOutput));
    }

    //file(line):  message [id] [notneeded]
    private List<Violation> parseOutputForViolations(List<String> toolOutput) {
        List<Violation> currviolations = new ArrayList<Violation>();
            
        for (String str : toolOutput) {
            Violation violation = new Violation();
            String data = "";
            boolean filenameready = false;
            boolean lineready = true;
            boolean messageready = true;
            boolean rulekeyready = true;
            
            for (int i = 0; i < str.length(); i++){
                if (str.charAt(i) == '(' && !filenameready){
                    filenameready = true;
                    lineready = false;
                    violation.setResourceName(data);
                    data = "";
                    ++i;
                }                
                if (str.charAt(i) == ')' && !lineready){
                    lineready = true;
                    messageready = false;
                    violation.setLine(Integer.parseInt(data));
                    data = "";
                    i+=2;
                }                  
                if (str.charAt(i) == '[' && !messageready){
                    messageready = true;
                    rulekeyready = false;
                    violation.setMessage(data.trim());
                    data = "";
                    ++i;
                }                                 
                if (str.charAt(i) == ']' && !rulekeyready){
                    rulekeyready = true;
                    violation.setRuleKey(data);
                    data = "";
                    ++i;
                }                
                data += str.charAt(i);
            }
            violation.setRuleName(KEY);
            
            if(violation.getResourceName() != null && violation.getMessage() != null){
                currviolations.add(violation);                       
            }
        }
        return currviolations;
    }
}
