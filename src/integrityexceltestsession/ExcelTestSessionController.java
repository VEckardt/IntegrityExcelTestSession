/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package integrityexceltestsession;

import com.ptc.services.common.api.IntegrityMessages;
import com.ptc.services.common.config.ExportProperties;
import static com.ptc.services.common.config.ExportProperties.workingDirectory;
import com.ptc.services.common.tm.TestSession;
import integrityexceltestsession.model.ViewEntry;
import static integrityexceltestsession.ExcelTestSessionController2.env;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.stage.DirectoryChooser;
import jfx.messagebox.MessageBox;

/**
 *
 * @author veckardt
 */
public class ExcelTestSessionController extends ExcelTestSessionController2 implements Initializable {

    private final static IntegrityMessages MC
            = new IntegrityMessages(ExcelTestSession.class);
    private ExportProperties props = new ExportProperties();
    @FXML
    private Label lSession1, lSession2, lFileName;
    @FXML
    private TableView<ViewEntry> table1 = new TableView<>();
    @FXML
    private TableView<ViewEntry> table2 = new TableView<>();
    @FXML
    private ProgressBar progressBar = new ProgressBar();
    @FXML
    private TableColumn<ViewEntry, String> tab1Col1, tab2Col1, tab1Col2, tab2Col2, tab1Col3, tab2Col3;
    @FXML
    private RadioButton rbAllTestCases, rbOpenTestCases;
    @FXML
    private TextArea logArea;
    private static final ObservableList<ViewEntry> table1Content
            = FXCollections.observableArrayList();
    private static final ObservableList<ViewEntry> table2Content
            = FXCollections.observableArrayList();
    // Property Elements
    // private String workingDirectory = props.getProperty("WorkingDirectory", "C:\\IntegrityExcelTestSession\\");
    private String templateFile = workingDirectory + props.getProperty("TemplateFileName", "ExcelTestLayout.xlsx");
    private String validSessionStates = props.getProperty("ValidTestSessionStates", "In Testing");
    private String filePrefix = props.getProperty("FilePrefix", "TestSession_");
    // System Variables
    private String testSessionId = env.get("MKSSI_ISSUE0");
    @FXML
    private CheckBox cOpenExcel;

    @FXML
    private void bShiftRight(ActionEvent event) {
        for (ViewEntry tf : table1.getSelectionModel().getSelectedItems()) {
            transfer(tf, table1.getItems(), table2Content, "");
            table1.getItems().remove(tf);
        }
    }

    @FXML
    private void bShiftRightAll(ActionEvent event) {
        List<ViewEntry> lst = new ArrayList<>();
        for (ViewEntry tf : table1.getItems()) {
            transfer(tf, table1.getItems(), table2Content, "");
            lst.add(tf);
        }
        for (ViewEntry tf : lst) {
            table1.getItems().remove(tf);
        }
    }

    @FXML
    private void delField2Action(ActionEvent event) {
        ViewEntry tf = table2.getSelectionModel().getSelectedItem();
        if (tf != null) {
            table2Content.remove(tf);
            log("Entry '" + tf.getId() + "' (" + tf.getVerdict() + ") removed from configuration.", 1);
        }
    }

    @FXML
    private void bCancel(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    private void bChangeDir() {
        final DirectoryChooser directoryChooser
                = new DirectoryChooser();
        File expPath = new File(props.workingDirectory);
        if (expPath.exists()) {
            directoryChooser.setInitialDirectory(new File(props.workingDirectory));
        }
        directoryChooser.setTitle("Select Working Directory");
        final File selectedDirectory
                = directoryChooser.showDialog(ExcelTestSession.stage);
        if (selectedDirectory != null) {
            // props.setGatewayExportPath(selectedDirectory.getAbsolutePath() + "\\");
            // lDirectoryName.setText(props.workingDirectory);
            // props.saveProperties();
        }
    }

    @FXML
    private void bImport(ActionEvent event) {

        // String sessionID = env.get("MKSSI_ISSUE0");
        // String sourceFile = basePath + filePrefix + testSessionId + ".xlsx";
        // importExcelFile(sessionID, sourceFile);
        if (checkSessionState()) {
            // generate the Excel File
            ExcelTSImport myTask = new ExcelTSImport(imSession, logArea, testSessionId, getFileName());
            if (true) {
                progressBar.progressProperty().unbind();
                progressBar.setProgress(0);
                progressBar.progressProperty().bind(myTask.progressProperty());

                Thread myTaskThread = new Thread(myTask);
                myTaskThread.start();
            } else {
                myTask.importFile();
            }
        }

        // System.exit(0);
    }

    @FXML
    private void bGenerate(ActionEvent event) {
        // Retrieve current settings
        Date asOfDate = new Date();

        if (checkSessionState()) {

            int mode = 0;
            if (rbAllTestCases.isSelected()) {
                mode = 1;
            } else if (rbOpenTestCases.isSelected()) {
                mode = 2;
            }
            // generate the Excel File
            ExcelTSExport myTask = new ExcelTSExport(imSession, logArea, mode, testSessionId, templateFile, getFileName(), asOfDate, cOpenExcel);
            if (false) {
                progressBar.progressProperty().unbind();
                progressBar.setProgress(0);
                progressBar.progressProperty().bind(myTask.progressProperty());

                Thread myTaskThread = new Thread(myTask);
                myTaskThread.start();
            } else {
                myTask.export();
            }
        }

        // System.exit(0);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Date asOfDate = new Date();
        if (testSessionId == null) {
            testSessionId = "630";
        }
        lSession1.setText("Test Session " + testSessionId);
        lSession2.setText("Test Session " + testSessionId);
        lFileName.setText(getFileName());

        // setFields(imSession, table1, tab1Col1, tab1Col2, tab1Col3, table1Content, testSessionId);
        // setFields(imSession, table2, tab2Col1, tab2Col2, tab2Col3, table2Content, "");
    }

    private boolean checkSessionState() {

        TestSession ts = new TestSession(imSession, testSessionId);
        String tsState = ts.getState();
        if (!("," + validSessionStates + ",").contains(tsState)) {
            MessageBox.show(ExcelTestSession.stage,
                    MC.getMessage("INVALID_TEST_SESSION_STATE").replace("{0}", tsState).replace("{1}", validSessionStates),
                    "Session " + testSessionId + ": State Invalid",
                    MessageBox.ICON_ERROR | MessageBox.OK);
            return false;
        }
        return true;
    }

    private String getFileName() {
        return workingDirectory + filePrefix + testSessionId + ".xlsx";
    }
}
