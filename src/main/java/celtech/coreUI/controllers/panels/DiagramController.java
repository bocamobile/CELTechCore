/*
 * Copyright 2014 CEL UK
 */
package celtech.coreUI.controllers.panels;

import celtech.printerControl.model.calibration.NozzleHeightStateTransitionManager;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import libertysystems.stenographer.Stenographer;
import libertysystems.stenographer.StenographerFactory;

/**
 *
 * @author tony
 */
class DiagramController implements Initializable
{

    private final Stenographer steno = StenographerFactory.getStenographer(
        DiagramController.class.getName());

    private final CalibrationInsetPanelController parentController;

    public DiagramController(CalibrationInsetPanelController parentController)
    {
        this.parentController = parentController;
    }

    @FXML
    private TextField xOffsetA;

    @FXML
    private TextField yOffsetA;

    @FXML
    protected ComboBox cmbYOffset;

    @FXML
    protected ComboBox cmbXOffset;

    @FXML
    private TextField calibrationTextField;

    @FXML
    private TextField fineNozzleLbl;

    @FXML
    private TextField fillNozzleLbl;

    @FXML
    private HBox xOffsetComboContainer;

    @FXML
    private VBox yOffsetComboContainer;

    @FXML
    private HBox yOffsetContainerB;

    @FXML
    private HBox yOffsetContainerC;

    @FXML
    private HBox xOffsetContainerB;

    @FXML
    private HBox xOffsetContainerC;

    @FXML
    private HBox perfectAlignmentContainer;

    @FXML
    private HBox incorrectAlignmentContainer;

    @FXML
    private Button buttonB;

    @FXML
    private Button buttonA;

    @FXML
    void buttonAAction(ActionEvent event)
    {
        parentController.buttonAAction(event);
    }

    @FXML
    void buttonBAction(ActionEvent event)
    {
        parentController.buttonBAction(event);
    }

    @FXML
    void upButtonAction(ActionEvent event)
    {
        parentController.upButtonAction(event);
    }

    @FXML
    void downButtonAction(ActionEvent event)
    {
        parentController.downButtonAction(event);
    }

    protected void setupZCoListener(ReadOnlyDoubleProperty zcoProperty)
    {
        steno.debug("add zco listener");
        if (calibrationTextField != null)
        {
            calibrationTextField.setText(String.format("%1.2f", zcoProperty.get()));
        }
        zcoProperty.addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(
                ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
            {
                steno.debug("zco listener fired");
                if (calibrationTextField != null)
                {
                    steno.debug("set zco text to " + String.format("%1.2f", newValue));
                    calibrationTextField.setText(String.format("%1.2f", newValue));
                    if (newValue.floatValue() <= 0f)
                    {
                        buttonA.setDisable(true);
                    } else
                    {
                        buttonA.setDisable(false);
                    }
                }
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        setupOffsetCombos();
        steno.debug("calibrationTextField is " + calibrationTextField);
        if (parentController.stateManager instanceof NozzleHeightStateTransitionManager)
        {
            ReadOnlyDoubleProperty zcoProperty
                = ((NozzleHeightStateTransitionManager) parentController.stateManager).getZcoProperty();
            if (calibrationTextField != null)
            {
                calibrationTextField.setText(String.format("%1.2f", zcoProperty.get()));
            }
            setupZCoListener(zcoProperty);
        }
    }

    private void setupOffsetCombos()
    {
        if (cmbXOffset != null)
        {
            cmbXOffset.getItems().add("A");
            cmbXOffset.getItems().add("B");
            cmbXOffset.getItems().add("C");
            cmbXOffset.getItems().add("D");
            cmbXOffset.getItems().add("E");
            cmbXOffset.getItems().add("F");
            cmbXOffset.getItems().add("G");
            cmbXOffset.getItems().add("H");
            cmbXOffset.getItems().add("I");
            cmbXOffset.getItems().add("J");
            cmbXOffset.getItems().add("K");
            cmbYOffset.getItems().add("1");
            cmbYOffset.getItems().add("2");
            cmbYOffset.getItems().add("3");
            cmbYOffset.getItems().add("4");
            cmbYOffset.getItems().add("5");
            cmbYOffset.getItems().add("6");
            cmbYOffset.getItems().add("7");
            cmbYOffset.getItems().add("8");
            cmbYOffset.getItems().add("9");
            cmbYOffset.getItems().add("10");
            cmbYOffset.getItems().add("11");

            cmbXOffset.valueProperty().addListener(
                (ObservableValue observable, Object oldValue, Object newValue) ->
                {
                    parentController.setXOffset(newValue.toString());
                });

            cmbYOffset.valueProperty().addListener(
                (ObservableValue observable, Object oldValue, Object newValue) ->
                {
                    parentController.setYOffset(Integer.parseInt(newValue.toString()));
                });

            cmbXOffset.setValue("F");
            cmbYOffset.setValue("6");

            xOffsetA.setText(xOffsetA.getText() + ":");
            yOffsetA.setText(yOffsetA.getText() + ":");
        }
    }

    void setScale(double requiredScale, Node rootNode)
    {
        rootNode.setScaleX(requiredScale);
        rootNode.setScaleY(requiredScale);

        double invertedScale = 1 / requiredScale;

        if (cmbXOffset != null)
        {
            xOffsetComboContainer.setScaleX(invertedScale);
            xOffsetComboContainer.setScaleY(invertedScale);
            yOffsetComboContainer.setScaleX(invertedScale);
            yOffsetComboContainer.setScaleY(invertedScale);
            xOffsetContainerB.setScaleX(invertedScale);
            xOffsetContainerB.setScaleY(invertedScale);
            xOffsetContainerC.setScaleX(invertedScale);
            xOffsetContainerC.setScaleY(invertedScale);
            yOffsetContainerB.setScaleX(invertedScale);
            yOffsetContainerB.setScaleY(invertedScale);
            yOffsetContainerC.setScaleX(invertedScale);
            yOffsetContainerC.setScaleY(invertedScale);

            incorrectAlignmentContainer.setScaleX(invertedScale);
            incorrectAlignmentContainer.setScaleY(invertedScale);
            perfectAlignmentContainer.setScaleX(invertedScale);
            perfectAlignmentContainer.setScaleY(invertedScale);
        }

        if (fineNozzleLbl != null)
        {

            fineNozzleLbl.setScaleX(invertedScale);
            fineNozzleLbl.setScaleY(invertedScale);
            fillNozzleLbl.setScaleX(invertedScale);
            fillNozzleLbl.setScaleY(invertedScale);
        }
    }

}
