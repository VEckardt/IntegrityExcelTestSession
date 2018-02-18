/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ptc.services.common.gateway;

import com.mks.api.Command;
import com.mks.api.Option;
import com.mks.api.response.APIException;
import com.mks.api.response.Response;
import com.mks.api.response.WorkItem;
import com.mks.api.response.WorkItemIterator;
import com.mks.gateway.mapper.ItemMapperSession;
import static com.ptc.services.common.api.IntegrityAPI.getTypeShortName;
import static com.ptc.services.common.gateway.LogAndDebug.log;
import static com.ptc.services.common.gateway.PrefixingHandler.addIdPrefix;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 *
 * @author veckardt
 */
public class DocHandler {

    // imSession.getAPIRequestVersion().getMinor() >= 13
    private static Boolean supportVersionedItems = true;
    private static Map<String, WorkItem> itemMap = new HashMap<String, WorkItem>();

    /**
     * Returns the Trace Id's such as Validates or Verifies and adds the Type
     * Short Name in front
     *
     * @param imSession
     * @param traceIDs
     * @return
     */
//    public static String getTraceIDsEnh_old(ItemMapperSession imSession, String traceIDs) {
//
//        String ret = "";
//        if (traceIDs != null && !traceIDs.trim().isEmpty()) {
//            for (String id : traceIDs.split(",")) {
//                WorkItem wi = null;
//                if (itemMap.containsKey(id)) {
//                    wi = itemMap.get(id);
//                } else {
//                    try {
//                        Command cmd = new Command(Command.IM, "issues");
//                        String fields = "ID,Type";
//                        if (supportVersionedItems) {
//                            cmd.addOption(new Option("includeVersionedItems"));
//                            fields = fields + ",Minor Version ID,Major Version ID,Live Item ID";
//                        }
//                        cmd.addOption(new Option("fields", fields));
//                        cmd.addSelection(id);
//                        Response response = imSession.executeCmd(cmd);
//
//                        // ResponseUtil.printResponse(response, 1, System.out);
//                        wi = response.getWorkItem(id);
//                        itemMap.put(id, wi);
//                    } catch (APIException ex) {
//                        wi = null;
//                    } catch (NoSuchElementException ex) {
//                        wi = null;
//                    }
//                }
//                if (wi != null) {
//                    if (supportVersionedItems) {
//                        if (!wi.getField("Live Item ID").getValueAsString().contentEquals(id)) {
//                            id = wi.getField("Live Item ID").getValueAsString() + "-" + wi.getField("Major Version ID").getValueAsString() + "." + wi.getField("Minor Version ID").getValueAsString();
//                        }
//                    }
//                    String typeName = wi.getField("Type").getValueAsString();
//                    DocNodeIterators.addIdPrefix(typeName);
//                    ret = ret + (ret.isEmpty() ? "" : ", ") + getTypeShortName(typeName) + "-" + id;
//                }
//            }
//        }
//        return ret;
//    }

    public static String getTraceIDsEnh(ItemMapperSession imSession, String traceIDs) {

        String ret = "";
        String idList = "";
        if (traceIDs != null && !traceIDs.trim().isEmpty()) {
            // build a list of items not yet in memory
            for (String id : traceIDs.split(",")) {
                WorkItem wi = null;
                if (!itemMap.containsKey(id)) {
                    // wi = itemMap.get(id);
                    idList = idList + (idList.isEmpty() ? "" : ",") + id;
                }
            }
            // load all remaining items
            if (!idList.isEmpty()) {
                try {
                    Command cmd = new Command(Command.IM, "issues");
                    String fields = "ID,Type";
                    if (supportVersionedItems) {
                        cmd.addOption(new Option("includeVersionedItems"));
                        fields = fields + ",Minor Version ID,Major Version ID,Live Item ID";
                    }
                    cmd.addOption(new Option("fields", fields));
                    for (String id : idList.split(",")) {
                        cmd.addSelection(id);
                    }
                    Response response = imSession.executeCmd(cmd);

                    // ResponseUtil.printResponse(response, 1, System.out);
                    WorkItemIterator wit = response.getWorkItems();
                    while (wit.hasNext()) {
                        WorkItem wi = wit.next();
                        itemMap.put(wi.getId(), wi);
                    }
                } catch (APIException ex) {
                } catch (NoSuchElementException ex) {
                }
            }
            // now build the user friendly item list
            for (String id : traceIDs.split(",")) {
                WorkItem wi = itemMap.get(id);
                if (supportVersionedItems) {
                    if (!wi.getField("Live Item ID").getValueAsString().contentEquals(id)) {
                        id = wi.getField("Live Item ID").getValueAsString() + "-" + wi.getField("Major Version ID").getValueAsString() + "." + wi.getField("Minor Version ID").getValueAsString();
                    }
                }
                String typeName = wi.getField("Type").getValueAsString();
                addIdPrefix(typeName);
                ret = ret + (ret.isEmpty() ? "" : ", ") + getTypeShortName(typeName) + "-" + id;
            }
        }

        return ret;
    }

    /**
     *
     * @param imSession
     * @param documentID
     * @throws APIException
     */
    public void toggleToInclude(ItemMapperSession imSession, int documentID) throws APIException {
        log("Begin of Toggle subdocuments to Include for document '" + documentID + "'...", 2);
        // im viewsegment --norecurseInclude --user=veckardt --xmlapi 5950

        Command cmd = new Command(Command.IM, "viewsegment");
        cmd.addOption(new Option("norecurseInclude"));
        cmd.addOption(new Option("norecurseReference"));
        cmd.addSelection("" + documentID);

        Response response = imSession.executeCmd(cmd);
        // ResponseUtil.printResponse(response,1,System.out);
        WorkItemIterator witDoc = response.getWorkItems();
        while (witDoc.hasNext()) {
            // im toggleinclude 5953
            WorkItem workItem = witDoc.next();
            if (workItem.getModelType().contains(".subsegment.")) {
                log("Found modeltype '" + workItem.getModelType().replace("im.Issue.", "") + "' for node " + workItem.getId(), 3);
                if (workItem.getModelType().endsWith("subsegment.reference")) {  // must be reference
                    // log(workItem.getModelType(),4);
                    Command cmd2 = new Command(Command.IM, "toggleinclude");
                    cmd2.addSelection(workItem.getId());
                    imSession.executeCmd(cmd2);
                    log("Reference mode for subdocument '" + workItem.getId() + "' changed to 'include'.", 4);
                } else {
                    log("toggling not needed.", 4);
                }
            }
        }
        log("End of Toggle subdocuments to Include.", 2);
    }

}
