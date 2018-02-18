/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ptc.services.common.gateway.tm;

import com.mks.api.response.WorkItem;
import com.mks.gateway.data.ExternalItem;
import com.mks.gateway.mapper.ItemMapperException;
import com.mks.gateway.mapper.UnsupportedPrototypeException;
import com.ptc.services.common.config.ExportProperties;
import com.ptc.services.common.tm.TestResult;
import static com.ptc.services.common.gateway.LogAndDebug.log;
import static com.ptc.services.common.gateway.PrefixingHandler.addIdPrefix;
import static com.ptc.services.common.gateway.tm.TestUtils.addBar;
import static com.ptc.services.common.gateway.tm.TestUtils.addSeparateDefectSection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author veckardt
 */
public class TestDefects {

    private static int defectTotalCount = 0;
    private static int defectPendingCount = 0;
    private static int defectOpenCount = 0;
    private static int defectClosedCount = 0;

    public static HashMap<String, ExternalItem> defectMap = new LinkedHashMap<String, ExternalItem>();

    /**
     * Counts the defects by phase
     */
    public static void countDefectsByPhase(ExternalItem defect) throws ItemMapperException {
        String phase = defect.getField(ExportProperties.fldTaskPhase).getStringValue();
        defect.add("Summary", "not available");
        defect.add("Type", "Defect");

        // this information is not available at all for redmine
        if (phase.contentEquals(ExportProperties.taskPhasePending)) {
            defectPendingCount++;
        } else if (phase.contentEquals(ExportProperties.taskPhaseOpen)) {
            defectOpenCount++;
        } else if (phase.contentEquals(ExportProperties.taskPhaseClosed)) {
            defectClosedCount++;
        }
        defectTotalCount++;
    }

    /**
     * Adds the Defect List incl. Header to the target list
     */
    public static void addDefectSection(ExternalItem target) throws UnsupportedPrototypeException, ItemMapperException {
        // If enabled, add again the list of defects
        if (defectMap.size() > 0 && addSeparateDefectSection) {

            ExternalItem sectionItem = new ExternalItem("ISSUE", "DEFECTS");
            sectionItem.add("Summary", "Defects");
            sectionItem.add("Type", "Defect Section");
            sectionItem.add("Defect Count", defectMap.size());

            for (Map.Entry entry : defectMap.entrySet()) {
                ExternalItem ei = (ExternalItem) entry.getValue();
                addIdPrefix(ei);
                ei.add("Test Case", "TC-" + entry.getKey().toString().split(":")[0]);
                sectionItem.addChild(ei);
            }
            target.addChild(sectionItem);
        }
    }

    /**
     * addDefectTotal
     */
    public static void addDefectTotal(ExternalItem target) throws ItemMapperException {
        // Generate Bar Chart for Defects
        Float defTotalCount = Float.valueOf(defectTotalCount);

        addBar(target, "Defect Total Count", "DeTCBar", defTotalCount, defectTotalCount);
        addBar(target, "Defect Pending Count", "DePCBar", defTotalCount, defectPendingCount);
        addBar(target, "Defect Open Count", "DeOCBar", defTotalCount, defectOpenCount);
        addBar(target, "Defect Closed Count", "DeCCBar", defTotalCount, defectClosedCount);
    }

    public static void handleRedmineIDs(WorkItem workItem, TestResult trc) {

        String redmineIds = trc.getRedmineIDs();
        if (redmineIds.length() > 0) {
            log("Handling Redmine as Defects with values: " + redmineIds, 1);
            // log("redmineIds: " + redmineIds, 1);
            for (String redmineId : redmineIds.split(",")) {
                try {
                    // log("redmineId 1: '" + redmineIds + "'", 1);
                    ExternalItem redmineItem = new ExternalItem("ISSUE", redmineId);
                    redmineItem.add("ID", redmineId);
                    redmineItem.add("Type", "Defect");
                    redmineItem.add("State", "Unknown");
                    redmineItem.add("Summary", "Unknown");

                    // log("redmineId 2: '" + redmineIds + "'", 1);
                    if (addSeparateDefectSection) {
                        defectMap.put(workItem.getId() + ":" + redmineId, redmineItem);
                    }
                    // log("redmineId 3: '" + redmineIds + "'", 1);
                    defectTotalCount++;
                    // this line causes a stack overflow issue!
                    // testCase.addChild(redmineItem);
                    // log("redmineId 4: '" + redmineIds + "'", 1);
                } catch (UnsupportedPrototypeException ex) {
                    Logger.getLogger(TestDefects.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                } catch (ItemMapperException ex) {
                    Logger.getLogger(TestDefects.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        }
    }
}
