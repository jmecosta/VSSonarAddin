/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tekla.vssonar.sensors;

import com.tekla.vssonar.configuration.Configuration;
import com.tekla.vssonar.utils.CmdExecutor;
import com.tekla.vssonar.utils.ProcessExecutor;
import com.tekla.vssonar.utils.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

/**
 *
 * @author jocs
 */
public class CpplintSensorTest  {
           
    String[] cmd  = {"python", Utils.loadResource("/cpplint.py").toString(), "--output=vs7", Utils.loadResource("/com/tekla/vssonar/code_chunks.cpp").toString()};
    List<String> lines = new ArrayList<String>();
    CmdExecutor exeMock;
    Configuration confMock;
    
    @Before public void Setup() {
        lines.add("code_chunks.cpp(10):  Lines should very rarely be longer than 140 characters  [whitespace/line_length-0] [4]");
        exeMock = mock(CmdExecutor.class);
        confMock = mock(Configuration.class);        
    }
    
    @Test public void TestNotExistFile() throws IOException, InterruptedException
    {
        lines.clear();        
        when(confMock.getUserConfigurationFile()).thenReturn("");
        
        ToolSensor NewSensor = new CpplintSensor(exeMock, confMock);                       
        when(exeMock.executeCmd(cmd, "stderr", null)).thenReturn(lines); 
        
        NewSensor.execTool(Utils.loadResource("/com/tekla/vssonar/code_chunks.cpp").toString());
        Assert.assertTrue( NewSensor.getViolations().isEmpty() );
    }
          
    @Test public void TestWithUserFile() throws IOException, InterruptedException
    {        
        String[] cmd1  = {"python", "cpplint_mod.py", Utils.loadResource("/com/tekla/vssonar/code_chunks.cpp").toString()};        
        when(confMock.getUserConfigurationFile()).thenReturn(Utils.loadResource("/com/tekla/vssonar/config.xml").toString());                
        ToolSensor NewSensor = new CpplintSensor(exeMock, confMock);                              
        when(exeMock.executeCmd(cmd1, "stderr", null)).thenReturn(lines);                
        NewSensor.execTool(Utils.loadResource("/com/tekla/vssonar/code_chunks.cpp").toString());
        Assert.assertTrue( NewSensor.getViolations().size() == 1 );
        Assert.assertTrue( NewSensor.getViolations().get(0).getMessage().equals("Lines should very rarely be longer than 140 characters"));
        Assert.assertTrue( NewSensor.getViolations().get(0).getResourceName().equals("code_chunks.cpp"));
        Assert.assertTrue( NewSensor.getViolations().get(0).getRuleKey().equals("whitespace/line_length-0"));
        Assert.assertTrue( NewSensor.getViolations().get(0).getLine() == 10);
        verify(exeMock, times(1)).executeCmd(cmd1, "stderr", null);
        
    }
        
    @Test public void TestCpplintReplacementString() throws IOException, InterruptedException
    {        
        List<String> linetest = new ArrayList<String>();
        linetest.add("example.cpp(181):  Function has been marked as deprecated, please find a suitable replacement. (function) - [rule] [1]");                
        linetest.add("ksalfkjakdjlsakjdlaksjd");
        String[] cmd1  = {"python", "cpplint_mod.py", Utils.loadResource("/com/tekla/vssonar/code_chunks.cpp").toString()};        
        
        when(confMock.getUserConfigurationFile()).thenReturn(Utils.loadResource("/com/tekla/vssonar/config.xml").toString());                
        ToolSensor NewSensor = new CpplintSensor(exeMock, confMock);                              
        when(exeMock.executeCmd(cmd1, "stderr", null)).thenReturn(linetest);                
        NewSensor.execTool(Utils.loadResource("/com/tekla/vssonar/code_chunks.cpp").toString());
        Assert.assertEquals(NewSensor.getViolations().size(), 1);
        Assert.assertEquals( NewSensor.getViolations().get(0).getMessage(), "Function has been marked as deprecated, please find a suitable replacement. (function) -");
        Assert.assertEquals( NewSensor.getViolations().get(0).getResourceName(), "example.cpp");
        Assert.assertEquals( NewSensor.getViolations().get(0).getRuleKey(), "rule");
        Assert.assertEquals( NewSensor.getViolations().get(0).getLine(), new Integer(181));                        
    }       
    
}
