/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ptc.services.common.gateway;

import com.mks.gateway.mapper.ItemMapperException;
import com.mks.gateway.mapper.config.ItemMapperConfig;
import static com.ptc.services.common.gateway.LogAndDebug.log;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author veckardt
 */
public class MappingConfig {
    /**
     *
     * @param mappingConfig
     * @param description
     * @throws ItemMapperException
     */
    public static void listMappingConfig(ItemMapperConfig mappingConfig, String description) {
        try {
            log("Mapping Config ID: " + mappingConfig.getId(), 2);
            Iterator<?> it = mappingConfig.getOutgoingFields().listIterator();
            while (it.hasNext()) {
                ItemMapperConfig.Field fld = (ItemMapperConfig.Field) it.next();
                log(" Mapping Field: " + fld.internalField + " => " + fld.externalField, 2);
            }
            log("End of listing " + description + " configuration.", 1);
        } catch (ItemMapperException ex) {
            Logger.getLogger(MappingConfig.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    




    /**
     *
     * @param mappingConfig
     * @param fieldName
     * @return
     * @throws ItemMapperException
     */
    public static String getExternalFieldName(ItemMapperConfig mappingConfig, String fieldName) throws ItemMapperException {
        // log("Mapping Config ID: " + mappingConfig.getId(), 2);
        Iterator<?> it = mappingConfig.getOutgoingFields().listIterator();
        while (it.hasNext()) {
            ItemMapperConfig.Field fld = (ItemMapperConfig.Field) it.next();
            if (fieldName.contentEquals(fld.internalField)) {
                return fld.externalField;
            }
        }
        return fieldName;
    }
    
}
