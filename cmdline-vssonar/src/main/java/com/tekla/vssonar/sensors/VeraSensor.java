/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tekla.vssonar.sensors;

import com.tekla.vssonar.configuration.Configuration;
import com.tekla.vssonar.utils.CmdExecutor;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.sonar.wsclient.services.Violation;

/**
 *
 * @author jocs
 */
public class VeraSensor extends ToolSensor {

    static final String KEY = "vera";
    static final String KEYSONAR = "vera++";
    static final String CONFIGFILE = "/vera++.xml";

    public VeraSensor(CmdExecutor executor, Configuration pluginconfig) {
        super(KEY, CONFIGFILE, executor, pluginconfig);
    }

    @Override
    public void execTool(String file) {
        List<String> cmdArray = packBasicExecutorCmd(file);
        String[] args = cmdArray.toArray(new String[cmdArray.size()]);
        
        // set additional environment variables for vera
        File tmp = new File(args[0]);
        String[] env = {"VERA_ROOT=" + tmp.getParent()};
        List<String> toolOutput = getExecutor().executeCmd(args, getToolConfig().getInStringParameter("error", KEY, getSensorExecoutput()), env);

        if (toolOutput.size() > 0) {
            getViolations().addAll(parseOutputForViolations(toolOutput));
        }
    }

    //file(line):  message [id] [notneeded]
    private List<Violation> parseOutputForViolations(List<String> toolOutput) {
        List<Violation> currviolations = new ArrayList<Violation>();


        for (String str : toolOutput) {
            String[] data = str.split(":");            
            // fix windows paths
            if(data.length == 4) {
                data[0] = data[0] + ":" + data[1];
                data[1] = data[2];
                data[2] = data[3];            
            }
            if (data.length > 2) {
                Violation violation = new Violation();
                violation.setResourceName(data[0]);
                violation.setLine(Integer.parseInt(data[1]));

                boolean rulekeyready = true;
                boolean startrule = false;
                String lastdata = "";
                String compdata = data[2];
                for (int i = 0; i < compdata.length(); i++) {
                    if (compdata.charAt(i) == '(' && !startrule) {
                        startrule = true;
                        rulekeyready = false;
                        lastdata = "";
                        ++i;
                    }                    
                    if (compdata.charAt(i) == ')' && !rulekeyready) {
                        rulekeyready = true;
                        violation.setRuleKey(lastdata);
                        lastdata = "";
                        ++i;
                    }
                    lastdata += compdata.charAt(i);
                }
                violation.setMessage(lastdata.trim());
                violation.setRuleName(KEYSONAR);

                if (violation.getResourceName() != null && violation.getMessage() != null) {
                    currviolations.add(violation);
                }
            }
        }

        return currviolations;
    }
}
