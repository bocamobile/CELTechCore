/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package celtech.appManager;

import celtech.configuration.ApplicationConfiguration;

/**
 *
 * @author ianhudson
 */
public enum ApplicationMode
{
    WELCOME("printerStatus", "Welcome"),
    CALIBRATION_CHOICE("printerStatus", "Calibration"),
    REGISTRATION("printerStatus", "registration"),
    PURGE("printerStatus", "purge"),
    PURGE2("printerStatus", "purge2"),
    ABOUT("printerStatus", "about"),
    SYSTEM_INFORMATION("printerStatus", "systemInformation"),
    EXTRAS_MENU("printerStatus", "extrasMenu"),
    //TODO printer status has to be last otherwise the temperature graph doesn't work!! Fix in DisplayManager
    STATUS("printerStatus", null),
    /**
     *
     */
    LAYOUT("layout", null),
    ADD_MODEL("layout", "loadModel"),
    MY_MINI_FACTORY("layout", "myMiniFactoryLoader"),
    /**
     *
     */
    SETTINGS("settings", null);

    private final String sidePanelFXMLPrefix;
    private final String insetPanelFXMLPrefix;

    private ApplicationMode(String sidePanelFXMLPrefix, String insetPanelFXMLPrefix)
    {
        this.sidePanelFXMLPrefix = sidePanelFXMLPrefix;
        this.insetPanelFXMLPrefix = insetPanelFXMLPrefix;
    }

    /**
     *
     * @return
     */
    public String getSidePanelFXMLName()
    {
        return ApplicationConfiguration.fxmlPanelResourcePath + sidePanelFXMLPrefix + "SidePanel" + ".fxml";
    }

    /**
     *
     * @return
     */
    public String getSlideOutFXMLName()
    {
        return ApplicationConfiguration.fxmlPanelResourcePath + sidePanelFXMLPrefix + "SlideOutPanel" + ".fxml";
    }

    /**
     *
     * @return
     */
    public String getInsetPanelFXMLName()
    {
        return ApplicationConfiguration.fxmlPanelResourcePath + insetPanelFXMLPrefix + "InsetPanel" + ".fxml";
    }

}
