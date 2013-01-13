/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tekla.vssonar;

import com.tekla.vssonar.configuration.PluginConfiguration;
import com.tekla.vssonar.sensors.Helpers;
import com.tekla.vssonar.sensors.SensorsManager;
import com.tekla.vssonar.utils.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.ParseException;
import org.junit.Assert;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Violation;

/**
 *
 * @author jocs
 */
public class SonarAnalysisTest {

    @Test
    public void testFailAutheticate() throws ParseException, IOException {
        String[] args = Helpers.CreateCliArgs();;

        PluginConfiguration config = new PluginConfiguration(args);
        ISonarQuery query = mock(ISonarQuery.class);
        when(query.queryProfile(config.getProjectKey())).thenThrow(new org.sonar.wsclient.connectors.ConnectionException("Authentication Failed"));
        try {
            SonarAnalysis analyse = new SonarAnalysis(query, config);
            Assert.fail("Should throw exception");
        } catch (org.sonar.wsclient.connectors.ConnectionException e) {
            Assert.assertEquals("Authentication Failed", e.getMessage());
        }
    }

    public void testFailProjectKey() throws ParseException, IOException {
        String[] args = Helpers.CreateCliArgs();;

        PluginConfiguration config = new PluginConfiguration(args);
        ISonarQuery query = mock(ISonarQuery.class);
        when(query.queryProfile(config.getProjectKey())).thenThrow(new org.sonar.wsclient.connectors.ConnectionException("Project Key is Incorrect"));
        try {
            SonarAnalysis analyse = new SonarAnalysis(query, config);
            Assert.fail("Should throw exception");
        } catch (org.sonar.wsclient.connectors.ConnectionException e) {
            Assert.assertEquals("Project Key is Incorrect", e.getMessage());
        }
    }

    @Test
    public void testReportSonar() throws ParseException, IOException {
        String[] args = Helpers.CreateCliArgs();

        PluginConfiguration config = new PluginConfiguration(args);
        ISonarQuery query = mock(ISonarQuery.class);
        when(query.queryProfile(config.getProjectKey())).thenReturn("Default");
        List<Violation> violations = new ArrayList<Violation>();
        Violation viol = new Violation();
        viol.setLine(100);
        viol.setSeverity("Major");
        viol.setResourceKey("test");
        viol.setMessage("message");
        violations.add(viol);
        when(query.queryResource(config.getResource())).thenReturn(true);
        when(query.queryViolations(config.getResource())).thenReturn(violations);
        SonarAnalysis analyse = new SonarAnalysis(query, config);        
        Integer violationcount = analyse.reportViolationsInSonar();
        Assert.assertEquals(new Integer(1), violationcount);
    }

    public void testReportSonarResourceNotFound() throws ParseException, IOException {
        String[] args = Helpers.CreateCliArgs();

        PluginConfiguration config = new PluginConfiguration(args);
        ISonarQuery query = mock(ISonarQuery.class);
        when(query.queryProfile(config.getProjectKey())).thenReturn("Default");
        when(query.queryResource(config.getResource())).thenReturn(false);
        SonarAnalysis analyse = new SonarAnalysis(query, config);        
        Integer violationcount = analyse.reportViolationsInSonar();
        Assert.assertEquals(new Integer(0), violationcount);
    }
    
    @Test
    public void testReportSonarNoDataInViolation() throws ParseException, IOException {
        String[] args = Helpers.CreateCliArgs();

        PluginConfiguration config = new PluginConfiguration(args);
        ISonarQuery query = mock(ISonarQuery.class);
        when(query.queryProfile(config.getProjectKey())).thenReturn("Default");
        List<Violation> violations = new ArrayList<Violation>();
        violations.add(new Violation());
        when(query.queryResource(config.getResource())).thenReturn(true);
        when(query.queryViolations(config.getResource())).thenReturn(violations);
        SonarAnalysis analyse = new SonarAnalysis(query, config);        
        Integer violationcount = analyse.reportViolationsInSonar();
        Assert.assertEquals(new Integer(1), violationcount);
    }

    @Test
    public void testReportLocalViolations() throws ParseException, IOException {
        String[] args = Helpers.CreateCliArgs();

        PluginConfiguration config = new PluginConfiguration(args);
        ISonarQuery query = mock(ISonarQuery.class);
        when(query.queryProfile(config.getProjectKey())).thenReturn("Default");
        List<Violation> violations = new ArrayList<Violation>();
        Violation viol = new Violation();
        viol.setLine(100);
        viol.setSeverity("Major");
        viol.setResourceKey("test");
        viol.setMessage("message");
        violations.add(viol);
        SonarAnalysis analyse = new SonarAnalysis(query, config);
        SensorsManager allSensors = mock(SensorsManager.class);
        when(allSensors.executeSensors(config.getLocalFilePath())).thenReturn(violations);
        Integer violationcount = analyse.reportLocalViolations(allSensors);
        Assert.assertEquals(new Integer(1), violationcount);
    }
    
    @Test
    public void testReportAddedLocalViolations() throws ParseException, IOException {
        String[] args = Helpers.CreateCliArgs();

        PluginConfiguration config = new PluginConfiguration(args);
        ISonarQuery query = mock(ISonarQuery.class);
        List<String> lines = new ArrayList<String>();
        lines.add("public class { kskjj }");
        lines.add("public class bb {}");
        lines.add("public class aa { dkjdsjk }");
        lines.add("public class cc {}");

        when(query.querySource(config.getResource())).thenReturn(lines);
        when(query.queryProfile(config.getProjectKey())).thenReturn("Default");
        when(query.queryIsRuleEnable("cs", "key", "Default")).thenReturn(true);
        List<Violation> violations = new ArrayList<Violation>();
        Violation viol = new Violation();
        viol.setLine(2);
        viol.setSeverity("Major");
        viol.setResourceKey("test");
        viol.setRuleKey("key");
        viol.setMessage("message");
        violations.add(viol);
        SonarAnalysis analyse = new SonarAnalysis(query, config);
        SensorsManager allSensors = mock(SensorsManager.class);
        when(allSensors.executeSensors(config.getLocalFilePath())).thenReturn(violations);       
        Integer violationcount = analyse.reportAddedLocalViolations(allSensors);
        Assert.assertEquals(new Integer(1), violationcount);
    }
    
    @Test
    public void testReportCoverage() throws ParseException, IOException {
        String[] args = Helpers.CreateCliArgs();

        PluginConfiguration config = new PluginConfiguration(args);
        ISonarQuery query = mock(ISonarQuery.class);
        List<String> lines = new ArrayList<String>();        
        lines.add("100");
        List<String> lines2 = new ArrayList<String>();
        lines2.add("100=2");        

        when(query.queryProfile(config.getProjectKey())).thenReturn("Default");
        when(query.queryNotCoveredLineHitsCoverage(config.getResource())).thenReturn(lines); 
        when(query.queryNotCoveredConditionsHitsCoverage(config.getResource())).thenReturn(lines2);        
        
        SonarAnalysis analyse = new SonarAnalysis(query, config);
        Integer violationcount = analyse.reportCoverage();
        Assert.assertEquals(new Integer(2), violationcount);
    }
    
    @Test
    public void testReportChangeLinesLocalViolations() throws ParseException, IOException {
        String[] args = Helpers.CreateCliArgs();

        PluginConfiguration config = new PluginConfiguration(args);
        ISonarQuery query = mock(ISonarQuery.class);
        List<String> lines = new ArrayList<String>();
        lines.add("public class { kskjj }");
        lines.add("public class bb {}");
        lines.add("public class aa { dkjdsjk }");
        lines.add("public class cc {}");

        when(query.querySource(config.getResource())).thenReturn(lines);
        when(query.queryProfile(config.getProjectKey())).thenReturn("Default");
        when(query.queryIsRuleEnable("cs", "key", "Default")).thenReturn(true);
        List<Violation> violations = new ArrayList<Violation>();
        Violation viol = new Violation();
        viol.setLine(2);
        viol.setSeverity("Major");
        viol.setResourceKey("test");
        viol.setRuleKey("key");
        viol.setMessage("message");
        violations.add(viol);
        SonarAnalysis analyse = new SonarAnalysis(query, config);
        Integer violationcount = analyse.reportChangedLines();
        Assert.assertEquals(new Integer(4), violationcount);
    }  
    
    //@Test
    public void testSonarRealConnection() throws ParseException, IOException {
        String[] args = {"-solution_path", "E:\\z_misctools\\SonarSupportTools\\VSSonarPlugin\\VSAddin",
            "-cmd", "report_sonar",
            "-username", "admin",
            "-password", "admin",
            
            "-sonarurl", "http://sonar:80",
                "-file_path", "E:\\z_misctools\\SonarSupportTools\\VSSonarPlugin\\VSAddin\\VSSonarPlugin\\Connect.cs"};
    

            PluginConfiguration config = new PluginConfiguration(args);
            Sonar sonar = Sonar.create(config.getSonarUrl(), config.getUserName(), config.getPassword());
            ISonarQuery query = new SonarQuery(sonar);
        
        SonarAnalysis analyse = new SonarAnalysis(query, config);        
        Integer violationcount = analyse.reportViolationsInSonar();
        Assert.assertEquals(new Integer(75), violationcount);
    }    
    
}
