package celtech.coreUI.controllers.panels;

import celtech.appManager.ApplicationMode;
import celtech.appManager.ApplicationStatus;
import celtech.configuration.ApplicationConfiguration;
import celtech.coreUI.controllers.SettingsScreenState;
import celtech.printerControl.model.HardwarePrinter;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

/**
 *
 * @author Ian
 */
public class AboutPanelController implements Initializable
{
    private SettingsScreenState settingsScreenState = null;
    private HardwarePrinter currentPrinter = null;

    @FXML
    private Label roboxSerialNumber;

    @FXML
    private Label headSerialNumber;

    @FXML
    private Label version;
    
    @FXML
    private void okPressed(ActionEvent event)
    {
        ApplicationStatus.getInstance().returnToLastMode();
    }

    @FXML
    private void systemInformationPressed(ActionEvent event)
    {
        ApplicationStatus.getInstance().setMode(ApplicationMode.SYSTEM_INFORMATION);        
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        settingsScreenState = SettingsScreenState.getInstance();
        
        settingsScreenState.selectedPrinterProperty().addListener(new ChangeListener<HardwarePrinter>()
        {
            @Override
            public void changed(ObservableValue<? extends HardwarePrinter> observable, HardwarePrinter oldValue, HardwarePrinter newValue)
            {
                if (currentPrinter != null)
                {
                    headSerialNumber.textProperty().unbind();
                }

                if (newValue != null)
                {
                    currentPrinter = newValue;
                    headSerialNumber.textProperty().bind(currentPrinter.getPrinterIdentity().printerUniqueIDProperty());
                }
            }
        });
        
        version.setText(ApplicationConfiguration.getApplicationVersion());
    }

}
