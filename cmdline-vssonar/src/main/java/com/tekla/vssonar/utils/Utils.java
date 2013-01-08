/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tekla.vssonar.utils;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jocs
 */
public final class Utils {

    private Utils() {
    }
            
    public static List<String> readLines(String filename) throws IOException {
        FileReader fileReader = new FileReader(filename);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<String> lines = new ArrayList<String>();
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            lines.add(line);
        }
        bufferedReader.close();
        return lines;
    }

    public static File loadResource(String resourceName) {
        URL resource = Utils.class.getResource(resourceName);
        File resourceAsFile = null;
        try {
            resourceAsFile = new File(resource.toURI());
        } catch (URISyntaxException e) {
            Utils.printToConsole("Cannot load resource: " + resourceName + " " + resource);
        } catch (IllegalArgumentException e) {
            Utils.printToConsole("Cannot load resource: " + resourceName + " " + resource);
        }

        return resourceAsFile;
    }

    public static File loadResourceAsStream(String resourceName) {
        final InputStream stream = Utils.class.getResourceAsStream(resourceName);

        File newFile = null;
        try {
            // write contents to temp file and return path to it
            newFile = File.createTempFile("vssonar", "tmp");


            newFile.createNewFile();
            OutputStream out = new FileOutputStream(newFile);

            byte buf[] = new byte[1024];
            int len;
            while ((len = stream.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            stream.close();

        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }

        return newFile;
    }
    
    public static File createTmpFile(List<String> content) {
        File newFile = null;
        try {
            // write contents to temp file and return path to it
            newFile = File.createTempFile("vssonar", "tmp");


            newFile.createNewFile();
            OutputStream out = new FileOutputStream(newFile);

            for(String tofile: content){
                out.write(tofile.getBytes());
            }
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }

        return newFile;
    }    

    public static void printToConsole(String string) {
        System.out.println(string); // NOSONAR
    }
}
