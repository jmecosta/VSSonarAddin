/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tekla.vssonar;

import com.saxman.textdiff.Report;
import com.saxman.textdiff.TextDiff;
import com.tekla.vssonar.configuration.PluginConfiguration;
import com.tekla.vssonar.sensors.SensorsManager;
import com.tekla.vssonar.utils.CmdExecutor;
import com.tekla.vssonar.utils.ISonarQuery;
import com.tekla.vssonar.utils.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.sonar.wsclient.services.ResourceQuery;
import org.sonar.wsclient.services.Violation;

/**
 *
 * @author jocs
 */
public class SonarAnalysis {

    private final PluginConfiguration config;   
    private final ISonarQuery sonarQuery;
    
    private String resource = "";
    private String localfilename = "";
    private String profile = "";
    private List<Violation> violationsLocal;
    private List<String> sourceinsonar = new ArrayList<String>();
    private List<String> localsource = new ArrayList<String>();
    private Report report;


    public SonarAnalysis(ISonarQuery sonarQuery, PluginConfiguration config) throws IOException {
        this.config = config;
        this.sonarQuery = sonarQuery;
        this.resource = config.getResource();                
        this.localfilename = config.getLocalFilePath();

        try{        
            profile = sonarQuery.queryProfile(config.getProjectKey());
        } catch(org.sonar.wsclient.connectors.ConnectionException e) {
            throw new org.sonar.wsclient.connectors.ConnectionException("Authentication Failed" );           
        } catch(java.lang.NullPointerException e) {
            throw new org.sonar.wsclient.connectors.ConnectionException("Project Key is Incorrect" );           
        }
    }

    private void analyseSourceCode() throws IOException {

        if(sonarQuery.queryResource(resource)) {
            Utils.printToConsole("Resource was not found, please validate your configuration");
            Utils.printToConsole("ProjectKey: " + config.getProjectKey());
            Utils.printToConsole("Resource: " + resource);
            return;
        }
        
        // read local source        
        localsource.addAll(Utils.readLines(config.getSolution() + "/" + config.getFile()));

        // read source in sonar
        sourceinsonar = sonarQuery.querySource(resource);

        // Differences to previous version in sonar
        report = new TextDiff().compare(sourceinsonar.toArray(new String[localsource.size()]),
                localsource.toArray(new String[localsource.size()]));

    }

    private Integer reportViolations(List<Violation> violations, boolean checkenabled, boolean checkseverity, boolean islocal) {
        Integer reportedViolations = 0;

        for (Violation entry : violations) {
            Integer line = 0;
            if (entry.getLine() != null) {
                line = entry.getLine();
            }
            String severity = entry.getSeverity();
            if(checkseverity){
                severity = sonarQuery.queryRuleSeverity(config.getprojectLanguage(), entry.getRuleKey(), profile);
            }
            
            String rulekey = entry.getRuleKey();
            if(islocal){
                rulekey = entry.getRuleName() + "." + entry.getRuleKey();
            }
            
            if (checkenabled) {
                if (sonarQuery.queryIsRuleEnable(config.getprojectLanguage(), entry.getRuleKey(), profile)) {
                    Utils.printToConsole(config.getSolution() + "\\" + config.getFile() + "(" + line + "):" + severity + ":" + rulekey + ":" + entry.getMessage());
                    reportedViolations++;
                }               
            } else {
                Utils.printToConsole(config.getSolution() + "\\" + config.getFile() + "(" + line + "):" + severity + ":" + rulekey + ":"  + ":" + entry.getMessage());
                reportedViolations++;
                
            }            
        }
        return reportedViolations;
    }

    private Integer reportViolations(List<Violation> violations, int linestart, int lineend, boolean islocal) {
        Integer tot = 0;
        for (Violation entry : violations) {
            Integer line = 0;
            if (entry.getLine() != null) {
                line = entry.getLine();
            }
            String rulekey = entry.getRuleKey();
            if(islocal){
                rulekey = entry.getRuleName() + "." + entry.getRuleKey();
            }
            
            if (line >= linestart
                    && line <= (lineend)
                    && sonarQuery.queryIsRuleEnable(config.getprojectLanguage(), entry.getRuleKey(), profile)) {
                tot += 1;
                Utils.printToConsole(config.getSolution() + "\\" + config.getFile() + "(" + line+ "):" +
                        sonarQuery.queryRuleSeverity(config.getprojectLanguage(), entry.getRuleKey(), profile) + ":" + rulekey + ":"  +
                        ":" + entry.getMessage());
            }
        }
        return tot;
    }
    
    public Integer reportChangedLines() throws IOException {
        Utils.printToConsole("REPORT CHANGES TO SOURCE IN SONAR");
        
        analyseSourceCode();

        int count = report.size();
        for (int i = 0; i < count - 1; i++) {
            if (!report.getCommand(i).command.equals("Match")) {
                Utils.printToConsole("NEW SONAR CHANGE:" + report.getCommand(i).command);
                if(report.getCommand(i).command.equals("Insert before")){
                    Utils.printToConsole("INSERTED BEFORE LINES: " + report.getCommand(i).oldLines.fromLineNum +
                            " to " + report.getCommand(i).oldLines.thruLineNum);                
                } else {
                    Utils.printToConsole("SOURCE IN SONAR: ");                
                    for(int j = report.getCommand(i).oldLines.fromLineNum; j<=report.getCommand(i).oldLines.thruLineNum; j++) {
                        Integer line = j +1;
                        Utils.printToConsole("(" + line + "):" + sourceinsonar.get(j));
                    }                                
                }           
                if (!report.getCommand(i).command.equals("Delete")) {
                    Utils.printToConsole("LOCAL CHANGES:");                
                    for(int j = report.getCommand(i).newLines.fromLineNum; j<=report.getCommand(i).newLines.thruLineNum; j++) {
                        Integer line = j +1;
                        Utils.printToConsole(localfilename + "(" + line + "):" + localsource.get(j));
                    }           
                }
            }                
        }
        return count;        
    }
    
    public Integer reportAddedLocalViolations(SensorsManager allSensors) throws IOException {
        Utils.printToConsole("Violations Added in Source Code Changes");
        
        // Run Static Analysis Tools        
        violationsLocal = allSensors.executeSensors(localfilename);

        // analyse source
        analyseSourceCode();
        
        Integer tot = 0;
        Integer count = report.size();
        for (int i = 0; i < count; i++) {
            if (!report.getCommand(i).command.equals("Match") && !report.getCommand(i).command.equals("Delete")) {
                Integer nmb = reportViolations(violationsLocal,
                        report.getCommand(i).newLines.fromLineNum + 1,
                        report.getCommand(i).newLines.thruLineNum + 1, true);
                tot += nmb;
            }
        }
        Utils.printToConsole("Total Added: " + tot);
        return tot;
    }

    public Integer reportViolationsInSonar() {
        
        if(!sonarQuery.queryResource(resource)) {
            Utils.printToConsole("Resource was not found, please validate your configuration");
            Utils.printToConsole("ProjectKey: " + config.getProjectKey());
            Utils.printToConsole("Resource: " + resource);
            return 0;
        }
        
        Integer violations = reportViolations(sonarQuery.queryViolations(resource), false, false, false);        
        Utils.printToConsole("Violations in Sonar: " + violations);
        return violations;
    }
    
    public Integer reportLocalViolations(SensorsManager allSensors) throws IOException {       
        // Run Static Analysis Tools
        violationsLocal = allSensors.executeSensors(localfilename);
        Utils.printToConsole("Local Violations Sonar: " + reportViolations(violationsLocal, true, true, true));
        return violationsLocal.size();
    }

    public Integer reportCoverage() {
        
        if(sonarQuery.queryResource(resource)) {
            Utils.printToConsole("Resource was not found, please validate your configuration");
            Utils.printToConsole("ProjectKey: " + config.getProjectKey());
            Utils.printToConsole("Resource: " + resource);
            return 0;
        }

                
        Integer lines = 0;
        List<String> uncoveredlines = new ArrayList<String>();
        List<String> uncoveredlinesconditions = new ArrayList<String>();
        
        Utils.printToConsole("Coverage Violations Sonar");
        uncoveredlines = sonarQuery.queryNotCoveredLineHitsCoverage(resource);
        
        Utils.printToConsole("Non Covered Lines: " + uncoveredlines.size());
        for(String line: uncoveredlines) {
            Utils.printToConsole(localfilename + "(" + line + "): Line/Function not covered");
            lines += 1;
        }
        
        uncoveredlinesconditions = sonarQuery.queryNotCoveredConditionsHitsCoverage(resource);
        Utils.printToConsole("Non Covered Conditions: " + uncoveredlinesconditions.size());
        for(String line: uncoveredlinesconditions) {
            lines += 1;            
            String [] details =  line.split("=");
            Utils.printToConsole(localfilename + "(" + details[0] + "): " + details[1] + " Conditions to be covered");
        }        
        return lines;
    }
}
