/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tekla.vssonar.sensors;

import com.tekla.vssonar.configuration.Configuration;
import com.tekla.vssonar.utils.CmdExecutor;
import com.tekla.vssonar.utils.Utils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.utils.StaxParser;
import org.sonar.wsclient.services.Violation;

/**
 *
 * @author jocs
 */
public class CppCheckSensor extends ToolSensor {

    static final String KEY = "cppcheck";
    static final String CONFIGFILE = "/cppcheck.xml";

    public CppCheckSensor(CmdExecutor executor, Configuration pluginconfig) {
        super(KEY, CONFIGFILE, executor, pluginconfig);
    }

    @Override
    public void execTool(String file) {
        List<String> cmdArray = packBasicExecutorCmd(file);

        String[] args = cmdArray.toArray(new String[cmdArray.size()]);
        String output = getToolConfig().getInStringParameter(ERROR, KEY, getSensorExecoutput());
        List<String> toolOutput = getExecutor().executeCmd(args, output, null);

        if (!toolOutput.isEmpty()) {

            File xml = Utils.createTmpFile(toolOutput);
            try {
                processReport(xml);
            } catch (XMLStreamException ex) {
                Logger.getLogger(CppCheckSensor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    protected void processReport(File report)
            throws javax.xml.stream.XMLStreamException {

        StaxParser parser = new StaxParser(new StaxParser.XmlStreamHandler() {

            /**
             * {@inheritDoc}
             */
            public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {
                rootCursor.advance();

                SMInputCursor errorCursor = rootCursor.childElementCursor(ERROR);
                while (errorCursor.getNext() != null) {
                    String file = errorCursor.getAttrValue("file");
                    // get file name                             
                    if (file != null) {

                        String line = errorCursor.getAttrValue("line");
                        String id = errorCursor.getAttrValue("id");
                        String msg = errorCursor.getAttrValue("msg");
                        Violation viol = new Violation();
                        viol.setLine(Integer.parseInt(line));
                        viol.setMessage(msg);
                        viol.setResourceName(file);
                        viol.setRuleKey(id);
                        viol.setRuleName(KEY);
                        getViolations().add(viol);

                    }

                }
            }
        });

        parser.parse(report);
    }
}
