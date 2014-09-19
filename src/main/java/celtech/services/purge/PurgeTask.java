/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package celtech.services.purge;

import celtech.coreUI.controllers.StatusScreenState;
import celtech.printerControl.Printer;
import celtech.printerControl.comms.commands.GCodeConstants;
import celtech.printerControl.comms.commands.exceptions.RoboxCommsException;
import celtech.services.ControllableService;
import celtech.utils.PrinterUtils;
import javafx.concurrent.Task;
import libertysystems.stenographer.Stenographer;
import libertysystems.stenographer.StenographerFactory;

/**
 *
 * @author Ian
 */
public class PurgeTask extends Task<PurgeStepResult> implements ControllableService
{

    private final Stenographer steno = StenographerFactory.getStenographer(PurgeTask.class.getName());
    private PurgeState desiredState = null;
    private int nozzleNumber = -1;

    private Printer printerToUse = null;
    private boolean keyPressed = false;

    private int purgeTemperature = 0;

    /**
     *
     * @param desiredState
     */
    public PurgeTask(PurgeState desiredState)
    {
        this.desiredState = desiredState;
    }

    @Override
    protected PurgeStepResult call() throws Exception
    {
        boolean success = false;

        StatusScreenState statusScreenState = StatusScreenState.getInstance();
        printerToUse = statusScreenState.getCurrentlySelectedPrinter();

        switch (desiredState)
        {
            case HEATING:
                try
                {
                    //Set the bed to 90 degrees C
                    int desiredBedTemperature = 90;
                    printerToUse.transmitDirectGCode(GCodeConstants.setBedTemperatureTarget + desiredBedTemperature, false);
                    printerToUse.transmitDirectGCode(GCodeConstants.goToTargetBedTemperature, false);
                    boolean bedHeatedOK = PrinterUtils.waitUntilTemperatureIsReached(printerToUse.bedTemperatureProperty(), this, desiredBedTemperature, 5, 600);

                    printerToUse.transmitDirectGCode(GCodeConstants.setFirstLayerNozzleTemperatureTarget + purgeTemperature, false);
                    printerToUse.transmitDirectGCode(GCodeConstants.goToTargetFirstLayerNozzleTemperature, false);
                    boolean extruderHeatedOK = PrinterUtils.waitUntilTemperatureIsReached(printerToUse.extruderTemperatureProperty(), this, purgeTemperature, 5, 300);

                    if (bedHeatedOK && extruderHeatedOK)
                    {
                        success = true;
                    }
                } catch (RoboxCommsException ex)
                {
                    steno.error("Error in purge - mode=" + desiredState.name());
                } catch (InterruptedException ex)
                {
                    steno.error("Interrrupted during purge - mode=" + desiredState.name());
                }

                break;

            case RUNNING_PURGE:
                printerToUse.transmitStoredGCode("Purge Material", false);
                PrinterUtils.waitOnMacroFinished(printerToUse, this);
                break;
        }

        return new PurgeStepResult(desiredState, success);
    }

    public void setPurgeTemperature(int purgeTemperature)
    {
        this.purgeTemperature = purgeTemperature;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean cancelRun()
    {
        return cancel();
    }
}
