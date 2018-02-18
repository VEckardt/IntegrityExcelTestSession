/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package integrityexceltestsession;

/**
 *
 * @author veckardt
 */
public class Copyright {

    public static final String COPYRIGHT = "(c)";
    public static String copyright = "Copyright " + COPYRIGHT + " 2014 PTC Inc.";
    public static String copyrightHtml = "Copyright &copy; 2014 PTC Inc.";
    public static String programName = "Integrity Excel Test Session";
    public static String programVersion = "0.6";
    public static String author = "Author: Volker Eckardt";
    public static String email = "email: veckardt@ptc.com";

    public static void write() {
        System.out.println("* " + programName + " - Version " + programVersion);
        System.out.println("* An utility to utilize Excel for test result handling");
        System.out.println("* Tested with Integrity 10.4");
        System.out.println("*");
        System.out.println("* " + copyright);
        System.out.println("* " + author + ", " + email + "\n");
    }

    public static void usage() {
        System.out.println("*");
        System.out.println("* Usage: ");
        System.out.println("*   <path-to-javaw>\\javaw -jar <path-to-jar>\\IntegrityExcelTestSession.jar");
        System.out.println("* Example:");
        System.out.println("*   C:\\Program Files\\Java\\jdk1.7.0_40\\bin\\javaw -jar ..\\IntegrityExcelTestSession.jar");
        System.out.println("* Additional Notes:");
        System.out.println("*   - a configuration file 'IntegrityGenAdminDoc.properties' can be used to specify default values");
        System.out.println("*   - a log file is created in directory '%temp%', the filename is 'IntegrityExcelTestSession_YYYY-MM-DD.log'");
        System.out.println("*");
    }
}
