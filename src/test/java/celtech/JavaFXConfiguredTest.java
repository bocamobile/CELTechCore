/*
 * Copyright 2014 CEL UK
 */
package celtech;

import celtech.configuration.ApplicationConfiguration;
import java.io.File;
import java.net.URL;
import java.util.Properties;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author tony
 */
public class JavaFXConfiguredTest
{

    @Rule
    public TemporaryFolder temporaryUserStorageFolder = new TemporaryFolder();

    @Before
    public void setUp()
    {
        Properties testProperties = new Properties();

        testProperties.setProperty("language", "UK");
        URL applicationInstallURL = this.getClass().getResource("/");
        URL applicationCommonURL = this.getClass().getResource("/Common/");
        String userStorageFolder = temporaryUserStorageFolder.getRoot().getAbsolutePath()
            + File.separator;
        ApplicationConfiguration.setInstallationProperties(
            testProperties,
            applicationInstallURL.getFile(),
            applicationCommonURL.getFile(),
            userStorageFolder);
        Lookup.initialise();

        new File(userStorageFolder
            + ApplicationConfiguration.printSpoolStorageDirectoryPath
            + File.separator).mkdir();

//        // force initialisation
//        URL configURL = this.getClass().getResource("/AutoMaker.configFile.xml");
//        System.setProperty("libertySystems.configFile", configURL.getFile());
//        String installDir = ApplicationConfiguration.getApplicationInstallDirectory(
//                Lookup.class);
//        PrintProfileContainer.getInstance();
    }

}
