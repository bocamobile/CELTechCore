package celtech.appManager;

import celtech.printerControl.comms.commands.rx.AckResponse;
import celtech.printerControl.model.HardwarePrinter;
import celtech.printerControl.model.Printer;
import celtech.services.firmware.FirmwareLoadResult;
import celtech.services.firmware.FirmwareLoadService;

/**
 *
 * @author Ian
 */
public class TestSystemNotificationManager implements SystemNotificationManager
{

    @Override
    public boolean askUserToDowngradeFirmware(int requiredFirmwareVersion, int actualFirmwareVersion)
    {
        return false;
    }

    @Override
    public boolean askUserToUpgradeFirmware(int requiredFirmwareVersion, int actualFirmwareVersion)
    {
        return false;
    }

    @Override
    public void processErrorPacketFromPrinter(AckResponse response, HardwarePrinter printer)
    {
    }

    @Override
    public void showCalibrationDialogue()
    {
    }

    @Override
    public void showDetectedPrintInProgressNotification()
    {
    }

    @Override
    public void showFirmwareUpgradeStatusNotification(FirmwareLoadResult result)
    {
    }

    @Override
    public void showGCodePostProcessFailedNotification()
    {
    }

    @Override
    public void showGCodePostProcessSuccessfulNotification()
    {
    }

    @Override
    public void showHeadUpdatedNotification()
    {
    }

    @Override
    public void showPrintJobCancelledNotification()
    {
    }

    @Override
    public void showPrintJobFailedNotification()
    {
    }

    @Override
    public void showPrintTransferInitiatedNotification()
    {
    }

    @Override
    public void showPrintTransferSuccessfulNotification(String printerName)
    {
    }

    @Override
    public void showReprintStartedNotification()
    {
    }

    @Override
    public void showSDCardNotification()
    {
    }

    @Override
    public void showSliceFailedNotification()
    {
    }

    @Override
    public void showSliceSuccessfulNotification()
    {
    }

    @Override
    public void configureFirmwareProgressDialog(FirmwareLoadService firmwareLoadService)
    {
    }

    @Override
    public void showNoSDCardDialog()
    {
    }

    @Override
    public void showNoPrinterIDDialog(Printer printer)
    {
    }
}
