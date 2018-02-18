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
import com.mks.api.response.WorkItemIterator;
import com.ptc.services.common.api.ExceptionHandler;
import com.ptc.services.common.api.IntegrityAPI;
import com.ptc.services.common.api.IntegrityMessages;
import static com.ptc.services.common.excel.ExcelWorkbook.copyRow;
import static com.ptc.services.common.excel.ExcelWorkbook.deleteRow;
import com.ptc.services.common.tm.TestResult;
import com.ptc.services.common.tools.FileUtils;
import static com.ptc.services.common.tools.StringUtils.getString;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import jfx.messagebox.MessageBox;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author veckardt
 */
public class ExcelTSExport extends Task<Void> {

    private final static IntegrityMessages MC
            = new IntegrityMessages(ExcelTestSession.class);
    private IntegrityAPI apiSession;
    private TextArea logArea;
    private String sessionID;
    private String templateFile;
    private String targetfile;
    private Date asOfDate;
    private int mode;
    private CheckBox cOpenExcel;

    public ExcelTSExport(IntegrityAPI apiSession, TextArea logArea, int mode, String sessionID, String templateFile, String targetfile, Date asOfDate, CheckBox cOpenExcel) {
        this.apiSession = apiSession;
        this.logArea = logArea;
        this.sessionID = sessionID;
        this.templateFile = templateFile;
        this.targetfile = targetfile;
        this.asOfDate = asOfDate;
        this.mode = mode;
        this.cOpenExcel = cOpenExcel;
    }

    public void export() {
        log("Exporting Test Session " + sessionID + " into an Excel file ...", 1);
        // String[] resultFilter = {""};

        try {

            updateProgress(1, 20);
            if (!FileUtils.canWriteFile(targetfile)) {
                updateProgress(1, 1);
                log("ERROR: Please close the file " + targetfile + " first, can not write to it!", 1);
                return;
            }

            //Read the spreadsheet that needs to be updated
            FileInputStream input_document = new FileInputStream(new File(templateFile));
            //Access the workbook
            XSSFWorkbook workbook = new XSSFWorkbook(input_document);
            //Access the worksheet, so that we can update / modify it.
            XSSFSheet sheet = workbook.getSheetAt(0);

            // rename sheet name
            String sheetName = "Session " + sessionID;
            workbook.setSheetName(workbook.getSheetIndex(sheet), sheetName);

            // add Excel Rigion Name
            // Name name = workbook.createName();
            // name.setNameName("TableBegin");
            // name.setRefersToFormula("'"+sheetName+"'!$A$5");
            // XSSFWorkbook workbook = new XSSFWorkbook();
            // XSSFSheet sheet = workbook.createSheet("Session " + sessionID);
            // setStyles(workbook);
            WorkItem wi = apiSession.getWorkItem(sessionID, "Summary,State");
            sheet.getRow(0).getCell(2).setCellValue(sessionID);
            sheet.getRow(1).getCell(2).setCellValue(wi.getField("Summary").getString());
            sheet.getRow(2).getCell(2).setCellValue(wi.getField("State").getValueAsString());

            int cntTestCases = 0;
            int cntTestSteps = 0;
            // First Target Row = 7th
            int rownum = 6;
            // Get all test cases in the session
            Command cmd = new Command(Command.TM, "testcases");
            cmd.addOption(new Option("substituteParams"));

            // if (mode == 2) {
            //     cmd.addOption(new Option("queryDefinition", "(field[Last Result] contains \"Not Run\")"));
            // } else {
            // cmd.addOption(new Option("AsOf", dfDayTimeUS.format(asOfDate)));
            // }
            // cmd.addOption(new Option("fields", "ID,Text::rich,Test Steps"));
            cmd.addOption(new Option("fields", "Text,Test Steps,Expected Results"));
            cmd.addSelection(sessionID);
            Response response = apiSession.executeCmd(cmd);

            int count = 2;

            WorkItemIterator witDoc = response.getWorkItems();

            while (witDoc.hasNext()) {
                updateProgress(count++, response.getWorkItemListSize() + 2);
                // 
                WorkItem workItem = witDoc.next();
                TestResult trc = new TestResult(apiSession, "viewresult", sessionID + ":" + workItem.getId(), null);
                if ((mode == 2 && getString(trc.getVerdict()).contentEquals("-"))
                        || (mode == 2 && getString(trc.getVerdict()).contentEquals(""))
                        || (mode == 2 && getString(trc.getVerdict()).contentEquals("Not Tested"))
                        || (mode == 1)) {
                    rownum++;
                    cntTestCases++;
                    String text = workItem.getField("Text").getString();
                    String expectedResults = workItem.getField("Expected Results").getString();

                    // 1: enter the test case data
                    copyRow(workbook, sheet, 5, rownum);
                    sheet.getRow(rownum).getCell(0).setCellValue("TC-" + workItem.getId());
                    sheet.getRow(rownum).getCell(1).setCellValue((String) null);
                    sheet.getRow(rownum).getCell(2).setCellValue(text);
                    sheet.getRow(rownum).getCell(3).setCellValue(expectedResults);
                    sheet.getRow(rownum).getCell(4).setCellValue(trc.getVerdict());
                    sheet.getRow(rownum).getCell(5).setCellValue(trc.getAnnotation());

                    try {
                        for (String step : workItem.getField("Test Steps").getValueAsString().split(",")) {
                            // log("step.trim() '" + step.trim() + "'", 1);
                            if (!step.trim().isEmpty()) {
                                TestResult trs = new TestResult(apiSession, "stepresults", sessionID + ":" + workItem.getId() + ":" + step.trim(), step.trim());

                                rownum++;
                                cntTestSteps++;
                                // 2: enter the test step data
                                copyRow(workbook, sheet, 6, rownum);
                                sheet.getRow(rownum).getCell(1).setCellValue("TS-" + step.trim());
                                sheet.getRow(rownum).getCell(2).setCellValue(trs.getSummary());
                                sheet.getRow(rownum).getCell(3).setCellValue(trs.getDescription());
                                sheet.getRow(rownum).getCell(4).setCellValue(trs.getVerdict());
                                sheet.getRow(rownum).getCell(5).setCellValue(trs.getAnnotation());
                            }
                        }
                    } catch (Exception ex) {
                        log("No test steps assigned for Test Case " + workItem.getId() + ".", 1);
                    }
                }
            }

            if (cntTestCases == 0) {
                log("INFO: No file written, all Test Cases have already a verdict assigned.", 1);
            } else {
                try {
                    input_document.close();

                    // delete the template rows
                    if (cntTestCases > 0) {
                        deleteRow(sheet, 6);
                        deleteRow(sheet, 5);
                        try (FileOutputStream out = new FileOutputStream(new File(targetfile))) {
                            workbook.write(out);
                        }
                        log("Excel file " + targetfile + " written.", 1);
                        log(cntTestCases + " Test Cases and " + cntTestSteps + " Steps exported successfully.", 1);

                        updateProgress(1, 1);

                        // MessageBox.show(ExcelTestSession.stage,
                        //         MC.getMessage("EXCEL_FILE_GENERATED").replace("{0}", targetfile),
                        //         "Result",
                        //         MessageBox.ICON_ERROR | MessageBox.OK);
                        if (cOpenExcel.isSelected()) {
                            FileUtils.openWindowsFile("Excel", targetfile);
                        } else {
                            log("INFO: Open in MS Excel not selected.", 2);
                        }
                    }

                } catch (FileNotFoundException ex) {
                    Logger.getLogger(ExcelTestSessionController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    log("ERROR: " + ex.getMessage(), 1);
                } catch (IOException ex) {
                    Logger.getLogger(ExcelTestSessionController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    log("ERROR: " + ex.getMessage(), 1);
                }
            }
            updateProgress(1, 1);
            System.exit(1);

        } catch (APIException ex) {
            // Logger.getLogger(ExcelTestSessionController.class.getName()).log(Level.SEVERE, null, ex);
            ExceptionHandler eh = new ExceptionHandler(ex);
            // INTEGRITY_API_ERROR
            MessageBox.show(ExcelTestSession.stage,
                    MC.getMessage("INTEGRITY_API_ERROR").replace("{0}", eh.getMessage()),
                    "API Error",
                    MessageBox.ICON_ERROR | MessageBox.OK);

            log(eh.getMessage(), 1);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ExcelTestSessionController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            log("ERROR: " + ex.getMessage(), 1);
        } catch (IOException ex) {
            Logger.getLogger(ExcelTestSessionController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            log("ERROR: " + ex.getMessage(), 1);
        }
    }

    @Override
    protected Void call() throws Exception {
        export();
        Thread.sleep(1);
        return null;
    }

    // logs the text
    public void log(String text, int level) {
        logArea.appendText("\n" + text);
        System.out.println(text);
    }
}
