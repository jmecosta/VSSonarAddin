/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tekla.vssonar.configuration;

import com.tekla.vssonar.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
/**
 *
 * @author jocs
 */
public class PluginConfiguration extends Configuration {
    public static final String SONARHOSTURL_KEY = "sonar.host.url";
    public static final String SONARJDBCPASSWORD_KEY = "sonar.jdbc.password";
    public static final String SONARJDBCURL_KEY = "sonar.jdbc.url";
    public static final String SONARJDBCUSERNAME_KEY = "sonar.jdbc.username";
    public static final String SONARLANGUAGE_KEY = "sonar.language";
    public static final String SONARSCMENABLED_KEY = "sonar.scm.enabled";
    public static final String SONARSCMURL_KEY = "sonar.scm.url";    
    public static final String PROPERTIESKEY = "properties";    
    public static final String PROJECT_KEY = "projectKey";
    public static final String ARTIFACT_ID_KEY = "artifactId";
    public static final String GROUP_ID_KEY = "groupId";
    public static final String USER_NAME_KEY = "username";
    public static final String USER_PASSWORD_KEY = "password";
    public static final String SONAR_HOSTURL_KEY = "sonarurl";
    public static final String SONAR_LANGUAGE_KEY = "language";
    
    public static final String FILE_PATH = "file_path";
    public static final String SONAR_POM_PATH = "pom_path";
    public static final String SONAR_RUNNER_PATH = "sonar_runner_path";
    public static final String SOLUTION_PATH = "solution_path";    
    public static final String USER_CONFIG = "user_config";    
    public static final String POMXML = "pom.xml";    
    public static final String SONARRUNNERFILE = "sonar-runner.properties";        

    public static final String CLI_CMD = "cmd";
    public static final String REPORT_ALL_LOCAL = "report_all_local";
    public static final String REPORT_COVERAGE = "report_coverage";
    public static final String REPORT_LOCAL = "report_local";
    public static final String REPORT_SONAR = "report_sonar";
    public static final String AUTH_SONAR = "auth_sonar";
    public static final String REPORT_SOURCE = "report_source";

    private Options options = new Options();
    private CommandLineParser parser = new PosixParser();
    private HelpFormatter formatter = new HelpFormatter();
    private String file = "";
    private String localFilePath = "";
    private String solution = "";
    private String sonarConfigPath = "";
    private String projectKey = "";
    private String groupId = "";    
    private String sonarUrl = "";
    private String projectLanguage = "";
    private String jdbcUrl = "";
    private String jdbcUserName = "";
    private String jdbcUserPassword = "";
    private boolean scmEnabled = false;
    private String scmUrl = "";
    private DataConfiguration pluginConfig = new DataConfiguration();
    private String userconfigurationfile = "";
    private String username = "";
    private String password = "";
    
    private static CommandLine cmd;
    private String command = "";

    public String getResource() {
        String resource = "";
        if(getprojectLanguage().equals("cs")){
            String[] filesplit = getFile().split("/");
            resource = getGroupId() + ":" + filesplit[0] + ":" + getFile().replace(filesplit[0] + "/", "");            
        } else {
            resource = getProjectKey() + ":" + getFile();
        }
        
        return resource;
    }
    
    public String getSonarConfigPath() {
        return sonarConfigPath;
    }    
    
    public String getCommand() {
        return command;
    }
    
    public String getUserName() {
        return username;
    }
    public String getPassword() {
        return password;
    }

    public String getUserConfigurationFile() {
        return userconfigurationfile;
    }
    
    public String getProjectKey() {
        return projectKey;
    }

    public String getGroupId() {
        if(groupId.isEmpty())
        {
            return projectKey.split(":")[0];
        }
        return groupId;
    }    

    public String getSonarUrl() {
        return sonarUrl;
    }

    public String getprojectLanguage() {
        return projectLanguage;
    }

    public String getjdbcUrl() {
        return jdbcUrl;
    }

    public String getjdbcUserName() {
        return jdbcUserName;
    }

    public String getjdbcUserPassword() {
        return jdbcUserPassword;
    }

    public boolean getscmEnabled() {
        return scmEnabled;
    }

    public String getscmUrl() {
        return scmUrl;
    }

    public PluginConfiguration(String[] args) throws ParseException, IOException {
        setupOptions();
        cmd = parser.parse(options, args);
        setupPluginConfiguration();
        checkConfiguration();
        printConfiguration();
    }

    private void setupPluginConfiguration() throws IOException {
        if (cmd.hasOption(CLI_CMD)) {
            command = cmd.getOptionValue(CLI_CMD);
        }                
        if (cmd.hasOption(SOLUTION_PATH)) {
            solution = cmd.getOptionValue(SOLUTION_PATH).trim();
            // make sure there is no \ in the end
            while (solution.endsWith("\\")) {
                solution = solution.substring(0, solution.length() - 1);
            }
        }        
        if (cmd.hasOption(SONAR_POM_PATH) || cmd.hasOption(SONAR_RUNNER_PATH)) {
            if(cmd.hasOption(SONAR_POM_PATH)) {
                sonarConfigPath = cmd.getOptionValue(SONAR_POM_PATH);
                createPomConfiguration(sonarConfigPath);
            } else {
                sonarConfigPath = cmd.getOptionValue(SONAR_RUNNER_PATH);
                createSonarRunnerConfiguration(sonarConfigPath);                
            }
        } else {
            // try to go into the solution to get properties from 
            // sonar runner or pom file
            String confPath = cmd.getOptionValue(SOLUTION_PATH) +  "\\" + POMXML;
            File conf = new File(cmd.getOptionValue(SOLUTION_PATH) +  "\\" + POMXML);
            if(conf.exists()) {
                sonarConfigPath = confPath;
                createPomConfiguration(sonarConfigPath);
            }
            confPath = cmd.getOptionValue(SOLUTION_PATH) +  "\\" + SONARRUNNERFILE;
            conf = new File(confPath);
            if(conf.exists()) {
                sonarConfigPath = confPath;
                createSonarRunnerConfiguration(sonarConfigPath);
            }
        }
        if (cmd.hasOption(FILE_PATH) && cmd.getOptionValue(FILE_PATH) != null) {   
            File tmp = new File(cmd.getOptionValue(FILE_PATH));
            if(tmp.isAbsolute()) {
                // get index of substring
                file = cmd.getOptionValue(FILE_PATH).substring(solution.length()+1).replace("\\", "/");
                localFilePath = cmd.getOptionValue(FILE_PATH);                                    
            } else {
                file = cmd.getOptionValue(FILE_PATH).replace("\\", "/");
                localFilePath = solution + "/" + cmd.getOptionValue(FILE_PATH);                                                
            }
        }                
        if (cmd.hasOption(USER_CONFIG)) {
            userconfigurationfile = cmd.getOptionValue(USER_CONFIG);
        }
        if (cmd.hasOption(PROJECT_KEY)) {
            projectKey = cmd.getOptionValue(PROJECT_KEY);
        }        
        if (cmd.hasOption(SONAR_HOSTURL_KEY)) {
            sonarUrl = cmd.getOptionValue(SONAR_HOSTURL_KEY);
        }        
        if (cmd.hasOption(SONAR_LANGUAGE_KEY)) {
            projectLanguage = cmd.getOptionValue(SONAR_LANGUAGE_KEY);
        }     
        if (cmd.hasOption(USER_NAME_KEY)) {
            username = cmd.getOptionValue(USER_NAME_KEY);
        }
        if (cmd.hasOption(USER_PASSWORD_KEY)) {
            password = cmd.getOptionValue(USER_PASSWORD_KEY);
        }
        
    }

    private void setupOptions() {           
        // add t option
        options.addOption(USER_NAME_KEY, true, "[Required] User Name ");
        options.addOption(USER_PASSWORD_KEY, true, "[Required] User Password");
        options.addOption(SOLUTION_PATH, true, "[Required] solution path");
        options.addOption(USER_CONFIG, true, "[Required] user configuration file");        
        options.addOption(CLI_CMD, true, "[Required] Operation to perform:\n" + 
                REPORT_SONAR + ": Report Violations in Sonar Server\n" +
                REPORT_ALL_LOCAL + ": Report Local Violations\n" +
                REPORT_SOURCE + ": Report Source Differences\n" +
                REPORT_COVERAGE + ": Report Coverage\n" +
                AUTH_SONAR + ": Authenticate Test \n");                      
        
        options.addOption(SONAR_POM_PATH, true, "[Optional] Pom file path location - default is solution path");
        options.addOption(SONAR_RUNNER_PATH, true, "[Optional] Sonar Runner file path location - default is solution path");
        options.addOption(PROJECT_KEY, true, "[Optional] Project Key");
        options.addOption(SONAR_HOSTURL_KEY, true, "[Optional] Host URL ID");
        options.addOption(SONAR_LANGUAGE_KEY, true, "[Optional] Project Language");
        options.addOption(FILE_PATH, true, "[Optional] file path - relative to solution path");
        
    }
    
    public String getFile() {
        return file;
    }
    
    public String getLocalFilePath() {
        return localFilePath;
    }
    
    public String getSolution() {
        return solution;
    }
    
    public void printHelp() {
        formatter.printHelp("vssonar-cli", options);
    }

    private void printConfiguration() {
        Utils.printToConsole( "File: " + file);
        Utils.printToConsole( "Solution: " + solution);
        Utils.printToConsole( "Sonar Analysis File: " + sonarConfigPath);
        Utils.printToConsole( "ProjectKey: " + projectKey);
        Utils.printToConsole( "SonarUrl: " + sonarUrl);
        Utils.printToConsole( "ProjectLanguage: " + projectLanguage);
        Utils.printToConsole( "UserConfiguration File: " + userconfigurationfile);
        Utils.printToConsole( "");
    }

    private void createPomConfiguration(String file) {
        
        pluginConfig.addNewSingleConfigurationElement(GROUP_ID_KEY);
        pluginConfig.addNewSingleConfigurationElement(ARTIFACT_ID_KEY);
        String[] elems = {SONARHOSTURL_KEY, SONARLANGUAGE_KEY, SONARJDBCURL_KEY, SONARJDBCUSERNAME_KEY, SONARJDBCPASSWORD_KEY, SONARSCMENABLED_KEY, SONARSCMURL_KEY};
        pluginConfig.addNewComplexConfigurationElement(PROPERTIESKEY, elems);
                
        // read xml
        DataConfiguration.getPropsFromXMLFile(pluginConfig.getConfigElems(), file);
        
        groupId = pluginConfig.getStringParameter(projectKey, GROUP_ID_KEY);
        projectKey = pluginConfig.getStringParameter(projectKey, GROUP_ID_KEY) + ":" + pluginConfig.getStringParameter(projectKey, ARTIFACT_ID_KEY);
        sonarUrl = pluginConfig.getInStringParameter(sonarUrl, PROPERTIESKEY, SONARHOSTURL_KEY);
        projectLanguage = pluginConfig.getInStringParameter(projectLanguage, PROPERTIESKEY, SONARLANGUAGE_KEY);
        jdbcUrl = pluginConfig.getInStringParameter(jdbcUrl, PROPERTIESKEY, SONARJDBCURL_KEY);
        jdbcUserName = pluginConfig.getInStringParameter(jdbcUserName, PROPERTIESKEY, SONARJDBCUSERNAME_KEY);
        jdbcUserPassword = pluginConfig.getInStringParameter(jdbcUserPassword, PROPERTIESKEY, SONARJDBCPASSWORD_KEY);
        scmUrl = pluginConfig.getInStringParameter(scmUrl, PROPERTIESKEY, SONARJDBCPASSWORD_KEY);
        scmEnabled = pluginConfig.getInBooleanParameter(scmEnabled, PROPERTIESKEY, SONARSCMURL_KEY);
    }

    private void createSonarRunnerConfiguration(String sonarConfigPath) throws IOException {
        List<String> lines = FileUtils.readLines(new File(sonarConfigPath), "utf-8");
        for(String line : lines) {
            if(line.contains(SONARHOSTURL_KEY)) {
                sonarUrl = line.split("=")[1].trim();            
            }
            if(line.contains("sonar." + PROJECT_KEY)) {
                projectKey = line.split("=")[1].trim();            
            }            
                        if(line.contains(SONARLANGUAGE_KEY)) {
                projectLanguage = line.split("=")[1].trim();            
            }            

        }
    }

    private void checkConfiguration() throws ParseException {
        String errormessage = "";
        if(command.isEmpty()) {
            errormessage = "<command not defined>";
        } else {
            if(!command.equals(AUTH_SONAR) && file.isEmpty()) {
                errormessage += "<no file to check>";
            }
        }   
        if(solution.isEmpty()) {
            errormessage += "<solution path not defined>";
        }
        if(password.isEmpty()) {
            errormessage += "<password not defined>";
        }
        if(username.isEmpty()) {
            errormessage += "<user not defined>";
        }
        if(sonarUrl.isEmpty()) {
            errormessage += "<sonar url not defined>";
        }
        if(projectKey.isEmpty()) {
            errormessage += "<project key not defined>";
        }
        if(projectLanguage.isEmpty()) {
            errormessage += "<project language not defined>";
        }
        
        if(!errormessage.isEmpty()) {
            throw new ParseException(errormessage);
        }                
    }
}
