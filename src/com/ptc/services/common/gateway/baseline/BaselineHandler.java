/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ptc.services.common.gateway.baseline;

import com.mks.api.Command;
import com.mks.api.Option;
import com.mks.api.response.APIException;
import com.mks.api.response.Item;
import com.mks.api.response.Response;
import com.mks.api.response.WorkItem;
import com.mks.api.response.WorkItemIterator;
import com.mks.gateway.data.ExternalItem;
import com.mks.gateway.data.ItemField;
import com.mks.gateway.mapper.ItemMapperException;
import com.mks.gateway.mapper.ItemMapperSession;
import static com.ptc.services.common.gateway.LogAndDebug.log;
import com.ptc.services.common.gateway.html.ClientStyle;
import com.ptc.services.common.gateway.html.HtmlTable;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import mks.util.DateUtil;

/**
 *
 * @author veckardt
 */
public class BaselineHandler {

    public static final String dtDayFormat = DateUtil.DEFAULT_DATEONLY_FORMAT;
    public static SimpleDateFormat baseline_sdf = new SimpleDateFormat(dtDayFormat);

    /**
     * generates Baseline Information, and set's the necessary fields
     *
     * @param startingDocument
     * @param asOfDate
     * @throws ItemMapperException
     * @throws APIException
     */
    public static void addBaselineInformation(ItemMapperSession imSession, ExternalItem startingDocument, Date asOfDate) throws ItemMapperException, APIException {

        String startingDocumentID = startingDocument.getInternalId();

        log("Adding Document Baseline Details ...", 2);

        Command cmd = new Command(Command.IM, "viewissue");
        cmd.addOption(new Option("showLabels"));
        cmd.addSelection(startingDocumentID);

        Response respViewDocument = imSession.executeCmd(cmd);
        WorkItemIterator witDoc = respViewDocument.getWorkItems();
        List<Baseline> baselineList = new LinkedList<Baseline>();
        while (witDoc.hasNext()) {
            WorkItem workItem = witDoc.next();
            if (workItem.contains("MKSIssueLabels")) {
                log("Got WorkItem representation of document " + startingDocumentID + ", trying to get the baseline information", 3);
                List<?> baselines = workItem.getField("MKSIssueLabels").getList();
                for (Object object : baselines) {
                    Item item = (Item) object;
                    Baseline tmp = new Baseline(item);

                    baselineList.add(tmp);
                }
            } else {
                log("NOTE: No baselines found for document " + startingDocumentID, 3);
            }
        }
        if (!baselineList.isEmpty()) {
            // baseline(s) found
            StringBuilder baselineSB = new StringBuilder();
            // iterate over baselines to get check if the date that was entered
            // by the user is a valid baseline date
            for (Baseline baseline : baselineList) {
                log("Got baseline " + baseline.toString(), 3);
                // if (baseline.getAsof().equalsIgnoreCase(asOf_gateway)) {
                // getAsOfDate
                if (baseline.getAsOfDate().compareTo(asOfDate) == 0) {
                    // there is a baseline with the same date the user entered..
                    // keep in mind that there might be another baseline with
                    // the same date
                    baselineSB.append((baselineSB.length() == 0 ? "" : ", "));
                    baselineSB.append(baseline.getLabel());
                }
            }

            // baselineSB
            log("AS-Document Version: " + baselineSB.toString(), 3);
            if (baselineSB.length() > 0) {
                startingDocument.add("Current Label", baselineSB.toString());
            } else {
                // TODO check where this validation needs to be done..
                // no baseline found
                startingDocument.add("Current Label", "This document does not match an offical Version");
            }
        } else {
            // no baseline found
            startingDocument.add("Current Label", "This document does not match an offical Version");
        }// still TODO wait for asof=now patch..then some checks are needless

        // sorting baselines as we also need to build some kind of baseline
        // history to print to the document.
        log("Sorting baselines ...", 3);
        Collections.sort(baselineList);

        // Generate Baseline Table
        HtmlTable historyTable = new HtmlTable();
        historyTable.setHeader(Arrays.asList("<b>Version</b>", "<b>Date</b>"));
        for (Baseline baseline : baselineList) {
            log("Baseline: " + baseline.toString(), 3);
            historyTable.addDataRow(Arrays.asList(baseline.getLabel(), baseline_sdf.format(baseline.getAsOfDate())));
        }
        startingDocument.getItemData().addField(
                new ItemField.RichContent("Baselines", ItemField.RichContentDataFormat.XHTML, "<div>" + historyTable.render(ClientStyle.getInstance()) + "</div>"));
        log("End of Adding Document Baseline Details ...", 2);
    }
}
