package celtech.coreUI.controllers.panels;

import celtech.Lookup;
import celtech.appManager.ApplicationMode;
import celtech.appManager.ApplicationStatus;
import celtech.appManager.ModelContainerProject;
import celtech.appManager.Project;
import celtech.configuration.ApplicationConfiguration;
import celtech.roboxbase.configuration.datafileaccessors.HeadContainer;
import celtech.roboxbase.configuration.datafileaccessors.SlicerParametersContainer;
import celtech.roboxbase.configuration.fileRepresentation.SlicerParametersFile;
import celtech.roboxbase.configuration.fileRepresentation.PrinterSettingsOverrides;
import celtech.coreUI.controllers.ProjectAwareController;
import celtech.modelcontrol.ModelContainer;
import celtech.modelcontrol.ProjectifiableThing;
import celtech.roboxbase.BaseLookup;
import celtech.roboxbase.configuration.BaseConfiguration;
import celtech.roboxbase.configuration.Filament;
import celtech.roboxbase.printerControl.model.Head;
import celtech.roboxbase.printerControl.model.Printer;
import celtech.roboxbase.services.slicer.PrintQualityEnumeration;
import celtech.roboxbase.printerControl.model.PrinterListChangesAdapter;
import celtech.roboxbase.printerControl.model.PrinterListChangesListener;
import celtech.roboxbase.utils.tasks.Cancellable;
import celtech.roboxbase.utils.tasks.SimpleCancellable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import libertysystems.stenographer.Stenographer;
import libertysystems.stenographer.StenographerFactory;
import org.apache.commons.io.FileUtils;

/**
 * FXML Controller class
 *
 * @author Ian Hudson @ Liberty Systems Limited
 */
public class TimeCostInsetPanelController implements Initializable, ProjectAwareController
{

    private final Stenographer steno = StenographerFactory.getStenographer(
            TimeCostInsetPanelController.class.getName());

    @FXML
    private HBox timeCostInsetRoot;

    @FXML
    private Label lblDraftTime;
    @FXML
    private Label lblNormalTime;
    @FXML
    private Label lblFineTime;
    @FXML
    private Label lblCustomTime;
    @FXML
    private Label lblDraftWeight;
    @FXML
    private Label lblNormalWeight;
    @FXML
    private Label lblFineWeight;
    @FXML
    private Label lblCustomWeight;
    @FXML
    private Label lblDraftCost;
    @FXML
    private Label lblNormalCost;
    @FXML
    private Label lblFineCost;
    @FXML
    private Label lblCustomCost;
    @FXML
    private RadioButton rbDraft;
    @FXML
    private RadioButton rbNormal;
    @FXML
    private RadioButton rbFine;
    @FXML
    private RadioButton rbCustom;
    @FXML
    private Label headType;

    private ToggleGroup qualityToggleGroup;

    private Project currentProject;
    private PrinterSettingsOverrides printerSettings;
    private Printer currentPrinter;
    private String currentHeadType;

    private final TimeCostThreadManager timeCostThreadManager = TimeCostThreadManager.getInstance();

    private MapChangeListener<Integer, Filament> effectiveFilamentListener = new MapChangeListener<Integer, Filament>()
    {

        @Override
        public void onChanged(MapChangeListener.Change<? extends Integer, ? extends Filament> change)
        {
            updateFields(currentProject);
        }
    };

    private final PrinterListChangesListener printerListChangesListener = new PrinterListChangesAdapter()
    {
        @Override
        public void whenHeadAdded(Printer printer)
        {
            if (printer == currentPrinter)
            {
                updateHeadType(currentPrinter);
            }
        }

        @Override
        public void whenHeadRemoved(Printer printer, Head head)
        {
            if (printer == currentPrinter)
            {
                updateHeadType(currentPrinter);
            }
        }
    };

    private final ChangeListener<Printer> selectedPrinterChangeListener = new ChangeListener<Printer>()
    {
        @Override
        public void changed(ObservableValue<? extends Printer> observable, Printer oldValue, Printer newValue)
        {
            if (currentPrinter != null)
            {
                currentPrinter.effectiveFilamentsProperty().removeListener(effectiveFilamentListener);
            }
            if (newValue != null)
            {
                newValue.effectiveFilamentsProperty().addListener(effectiveFilamentListener);
            }
            updateHeadType(newValue);

            if (currentPrinter != newValue)
            {
                updateFields(currentProject);
            }
            currentPrinter = newValue;
        }
    };

    private final ChangeListener<ApplicationMode> applicationModeChangeListener = new ChangeListener<ApplicationMode>()
    {
        @Override
        public void changed(ObservableValue<? extends ApplicationMode> observable, ApplicationMode oldValue, ApplicationMode newValue)
        {
            if (newValue == ApplicationMode.SETTINGS)
            {
                timeCostInsetRoot.setVisible(true);
                timeCostInsetRoot.setMouseTransparent(false);
                if (Lookup.getSelectedProjectProperty().get() == currentProject)
                {
                    updateFields(currentProject);
                }
            } else
            {
                timeCostInsetRoot.setVisible(false);
                timeCostInsetRoot.setMouseTransparent(true);
                timeCostThreadManager.cancelRunningTimeCostTasks();
            }
        }
    };

    /**
     * Initialises the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        try
        {
            BaseLookup.getPrinterListChangesNotifier().addListener(printerListChangesListener);

            Lookup.getSelectedPrinterProperty().addListener(selectedPrinterChangeListener);

            if (Lookup.getSelectedPrinterProperty().get() != null)
            {
                currentPrinter = Lookup.getSelectedPrinterProperty().get();
                currentPrinter.effectiveFilamentsProperty().addListener(effectiveFilamentListener);
            }

            updateHeadType(Lookup.getSelectedPrinterProperty().get());

            ApplicationStatus.getInstance()
                    .modeProperty().addListener(applicationModeChangeListener);

            setupQualityRadioButtons();

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void updateHeadType(Printer printer)
    {
        String headTypeBefore = currentHeadType;
        if (printer != null && printer.headProperty().get() != null)
        {
            currentHeadType = printer.headProperty().get().typeCodeProperty().get();
        } else
        {
            currentHeadType = HeadContainer.defaultHeadID;
        }
        if (headTypeBefore != currentHeadType)
        {
            headType.setText("Estimates for head type: " + currentHeadType.toString());
            updateFields(currentProject);
        }
    }

    private void setupQualityRadioButtons()
    {
        qualityToggleGroup = new ToggleGroup();
        rbDraft.setToggleGroup(qualityToggleGroup);
        rbDraft.setUserData(PrintQualityEnumeration.DRAFT);
        rbNormal.setToggleGroup(qualityToggleGroup);
        rbNormal.setUserData(PrintQualityEnumeration.NORMAL);
        rbFine.setToggleGroup(qualityToggleGroup);
        rbFine.setUserData(PrintQualityEnumeration.FINE);
        rbCustom.setToggleGroup(qualityToggleGroup);
        rbCustom.setUserData(PrintQualityEnumeration.CUSTOM);
        rbDraft.setSelected(true);
        qualityToggleGroup.selectedToggleProperty().addListener(
                (ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) ->
                {
                    printerSettings.setPrintQuality((PrintQualityEnumeration) newValue.getUserData());
                });
    }

    private void unbindProject(Project project)
    {
        project.removeProjectChangesListener(projectChangesListener);
    }

    Project.ProjectChangesListener projectChangesListener;

    private void bindProject(Project project)
    {
        printerSettings = project.getPrinterSettings();

        projectChangesListener = new Project.ProjectChangesListener()
        {

            @Override
            public void whenModelAdded(ProjectifiableThing modelContainer)
            {
                updateFields(project);
            }

            @Override
            public void whenModelsRemoved(Set<ProjectifiableThing> modelContainers)
            {
                updateFields(project);
            }

            @Override
            public void whenAutoLaidOut()
            {
            }

            @Override
            public void whenModelsTransformed(Set<ProjectifiableThing> modelContainers)
            {
                updateFields(project);
            }

            @Override
            public void whenModelChanged(ProjectifiableThing modelContainer, String propertyName)
            {
                updateFields(project);
            }

            @Override
            public void whenPrinterSettingsChanged(PrinterSettingsOverrides printerSettings)
            {
                updateFields(project);
            }
        };
        updateFields(project);
        updatePrintQuality(printerSettings);
        project.addProjectChangesListener(projectChangesListener);

    }

    @Override
    public void setProject(Project project)
    {
        currentProject = project;
        whenProjectChanged(project);
    }

    private void whenProjectChanged(Project project)
    {
        currentProject = project;
        if (project != null)
        {
            if (currentProject != null)
            {
                unbindProject(currentProject);
            }
            bindProject(project);
            currentProject = project;
        }
    }

    public void clearPrintJobDirectories()
    {
        try
        {
            String filePath = BaseConfiguration.getApplicationStorageDirectory()
                    + ApplicationConfiguration.timeAndCostFileSubpath;
            File folder = new File(filePath);
            for (final File fileEntry : folder.listFiles())
            {
                if (fileEntry.isDirectory())
                {
                    try
                    {
                        FileUtils.deleteDirectory(fileEntry);
                    } catch (IOException ex)
                    {
                    }
                }
            }
        } catch (Exception ex)
        {
        }
    }

    /**
     * Update the time, cost and weight fields. Long running calculations must
     * be performed in a background thread. Run draft, normal and fine
     * sequentially to avoid flooding the CPU(s).
     */
    private void updateFields(Project project)
    {

        if (ApplicationStatus.getInstance().modeProperty().get() != ApplicationMode.SETTINGS)
        {
            return;
        }

        lblDraftTime.setText("...");
        lblNormalTime.setText("...");
        lblFineTime.setText("...");
        lblCustomTime.setText("...");
        lblDraftWeight.setText("...");
        lblNormalWeight.setText("...");
        lblFineWeight.setText("...");
        lblCustomWeight.setText("...");
        lblDraftCost.setText("...");
        lblNormalCost.setText("...");
        lblFineCost.setText("...");
        lblCustomCost.setText("...");

        Cancellable cancellable = new SimpleCancellable();
        if (currentProject != null
                && currentProject.getNumberOfProjectifiableElements() > 0
                && currentPrinter != null
                && currentPrinter.headProperty().get() != null)
        {
            SlicerParametersFile slicerParameters = currentProject.getPrinterSettings().getSettings(currentPrinter.headProperty().get().typeCodeProperty().get());

            //NOTE - this needs to change if raft settings in slicermapping.dat is changed
            double raftOffset = slicerParameters.getRaftBaseThickness_mm()
                    //Raft interface thickness
                    + 0.28
                    //Raft surface layer thickness * surface layers
                    + (slicerParameters.getInterfaceLayers() * 0.27)
                    + slicerParameters.getRaftAirGapLayer0_mm();

            boolean aModelIsOffTheBed = false;
            for (ProjectifiableThing projectifiableThing : currentProject.getTopLevelThings())
            {
                if (projectifiableThing instanceof ModelContainer)
                {
                    ModelContainer modelContainer = (ModelContainer) projectifiableThing;

                    //TODO use settings derived offset values for spiral
                    if (modelContainer.isOffBedProperty().get()
                            || (currentProject.getPrinterSettings().getRaftOverride()
                            && modelContainer.isModelTooHighWithOffset(raftOffset))
                            || (currentProject.getPrinterSettings().getSpiralPrintOverride()
                            && modelContainer.isModelTooHighWithOffset(0.5)))
                    {
                        aModelIsOffTheBed = true;
                        break;
                    }
                }
            }

            if (!aModelIsOffTheBed)
            {

                Runnable runUpdateFields = () ->
                {
                    try
                    {
                        Thread.sleep(500);
                    } catch (InterruptedException ex)
                    {
                        return;
                    }
                    if (cancellable.cancelled().get())
                    {
                        return;
                    }

                    if (currentProject.getPrintQuality() == PrintQualityEnumeration.CUSTOM
                            && !currentProject.getPrinterSettings().getSettingsName().equals(""))
                    {
                        SlicerParametersFile customSettings = currentProject.getPrinterSettings().getSettings(
                                currentHeadType);
                        updateFieldsForProfile(project, customSettings, lblCustomTime,
                                lblCustomWeight,
                                lblCustomCost, cancellable);
                        if (cancellable.cancelled().get())
                        {
                            return;
                        }
                    }
                    SlicerParametersFile settings = SlicerParametersContainer.getSettings(
                            BaseConfiguration.draftSettingsProfileName,
                            currentHeadType);
                    updateFieldsForProfile(project, settings, lblDraftTime,
                            lblDraftWeight,
                            lblDraftCost, cancellable);
                    if (cancellable.cancelled().get())
                    {
                        return;
                    }
                    settings = SlicerParametersContainer.getSettings(
                            BaseConfiguration.normalSettingsProfileName,
                            currentHeadType);
                    updateFieldsForProfile(project, settings, lblNormalTime,
                            lblNormalWeight,
                            lblNormalCost, cancellable);
                    if (cancellable.cancelled().get())
                    {
                        return;
                    }
                    settings = SlicerParametersContainer.getSettings(
                            BaseConfiguration.fineSettingsProfileName,
                            currentHeadType);
                    updateFieldsForProfile(project, settings, lblFineTime,
                            lblFineWeight,
                            lblFineCost, cancellable);
                };

                clearPrintJobDirectories();

                timeCostThreadManager.cancelRunningTimeCostTasksAndRun(runUpdateFields, cancellable);
            }
        }
    }

    /**
     * Update the time, cost and weight fields for the given profile and fields.
     * Long running calculations must be performed in a background thread.
     */
    private void updateFieldsForProfile(Project project, SlicerParametersFile settings,
            Label lblTime, Label lblWeight, Label lblCost, Cancellable cancellable)
    {
        boolean slicedAndPostProcessed = false;

        if (project instanceof ModelContainerProject)
        {
            if (settings != null)
            {
                String working = Lookup.i18n("timeCost.working");
                BaseLookup.getTaskExecutor().runOnGUIThread(() ->
                {
                    lblTime.setText(working);
                    lblWeight.setText(working);
                    lblCost.setText(working);
                });

                GetTimeWeightCost updateDetails = new GetTimeWeightCost((ModelContainerProject) project, settings,
                        lblTime, lblWeight,
                        lblCost, cancellable);

                try
                {
                    slicedAndPostProcessed = updateDetails.runSlicerAndPostProcessor();
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }

            if (!slicedAndPostProcessed
                    && !cancellable.cancelled().get())
            {
                steno.error("Error running slicer/postprocessor");
                String failed = Lookup.i18n("timeCost.failed");
                BaseLookup.getTaskExecutor().runOnGUIThread(() ->
                {
                    lblTime.setText(failed);
                    lblWeight.setText(failed);
                    lblCost.setText(failed);
                });
            }
        }
    }

    private void updatePrintQuality(PrinterSettingsOverrides printerSettings)
    {
        switch (printerSettings.getPrintQuality())
        {
            case DRAFT:
                rbDraft.setSelected(true);
                break;
            case NORMAL:
                rbNormal.setSelected(true);
                break;
            case FINE:
                rbFine.setSelected(true);
                break;
            case CUSTOM:
                rbCustom.setSelected(true);
                break;
        }
    }

    @Override
    public void shutdownController()
    {

        if (currentPrinter != null)
        {
            currentPrinter.effectiveFilamentsProperty().removeListener(effectiveFilamentListener);
        }

        if (currentProject != null)
        {
            unbindProject(currentProject);
        }
        currentProject = null;

        BaseLookup.getPrinterListChangesNotifier().removeListener(printerListChangesListener);

        Lookup.getSelectedPrinterProperty().removeListener(selectedPrinterChangeListener);

        ApplicationStatus.getInstance()
                .modeProperty().removeListener(applicationModeChangeListener);
    }
}
