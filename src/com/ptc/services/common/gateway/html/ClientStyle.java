/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ptc.services.common.gateway.html;

/**
 *
 * @author Steve Ridley
 */
public class ClientStyle extends DefaultStyle {
    private static ClientStyle instance = new ClientStyle();
    
    public static IStyle getInstance() {
        return instance;
    }

    @Override
    public String getTableStyle() {
        return " width=\"100%\"";
    }

    @Override
    public String getTableHeaderCellStyle() {
        return " bgcolor=\"000000\" style=\"border-top-style:solid; border-top-color:#00000;  border-top-width:3px;  border-bottom-style:solid; border-bottom-color:black; border-bottom-width:1px;\"";        
    }
    
    @Override
    public String getTableRowCellStyle() {
        return " style=\"border-bottom-color:black; border-width:0px; border-bottom-width:1px;border-style:solid\"";
    }
            
}
