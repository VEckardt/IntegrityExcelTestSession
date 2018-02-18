/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ptc.services.common.config;

import static java.lang.System.out;

/**
 *
 * @author veckardt
 */
public class Copyright {

    public static final String COPYRIGHT = "(c)";
    public static String copyright = "Copyright " + COPYRIGHT + " 2013, 2014, 2015 PTC Inc.";
    public static String copyrightHtml = "Copyright &copy; 2013, 2014, 2015 PTC Inc.";
    public static String programName = "Integrity Custom Export for Word and Excel";
    public static String programVersion = "0.9.0";
    public static String author = "Author: Volker Eckardt";
    public static String email = "email: veckardt@ptc.com";

    public static void write() {
        out.println("* " + programName + " - Version " + programVersion);
        out.println("* A utility to enhance the Word Gateway for Integrity");
        out.println("* Tested with Integrity 10.4 and 10.6");
        out.println("*");
        out.println("* " + copyright);
        out.println("* " + author + ", " + email + "\n");
    }

    public static void usage() {
        out.println("*");
        out.println("* Usage: ");
        out.println("*   <path-to-javaw>\\javaw -jar <path-to-jar>\\IntegrityCustomGateway.jar");
        out.println("* Example:");
        out.println("*   C:\\Program Files\\Java\\jdk1.7.0_21\\bin\\javaw -jar C:\\IntegrityClient10\\lib\\IntegrityCustomGateway.jar");
        out.println("* Additional Notes:");
        out.println("*   - a configuration file 'CustomGateway.properties' can be used to specify default values");
        out.println("*   - a log file is created in directory '%temp%', the filename is 'IntegrityCustomGateway_YYYY-MM-DD.log'");
        out.println("*");
    }
}
