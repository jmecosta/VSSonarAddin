/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tekla.vssonar.sensors;

import com.tekla.vssonar.configuration.Configuration;
import com.tekla.vssonar.configuration.PluginConfiguration;
import com.tekla.vssonar.utils.CmdExecutor;
import com.tekla.vssonar.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

/**
 *
 * @author jocs
 */
public class VeraSensorTest  {
           
    List<String> lines = new ArrayList<String>();
    CmdExecutor exeMock;
    Configuration confMock;
    
    @Before public void Setup() {
        lines.add("e:/code_chunks.cpp(10):  Lines should very rarely be longer than 140 characters  [whitespace/line_length-0] [4]");
        exeMock = mock(CmdExecutor.class);
        confMock = mock(Configuration.class);        
    }
    
    @Test public void TestLoadExecutableFromXmlReplaceArgs() throws IOException, InterruptedException, ParseException
    {        
        String[] cmd  = {"C:\\vera++\\vera++.exe", "-arg2", "-arg1", Utils.loadResource("/com/tekla/vssonar/code_chunks.cpp").toString()};
        when(confMock.getUserConfigurationFile()).thenReturn(Utils.loadResource("/vssonar.xml").toString());
        when(confMock.getFile()).thenReturn("");
        when(exeMock.executeCmd(cmd, "stderr", null)).thenReturn(Helpers.readFileData(Utils.loadResource("/com/tekla/vssonar/rats-result.xml")));
        ToolSensor NewSensor = new VeraSensor(exeMock, confMock);          
        NewSensor.execTool(Utils.loadResource("/com/tekla/vssonar/code_chunks.cpp").toString());
        String[] env = {"VERA_ROOT=C:\\vera++"};
        verify(exeMock, times(1)).executeCmd(cmd, "stderr", env);
    }
    
    @Test public void TestVeraRelativeOutput() throws IOException, InterruptedException
    {        
        List<String> linetest = new ArrayList<String>();
        linetest.add("example.cpp:21: (T002) reserved name used for macro (incorrect use of underscore)");                
        linetest.add("ksalfkjakdjlsakjdlaksjd");
        String[] cmd  = {"vera++.exe", "-nodup", "-showrules", Utils.loadResource("/com/tekla/vssonar/code_chunks.cpp").toString()};        

        // set additional environment variables for vera
        File tmp = new File("vera++.exe");
        String[] env = {"VERA_ROOT=" + tmp.getParent()};
        
        when(confMock.getUserConfigurationFile()).thenReturn(Utils.loadResource("/com/tekla/vssonar/config.xml").toString());                
        ToolSensor NewSensor = new VeraSensor(exeMock, confMock);                              
        when(exeMock.executeCmd(cmd, "stderr", env)).thenReturn(linetest);                
        NewSensor.execTool(Utils.loadResource("/com/tekla/vssonar/code_chunks.cpp").toString());
        Assert.assertTrue( NewSensor.getViolations().size() == 1 );
        Assert.assertTrue( NewSensor.getViolations().get(0).getMessage().equals("reserved name used for macro (incorrect use of underscore)"));
        Assert.assertTrue( NewSensor.getViolations().get(0).getResourceName().equals("example.cpp"));
        Assert.assertTrue( NewSensor.getViolations().get(0).getRuleKey().equals("T002"));
        Assert.assertTrue( NewSensor.getViolations().get(0).getLine() == 21);                        
    }       
    
    @Test public void TestVeraAbsoluteOutput() throws IOException, InterruptedException
    {        
        List<String> linetest = new ArrayList<String>();
        linetest.add("c:\\example.cpp:21: (T002) reserved name used for macro (incorrect use of underscore)");                
        linetest.add("ksalfkjakdjlsakjdlaksjd");
        String[] cmd  = {"vera++.exe", "-nodup", "-showrules", Utils.loadResource("/com/tekla/vssonar/code_chunks.cpp").toString()};
        
        // set additional environment variables for vera
        File tmp = new File("vera++.exe");
        String[] env = {"VERA_ROOT=" + tmp.getParent()};
       
        
        when(confMock.getUserConfigurationFile()).thenReturn(Utils.loadResource("/com/tekla/vssonar/config.xml").toString());                
        ToolSensor NewSensor = new VeraSensor(exeMock, confMock);                              
        when(exeMock.executeCmd(cmd, "stderr", env)).thenReturn(linetest);                
        NewSensor.execTool(Utils.loadResource("/com/tekla/vssonar/code_chunks.cpp").toString());
        Assert.assertTrue( NewSensor.getViolations().size() == 1 );
        Assert.assertTrue( NewSensor.getViolations().get(0).getMessage().equals("reserved name used for macro (incorrect use of underscore)"));
        Assert.assertTrue( NewSensor.getViolations().get(0).getResourceName().equals("c:\\example.cpp"));
        Assert.assertTrue( NewSensor.getViolations().get(0).getRuleKey().equals("T002"));
        Assert.assertTrue( NewSensor.getViolations().get(0).getLine() == 21);                        
    }     
}
