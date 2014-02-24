/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package celtech.coreUI.controllers.sidePanels;

import celtech.appManager.ApplicationMode;
import celtech.appManager.ApplicationStatus;
import celtech.appManager.Project;
import celtech.appManager.ProjectMode;
import celtech.coreUI.DisplayManager;
import celtech.coreUI.components.RestrictedTextField;
import celtech.coreUI.controllers.StatusScreenState;
import celtech.coreUI.visualisation.SelectionContainer;
import celtech.coreUI.visualisation.ThreeDViewManager;
import celtech.modelcontrol.ModelContainer;
import celtech.printerControl.Printer;
import celtech.printerControl.comms.commands.exceptions.RoboxCommsException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import libertysystems.stenographer.Stenographer;
import libertysystems.stenographer.StenographerFactory;

/**
 * FXML Controller class
 *
 * @author Ian Hudson @ Liberty Systems Limited
 */
public class LayoutSidePanelController implements Initializable
{

    private Stenographer steno = StenographerFactory.getStenographer(LayoutSidePanelController.class.getName());
    private Project boundProject = null;
    private ModelContainer boundModel = null;
    private StatusScreenState statusScreenState = null;

    @FXML
    private Slider xslider;
    @FXML
    private Slider yslider;
    @FXML
    private Slider zslider;

    @FXML
    private TextField depthTextField;

    @FXML
    private Label gcodeLineNumber;

    @FXML
    private Label layerNumber;

    @FXML
    private Label totalLayers;

    @FXML
    private VBox gcodePanel;

    @FXML
    private TextField heightTextField;

    @FXML
    private Slider maxLayerSlider;

    @FXML
    private VBox meshPanel;

    @FXML
    private Slider minLayerSlider;

    @FXML
    private TableView<ModelContainer> modelDataTableView;

    @FXML
    private TextField rotationTextField;

    @FXML
    private TextField scaleTextField;

    @FXML
    private VBox selectedGCodeBox;

    @FXML
    private CheckBox showSupport;

    @FXML
    private CheckBox showTravel;

    @FXML
    private Label totalGCodeLines;

    @FXML
    private TextField widthTextField;

    @FXML
    private TextField xAxisTextField;

    @FXML
    private TextField yAxisTextField;

    @FXML
    private Label minLayerValue;

    @FXML
    private Label maxLayerValue;

    @FXML
    private GridPane selectedItemGrid;

    private TableColumn modelNameColumn = new TableColumn();
    private TableColumn scaleColumn = new TableColumn();
    private TableColumn rotationColumn = new TableColumn();

    private SelectionContainer selectionContainer = null;
    private ListChangeListener<ModelContainer> selectionContainerModelsListener = null;
    private ChangeListener<ModelContainer> selectedItemListener = null;
    private DisplayManager displayManager = DisplayManager.getInstance();

    private StringConverter doubleTwoDigitsConverter = null;
    private StringConverter doubleOneDigitConverter = null;
    private ListChangeListener<ModelContainer> modelChangeListener = null;

    private ChangeListener<Number> modelScaleChangeListener = null;
    private ChangeListener<Number> modelRotationChangeListener = null;
    private ChangeListener<Number> widthListener = null;
    private ChangeListener<Number> heightListener = null;
    private ChangeListener<Number> depthListener = null;
    private ChangeListener<Number> xAxisListener = null;
    private ChangeListener<Number> yAxisListener = null;

    private final String scaleFormat = "%.0f";
    private final String rotationFormat = "%.0f";

    private ListChangeListener selectionListener = null;

    @FXML
    void changeToSettings(MouseEvent event)
    {
        ApplicationStatus.getInstance().setMode(ApplicationMode.SETTINGS);
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        statusScreenState = StatusScreenState.getInstance();

        xslider.valueProperty().addListener(new ChangeListener<Number>()
        {

            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1)
            {
                selectionContainer.selectedModelsProperty().get(0).setRotationX(t1.doubleValue());
            }
        });

        yslider.valueProperty().addListener(new ChangeListener<Number>()
        {

            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1)
            {
                selectionContainer.selectedModelsProperty().get(0).setRotationY(t1.doubleValue());
            }
        });

        zslider.valueProperty().addListener(new ChangeListener<Number>()
        {

            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1)
            {
                selectionContainer.selectedModelsProperty().get(0).setRotationZ(t1.doubleValue());
            }
        });

        ResourceBundle languageBundle = DisplayManager.getLanguageBundle();
        String modelNameLabelString = languageBundle.getString("sidePanel_layout.ModelNameLabel");
        String scaleLabelString = languageBundle.getString("sidePanel_layout.ScaleLabel");
        String rotationLabelString = languageBundle.getString("sidePanel_layout.RotationLabel");

        scaleTextField.setText("-");
        rotationTextField.setText("-");
        widthTextField.setText("-");
        depthTextField.setText("-");
        heightTextField.setText("-");
        xAxisTextField.setText("-");
        yAxisTextField.setText("-");

        doubleTwoDigitsConverter = new StringConverter<Double>()
        {
            @Override
            public String toString(Double t)
            {
                return String.format("%.2f", t);
            }

            @Override
            public Double fromString(String string)
            {
                return Double.valueOf(string);
            }
        };

        doubleOneDigitConverter = new StringConverter<Double>()
        {
            @Override
            public String toString(Double t)
            {
                return String.format("%.1f", t);
            }

            @Override
            public Double fromString(String string)
            {
                return Double.valueOf(string);
            }
        };

        modelNameColumn.setText(modelNameLabelString);
        modelNameColumn.setCellValueFactory(new PropertyValueFactory<ModelContainer, String>("modelName"));
        modelNameColumn.setMinWidth(170);
        modelNameColumn.setMaxWidth(170);
        modelNameColumn.setEditable(false);

        scaleColumn.setText(scaleLabelString);
        scaleColumn.setCellValueFactory(new PropertyValueFactory<ModelContainer, Double>("scale"));
        scaleColumn.setMinWidth(60);
        scaleColumn.setPrefWidth(60);
        scaleColumn.setMaxWidth(60);
        scaleColumn.setCellFactory(new Callback<TableColumn<ModelContainer, Double>, TableCell<ModelContainer, Double>>()
        {
            @Override
            public TableCell<ModelContainer, Double> call(TableColumn<ModelContainer, Double> param)
            {
                return new TableCell<ModelContainer, Double>()
                {

                    @Override
                    protected void updateItem(Double item, boolean empty)
                    {
                        super.updateItem(item, empty);

                        if (!empty)
                        {
                            // Use a SimpleDateFormat or similar in the format method
                            setText(String.format("%.0f%%", item * 100));
                        } else
                        {
                            setText(null);
                        }
                    }
                };
            }
        });

        rotationColumn.setText(rotationLabelString);
        rotationColumn.setCellValueFactory(new PropertyValueFactory<ModelContainer, Double>("rotationY"));
        rotationColumn.setMinWidth(60);
        rotationColumn.setPrefWidth(60);
        rotationColumn.setMaxWidth(60);
        rotationColumn.setCellFactory(new Callback<TableColumn<ModelContainer, Double>, TableCell<ModelContainer, Double>>()
        {
            @Override
            public TableCell<ModelContainer, Double> call(TableColumn<ModelContainer, Double> param)
            {
                return new TableCell<ModelContainer, Double>()
                {

                    @Override
                    protected void updateItem(Double item, boolean empty)
                    {
                        super.updateItem(item, empty);

                        if (!empty)
                        {
                            // Use a SimpleDateFormat or similar in the format method
                            setText(String.format("%.0fº", item));
                        } else
                        {
                            setText(null);
                        }
                    }
                };
            }
        });

        modelDataTableView.getColumns().addAll(modelNameColumn, scaleColumn, rotationColumn);
        modelDataTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        modelDataTableView.setEditable(true);
        modelDataTableView.getSortOrder().add(modelNameColumn);

        Label noModelsLoadedPlaceholder = new Label();
        noModelsLoadedPlaceholder.setText(languageBundle.getString("sidePanel_layout.noModelsLoaded"));
        modelDataTableView.setPlaceholder(noModelsLoadedPlaceholder);

        modelScaleChangeListener = new ChangeListener<Number>()
        {

            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1)
            {
                scaleTextField.setText(String.format(scaleFormat, t1.doubleValue() * 100));
            }
        };

        modelRotationChangeListener = new ChangeListener<Number>()
        {

            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1)
            {
                rotationTextField.setText(String.format(rotationFormat, t1));
            }
        };

        selectionListener = new ListChangeListener<ModelContainer>()
        {

            @Override
            public void onChanged(ListChangeListener.Change<? extends ModelContainer> change)
            {
                ModelContainer changedModel = null;
                while (change.next())
                {
                    if (change.wasAdded())
                    {
                        for (ModelContainer additem : change.getAddedSubList())
                        {
                            displayManager.selectModel(additem);
                            changedModel = additem;
                        }
                    } else if (change.wasRemoved())
                    {
                        for (ModelContainer additem : change.getRemoved())
                        {
                            displayManager.deselectModel(additem);
                        }
                    } else if (change.wasReplaced())
                    {
                        steno.info("Replaced: ");
                    } else if (change.wasUpdated())
                    {
                        steno.info("Updated: ");
                    }
                }
            }
        };

        modelDataTableView.getSelectionModel().getSelectedItems().addListener(selectionListener);

        selectionContainerModelsListener = new ListChangeListener<ModelContainer>()
        {

            @Override
            public void onChanged(ListChangeListener.Change<? extends ModelContainer> change)
            {
                while (change.next())
                {
                    if (change.wasAdded())
                    {
                        for (ModelContainer additem : change.getAddedSubList())
                        {
                            modelDataTableView.getSelectionModel().select(additem);
                        }
                    } else if (change.wasRemoved())
                    {
                        for (ModelContainer additem : change.getRemoved())
                        {
                            int modelIndex = modelDataTableView.getItems().indexOf(additem);
                            if (modelIndex != -1)
                            {
                                modelDataTableView.getSelectionModel().clearSelection(modelIndex);
                            }
                        }
                    } else if (change.wasReplaced())
                    {
                        steno.info("Replaced: ");
                    } else if (change.wasUpdated())
                    {
                        steno.info("Updated: ");
                    }
                }
            }
        };

        scaleTextField.setOnKeyPressed(
                new EventHandler<KeyEvent>()
                {

                    @Override
                    public void handle(KeyEvent t
                    )
                    {
                        switch (t.getCode())
                        {
                            case ENTER:
                            case TAB:
                                displayManager.getCurrentlyVisibleViewManager().scaleSelection(Double.valueOf(scaleTextField.getText()) / 100);
                                break;
                            case DECIMAL:
                            case BACK_SPACE:
                            case LEFT:
                            case RIGHT:
                                break;
                            default:
                                t.consume();
                                break;
                        }
                    }
                }
        );

        rotationTextField.setOnKeyPressed(
                new EventHandler<KeyEvent>()
                {

                    @Override
                    public void handle(KeyEvent t
                    )
                    {
                        switch (t.getCode())
                        {
                            case ENTER:
                            case TAB:
                                displayManager.getCurrentlyVisibleViewManager().rotateSelection(Double.valueOf(rotationTextField.getText()));
                                break;
                            case DECIMAL:
                            case BACK_SPACE:
                            case LEFT:
                            case RIGHT:
                                break;
                            default:
                                t.consume();
                                break;
                        }
                    }
                }
        );

        widthTextField.setOnKeyPressed(
                new EventHandler<KeyEvent>()
                {

                    @Override
                    public void handle(KeyEvent t
                    )
                    {
                        switch (t.getCode())
                        {
                            case ENTER:
                            case TAB:
                                displayManager.getCurrentlyVisibleViewManager().resizeSelectionWidth(Double.valueOf(widthTextField.getText()));
                                break;
                            case DECIMAL:
                            case BACK_SPACE:
                            case LEFT:
                            case RIGHT:
                                break;
                            default:
                                t.consume();
                                break;
                        }
                    }
                }
        );

        heightTextField.setOnKeyPressed(
                new EventHandler<KeyEvent>()
                {

                    @Override
                    public void handle(KeyEvent t
                    )
                    {
                        switch (t.getCode())
                        {
                            case ENTER:
                            case TAB:
                                displayManager.getCurrentlyVisibleViewManager().resizeSelectionHeight(Double.valueOf(heightTextField.getText()));
                                break;
                            case DECIMAL:
                            case BACK_SPACE:
                            case LEFT:
                            case RIGHT:
                                break;
                            default:
                                t.consume();
                                break;
                        }
                    }
                }
        );

        depthTextField.setOnKeyPressed(
                new EventHandler<KeyEvent>()
                {

                    @Override
                    public void handle(KeyEvent t
                    )
                    {
                        switch (t.getCode())
                        {
                            case ENTER:
                            case TAB:
                                displayManager.getCurrentlyVisibleViewManager().resizeSelectionDepth(Double.valueOf(depthTextField.getText()));
                                break;
                            case DECIMAL:
                            case BACK_SPACE:
                            case LEFT:
                            case RIGHT:
                                break;
                            default:
                                t.consume();
                                break;
                        }
                    }
                }
        );

        xAxisTextField.setOnKeyPressed(
                new EventHandler<KeyEvent>()
                {

                    @Override
                    public void handle(KeyEvent t
                    )
                    {
                        switch (t.getCode())
                        {
                            case ENTER:
                            case TAB:
                                displayManager.getCurrentlyVisibleViewManager().translateSelectionXTo(Double.valueOf(xAxisTextField.getText()));
                                break;
                            case DECIMAL:
                            case BACK_SPACE:
                            case LEFT:
                            case RIGHT:
                                break;
                            default:
                                t.consume();
                                break;
                        }
                    }
                }
        );

        yAxisTextField.setOnKeyPressed(
                new EventHandler<KeyEvent>()
                {

                    @Override
                    public void handle(KeyEvent t
                    )
                    {
                        switch (t.getCode())
                        {
                            case ENTER:
                            case TAB:
                                displayManager.getCurrentlyVisibleViewManager().translateSelectionZTo(Double.valueOf(yAxisTextField.getText()));
                                break;
                            case DECIMAL:
                            case BACK_SPACE:
                            case LEFT:
                            case RIGHT:
                                break;
                            default:
                                t.consume();
                                break;
                        }
                    }
                }
        );

        showTravel.selectedProperty()
                .addListener(new ChangeListener<Boolean>()
                        {
                            @Override
                            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1
                            )
                            {
                                boundProject.getLoadedModels().get(0).showTravel(t1.booleanValue());
                            }
                }
                );

        showSupport.selectedProperty()
                .addListener(new ChangeListener<Boolean>()
                        {
                            @Override
                            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1
                            )
                            {
                                boundProject.getLoadedModels().get(0).showSupport(t1.booleanValue());
                            }
                }
                );

        modelChangeListener = new ListChangeListener<ModelContainer>()
        {

            @Override
            public void onChanged(ListChangeListener.Change<? extends ModelContainer> change)
            {
                while (change.next())
                {
                    if (change.wasAdded())
                    {
                        for (ModelContainer additem : change.getAddedSubList())
                        {
                            boundModel = additem;
                            boundModel = boundProject.getLoadedModels().get(0);
                            minLayerSlider.maxProperty().bind(boundModel.numberOfLayersProperty());
                            maxLayerSlider.maxProperty().bind(boundModel.numberOfLayersProperty());
                            minLayerSlider.valueProperty().set(0);
                            maxLayerSlider.valueProperty().set(boundModel.numberOfLayersProperty().get());
                            boundModel.minLayerVisibleProperty().bind(minLayerSlider.valueProperty());
                            boundModel.maxLayerVisibleProperty().bind(maxLayerSlider.valueProperty());
                            gcodeLineNumber.textProperty().bind(boundModel.selectedGCodeLineProperty().asString());
                            totalGCodeLines.textProperty().bind(boundModel.linesOfGCodeProperty().asString());
                            layerNumber.textProperty().bind(boundModel.currentLayerProperty().asString());
                            totalLayers.textProperty().bind(boundModel.numberOfLayersProperty().asString());
                        }
                    } else if (change.wasRemoved())
                    {
                        for (ModelContainer additem : change.getRemoved())
                        {
                        }
                    } else if (change.wasReplaced())
                    {
                        steno.info("Replaced: ");
                    } else if (change.wasUpdated())
                    {
                        steno.info("Updated: ");
                    }
                }
            }
        };

        widthListener = new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1)
            {
                widthTextField.setText(doubleOneDigitConverter.toString(t1));
            }
        };

        heightListener = new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1)
            {
                heightTextField.setText(doubleOneDigitConverter.toString(t1));
            }
        };

        depthListener = new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1)
            {
                depthTextField.setText(doubleOneDigitConverter.toString(t1));
            }
        };

        xAxisListener = new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1)
            {
                xAxisTextField.setText(doubleOneDigitConverter.toString(t1));
            }
        };

        yAxisListener = new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1)
            {
                yAxisTextField.setText(doubleOneDigitConverter.toString(t1));
            }
        };

        minLayerValue.textProperty()
                .bind(minLayerSlider.valueProperty().asString("%.0f"));
        maxLayerValue.textProperty()
                .bind(maxLayerSlider.valueProperty().asString("%.0f"));

        minLayerSlider.valueProperty()
                .addListener(new ChangeListener<Number>()
                        {
                            @Override
                            public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue
                            )
                            {
                                if (newValue.doubleValue() >= maxLayerSlider.getValue())
                                {
                                    maxLayerSlider.adjustValue(newValue.doubleValue() + 1);
                                }
                            }
                }
                );

        maxLayerSlider.valueProperty()
                .addListener(new ChangeListener<Number>()
                        {
                            @Override
                            public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue
                            )
                            {
                                if (newValue.doubleValue() <= minLayerSlider.getValue())
                                {
                                    minLayerSlider.adjustValue(newValue.doubleValue() - 1);
                                }
                            }
                }
                );
    }

    public void bindLoadedModels(final ThreeDViewManager viewManager)
    {
        ObservableList<ModelContainer> loadedModels = viewManager.getLoadedModels();

        if (selectionContainer != null)
        {
            selectionContainer.selectedModelsProperty().removeListener(selectionContainerModelsListener);
            selectionContainer.widthProperty().removeListener(widthListener);
            selectionContainer.heightProperty().removeListener(heightListener);
            selectionContainer.depthProperty().removeListener(depthListener);
            selectionContainer.centreXProperty().removeListener(xAxisListener);
            selectionContainer.centreZProperty().removeListener(yAxisListener);
            selectionContainer.scaleProperty().removeListener(modelScaleChangeListener);
            selectionContainer.rotationYProperty().removeListener(modelRotationChangeListener);
            selectedItemGrid.visibleProperty().unbind();
        }

        selectionContainer = viewManager.getSelectionContainer();

        modelDataTableView.setItems(loadedModels);

        selectionContainer.selectedModelsProperty().addListener(selectionContainerModelsListener);

        if (selectionContainer.selectedModelsProperty().size() > 0)
        {
            widthTextField.setText(doubleOneDigitConverter.toString(selectionContainer.getWidth()));
            heightTextField.setText(doubleOneDigitConverter.toString(selectionContainer.getHeight()));
            depthTextField.setText(doubleOneDigitConverter.toString(selectionContainer.getDepth()));
            xAxisTextField.setText(doubleOneDigitConverter.toString(selectionContainer.getCentreX()));
            yAxisTextField.setText(doubleOneDigitConverter.toString(selectionContainer.getCentreZ()));
            scaleTextField.setText(String.format(scaleFormat, selectionContainer.getScale() * 100));
            rotationTextField.setText(String.format(rotationFormat, selectionContainer.getRotationY()));
        } else
        {
            widthTextField.setText("-");
            heightTextField.setText("-");
            depthTextField.setText("-");
            xAxisTextField.setText("-");
            yAxisTextField.setText("-");
            scaleTextField.setText("-");
            rotationTextField.setText("-");
        }

        selectedItemGrid.visibleProperty().bind(Bindings.isNotEmpty(selectionContainer.selectedModelsProperty()));
        selectionContainer.centreXProperty().addListener(xAxisListener);
        selectionContainer.centreZProperty().addListener(yAxisListener);
        selectionContainer.widthProperty().addListener(widthListener);
        selectionContainer.heightProperty().addListener(heightListener);
        selectionContainer.depthProperty().addListener(depthListener);
        selectionContainer.scaleProperty().addListener(modelScaleChangeListener);
        selectionContainer.rotationYProperty().addListener(modelRotationChangeListener);

        if (boundProject != null)
        {
            meshPanel.visibleProperty().unbind();
            gcodePanel.visibleProperty().unbind();
            boundProject.getLoadedModels().removeListener(modelChangeListener);
            minLayerSlider.maxProperty().unbind();
            maxLayerSlider.maxProperty().unbind();
            gcodeLineNumber.textProperty().unbind();
        }

        if (boundModel != null)
        {
            boundModel.maxLayerVisibleProperty().unbind();
            boundModel.minLayerVisibleProperty().unbind();
        }

        boundProject = displayManager.getCurrentlyVisibleProject();
        meshPanel.visibleProperty().bind(boundProject.projectModeProperty().isEqualTo(ProjectMode.MESH));
        gcodePanel.visibleProperty().bind(boundProject.projectModeProperty().isEqualTo(ProjectMode.GCODE));

        if (boundProject.getLoadedModels().size() > 0)
        {
            boundModel = boundProject.getLoadedModels().get(0);
            minLayerSlider.maxProperty().bind(boundModel.numberOfLayersProperty());
            maxLayerSlider.maxProperty().bind(boundModel.numberOfLayersProperty());
            minLayerSlider.valueProperty().set(0);
            maxLayerSlider.valueProperty().set(boundModel.numberOfLayersProperty().get());
            boundModel.minLayerVisibleProperty().bind(minLayerSlider.valueProperty());
            boundModel.maxLayerVisibleProperty().bind(maxLayerSlider.valueProperty());
            gcodeLineNumber.textProperty().bind(boundModel.selectedGCodeLineProperty().asString());
            totalGCodeLines.textProperty().bind(boundModel.linesOfGCodeProperty().asString());
            layerNumber.textProperty().bind(boundModel.currentLayerProperty().asString());
            totalLayers.textProperty().bind(boundModel.numberOfLayersProperty().asString());
        }

        boundProject.getLoadedModels().addListener(modelChangeListener);
    }

}
