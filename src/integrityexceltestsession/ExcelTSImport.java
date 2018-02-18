/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package integrityexceltestsession;

import com.mks.api.Command;
import com.mks.api.Option;
import com.mks.api.response.APIException;
import com.mks.api.response.Response;
import com.mks.api.response.WorkItem;
import com.ptc.services.common.api.IntegrityAPI;
import com.ptc.services.common.api.IntegrityMessages;
import com.ptc.services.common.tm.TestResult;
import static com.ptc.services.common.tools.FileUtils.canWriteFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author veckardt
 */
public class ExcelTSImport extends Task<Void> {

    private final static IntegrityMessages MC
            = new IntegrityMessages(ExcelTestSession.class);
    private IntegrityAPI apiSession;
    private String sessionID;
    private TextArea logArea;
    private String sourceFile;

    public ExcelTSImport(IntegrityAPI apiSession, TextArea logArea, String sessionID, String sourceFile) {
        this.apiSession = apiSession;
        this.logArea = logArea;
        this.sessionID = sessionID;
        this.sourceFile = sourceFile;
    }

    public void importFile() {
        log("Importing test results for session " + sessionID + " ...", 1);
        updateProgress(1, 20);

        File f = new File(sourceFile);
        if (!f.isFile()) {
            updateProgress(1, 1);
            log("ERROR: The input file " + sourceFile + " doesn't exist!", 1);
            return;
        }
        if (!canWriteFile(sourceFile)) {
            updateProgress(1, 1);
            log("ERROR: Please close the file " + sourceFile + " first, can not set import status in it!", 1);
            return;
        }

        int cntTestCases = 0;
        int cntTestSteps = 0;
        updateProgress(1, 20);

        FileInputStream input_document = null;
        try {
            input_document = new FileInputStream(new File(sourceFile));
            //Access the workbook
            XSSFWorkbook workbook = new XSSFWorkbook(input_document);
            //Access the worksheet, so that we can update / modify it.
            XSSFSheet sheet = workbook.getSheetAt(0);

            String testCaseID = "";
            String tcResult = "";
            String tcAnnotation = "";
            int count = 1;
            int errCnt = 0;

            HashMap stepMap = new HashMap();
            Row tcRow = null;

            // loop through all rows in the excel sheet
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {

                updateProgress(count++, sheet.getLastRowNum() + 1);

                Row row = rowIterator.next();

                if (row.getCell(0).getStringCellValue().startsWith("TC-")) {
                    cntTestCases++;
                    if (!testCaseID.isEmpty()) {
                        try {
                            log("Importing Case " + tcRow.getCell(0).getStringCellValue() + " with result '" + tcResult + "' ...", 2);
                            importResult(sessionID, testCaseID, stepMap, tcResult.contentEquals("-") ? "" : tcResult, tcAnnotation);
                            tcRow.getCell(6).setCellValue("ok");
                            tcRow.getCell(7).setCellValue("");
                        } catch (APIException ex) {
                            tcRow.getCell(6).setCellValue("error");
                            tcRow.getCell(7).setCellValue(ex.getMessage());
                            errCnt++;
                        }
                        stepMap.clear();
                    }

                    testCaseID = row.getCell(0).getStringCellValue().replace("TC-", "");
                    tcResult = row.getCell(4).getStringCellValue();
                    tcAnnotation = row.getCell(5).getStringCellValue();
                    tcRow = row;

                    // try {
                    //     importResult(sessionID, testCaseID, null, result.contentEquals("-") ? "" : result, annotation);
                    //     row.getCell(6).setCellValue("ok");
                    //     row.getCell(7).setCellValue("");
                    // } catch (APIException ex) {
                    //     row.getCell(6).setCellValue("error");
                    //     row.getCell(7).setCellValue(ex.getMessage());
                    // }
                    // Iterator<Cell> cellIterator = row.cellIterator();
                    // while (cellIterator.hasNext()) {
                    //     Cell cell = cellIterator.next();
                    //     // System.out.print(cell.getStringCellValue() + "\t\t");
                    //     // log ("Found Case: "+cell.getStringCellValue(),2);
                    // }
                } else if (row.getCell(1).getStringCellValue().startsWith("TS-")) {
                    cntTestSteps++;
                    String testStepID = row.getCell(1).getStringCellValue().replace("TS-", "");
                    String result = row.getCell(4).getStringCellValue();
                    String annotation = row.getCell(5).getStringCellValue();
                    // log("Importing Step " + row.getCell(1).getStringCellValue() + " with verdict '" + result + "' ...", 2);
                    log("Adding Step " + row.getCell(1).getStringCellValue() + " with verdict '" + result + "' ...", 2);
                    stepMap.put(testCaseID + "*" + testStepID, new TestResult(testStepID, result, annotation));

                    // try {
                    //     importResult(sessionID, testCaseID, testStepID, result.contentEquals("-") ? "" : result, annotation);
                    //     row.getCell(6).setCellValue("ok");
                    //     row.getCell(7).setCellValue("");
                    // } catch (APIException ex) {
                    //     row.getCell(6).setCellValue("error");
                    //     row.getCell(7).setCellValue(ex.getMessage());
                    // }
                    // Iterator<Cell> cellIterator = row.cellIterator();
                    // while (cellIterator.hasNext()) {
                    //     Cell cell = cellIterator.next();
                    //     // System.out.print(cell.getStringCellValue() + "\t\t");
                    //     // log ("Found Step: "+cell.getStringCellValue(),2);
                    // }
                }
            }

            if (tcRow != null) {
                try {
                    importResult(sessionID, testCaseID, stepMap, tcResult.contentEquals("-") ? "" : tcResult, tcAnnotation);
                    tcRow.getCell(6).setCellValue("ok");
                    tcRow.getCell(7).setCellValue("");
                } catch (APIException ex) {
                    tcRow.getCell(6).setCellValue("error");
                    tcRow.getCell(7).setCellValue(ex.getMessage());
                }
            }

            // write the target Excel file
            log("Updating Excel file with results ...", 1);
            FileOutputStream out = new FileOutputStream(new File(sourceFile));
            workbook.write(out);
            out.close();
            log("Excel file " + sourceFile + " updated.", 1);

            updateProgress(1, 1);
            if (errCnt == 0) {
                log("INFO: " + cntTestCases + " Test Cases and " + cntTestSteps + " Test Steps imported successfully.", 1);
            } else {
                log("ERROR: " + cntTestCases + " Test Cases and " + cntTestSteps + " Test Steps imported. " + errCnt + " errors detected.", 1);
                log("Please check the Excel file provided for further error hints!", 1);
            }
            // } catch (InvalidFormatException ex) {
            //     Logger.getLogger(ExcelTestSessionController.class.getName()).log(Level.SEVERE, null, ex);
            //     log(ex.getMessage(), 1);

        } catch (FileNotFoundException ex) {
            Logger.getLogger(ExcelTestSessionController.class.getName()).log(Level.SEVERE, null, ex);
            log(ex.getMessage(), 1);
        } catch (IOException ex) {
            Logger.getLogger(ExcelTestSessionController.class.getName()).log(Level.SEVERE, null, ex);
            log(ex.getMessage(), 1);
        } finally {
            try {
                input_document.close();
            } catch (IOException ex) {
                Logger.getLogger(ExcelTestSessionController.class.getName()).log(Level.SEVERE, null, ex);
                log(ex.getMessage(), 1);
            }
        }

        // MessageBox.show(null,
        //         MC.getMessage("EXCEL_DATA_IMPORTED").replace("{0}", sourceFile),
        //         "Result",
        //         MessageBox.ICON_ERROR | MessageBox.OK);
    }

    @Override
    protected Void call() throws Exception {
        importFile();
        Thread.sleep(1);
        return null;
    }

    // logs the text
    public void log(String text, int level) {
        logArea.appendText("\n" + text);
        System.out.println(text);
    }

    private boolean importResult(String sessionID, String testCaseID, HashMap stepMap, String verdict, String annotation) throws APIException {

        // Step one list test steps in session
        // get current results
        // get current step results
        // push all into an array for comparision
        // if equal, then dont upload, else upload
        Map<String, String> currentResults = new HashMap<>();

        // Step one list test steps in session
        Command cmd4 = new Command(Command.TM, "testcases");
        cmd4.addOption(new Option("fields", "ID,Test Steps"));
        cmd4.addSelection(sessionID);
        Response response = apiSession.executeCmd(cmd4);
        // WorkItemIterator witDoc = response.getWorkItems();
        // while (witDoc.hasNext()) {
        WorkItem workItem = response.getWorkItem(testCaseID);
        // get current results
        TestResult trc = new TestResult(apiSession, "viewresult", sessionID + ":" + testCaseID, null);
        String resultString = getString(trc.getVerdict()) + ";" + getString(trc.getAnnotation());
        try {
            // get current step results
            // ListIterator<Item> li = trc.getTestSteps().getList().listIterator();

            // while (li.hasNext()) {
            //     Item resultItem = li.next();
            //     resultString = resultString + ";" + resultItem.getField("stepVerdict").getValueAsString() + ";" + getString(resultItem.getField("stepAnnotation").getString());
            //     // log("In loop", 3);
            // }
            for (String step : workItem.getField("Test Steps").getValueAsString().split(",")) {
                if (!step.trim().isEmpty()) {
                    TestResult trs = new TestResult(apiSession, "stepresults", sessionID + ":" + workItem.getId() + ":" + step.trim(), step.trim());
                    resultString = resultString + ";" + getString(trs.getVerdict()) + ";" + getString(trs.getAnnotation());
                }
            }
        } catch (Exception ex) {
            // ExceptionHandler eh = new ExceptionHandler(ex);
            // log ("Exception:"+ex.getMessage(),2);
            Logger.getLogger(ExcelTestSessionController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            log("No test steps assigned for Test Case " + testCaseID + ".", 1);
        }
        currentResults.put(sessionID + ":" + testCaseID, resultString);
        // }
        // push all into an array for comparision
        // if equal, then dont upload, else upload

        // for (Entry entry : currentResults.entrySet()) {
        //     log(">> " + entry.getKey() + ": " + entry.getValue(), 1);
        // }
        try {
            // first, try to create a new result
            // if this works, fine, otherwiese move on with edit instead
            Command cmd2 = new Command(Command.TM, "createresult");
            cmd2.addOption(new Option("sessionID", sessionID));
            cmd2.addOption(new Option("verdict", verdict));
            cmd2.addOption(new Option("annotation", annotation));

            String newResultString = verdict + ";" + annotation;

            // Set set = 
            // Get an iterator
            Iterator i = stepMap.entrySet().iterator();
            // Display elements
            while (i.hasNext()) {
                Map.Entry me = (Map.Entry) i.next();
                TestResult tr = (TestResult) me.getValue();
                // System.out.print(me.getKey() + ": ");
                // System.out.println(me.getValue());

                cmd2.addOption(new Option("stepVerdict", "stepID=" + tr.getID() + ":verdict=" + tr.getVerdict2() + ":annotation=" + tr.getAnnotation() + ""));
                newResultString = newResultString + ";" + tr.getVerdict() + ";" + tr.getAnnotation();
            }

            cmd2.addSelection(testCaseID);
            String key = sessionID + ":" + testCaseID;

            String currentResultString = currentResults.get(sessionID + ":" + testCaseID);
            // log("currentResultString (" + key + "): " + currentResultString, 3);
            // log("newResultString     (" + key + "): " + newResultString, 3);

            if (!currentResultString.contentEquals(newResultString)) {
                Response response2 = apiSession.executeCmd(cmd2);
                response2.getExitCode();
            } else {
                log("Create result skipped for: " + sessionID + ":" + testCaseID + ", is the same.", 1);
            }

        } catch (APIException ex) {
            // if the creation is not possible, then we will try to edit
            //
            try {
                Logger.getLogger(ExcelTestSessionController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                // try overwrting
                Command cmd3 = new Command(Command.TM, "editresult");
                cmd3.addOption(new Option("sessionID", sessionID));
                cmd3.addOption(new Option("verdict", verdict));
                cmd3.addOption(new Option("annotation", annotation));

                String newResultString = verdict + ";" + annotation;

                Set set = stepMap.entrySet();
                // Get an iterator
                Iterator i = set.iterator();
                // Display elements
                while (i.hasNext()) {
                    Map.Entry me = (Map.Entry) i.next();
                    TestResult tr = (TestResult) me.getValue();
                    // System.out.print(me.getKey() + ": ");
                    // System.out.println(me.getValue());

                    cmd3.addOption(new Option("stepVerdict", "stepID=" + tr.getID() + ":verdict=" + tr.getVerdict2() + ":annotation=" + tr.getAnnotation() + ""));
                    newResultString = newResultString + ";" + tr.getVerdict() + ";" + tr.getAnnotation();
                }
                cmd3.addSelection(testCaseID);
                String key = sessionID + ":" + testCaseID;
                String currentResultString = currentResults.get(sessionID + ":" + testCaseID);
                // log("currentResultString (" + key + "): " + currentResultString, 3);
                // log("newResultString     (" + key + "): " + newResultString, 3);

                if (!currentResultString.contentEquals(newResultString)) {
                    Response response3 = apiSession.executeCmd(cmd3);
                    response3.getExitCode();
                } else {
                    log("Edit result skipped for: " + sessionID + ":" + testCaseID + ", is the same.", 1);
                }

            } catch (APIException ex1) {
                Logger.getLogger(ExcelTestSessionController.class.getName()).log(Level.SEVERE, ex1.getMessage(), ex1);
                throw ex1;
            }
        }

        return true;
    }

    private String getString(String text) {
        if (text == null) {
            return "";
        } else {
            return text;
        }
    }

    /*
     * private boolean importResult(String sessionID, String testCaseID, String testStepID, String verdict, String annotation) throws APIException {

     // Usage: tm createresult options... test id...; options are:
     //         --[no]forceEdit  edit the result if it already exists
     //         --addAttachment=value  Where value is path=pathToFile[,name=nameOfAttachment][,summary=shortDescription]
     //         --addRelatedItem=value  Where value is related item ID
     //         --annotation=value  Where value is the test result annotation
     //         --field=value  Where 'value' is 'fieldName=fieldValue'
     //         --stepVerdict=value  Where value is stepID=value[:verdict=value][:annotation=value]
     //         --verdict=test verdict  Where value is the test verdict
     //         --sessionID=value  The item id of the test session

     if (verdict.contentEquals(TestResult.getNotTestedText())) {
     verdict = "";
     }

     if (testStepID == null) {
     try {
     Command cmd = new Command(Command.TM, "createresult");
     cmd.addOption(new Option("sessionID", sessionID));
     cmd.addOption(new Option("verdict", verdict));
     cmd.addOption(new Option("annotation", annotation));
     cmd.addSelection(testCaseID);
     Response response = apiSession.executeCmd(cmd);
     response.getExitCode();
     } catch (APIException ex) {
     try {
     Logger.getLogger(ExcelTestSessionController.class.getName()).log(Level.SEVERE, null, ex);
     // try overwrting
     Command cmd = new Command(Command.TM, "editresult");
     cmd.addOption(new Option("sessionID", sessionID));
     cmd.addOption(new Option("verdict", verdict));
     cmd.addOption(new Option("annotation", annotation));
     cmd.addSelection(testCaseID);
     Response response = apiSession.executeCmd(cmd);
     response.getExitCode();
     } catch (APIException ex1) {
     Logger.getLogger(ExcelTestSessionController.class.getName()).log(Level.SEVERE, null, ex1);
     throw ex1;
     }
     }

     } else {

     try {
     Command cmd = new Command(Command.TM, "createresult");
     cmd.addOption(new Option("sessionID", sessionID));
     cmd.addOption(new Option("stepVerdict", "stepID=" + testStepID + ":verdict=" + verdict + ":annotation=" + annotation + ""));
     // cmd.addOption(new Option("annotation", annotation));
     cmd.addSelection(testCaseID);
     Response response = apiSession.executeCmd(cmd);
     response.getExitCode();
     } catch (APIException ex) {
     try {
     Logger.getLogger(ExcelTestSessionController.class.getName()).log(Level.SEVERE, null, ex);
     Command cmd = new Command(Command.TM, "createresult");
     cmd.addOption(new Option("sessionID", sessionID));
     cmd.addOption(new Option("stepVerdict", "stepID=" + testStepID + ":verdict=" + verdict + ":annotation=" + annotation + ""));
     // cmd.addOption(new Option("annotation", annotation));
     cmd.addSelection(testCaseID);
     Response response = apiSession.executeCmd(cmd);
     response.getExitCode();
     } catch (APIException ex1) {
     Logger.getLogger(ExcelTestSessionController.class.getName()).log(Level.SEVERE, null, ex1);
     throw ex1;
     }
     }
     }
     return true;
     }
     */
}
