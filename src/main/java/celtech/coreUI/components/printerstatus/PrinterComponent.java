/*
 * Copyright 2014 CEL UK
 */
package celtech.coreUI.components.printerstatus;

import celtech.coreUI.DisplayManager;
import celtech.printerControl.Printer;
import celtech.printerControl.PrinterStatusEnumeration;
import static celtech.printerControl.comms.commands.ColourStringConverter.colourToString;
import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 *
 * @author tony
 */
public class PrinterComponent extends Pane implements PropertyChangeListener
{

    private boolean selected;
    private Size currentSize;

    public enum Size
    {

        SIZE_SMALL, SIZE_MEDIUM, SIZE_LARGE;
    }

    public enum Status
    {

        READY, PRINTING, PAUSED, NOTIFICATION, ERROR
    }

    @FXML
    private Label name;

    @FXML
    private Pane innerPane;

    @FXML
    private WhiteProgressBarComponent progressBar;

    @FXML
    private PrinterSVGComponent printerSVG;
    private final Printer printer;
    private ChangeListener<String> nameListener;
    private ChangeListener<Color> colorListener;
    private ChangeListener<Number> progressListener;

    public PrinterComponent(Printer printer)
    {
        this.printer = printer;
        URL fxml = getClass().getResource("/celtech/resources/fxml/printerstatus/printer.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(fxml);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try
        {
            fxmlLoader.load();
        } catch (IOException exception)
        {
            throw new RuntimeException(exception);
        }

        initialise();
    }

    public void setStatus(Status status)
    {
        printerSVG.setStatus(status);
    }

    public void setName(String value)
    {
        nameTextProperty().set(value);
    }

    public StringProperty nameTextProperty()
    {
        return name.textProperty();
    }

    /**
     * Initialise the component
     */
    private void initialise()
    {

        setStyle("-fx-background-color: white;");

        name.setTextFill(Color.WHITE);
        name.setText(printer.getPrinterFriendlyName());
        setColour(printer.getPrinterColour());

        progressListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) ->
        {
            setProgress((double) newValue);
        };

        nameListener = (ObservableValue<? extends String> observable, String oldValue, String newValue) ->
        {
            setName(newValue);
        };

        colorListener = (ObservableValue<? extends Color> observable, Color oldValue, Color newValue) ->
        {
            setColour(newValue);
        };

        printer.printerFriendlyNameProperty().addListener(nameListener);
        printer.printerColourProperty().addListener(colorListener);
        printer.getPrintQueue().progressProperty().addListener(progressListener);
        printer.getPrintQueue().addPropertyChangeListener(this);

        setSize(Size.SIZE_LARGE);
        updateStatus(printer.getPrintQueue().getPrintStatus());
    }

    public void setProgress(double progress)
    {
        progressBar.setProgress(progress);
    }

    public void setColour(int color)
    {
        String colourHexString = String.format("#%06X", color);
        String style = "-fx-background-color: " + colourHexString + ";";
        innerPane.setStyle(style);
    }

    public void setColour(Color color)
    {
        String colourHexString = "#" + colourToString(color);
        String style = "-fx-background-color: " + colourHexString + ";";
        innerPane.setStyle(style);
    }

    public void setSelected(boolean select)
    {
        if (selected != select)
        {
            selected = select;
            redraw();
        }
    }

    public void setSize(Size size)
    {
        if (size != currentSize)
        {
            currentSize = size;
            redraw();
        }
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equals("printStatus")) {
            PrinterStatusEnumeration newStatus = (PrinterStatusEnumeration) evt.getNewValue();
            updateStatus(newStatus);
        }
    }

    private void updateStatus(PrinterStatusEnumeration newStatus)
    {
        Status status;
        switch (newStatus) {
            case ERROR:
                status = Status.ERROR;
                break;
            case EXECUTING_MACRO:
            case POST_PROCESSING:
            case PRINTING:
            case SENDING_TO_PRINTER:
            case SLICING:
                status = Status.PRINTING;
                break;
            case PAUSED:
                status = Status.PAUSED;
                break;
            default:
                status = Status.READY;
                break;
        }
        setStatus(status);
    }
    

    private void redraw()
    {
        int sizePixels;
        int fontSize;
        int progressBarWidth;
        int progressBarHeight;
        double progressBarYOffset;
        double nameLayoutY;
        int borderWidth;
        if (selected)
        {
            borderWidth = 3;
        } else
        {
            borderWidth = 0;
        }

        switch (currentSize)
        {
            case SIZE_SMALL:
                sizePixels = 80;
                fontSize = 9;
                progressBarWidth = 65;
                progressBarHeight = 6;
                progressBarYOffset = 17;

                break;
            case SIZE_MEDIUM:
                sizePixels = 120;
                fontSize = 14;
                progressBarWidth = 100;
                progressBarHeight = 9;
                progressBarYOffset = 26;
                break;
            default:
                sizePixels = 260;
                fontSize = 30;
                progressBarWidth = 220;
                progressBarHeight = 20;
                progressBarYOffset = 55;
                break;
        }
        setMinWidth(sizePixels);
        setMaxWidth(sizePixels);
        setMinHeight(sizePixels);
        setMaxHeight(sizePixels);

        double progressBarX = (sizePixels - progressBarWidth) / 2.0;
        double progressBarY = sizePixels - progressBarYOffset - progressBarHeight;

        innerPane.setMinWidth(sizePixels - borderWidth * 2);
        innerPane.setMaxWidth(sizePixels - borderWidth * 2);
        innerPane.setMinHeight(sizePixels - borderWidth * 2);
        innerPane.setMaxHeight(sizePixels - borderWidth * 2);
        innerPane.setTranslateX(borderWidth);
        innerPane.setTranslateY(borderWidth);
        printerSVG.setSize(sizePixels);
        progressBar.setLayoutX(progressBarX);
        progressBar.setLayoutY(progressBarY);
        progressBar.setControlWidth(progressBarWidth);
        progressBar.setControlHeight(progressBarHeight);

        for (Node child : innerPane.getChildren())
        {
            child.setTranslateX(-borderWidth);
            child.setTranslateY(-borderWidth);
        }

        name.setStyle("-fx-font-size: " + fontSize + "pt !important;");
        name.setLayoutX(progressBarX);

        Font primaryFont = DisplayManager.getInstance().getPrimaryFont();
        FontMetrics fontMetrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(primaryFont);
        double xHeight = fontMetrics.getXheight() * fontSize / primaryFont.getSize();
        nameLayoutY = sizePixels - (progressBarYOffset / 2) + xHeight / 2;
        name.setLayoutY(nameLayoutY);
    }
}
