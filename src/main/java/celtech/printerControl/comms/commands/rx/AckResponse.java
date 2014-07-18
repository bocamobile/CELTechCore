package celtech.printerControl.comms.commands.rx;

/**
 *
 * @author ianhudson
 */
public class AckResponse extends RoboxRxPacket
{
    /*
     Error flags as at firmware v577
     ERROR_SD_CARD 0
     ERROR_CHUNK_SEQUENCE 1
     ERROR_FILE_TOO_LARGE 2
     ERROR_GCODE_LINE_TOO_LONG 3
     ERROR_USB_RX 4
     ERROR_USB_TX 5
     ERROR_BAD_COMMAND 6
     ERROR_HEAD_EEPROM 7
     ERROR_BAD_FIRMWARE_FILE 8
     ERROR_FLASH_CHECKSUM 9
     ERROR_GCODE_BUFFER_OVERRUN 10
     ERROR_FILE_READ_CLOBBERED 11
     ERROR_MAX_GANTRY_ADJUSTMENT 12
     ERROR_REEL_EEPROM 13
     ERROR_E_FILAMENT_SLIP 14
     ERROR_D_FILAMENT_SLIP 15
     ERROR_NOZZLE_FLUSH_NEEDED 16
     */

    private final String charsetToUse = "US-ASCII";
    private byte[] errorFlags = new byte[32];
    private final int errorFlagBytes = 32;

    /*
     * Error flags - starting with byte 0
     */
    private boolean sdCardError = false;
    private boolean chunkSequenceError = false;
    private boolean fileTooLargeError = false;
    private boolean gcodeLineTooLongError = false;
    private boolean usbRXError = false;
    private boolean usbTXError = false;
    private boolean badCommandError = false;
    private boolean headEEPROMError = false;
    private boolean badFirmwareFileError = false;
    private boolean flashChecksumError = false;
    private boolean gcodeBufferOverrunError = false;
    private boolean fileReadClobbered = false;
    private boolean maxGantryAdjustment = false;
    private boolean reelEEPROMError = false;
    private boolean eFilamentSlipError = false;
    private boolean dFilamentSlipError = false;
    private boolean nozzleFlushNeededError = false;
    private boolean zTopSwitchError = false;
    private boolean unknown_error_code = false;

    /**
     *
     * @return
     */
    public boolean isSdCardError()
    {
        return sdCardError;
    }

    /**
     *
     * @return
     */
    public boolean isChunkSequenceError()
    {
        return chunkSequenceError;
    }

    /**
     *
     * @return
     */
    public boolean isFileTooLargeError()
    {
        return fileTooLargeError;
    }

    /**
     *
     * @return
     */
    public boolean isGcodeLineTooLongError()
    {
        return gcodeLineTooLongError;
    }

    /**
     *
     * @return
     */
    public boolean isUsbRXError()
    {
        return usbRXError;
    }

    /**
     *
     * @return
     */
    public boolean isUsbTXError()
    {
        return usbTXError;
    }

    /**
     *
     * @return
     */
    public boolean isBadCommandError()
    {
        return badCommandError;
    }

    /**
     *
     * @return
     */
    public boolean isHeadEepromError()
    {
        return headEEPROMError;
    }

    /**
     *
     * @return
     */
    public boolean isBadFirmwareFileError()
    {
        return badFirmwareFileError;
    }

    /**
     *
     * @return
     */
    public boolean isFlashChecksumError()
    {
        return flashChecksumError;
    }

    /**
     *
     * @return
     */
    public boolean isGCodeBufferOverrunError()
    {
        return gcodeBufferOverrunError;
    }

    /**
     *
     * @return
     */
    public boolean isFileReadClobbered()
    {
        return fileReadClobbered;
    }

    /**
     *
     * @return
     */
    public boolean isMaxGantryAdjustment()
    {
        return maxGantryAdjustment;
    }

    /**
     *
     * @return
     */
    public boolean isReelEEPROMError()
    {
        return reelEEPROMError;
    }

    /**
     *
     * @return
     */
    public boolean isEFilamentSlipError()
    {
        return eFilamentSlipError;
    }

    /**
     *
     * @return
     */
    public boolean isDFilamentSlipError()
    {
        return dFilamentSlipError;
    }

    /**
     *
     * @return
     */
    public boolean isNozzleFlushNeededError()
    {
        return nozzleFlushNeededError;
    }

    /**
     *
     * @return
     */
    public boolean isUnknown_Error_Code()
    {
        return unknown_error_code;
    }

    /**
     *
     * @return
     */
    public boolean isZTopSwitchError()
    {
        return zTopSwitchError;
    }

    /**
     *
     * @return
     */
    public boolean isError()
    {
        return sdCardError
            || chunkSequenceError
            || fileTooLargeError
            || gcodeLineTooLongError
            || usbRXError
            || usbTXError
            || badCommandError
            || headEEPROMError
            || badFirmwareFileError
            || flashChecksumError
            || gcodeBufferOverrunError
            || fileReadClobbered
            || maxGantryAdjustment
            || reelEEPROMError
            || eFilamentSlipError
            || dFilamentSlipError
            || nozzleFlushNeededError
            || zTopSwitchError
            || unknown_error_code;
    }

    /*
     * Errors...
     */
    /**
     *
     */
    public AckResponse()
    {
        super(RxPacketTypeEnum.ACK_WITH_ERRORS, false, false);
    }

    /**
     *
     * @param byteData
     * @return
     */
    @Override
    public boolean populatePacket(byte[] byteData)
    {
        boolean success = false;

        int byteOffset = 1;

        this.sdCardError = (byteData[byteOffset] & 1) > 0 ? true : false;
        byteOffset += 1;

        this.chunkSequenceError = (byteData[byteOffset] & 1) > 0 ? true : false;
        byteOffset += 1;

        this.fileTooLargeError = (byteData[byteOffset] & 1) > 0 ? true : false;
        byteOffset += 1;

        this.gcodeLineTooLongError = (byteData[byteOffset] & 1) > 0 ? true : false;
        byteOffset += 1;

        this.usbRXError = (byteData[byteOffset] & 1) > 0 ? true : false;
        byteOffset += 1;

        this.usbTXError = (byteData[byteOffset] & 1) > 0 ? true : false;
        byteOffset += 1;

        this.badCommandError = (byteData[byteOffset] & 1) > 0 ? true : false;
        byteOffset += 1;

        this.headEEPROMError = (byteData[byteOffset] & 1) > 0 ? true : false;
        byteOffset += 1;

        this.badFirmwareFileError = (byteData[byteOffset] & 1) > 0 ? true : false;
        byteOffset += 1;

        this.flashChecksumError = (byteData[byteOffset] & 1) > 0 ? true : false;
        byteOffset += 1;

        this.gcodeBufferOverrunError = (byteData[byteOffset] & 1) > 0 ? true : false;
        byteOffset += 1;

        this.fileReadClobbered = (byteData[byteOffset] & 1) > 0 ? true : false;
        byteOffset += 1;

        this.maxGantryAdjustment = (byteData[byteOffset] & 1) > 0 ? true : false;
        byteOffset += 1;

        this.reelEEPROMError = (byteData[byteOffset] & 1) > 0 ? true : false;
        byteOffset += 1;

        this.eFilamentSlipError = (byteData[byteOffset] & 1) > 0 ? true : false;
        byteOffset += 1;

        this.dFilamentSlipError = (byteData[byteOffset] & 1) > 0 ? true : false;
        byteOffset += 1;

        this.nozzleFlushNeededError = (byteData[byteOffset] & 1) > 0 ? true : false;
        byteOffset += 1;

        this.zTopSwitchError = (byteData[byteOffset] & 1) > 0 ? true : false;
        byteOffset += 1;

        unknown_error_code = false;
        for (; byteOffset < this.getPacketType().getPacketSize(); byteOffset++)
        {
            if ((byteData[byteOffset] & 1) > 0)
            {
                unknown_error_code = true;
            }
        }

        return !isError();
    }

    /**
     *
     * @return
     */
    public String getErrorsAsString()
    {
        StringBuilder outputString = new StringBuilder();

        if (isSdCardError())
        {
            outputString.append("SD card error");
        } else if (isChunkSequenceError())
        {
            outputString.append("Chunk sequence error");
        } else if (isFileTooLargeError())
        {
            outputString.append("File too large error");
        } else if (isGcodeLineTooLongError())
        {
            outputString.append("GCode line too long error");
        } else if (isUsbRXError())
        {
            outputString.append("USB RX error");
        } else if (isUsbTXError())
        {
            outputString.append("USB TX error");
        } else if (isBadCommandError())
        {
            outputString.append("Bad command error");
        } else if (isHeadEepromError())
        {
            outputString.append("Head EEPROM error");
        } else if (isBadFirmwareFileError())
        {
            outputString.append("Bad firmware error");
        } else if (isFlashChecksumError())
        {
            outputString.append("Flash Checksum error");
        } else if (isGCodeBufferOverrunError())
        {
            outputString.append("GCode overrun error");
        } else if (isFileReadClobbered())
        {
            outputString.append("File read clobbered error");
        } else if (isMaxGantryAdjustment())
        {
            outputString.append("Max gantry adjustment error");
        } else if (isReelEEPROMError())
        {
            outputString.append("Reel EEPROM error");
        } else if (isEFilamentSlipError())
        {
            outputString.append("Extruder 1 filament slip error");
        } else if (isDFilamentSlipError())
        {
            outputString.append("Extruder 2 filament slip error");
        } else if (isNozzleFlushNeededError())
        {
            outputString.append("Nozzle flush required error");
        } else if (isZTopSwitchError())
        {
            outputString.append("Z top switch error");
        } else if (isUnknown_Error_Code())
        {
            outputString.append("Unknown error from printer");
        }

        return outputString.toString();
    }

    /**
     *
     * @return
     */
    public String toString()
    {
        StringBuilder outputString = new StringBuilder();

        outputString.append("Error Report\n");
        outputString.append("Packet type:");
        outputString.append(getPacketType().name());
        outputString.append("\n");
        outputString.append("SD card error: " + isSdCardError());
        outputString.append("\n");
        outputString.append("Chunk sequence error: " + isChunkSequenceError());
        outputString.append("\n");
        outputString.append("File too large error: " + isFileTooLargeError());
        outputString.append("\n");
        outputString.append("GCode line too long error: " + isGcodeLineTooLongError());
        outputString.append("\n");
        outputString.append("USB RX error: " + isUsbRXError());
        outputString.append("\n");
        outputString.append("USB TX error: " + isUsbTXError());
        outputString.append("\n");
        outputString.append("Bad command error: " + isBadCommandError());
        outputString.append("\n");
        outputString.append("Head EEPROM error: " + isHeadEepromError());
        outputString.append("\n");
        outputString.append("Bad firmware error: " + isBadFirmwareFileError());
        outputString.append("\n");
        outputString.append("Flash Checksum error: " + isFlashChecksumError());
        outputString.append("\n");
        outputString.append("GCode overrun error: " + isGCodeBufferOverrunError());
        outputString.append("\n");
        outputString.append("File read clobbered error: " + isFileReadClobbered());
        outputString.append("\n");
        outputString.append("Max gantry adjustment error: " + isMaxGantryAdjustment());
        outputString.append("\n");
        outputString.append("Reel EEPROM error: " + isReelEEPROMError());
        outputString.append("\n");
        outputString.append("Extruder 1 filament slip error: " + isEFilamentSlipError());
        outputString.append("\n");
        outputString.append("Extruder 2 filament slip error: " + isDFilamentSlipError());
        outputString.append("\n");
        outputString.append("Nozzle flush required error: " + isNozzleFlushNeededError());
        outputString.append("\n");
        outputString.append("Z top switch error: " + isZTopSwitchError());
        outputString.append("\n");
        outputString.append("Unknown error: " + isUnknown_Error_Code());
        outputString.append("\n");
        outputString.append(">>>>>>>>>>\n");

        return outputString.toString();
    }
}
