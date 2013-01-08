/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tekla.vssonar.configuration;

/**
 *
 * @author jocs
 */
public abstract class Configuration {

    public Configuration() {
    }
    public abstract String getUserConfigurationFile();
    
    public abstract String getFile();

    public abstract String getProjectKey();

    public abstract String getSolution();

    public abstract String getSonarUrl();

    public abstract String getjdbcUrl();

    public abstract String getjdbcUserName();

    public abstract String getjdbcUserPassword();

    public abstract String getprojectLanguage();

    public abstract boolean getscmEnabled();

    public abstract String getscmUrl();
    
}
