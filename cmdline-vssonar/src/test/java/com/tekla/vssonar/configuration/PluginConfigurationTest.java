/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tekla.vssonar.configuration;

import com.tekla.vssonar.utils.Utils;
import java.io.File;
import java.io.IOException;
import org.apache.commons.cli.ParseException;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jocs
 */
public class PluginConfigurationTest {
        
    @Test
    public void testParseException() throws IOException {
        String [] args = {"", "", "", "", ""};
        try
        {
            PluginConfiguration config = new PluginConfiguration(args);
            Assert.fail("Did not thorw exception");
        } catch(org.apache.commons.cli.ParseException ex){
            Assert.assertEquals(ex.getMessage(), "<command not defined><solution path not defined><password not defined><user not defined><sonar url not defined><project key not defined><project language not defined>");
        }
    }
    
    @Test
    public void testNOkNoSonarServer() throws IOException {
        String [] args = {"-solution_path", " c:\\src", "-cmd", "auth_sonar"};
        try
        {
            PluginConfiguration config = new PluginConfiguration(args);
            Assert.fail("Parse Was Ok, Should Fail");
        } catch(org.apache.commons.cli.ParseException ex){
            Assert.assertEquals(ex.getMessage(), "<password not defined><user not defined><sonar url not defined><project key not defined><project language not defined>");
        }
    }
    @Test
    public void testNOkNoCmdServer() throws IOException {
        String [] args = {"-solution_path", " c:\\src"};
        try
        {
            PluginConfiguration config = new PluginConfiguration(args);
            Assert.fail("Parse Was Ok, Should Fail");
        } catch(org.apache.commons.cli.ParseException ex){
            Assert.assertEquals(ex.getMessage(), "<command not defined><password not defined><user not defined><sonar url not defined><project key not defined><project language not defined>");
        }
    }    
    
    @Test
    public void testWithPomFileOnlyAuthentication() throws ParseException, IOException {
        File pom = Utils.loadResource("/pom.xml");
        String solpath= pom.getParent().toString();
        String [] args = {"-solution_path", solpath, "-cmd", "auth_sonar", "-username", "test", "-password", "test"};

        PluginConfiguration config = new PluginConfiguration(args);
        Assert.assertEquals("", config.getFile());
        Assert.assertEquals("auth_sonar", config.getCommand());
    }    

    @Test
    public void testWithPomRelativePathFile() throws ParseException, IOException {
        File pom = Utils.loadResource("/pom.xml");
        String solpath= pom.getParent().toString();
        String [] args = {"-solution_path", solpath, 
            "-cmd", "report_sonar", 
            "-username", "test", 
            "-password", "test", 
            "-file_path", "src\\debug.cs"};
        
        PluginConfiguration config = new PluginConfiguration(args);
        Assert.assertEquals("src/debug.cs", config.getFile());
    } 

    @Test
    public void testWithPomAbsolutePathFile() throws ParseException, IOException {
        File pom = Utils.loadResource("/pom.xml");
        String solpath= pom.getParent().toString();
        String [] args = {"-solution_path", solpath, 
            "-cmd", "report_sonar", 
            "-username", "test", 
            "-password", "test", 
            "-file_path", solpath + "\\src\\debug.cs"};
        
        PluginConfiguration config = new PluginConfiguration(args);
        Assert.assertEquals("src/debug.cs", config.getFile());
    }    
    
    @Test
    public void testOverwritePomFile() throws ParseException, IOException {
        File pom = Utils.loadResource("/pom.xml");
        String solpath = pom.getParent().toString();
        String [] args = {"-solution_path", solpath, 
            "-cmd", "report_sonar", 
            "-username", "test", 
            "-password", "test",
            "-projectKey", "projectKey",
            "-sonarurl", "sonarurl",
            "-user_config", "user_config",
            "-language", "language",
            "-file_path", solpath + "\\src\\debug.cs"};
        
        PluginConfiguration config = new PluginConfiguration(args);

        Assert.assertEquals("src/debug.cs", config.getFile());
        Assert.assertEquals("projectKey", config.getProjectKey());
        Assert.assertEquals("test", config.getPassword());
        Assert.assertEquals("test", config.getUserName());
        Assert.assertEquals("language", config.getprojectLanguage());
        Assert.assertEquals("sonarurl", config.getSonarUrl());
        Assert.assertEquals("user_config", config.getUserConfigurationFile());
        Assert.assertEquals("report_sonar", config.getCommand());
        Assert.assertEquals(solpath + "\\pom.xml", config.getSonarConfigPath());
        
    }    
    
    @Test
    public void testWithPomFileOnlyAuthenticationFails() throws IOException {
        File pom = Utils.loadResource("/pom.xml");
        String solpath= pom.getParent().toString();
        String [] args = {"-solution_path", solpath, "-cmd", "report_sonar", "-username", "test", "-password", "test"};

        try {
            PluginConfiguration config = new PluginConfiguration(args);
            Assert.fail("Should Throw Exception");
        } catch (ParseException ex) {
            Assert.assertEquals("<no file to check>", ex.getMessage());
        }
    }    

    @Test
    public void testWithPomFileFailsNoFileToCheck() throws IOException {
        File pom = Utils.loadResource("/pom.xml");
        String solpath= pom.getParent().toString();
        String [] args = {"-solution_path", solpath, "-cmd", "auth_sonar"};

        try {
            PluginConfiguration config = new PluginConfiguration(args);
        } catch (ParseException ex) {
            Assert.assertEquals("<password not defined><user not defined>", ex.getMessage());
        }
    }    
    
    @Test
    public void testWithSonarRunnerAbsolutePathFile() throws ParseException, IOException {
        File pom = Utils.loadResource("/sonar-runner/sonar-runner.properties");
        String solpath= pom.getParent().toString();
        String [] args = {"-solution_path", solpath, 
            "-cmd", "report_sonar", 
            "-username", "test", 
            "-password", "test", 
            "-file_path", solpath + "\\src\\debug.cs"};
        
        PluginConfiguration config = new PluginConfiguration(args);
        Assert.assertEquals("src/debug.cs", config.getFile());
        Assert.assertEquals("java-sonar-runner-simple", config.getProjectKey());
        Assert.assertEquals("test", config.getPassword());
        Assert.assertEquals("test", config.getUserName());
        Assert.assertEquals("cs", config.getprojectLanguage());
        Assert.assertEquals("http://localhost:9000", config.getSonarUrl());
        Assert.assertEquals("report_sonar", config.getCommand());        
    }    
    
}
