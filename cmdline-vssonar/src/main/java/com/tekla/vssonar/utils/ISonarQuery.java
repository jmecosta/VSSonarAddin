/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tekla.vssonar.utils;

import java.util.List;
import org.sonar.wsclient.services.Violation;

/**
 *
 * @author jocs
 */
public interface ISonarQuery {

    String queryAllRuleSeverity(String language, String repository);

    boolean queryIsRuleEnable(String language, String key, String profile);

    List<String> queryNotCoveredConditionsHitsCoverage(String resource);

    List<String> queryNotCoveredLineHitsCoverage(String resource);

    String queryProfile(String language, String projectKey);

    String queryRuleSeverity(String language, String key, String profile);

    List<String> querySource(String resource);

    List<Violation> queryViolations(String resource);
    
}
