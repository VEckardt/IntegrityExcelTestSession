/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ptc.services.common.gateway.tracedefinition;

import java.util.ArrayList;
import java.util.Properties;

/**
 *
 * @author veckardt
 */
public class TraceDefinitionList extends ArrayList<TraceDefinition> {

    /**
     * TraceDefinitionList
     *
     * @param configProps
     */
    public TraceDefinitionList(Properties configProps) {
        String traceDefList = configProps.getProperty("traceDefinition", "");
        String traceDefFilter = configProps.getProperty("traceDefinitionFilter", "");
        if (!traceDefList.isEmpty()) {
            for (int i = 1; i <= traceDefList.split(",").length; i++) {
                // for (String traceDef : traceDefList.split(",")) {
                this.add(new TraceDefinition(traceDefList.split(",")[i - 1], traceDefFilter.isEmpty() ? "" : traceDefFilter.split(",")[i - 1]));
            }
        }
    }

    /**
     * TraceDefinitionList
     * @param traceDefList
     */
    public TraceDefinitionList(String traceDefList) {
        if (traceDefList != null && !traceDefList.isEmpty()) {
            for (String traceDef : traceDefList.split(",")) {
                this.add(new TraceDefinition(traceDef, ""));
            }
        }
    }
}
