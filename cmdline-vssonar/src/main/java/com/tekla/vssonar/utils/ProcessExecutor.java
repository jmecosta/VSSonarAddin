/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tekla.vssonar.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jocs
 */
public class ProcessExecutor extends CmdExecutor {

    @Override
    public List<String> executeCmd(String[] cmdarray, String output, String[] environemnt) {
        List<String> lines = new ArrayList<String>();
        String cmd = "";
        for (String arg : cmdarray) {
            cmd = (cmd + " " + arg).trim();
        }

        try {
            List<String> args = new ArrayList<String>(cmdarray.length);
            for (String arg : cmdarray) {
                args.add(arg);
            }

            ProcessBuilder pb = new ProcessBuilder(args);
            if (environemnt != null) {
                Map<String, String> env = pb.environment();
                for (String var : environemnt) {
                    env.put(var.split("=")[0], var.split("=")[1]);
                }
            }

            Process p = pb.start();
            String line;
            BufferedReader input;
            if (output.equals("stderr")) {
                input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            } else {
                input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            }

            while ((line = input.readLine()) != null) {
                lines.add(line);
            }
            input.close();


        } catch (IOException ex) {
            Logger.getLogger(ProcessExecutor.class.getName()).log(Level.SEVERE, null, ex);
            Utils.printToConsole("Error Executing Cmd: " + ex.toString());
        }
        return lines;
    }
}
