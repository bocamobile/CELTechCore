/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package celtech.services.calibration;

import celtech.configuration.HeaterMode;
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
public class CalibrateBTask extends Task<NozzleBCalibrationStepResult> implements
    ControllableService
{

    private final Stenographer steno = StenographerFactory.getStenographer(
        CalibrateBTask.class.getName());
    private NozzleBCalibrationState desiredState = null;
    private int nozzleNumber = -1;

    private Printer printer = null;
    private boolean keyPressed = false;
    private boolean lookingForKeyPress = false;

    /**
     *
     * @param desiredState
     */
    public CalibrateBTask(NozzleBCalibrationState desiredState, Printer printer)
    {
        this.desiredState = desiredState;
        this.printer = printer;
    }

    /**
     *
     * @param desiredState
     * @param nozzleNumber
     */
    public CalibrateBTask(NozzleBCalibrationState desiredState, int nozzleNumber, Printer printer)
    {
        this.desiredState = desiredState;
        this.nozzleNumber = nozzleNumber;
        this.printer = printer;
    }

    @Override
    protected NozzleBCalibrationStepResult call() throws Exception
    {
        boolean success = false;

        switch (desiredState)
        {
            case HEATING:
                try
                {
                    printer.transmitDirectGCode("M104", false);
                    if (PrinterUtils.waitOnBusy(printer, this) == false)
                    {
                        printer.transmitStoredGCode("Home_all");
                        if (PrinterUtils.waitOnMacroFinished(printer, this) == false
                            && isCancelled() == false)
                        {
                            printer.transmitDirectGCode("G0 Z50", false);
                            if (PrinterUtils.waitOnBusy(printer, this) == false
                                && isCancelled() == false)
                            {
                                printer.transmitDirectGCode("M104", false);
                                if (printer.getNozzleHeaterMode() == HeaterMode.FIRST_LAYER)
                                {
                                    PrinterUtils.waitUntilTemperatureIsReached(printer.extruderTemperatureProperty(), this,
                                        printer.getNozzleFirstLayerTargetTemperature(), 5, 300);
                                } else
                                {
                                    PrinterUtils.waitUntilTemperatureIsReached(printer.extruderTemperatureProperty(), this,
                                        printer.getNozzleTargetTemperature(), 5, 300);
                                }
                                printer.transmitDirectGCode(GCodeConstants.switchOnHeadLEDs,
                                                                 false);
                            }
                        }
                    }

                } catch (RoboxCommsException ex)
                {
                    steno.error("Error in needle valve calibration - mode=" + desiredState.name());
                } catch (InterruptedException ex)
                {
                    steno.error("Interrrupted during needle valve calibration - mode="
                        + desiredState.name());
                }

                break;
            case NO_MATERIAL_CHECK:
                extrudeUntilStall();
                break;
            case MATERIAL_EXTRUDING_CHECK_FINE_NOZZLE:
                try
                {
                    extrudeForNozzle(0);
                } catch (RoboxCommsException ex)
                {
                    steno.error("Error in needle valve calibration - mode=" + desiredState.name());
                }
                break;
            case MATERIAL_EXTRUDING_CHECK_FILL_NOZZLE:
                try
                {
                    extrudeForNozzle(1);
                } catch (RoboxCommsException ex)
                {
                    steno.error("Error in needle valve calibration - mode=" + desiredState.name());
                }
                break;                
            case PRE_CALIBRATION_PRIMING:
                success = extrudeUntilStall();
                break;
            case POST_CALIBRATION_PRIMING:
                success = extrudeUntilStall();
                break;
            case CONFIRM_MATERIAL_EXTRUDING:
                try
                {
                    printer.transmitDirectGCode("T" + nozzleNumber, false);
                    printer.transmitDirectGCode("G0 B1", false);
                    if (nozzleNumber == 0)
                    {
                        printer.transmitDirectGCode("G1 E10 F75", false);
                    } else
                    {
                        printer.transmitDirectGCode("G1 E10 F100", false);
                    }
                    PrinterUtils.waitOnBusy(printer, this);
                } catch (RoboxCommsException ex)
                {
                    steno.error("Error in needle valve calibration - mode=" + desiredState.name());
                }
                break;
            case PARKING:
                try
                {
                    printer.transmitStoredGCode("Park");
                    success = true;
                } catch (RoboxCommsException ex)
                {
                    steno.error("Error in needle valve calibration - mode=" + desiredState.name());
                }
                break;
        }

        return new NozzleBCalibrationStepResult(desiredState, success);
    }

    private void extrudeForNozzle(int nozzleNumber) throws RoboxCommsException
    {
        printer.transmitDirectGCode("T" + nozzleNumber, false);
        printer.transmitDirectGCode("G0 B2", false);
        if (nozzleNumber == 0)
        {
            printer.transmitDirectGCode("G1 E10 F75", false);
        } else
        {
            printer.transmitDirectGCode("G1 E10 F100", false);
        }
        PrinterUtils.waitOnBusy(printer, this);
    }

    private boolean extrudeUntilStall()
    {
        boolean success = false;
        try
        {
            printer.transmitDirectGCode("T" + nozzleNumber, false);

            printer.transmitDirectGCode("G36 E700 F2000", false);
            PrinterUtils.waitOnBusy(printer, this);

            success = true;
        } catch (RoboxCommsException ex)
        {
            steno.error("Error in needle valve priming - mode=" + desiredState.name());
        }
        return success;
    }

//    private void waitForKeyPress() throws InterruptedException
//    {
//        lookingForKeyPress = true;
//        while (keyPressed == false && isCancelled() == false)
//        {
//            Thread.sleep(100);
//        }
//        keyPressed = false;
//        lookingForKeyPress = false;
//    }

    /**
     *
     * @return
     */
    @Override
    public boolean cancelRun()
    {
        return cancel();
    }

    /**
     *
     */
    public void keyPressed()
    {
        if (lookingForKeyPress)
        {
            keyPressed = true;
        }
    }
}
