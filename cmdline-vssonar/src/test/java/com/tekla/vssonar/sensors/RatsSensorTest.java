/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tekla.vssonar.sensors;

import com.tekla.vssonar.configuration.Configuration;
import com.tekla.vssonar.configuration.PluginConfiguration;
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
import org.apache.commons.cli.ParseException;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

/**
 *
 * @author jocs
 */
public class RatsSensorTest {
    String[] cmd  = {"rats.exe", Utils.loadResource("/com/tekla/vssonar/code_chunks.cpp").toString()};    
    
    List<String> lines = new ArrayList<String>();
    CmdExecutor exeMock;
    Configuration confMock;
       
    @Before public void Setup() {
        exeMock = mock(CmdExecutor.class);
        confMock = mock(Configuration.class);        
    }
    
    @Test public void TestLoadExecutableFromXmlIgnoreArgs() throws IOException, InterruptedException, ParseException
    {            
        cmd[0] = "C:\\rats-2.3\\rats.exe";
        when(confMock.getUserConfigurationFile()).thenReturn(Utils.loadResource("/vssonar.xml").toString());
        when(confMock.getFile()).thenReturn("");
        when(exeMock.executeCmd(cmd, "stdout", null)).thenReturn(Helpers.readFileData(Utils.loadResource("/com/tekla/vssonar/rats-result.xml")));
        ToolSensor NewSensor = new RatsSensor(exeMock, confMock);          
        NewSensor.execTool(Utils.loadResource("/com/tekla/vssonar/code_chunks.cpp").toString());
        verify(exeMock, times(1)).executeCmd(cmd, "stdout", null);
    } 
    
    @Test public void TestXMLRats() throws IOException, InterruptedException
    {        
        when(confMock.getUserConfigurationFile()).thenReturn("");
        when(confMock.getFile()).thenReturn("");

        when(exeMock.executeCmd(cmd, "stdout", null)).thenReturn(Helpers.readFileData(Utils.loadResource("/com/tekla/vssonar/rats-result.xml")));
        ToolSensor NewSensor = new RatsSensor(exeMock, confMock);        
        NewSensor.execTool(Utils.loadResource("/com/tekla/vssonar/code_chunks.cpp").toString());
        verify(exeMock, times(1)).executeCmd(cmd, "stdout", null);
        Assert.assertEquals( NewSensor.getViolations().size(), 4);                   
        Assert.assertEquals( NewSensor.getViolations().get(0).getRuleKey(), "getenv");
        Assert.assertEquals( NewSensor.getViolations().get(0).getLine(), new Integer(38));
        Assert.assertEquals( NewSensor.getViolations().get(1).getRuleKey(), "fixed size global buffer");
    }     
}
