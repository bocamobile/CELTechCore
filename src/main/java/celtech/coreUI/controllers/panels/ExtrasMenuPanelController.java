package celtech.coreUI.controllers.panels;

import celtech.Lookup;
import celtech.appManager.ApplicationStatus;
import celtech.configuration.ApplicationConfiguration;
import celtech.configuration.UserPreferences;
import celtech.coreUI.components.VerticalMenu;
import celtech.coreUI.controllers.panels.userpreferences.Preferences;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import libertysystems.stenographer.Stenographer;
import libertysystems.stenographer.StenographerFactory;

public class ExtrasMenuPanelController implements Initializable
{

    private class InnerPanelDetails
    {

        Node node;
        ExtrasMenuInnerPanel innerPanel;

        InnerPanelDetails(Node node, ExtrasMenuInnerPanel innerPanel)
        {
            this.node = node;
            this.innerPanel = innerPanel;
        }
    }

    List<InnerPanelDetails> innerPanelDetails = new ArrayList<>();

    private final Stenographer steno = StenographerFactory.getStenographer(
        ExtrasMenuPanelController.class.getName());

    private ResourceBundle resources;

    @FXML
    private VerticalMenu libraryMenu;

    @FXML
    private VBox insetNodeContainer;

    @FXML
    private HBox buttonBoxContainer;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {

        this.resources = resources;

        ButtonBox buttonBox = new ButtonBox(Lookup.getExtrasInnerPanel());
        buttonBoxContainer.getChildren().add(buttonBox);

        setupInnerPanels();

        buildExtras();
    }

    /**
     * Define the inner panels to be offered in the main menu. For the future this is configuration
     * information that could be e.g. stored in XML or in a plugin.
     */
    private void setupInnerPanels()
    {
        loadInnerPanel(
            ApplicationConfiguration.fxmlPanelResourcePath + "filamentLibraryPanel.fxml",
            new FilamentLibraryPanelController());
        UserPreferences userPreferences = Lookup.getUserPreferences();
        loadInnerPanel(
            ApplicationConfiguration.fxmlPanelResourcePath + "preferencesPanel.fxml",
            new PreferencesInnerPanelController("preferences.environment",
                                                Preferences.createEnvironmentPreferences(
                                                    userPreferences)));
        loadInnerPanel(
            ApplicationConfiguration.fxmlPanelResourcePath + "preferencesPanel.fxml",
            new PreferencesInnerPanelController("preferences.printing",
                                                Preferences.createPrintingPreferences(
                                                    userPreferences)));        
    }

    /**
     * Load the given inner panel.
     */
    private void loadInnerPanel(String fxmlLocation, ExtrasMenuInnerPanel extrasMenuInnerPanel)
    {
        URL fxmlURL = getClass().getResource(fxmlLocation);
        FXMLLoader loader = new FXMLLoader(fxmlURL, resources);
        loader.setController(extrasMenuInnerPanel);
        try
        {
            Node node = loader.load();
            InnerPanelDetails innerPanelDetails = new InnerPanelDetails(node, extrasMenuInnerPanel);
            this.innerPanelDetails.add(innerPanelDetails);
        } catch (IOException ex)
        {
            steno.error("Unable to load panel: " + fxmlLocation + " " + ex);
        }
    }

    /**
     * Open the given inner panel.
     */
    private void doOpenInnerPanel(InnerPanelDetails innerPanel)
    {
        insetNodeContainer.getChildren().clear();
        insetNodeContainer.getChildren().add(innerPanel.node);
    }

    /**
     * For each InnerPanel, create a menu item that will open it.
     */
    private void buildExtras()
    {
        libraryMenu.setTitle(Lookup.i18n("extrasMenu.title"));

        for (InnerPanelDetails innerPanelDetails : innerPanelDetails)
        {
            libraryMenu.addItem(Lookup.i18n(innerPanelDetails.innerPanel.getMenuTitle()), () ->
                            {
                                doOpenInnerPanel(innerPanelDetails);
                                Lookup.setExtrasInnerPanel(innerPanelDetails.innerPanel);
                                return null;
            }, null);
        }
    }

    @FXML
    private void okPressed(ActionEvent event)
    {
        ApplicationStatus.getInstance().returnToLastMode();
    }
}