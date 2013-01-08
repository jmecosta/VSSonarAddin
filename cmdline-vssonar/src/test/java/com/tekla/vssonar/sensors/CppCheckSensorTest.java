/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tekla.vssonar.sensors;

import com.tekla.vssonar.configuration.Configuration;
import com.tekla.vssonar.utils.CmdExecutor;
import com.tekla.vssonar.utils.ProcessExecutor;
import com.tekla.vssonar.utils.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author jocs
 */
public class CppCheckSensorTest {
    String[] cmd  = {"cppcheck.exe", "--inline-suppr", "--enable=all", "--xml", Utils.loadResource("/com/tekla/vssonar/code_chunks.cpp").toString()};    
    
    List<String> lines = new ArrayList<String>();
    CmdExecutor exeMock;
    Configuration confMock;        
    
    @Before public void Setup() {
        exeMock = mock(CmdExecutor.class);
        confMock = mock(Configuration.class);        
    }
    
    @Test public void TestXMLCppCheck() throws IOException, InterruptedException
    {        
        when(confMock.getUserConfigurationFile()).thenReturn("");
        when(confMock.getFile()).thenReturn("");

        when(exeMock.executeCmd(cmd, "stderr", null)).thenReturn(Helpers.readFileData(Utils.loadResource("/com/tekla/vssonar/cppcheck-result.xml")));
        ToolSensor NewSensor = new CppCheckSensor(exeMock, confMock);        
        NewSensor.execTool(Utils.loadResource("/com/tekla/vssonar/code_chunks.cpp").toString());
        
        Assert.assertEquals( NewSensor.getViolations().size(), 4);                   
        Assert.assertEquals( NewSensor.getViolations().get(0).getRuleKey(), "missingInclude");
        Assert.assertEquals( NewSensor.getViolations().get(0).getLine(), new Integer(4));
        Assert.assertEquals( NewSensor.getViolations().get(1).getRuleKey(), "missingInclude");
    }     
}
