/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tekla.vssonar.utils;

import java.util.ArrayList;
import java.util.List;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.*;

/**
 *
 * @author jocs
 */
public class SonarQuery implements ISonarQuery {

    private List<Rule> findAll = null;
    private final Sonar sonar;
    
    public SonarQuery(Sonar sonar){
        this.sonar = sonar;
    }

    @Override
    public boolean queryIsRuleEnable(String language, String key, String profile) {
        RuleQuery ruleq = new RuleQuery(language).setProfile(profile);
        if(findAll == null) {
            findAll = sonar.findAll(ruleq);
        }
        boolean isEnabled = false;
        for(Rule i: findAll) {            
            if(i.getConfigKey().contains(key) && i.isActive()) {
                isEnabled = true;
            }        
        }
        return isEnabled;
    }

    @Override
    public String queryRuleSeverity(String language, String key, String profile) {
        String severity = "NOT DEFINED";
        RuleQuery ruleq = new RuleQuery(language).setProfile(profile);
        if(findAll == null) {
            findAll = sonar.findAll(ruleq);
        }
        for(Rule i: findAll) {
            if(i.getConfigKey().contains(key)) {                
                severity = i.getSeverity();
            }        
        }
        return severity;
    }
    
    @Override
    public String queryAllRuleSeverity(String language, String repository) {
        String severity = "NOT DEFINED";
        List<Rule> rulesall = null;       
        RuleQuery ruleq = new RuleQuery(language).setProfile(repository);
        rulesall = sonar.findAll(ruleq);
        Integer blockercount = 0;
        Integer criticalcount = 0;
        Integer majorcount = 0;
        Integer minorcount = 0;
        Integer infocount = 0;
        
        
        Utils.printToConsole("Language: " + language);        
        for(Rule i: rulesall) {
            Utils.printToConsole(": severity: ========== " + i.getSeverity() + " ============== " + "Rule: " + i.getKey());
            if (i.getSeverity().equalsIgnoreCase("Blocker") && i.isActive()) {
                blockercount++;
            }
            if (i.getSeverity().equalsIgnoreCase("Critical") && i.isActive()) {
                criticalcount++;            
            }
            if (i.getSeverity().equalsIgnoreCase("Major") && i.isActive()) {
                majorcount++;            
            }
            if (i.getSeverity().equalsIgnoreCase("Minor") && i.isActive()) {
                minorcount++;            
            }
            if (i.getSeverity().equalsIgnoreCase("Info") && i.isActive()) {
                infocount++;            
            }            
        }
        Utils.printToConsole("Blocker: " + blockercount);        
        Utils.printToConsole("Critical: " + criticalcount);        
        Utils.printToConsole("Major: " + majorcount);        
        Utils.printToConsole("Minor: " + minorcount);        
        Utils.printToConsole("Info: " + infocount);        
        
        return severity;
    }    

    @Override
    public String queryProfile(String language, String projectKey) { 
        ResourceQuery query = ResourceQuery.createForMetrics(projectKey, "profile");
        Resource struts = sonar.find(query);
        Measure profile = struts.getMeasure("profile");
        return profile.getData();
    }

    @Override
    public List<String> queryNotCoveredLineHitsCoverage(String resource) {
        String [] metrics = {"coverage_line_hits_data"};
        Resource struts = sonar.find(ResourceQuery.createForMetrics(resource, metrics));
        List<String> uncoveredLines = new ArrayList<String>();
        
        // get coverage line hits data
        if(struts!=null) {
            Measure linehits = struts.getMeasure("coverage_line_hits_data");
            if(linehits != null) {
                String[] hits = linehits.getData().split(";");
                for(String hit: hits) {
                    if(hit.contains("=0")) {
                        uncoveredLines.add(hit.split("=")[0]);
                    }                
                }            
            }
        }
        
        return uncoveredLines;
    }
    
    @Override
    public List<String> queryNotCoveredConditionsHitsCoverage(String resource) {
        String [] metrics = {"conditions_by_line", "covered_conditions_by_line"};
        Resource struts = sonar.find(ResourceQuery.createForMetrics(resource, metrics));
        List<String> uncoveredLines = new ArrayList<String>();
        
        // get coverage line hits data
        Measure conditionsbyline = struts.getMeasure("conditions_by_line");
        if(conditionsbyline != null) {
            String[] conditionsByline = conditionsbyline.getData().split(";");
            Measure coveredconditionsbyline = struts.getMeasure("covered_conditions_by_line");
            String[] covcondbyline = coveredconditionsbyline.getData().split(";");

            for(int i=0; i< covcondbyline.length; i++) {
                if( !conditionsByline[i].equals(covcondbyline[i])){
                    String line = conditionsByline[i].split("=")[0];
                    String totcond = conditionsByline[i].split("=")[1];
                    String unccond = covcondbyline[i].split("=")[1];
                    Integer uncov = Integer.parseInt(totcond) - Integer.parseInt(unccond);
                    uncoveredLines.add(line + "=" + uncov.toString());
                }
            }
        }
        return uncoveredLines;
    }
      
    @Override
    public List<Violation> queryViolations(String resource) {
        return sonar.findAll((ViolationQuery.createForResource(resource)));
    }
    
    @Override
    public List<String> querySource(String resource) {
        List<String> sourcelines = new ArrayList<String>();
        Source source = sonar.find((SourceQuery.create(resource)));
        for(String line : source.getLines()) {            
            sourcelines.add(line);
        }
        return sourcelines;
    }        
}
