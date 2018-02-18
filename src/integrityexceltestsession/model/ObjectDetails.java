/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package integrityexceltestsession.model;

import com.mks.api.Command;
import com.mks.api.Option;
import com.mks.api.response.APIException;
import com.mks.api.response.Response;
import com.mks.api.response.WorkItem;
import com.mks.api.response.WorkItemIterator;
import com.ptc.services.common.api.ExceptionHandler;
import com.ptc.services.common.api.IntegrityAPI;
import com.ptc.services.common.api.IntegrityMessages;
import integrityexceltestsession.ExcelTestSession;
import java.util.Map;
import java.util.TreeMap;
import javafx.stage.Stage;
import jfx.messagebox.MessageBox;

/**
 *
 * @author veckardt
 */
public class ObjectDetails {

    private final static IntegrityMessages MC =
            new IntegrityMessages(ExcelTestSession.class);    
    private IntegrityAPI apiSession;
    private String testSessionId;
    private String filter;
    private Map<String, String[]> sortedObjects = new TreeMap<>();
    Stage stage = ExcelTestSession.stage;

    public ObjectDetails(IntegrityAPI apiSession, String testSessionId, String filter) {
        this.apiSession = apiSession;

        this.testSessionId = testSessionId;
        this.filter = filter;

        loadData();
    }

    public Map<String, String[]> getObjectsMap() {
        return this.sortedObjects;
    }

    public String getTestSessionId() {
        return this.testSessionId;
    }

    public IntegrityAPI getApiSession() {
        return this.apiSession;
    }

    public void loadData() {
        System.out.println("Loading '" + testSessionId + "' data ...");
        try {
            Command cmd = new Command(Command.TM, "testcases");
            cmd.addOption(new Option("fields", "id,text,last result"));
            cmd.addSelection(testSessionId);
            Response response = apiSession.executeCmd(cmd);
            WorkItemIterator wit = response.getWorkItems();
            while (wit.hasNext()) {
                WorkItem wi = wit.next();

                String id = wi.getField("id").getValueAsString();
                String text = wi.getField("text").getValueAsString();
                String verdict = wi.getField("last result").getValueAsString();
                sortedObjects.put(id, new String[]{text, verdict});
            }
        } catch (APIException ex) {
            // System.out.println(ex.toString());
            ExceptionHandler eh = new ExceptionHandler(ex);
            MessageBox.show(ExcelTestSession.stage,
                    MC.getMessage("INTEGRITY_API_ERROR").replace("{0}", eh.getMessage()),
                    "Result",
                    MessageBox.ICON_ERROR | MessageBox.OK);               
            System.out.println(eh.toString());
            System.exit(-1);
        }
    }

    // public String singular(String typeName) {
    //     return typeName.replace("ies", "y").replace("s", "");
    // }
}
