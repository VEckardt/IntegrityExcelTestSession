/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ptc.services.common.gateway.tm;

import com.mks.api.Command;
import com.mks.api.Option;
import com.mks.api.response.APIException;
import com.mks.api.response.Response;
import com.mks.api.response.WorkItem;
import com.mks.gateway.mapper.ItemMapperSession;
import static com.ptc.services.common.config.ExportProperties.fldState;
import static com.ptc.services.common.config.ExportProperties.fldTestsAsOfDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author veckardt
 */
public class TestSession extends com.ptc.services.common.tm.TestSession {

    public TestSession(ItemMapperSession imSession, String id) {
        super(id);
        try {
            Command cmd = new Command(Command.IM, "issues");
            cmd.addOption(new Option("fields", fldTestsAsOfDate + "," + fldState));
            cmd.addSelection(id);
            Response result = imSession.executeCmd(cmd);
            WorkItem wi = result.getWorkItem(id);

            setAsOfDate(wi.getField(fldTestsAsOfDate).getDateTime());
            setState(wi.getField(fldState).getValueAsString());
        } catch (APIException ex) {
            Logger.getLogger(TestSession.class.getName()).log(Level.SEVERE, "Session id '" + id + "' is invalid!", ex);
        }
    }
}
