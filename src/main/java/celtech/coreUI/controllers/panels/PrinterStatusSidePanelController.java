package celtech.coreUI.controllers.panels;

import celtech.Lookup;
import celtech.appManager.ApplicationStatus;
import celtech.configuration.ApplicationConfiguration;
import celtech.coreUI.components.material.MaterialComponent;
import celtech.coreUI.components.printerstatus.PrinterGridComponent;
import celtech.roboxbase.BaseLookup;
import celtech.roboxbase.configuration.BaseConfiguration;
import celtech.roboxbase.printerControl.model.Extruder;
import celtech.roboxbase.printerControl.model.Head;
import celtech.roboxbase.printerControl.model.NozzleHeater;
import celtech.roboxbase.printerControl.model.Printer;
import celtech.roboxbase.printerControl.model.PrinterListChangesListener;
import celtech.roboxbase.printerControl.model.Reel;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import libertysystems.stenographer.Stenographer;
import libertysystems.stenographer.StenographerFactory;

/**
 * FXML Controller class
 *
 * @author Ian Hudson @ Liberty Systems Limited
 */
public class PrinterStatusSidePanelController implements Initializable, SidePanelManager,
        PrinterListChangesListener
{

    private final Stenographer steno = StenographerFactory.getStenographer(
            PrinterStatusSidePanelController.class.getName());

    private final ApplicationStatus applicationStatus = ApplicationStatus.getInstance();

    @FXML
    private VBox materialContainer;

    @FXML
    private HBox temperatureChartXLabels;

    @FXML
    private GridPane legendContainer;

    @FXML
    protected LineChart<Number, Number> temperatureChart;

    @FXML
    private NumberAxis temperatureAxis;

    @FXML
    private NumberAxis timeAxis;

    @FXML
    private Label legendNozzleS;

    @FXML
    private Label legendNozzleT;

    @FXML
    private Label legendBed;

    @FXML
    private Label legendAmbient;

    @FXML
    private PrinterGridComponent printerGridComponent;

    @FXML
    private VBox headPanel;

    @FXML
    private Label headTitleBold;

    @FXML
    private Label headTitleLight;

    @FXML
    private Label headDescription;

    @FXML
    private Label headNozzles;

    @FXML
    private Label headFeeds;

    @FXML
    private VBox headDataBox;

    @FXML
    private VBox noheadDataBox;

    @FXML
    private Group noHead;

    @FXML
    private Group singleMaterialLiteHead;

    @FXML
    private Group singleMaterialHead;

    @FXML
    private Group dualMaterialHead;

    @FXML
    private VBox graphContainer;

    @FXML
    private VBox graphAlternativeGrid;

    @FXML
    private Label graphAlternativeN1Temp;

    @FXML
    private Label graphAlternativeN2Temp;

    @FXML
    private Label graphAlternativeBedTemp;

    @FXML
    private Label graphAlternativeAmbientTemp;

    @FXML
    private Label graphAlternativeN1Legend;

    @FXML
    private Label graphAlternativeN2Legend;

    @FXML
    private Label graphAlternativeBedLegend;

    @FXML
    private Label graphAlternativeAmbientLegend;

    @FXML
    private VBox uberContainer;

    private Printer previousSelectedPrinter = null;

    private final int MAX_DATA_POINTS = 210;

    private LineChart.Series<Number, Number> currentAmbientTemperatureHistory = null;

    private ChartManager chartManager;

    private final ListChangeListener<XYChart.Data<Number, Number>> graphDataPointChangeListener
            = (ListChangeListener.Change<? extends XYChart.Data<Number, Number>> change) ->
            {
                while (change.next())
                {
                    if (change.wasAdded() || change.wasRemoved())
                    {
                        timeAxis.setLowerBound(currentAmbientTemperatureHistory.getData().size()
                                - MAX_DATA_POINTS);
                        timeAxis.setUpperBound(currentAmbientTemperatureHistory.getData().size());
                    } else if (change.wasReplaced())
                    {
                    } else if (change.wasUpdated())
                    {
                    }
                }
            };

    /**
     * Initialises the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        chartManager = new ChartManager(temperatureChart);

        Lookup.getSelectedPrinterProperty().addListener(
                (ObservableValue<? extends Printer> observable, Printer oldValue, Printer newValue) ->
                {
                    whenPrinterSelected(newValue);
                });

        graphAlternativeAmbientLegend.setVisible(false);
        graphAlternativeAmbientTemp.setVisible(false);
        graphAlternativeBedLegend.setVisible(false);
        graphAlternativeBedTemp.setVisible(false);
        showNoHead();

        initialiseTemperatureChart();
        controlDetailsVisibility();

        headPanel.setVisible(false);

        BaseLookup.getPrinterListChangesNotifier().addListener(this);

        uberContainer.heightProperty().addListener(new ChangeListener<Number>()
        {

            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1)
            {
                if (t1.intValue() < 140)
                {
                    graphContainer.setVisible(false);
                    graphContainer.setMaxHeight(0);
                    graphAlternativeGrid.setVisible(true);
                    graphAlternativeGrid.setMaxHeight(-1);
                } else
                {
                    graphContainer.setVisible(true);
                    graphContainer.setMaxHeight(-1);
                    graphAlternativeGrid.setVisible(false);
                    graphAlternativeGrid.setMaxHeight(0);
                }
            }
        });
    }

    private void initialiseTemperatureChart()
    {
        timeAxis = new NumberAxis(0, MAX_DATA_POINTS, 30);
        timeAxis.setForceZeroInRange(false);
        timeAxis.setAutoRanging(true);

        temperatureAxis = new NumberAxis();
        temperatureAxis.setAutoRanging(false);

        temperatureChart.setAnimated(false);
        temperatureChart.setLegendVisible(false);
        temperatureChart.setLegendSide(Side.RIGHT);

        temperatureChart.setVisible(false);

        graphAlternativeGrid.setVisible(false);
    }

    /**
     * When a printer is selected bind to it and show temperature chart etc if
     * necessary.
     *
     * @param printer
     */
    private void whenPrinterSelected(Printer printer)
    {
        if (previousSelectedPrinter != null)
        {
            unbindPrinter(previousSelectedPrinter);
            if (previousSelectedPrinter.headProperty().get() != null)
            {
                unbindHeadProperties(previousSelectedPrinter.headProperty().get());
            }
        }

        if (printer != null)
        {
            previousSelectedPrinter = printer;
            Lookup.setSelectedPrinter(printer);
            bindDetails(printer);
            if (printer.headProperty().get() != null)
            {
                bindHeadProperties(printer.headProperty().get());
            }
        }
        controlDetailsVisibility();
    }

    private void bindDetails(Printer printer)
    {
        if (Lookup.getSelectedPrinterProperty().get() != null)
        {
            unbindPrinter(Lookup.getSelectedPrinterProperty().get());
        }

        if (printer != null)
        {
            bindPrinter(printer);
        }
    }

    private void bindPrinter(Printer printer)
    {
        currentAmbientTemperatureHistory = printer.getPrinterAncillarySystems().getAmbientTemperatureHistory();
        chartManager.setLegendLabels(legendNozzleS, legendNozzleT, legendBed, legendAmbient);
        chartManager.bindPrinter(printer);

        graphAlternativeBedTemp.setVisible(true);
        graphAlternativeBedLegend.setVisible(true);
        graphAlternativeBedTemp.textProperty().bind(printer.getPrinterAncillarySystems().bedTemperatureProperty().asString("%d°C"));
        graphAlternativeAmbientTemp.setVisible(true);
        graphAlternativeAmbientLegend.setVisible(true);
        graphAlternativeAmbientTemp.textProperty().bind(printer.getPrinterAncillarySystems().ambientTemperatureProperty().asString("%d°C"));

        refreshMaterialContainer(printer);
    }

    private void refreshMaterialContainer(Printer printer)
    {
        materialContainer.getChildren().clear();
        for (int extruderNumber = 0; extruderNumber < 2; extruderNumber++)
        {
            Extruder extruder = printer.extrudersProperty().get(extruderNumber);
            if (extruder.isFittedProperty().get())
            {
                MaterialComponent materialComponent
                        = new MaterialComponent(printer, extruderNumber);
                materialContainer.getChildren().add(materialComponent);
                if (printer.extrudersProperty().size() > 1)
                {
                    materialComponent.setMaxHeight(110);
                } else
                {
                    materialComponent.setMaxHeight(120);
                }
            }
        }
    }

    private void unbindMaterialContainer()
    {
        materialContainer.getChildren().clear();
    }

    private void unbindPrinter(Printer printer)
    {
        if (printer.headProperty().get() != null)
        {
            unbindHeadProperties(printer.headProperty().get());
        }

        graphAlternativeBedTemp.setVisible(false);
        graphAlternativeBedLegend.setVisible(false);
        graphAlternativeBedTemp.textProperty().unbind();
        graphAlternativeAmbientTemp.setVisible(false);
        graphAlternativeAmbientLegend.setVisible(false);
        graphAlternativeAmbientTemp.textProperty().unbind();

        currentAmbientTemperatureHistory = null;
        chartManager.unbindPrinter();

        unbindMaterialContainer();
    }

    private void showNoHead()
    {
        noheadDataBox.setVisible(true);
        noHead.setVisible(true);
        headDataBox.setVisible(false);
        dualMaterialHead.setVisible(false);
        singleMaterialHead.setVisible(false);
        singleMaterialLiteHead.setVisible(false);
        graphAlternativeN1Legend.setVisible(false);
        graphAlternativeN1Temp.setVisible(false);
        graphAlternativeN2Legend.setVisible(false);
        graphAlternativeN2Temp.setVisible(false);
    }

    private void unbindHeadProperties(Head head)
    {
        head.getNozzleHeaters().get(0).getNozzleTemperatureHistory().getData().removeListener(
                graphDataPointChangeListener);
        chartManager.removeAllNozzles();

        graphAlternativeN1Temp.textProperty().unbind();
        graphAlternativeN2Temp.textProperty().unbind();

        showNoHead();
    }

    private void bindHeadProperties(Head head)
    {
        head.getNozzleHeaters().get(0).getNozzleTemperatureHistory().getData().addListener(
                graphDataPointChangeListener);

        if (head.getNozzleHeaters().size() > 0)
        {
            graphAlternativeN1Temp.setVisible(true);
            graphAlternativeN1Legend.setVisible(true);
            graphAlternativeN1Temp.textProperty().bind(Bindings
                    .when(head.getNozzleHeaters().get(0).nozzleTemperatureProperty()
                            .greaterThanOrEqualTo(BaseConfiguration.minTempToDisplayOnGraph))
                    .then(head.getNozzleHeaters().get(0).nozzleTemperatureProperty().asString("%d°C"))
                    .otherwise(Lookup.i18n("printerStatus.tempOutOfRangeLow")));

            if (head.headTypeProperty().get() == Head.HeadType.SINGLE_MATERIAL_HEAD)
            {
                legendNozzleS.setText(Lookup.i18n("sidePanel_printerStatus.nozzleLabel"));
            } else
            {
                legendNozzleS.setText(Lookup.i18n("printerStatus.temperatureGraphNozzleLabel0"));
        }

            NozzleHeater nozzleHeater = head.getNozzleHeaters().get(0);
            chartManager.addNozzle(0,
                    nozzleHeater.getNozzleTemperatureHistory(),
                    nozzleHeater.heaterModeProperty(),
                    nozzleHeater.nozzleTargetTemperatureProperty(),
                    nozzleHeater.nozzleFirstLayerTargetTemperatureProperty(),
                    nozzleHeater.nozzleTemperatureProperty());
        }

        if (head.getNozzleHeaters().size() > 1)
        {
            graphAlternativeN2Legend.setVisible(true);
            graphAlternativeN2Temp.setVisible(true);
            graphAlternativeN2Temp.textProperty().bind(Bindings
                    .when(head.getNozzleHeaters().get(1).nozzleTemperatureProperty()
                            .greaterThanOrEqualTo(BaseConfiguration.minTempToDisplayOnGraph))
                    .then(head.getNozzleHeaters().get(1).nozzleTemperatureProperty().asString("%d°C"))
                    .otherwise(Lookup.i18n("printerStatus.tempOutOfRangeLow")));
            graphAlternativeN1Legend.setText(Lookup.i18n("printerStatus.Nozzle1ControlLabel"));
            legendNozzleT.setText(Lookup.i18n("printerStatus.temperatureGraphNozzleLabel1"));
            NozzleHeater nozzleHeater = head.getNozzleHeaters().get(1);
            chartManager.addNozzle(1,
                    nozzleHeater.getNozzleTemperatureHistory(),
                    nozzleHeater.heaterModeProperty(),
                    nozzleHeater.nozzleTargetTemperatureProperty(),
                    nozzleHeater.nozzleFirstLayerTargetTemperatureProperty(),
                    nozzleHeater.nozzleTemperatureProperty());
        } else
        {
            graphAlternativeN1Legend.setText(Lookup.i18n("sidePanel_printerStatus.nozzleLabel"));
        }

        headTitleBold.setText(Lookup.i18n("headPanel." + head.typeCodeProperty().get() + ".titleBold"));
        headTitleLight.setText(Lookup.i18n("headPanel." + head.typeCodeProperty().get() + ".titleLight"));
        headDescription.setText(Lookup.i18n("headPanel." + head.typeCodeProperty().get() + ".description"));
        headNozzles.setText(Lookup.i18n("headPanel." + head.typeCodeProperty().get() + ".nozzles"));
        headFeeds.setText(Lookup.i18n("headPanel." + head.typeCodeProperty().get() + ".feeds"));

        if (head.headTypeProperty().get() == Head.HeadType.DUAL_MATERIAL_HEAD)
        {
            noheadDataBox.setVisible(false);
            noHead.setVisible(false);
            headDataBox.setVisible(true);
            dualMaterialHead.setVisible(true);
            singleMaterialHead.setVisible(false);
            singleMaterialLiteHead.setVisible(false);
        } else if (head.typeCodeProperty().get().equals("RBX01-SL"))
        {
            noheadDataBox.setVisible(false);
            noHead.setVisible(false);
            headDataBox.setVisible(true);
            dualMaterialHead.setVisible(false);
            singleMaterialHead.setVisible(false);
            singleMaterialLiteHead.setVisible(true);
        } else
        {
            noheadDataBox.setVisible(false);
            noHead.setVisible(false);
            headDataBox.setVisible(true);
            dualMaterialHead.setVisible(false);
            singleMaterialHead.setVisible(true);
            singleMaterialLiteHead.setVisible(false);
        }
    }

    private void controlDetailsVisibility()
    {
        boolean visible = Lookup.getSelectedPrinterProperty().get() != null;

        temperatureChart.setVisible(visible);
        temperatureChartXLabels.setVisible(visible);
        legendContainer.setVisible(visible);
        materialContainer.setVisible(visible);
    }

    @Override
    public void configure(Initializable slideOutController)
    {
    }

    @Override
    public void whenPrinterAdded(Printer printer)
    {
        controlDetailsVisibility();
        headPanel.setVisible(true);
    }

    @Override
    public void whenPrinterRemoved(Printer printer)
    {
        controlDetailsVisibility();
        headPanel.setVisible(false);
    }

    @Override
    public void whenHeadAdded(Printer printer)
    {
        if (printer == Lookup.getSelectedPrinterProperty().get())
        {
            Head head = printer.headProperty().get();
            bindHeadProperties(head);
        }
    }

    @Override
    public void whenHeadRemoved(Printer printer, Head head)
    {
        if (printer == Lookup.getSelectedPrinterProperty().get())
        {
            unbindHeadProperties(head);
        }
    }

    @Override
    public void whenReelAdded(Printer printer, int reelIndex)
    {
    }

    @Override
    public void whenReelRemoved(Printer printer, Reel reel, int reelNumber)
    {
    }

    @Override
    public void whenReelChanged(Printer printer, Reel reel)
    {
    }

    @Override
    public void whenExtruderAdded(Printer printer, int extruderIndex)
    {
        if (printer == Lookup.getSelectedPrinterProperty().get())
        {
            refreshMaterialContainer(printer);
        }
    }

    @Override
    public void whenExtruderRemoved(Printer printer, int extruderIndex)
    {
        if (printer == Lookup.getSelectedPrinterProperty().get())
        {
            refreshMaterialContainer(printer);
        }
    }
}
