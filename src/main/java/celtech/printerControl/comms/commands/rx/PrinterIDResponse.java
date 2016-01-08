package celtech.printerControl.comms.commands.rx;

import static celtech.printerControl.comms.commands.ColourStringConverter.stringToColor;
import celtech.printerControl.comms.commands.PrinterIDDataStructure;
import celtech.printerControl.comms.commands.StringToBase64Encoder;
import celtech.printerControl.comms.commands.tx.WritePrinterID;
import java.io.UnsupportedEncodingException;
import javafx.scene.paint.Color;

/**
 *
 * @author ianhudson
 */
public class PrinterIDResponse extends RoboxRxPacket
{

    private final String charsetToUse = "US-ASCII";

    private String model;
    private String edition;
    private String weekOfManufacture;
    private String yearOfManufacture;
    private String poNumber;
    private String serialNumber;
    private String checkByte;
    private String printerFriendlyName;
    private Color printerColour;

    /**
     *
     */
    public PrinterIDResponse()
    {
        super(RxPacketTypeEnum.PRINTER_ID_RESPONSE, false, false);
    }

    /**
     *
     * @param byteData
     * @return
     */
    @Override
    public boolean populatePacket(byte[] byteData, float requiredFirmwareVersion)
    {
        setMessagePayload(byteData);

        boolean success = false;

        try
        {
            int byteOffset = 1;

            this.model = new String(byteData, byteOffset, PrinterIDDataStructure.modelBytes, charsetToUse);
            this.model = this.model.trim();
            byteOffset += PrinterIDDataStructure.modelBytes;

            this.edition = new String(byteData, byteOffset, PrinterIDDataStructure.editionBytes, charsetToUse);
            this.edition = this.edition.trim();
            byteOffset += PrinterIDDataStructure.editionBytes;

            this.weekOfManufacture = new String(byteData, byteOffset, PrinterIDDataStructure.weekOfManufactureBytes, charsetToUse);
            this.weekOfManufacture = this.weekOfManufacture.trim();
            byteOffset += PrinterIDDataStructure.weekOfManufactureBytes;

            this.yearOfManufacture = new String(byteData, byteOffset, PrinterIDDataStructure.yearOfManufactureBytes, charsetToUse);
            this.yearOfManufacture = this.yearOfManufacture.trim();
            byteOffset += PrinterIDDataStructure.yearOfManufactureBytes;

            this.poNumber = new String(byteData, byteOffset, PrinterIDDataStructure.poNumberBytes, charsetToUse);
            this.poNumber = this.poNumber.trim();
            byteOffset += PrinterIDDataStructure.poNumberBytes;

            this.serialNumber = new String(byteData, byteOffset, PrinterIDDataStructure.serialNumberBytes, charsetToUse);
            this.serialNumber = this.serialNumber.trim();
            byteOffset += PrinterIDDataStructure.serialNumberBytes;

            this.checkByte = new String(byteData, byteOffset, PrinterIDDataStructure.checkByteBytes, charsetToUse);
            this.checkByte = this.checkByte.trim();
            byteOffset += PrinterIDDataStructure.checkByteBytes;

            byteOffset += WritePrinterID.BYTES_FOR_FIRST_PAD;

            this.printerFriendlyName = new String(byteData, byteOffset, PrinterIDDataStructure.printerFriendlyNameBytes, charsetToUse);
            this.printerFriendlyName = this.printerFriendlyName.trim();
            // beta testers will have unencoded printer names.
            // TODO: remove this test after 1.000.08 has been out for a while
            if (StringToBase64Encoder.isEncodedData(this.printerFriendlyName))
            {
                this.printerFriendlyName = StringToBase64Encoder.decode(this.printerFriendlyName);
            }
            byteOffset += PrinterIDDataStructure.printerFriendlyNameBytes;

            byteOffset += WritePrinterID.BYTES_FOR_SECOND_PAD;

            String colourDigits = new String(byteData, byteOffset,
                                             PrinterIDDataStructure.colourBytes * 3, charsetToUse);
            byteOffset += PrinterIDDataStructure.colourBytes * 3;

            printerColour = stringToColor(colourDigits);

            success = true;

        } catch (UnsupportedEncodingException ex)
        {
            steno.error("Failed to convert byte array to Printer ID Response");
        }

        return success;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString()
    {
        StringBuilder outputString = new StringBuilder();

        outputString.append(">>>>>>>>>>\n");
        outputString.append("Packet type:");
        outputString.append(getPacketType().name());
        outputString.append("\n");
        outputString.append("ID: " + getModel());
        outputString.append("\n");
        outputString.append(">>>>>>>>>>\n");

        return outputString.toString();
    }

    /**
     *
     * @return
     */
    public Color getPrinterColour()
    {
        return printerColour;
    }

    /**
     *
     * @return
     */
    public String getModel()
    {
        return model;
    }

    /**
     *
     * @return
     */
    public String getEdition()
    {
        return edition;
    }

    /**
     *
     * @return
     */
    public String getWeekOfManufacture()
    {
        return weekOfManufacture;
    }

    /**
     *
     * @return
     */
    public String getYearOfManufacture()
    {
        return yearOfManufacture;
    }

    /**
     *
     * @return
     */
    public String getPoNumber()
    {
        return poNumber;
    }

    /**
     *
     * @return
     */
    public String getSerialNumber()
    {
        return serialNumber;
    }

    /**
     *
     * @return
     */
    public String getCheckByte()
    {
        return checkByte;
    }

    /**
     *
     * @return
     */
    public String getPrinterFriendlyName()
    {
        return printerFriendlyName;
    }

    public void setModel(String model)
    {
        this.model = model;
    }

    public void setEdition(String edition)
    {
        this.edition = edition;
    }

    public void setWeekOfManufacture(String weekOfManufacture)
    {
        this.weekOfManufacture = weekOfManufacture;
    }

    public void setYearOfManufacture(String yearOfManufacture)
    {
        this.yearOfManufacture = yearOfManufacture;
    }

    public void setPoNumber(String poNumber)
    {
        this.poNumber = poNumber;
    }

    public void setSerialNumber(String serialNumber)
    {
        this.serialNumber = serialNumber;
    }

    public void setPrinterColour(Color printerColour)
    {
        this.printerColour = printerColour;
    }

    public void setPrinterFriendlyName(String printerFriendlyName)
    {
        this.printerFriendlyName = printerFriendlyName;
    }

    @Override
    public int packetLength(float requiredFirmwareVersion)
    {
        return 257;
    }

}
