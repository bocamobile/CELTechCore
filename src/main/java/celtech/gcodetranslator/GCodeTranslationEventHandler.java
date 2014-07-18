
package celtech.gcodetranslator;

import celtech.gcodetranslator.events.GCodeParseEvent;

/**
 *
 * @author Ian
 */
public interface GCodeTranslationEventHandler
{    

    /**
     *
     * @param event
     * @throws NozzleCloseSettingsError
     */
    public void processEvent(GCodeParseEvent event) throws NozzleCloseSettingsError;

    /**
     *
     * @param line
     */
    public void unableToParse(String line);
}
