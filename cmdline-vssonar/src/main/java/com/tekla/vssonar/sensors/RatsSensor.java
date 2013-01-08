/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tekla.vssonar.sensors;

import com.tekla.vssonar.configuration.Configuration;
import com.tekla.vssonar.utils.CmdExecutor;
import com.tekla.vssonar.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.sonar.wsclient.services.Violation;

/**
 *
 * @author jocs
 */
public class RatsSensor extends ToolSensor {

    static final String KEY = "rats";
    static final String CONFIGFILE = "/rats.xml";
    private static final String MISSING_RATS_TYPE = "fixed size global buffer";

    public RatsSensor(CmdExecutor executor, Configuration pluginconfig) {
        super(KEY, CONFIGFILE, executor, pluginconfig);
    }

    @Override
    public void execTool(String file) {
        List<String> cmdArray = packBasicExecutorCmd(file);
        String[] args = cmdArray.toArray(new String[cmdArray.size()]);
        
        String output = getToolConfig().getInStringParameter("error", KEY, getSensorExecoutput());        
        List<String> toolOutput = getExecutor().executeCmd(args, output, null);
        if (!toolOutput.isEmpty()) {
            // write output lines to tmpfile
            File xml = Utils.createTmpFile(toolOutput);
                try {
                    // parse xml
                    processReport(xml);
                } catch (JDOMException ex) {
                    Logger.getLogger(RatsSensor.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(RatsSensor.class.getName()).log(Level.SEVERE, null, ex);
                }                 
        }
    }

    protected void processReport(File report)
            throws org.jdom.JDOMException, java.io.IOException {
        SAXBuilder builder = new SAXBuilder(false);
        Element root = builder.build(report).getRootElement();

        List<Element> vulnerabilities = root.getChildren("vulnerability");
        for (Element vulnerability : vulnerabilities) {
            String type = getVulnerabilityType(vulnerability.getChild("type"));
            String message = vulnerability.getChild("message").getTextTrim();

            List<Element> files = vulnerability.getChildren("file");

            for (Element file : files) {
                String fileName = file.getChild("name").getTextTrim();

                List<Element> lines = file.getChildren("line");
                for (Element lineElem : lines) {
                    int line = Integer.parseInt(lineElem.getTextTrim());
                    Violation viol = new Violation();
                    viol.setLine(line);
                    viol.setMessage(message);
                    viol.setResourceName(fileName);
                    viol.setRuleKey(type);
                    viol.setRuleName(KEY);
                    getViolations().add(viol);
                }
            }
        }
    }

    private String getVulnerabilityType(Element child) {
        if (child != null) {
            return child.getTextTrim();
        }
        return MISSING_RATS_TYPE;
    }
}
