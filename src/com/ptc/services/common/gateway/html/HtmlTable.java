/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ptc.services.common.gateway.html;
import com.ptc.services.common.gateway.html.IStyle;
import java.util.ArrayList;
//<editor-fold defaultstate="collapsed" desc="comment">
import java.util.Arrays;
import java.util.List;
//</editor-fold>
/**
 *
 * @author Gerald Neumann
 */
public class HtmlTable {
    
    private int cols = 0;
    private int rows = 0;
    private List<String> tableHeader;
    private ArrayList<List<String>> tableRows;
     
    public HtmlTable() {
    }
    
    public HtmlTable(int cols, int rows) {
        this.cols=cols;
        this.rows=rows;
    }
    
    public String render() {
        return render(DefaultStyle.getInstance());
    }
    
    public String render(IStyle style) {                
        StringBuilder sb = new StringBuilder();
        
        // HtmlTable definition
        sb.append("<table").append(style.getTableStyle()).append(">");
           
        // HtmlTable header
        if (tableHeader != null) {
           sb.append("<tr").append(style.getTableRowStyle()).append(">");
           //Unfortunately this code does not work with Word 2003 even if it is conforming to HTML :(
           //The table column style has to be passed in the <th> tag
           for (String item: tableHeader) {
               sb.append("<td").append(style.getTableHeaderCellStyle()).append(">").append(item).append("</td>");
           }
           sb.append("</tr>");
        }       
        
        // HtmlTable daata
        if (tableRows != null) {
           
           for (List<String> row: tableRows) {
              sb.append("<tr").append(style.getTableRowStyle()).append(">");
              
              for (String cell: row) {
                  sb.append("<td").append(style.getTableRowCellStyle()).append(">").append(cell).append("</td>");
              }
              sb.append("</tr>");
           }           
        }
        
        sb.append("</table>");
        
        return sb.toString();
    }       

    public void setHeader(String[] headerCols) {
        this.setHeader(Arrays.asList(headerCols));
    }

    public void setHeader(List<String> headerCols) {
        
        // set number of table cols to number of header columns
        if (cols == 0) {
            cols = headerCols.size();
            if (rows == 0) rows = 1;
        } 
        // TODO: error handling
        //else if (headerCols.size() > cols) throw new Exception("Number of header columns does not match number of table columns");                
        
        tableHeader = headerCols;
    }

    public void addDataRow(String[] dataCols) {
        this.addDataRow(Arrays.asList(dataCols));
    }

    public void addDataRow(List<String> dataCols) {
        if (tableRows == null) tableRows = new ArrayList<List<String>>();   
        tableRows.add(dataCols);
    }

    public boolean isEmpty() {
        return tableRows == null;
    }
    
}