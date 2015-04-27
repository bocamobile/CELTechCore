package celtech.printerControl;

import celtech.configuration.ApplicationConfiguration;
import celtech.gcodetranslator.PrintJobStatistics;
import java.io.File;
import java.io.IOException;

/**
 * A PrintJob represents a print run of a Project, and is associated with a print job directory in
 * the print spool directory.
 *
 * @author Ian
 */
public class PrintJob
{

    private String jobUUID = null;
    private String printJobDirectory = null;

    private PrintJob(String jobUUID)
    {
        this.jobUUID = jobUUID;
        this.printJobDirectory = ApplicationConfiguration.getPrintSpoolDirectory() + jobUUID
            + File.separator;
    }

    private PrintJob(String jobUUID, String printJobDirectory)
    {
        this.jobUUID = jobUUID;
        this.printJobDirectory = printJobDirectory;
    }

    /**
     * Instantiate a PrintJob from the data in the spool directory
     *
     * @param jobUUID
     * @return
     */
    public static PrintJob readJobFromDirectory(String jobUUID)
    {
        return new PrintJob(jobUUID);
    }

    /**
     * Instantiate a PrintJob from the data in the spool directory
     *
     * @param jobUUID
     * @param printJobDirectory
     * @return
     */
    public static PrintJob readJobFromDirectory(String jobUUID, String printJobDirectory)
    {
        return new PrintJob(jobUUID, printJobDirectory);
    }

    /**
     * Get the location of the gcode file as produced by the slicer
     *
     * @return
     */
    public String getGCodeFileLocation()
    {
        String printjobFilename = printJobDirectory
            + jobUUID
            + ApplicationConfiguration.gcodeTempFileExtension;
        return printjobFilename;
    }

    /**
     * Return if the roboxised file is found in the print spool directory
     *
     * @return
     */
    public boolean roboxisedFileExists()
    {
        File printJobFile = new File(getRoboxisedFileLocation());
        return printJobFile.exists();
    }

    /**
     * @return the jobUUID
     */
    public String getJobUUID()
    {
        return jobUUID;
    }

    /**
     * Get the location of the roboxised file
     *
     * @return
     */
    public String getRoboxisedFileLocation()
    {
        return printJobDirectory
            + jobUUID
            + ApplicationConfiguration.gcodePostProcessedFileHandle
            + ApplicationConfiguration.gcodeTempFileExtension;
    }

    /**
     * Get the location of the statistics file
     *
     * @return
     */
    public String getStatisticsFileLocation()
    {
        return printJobDirectory
            + jobUUID
            + ApplicationConfiguration.statisticsFileExtension;
    }

    public PrintJobStatistics getStatistics() throws IOException
    {
        return PrintJobStatistics.readFromFile(getStatisticsFileLocation());
    }

}
