/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ptc.services.common.config;

// import static com.mks.gateway.driver.word.exporter.WordTransformer.TEMPLATE_NAME;
import com.ptc.services.common.api.ApplicationProperties;
import java.text.SimpleDateFormat;
import mks.util.DateUtil;

/**
 *
 * @author veckardt
 */
public final class ExportProperties extends ApplicationProperties {

    public static String MAGENTA_RGBVALUE = "E20074";
    // private static final String propertiesFileName = "WordExport.properties";
    // private static Properties properties;
    // public variables
    public static final String dtDayFormat = DateUtil.DEFAULT_DATEONLY_FORMAT;
    public static final String dtDayTimeFormat = DateUtil.DEFAULT_DATETIMEONLY_FORMAT; // "dd.MM.yyyy HH:mm:ss";
    public static SimpleDateFormat baseline_sdf = new SimpleDateFormat(dtDayFormat);
    // List of Field Names used in version with Testting
    // Field Names
    public static String fldTestObjective;
    public static String fldTestsAsOfDate;
    public static String fldRunPercentage;
    public static String fldPlannedCount;
    public static String fldRunCount;
    public static String fldPassCount;
    public static String fldFailCount;
    public static String fldOtherCount;
    public static String fldText;
    public static String fldSummary;
    public static String fldTestSteps;
    public static String fldValidates;
    public static String fldTaskPhase;
    public static String fldType;
    public static String fldIsRelatedTo;
    // Task Phases
    public static String taskPhasePending;
    public static String taskPhaseOpen;
    public static String taskPhaseClosed;
    // Other Properties
    public static String allowedTypes;
    public static String workingDirectory;

    public static final String testObjectiveTypeName = "Test Objective";
    public static final String testSessionTypeName = "Test Session";
    public static final String defectTypeName = "Defect";
    public static final String testStepTypeName = "Test Step";
    public static final String fldState = "State";

    // Constructor
    public ExportProperties() {
        // loadProperties();
        super(ExportProperties.class);
        // List of Field Names used in version with Testting
        // Field Names
        fldTestObjective = getProperty("fldTestObjective", "Test Objective");
        fldTestsAsOfDate = getProperty("fldTestsAsOfDate", "Tests As Of Date");
        fldRunPercentage = getProperty("fldRunPercentage", "Run Percentage");
        fldPlannedCount = getProperty("fldPlannedCount", "Planned Count");
        fldRunCount = getProperty("fldRunCount", "Run Count");
        fldPassCount = getProperty("fldPassCount", "Pass Count");
        fldFailCount = getProperty("fldFailCount", "Fail Count");
        fldOtherCount = getProperty("fldOtherCount", "Other Count");
        fldText = getProperty("fldText", "Text");
        fldSummary = getProperty("fldSummary", "Summary");
        fldTestSteps = getProperty("fldTestSteps", "Test Steps");
        fldValidates = getProperty("fldValidates", "Validates");
        fldTaskPhase = getProperty("fldTaskPhase", "Task Phase");
        fldType = getProperty("fldType", "Type");
        fldIsRelatedTo = getProperty("fldIsRelatedTo", "Is Related To");
        // Task Phases
        taskPhasePending = getProperty("taskPhasePending", "Pending");
        taskPhaseOpen = getProperty("taskPhaseOpen", "Open");
        taskPhaseClosed = getProperty("taskPhaseClosed", "Closed");
        // Other Properties
        allowedTypes = getProperty("allowedTypes", "Doc List");
        workingDirectory = getProperty("workingDirectory", "Doc List");
    }
}
