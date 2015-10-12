package celtech.coreUI.controllers.utilityPanels;

import celtech.Lookup;
import celtech.configuration.HeaterMode;
import celtech.coreUI.controllers.StatusInsetController;
import celtech.printerControl.PrintQueueStatus;
import celtech.printerControl.model.Head;
import celtech.printerControl.model.Printer;
import celtech.printerControl.model.PrinterException;
import celtech.printerControl.model.Reel;
import celtech.utils.PrinterListChangesListener;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import libertysystems.stenographer.Stenographer;
import libertysystems.stenographer.StenographerFactory;

/**
 * FXML Controller class
 *
 * @author Ian Hudson @ Liberty Systems Limited
 */
public class TweakPanelController implements Initializable, StatusInsetController, PrinterListChangesListener
{

    private final Stenographer steno = StenographerFactory.getStenographer(TweakPanelController.class.getName());

    @FXML
    private VBox container;

    @FXML
    private Slider speedMultiplierSlider;

    @FXML
    private Slider extrusionMultiplier1Slider;

    @FXML
    private VBox extrusionMultiplier1Box;

    @FXML
    private Slider extrusionMultiplier2Slider;

    @FXML
    private VBox extrusionMultiplier2Box;

    @FXML
    private Slider nozzleTemperature1Slider;

    @FXML
    private VBox nozzleTemperature1Box;

    @FXML
    private Slider nozzleTemperature2Slider;

    @FXML
    private VBox nozzleTemperature2Box;

    @FXML
    private Slider bedTemperatureSlider;

    private Printer currentPrinter = null;

    private final ChangeListener<PrintQueueStatus> printQueueStatusListener = new ChangeListener<PrintQueueStatus>()
    {
        @Override
        public void changed(ObservableValue<? extends PrintQueueStatus> ov, PrintQueueStatus t, PrintQueueStatus newStatus)
        {
            if (newStatus == PrintQueueStatus.PRINTING)
            {
                unbind();
                bind();
            } else
            {
                unbind();
            }
        }
    };

    private boolean inhibitFeedrate = false;
    private final ChangeListener<Number> feedRateChangeListener
            = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) ->
            {
                inhibitFeedrate = true;
                speedMultiplierSlider.setValue(newValue.doubleValue() * 100.0);
                inhibitFeedrate = false;
            };

    private final ChangeListener<Number> speedMultiplierSliderListener
            = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) ->
            {
                if (!inhibitFeedrate)
                {
                    try
                    {
                        steno.info("Writing feedrate");
                        currentPrinter.changeFeedRateMultiplier(newValue.doubleValue() / 100.0);
                    } catch (PrinterException ex)
                    {
                        steno.error("Error setting feed rate multiplier - " + ex.getMessage());
                    }
                }
            };

    private boolean inhibitBed = false;
    private final ChangeListener<Number> bedTargetTemperatureChangeListener
            = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) ->
            {
                inhibitBed = true;
                bedTemperatureSlider.setValue(newValue.doubleValue());
                inhibitBed = false;
            };

    private final ChangeListener<Number> bedTempSliderListener
            = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) ->
            {
                if (!inhibitBed)
                {
                    steno.info("Writing bed");
                    currentPrinter.setBedTargetTemperature(newValue.intValue());
                }
            };

    private boolean inhibitExtrusion1 = false;
    private final ChangeListener<Number> extrusionMultiplier1ChangeListener
            = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) ->
            {
                inhibitExtrusion1 = true;
                extrusionMultiplier1Slider.setValue(newValue.doubleValue() * 100.0);
                inhibitExtrusion1 = false;
            };

    private final ChangeListener<Number> extrusionMultiplier1SliderListener
            = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) ->
            {
                if (!inhibitExtrusion1)
                {
                    try
                    {
                        steno.info("Writing extrusion 1");

                        currentPrinter.changeFilamentInfo("E", currentPrinter.extrudersProperty().get(0).filamentDiameterProperty().get(), newValue.doubleValue() / 100.0);
                    } catch (PrinterException ex)
                    {
                        steno.error("Failed to set extrusion multiplier");
                    }
                }
            };

    private boolean inhibitExtrusion2 = false;
    private final ChangeListener<Number> extrusionMultiplier2ChangeListener
            = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) ->
            {
                inhibitExtrusion2 = true;
                extrusionMultiplier2Slider.setValue(newValue.doubleValue() * 100.0);
                inhibitExtrusion2 = false;
            };

    private final ChangeListener<Number> extrusionMultiplier2SliderListener
            = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) ->
            {
                if (!inhibitExtrusion2)
                {
                    try
                    {
                        steno.info("Writing extrusion 2");

                        currentPrinter.changeFilamentInfo("D", currentPrinter.extrudersProperty().get(1).filamentDiameterProperty().get(), newValue.doubleValue() / 100.0);
                    } catch (PrinterException ex)
                    {
                        steno.error("Failed to set extrusion multiplier");
                    }
                }
            };

    private boolean inhibitNozzleTemp1 = false;
    private final ChangeListener<Number> nozzleTemp1ChangeListener
            = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) ->
            {
                inhibitNozzleTemp1 = true;
                nozzleTemperature1Slider.setValue(newValue.intValue());
                inhibitNozzleTemp1 = false;
            };

    private final ChangeListener<Number> nozzleTemp1SliderListener
            = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) ->
            {
                if (!inhibitNozzleTemp1)
                {
                    steno.info("Writing nozzle 1");

                    currentPrinter.setNozzleHeaterTargetTemperature(0, newValue.intValue());
                }
            };

    private boolean inhibitNozzleTemp2 = false;
    private final ChangeListener<Number> nozzleTemp2ChangeListener
            = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) ->
            {
                inhibitNozzleTemp2 = true;
                nozzleTemperature2Slider.setValue(newValue.intValue());
                inhibitNozzleTemp2 = false;
            };

    private final ChangeListener<Number> nozzleTemp2SliderListener
            = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) ->
            {
                if (!inhibitNozzleTemp2)
                {
                    steno.info("Writing nozzle 2");

                    currentPrinter.setNozzleHeaterTargetTemperature(1, newValue.intValue());
                }
            };

    private final ChangeListener<HeaterMode> heaterModeListener
            = (ObservableValue<? extends HeaterMode> observable, HeaterMode oldValue, HeaterMode newValue) ->
            {
                updateNozzleTemperatureDisplay();
            };

    /**
     * Initialises the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        container.setVisible(false);
        Lookup.getSelectedPrinterProperty().addListener(
                (ObservableValue<? extends Printer> observable, Printer oldValue, Printer newValue) ->
                {
                    bindToPrintEngineStatus(newValue);
                });

        Lookup.getPrinterListChangesNotifier().addListener(this);

        if (Lookup.getSelectedPrinterProperty().get() != null)
        {
            bindToPrintEngineStatus(Lookup.getSelectedPrinterProperty().get());
        }
    }

    private void bindToPrintEngineStatus(Printer printer)
    {
        if (currentPrinter != null)
        {
            currentPrinter.getPrintEngine().printQueueStatusProperty().removeListener(printQueueStatusListener);
        }

        currentPrinter = printer;
        if (currentPrinter != null)
        {
            printer.getPrintEngine().printQueueStatusProperty().addListener(printQueueStatusListener);
            if (printer.getPrintEngine().printQueueStatusProperty().get() == PrintQueueStatus.PRINTING)
            {
                bind();
            } else
            {
                unbind();
            }
            Lookup.getPrinterListChangesNotifier().addListener(this);

        }
    }

    private void bind()
    {
        speedMultiplierSlider.setValue(currentPrinter.getPrinterAncillarySystems().
                feedRateMultiplierProperty().get() * 100.0);
        speedMultiplierSlider.valueProperty().addListener(speedMultiplierSliderListener);
        currentPrinter.getPrinterAncillarySystems().feedRateMultiplierProperty().addListener(
                feedRateChangeListener);

        bedTemperatureSlider.setValue(currentPrinter.getPrinterAncillarySystems().
                bedTargetTemperatureProperty().get());
        bedTemperatureSlider.valueProperty().addListener(bedTempSliderListener);
        currentPrinter.getPrinterAncillarySystems().bedTargetTemperatureProperty().addListener(
                bedTargetTemperatureChangeListener);

        if (currentPrinter.extrudersProperty().get(0).isFittedProperty().get())
        {
            extrusionMultiplier1Slider.setValue(currentPrinter.extrudersProperty().get(0).extrusionMultiplierProperty().doubleValue() * 100.0);
            extrusionMultiplier1Slider.valueProperty().addListener(extrusionMultiplier1SliderListener);
            currentPrinter.extrudersProperty().get(0).extrusionMultiplierProperty().addListener(extrusionMultiplier1ChangeListener);
        }

        if (currentPrinter.extrudersProperty().get(1).isFittedProperty().get())
        {
            extrusionMultiplier2Slider.setValue(currentPrinter.extrudersProperty().get(1).extrusionMultiplierProperty().doubleValue() * 100.0);
            extrusionMultiplier2Slider.valueProperty().addListener(extrusionMultiplier2SliderListener);
            currentPrinter.extrudersProperty().get(1).extrusionMultiplierProperty().addListener(extrusionMultiplier2ChangeListener);
            extrusionMultiplier2Box.setVisible(true);
            extrusionMultiplier2Box.setMaxHeight(1000);
            extrusionMultiplier2Box.setMinHeight(0);
        } else
        {
            extrusionMultiplier2Box.setVisible(false);
            extrusionMultiplier2Box.setMaxHeight(0);
            extrusionMultiplier2Box.setMinHeight(0);
        }

        updateNozzleTemperatureDisplay();
        container.setVisible(true);
    }

    private void unbind()
    {
        container.setVisible(false);
        speedMultiplierSlider.valueProperty().removeListener(speedMultiplierSliderListener);
        bedTemperatureSlider.valueProperty().removeListener(bedTempSliderListener);
        extrusionMultiplier1Slider.valueProperty().removeListener(extrusionMultiplier1SliderListener);
        extrusionMultiplier2Slider.valueProperty().removeListener(extrusionMultiplier2SliderListener);

        currentPrinter.getPrinterAncillarySystems().feedRateMultiplierProperty().removeListener(
                feedRateChangeListener);
        currentPrinter.getPrinterAncillarySystems().bedTargetTemperatureProperty().removeListener(
                bedTargetTemperatureChangeListener);
        if (currentPrinter.extrudersProperty().get(0).isFittedProperty().get())
        {
            currentPrinter.extrudersProperty().get(0).extrusionMultiplierProperty().removeListener(
                    extrusionMultiplier1ChangeListener);
        }
        if (currentPrinter.extrudersProperty().get(1).isFittedProperty().get())
        {
            currentPrinter.extrudersProperty().get(1).extrusionMultiplierProperty().removeListener(
                    extrusionMultiplier2ChangeListener);
        }

        updateNozzleTemperatureDisplay();
    }

    private void updateNozzleTemperatureDisplay()
    {
        if (currentPrinter != null
                && currentPrinter.headProperty().get() != null)
        {
            if (currentPrinter.headProperty().get().headTypeProperty().get() == Head.HeadType.SINGLE_MATERIAL_HEAD)
            {
                if (currentPrinter.headProperty().get().getNozzleHeaters().get(0).heaterModeProperty().get() == HeaterMode.FIRST_LAYER)
                {
                    nozzleTemperature1Slider.setValue(currentPrinter.headProperty().get().getNozzleHeaters().get(0).nozzleFirstLayerTargetTemperatureProperty().get());
                } else
                {
                    nozzleTemperature1Slider.setValue(currentPrinter.headProperty().get().getNozzleHeaters().get(0).nozzleTargetTemperatureProperty().get());
                }
                nozzleTemperature1Slider.valueProperty().addListener(nozzleTemp1SliderListener);
                currentPrinter.headProperty().get().getNozzleHeaters().get(0).nozzleTargetTemperatureProperty().addListener(nozzleTemp1ChangeListener);

                currentPrinter.headProperty().get().getNozzleHeaters().get(0).heaterModeProperty().addListener(heaterModeListener);

                nozzleTemperature1Box.setVisible(true);
                nozzleTemperature1Box.setMaxHeight(1000);
                nozzleTemperature1Box.setMinHeight(0);
                nozzleTemperature2Box.setVisible(false);
                nozzleTemperature2Box.setMaxHeight(0);
                nozzleTemperature2Box.setMinHeight(0);
            } else
            {
                if (currentPrinter.headProperty().get().getNozzleHeaters().get(0).heaterModeProperty().get() == HeaterMode.FIRST_LAYER)
                {
                    nozzleTemperature1Slider.setValue(currentPrinter.headProperty().get().getNozzleHeaters().get(0).nozzleFirstLayerTargetTemperatureProperty().get());
                } else if (currentPrinter.headProperty().get().getNozzleHeaters().get(0).heaterModeProperty().get() == HeaterMode.NORMAL)
                {
                    nozzleTemperature1Slider.setValue(currentPrinter.headProperty().get().getNozzleHeaters().get(0).nozzleTargetTemperatureProperty().get());
                }
                nozzleTemperature1Slider.valueProperty().addListener(nozzleTemp1SliderListener);
                currentPrinter.headProperty().get().getNozzleHeaters().get(0).nozzleTargetTemperatureProperty().addListener(nozzleTemp1ChangeListener);
                currentPrinter.headProperty().get().getNozzleHeaters().get(0).nozzleFirstLayerTargetTemperatureProperty().addListener(nozzleTemp1ChangeListener);
                currentPrinter.headProperty().get().getNozzleHeaters().get(0).heaterModeProperty().addListener(heaterModeListener);

                if (currentPrinter.headProperty().get().getNozzleHeaters().get(1).heaterModeProperty().get() == HeaterMode.FIRST_LAYER)
                {
                    nozzleTemperature2Slider.setValue(currentPrinter.headProperty().get().getNozzleHeaters().get(1).nozzleFirstLayerTargetTemperatureProperty().get());
                } else if (currentPrinter.headProperty().get().getNozzleHeaters().get(1).heaterModeProperty().get() == HeaterMode.NORMAL)
                {
                    nozzleTemperature2Slider.setValue(currentPrinter.headProperty().get().getNozzleHeaters().get(1).nozzleTargetTemperatureProperty().get());
                }
                nozzleTemperature2Slider.valueProperty().addListener(nozzleTemp2SliderListener);
                currentPrinter.headProperty().get().getNozzleHeaters().get(1).nozzleTargetTemperatureProperty().addListener(nozzleTemp2ChangeListener);
                currentPrinter.headProperty().get().getNozzleHeaters().get(1).nozzleFirstLayerTargetTemperatureProperty().addListener(nozzleTemp2ChangeListener);
                currentPrinter.headProperty().get().getNozzleHeaters().get(1).heaterModeProperty().addListener(heaterModeListener);

                nozzleTemperature1Box.setVisible(true);
                nozzleTemperature1Box.setMaxHeight(1000);
                nozzleTemperature1Box.setMinHeight(0);
                nozzleTemperature2Box.setVisible(true);
                nozzleTemperature2Box.setMaxHeight(1000);
                nozzleTemperature2Box.setMinHeight(0);
            }
        } else
        {
            nozzleTemperature1Slider.valueProperty().removeListener(nozzleTemp1SliderListener);

            nozzleTemperature1Box.setVisible(false);
            nozzleTemperature1Box.setMaxHeight(0);
            nozzleTemperature1Box.setMinHeight(0);

            nozzleTemperature2Slider.valueProperty().removeListener(nozzleTemp2SliderListener);

            nozzleTemperature2Box.setVisible(false);
            nozzleTemperature2Box.setMaxHeight(0);
            nozzleTemperature2Box.setMinHeight(0);

            if (currentPrinter.headProperty().get() != null)
            {
                currentPrinter.headProperty().get().getNozzleHeaters().get(0).nozzleTargetTemperatureProperty().removeListener(nozzleTemp1ChangeListener);
                currentPrinter.headProperty().get().getNozzleHeaters().get(0).nozzleFirstLayerTargetTemperatureProperty().removeListener(nozzleTemp1ChangeListener);
                currentPrinter.headProperty().get().getNozzleHeaters().get(0).heaterModeProperty().removeListener(heaterModeListener);

                currentPrinter.headProperty().get().getNozzleHeaters().get(1).nozzleTargetTemperatureProperty().removeListener(nozzleTemp2ChangeListener);
                currentPrinter.headProperty().get().getNozzleHeaters().get(1).nozzleFirstLayerTargetTemperatureProperty().removeListener(nozzleTemp2ChangeListener);
                currentPrinter.headProperty().get().getNozzleHeaters().get(1).heaterModeProperty().removeListener(heaterModeListener);
            }
        }
    }

    @Override
    public void whenPrinterAdded(Printer printer)
    {
    }

    @Override
    public void whenPrinterRemoved(Printer printer)
    {
    }

    @Override
    public void whenHeadAdded(Printer printer)
    {
        steno.info("Head added");
        updateNozzleTemperatureDisplay();
    }

    @Override
    public void whenHeadRemoved(Printer printer, Head head)
    {
        steno.info("Head removed");
        updateNozzleTemperatureDisplay();
    }

    @Override
    public void whenReelAdded(Printer printer, int reelIndex)
    {
    }

    @Override
    public void whenReelRemoved(Printer printer, Reel reel, int reelIndex)
    {
    }

    @Override
    public void whenReelChanged(Printer printer, Reel reel)
    {
    }

    @Override
    public void whenExtruderAdded(Printer printer, int extruderIndex)
    {
        steno.info("Extruder added");

    }

    @Override
    public void whenExtruderRemoved(Printer printer, int extruderIndex)
    {
    }

}
