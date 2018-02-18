/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ptc.services.common.gateway;

import com.mks.gateway.data.ExternalItem;
import com.mks.gateway.mapper.ItemMapperException;
import com.mks.gateway.mapper.ItemMapperSession;
import com.mks.gateway.mapper.UnsupportedPrototypeException;
import com.mks.gateway.mapper.config.ItemMapperConfig;
import com.ptc.services.common.api.IntegrityFieldList;
import static com.ptc.services.common.gateway.LogAndDebug.log;
import com.ptc.services.common.tools.EnvUtil;

/**
 *
 * @author veckardt
 */
public class CustomMapperConfig extends ItemMapperConfig {

    private static final String listOfColumns = EnvUtil.getListOfColumns();
    private static final String numFields = System.getenv().get("MKSSI_NFIELD");
    private IntegrityFieldList ifl;

    /**
     * Constructor 1
     */
    public CustomMapperConfig(String id, ItemMapperSession imSession) throws ItemMapperException {
        super(id);

        log("listOfColumns: " + listOfColumns, 1);
        ifl = new IntegrityFieldList(imSession, listOfColumns);
        log("IntegrityFieldList size(): " + ifl.size(), 1);

        addAttribute("Type", "Type", ifl);
        for (String column : listOfColumns.split(",")) {
            // no need to add Section here
            if (!column.contentEquals("Section")) {
                addAttribute(column.replace("::rich", ""), column.replace("::rich", ""), ifl);
            }
        }
    }

    /**
     * Constructor 2
     *
     * @return
     */
//    public CustomMapperConfig(String id, String columnList, IntegrityFieldList ifl) throws ItemMapperException {
//        super(id);
//
//        addAttribute("Type", "Type", ifl);
//        for (String column : columnList.split(",")) {
//            // no need to add Section here
//            if (!column.contentEquals("Section")) {
//                addAttribute(column.replace("::rich", ""), column.replace("::rich", ""), ifl);
//            }
//        }
//    }
    public IntegrityFieldList getIntegrityFieldList() {
        return ifl;
    }

    public Boolean isRichContent(String field) {
        return (ifl.get(field).getRichContent() == null ? false : ifl.get(field).getRichContent());
    }

    public static String getNumFields() {
        return numFields;
    }

    /**
     *
     * @return @throws UnsupportedPrototypeException
     * @throws ItemMapperException
     */
    public ExternalItem getColumnItem() throws UnsupportedPrototypeException, ItemMapperException {
        //
        // Add the titles for the output table
        //
        int j = 0;
        ExternalItem columnItem = new ExternalItem("CONTENT", "COLUMNS");
        columnItem.add("ID", "COLUMNS");
        columnItem.add("Type", "Columns");
        for (String col : listOfColumns.split(",")) {
            columnItem.add("Col" + (++j), col);
        }
        columnItem.add("numFields", j);
        return columnItem;
    }

//    public static final String FIELDTYPE_TYPE = "type";
//    public static final String FIELDTYPE_ID = "id";
//    public static final String FIELDTYPE_ATTACHMENT = "attachment";
//    public static final String FIELDTYPE_RELATIONSHIP = "relationship";
//    public static final String FIELDTYPE_RICHCONTENT = "richcontent";
//    public static final String FIELDTYPE_DATE = "date";
//    public static final String FIELDTYPE_DATETIME = "date-time";
//    public static final String FIELDTYPE_IBPL = "ibpl";
//    public static final String FIELDTYPE_NUMBER = "number";
//    public static final String DATATYPE_XHTML = "xhtml";
//    public static final String DATATYPE_DATE = "date";
//    public static final String INTERNAL_DATE_FORMAT = "MM/dd/yyyy";
//    public static final String INTERNAL_DATETIME_FORMAT = "MM/dd/yyyy hh:mm:ss";
//    setAttribute(mapFieldKey, FieldAttribute.INTERNAL_FIELD_ATTRIBUTE, mapInternalField);
//    setAttribute(mapFieldKey, FieldAttribute.PROPERTY_ATTRIBUTE, mapAttributeName);
//    setAttribute(mapFieldKey, FieldAttribute.MAP_CONDITIONAL_ATTRIBUTE, "true");
//    setAttribute(mapFieldKey, FieldAttribute.MAP_EXTERNAL_KEY_ATTRIBUTE, mapExternalField);
//    setAttribute(mapFieldKey, FieldAttribute.MAP_INTERNAL_KEY_ATTRIBUTE, mapInternalField);
//    setAttribute(mapFieldKey, FieldAttribute.DEFAULT_MAP_ATTRIBUTE, defaultMap);        
    // this.addMapping("12-Dynamic-Document");
    // this.addMapping(id);
    private void addAttribute(String internal, String external, IntegrityFieldList ifl) throws ItemMapperException {
        // this.setAttribute(internal, ItemMapperConfig.FieldAttribute.DEFAULT_ATTRIBUTE, "Type");
        // this.setAttribute(internal, ItemMapperConfig.FieldAttribute.EXTERNAL_FIELD_ATTRIBUTE, external);    
        this.setAttribute(internal, ItemMapperConfig.FieldAttribute.MAP_EXTERNAL_KEY_ATTRIBUTE, external);

        this.setAttribute(internal, ItemMapperConfig.FieldAttribute.INTERNAL_FIELD_ATTRIBUTE, internal);
        this.setAttribute(internal, ItemMapperConfig.FieldAttribute.MAP_INTERNAL_KEY_ATTRIBUTE, internal);

        this.setAttribute(internal, ItemMapperConfig.FieldAttribute.DIRECTION_ATTRIBUTE, "out");

        if (ifl.get(internal) != null && ifl.get(internal).getRichContent() != null && ifl.get(internal).getRichContent()) {
            this.setAttribute(internal, ItemMapperConfig.FieldAttribute.DATA_TYPE_ATTRIBUTE, "xhtml");
            this.setAttribute(internal, ItemMapperConfig.FieldAttribute.FIELD_TYPE_ATTRIBUTE, "richcontent");
            this.setAttribute(internal, ItemMapperConfig.FieldAttribute.ATTACHMENT_ATTRIBUTE, ifl.get(internal).getDefaultAttachmentField()); // "Text Attachments");
        }
        // data-type="xhtml" field-type="richcontent" attachment="Text Attachments" direction="out" />
    }

    /*
     // We can not use the local standard routines, because we added additional data to the XML structure
     // this code here is just to remind me about that :)
     // this is local
     FileConfigLoader fcl = new FileConfigLoader();
     ItemMapperConfig loadConfigFile = fcl.loadConfigFile(System.getProperty("mks.gateway.configdir") + "\\Document_Export-2.xml");
     log("getOutgoingFields: " + loadConfigFile.getOutgoingFields().size(), 10);

     // List all segment fields from this xml file
     for (Field field : loadConfigFile.getOutgoingFields()) {
     log("out: field.externalField: " + field.externalField.toString());
     log("out: field.internalField: " + field.internalField);
     log("out: field.dataType: " + field.dataType);
     log("out: field.fieldType: " + field.fieldType);
     };

     // List all CONTENT fields from this xml file
     for (Field field : loadConfigFile.getExternalOutgoingFields("CONTENT")) {
     log("content-out: field.externalField: " + field.externalField.toString());
     log("content-out: field.internalField: " + field.internalField);
     log("content-out: field.dataType: " + field.dataType);
     log("content-out: field.fieldType: " + field.fieldType);
     };
     */
                // ExternalItem ei1 = new ExternalItem("DOCUMENT", ei.getId().getInternalID());
    // List<ExternalItem>
    //   wordItems
    //   gatewayItems
    //   flatItemList
    // ExternalItem (single Item)
    //   startingDocument = gatewayItems.get(0);
    //
    // case 1:
    // generateChildsInfo(startingDocument, result, cmdRunner, asOf);
}
