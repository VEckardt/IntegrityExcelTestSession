/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ptc.services.common.gateway.html;

import com.ptc.services.common.gateway.html.IStyle;

/**
 *
 * @author Gerald Neumann
 */
public class DefaultStyle implements IStyle {   
    
    private static DefaultStyle instance = new DefaultStyle();
    
    public DefaultStyle () {
        //TODO throw new Exception("Instance of this class is not allowed");
    }

    public static IStyle getInstance() {
        return instance;
    }
    
    @Override
    public String getTableStyle() {
        return "";
    }

    @Override
    public String getTableHeaderCellStyle() {
        return " bgcolor=\"AAAAAA\"";
    }

    @Override
    public String getTableRowStyle() {
        return "";
    }

    @Override
    public String getTableCellStyle() {
        return "";
    }

    @Override
    public String getTableRowCellStyle() {
        return "";
    }

}
