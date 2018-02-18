/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ptc.services.common.tm;

import com.mks.api.Command;
import com.mks.api.Option;
import com.mks.api.response.APIException;
import com.mks.api.response.Response;
import com.mks.api.response.WorkItem;
import com.ptc.services.common.api.IntegrityAPI;
import static com.ptc.services.common.config.ExportProperties.fldState;
import static com.ptc.services.common.config.ExportProperties.fldTestsAsOfDate;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author veckardt
 */
public class TestSession {

    String tsId;
    Date asOfDate;
    String state;

    public TestSession(String id) {
        tsId = id;
    }

    public TestSession(IntegrityAPI imSession, String id) {
        this.tsId = id;
        try {
            Command cmd = new Command(Command.IM, "issues");
            cmd.addOption(new Option("fields", fldTestsAsOfDate + "," + fldState));
            cmd.addSelection(tsId);
            Response result = imSession.executeCmd(cmd);
            WorkItem wi = result.getWorkItem(tsId);

            asOfDate = wi.getField(fldTestsAsOfDate).getDateTime();
            state = wi.getField(fldState).getValueAsString();
        } catch (APIException ex) {
            Logger.getLogger(TestSession.class.getName()).log(Level.SEVERE, "Session id '" + tsId + "' is invalid!", ex);
        }
    }
    
//    public TestSession(ItemMapperSession imSession, String id) {
//        // super(id);
//        try {
//            Command cmd = new Command(Command.IM, "issues");
//            cmd.addOption(new Option("fields", fldTestsAsOfDate + "," + fldState));
//            cmd.addSelection(id);
//            Response result = imSession.executeCmd(cmd);
//            WorkItem wi = result.getWorkItem(id);
//
//            setAsOfDate(wi.getField(fldTestsAsOfDate).getDateTime());
//            setState(wi.getField(fldState).getValueAsString());
//        } catch (APIException ex) {
//            Logger.getLogger(com.ptc.services.common.gateway.tm.TestSession.class.getName()).log(Level.SEVERE, "Session id '" + id + "' is invalid!", ex);
//        }
//    }    

    public void setAsOfDate(Date date) {
        this.asOfDate = date;
    }

    public void setState(String state) {
        this.state = state;
    }

//    public TestSession(ItemMapperSession imSession, String id) {
//        this.tsId = id;
//        try {
//            Command cmd = new Command(Command.IM, "issues");
//            cmd.addOption(new Option("fields", fldTestsAsOfDate + "," + fldState));
//            cmd.addSelection(tsId);
//            Response result = imSession.executeCmd(cmd);
//            WorkItem wi = result.getWorkItem(tsId);
//
//            asOfDate = wi.getField(fldTestsAsOfDate).getDateTime();
//            state = wi.getField(fldState).getValueAsString();
//        } catch (APIException ex) {
//            Logger.getLogger(TestSession.class.getName()).log(Level.SEVERE, "Session id '" + tsId + "' is invalid!", ex);
//        }
//    }
    /**
     * Returns the test Session As Of Date, as it is now.
     *
     * @return
     */
    public Date getAsOfDate() {
        return asOfDate;
    }

    /**
     * Returns the test Session As Of Date, as it is now.
     *
     * @return
     */
    public String getState() {
        return state;
    }
}
