package celtech.configuration.datafileaccessors;

import celtech.configuration.ApplicationConfiguration;
import celtech.configuration.fileRepresentation.UserPreferenceFile;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import libertysystems.stenographer.Stenographer;
import libertysystems.stenographer.StenographerFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

/**
 *
 * @author ianhudson
 */
public class UserPreferenceContainer
{

    private static final Stenographer steno = StenographerFactory.getStenographer(UserPreferenceContainer.class.getName());
    private static UserPreferenceContainer instance = null;
    private static UserPreferenceFile userPreferenceFile = null;
    private static final ObjectMapper mapper = new ObjectMapper();
    public static final String defaultUserPreferenceFilename = "roboxpreferences.pref";

    private UserPreferenceContainer()
    {
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);

        File userPreferenceInputFile = new File(ApplicationConfiguration.getUserStorageDirectory() + defaultUserPreferenceFilename);
        if (!userPreferenceInputFile.exists())
        {
            userPreferenceFile = new UserPreferenceFile();
            try
            {
                mapper.writeValue(userPreferenceInputFile, userPreferenceFile);
            } catch (IOException ex)
            {
                steno.error("Error trying to create user preferences file");
            }
        } else
        {
            try
            {
                userPreferenceFile = mapper.readValue(userPreferenceInputFile, UserPreferenceFile.class);

            } catch (IOException ex)
            {
                steno.error("Error loading user preferences " + userPreferenceInputFile.getAbsolutePath());
            }
        }
    }

    /**
     *
     * @return
     */
    public static UserPreferenceContainer getInstance()
    {
        if (instance == null)
        {
            instance = new UserPreferenceContainer();
        }

        return instance;
    }

    /**
     *
     * @return
     */
    public static UserPreferenceFile getUserPreferenceFile()
    {
        if (instance == null)
        {
            instance = new UserPreferenceContainer();
        }

        return userPreferenceFile;
    }
}