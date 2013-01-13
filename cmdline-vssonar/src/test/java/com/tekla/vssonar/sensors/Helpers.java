/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tekla.vssonar.sensors;

import com.tekla.vssonar.utils.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jocs
 */
public class Helpers {
public static List<String> readFileData(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;
        List<String> stringBuilder = new ArrayList<String>();

        while ((line = reader.readLine()) != null) {
            if (!line.trim().isEmpty()) {
                stringBuilder.add(line);
            }
        }

        return stringBuilder;
    }

    public static String[] CreateCliArgs() {
        File pom = Utils.loadResource("/pom.xml");
        String solpath = pom.getParent().toString();
        String[] args = {"-solution_path", solpath,
            "-cmd", "report_sonar",
            "-username", "test",
            "-password", "test",
            "-sonarurl", "http://sonar:80",
                "-file_path", solpath + "\\src.cs"};
        return args;
    }
}
