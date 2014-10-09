/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package celtech.coreUI.controllers.utilityPanels;

import celtech.configuration.EEPROMState;
import celtech.configuration.Filament;
import celtech.configuration.FilamentContainer;
import celtech.configuration.MaterialType;
import celtech.coreUI.components.RestrictedTextField;
import celtech.coreUI.controllers.StatusScreenState;
import celtech.printerControl.model.Printer;
import celtech.printerControl.comms.commands.exceptions.RoboxCommsException;
import celtech.printerControl.model.PrinterException;
import celtech.printerControl.model.Reel;
import java.net.URL;
import java.text.ParseException;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import libertysystems.stenographer.Stenographer;
import libertysystems.stenographer.StenographerFactory;

/**
 * FXML Controller class
 *
 * @author Ian
 */
public class ReelDataPanelController implements Initializable
{

    private final Stenographer steno = StenographerFactory.getStenographer(
        ReelDataPanelController.class.getName());
    private Printer connectedPrinter = null;
    private StatusScreenState statusScreenState = null;

    @FXML
    Pane reelContainer;

    @FXML
    private RestrictedTextField reelFilamentName;

    @FXML
    private RestrictedTextField reelFirstLayerBedTemperature;

    @FXML
    private RestrictedTextField reelAmbientTemperature;

    @FXML
    private RestrictedTextField reelFilamentDiameter;

    @FXML
    private RestrictedTextField reelRemainingFilament;

    @FXML
    private TextField filamentID;

    @FXML
    private RestrictedTextField reelNozzleTemperature;

    @FXML
    private RestrictedTextField reelFeedRateMultiplier;

    @FXML
    private RestrictedTextField reelFirstLayerNozzleTemperature;

    @FXML
    private RestrictedTextField reelFilamentMultiplier;

    @FXML
    private RestrictedTextField reelBedTemperature;

    @FXML
    private ComboBox<MaterialType> reelMaterialType;

    @FXML
    private ColorPicker reelDisplayColor;

    @FXML
    private Button reelWriteConfig;

    @FXML
    private Button saveFilamentAs;

    @FXML
    void filamentSaveAs(ActionEvent event)
    {
        Filament newFilament;
        try
        {
            newFilament = makeFilamentFromFields();
            newFilament.setFilamentID(null);
            String safeNewFilamentName = FilamentContainer.suggestNonDuplicateName(
                newFilament.getFriendlyFilamentName());
            newFilament.setFriendlyFilamentName(safeNewFilamentName);
            FilamentContainer.saveFilament(newFilament);
        } catch (ParseException ex)
        {
            steno.info("Parse error getting filament data");
        }

    }

    @FXML
    void writeReelConfig(ActionEvent event)
    {
        try
        {
            Filament newFilament = makeFilamentFromFields();
            try
            {

                FilamentContainer.saveFilament(newFilament);

                connectedPrinter.transmitWriteReelEEPROM(newFilament);

            } catch (RoboxCommsException ex)
            {
                steno.error("Error writing reel EEPROM");
            }

        } catch (ParseException ex)
        {
            steno.info("Parse error getting filament data");
        }
        readReelConfig(event);

    }

    private Filament makeFilamentFromFields() throws ParseException
    {
        float remainingFilament = 0;
        try
        {
            remainingFilament = Float.valueOf(reelRemainingFilament.getText());
        } catch (NumberFormatException ex)
        {
            steno.error("Error parsing filament parameters");
        }
        Filament newFilament = new Filament(
            reelFilamentName.getText(),
            reelMaterialType.getSelectionModel().getSelectedItem(),
            filamentID.getText(),
            reelFilamentDiameter.getFloatValue(),
            reelFilamentMultiplier.getFloatValue(),
            reelFeedRateMultiplier.getFloatValue(),
            Integer.valueOf(reelAmbientTemperature.getText()),
            Integer.valueOf(reelFirstLayerBedTemperature.getText()),
            Integer.valueOf(reelBedTemperature.getText()),
            Integer.valueOf(reelFirstLayerNozzleTemperature.getText()),
            Integer.valueOf(reelNozzleTemperature.getText()),
            reelDisplayColor.getValue(),
            true);
        newFilament.setRemainingFilament(remainingFilament);
        return newFilament;
    }

    void readReelConfig(ActionEvent event)
    {
        try
        {
            connectedPrinter.readReelEEPROM();
        } catch (RoboxCommsException ex)
        {
            steno.error("Error reading reel EEPROM");
        }
    }

    void formatReelEEPROM(ActionEvent event)
    {
        try
        {
            connectedPrinter.formatReelEEPROM();
        } catch (PrinterException ex)
        {
            steno.error("Error formatting reel EEPROM");
        }
        readReelConfig(null);
    }

    private ChangeListener<Boolean> reelDataChangeListener = new ChangeListener<Boolean>()
    {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
            Boolean newValue)
        {

            if (connectedPrinter.reelsProperty().get(0).isUserFilament() == false)
            {
                reelContainer.disableProperty().set(false);
                reelWriteConfig.disableProperty().set(false);
            } else
            {
                reelContainer.disableProperty().set(true);
                reelWriteConfig.disableProperty().set(true);
            }

            //TODO modify for multiple reels
            filamentID.setText(connectedPrinter.reelsProperty().get(0).filamentIDProperty().get());
            reelAmbientTemperature.setText(String.format("%d",
                                                         connectedPrinter.reelsProperty().get(0).ambientTemperatureProperty().get()));
            reelFirstLayerBedTemperature.setText(String.format("%d",
                                                               connectedPrinter.reelsProperty().get(0).firstLayerBedTemperatureProperty().get()));
            reelBedTemperature.setText(String.format("%d",
                                                     connectedPrinter.reelsProperty().get(0).bedTemperatureProperty().get()));
            reelFirstLayerNozzleTemperature.setText(String.format("%d",
                                                                  connectedPrinter.reelsProperty().get(0).firstLayerNozzleTemperatureProperty().get()));
            reelNozzleTemperature.setText(String.format("%d",
                                                        connectedPrinter.reelsProperty().get(0).nozzleTemperatureProperty().get()));
            reelFilamentMultiplier.setText(String.format("%.2f",
                                                         connectedPrinter.reelsProperty().get(0).filamentMultiplierProperty().get()));
            reelFeedRateMultiplier.setText(String.format("%.2f",
                                                         connectedPrinter.reelsProperty().get(0).feedRateMultiplierProperty().get()));
            reelRemainingFilament.setText(String.format("%.0f",
                                                        connectedPrinter.reelsProperty().get(0).remainingFilamentProperty().get()));
            reelFilamentDiameter.setText(String.format("%.2f",
                                                       connectedPrinter.reelsProperty().get(0).diameterProperty().get()));
            reelFilamentName.setText(connectedPrinter.reelsProperty().get(0).friendlyFilamentNameProperty().get());
            MaterialType reelMaterialTypeVal = connectedPrinter.reelsProperty().get(0).materialProperty().get();
            reelMaterialType.getSelectionModel().select(reelMaterialTypeVal);
            reelDisplayColor.setValue(connectedPrinter.reelsProperty().get(0).displayColourProperty().get());
        }
    };
    /*
     * Initializes the controller class.
     */

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        for (MaterialType materialType : MaterialType.values())
        {
            reelMaterialType.getItems().add(materialType);
        }

        statusScreenState = StatusScreenState.getInstance();

        statusScreenState.currentlySelectedPrinterProperty().addListener(
            new ChangeListener<Printer>()
            {

                @Override
                public void changed(ObservableValue<? extends Printer> observable, Printer oldValue,
                    Printer newValue)
                {
                    if (connectedPrinter != null)
                    {
                        unbindFromPrinter(connectedPrinter);
                    }

                    if (newValue != null)
                    {
                        bindToPrinter(newValue);
                    }
                }
            });
    }

    private void unbindFromPrinter(Printer printer)
    {
        if (connectedPrinter != null)
        {
            reelContainer.visibleProperty().unbind();
            reelWriteConfig.visibleProperty().unbind();
            saveFilamentAs.visibleProperty().unbind();

            connectedPrinter = null;
        }
    }

    private void bindToPrinter(Printer printer)
    {
        if (connectedPrinter == null)
        {
            connectedPrinter = printer;

            reelContainer.visibleProperty().bind(Bindings.isNotEmpty(connectedPrinter.reelsProperty()));
            reelWriteConfig.visibleProperty().bind(Bindings.isNotEmpty(connectedPrinter.reelsProperty()));
            saveFilamentAs.visibleProperty().bind(Bindings.isNotEmpty(connectedPrinter.reelsProperty()));
        }
    }
}
