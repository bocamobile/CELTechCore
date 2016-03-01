package celtech.printerControl.comms;

import celtech.printerControl.comms.commands.exceptions.ConnectionLostException;
import celtech.printerControl.comms.commands.exceptions.InvalidCommandByteException;
import celtech.printerControl.comms.commands.exceptions.InvalidResponseFromPrinterException;
import celtech.printerControl.comms.commands.exceptions.RoboxCommsException;
import celtech.printerControl.comms.commands.exceptions.UnableToGenerateRoboxPacketException;
import celtech.printerControl.comms.commands.exceptions.UnknownPacketTypeException;
import celtech.printerControl.comms.commands.rx.RoboxRxPacket;
import celtech.printerControl.comms.commands.rx.RoboxRxPacketFactory;
import celtech.printerControl.comms.commands.rx.RxPacketTypeEnum;
import celtech.printerControl.comms.commands.tx.RoboxTxPacket;
import celtech.printerControl.model.Printer;
import jssc.SerialPortException;

/**
 *
 * @author Ian Hudson @ Liberty Systems Limited
 */
public class RoboxRemoteCommandInterface extends CommandInterface
{

    private boolean stillWaitingForStatus = false;
    private final SerialPortManager serialPortManager;

    public RoboxRemoteCommandInterface(PrinterStatusConsumer controlInterface,
            DeviceDetector.DetectedPrinter printerHandle,
            boolean suppressPrinterIDChecks, int sleepBetweenStatusChecks)
    {
        super(controlInterface, printerHandle, suppressPrinterIDChecks, sleepBetweenStatusChecks);
        this.setName("HCI:" + printerHandle + " " + this.toString());
        serialPortManager = new SerialPortManager(printerHandle.getConnectionHandle());
    }

    @Override
    protected boolean connectToPrinter()
    {
        return serialPortManager.connect(115200);
    }

    @Override
    protected void disconnectPrinter()
    {
        if (serialPortManager.serialPort.isOpened())
        {
            try
            {
                serialPortManager.disconnect();
            } catch (SerialPortException ex)
            {
                steno.error("Failed to shut down serial port " + ex.getMessage());
            }
        }

        controlInterface.disconnected(printerHandle);
        keepRunning = false;
    }

    @Override
    public synchronized RoboxRxPacket writeToPrinter(RoboxTxPacket messageToWrite,
            boolean dontPublishResult) throws RoboxCommsException
    {
        RoboxRxPacket receivedPacket = null;

        if (commsState == RoboxCommsState.CONNECTED
                || commsState == RoboxCommsState.CHECKING_FIRMWARE
                || commsState == RoboxCommsState.CHECKING_ID
                || commsState == RoboxCommsState.DETERMINING_PRINTER_STATUS)
        {
            try
            {
                byte[] outputBuffer = messageToWrite.toByteArray();

                serialPortManager.writeAndWaitForData(outputBuffer);

                byte[] respCommand = serialPortManager.readSerialPort(1);

                RxPacketTypeEnum packetType = RxPacketTypeEnum.getEnumForCommand(respCommand[0]);
                if (packetType != null)
                {
                    if (packetType != messageToWrite.getPacketType().getExpectedResponse())
                    {
                        throw new InvalidResponseFromPrinterException(
                                "Expected response of type "
                                + messageToWrite.getPacketType().getExpectedResponse().name()
                                + " and got "
                                + packetType);
                    }
                    steno.trace("Got a response packet back of type: " + packetType.toString());
                    RoboxRxPacket rxPacketTemplate = RoboxRxPacketFactory.createPacket(packetType);
                    int packetLength = rxPacketTemplate.packetLength(firmwareVersionInUse);

                    byte[] inputBuffer = null;
                    if (packetType.containsLengthField())
                    {
                        byte[] lengthData = serialPortManager.readSerialPort(packetType.
                                getLengthFieldSize());

                        int payloadSize = Integer.valueOf(new String(lengthData), 16);
                        if (packetType == RxPacketTypeEnum.LIST_FILES_RESPONSE)
                        {
                            payloadSize = payloadSize * 16;
                        }

                        inputBuffer = new byte[1 + packetType.getLengthFieldSize() + payloadSize];
                        for (int i = 0; i < packetType.getLengthFieldSize(); i++)
                        {
                            inputBuffer[1 + i] = lengthData[i];
                        }

                        byte[] payloadData = serialPortManager.readSerialPort(payloadSize);
                        for (int i = 0; i < payloadSize; i++)
                        {
                            inputBuffer[1 + packetType.getLengthFieldSize() + i] = payloadData[i];
                        }
                    } else
                    {
                        inputBuffer = new byte[packetLength];
                        int bytesToRead = packetLength - 1;
                        byte[] payloadData = serialPortManager.readSerialPort(bytesToRead);
                        for (int i = 0; i < bytesToRead; i++)
                        {
                            inputBuffer[1 + i] = payloadData[i];
                        }
                    }

                    inputBuffer[0] = respCommand[0];

                    try
                    {
                        receivedPacket = RoboxRxPacketFactory.createPacket(inputBuffer, firmwareVersionInUse);
                        steno.
                                trace("Got packet of type " + receivedPacket.getPacketType().name());

                        if (!dontPublishResult)
                        {
                            printerToUse.processRoboxResponse(receivedPacket);
                        }
                    } catch (InvalidCommandByteException ex)
                    {
                        steno.error("Command byte of " + String.format("0x%02X", inputBuffer[0])
                                + " is invalid.");
                    } catch (UnknownPacketTypeException ex)
                    {
                        steno.error("Packet type unknown for command byte "
                                + String.format("0x%02X", inputBuffer[0]) + " is invalid.");
                    } catch (UnableToGenerateRoboxPacketException ex)
                    {
                        steno.error("A packet that appeared to be of type " + packetType.name()
                                + " could not be unpacked.");
                    }
                } else
                {
                    // Attempt to drain the crud from the input
                    // There shouldn't be anything here but just in case...
                    byte[] storage = serialPortManager.readAllDataOnBuffer();

                    try
                    {
                        String received = new String(storage);

                        steno.warning("Invalid packet received from firmware: " + received);
                    } catch (Exception e)
                    {
                        steno.warning(
                                "Invalid packet received from firmware - couldn't print contents");
                    }

//                    InvalidResponseFromPrinterException exception = new InvalidResponseFromPrinterException("Invalid response - got: " + received);
//                    throw exception;
                }
            } catch (SerialPortException ex)
            {
                steno.exception("Serial port exception", ex);
                actionOnCommsFailure();
            }
        } else
        {
            throw new RoboxCommsException("Invalid state for writing data");
        }
//        steno.debug("Command Interface send - completed " + messageToWrite.getPacketType());
        return receivedPacket;
    }

    private void actionOnCommsFailure() throws ConnectionLostException
    {
        //If we get an exception then abort and treat
        steno.debug("Error during write to printer");
        disconnectPrinter();
        keepRunning = false;
        throw new ConnectionLostException();
    }

    void setPrinterToUse(Printer newPrinter)
    {
        this.printerToUse = newPrinter;
    }

    /**
     *
     * @param sleepMillis
     */
    @Override
    public void setSleepBetweenStatusChecks(int sleepMillis)
    {
        sleepBetweenStatusChecks = sleepMillis;
    }
}