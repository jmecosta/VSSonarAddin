package com.tekla.vssonar;

import com.tekla.vssonar.configuration.PluginConfiguration;
import com.tekla.vssonar.sensors.SensorsManager;
import com.tekla.vssonar.utils.CmdExecutor;
import com.tekla.vssonar.utils.ISonarQuery;
import com.tekla.vssonar.utils.ProcessExecutor;
import com.tekla.vssonar.utils.SonarQuery;
import com.tekla.vssonar.utils.Utils;
import java.io.IOException;
import org.apache.commons.cli.ParseException;
import org.sonar.wsclient.Sonar;

/**
 * VssonarCLi command line entry point
 *
 */
public final class VssonarCli {

    private VssonarCli() {
    }

    public static void main(String[] args) throws ParseException, IOException, InterruptedException {

        try {
            PluginConfiguration config = new PluginConfiguration(args);
            CmdExecutor executor = new ProcessExecutor();
            Sonar sonar = Sonar.create(config.getSonarUrl(), config.getUserName(), config.getPassword());
            ISonarQuery query = new SonarQuery(sonar);
            SonarAnalysis analyse = new SonarAnalysis(query, config);

            if (config.getCommand().equals(PluginConfiguration.REPORT_SONAR)) {
                analyse.reportViolationsInSonar();
            }

            if (config.getCommand().equals(PluginConfiguration.REPORT_ALL_LOCAL)) {
                SensorsManager allSensors = new SensorsManager(executor, config);                
                analyse.reportLocalViolations(allSensors);
            }
            if (config.getCommand().equals(PluginConfiguration.REPORT_SOURCE)) {
                analyse.reportChangedLines();
            }
            if (config.getCommand().equals(PluginConfiguration.REPORT_LOCAL)) {
                SensorsManager allSensors = new SensorsManager(executor, config);                
                analyse.reportAddedLocalViolations(allSensors);
            }
            if (config.getCommand().equals(PluginConfiguration.REPORT_COVERAGE)) {
                analyse.reportCoverage();
            }
        } catch (Exception e) {
            Utils.printToConsole(e.getMessage());
        }
    }
}
