/*
 * Copyright 2015 CEL UK
 */
package celtech.printerControl.model;

import celtech.Lookup;
import celtech.configuration.Filament;
import celtech.printerControl.PrinterStatus;
import celtech.comms.remote.exceptions.RoboxCommsException;
import celtech.comms.remote.rx.HeadEEPROMDataResponse;
import celtech.utils.PrinterUtils;
import celtech.utils.tasks.Cancellable;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javax.print.PrintException;
import libertysystems.stenographer.Stenographer;
import libertysystems.stenographer.StenographerFactory;

/**
 *
 * @author tony
 */
public class PurgeActions extends StateTransitionActions
{

    public class PurgeException extends Exception
    {

        public PurgeException(String message)
        {
            super(message);
        }
    }

    private final Stenographer steno = StenographerFactory.getStenographer(
            PurgeActions.class.getName());

    private final Printer printer;

    private PurgePrinterErrorHandler printerErrorHandler;

    private HeadEEPROMDataResponse savedHeadData;

    private final List<Float> nozzleFilamentTemperature;
    private final List<IntegerProperty> lastDisplayTemperature;
    private final List<IntegerProperty> currentDisplayTemperature;
    private final List<IntegerProperty> purgeTemperature;

    private final BooleanProperty purgeNozzleHeater0;
    private final BooleanProperty purgeNozzleHeater1;
    /**
     * The filament that will be used during the purge, either the filament on
     * the current reel or a custom filament loaded on the SettingsScreen that
     * will be used for a print that has been requested.
     */
    private final List<Filament> purgeFilament;

    private boolean failedActionPerformed = false;
    private boolean doorNeedsOpening = false;

    PurgeActions(Printer printer, Cancellable userCancellable, Cancellable errorCancellable)
    {
        super(userCancellable, errorCancellable);
        this.printer = printer;
        PrinterUtils.setCancelledIfPrinterDisconnected(printer, errorCancellable);

        purgeTemperature = new ArrayList<>();
        lastDisplayTemperature = new ArrayList<>();
        currentDisplayTemperature = new ArrayList<>();
        nozzleFilamentTemperature = new ArrayList<>();
        purgeFilament = new ArrayList<>();
        for (int i = 0; i < getNumNozzleHeaters(); i++)
        {
            purgeTemperature.add(new SimpleIntegerProperty(0));
            lastDisplayTemperature.add(new SimpleIntegerProperty(0));
            currentDisplayTemperature.add(new SimpleIntegerProperty(0));
            nozzleFilamentTemperature.add(new Float(0));
            purgeFilament.add(null);
        }

        purgeNozzleHeater0 = new SimpleBooleanProperty(false);
        purgeNozzleHeater1 = new SimpleBooleanProperty(false);

    }

    @Override
    public void initialise()
    {

        for (int i = 0; i < getNumNozzleHeaters(); i++)
        {
            lastDisplayTemperature.get(i).set(0);
            currentDisplayTemperature.get(i).set(0);
            nozzleFilamentTemperature.set(i, 0f);
        }
        savedHeadData = null;
        purgeNozzleHeater0.set(false);
        purgeNozzleHeater1.set(false);
    }

    private void resetPrinter() throws PrinterException
    {
        printer.gotoNozzlePosition(0);
        printer.switchBedHeaterOff();
        switchHeatersAndHeadLightOff();

        PrinterUtils.waitOnBusy(printer, (Cancellable) null);
        try
        {
            // wait for above actions to complete so that AutoMaker does not return
            // to Status page until cancel/reset is complete
            Thread.sleep(2000);
        } catch (InterruptedException ex)
        {
            steno.error("Wait interrupted");
        }

    }

    public void doInitialiseAction() throws RoboxCommsException, PrintException
    {
        printer.setPrinterStatus(PrinterStatus.PURGING_HEAD);

        printerErrorHandler = new PurgePrinterErrorHandler(printer, errorCancellable);
        printerErrorHandler.registerForPrinterErrors();

        // put the write after the purge routine once the firmware no longer raises an error whilst connected to the host computer
        //TODO make PURGE work for dual material head
        savedHeadData = printer.readHeadEEPROM(true);
    }

    private int getNumNozzleHeaters()
    {
        return printer.headProperty().get().getNozzleHeaters().size();
    }

    void doHeatingAction() throws InterruptedException, PurgeException
    {

        //Set the bed to 90 degrees C
        int desiredBedTemperature = 90;
        printer.setBedTargetTemperature(desiredBedTemperature);
        printer.goToTargetBedTemperature();
        boolean bedHeatFailed = PrinterUtils.waitUntilTemperatureIsReached(
                printer.getPrinterAncillarySystems().bedTemperatureProperty(), null,
                desiredBedTemperature, 5, 600, userOrErrorCancellable);

        if (bedHeatFailed)
        {
            throw new PurgeException("Bed heat failed");
        }

        if (purgeNozzleHeater0.get())
        {
            printer.setNozzleHeaterTargetTemperature(0, purgeTemperature.get(0).get());
            printer.goToTargetNozzleHeaterTemperature(0);
        }

        if (purgeNozzleHeater1.get())
        {
            printer.setNozzleHeaterTargetTemperature(1, purgeTemperature.get(1).get());
            printer.goToTargetNozzleHeaterTemperature(1);
        }

        if (purgeNozzleHeater0.get())
        {
            boolean nozzleHeatFailed = PrinterUtils.waitUntilTemperatureIsReached(
                    printer.headProperty().get().getNozzleHeaters().get(0).nozzleTemperatureProperty(),
                    null, purgeTemperature.get(0).get(), 5, 300, userOrErrorCancellable);

            if (nozzleHeatFailed)
            {
                throw new PurgeException("Nozzle 0 heat failed");
            }
        }

        if (purgeNozzleHeater1.get())
        {
            boolean nozzleHeatFailed = PrinterUtils.waitUntilTemperatureIsReached(
                    printer.headProperty().get().getNozzleHeaters().get(1).nozzleTemperatureProperty(),
                    null, purgeTemperature.get(1).get(), 5, 300, userOrErrorCancellable);

            if (nozzleHeatFailed)
            {
                throw new PurgeException("Nozzle 1 heat failed");
            }
        }
    }

    void doRunPurgeAction() throws PrinterException
    {
        doorNeedsOpening = true;
        if (!purgeNozzleHeater0.get() && !purgeNozzleHeater1.get())
        {
            throw new RuntimeException("At least one nozzle must be purged");
        }

        boolean purgeNozzle0 = false;
        boolean purgeNozzle1 = false;

        if (printer.headProperty().get().headTypeProperty().get() == Head.HeadType.SINGLE_MATERIAL_HEAD)
        {
            purgeNozzle0 = true;
            purgeNozzle1 = true;
        } else
        {
            purgeNozzle0 = purgeNozzleHeater0.get();
            purgeNozzle1 = purgeNozzleHeater1.get();
        }

        printer.purgeMaterial(purgeNozzle0, purgeNozzle1, true, userOrErrorCancellable);
    }

    public void doFinishedAction() throws RoboxCommsException, PrinterException
    {
        float reel1FilamentTemperature = 0;
        if (nozzleFilamentTemperature.size() > 1)
        {
            reel1FilamentTemperature = nozzleFilamentTemperature.get(1);
        }

        printer.transmitWriteHeadEEPROM(
                savedHeadData.getHeadTypeCode(),
                savedHeadData.getUniqueID(),
                savedHeadData.getMaximumTemperature(),
                savedHeadData.getThermistorBeta(),
                savedHeadData.getThermistorTCal(),
                savedHeadData.getNozzle1XOffset(),
                savedHeadData.getNozzle1YOffset(),
                savedHeadData.getNozzle1ZOffset(),
                savedHeadData.getNozzle1BOffset(),
                savedHeadData.getFilamentID(0),
                savedHeadData.getFilamentID(1),
                savedHeadData.getNozzle2XOffset(),
                savedHeadData.getNozzle2YOffset(),
                savedHeadData.getNozzle2ZOffset(),
                savedHeadData.getNozzle2BOffset(),
                nozzleFilamentTemperature.get(0),
                reel1FilamentTemperature,
                savedHeadData.getHeadHours());
        printer.readHeadEEPROM(false);
        printer.setPrinterStatus(PrinterStatus.IDLE);
        deregisterPrinterErrorHandler();
    }

    private void openDoor()
    {
        // needs to run on gui thread to make sure it is called after status set to idle
        Lookup.getTaskExecutor().
                runOnGUIThread(() ->
                        {
                            try
                            {
                                printer.goToOpenDoorPosition(null);
                            } catch (PrinterException ex)
                            {
                                steno.warning("could not go to open door");
                            }
                });
    }

    public void doFailedAction() throws RoboxCommsException, PrinterException
    {
        // this can be called twice if an error occurs
        if (failedActionPerformed)
        {
            return;
        }

        failedActionPerformed = true;

        try
        {
            abortAnyOngoingPrint();
            resetPrinter();
        } catch (PrinterException ex)
        {
            steno.error("Error running failed action");
        }
        deregisterPrinterErrorHandler();
        printer.setPrinterStatus(PrinterStatus.IDLE);
        if (doorNeedsOpening)
        {
            openDoor();
        }
    }

    private void deregisterPrinterErrorHandler()
    {
        try
        {
            printerErrorHandler.deregisterForPrinterErrors();
        } catch (Exception ex)
        {
            steno.error("Error deregistering printer handler");
        }
    }

    private void switchHeatersAndHeadLightOff() throws PrinterException
    {
        printer.switchAllNozzleHeatersOff();
        printer.switchOffHeadLEDs();
    }

    public ReadOnlyIntegerProperty getLastMaterialTemperatureProperty(int nozzleHeaterNumber)
    {
        return lastDisplayTemperature.get(nozzleHeaterNumber);
    }

    public ReadOnlyIntegerProperty getCurrentMaterialTemperatureProperty(int nozzleHeaterNumber)
    {
        return currentDisplayTemperature.get(nozzleHeaterNumber);
    }

    public ReadOnlyIntegerProperty getPurgeTemperatureProperty(int nozzleHeaterNumber)
    {
        return purgeTemperature.get(nozzleHeaterNumber);
    }

    public void setPurgeTemperature(int nozzleHeaterNumber, int newPurgeTemperature)
    {
        purgeTemperature.get(nozzleHeaterNumber).set(newPurgeTemperature);
    }

    public void setPurgeFilament(int nozzleHeaterNumber, Filament filament) throws PrintException
    {
        purgeFilament.set(nozzleHeaterNumber, filament);
        updatePurgeTemperature(nozzleHeaterNumber, filament);
    }

    void setPurgeNozzleHeater0(boolean selected)
    {
        purgeNozzleHeater0.set(selected);
    }

    BooleanProperty getPurgeNozzleHeater0()
    {
        return purgeNozzleHeater0;
    }

    void setPurgeNozzleHeater1(boolean selected)
    {
        purgeNozzleHeater1.set(selected);
    }

    BooleanProperty getPurgeNozzleHeater1()
    {
        return purgeNozzleHeater1;
    }

    private void updatePurgeTemperature(int nozzleHeaterNumber, Filament filament) throws PrintException
    {
        // The nozzle should be heated to a temperature halfway between the last
        // temperature stored on the head and the current required temperature stored
        // on the reel
        if (purgeFilament.get(nozzleHeaterNumber) != null)
        {
            nozzleFilamentTemperature.set(nozzleHeaterNumber, (float)filament.getNozzleTemperature());
        } else
        {
            throw new PrintException("The purge filament must be set");
        }

        if (savedHeadData != null)
        {
            float temperatureDifference = nozzleFilamentTemperature.get(nozzleHeaterNumber)
                    - savedHeadData.getLastFilamentTemperature(nozzleHeaterNumber);
            lastDisplayTemperature.get(nozzleHeaterNumber).set(
                    (int) savedHeadData.getLastFilamentTemperature(nozzleHeaterNumber));
            currentDisplayTemperature.get(nozzleHeaterNumber).set(
                    nozzleFilamentTemperature.get(nozzleHeaterNumber).intValue());
            purgeTemperature.get(nozzleHeaterNumber).set((int) Math.min(savedHeadData.getMaximumTemperature(),
                    Math.max(180.0,
                            savedHeadData.getLastFilamentTemperature(nozzleHeaterNumber)
                            + (temperatureDifference / 2))));
        }

    }

    @Override
    /**
     * This is run immediately after the user presses the cancel button.
     */
    void whenUserCancelDetected()
    {
        abortAnyOngoingPrint();
    }

    @Override
    /**
     * This is run immediately after the printer error is detected.
     */
    void whenErrorDetected()
    {
        abortAnyOngoingPrint();
    }

    @Override
    /**
     * This is run after a Cancel or Error but not until any ongoing Action has
     * completed / stopped. We reset the printer here and not at the time of
     * error/cancel detection because if done immediately the ongoing Action
     * could undo the effects of the reset.
     */
    void resetAfterCancelOrError()
    {
        try
        {
            doFailedAction();
        } catch (Exception ex)
        {
            ex.printStackTrace();
            steno.error("error resetting printer " + ex);
        }
    }

    private void abortAnyOngoingPrint()
    {
        try
        {
            if (printer.canCancelProperty().get())
            {
                printer.cancel(null);
            }
        } catch (PrinterException ex)
        {
            steno.error("Failed to abort purge print - " + ex.getMessage());
        }
    }

}
