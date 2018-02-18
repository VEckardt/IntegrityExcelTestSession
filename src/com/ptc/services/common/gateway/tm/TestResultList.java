/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ptc.services.common.gateway.tm;

import com.mks.api.Command;
import com.mks.api.Option;
import com.mks.api.response.APIException;
import com.mks.api.response.Response;
import com.mks.api.response.WorkItem;
import com.mks.api.response.WorkItemIterator;
import com.mks.gateway.mapper.ItemMapperSession;
import com.ptc.services.common.api.ExceptionHandler;
import com.ptc.services.common.tm.TestResult;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author veckardt
 */
public class TestResultList extends ArrayList<TestResult> {

    List<String> issueIds = new LinkedList<String>();

    public TestResultList() {

    }

    public TestResultList(ItemMapperSession imSession, String selection, Boolean lastResultOnly) {

        // retrieve all test results for the case
        try {
            Command cmd = new Command(Command.TM, "results");
            cmd.addOption(new Option("fields", "Session ID"));
            if (lastResultOnly) {
                cmd.addOption(new Option("lastresult"));
            }
            cmd.addOption(new Option("caseID", selection));
            Response response = imSession.executeCmd(cmd);

            WorkItemIterator wit = response.getWorkItems();
            while (wit.hasNext()) {
                WorkItem wi = wit.next();
                issueIds.add(wi.getField("sessionID").getValueAsString() + ":" + selection);
                this.add(new TestResult(imSession, "viewresult", wi.getField("sessionID").getValueAsString() + ":" + selection));
            }
// this does not work :(  MKS124814: Cannot show view information: All results specified in the selection must be from the same session [11ms]
//            cmd = new Command(Command.TM, "viewresult");
//            for (String issue : issueIds) 
//                cmd.addSelection(issue);
//            response = imSession.executeCmd(cmd);            
//            wit = response.getWorkItems();
//            while (wit.hasNext()) {
//                WorkItem wi = wit.next();
//                this.add(new TestResult(wi));
//            }

        } catch (APIException ex) {
            ExceptionHandler eh = new ExceptionHandler(ex);
            System.out.println(eh.getMessage());
        }

    }

    public void addStepResults(
            String testCaseID,
            String testStepID,
            String verdict,
            String annotation) {
        super.add(new TestResult(testCaseID, testStepID, verdict, annotation));
    }

    public TestResult getLastTestResult() {
        return this.size() > 0 ? this.get(0) : null;
    }

    private static class SortAscending implements Comparator<TestResult> {

        @Override
        public int compare(TestResult o1, TestResult o2) {
            return o1.getModifiedDate().compareTo(o2.getModifiedDate());
        }
    }

    private static class SortDescending implements Comparator<TestResult> {

        @Override
        public int compare(TestResult o1, TestResult o2) {
            return o2.getModifiedDate().compareTo(o1.getModifiedDate());
        }
    }
}
