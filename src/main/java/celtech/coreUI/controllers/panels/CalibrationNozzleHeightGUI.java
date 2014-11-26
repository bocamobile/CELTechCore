/*
 * Copyright 2014 CEL UK
 */
package celtech.coreUI.controllers.panels;

import celtech.printerControl.model.calibration.StateTransitionManager;
import celtech.printerControl.model.calibration.StateTransitionManager.GUIName;
import celtech.printerControl.model.calibration.StateTransition;
import celtech.services.calibration.NozzleOffsetCalibrationState;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import libertysystems.stenographer.Stenographer;
import libertysystems.stenographer.StenographerFactory;

/**
 *
 * @author tony
 */
public class CalibrationNozzleHeightGUI
{

    private final Stenographer steno = StenographerFactory.getStenographer(
        CalibrationNozzleHeightGUI.class.getName());

    private CalibrationInsetPanelController controller;
    StateTransitionManager<NozzleOffsetCalibrationState> stateManager;
    Map<GUIName, Button> namesToButtons = new HashMap<>();

    public CalibrationNozzleHeightGUI(CalibrationInsetPanelController controller,
        StateTransitionManager<NozzleOffsetCalibrationState> stateManager)
    {
        this.controller = controller;
        this.stateManager = stateManager;

        stateManager.stateGUITProperty().addListener(new ChangeListener()
        {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue)
            {
                setState((NozzleOffsetCalibrationState) newValue);
            }
        });
        populateNamesToButtons(controller);
    }

    private void showAppropriateButtons(NozzleOffsetCalibrationState state)
    {
        controller.hideAllInputControlsExceptStepNumber();
        if (state != NozzleOffsetCalibrationState.FAILED && state
            != NozzleOffsetCalibrationState.FINISHED)
        {
            controller.cancelCalibrationButton.setVisible(true);
        }
        for (StateTransition<NozzleOffsetCalibrationState> allowedTransition : this.stateManager.getTransitions())
        {
            if (namesToButtons.containsKey(allowedTransition.getGUIName()))
            {
                namesToButtons.get(allowedTransition.getGUIName()).setVisible(true);
            }
        }
    }

    public void setState(NozzleOffsetCalibrationState state)
    {
        steno.info("GUI going to state " + state);
        showAppropriateButtons(state);
        switch (state)
        {
            case IDLE:
                controller.calibrationStatus.setText(state.getStepTitle());
                controller.showDiagram("nozzleheight",
                                       "Nozzle Height Illustrations_Step 1 and 5.fxml");
                break;
            case INITIALISING:
                controller.calibrationMenu.disableNonSelectedItems();
                controller.calibrationStatus.setText(state.getStepTitle());
                controller.showDiagram("nozzleheight", "Nozzle Height Illustrations_Step 2.fxml");
                controller.stepNumber.setText(String.format("Step %s of 7", 1));
                break;
            case HEATING:
                controller.showSpinner();
                controller.setCalibrationProgressVisible(
                    CalibrationInsetPanelController.ProgressVisibility.TEMP);
                controller.calibrationStatus.setText(state.getStepTitle());
                controller.stepNumber.setText(String.format("Step %s of 7", 2));
                break;
            case HEAD_CLEAN_CHECK:
                controller.calibrationStatus.setText(state.getStepTitle());
                controller.showDiagram("nozzleheight", "Nozzle Height Illustrations_Step 4.fxml");
                controller.stepNumber.setText(String.format("Step %s of 7", 3));
                break;
            case MEASURE_Z_DIFFERENCE:
                controller.calibrationStatus.setText(state.getStepTitle());
                controller.showDiagram("nozzleheight",
                                       "Nozzle Height Illustrations_Step 1 and 5.fxml");
                controller.stepNumber.setText(String.format("Step %s of 7", 4));
                break;
            case INSERT_PAPER:
                controller.calibrationStatus.setText(state.getStepTitle());
                controller.showDiagram("nozzleheight", "Nozzle Height Illustrations_Step 6.fxml");
                controller.stepNumber.setText(String.format("Step %s of 7", 5));
                break;
            case PROBING:
                controller.calibrationStatus.setText(state.getStepTitle());
                controller.showDiagram("nozzleheight", "Nozzle Height Illustrations_Step 7.fxml",
                                       false);
                controller.stepNumber.setText(String.format("Step %s of 7", 6));
                break;
            case LIFT_HEAD:
                break;
            case REPLACE_PEI_BED:
                controller.calibrationStatus.setText(state.getStepTitle());
                controller.showDiagram("nozzleheight", "Nozzle Height Illustrations_Step 8.fxml");
                controller.stepNumber.setText(String.format("Step %s of 7", 7));
                break;
            case FINISHED:
                controller.calibrationStatus.setText(state.getStepTitle());
                controller.showDiagram("nozzleheight", "Nozzle Height Illustrations_Step 9.fxml");
                break;
            case FAILED:
                controller.calibrationStatus.setText(state.getStepTitle());
                controller.showDiagram("nozzleheight", "Nozzle Height Illustrations_Failure.fxml");
                controller.stepNumber.setText("");
                break;
        }
    }

    private void populateNamesToButtons(CalibrationInsetPanelController controller)
    {
        namesToButtons.put(GUIName.YES, controller.buttonA);
        namesToButtons.put(GUIName.NO, controller.buttonB);
        namesToButtons.put(GUIName.NEXT, controller.nextButton);
        namesToButtons.put(GUIName.RETRY, controller.retryPrintButton);
        namesToButtons.put(GUIName.START, controller.startCalibrationButton);
        namesToButtons.put(GUIName.BACK, controller.backToStatus);
    }

}
