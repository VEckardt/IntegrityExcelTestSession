/*
 * Copyright:      Copyright 2015 (c) Parametric Technology GmbH
 * Product:        PTC Integrity Lifecycle Manager
 * Author:         Volker Eckardt, Senior Consultant ALM
 * Purpose:        Custom Developed Code
 * **************  File Version Details  **************
 * Revision:       $Revision$
 * Last changed:   $Date$
 */
package com.ptc.services.common.excel;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author veckardt
 */
public class NewClass {

    XSSFSheet ws;

    public void test() throws FileNotFoundException, IOException {

        DataValidation dataValidation = null;
        DataValidationConstraint constraint = null;
        DataValidationHelper validationHelper = null;

        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet1 = (XSSFSheet) wb.createSheet("sheet1");

        validationHelper = new XSSFDataValidationHelper(sheet1);
        CellRangeAddressList addressList = new CellRangeAddressList(0, 5, 0, 0);
        constraint = validationHelper.createExplicitListConstraint(new String[]{"SELECT", "10", "20", "30"});
        dataValidation = validationHelper.createValidation(constraint, addressList);
        dataValidation.setSuppressDropDownArrow(true);
        sheet1.addValidationData(dataValidation);

        FileOutputStream fileOut = new FileOutputStream("c:\\temp\\vineet.xlsx");
        wb.write(fileOut);
        fileOut.close();

    }

}
