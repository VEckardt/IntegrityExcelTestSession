/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ptc.services.common.gateway;

import com.ptc.services.common.gateway.tracedefinition.TraceDefinitionList;
import com.ptc.services.common.gateway.tracedefinition.TraceDefinition;
import com.mks.api.response.APIException;
import com.mks.api.response.WorkItem;
import com.mks.gateway.data.ExternalItem;
import com.mks.gateway.data.ItemField;
import com.mks.gateway.driver.IIntegrationDriver;
import com.mks.gateway.mapper.ItemMapperException;
import com.mks.gateway.mapper.ItemMapperSession;
import com.mks.gateway.mapper.config.ItemMapperConfig;
import static com.ptc.services.common.config.Config.dfDayTimeShort;
import static com.ptc.services.common.config.ExportProperties.fldTestsAsOfDate;
import static com.ptc.services.common.gateway.ItemHandler.getNewItem;
import static com.ptc.services.common.gateway.ItemHandler.isValidItem;
import static com.ptc.services.common.gateway.ItemHandler.replaceMKSItemId;
import static com.ptc.services.common.gateway.ItemHandler.replaceRefId;
import static com.ptc.services.common.gateway.ItemHandler.setBookmarkForField;
import static com.ptc.services.common.gateway.LogAndDebug.log;
import static com.ptc.services.common.gateway.PrefixingHandler.addIdPrefix;
import com.ptc.services.common.tm.TestResult;
import com.ptc.services.common.gateway.tm.TestUtils;
import static com.ptc.services.common.gateway.tm.TestUtils.addLastResultToTestCase;
import static com.ptc.services.common.gateway.tm.TestUtils.testSessionMap;
import com.ptc.services.common.tools.EnvUtil;
import static com.ptc.services.common.tools.StringUtils.isEmpty;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author veckardt
 */
public class DocNodeIterators {

    public static Boolean containsReferencesToReference = false;
    private static final String listOfColumns = EnvUtil.getListOfColumns();
    private static ItemMapperSession imSession;
    private static ItemMapperConfig mappingConfig;
    private static ItemMapperConfig customMappingConfig;
    private static IIntegrationDriver.IGatewayDriver iGatewayDriver;
    private static int level = 0;
    private static TraceDefinitionList traceDefList;
    private static final String fieldPrefix = "Fld";
    public static ArrayList<ExternalItem> childList = new ArrayList<ExternalItem>();
    private static int[] sectionList = new int[20]; // should be good enought

    private static final String contentLevel = "ContentLevel";

    private DocNodeIterators() {
        // this will avoid that someone innitiates this class
    }

    private static String getType(ExternalItem item) throws ItemMapperException {
        return item.getValueAsString("Type");
    }

    private static String getLevel(ExternalItem item) throws ItemMapperException {
        return item.getValueAsString(contentLevel);
    }

    /**
     *
     * @param imSession
     * @param mappingConfig
     */
    public static void init(ItemMapperSession imSession,
            IIntegrationDriver.IGatewayDriver iGatewayDriver,
            ItemMapperConfig mappingConfig,
            ItemMapperConfig customMappingConfig,
            TraceDefinitionList traceDefList) {
        DocNodeIterators.imSession = imSession;
        DocNodeIterators.iGatewayDriver = iGatewayDriver;
        DocNodeIterators.mappingConfig = mappingConfig;
        DocNodeIterators.customMappingConfig = customMappingConfig;
        DocNodeIterators.traceDefList = traceDefList;
    }

    public static void setLevel(int newLevel) {
        level = newLevel;
    }

    /**
     * Rebuild the new document list by flattening the structure and adding the
     * traced items right in between
     */
//    public static void buildNewDocList(ExternalItem source, ExternalItem target, Date asOfDate) throws ItemMapperException, APIException {
//
//        // ArrayList<ExternalItem> array = new ArrayList<ExternalItem>();
//        debug("Level-" + level + ": In IterateChilds ...", level + 1);
//        // does this item have any children?
//        if (!source.getChildren().isEmpty()) {
//            // walk through all childs
//            Iterator<ExternalItem> childs = source.childrenIterator();
//            level++;
//
//            while (childs.hasNext()) {
//                ExternalItem currentChild = (ExternalItem) childs.next();
//                // String internalId = currentChild.getInternalId();
//
//                // As we are flattening the structure, we would have to add another id for the level
//                // based on this, the template will also to be changed
//                currentChild.add(contentLevel, level);
//                target.addChild(currentChild);
//
//                for (String key : traceDefList.keySet()) {
//                    // for (String traceDef : traceDefinition.split(",")) {
//                    if (getType(currentChild).contentEquals(key)) {
//                        ItemField traceField = currentChild.getField(traceDefList.get(key));
//                        if (traceField != null && traceField.hasValue()) {
//                            String traceIDs = traceField.getStringValue();
//
//                            int num = 0;
//                            for (String traceID : traceIDs.split(",")) {
//                                debug("Adding one more item with ID " + traceID + " ...", level + 1);
//                                ExternalItem item = getNewItem(imSession, mappingConfig, iGatewayDriver, traceID, asOfDate, null);
//                                item.add(contentLevel, level + 1);
//                                item.add("Order", ++num);
//                                // log("Adding Test Case " + valItemID + "", 2);
//                                target.addChild(item);
//                            }
//                        }
//                    }
//                }
//                // setBookmarkForField(currentChild, "ID", containsReferencesToReference);
//
//                // drill down to the childs now
//                buildNewDocList(currentChild, target, asOfDate);
//            }
//            level--;
//            // log("source.getChildren().size(): " + source.getChildren().size(), 2);
//
//        } else {
//            debug("Level-" + level + ": No childs.", level + 1);
//        }
//
//        debug("Level-" + level + ": End IterateChilds.", level + 1);
//    }
    /**
     * Main Item Handler for a document
     *
     * it calls itself recursively we look for childs, create a copy, and pussle
     * the additional trace data in (if any)
     */
    public static void iterateChildsDynamicDocs(ExternalItem source, ExternalItem target, CustomMapperConfig customMapper, Date asOfDate) throws ItemMapperException, APIException {

        log("C-1: In iterateChildsDynamicDocs ...", 1);
        // does this item have any children?
        if (!source.getChildren().isEmpty()) {
            // walk through all childs
            Iterator<ExternalItem> childs = source.childrenIterator();
            while (childs.hasNext()) {
                ExternalItem currentChild = (ExternalItem) childs.next();
                String section = currentChild.getValueAsString("Section");
                // debug("C-2: Adding Child " + currentChild.getValueAsString("Type") + " " + currentChild.getInternalId() + " to " + target.getValueAsString("Type") + " " + target.getInternalId() + " ... ", 2);

                // if (docNodeIDs.containsKey(currentChild.getInternalId()))
                ExternalItem item = ItemHandler.getNewItem(imSession, customMappingConfig, iGatewayDriver, currentChild.getInternalId(), asOfDate, null);

                int i = 0;
                // log("listOfColumns => " + listOfColumns, 2);
                for (String fieldName : listOfColumns.split(",")) {
                    i++;
                    // log("fieldName => " + fieldName, 2);
                    if (fieldName.contentEquals("Section")) {
                        item.add(fieldPrefix + i, section);
                        // } else if (fieldName.contentEquals("Text")) {
                    } else if (customMapper.isRichContent(fieldName)) {
                        item.getItemData().addField(new ItemField.RichContent(fieldPrefix + i, currentChild.getValueAsString(fieldName)));
                    } else {
                        item.add(fieldPrefix + i, item.getField(fieldName).getStringValue());
                    }
                }
                item.add("numFields", i);

                target.addChild(item);

                iterateChildsDynamicDocs(currentChild, target, customMapper, asOfDate);

            }
        } else {
            log("no childs.", 2);
        }

        log("End iterateChildsDynamicDocs.", 1);
    }

    /**
     * Main Item Handler for a document
     *
     * it calls itself recursively we look for childs, create a copy, and pussle
     * the additional trace data in (if any)
     */
//    public static void iterateChilds2(ExternalItem source, ExternalItem target, Date asOfDate, String relRmDocId) throws ItemMapperException, APIException {
//
//        debug("I-1: In IterateChilds ...", 1);
//        // does this item have any children?
//        if (!source.getChildren().isEmpty()) {
//            // walk through all childs
//            Iterator<ExternalItem> childs = source.childrenIterator();
//            while (childs.hasNext()) {
//                ExternalItem currentChild = (ExternalItem) childs.next();
//                String currentType = currentChild.getValueAsString("Type");
//
//                ExternalItem ein = (ExternalItem) currentChild.cloneNode(false);
//                target.addChild(ein);
//
//                ExternalItem sectionItem2 = new ExternalItem("CONTENT", currentChild.getInternalId() + "1");
//                sectionItem2.add("Type", "Test Case");
//                sectionItem2.add("Category", "Heading");
//                sectionItem2.add("Text", "iterateChilds Combined Output Document");
//                // ei.insertBefore(ei.getFirstChild(), sectionItem2);
//
//                int i = 0;
//                if (currentChild.getChildren().size() > 0) {
//
//                    ArrayList<ExternalItem> al = new ArrayList<ExternalItem>();
//                    Iterator<ExternalItem> ie = currentChild.childrenIterator();
//
//                    while (ie.hasNext()) {
//                        log(currentType + ", " + currentChild.getId().getInternalID() + ", Step HN" + (++i), 4);
//                        ExternalItem ei = (ExternalItem) ie.next();
//                        al.add(ei);
//                    }
//
//                    currentChild.removeAllChildren();
//                    // currentChild.addChild(sectionItem2);
//                    al.add(sectionItem2);
//
//                    for (ExternalItem ae : al) {
//                        log(currentType + ", " + currentChild.getId().getInternalID() + ", Step EI" + (++i), 4);
//
//                        target.addChild(ae);
//                    }
//                }
//                iterateChilds2(currentChild, ein, asOfDate, relRmDocId);
//
//            }
//        } else {
//            debug("no childs.", 2);
//        }
//
//        debug("End IterateChilds.", 1);
//    }
    /**
     * Main Item Handler for a document
     *
     * it calls itself recursively we look for childs, create a copy, and pussle
     * the additional trace data in (if any)
     */
    public static void iterateChilds(ExternalItem source, Date asOfDate, Boolean addTestResults) throws ItemMapperException, APIException {

        debug("Level-" + level + ": In IterateChilds ...", level + 1);

        // does this item have any children?
        if (!source.getChildren().isEmpty()) {
            // walk through all childs
            level++;
            Iterator<ExternalItem> childs = source.childrenIterator();
            while (childs.hasNext()) {
                ExternalItem currentChild = (ExternalItem) childs.next();
                // String currentType = currentChild.getField("Type").getStringValue();

                addIdPrefix(currentChild);
                // setBookmarkForField(currentChild, "ID", getTypeShortName(currentType), containsReferencesToReference);
                currentChild.getItemData().addField(contentLevel, level);
                childList.add(currentChild);
                iterateChilds(currentChild, asOfDate, addTestResults);

                // handle the test case type
                if (addTestResults && TestUtils.isTestCase(imSession, currentChild)) {
                    addLastResultToTestCase(imSession, iGatewayDriver, currentChild, mappingConfig, asOfDate);
                }
            }
            level--;
        } else {
            debug("no childs.", 2);
        }

        debug("End IterateChilds.", 1);
    }

    /**
     * addRelatedItems
     *
     * @param listRoot
     * @throws ItemMapperException
     * @throws APIException
     */
    public static void addRelatedItems(ExternalItem listRoot, Date asOfDate) throws ItemMapperException, APIException {
        int cpos = 0;
        // handleRoot Item separately
        for (TraceDefinition traceDef : traceDefList) {
            if (getType(listRoot).contentEquals(traceDef.getTypeName())) {

                String idList = listRoot.getValueAsString(traceDef.getTraceField());
                log("Adding Item Information for Trace " + traceDef.getTraceField() + ": " + idList, 1);

                if (!isEmpty(idList)) {
                    log("idList: " + idList, 2);
                    for (String itemId : idList.split(",")) {

                        ExternalItem item = getNewItem(imSession, mappingConfig, iGatewayDriver, itemId, asOfDate, null);
                        if (isValidItem(item, traceDef.getTraceFilter())) {
                            item.add(contentLevel, "1");
                            childList.add((cpos++), item);
                        }
                    }
                }
            }
        }
    }

    /**
     * handleChildList
     *
     * @param listRoot
     * @throws ItemMapperException
     * @throws APIException
     */
    public static void rebuildChildListWithTraces(ExternalItem listRoot, Boolean addTestResults) throws ItemMapperException, APIException {

        // only perform something if the trace definition is set
        if (!traceDefList.isEmpty()) {

            log("Starting rebuildChildListWithTraces ... ", 1);

            // count the number of items in memory
            log("Initial Item List size: " + childList.size(), 1);
            for (int i = 0; i < childList.size(); i++) {
                childList.get(i).removeAllChildren();
            }

            // clean the list, to rebuild
            listRoot.removeAllChildren();

            // handleRoot Item separately
            addRelatedItems(listRoot, new Date());
            // for all items in list
            for (int i = 0; i < childList.size(); i++) {

                ExternalItem childItem = childList.get(i);
                if (childItem.isContentItem()) {
                    // for each tracing set, such as Test Case > Test Step
                    for (TraceDefinition traceDef : traceDefList) {

                        // is the current item type equal to the trace type?
                        if (getType(childItem).contentEquals(traceDef.getTypeName())) {

                            // get all trace id's from defined field
                            String idList = childItem.getValueAsString(traceDef.getTraceField()) + "";
                            String newLevel = childItem.getValueAsString(contentLevel);

                            String sessionID = "";

                            Date asOfDate = new Date();

                            // check, update the step list with the session date
                            if (addTestResults && TestUtils.isTestCase(imSession, childItem)) {

                                sessionID = childItem.getValueAsString("Session ID") + "";
                                ExternalItem session = testSessionMap.get(sessionID);
                                if (session != null) {

                                    try {
                                        // retrieve the as of date
                                        String sessionAsOF = session.getValueAsString(fldTestsAsOfDate) + "";
                                        if (!sessionAsOF.isEmpty()) {
                                            asOfDate = dfDayTimeShort.parse(sessionAsOF);
                                            WorkItem wi = ItemHandler.getWorkItemAsOf(imSession, childItem.getInternalId(), traceDef.getTraceField(), asOfDate);
                                            // if (!asOfTraceList.contentEquals(idList)) {
                                            //     log("ERROR: Trace lists differ: " + asOfTraceList + " vs. " + idList, 1);
                                            idList = wi.getField(traceDef.getTraceField()).getValueAsString();
                                            // }
                                        }
                                    } catch (ParseException ex) {
                                        Logger.getLogger(DocNodeIterators.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                                        asOfDate = new Date();
                                    }
                                }
                            }

                            // any traces set?
                            if (!idList.isEmpty()) {
                                log("Adding for " + childItem.getInternalId() + " from relationship " + traceDef.getTraceField() + ": " + idList + " ...", 2);
                                int cnt = 1;
                                // for all trace ids in the list
                                for (String itemId : idList.split(",")) {

                                    ExternalItem item = getNewItem(imSession, mappingConfig, iGatewayDriver, itemId, asOfDate, null);

                                    // only if test cases set
                                    if (addTestResults && TestUtils.isTestCase(imSession, childItem)) {
                                        // sessionID = childList.get(i).getValueAsString("Session ID") + "";
                                        // only if session set
                                        if (!sessionID.isEmpty()) {
                                            // get test results for steps and store them
                                            TestResult trs = new TestResult(imSession, "stepresults", sessionID + ":" + childItem.getId().getInternalID() + ":" + itemId);
                                            trs.addTestResultData(item);
                                        }
                                    }

                                    // add the type prefix gobally 
                                    addIdPrefix(item);
                                    // store also the current level in the new item, wich is one level below
                                    item.add(contentLevel, Integer.parseInt(newLevel) + 1);
                                    // and put it at the end of the current item childs'
                                    childList.add(i + (cnt++), item);
                                    // i++;
                                }
                            }
                        }
                    }
                }
            }

            log("Final List size after adding related items: " + childList.size(), 1);

            // String compLevel = "1";
            for (int j = 0; j < childList.size(); j++) {
                String newLevel = getLevel(childList.get(j));

                if (newLevel == null || newLevel.isEmpty()) {
                    log("ERROR! ERROR", 2);
                }
                // log(i + ": newLevel: '" + newLevel + "'", 1);
                // vermerke das aktuell letzte Item auf dem aktuellen Level
                sectionList[Integer.parseInt(newLevel)] = j;

                // wenn root, dann aktuelles Item Root zuordnen
                if (newLevel.contentEquals("1")) {
                    listRoot.addChild(childList.get(j));
                    //     compLevel = "1";
                    // } else if (!compLevel.contentEquals(newLevel)) {
                    //     childList.get(i - 1).addChild(childList.get(i));
                    //     compLevel = newLevel;
                } else {
                    // listRoot.addChild(childList.get(i));
                    childList.get(sectionList[Integer.parseInt(newLevel) - 1]).addChild(childList.get(j));
                }
            }
            log("Finished rebuildChildListWithTraces. ", 1);

        }
    }

    private void handleBookmarksAndRefs(ExternalItem externalItem) throws ItemMapperException {
        // setting the bookmarks for the reference types only
        String currentType = getType(externalItem);

        if (currentType.contentEquals("Reference")) {
            log("** Checking Reference for Bookmarks ...", 1);
            setBookmarkForField(externalItem, "Text Key", "REF", containsReferencesToReference);
        }
        if (containsReferencesToReference) {
            // ** Checking Requirement & Specifications only ...
            if (currentType.contentEquals("Requirement") || currentType.contentEquals("Specification")) {
                replaceRefId(externalItem, "Text");
                replaceMKSItemId(externalItem, "Text");
            }
        }
        if (currentType.contentEquals("Requirement")
                || currentType.contentEquals("Input")
                || currentType.contentEquals("Specification") // || currentType.contentEquals("Test Case")
                ) {
            replaceRefId(externalItem, "Satisfies");
            replaceRefId(externalItem, "Models");
            replaceRefId(externalItem, "Satisfied By");
            // replaceRefId(currentChild, "Text");
        }
    }

    /**
     * Drill Down
     *
     * This is the routine to parse the trace fields, and add appropriate item
     * data
     */
//    private void drillDown(List<RelationshipEntry> reList, ExternalItem copyItem, ExternalItem target, Date asOfDate, String relRmDocId) throws UnsupportedPrototypeException, APIException, ItemMapperException {
//        // for all re entries
//        debug("D-Start: In DrillDown ... ", 3);
//
//        for (RelationshipEntry re : reList) {
//
//            // valdiate, if a related item exists
//            String relFieldValue = getFieldValue(copyItem, re.getInternal()); // re.getExternal());
//            // debug("In Adding related Items loop '" + re.getExternal() + "' ...", 4);
//            if (!isEmpty(relFieldValue)) {
//                debug("D-2: Found related item(s), validating " + relFieldValue + " ...", 4);
//                // for all related items, if multiple
//                int validItemsCounter = 0;
//                for (String id : relFieldValue.split(",")) {
//                    //
//                    // Version 1
//                    // ExternalItem relatedItem = getExternalItem(imSession, re.getExternal(), id, re, asOf);
//                    // 
//                    ExternalItem relatedItem = ItemHandler.getNewItem(imSession, mappingConfig, iGatewayDriver, id, asOfDate, null);
//                    if (!isEmpty(relRmDocId)) {
//                        // nur das ist Siemens !!
//                        String itemDocID = relatedItem.getField("Document ID").getStringValue();
//                        if (itemDocID.contentEquals(relRmDocId)) {
//
//                            // add also this additional item to the target level
//                            if (isValidItem(relatedItem, re.getCriteriaSet())) {
//                                debug("D-7: Adding Child ..." + relatedItem.getValueAsString("Text"), 8);
//                                debug("D-8: To Target ..." + target.getValueAsString("Text"), 8);
//                                // target.
//
//                                replaceRefId(relatedItem, "Text");
//                                replaceMKSItemId(relatedItem, "Text");
//                                setBookmarkForField(relatedItem, "ID", containsReferencesToReference);
//                                replaceRefId(relatedItem, "Satisfied By");
//
//                                target.addChild(relatedItem);
//
//                                // not used for Siemens
//                                // here we have only one level
//                                // drillDown(mappingConfig, re.getRelationshipEntryList(), relatedItem, target, asOfDate, relRmDocId);
//                                validItemsCounter++;
//                            }
//                        } else {
//                            debug("Skipping RM Item with ID " + id + ", because it doesn't belong to the document " + relRmDocId + ".", 1);
//                        }
//
//                    } else {
//                        // das ist nicht Siemens
//                        // add also this additional item to the target level
//                        if (isValidItem(relatedItem, re.getCriteriaSet())) {
//                            target.addChild(relatedItem);
//                            drillDown(re.getRelationshipEntryList(), relatedItem, target, asOfDate, relRmDocId);
//                            validItemsCounter++;
//                        }
//                    }
//                }
//                log(validItemsCounter + " related item(s) added that passed the criteria '" + re.getCriteriaSet() + "'.", 4);
//            }
//        }
//        debug("D-End: Of DrillDown. ", 3);
//    }
    private static void debug(String text, int level) {
        log(text, level);
    }

}
