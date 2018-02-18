/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ptc.services.common.gateway.tracedefinition;

/**
 *
 * @author veckardt
 */
public class TraceDefinition {
    
    private String typeName;
    private String traceField;
    private String traceFilter;
    
    public TraceDefinition (String defString, String defFilter){
        this.typeName = defString.split(":")[0];
        this.traceField = defString.split(":")[1];
        this.traceFilter = defFilter;
    }
    
    public String getTypeName (){
        return this.typeName;
    }
    public String getTraceField (){
        return this.traceField;
    }
    public String getTraceFilter (){
        return this.traceFilter;
    }
}
