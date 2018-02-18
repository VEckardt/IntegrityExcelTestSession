/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ptc.services.common.gateway.html;

//import com.ptc.services.common.gateway.*;
//import com.mks.api.Command;
//import com.mks.api.Option;
//import com.mks.api.response.APIException;
//import com.mks.api.response.Response;
//import com.mks.api.response.WorkItem;
//import com.mks.api.response.WorkItemIterator;
//import com.mks.gateway.data.ExternalItem;
//import com.mks.gateway.data.ItemField;
//import com.mks.gateway.mapper.ItemMapperSession;
//import static com.ptc.services.common.api.IntegrityGatewayTools.log;
//import static com.ptc.services.common.utils.StringUtils.isEmpty;
//import com.ptc.services.gateway.exporter.word.mapping.FieldDef;
//import com.ptc.services.gateway.exporter.word.mapping.FieldMapping;

/**
 *
 * @author veckardt
 */
public class RelationshipsInHtml {

//    /**
//     * retrieves the traced requirements (Is Related To)
//     *
//     * @param fieldName
//     * @param traceDirection
//     * @param displayContentTraceFields
//     * @param displayContentTraceFieldsArr
//     * @param cmd
//     * @param runner
//     * @param currentChild
//     * @throws Exception
//     */
//    public static void addTraceInformation(
//            ItemMapperSession imSession,
//            String itemIDs,
//            FieldMapping fml,
//            ExternalItem currentChild, String asOf, String delimiter) throws Exception {
//
//        // get the items
//        WorkItemIterator cTR = getItemDetails2(imSession, itemIDs, fml.getFieldListInt(), asOf);
//
//        // construct the  html output
//        StringBuilder data = new StringBuilder("");
//        while (cTR.hasNext()) {
//            WorkItem contentTraceWI = cTR.next();
//            log("Got WorkItem " + contentTraceWI.getId() + " for Content Trace Item.", 2);
//            // getting all the fields defined in the properties..
//            for (FieldDef fld : fml.fields) {
//                log("Reading field from contentTrace Item: " + fld.fieldDisplayName, 2);
//                if (contentTraceWI.contains(fld.fieldName)) {
//                    data.append(data.length() == 0 ? "<div><p>" : delimiter);
//                    data.append("<u>" + fld.fieldDisplayName + "</u>").append(":&nbsp;").append(contentTraceWI.getField(fld.fieldName).getValueAsString());
//                }
//            }
//            data.append("</p>");
//            if (cTR.hasNext() && !delimiter.contentEquals(", ")) {
//                data.append("<br>");
//            }
//        }
//        data.append("</div>");
//        // currentChild.add(fieldName, contentInformation.toString());
//        currentChild.getItemData().addField(new ItemField.RichContent(fml.externalName, data.toString()));
//    }
//
//    /**
//     * retrieves the traced requirements (Is Related To)
//     *
//     * @param traceDirection
//     * @param runner
//     * @param currentChild
//     * @throws Exception
//     */
//    public static void addRelationshipTable(
//            ItemMapperSession imSession,
//            String itemIDs,
//            FieldMapping fml,
//            ExternalItem currentChild, String asOf) throws Exception {
//
//        // get the items
//        WorkItemIterator cTR = getItemDetails2(imSession, itemIDs, fml.getFieldListInt(), asOf);
//
//        // build and check the chields first
//        StringBuilder tableDataInformation = new StringBuilder();
//        while (cTR.hasNext()) {
//            WorkItem contentTraceWI = cTR.next();
//            // getting all the fields defined in the properties..
//            tableDataInformation.append("<tr>");
//            for (FieldDef fld : fml.fields) {
//                if (contentTraceWI.contains(fld.fieldName)) {
//                    tableDataInformation.append((!isEmpty(fld.style) ? "<td " + fld.style + ">" : "<td>"));
//                    String value = contentTraceWI.getField(fld.fieldName).getValueAsString();
//                    tableDataInformation.append((!isEmpty(value) ? value : "  "));
//                    tableDataInformation.append("</td>");
//                }
//            }
//            tableDataInformation.append("</tr>");
//        }
//
//        StringBuilder tableHeaderInformation = new StringBuilder();
//        // any data processed?
//        if (tableDataInformation.length() > 0) {
//
//            tableHeaderInformation = new StringBuilder("<div>");
//            tableHeaderInformation.append(!isEmpty(fml.tableStyle) ? "<table " + fml.tableStyle + ">" : "<table>");
//            tableHeaderInformation.append("<tr>");
//            // for (String column : fml.getFieldDisplayList()) {
//            for (FieldDef fld : fml.fields) {
//                // log(fml.tableHeaderStyle,5);
//                // IMPORTANT: we can not style <th> here, Word Converter will not allow, therefore we keep the td also for th
//                tableHeaderInformation.append((!isEmpty(fml.tableHeaderStyle) ? "<td " + fml.tableHeaderStyle + ">" : "<td>"));
//                tableHeaderInformation.append(fld.fieldDisplayName);
//                tableHeaderInformation.append("</td>");
//            }
//            tableHeaderInformation.append("</tr>");
//            tableHeaderInformation.append(tableDataInformation.toString());
//            tableHeaderInformation.append("</table></div>");
//
//        } else {
//            tableHeaderInformation.append(fml.ifEmpty);
//        }
//
//        // log (tableHeaderInformation.toString(),5);
//        // currentChild.add(fieldName, contentInformation.toString());
//        currentChild.getItemData().addField(new ItemField.RichContent(fml.externalName, ItemField.RichContentDataFormat.XHTML, tableHeaderInformation.toString()));
//    }
//
//    /**
//     * retrieves the traced requirements (Is Related To)
//     *
//     * @param traceDirection
//     * @param runner
//     * @param currentChild
//     * @throws Exception
//     */
//    public static void addRelationshipTableVertical(
//            ItemMapperSession imSession,
//            String itemIDs,
//            FieldMapping fml,
//            ExternalItem currentChild, String asOf) throws Exception {
//
//        // get the items
//        WorkItemIterator cTR = getItemDetails2(imSession, itemIDs, fml.getFieldListInt(), asOf);
//
//        StringBuilder data = new StringBuilder();
//        data.append(!isEmpty(fml.tableStyle) ? "<table " + fml.tableStyle + ">" : "<table>");
//        while (cTR.hasNext()) {
//            WorkItem contentTraceWI = cTR.next();
//            for (FieldDef fld : fml.fields) {
//                if (contentTraceWI.contains(fld.fieldName)) {
//                    data.append("<tr>");
//
//                    data.append((!isEmpty(fml.tableHeaderStyle) ? "<td " + fml.tableHeaderStyle + ">" : "<td>"));
//                    data.append(fld.fieldDisplayName);
//                    data.append("</td>");
//
//                    data.append((!isEmpty(fld.style) ? "<td " + fld.style + ">" : "<td>"));
//                    String value = contentTraceWI.getField(fld.fieldName).getValueAsString();
//                    data.append((!isEmpty(value) ? value : ""));
//                    data.append("</td>");
//
//                    data.append("</tr>");
//                }
//            }
//            if (cTR.hasNext()) {
//                // add another row to devide the item tables
//                data.append("<tr><td  bgcolor=white style='border-style:solid; border-color:white; border-width:1px;'></td></tr>");
//            }
//        }
//        data.append("</table>");
//
//        // Add to output stream
//        currentChild.getItemData().addField(new ItemField.RichContent(fml.externalName, ItemField.RichContentDataFormat.XHTML, data.toString()));
//    }
//
//    /**
//     * getItemDetails2
//     *
//     * @param imSession
//     * @param itemIDs
//     * @param fieldList
//     * @param asOf
//     * @return
//     * @throws APIException
//     */
//    public static WorkItemIterator getItemDetails2(ItemMapperSession imSession, String itemIDs, String fieldList, String asOf) throws APIException {
//        // constructing the command
//        Command cmd = new Command(Command.IM, "issues");
//        cmd.addOption(new Option("fields", fieldList));
//
//        for (String id : itemIDs.split(",")) {
//            cmd.addSelection(id);
//        }
//        if (asOf != null && !asOf.isEmpty()) {
//            log("Running command 'im issues' to get items with asOf Option: " + asOf, 3);
//            cmd.addOption(new Option("asOf", asOf));
//        }
//
//        // not possible here
//        // cmd.addOption(new Option("substituteParams"));
//        Response response = imSession.executeCmd(cmd);
//
//        // For all Related WorkItems
//        return response.getWorkItems();
//
//    }

    /**
     * recursive method that gathers certain information from the child elements
     *
     * @param ei
     * @param result
     * @param runner
     * @param paretoInScope
     * @param asOf
     */
//    public void generateChildsInfo(ItemMapperSession imSession, ExternalItem ei, List<String> result, String asOf) {
//        try {
//            if (!ei.getChildren().isEmpty()) {
//                // there are childs attached..
//
//                Iterator<ExternalItem> childs = ei.childrenIterator();
//                while (childs.hasNext()) {
//                    ExternalItem currentChild = (ExternalItem) childs.next();
//                    String nodeID = currentChild.getInternalId();
//                    log("--------------------------------------------------------------------", 2);
//                    log("----------------------------- (" + nodeID + ") -----------------------------------", 2);
//                    log("--------------------------------------------------------------------", 2);
//
//                    log("Item category is: " + getFieldValue(currentChild, "Category"), 2);
//                    // logger.message(CATEGORY, logger.LOW, "child: " +
//                    // currentChild.getField("Section").getStringValue() + ": "
//                    // + currentChild.toString());
//
//                    for (FieldMapping fml : mappingFile.getFieldMappingList()) {
//
//                        log("Criteria is: '" + fml.criteriaField + "' contains '" + fml.criteriaValue + "'?", 2);
//
//                        String currentValue = getFieldValue(currentChild, fml.criteriaField);
//                        if ((fml.criteriaValue.contains(currentValue))) {
//                            log("Found " + fml.criteriaField + " '" + currentValue + "', trying to get relationship data...", 2);
//
//                            // get relationship data
//                            WorkItemIterator wit = getItemDetails2(imSession, nodeID, fml.internalName, asOf);
//                            while (wit.hasNext()) {
//                                WorkItem workItem = wit.next();
//                                if (workItem.contains(fml.internalName)) {
//                                    String relatedNodeIDs = workItem.getField(fml.internalName).getValueAsString();
//
//                                    if (!isEmpty(relatedNodeIDs)) {
//                                        log("Related items via '" + fml.internalName + "' are: " + relatedNodeIDs, 2);
//                                        if (fml.type.contentEquals("table")) {
//                                            addRelationshipTable(imSession, relatedNodeIDs, fml, currentChild, asOf);
//                                        } else if (fml.type.contentEquals("tableVertical")) {
//                                            addRelationshipTableVertical(imSession, relatedNodeIDs, fml, currentChild, asOf);
//                                        } else if (fml.type.contentEquals("newlineDelimited")) {
//                                            addTraceInformation(imSession, relatedNodeIDs, fml, currentChild, asOf, "<br></br>");
//                                        } else { // default = if (fml.type.contentEquals("commaDelimited")) {
//                                            addTraceInformation(imSession, relatedNodeIDs, fml, currentChild, asOf, ", ");
//                                        }
//
//                                    } else {
//                                        currentChild.getItemData().addField(new ItemField.RichContent(fml.externalName, ItemField.RichContentDataFormat.XHTML, "<div>" + fml.ifEmpty + "</div>"));
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    generateChildsInfo(imSession, currentChild, result, asOf);
//                }
//                log("--------------------------------------------------------------------", 2);
//            }
//        } catch (Exception e) {
//            GatewayLogger.logError(e, 10);
//            if (result != null) {
//                result.add("Error in 'generateChildsInfo': " + e.getMessage());
//            }
//        }
//    }
}
