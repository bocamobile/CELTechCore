package celtech.gcodetranslator;

/**
 *
 * @author Ian
 */
public class NotEnoughAvailableExtrusionException extends Exception
{
    public NotEnoughAvailableExtrusionException(String exceptionInformation)
    {
        super(exceptionInformation);
    }
}
