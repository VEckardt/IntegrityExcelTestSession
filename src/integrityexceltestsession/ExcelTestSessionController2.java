/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package integrityexceltestsession;

import com.ptc.services.common.api.IntegrityAPI;
import integrityexceltestsession.model.ViewEntry;
import integrityexceltestsession.model.ObjectDetails;
import java.util.Map;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 *
 * @author veckardt
 */
public class ExcelTestSessionController2 {

    public static Map<String, String> env = System.getenv();
    public IntegrityAPI imSession = new IntegrityAPI(env, "IntegrityExcelTestSession");

    // moves all data from one side to the other
    // if already exists, it will overwrite
    public void transfer(ViewEntry tfFrom, ObservableList<ViewEntry> fromTableContent, ObservableList<ViewEntry> toTableContent, String targetTypeName) {
        // ViewEntry tffrom = sourceTable.getSelectionModel().getSelectedItem();
        boolean exists = false;
        for (ViewEntry tf : toTableContent) {
            // check, if the value already exists at the target
            if (tf.getId().equals(tfFrom.getId()) && tf.getText().equals(tfFrom.getText())) {
                // log(singular(targetTypeName) + " '" + tfFrom.getName() + "' updated.");
                // if yes, remove it first
                toTableContent.remove(tf);
                // and place the new value at the end
                toTableContent.add(tfFrom);
                exists = true;
                break;
            }
        }
        if (!exists) {
            // if not exists, just add it
            toTableContent.add(tfFrom);
            // log(singular(targetTypeName) + " '" + tfFrom.getName() + "' added to configuration.");
        }
        // fromTableContent.remove(tfFrom);
    }

    // This procedure puts the data into the fx display tables
    // utilizing the session, and the object type selected
    public void setFields(IntegrityAPI apiSession, TableView<ViewEntry> table, TableColumn<ViewEntry, String> col1,
            TableColumn<ViewEntry, String> col2, TableColumn<ViewEntry, String> col3, ObservableList<ViewEntry> tabContent,
            String sessionId) {
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabContent.clear();

        // this is the pointer to the function in ViewEntry object Name => getName
        col1.setCellValueFactory(new PropertyValueFactory<ViewEntry, String>("Id"));
        col2.setCellValueFactory(new PropertyValueFactory<ViewEntry, String>("Text"));
        col3.setCellValueFactory(new PropertyValueFactory<ViewEntry, String>("Verdict"));

        if (!sessionId.isEmpty()) {
            ObjectDetails td = new ObjectDetails(apiSession, sessionId, "");
            for (Map.Entry pairs : td.getObjectsMap().entrySet()) {
                // System.out.println(pairs.getKey() + " = " + pairs.getValue());
                String[] sa = (String[]) pairs.getValue();
                tabContent.add(new ViewEntry(pairs.getKey().toString(), sa[0].substring(0, 10), sa[1]));
            }
        }

        table.setItems(tabContent);
    }

    public void log(String text, int level) {
        imSession.log(text, level);
    }
}
