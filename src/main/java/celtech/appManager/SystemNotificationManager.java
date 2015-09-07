package celtech.appManager;

import celtech.configuration.fileRepresentation.HeadFile;
import celtech.printerControl.comms.commands.rx.FirmwareError;
import celtech.printerControl.model.Printer;
import celtech.services.firmware.FirmwareLoadResult;
import celtech.services.firmware.FirmwareLoadService;
import celtech.utils.tasks.TaskResponder;
import java.util.Optional;

/**
 *
 * @author Ian
 */
public interface SystemNotificationManager
{

    public enum PrinterErrorChoice
    {

        CONTINUE, ABORT, RETRY, OK;
    }

    void showInformationNotification(String title, String message);

    void showWarningNotification(String title, String message);

    void showErrorNotification(String title, String message);

    /**
     * Returns true for no update and false for update
     *
     * @return True if the user has agreed to update, otherwise false
     */
    boolean askUserToUpdateFirmware();

    void processErrorPacketFromPrinter(FirmwareError error, Printer printer);

    void showCalibrationDialogue();

    void showFirmwareUpgradeStatusNotification(FirmwareLoadResult result);

    void showGCodePostProcessFailedNotification();

    void showGCodePostProcessSuccessfulNotification();

    void showHeadUpdatedNotification();

    void showPrintJobCancelledNotification();

    void showPrintJobFailedNotification();

    void showPrintTransferInitiatedNotification();

    void showPrintTransferSuccessfulNotification(String printerName);

    void showPrintTransferFailedNotification(String printerName);

    void removePrintTransferFailedNotification();

    void showReprintStartedNotification();

    void showSDCardNotification();

    void showSliceFailedNotification();

    void showSliceSuccessfulNotification();

    void configureFirmwareProgressDialog(FirmwareLoadService firmwareLoadService);

    public void showNoSDCardDialog();

    void showNoPrinterIDDialog(Printer printer);

    boolean showOpenDoorDialog();

    boolean showModelTooBigDialog(String modelFilename);

    boolean showApplicationUpgradeDialog(String applicationName);

    public PurgeResponse showPurgeDialog();

    public boolean showJobsTransferringShutdownDialog();

    public void showProgramInvalidHeadDialog(TaskResponder<HeadFile> taskResponse);

    public void showHeadNotRecognisedDialog(String printerName);

    /**
     * Show a dialog to the user asking them to choose between available Continue, Abort or Retry
     * actions when a printer error has occurred.
     */
    public Optional<PrinterErrorChoice> showPrinterErrorDialog(String title, String message,
        boolean showContinueOption,
        boolean showAbortOption, boolean showRetryOption, boolean showOKOption);

    public void showReelNotRecognisedDialog(String printerName);

    public void showReelUpdatedNotification();

    public void askUserToClearBed();

    public boolean confirmAdvancedMode();

    public void showKeepPushingFilamentNotification();

    public void hideKeepPushingFilamentNotification();

    public void showEjectFailedDialog(Printer printer);

    public void showFilamentMotionCheckBanner();

    public void hideFilamentMotionCheckBanner();

    public void showFilamentStuckMessage();

    public void showLoadFilamentNowMessage();
    
    public void informModelIsInvalid();
}
