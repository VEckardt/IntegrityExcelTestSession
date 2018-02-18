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
import com.mks.gateway.GatewayLogger;
import com.mks.gateway.data.ExternalItem;
import com.mks.gateway.data.ItemField;
import com.mks.gateway.data.Transformer;
import com.mks.gateway.driver.IIntegrationDriver.IGatewayDriver;
import com.mks.gateway.mapper.ItemMapperException;
import com.mks.gateway.mapper.ItemMapperSession;
import com.mks.gateway.mapper.bridge.IMAdapter;
import com.mks.gateway.mapper.bridge.MappingItem;
import com.mks.gateway.mapper.config.ItemMapperConfig;
import com.mks.gateway.tool.exception.GatewayException;
import static com.ptc.services.common.config.Config.dfDayTime;
import com.ptc.services.common.config.ExportProperties;
import static com.ptc.services.common.config.ExportProperties.fldIsRelatedTo;
import static com.ptc.services.common.config.ExportProperties.fldText;
import static com.ptc.services.common.gateway.LogAndDebug.log;
import com.ptc.services.common.tools.EnvUtil;
import static com.ptc.services.common.tools.StringUtils.isEmpty;

import java.io.File;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author veckardt
 */
public class ItemHandler {

    // private static String fldText = "Text";
    private static final String listOfColumns = EnvUtil.getListOfColumns();

    private ItemHandler() {
        // to avoid any class instanciation
    }

    /**
     * getFieldValue Returns a dedicated value from an ExternalItem if no field
     * name is given, an empty string is returned
     *
     * @param ei
     * @param fieldName
     * @return
     */
    public static String getFieldValue(ExternalItem ei, String fieldName) {
        String fieldValue = "";
        try {
            if (ei.getField(fieldName) != null) {
                fieldValue = ei.getField(fieldName).getStringValue();
            }
            return fieldValue;
        } catch (ItemMapperException ex) {
            GatewayLogger.logError(ex.getMessage(), ex, 10);
        }
        return fieldValue;
    }

    public static String getRelatedDocId(ItemMapperSession imSession, String docId) {
        try {
            WorkItem workItem = getWorkItem(imSession, docId, fldIsRelatedTo + ",Type");
            if (workItem.getField("Type").getValueAsString().contentEquals("Doc List")) {
                return workItem.getField(fldIsRelatedTo).getValueAsString();
            }
        } catch (NoSuchElementException ne) {
        } catch (APIException ex) {
            Logger.getLogger(ItemHandler.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return null;
    }

    /**
     * copyAllFieldValues
     *
     * @param fromItem
     * @param toItem
     * @throws ItemMapperException
     */
    public static void copyAllFieldValues(ExternalItem fromItem, ExternalItem toItem) throws ItemMapperException {
        for (String fieldName : fromItem.getFieldNames()) {
            // String value = fromItem.getField(fieldName).getStringValue();
            // log(fieldName + " => " + fromItem.getField(fieldName).getFieldtype().name(), 1);
            ItemField itemField;
            if (fromItem.getField(fieldName).getFieldtype().name().contentEquals("RICHCONTENT")) {
                itemField = new com.mks.gateway.data.ItemField.RichContent(fieldName, fromItem.getField(fieldName).getStringValue());
            } else {
                itemField = new ItemField(fieldName, fromItem.getField(fieldName).getValue());
            }
            toItem.getItemData().addField(itemField);
        }
    }

    /**
     * Perform an Item Copy (from the Document Header only)
     */
    public static ExternalItem getItemCopy(ExternalItem item) throws ItemMapperException {
        ExternalItem newItem = new ExternalItem(item.getPrototype(), item.getAttribute("ID"));

        log("Creating item copy for " + item.getPrototype() + ": '" + item.getAttribute("ID") + "'", 3);
        for (String fieldName : item.getFieldNames()) {
            // log("Get data for field: '" + fieldName + "'", 1);
            String fieldValue = getFieldValue(item, fieldName);
            if (fieldName.contentEquals("Text") || fieldValue.contains("<div>")) {
                newItem.getItemData().addField(new ItemField.RichContent(fieldName, fieldValue));
            } else {
                newItem.add(fieldName, fieldValue);
            }
        }
        return newItem;
    }

    /**
     * isValidItem
     *
     * This routine checks if this item fulfils the given criteria if no
     * criteria is given it returns true otherwise it returns the appropriate
     * check status
     */
    public static Boolean isValidItem(ExternalItem item, String criteriaSet) throws ItemMapperException {
        Boolean isValid = true;
        if (!isEmpty(criteriaSet)) {
            for (String criteriaSingle : criteriaSet.split(";")) {
                String[] criteria = criteriaSingle.split("=");
                ItemField itemField = item.getField(criteria[0]);
                if (itemField == null) {
                    log("ERROR: Criteria field '" + criteria[0] + "' is not in the list of defined entry fields! Please correct your XML setup.", 1);
                    isValid = false;
                } else if (!("," + criteria[1] + ",").contains("," + itemField.getStringValue() + ",")) {
                    isValid = false;
                }
            }
        }
        return isValid;
    }

    /**
     * Retrieve a new Item from Integrity
     */
    public static MappingItem getNewItem(
            ItemMapperSession imSession,
            ItemMapperConfig mappingConfig,
            IGatewayDriver gatewayDriver,
            String itemID,
            Date asOf,
            String text) throws GatewayException, ItemMapperException, APIException {
        MappingItem item = MappingItem.newIssueItem(Integer.parseInt(itemID.replace("__MKSID__", "").trim()), asOf);
        // logLine(1);
        item.setItemAttribute(ExternalItem.Attribute.ADAPTER.getStringValue(), "com.mks.gateway.mapper.bridge.IMAdapter");
        IMAdapter adapter = (IMAdapter) IMAdapter.getAdapter(imSession, mappingConfig, item, gatewayDriver);
        adapter.retrieveItem(item, mappingConfig, false, asOf);
        if (text != null) {
            // this section is only needed if we want to overwrite the text with text and parameter substitution 
            ItemField itemField;
            if (text.startsWith("<!-- MKS HTML -->")) {
                itemField = new com.mks.gateway.data.ItemField.RichContent(fldText, text);
            } else {
                itemField = new ItemField(fldText, text);
            }
            item.replaceInternal(itemField);
        }
        Transformer.transform(item, imSession, mappingConfig, false, true, asOf);
        return item;
    }

    /**
     * Add the usual user details
     */
    public static void addUserDetails(ItemMapperSession imSession, ExternalItem ei) {
        try {
            Command cmd = new Command(Command.IM, "viewuser");
            cmd.addSelection(imSession.getDefaultUsername());
            Response response = imSession.executeCmd(cmd);
            WorkItem userData = response.getWorkItem(imSession.getDefaultUsername());

            ei.add("Current Username", imSession.getDefaultUsername());
            ei.add("User Full Name", userData.getField("fullname").getValueAsString());
            ei.add("User Description", userData.getField("description").getValueAsString());
            ei.add("User Email", userData.getField("email").getValueAsString());

        } catch (APIException ex) {
            Logger.getLogger(ItemHandler.class
                    .getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ItemMapperException ex) {
            Logger.getLogger(ItemHandler.class
                    .getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    /**
     * setBookmarkForField
     *
     * @param currentChild
     * @param fieldname
     */
    public static void setBookmarkForField(ExternalItem currentChild, String fieldname, String refIdent, Boolean containsReferencesToReference) {

        try {
            if (currentChild.getField(fieldname) != null && currentChild.getField(fieldname).getValue() != null) {
                String currentValue = currentChild.getField(fieldname).getValue().toString();

                Pattern pattern;
                // String refIdent = "REF";
                if (fieldname.contentEquals("ID")) {
                    pattern = Pattern.compile("([0-9][0-9]*)");
                    // refIdent = "ID";
                } else {
                    pattern = Pattern.compile("(/[0-9][0-9]*/)");
                }

                // [^>]
                Matcher matcher = pattern.matcher(currentValue);
                if (matcher.find()) {
                    containsReferencesToReference = true;
                    // log("************* Contains " + matcher.group() + " *************", 1);
                    // currentChild.add("Text2", currentChild.getField("Text").getValue());

                    // log("group = " + matcher.group(), 1);
                    String refId = matcher.group().replaceAll("\\D+", "");
                    if (!refId.isEmpty()) {
                        // format the ref id with leading zeros
                        // refId = ("0000000" + refId).substring(refId.length());
                        // log("refId = " + refId, 1);
                        // log("group = "+matcher.group(),1);
                        currentValue = (matcher.replaceAll("<a name=\"" + refIdent.replace("-", "") + refId + "\">$1</a>"));
                        // this step will overwrite the current text
                        currentChild.getItemData().addField(new ItemField.RichContent(fieldname, currentValue));
                    }
                }
            }
        } catch (ItemMapperException ex) {
            Logger.getLogger(ItemHandler.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param item
     * @param textFieldName
     */
    public static void replaceMKSItemId(ExternalItem item, String textFieldName) {
        try {
            if (item.getField(textFieldName) != null && item.getField(textFieldName).getValue() != null) {
                String text = item.getField(textFieldName).getValue().toString();
                String newRefIdent = "mks:///document/bookmark?bookmarkname=ID";
                String oldRefIdent = "mks:///item?itemid=";

                text = text.replace(oldRefIdent, newRefIdent);
                item.getItemData().addField(new ItemField.RichContent(textFieldName, text));
            }
        } catch (ItemMapperException ex) {
            Logger.getLogger(ItemHandler.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    /**
     * Replaces the text /n/ with a corresponding hyperlink
     *
     * @param currentChild
     * @param textFieldName, e.g. Text
     */
    public static void replaceRefId(ExternalItem currentChild, String textFieldName) {
        try {
            if (currentChild.getField(textFieldName) != null && currentChild.getField(textFieldName).getValue() != null) {
                String text = currentChild.getField(textFieldName).getValue().toString();
                String refIdent = "mks:///document/bookmark?bookmarkname=";  // was before just empty
                // String refIdent = "#";

                // <a href="mks:///document/bookmark?bookmarkname=REF0001">/1/</a> 
                Pattern pattern;
                String keyIdent = "REF";
                String closingTag = "/";
                if (textFieldName.contentEquals("Text")) {
                    pattern = Pattern.compile("([^>]/[0-9][0-9]*/)");
                } else {
                    pattern = Pattern.compile("([0-9][0-9]*)");
                    keyIdent = "ID";
                    closingTag = "";
                }

                log("==> " + textFieldName + ": " + text, 6);

                Matcher matcher = pattern.matcher(text);

                // check if at least one replacement has to be performed
                if (matcher.groupCount() > 0) {
                    // for all entries in matcher string
                    while (matcher.find()) {
                        String refText = matcher.group();
                        log("************* " + currentChild.getId().getInternalID() + " contains '" + refText + "' *************", 1);
                        String refId = refText.replaceAll("[^0-9]+", "");
                        // format the ref id with leading zeros
                        // String refIdFormat = ("0000000" + refId).substring(refId.length());
                        log("Replacing refId '" + refId + "' with '" + refIdent + keyIdent + refId + "' ...", 1);
                        text = text.replace(closingTag + refId + closingTag, "<a href=\"" + refIdent + keyIdent + refId + "\">" + closingTag + refId + closingTag + " </a>");
                        // this step will overwrite the current text
                    }
                    // text = "<pre>"+text+"</pre>";
                    // log("\nNew Text Field: " + text, 5);
                    if (textFieldName.contentEquals("Satisfied By")) {
                        text = "&darr;" + text;
                    } else if (textFieldName.contentEquals("Satisfies")) {
                        text = "&uarr;" + text;
                    } else if (textFieldName.contentEquals("Models")) {
                        text = "&uarr;" + text;
                    }
                    // text = text.replace(",", "");
                    currentChild.getItemData().addField(new ItemField.RichContent(textFieldName, text));
                }
            }
            // return text;
        } catch (ItemMapperException ex) {
            Logger.getLogger(ItemHandler.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * getWorkItemAsOf
     *
     * @param imSession
     * @param itemId
     * @param fieldList
     * @param asOf
     * @return
     * @throws APIException
     */
    public static WorkItem getWorkItemAsOf(ItemMapperSession imSession, String itemId, String fieldList, Date asOf) throws APIException {
        Command cmd = new Command(Command.IM, "issues");
        if (asOf != null) {
            cmd.addOption(new Option("asof", dfDayTime.format(asOf)));
        }
        cmd.addOption(new Option("fields", fieldList));
        cmd.addSelection(itemId);
        Response response = imSession.executeCmd(cmd);
        return response.getWorkItem(itemId);
    }

    /**
     * getWorkItem
     *
     * @param imSession
     * @param itemId
     * @param fieldList
     * @return
     * @throws APIException
     */
    public static WorkItem getWorkItem(ItemMapperSession imSession, String itemId, String fieldList) throws APIException {
        return getWorkItemAsOf(imSession, itemId, fieldList, null);
    }

    /**
     * addFileName
     *
     * @param externalItem
     * @param destinationFile
     */
    public static void addFileName(ExternalItem externalItem, File destinationFile) {
        try {
            // Remove drive and directory from path - just keep filename
            String strippedFileName;
            int iLastBackslash = destinationFile.toString().lastIndexOf("\\");
            if (iLastBackslash > -1) {
                strippedFileName = destinationFile.toString().substring(iLastBackslash + 1);
            } else {
                strippedFileName = destinationFile.toString();
            }
            log("Field 'Filename' added to the document output.", 3);
            externalItem.getItemData().addField("Filename", strippedFileName);
        } catch (ItemMapperException ex) {
            GatewayLogger.logError(ex.getMessage(), ex, 10);
        }
    }

    /**
     * getNewItem
     *
     * @param imSession
     * @param iGatewayDriver
     * @param mappingConfig
     * @param itemID
     * @param asOf
     * @return
     * @throws com.mks.gateway.tool.exception.GatewayException
     * @throws com.mks.gateway.mapper.ItemMapperException
     */
    public static MappingItem getNewItem(ItemMapperSession imSession, IGatewayDriver iGatewayDriver, ItemMapperConfig mappingConfig, String itemID, Date asOf) throws GatewayException, ItemMapperException, APIException {
        return getNewItem(imSession, iGatewayDriver, mappingConfig, itemID, asOf, null);
    }

    /**
     * getNewItem
     *
     * @param imSession
     * @param iGatewayDriver
     * @param mappingConfig
     * @param itemID
     * @param asOf
     * @param newText
     * @return
     * @throws com.mks.gateway.tool.exception.GatewayException
     * @throws com.mks.gateway.mapper.ItemMapperException
     */
    public static MappingItem getNewItem(ItemMapperSession imSession, IGatewayDriver iGatewayDriver, ItemMapperConfig mappingConfig, String itemID, Date asOf, String newText) throws GatewayException, ItemMapperException, APIException {
        // 1: newContentItem
        // 2: newIssueItem
        // System.out.println("itemID = " + itemID);

        MappingItem item = MappingItem.newIssueItem(Integer.parseInt(itemID.replace("__MKSID__", "").trim()), asOf);

        // log("Retrieving new item " + item.getId(), 1);
        // public static void transform(MappingItem item, ItemMapperSession session, ItemMapperConfig config, boolean incoming, boolean downloadAttachments, Date asOf)
        // DontUse_CustomMapperConfig ccm = new DontUse_CustomMapperConfig("Sample ALM Document Export");
        // Field fld = ccm.constructField("ID");
        // ccm
        // to be tested!
        item.setItemAttribute(ExternalItem.Attribute.ADAPTER.getStringValue(), "com.mks.gateway.mapper.bridge.IMAdapter");

        // 1: ItemAdapter 
        // 2: RelationshipTreeAdapter
        // 3: RQAdapter
        // log("Step 1: Defining Adpater ...", 2);
        IMAdapter adapter = (IMAdapter) IMAdapter.getAdapter(imSession, mappingConfig, item, iGatewayDriver);

        // Iterator<?> it3 = adapter.getConfigTemplateIds().listIterator();
        // while (it3.hasNext()) {
        //    log("Server Config Template: " + it3.next().toString(), 2);
        // }
        // Retrieve Item
        // log("Step 2: Retrieving Item ...", 2);
        adapter.retrieveItem(item, mappingConfig, false, asOf);

//                    String fieldvalue = field.getValueAsString();
//                    if(fieldvalue != null && fieldvalue.startsWith("<!-- MKS HTML -->"))
//                        itemField = new com.mks.gateway.data.ItemField.RichContent(fieldname, fieldvalue);
//                    else
//                        itemField = new ItemField(fieldname, fieldvalue);         
        if (newText != null) {
            ItemField itemField;
            // mappingConfig.getConfigFields().get(defectOpenCount)
            if (newText.startsWith("<!-- MKS HTML -->")) {
                itemField = new com.mks.gateway.data.ItemField.RichContent(ExportProperties.fldText, newText);
            } else {
                itemField = new ItemField(ExportProperties.fldText, newText);
            }
            item.replaceInternal(itemField);
        }

        // 1: ItemAdapter.jad
        // public final void retrieveItem(MappingItem item, ItemMapperConfig config, boolean updateBaseline, Date asOf)
        //     throws APIException, ItemMapperException
        // {de
        //     item.primeInternalId(session, config);
        //     retrieveItemData(item, config, updateBaseline, asOf);
        //     item.primeExternalId(session, config);
        // }        
        // 2: IMAdapter.jad
        // protected final void retrieveItemData(MappingItem item, ItemMapperConfig config, boolean updateBaseline, Date asOf)
        //     throws APIException, ItemMapperException
        // {
        //     if(config == null)
        //         return;
        //     retrieveIssueData(item, config, false, asOf);
        //     if(updateBaseline)
        //     {
        //         Date timestamp = updateBaselineTimestamp(item);
        //         cacheBaselineItem(item, timestamp);
        //     }
        // }
        // with 4 PARAMS
        // private void retrieveIssueData(MappingItem item, ItemMapperConfig config, boolean incoming, Date asOf)
        // then, with 5 Params
        // protected void retrieveIssueData(MappingItem issueItem, Collection issueFields, ItemMapperConfig config, String attachmentPath, Date asOf)
        // a) issuesCmd = new Command("im", "viewissue");
        // b) issuesCmd = new Command("im", "issues");
        // WorkItem issuedata = executeCmd(issuesCmd).getWorkItem(issueID);
        // populateIssueItem(issueItem, issuedata, attachmentPath, asOf);
        // log("Step 3: Transforming Item ...", 2);
        Transformer.transform(item, imSession, mappingConfig, false, true, asOf);

        // log("End of Retrieving new item " + item.getId(), 1);
        return item;
    }
}
