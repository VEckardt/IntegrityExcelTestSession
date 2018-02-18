/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ptc.services.common.gateway.tm;

import com.mks.api.Command;
import com.mks.api.Option;
import com.mks.api.response.APIException;
import com.mks.api.response.InvalidCommandOptionException;
import com.mks.api.response.Response;
import com.mks.api.response.WorkItem;
import com.mks.gateway.data.ExternalItem;
import com.mks.gateway.data.ItemField;
import com.mks.gateway.driver.IIntegrationDriver;
import com.mks.gateway.mapper.ItemMapperException;
import com.mks.gateway.mapper.ItemMapperSession;
import com.mks.gateway.mapper.config.ItemMapperConfig;
import static com.ptc.services.common.config.Config.dfDayTime;
import com.ptc.services.common.config.ExportProperties;
import static com.ptc.services.common.config.ExportProperties.defectTypeName;
import static com.ptc.services.common.config.ExportProperties.testSessionTypeName;
import com.ptc.services.common.gateway.DocHandler;
import com.ptc.services.common.tm.TestResult;
import static com.ptc.services.common.gateway.ItemHandler.getNewItem;
import static com.ptc.services.common.gateway.LogAndDebug.log;
import static com.ptc.services.common.gateway.tm.TestDefects.countDefectsByPhase;
import static com.ptc.services.common.gateway.tm.TestDefects.defectMap;
import static com.ptc.services.common.tools.StringUtils.repeat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author veckardt
 */
public class TestUtils {

    // Private
    public static Boolean addSeparateDefectSection = true;

    private static Map<String, Boolean> testCaseTypes = new TreeMap<String, Boolean>();
    public static Map<String, ExternalItem> testSessionMap = new TreeMap<String, ExternalItem>();
    public static TestResultFieldList testResultFields = new TestResultFieldList();

    public static void init(ItemMapperSession imSession) throws APIException {
        testResultFields.init(imSession);
    }

    private TestUtils() {

    }

    /**
     * Returns true if the item is of type TestCase
     *
     * @param item
     * @return
     */
    public static Boolean isTestCase(ItemMapperSession imSession, ExternalItem item) throws ItemMapperException, APIException {
        String type = item.getValueAsString("Type");
        if (testCaseTypes.containsKey(type)) {
            return testCaseTypes.get(type);
        } else {
            // Determine dynmically if this type is a test case
            Command cmd = new Command(Command.IM, "types");
            cmd.addOption(new Option("fields", "TestRole"));
            cmd.addSelection(type);
            Response response = imSession.executeCmd(cmd);
            // ResponseUtil.printResponse(response, 1, System.out);
            WorkItem wi = response.getWorkItem(type);
            String testRole = wi.getField("testRole").getValueAsString();
            testCaseTypes.put(type, testRole.contentEquals("testCase"));
            return testRole.contentEquals("testCase");
        }
        // return getType(item).contentEquals("Test Case");
    }

    /**
     * Adds Test Result INformation to the current child
     *
     * @param currentChild
     * @param mappingConfig
     */
    public static void addLastResultToTestCase(ItemMapperSession imSession, IIntegrationDriver.IGatewayDriver gatewayDriver, ExternalItem currentChild, ItemMapperConfig mappingConfig, Date asOfDate) throws ItemMapperException, APIException {

        // log ("Addding bookmark ",2);
        // setBookmarkForField(currentChild, "ID", DocNodeIterators.containsReferencesToReference);
        String testCaseID = currentChild.getId().getInternalID();
        log("Begin addLastResultToTestCase: " + testCaseID, 2);

        // Get the last result recorded for this test case 
        TestResultList trl = new TestResultList(imSession, testCaseID, true);
        if (trl.size() > 0) {
            // sorta ll results descending
            // nothing to sort anymore: Collections.sort(trl, new TestResultList.SortDescending());
            // get the last result
            TestResult trLast = trl.getLastTestResult();
            trLast.addTestResultData(currentChild);

            // record also the session id in the session array
            if (!testSessionMap.containsKey(trLast.getSessionID())) {
                ItemMapperConfig imcTestSession = mappingConfig.getSubConfig(testSessionTypeName);
                ExternalItem testSession = getNewItem(imSession, gatewayDriver, imcTestSession, trLast.getSessionID(), asOfDate, null);
                // testSession.setIdAttribute(trLast.getSessionID(), true);
                testSessionMap.put(trLast.getSessionID(), testSession);
            }
            // 
            currentChild.add("Session Summary", testSessionMap.get(trLast.getSessionID()).getField("Summary").getStringValue());

            // check attached defects
            if (!trLast.getRelatedItems().isEmpty()) {

                ItemMapperConfig imcDefect = mappingConfig.getSubConfig(defectTypeName);

                for (String relatedDefect : trLast.getRelatedItems().split(",")) {

                    // get the defect and place it into the defect array
                    ExternalItem defect = getNewItem(imSession, gatewayDriver, imcDefect, relatedDefect, new Date(), null);
                    if (defect.getField("Type").getStringValue().contentEquals(defectTypeName)) {
                        if (addSeparateDefectSection) {
                            defectMap.put(testCaseID + ":" + defect.getInternalId(), defect);
                        }
                        countDefectsByPhase(defect);
                        // testCase.addChild(defect);
                    }
                }
            }
            // this does not work yet
            Response response = getTestCasesBySession(imSession, trLast.getSessionID(), new Date(), testCaseID);
            // ResponseUtil.printResponse(response, 1, System.out);
            WorkItem wi = response.getWorkItem(testCaseID);
            if (wi.getField("Text") != null) {
                String text = wi.getField("Text").getString();
                // log("1: " + text, 1);

                // while (text.contains("attachmentname="))
                // text = text.replace(" ", "%20");
                // text = text.replaceAll(" ([^<]*>)", "%20");
// Pattern p = Pattern.compile("s\\([^\\)]*\\s+[^\\)]*\\)");
//        Matcher m = p.matcher(text);
//        while (m.find()) {
//            String temp = m.group();
//            text = text.replace(temp, temp.replaceAll("\\s", ""));
//        }                
                boolean on = false;
                String ch;
                String text2 = "";
                // log("text.length(): "+ text.length(),1);

                for (int i = 0; i < text.length(); i++) {
                    if (i < text.length() - 7 && text.substring(i, i + 8).contentEquals("fieldid=")) {
                        on = true;
                    }
                    if (on) {
                        ch = (text.charAt(i) == ' ') ? "%20" : text.substring(i, i + 1);

                        if (text.charAt(i) == '"') {
                            on = false;
                        }
                    } else {
                        ch = text.substring(i, i + 1);
                        // log("ch = '" + ch + "'", 1);
                    }
                    text2 = text2 + ch;
                }
                text2 = text2.replace("mks:///item/field?fieldid=", testCaseID + "/");
                text2 = text2.replace("&amp;attachmentname=", "/");
                text2 = text2.replace("&attachmentname=", "/");

                // log("Text 1: " + text, 1);
                // log("Text 2: " + text2, 1);
                currentChild.getItemData().addField(new ItemField.RichContent("Text", ItemField.RichContentDataFormat.XHTML, text2));
            }
        }
        // log("Validates: "+testCase.getField("Validates").getStringValue(),1);
        String traceIdsEnh = DocHandler.getTraceIDsEnh(imSession, currentChild.getField(ExportProperties.fldValidates).getStringValue());
        currentChild.add("Trace IDs Enh", traceIdsEnh);
        log("End of addLastResultToTestCase: " + testCaseID, 2);
    }

    /**
     * Gets the Test Case Information for a Session, and optionally for a single
     * Test Case only
     */
    public static Response getTestCasesBySession(ItemMapperSession imSession, String sessionID, Date asOfDate, String queryID) throws APIException {
        // Get all test cases in the session
        Command cmd = new Command(Command.TM, "testcases");
        cmd.addOption(new Option("substituteParams"));
        cmd.addOption(new Option("fields", "Type," + ExportProperties.fldText + "::rich," + ExportProperties.fldTestSteps));
        // 
        if (!queryID.isEmpty()) {
            cmd.addOption(new Option("queryDefinition", "(field[ID]=" + queryID + ")"));
        } else {
            cmd.addOption(new Option("AsOf", new SimpleDateFormat(ExportProperties.dtDayTimeFormat).format(asOfDate)));
        }
        cmd.addSelection(sessionID);
        Response response;
        try {
            response = imSession.executeCmd(cmd);
        } catch (InvalidCommandOptionException ex) {

            cmd.clearOptionList();
            cmd.addOption(new Option("substituteParams"));
            cmd.addOption(new Option("fields", "Type," + ExportProperties.fldText + "::rich," + ExportProperties.fldTestSteps));
            // 
            if (!queryID.isEmpty()) {
                cmd.addOption(new Option("queryDefinition", "(field[ID]=" + queryID + ")"));
            } else {
                cmd.addOption(new Option("AsOf", dfDayTime.format(asOfDate)));
            }
            cmd.addSelection(sessionID);
            response = imSession.executeCmd(cmd);
        }
        return response;
    }

    /**
     * Adds a single bar to the Defect List
     */
    public static void addBar(ExternalItem target, String fieldName, String fieldShortName, Float baseInt, int value) throws ItemMapperException {
        String barChar = "\u007C";
        int baseNum = 250;

        // add field and value if not empty
        if (fieldName != null) {
            target.getItemData().addField(fieldName, value);
        }
        // add bar
        target.add(fieldShortName, repeat(barChar, (int) (baseNum / baseInt * Float.valueOf(value))));
    }
}
