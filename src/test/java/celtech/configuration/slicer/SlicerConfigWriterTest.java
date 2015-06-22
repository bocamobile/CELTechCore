/*
 * Copyright 2015 CEL UK
 */
package celtech.configuration.slicer;

import celtech.JavaFXConfiguredTest;
import celtech.configuration.SlicerType;
import celtech.configuration.fileRepresentation.SlicerParametersFile;
import celtech.coreUI.controllers.PrinterSettings;
import celtech.printerControl.model.Head.HeadType;
import celtech.services.slicer.PrintQualityEnumeration;
import java.io.File;
import java.io.IOException;
import java.util.List;
import static org.apache.commons.io.FileUtils.readLines;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author tony
 */
public class SlicerConfigWriterTest extends JavaFXConfiguredTest
{

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testGenerateConfigForRaftOnCuraDraft() throws IOException
    {
        String TEMPFILENAME = "output.roboxprofile";
        SlicerConfigWriter configWriter = SlicerConfigWriterFactory.getConfigWriter(
            SlicerType.Cura);
        PrinterSettings printerSettings = new PrinterSettings();
        printerSettings.setPrintQuality(PrintQualityEnumeration.DRAFT);
        printerSettings.setRaftOverride(true);
        String destinationFile = temporaryFolder.getRoot().getAbsolutePath() + File.separator
            + TEMPFILENAME;
        configWriter.generateConfigForSlicer(printerSettings.getSettings(HeadType.SINGLE_MATERIAL_HEAD), destinationFile);
        List<String> outputData = readLines(new File(destinationFile));
        assertTrue(outputData.contains("raftBaseThickness=300"));
        assertTrue(outputData.contains("raftInterfaceThickness=280"));
    }

    @Test
    public void testGenerateConfigForRaftOnCuraNormal() throws IOException
    {
        String TEMPFILENAME = "output.roboxprofile";
        SlicerConfigWriter configWriter = SlicerConfigWriterFactory.getConfigWriter(
            SlicerType.Cura);
        PrinterSettings printerSettings = new PrinterSettings();
        printerSettings.setPrintQuality(PrintQualityEnumeration.NORMAL);
        printerSettings.setRaftOverride(true);
        String destinationFile = temporaryFolder.getRoot().getAbsolutePath() + File.separator
            + TEMPFILENAME;
        configWriter.generateConfigForSlicer(printerSettings.getSettings(HeadType.SINGLE_MATERIAL_HEAD), destinationFile);
        List<String> outputData = readLines(new File(destinationFile));
        assertTrue(outputData.contains("raftBaseThickness=300"));
        assertTrue(outputData.contains("raftInterfaceThickness=280"));
    }

    @Test
    public void testGenerateConfigForRaftOnCuraFine() throws IOException
    {
        String TEMPFILENAME = "output.roboxprofile";
        SlicerConfigWriter configWriter = SlicerConfigWriterFactory.getConfigWriter(
            SlicerType.Cura);
        PrinterSettings printerSettings = new PrinterSettings();
        printerSettings.setPrintQuality(PrintQualityEnumeration.FINE);
        printerSettings.setRaftOverride(true);
        String destinationFile = temporaryFolder.getRoot().getAbsolutePath() + File.separator
            + TEMPFILENAME;
        configWriter.generateConfigForSlicer(printerSettings.getSettings(HeadType.SINGLE_MATERIAL_HEAD), destinationFile);
        List<String> outputData = readLines(new File(destinationFile));
        assertTrue(outputData.contains("raftBaseThickness=300"));
        assertTrue(outputData.contains("raftInterfaceThickness=280"));
    }

    @Test
    public void testGenerateConfigForNoRaftOnCuraDraft() throws IOException
    {
        String TEMPFILENAME = "output.roboxprofile";
        SlicerConfigWriter configWriter = SlicerConfigWriterFactory.getConfigWriter(
            SlicerType.Cura);
        PrinterSettings printerSettings = new PrinterSettings();
        printerSettings.setPrintQuality(PrintQualityEnumeration.DRAFT);
        printerSettings.setRaftOverride(false);
        String destinationFile = temporaryFolder.getRoot().getAbsolutePath() + File.separator
            + TEMPFILENAME;
        configWriter.generateConfigForSlicer(printerSettings.getSettings(HeadType.SINGLE_MATERIAL_HEAD), destinationFile);
        List<String> outputData = readLines(new File(destinationFile));
        assertTrue(outputData.contains("raftBaseThickness=0"));
        assertTrue(outputData.contains("raftInterfaceThickness=280"));
    }

    @Test
    public void testGenerateConfigForNoRaftOnCuraNormal() throws IOException
    {
        String TEMPFILENAME = "output.roboxprofile";
        SlicerConfigWriter configWriter = SlicerConfigWriterFactory.getConfigWriter(
            SlicerType.Cura);
        PrinterSettings printerSettings = new PrinterSettings();
        printerSettings.setPrintQuality(PrintQualityEnumeration.NORMAL);
        printerSettings.setRaftOverride(false);
        String destinationFile = temporaryFolder.getRoot().getAbsolutePath() + File.separator
            + TEMPFILENAME;
        configWriter.generateConfigForSlicer(printerSettings.getSettings(HeadType.SINGLE_MATERIAL_HEAD), destinationFile);
        List<String> outputData = readLines(new File(destinationFile));
        assertTrue(outputData.contains("raftBaseThickness=0"));
        assertTrue(outputData.contains("raftInterfaceThickness=280"));
    }

    @Test
    public void testGenerateConfigForNoRaftOnCuraFine() throws IOException
    {
        String TEMPFILENAME = "output.roboxprofile";
        SlicerConfigWriter configWriter = SlicerConfigWriterFactory.getConfigWriter(
            SlicerType.Cura);
        PrinterSettings printerSettings = new PrinterSettings();
        printerSettings.setPrintQuality(PrintQualityEnumeration.FINE);
        printerSettings.setRaftOverride(false);
        String destinationFile = temporaryFolder.getRoot().getAbsolutePath() + File.separator
            + TEMPFILENAME;
        configWriter.generateConfigForSlicer(printerSettings.getSettings(HeadType.SINGLE_MATERIAL_HEAD), destinationFile);
        List<String> outputData = readLines(new File(destinationFile));
        assertTrue(outputData.contains("raftBaseThickness=0"));
        assertTrue(outputData.contains("raftInterfaceThickness=280"));
    }

    @Test
    public void testGenerateConfigForRaftOnSlic3rDraft() throws IOException
    {
        String TEMPFILENAME = "output.roboxprofile";
        SlicerConfigWriter configWriter = SlicerConfigWriterFactory.getConfigWriter(
            SlicerType.Slic3r);
        PrinterSettings printerSettings = new PrinterSettings();
        printerSettings.setPrintQuality(PrintQualityEnumeration.DRAFT);
        printerSettings.setRaftOverride(true);
        String destinationFile = temporaryFolder.getRoot().getAbsolutePath() + File.separator
            + TEMPFILENAME;
        configWriter.generateConfigForSlicer(printerSettings.getSettings(HeadType.SINGLE_MATERIAL_HEAD), destinationFile);
        List<String> outputData = readLines(new File(destinationFile));
        for (String outputData1 : outputData)
        {
            System.out.println(outputData1);
        }
        assertTrue(outputData.contains("raft_layers = 3"));
        assertTrue(outputData.contains("support_material_interface_layers = 2"));
    }

    @Test
    public void testGenerateConfigForNoRaftOnSlic3rDraft() throws IOException
    {
        String TEMPFILENAME = "output.roboxprofile";
        SlicerConfigWriter configWriter = SlicerConfigWriterFactory.getConfigWriter(
            SlicerType.Slic3r);
        PrinterSettings printerSettings = new PrinterSettings();
        printerSettings.setPrintQuality(PrintQualityEnumeration.DRAFT);
        printerSettings.setRaftOverride(false);
        String destinationFile = temporaryFolder.getRoot().getAbsolutePath() + File.separator
            + TEMPFILENAME;
        configWriter.generateConfigForSlicer(printerSettings.getSettings(HeadType.SINGLE_MATERIAL_HEAD), destinationFile);
        List<String> outputData = readLines(new File(destinationFile));
        assertTrue(outputData.contains("raft_layers = 0"));
    }

    @Test
    public void testGenerateConfigForRaftOnSlic3rNormal() throws IOException
    {
        String TEMPFILENAME = "output.roboxprofile";
        SlicerConfigWriter configWriter = SlicerConfigWriterFactory.getConfigWriter(
            SlicerType.Slic3r);
        PrinterSettings printerSettings = new PrinterSettings();
        printerSettings.setPrintQuality(PrintQualityEnumeration.NORMAL);
        printerSettings.setRaftOverride(true);
        String destinationFile = temporaryFolder.getRoot().getAbsolutePath() + File.separator
            + TEMPFILENAME;
        configWriter.generateConfigForSlicer(printerSettings.getSettings(HeadType.SINGLE_MATERIAL_HEAD), destinationFile);
        List<String> outputData = readLines(new File(destinationFile));
        assertTrue(outputData.contains("raft_layers = 2"));
    }

    @Test
    public void testGenerateConfigForNoRaftOnSlic3rNormal() throws IOException
    {
        String TEMPFILENAME = "output.roboxprofile";
        SlicerConfigWriter configWriter = SlicerConfigWriterFactory.getConfigWriter(
            SlicerType.Slic3r);
        PrinterSettings printerSettings = new PrinterSettings();
        printerSettings.setPrintQuality(PrintQualityEnumeration.NORMAL);
        printerSettings.setRaftOverride(false);
        String destinationFile = temporaryFolder.getRoot().getAbsolutePath() + File.separator
            + TEMPFILENAME;
        configWriter.generateConfigForSlicer(printerSettings.getSettings(HeadType.SINGLE_MATERIAL_HEAD), destinationFile);
        List<String> outputData = readLines(new File(destinationFile));
        assertTrue(outputData.contains("raft_layers = 0"));
    }

    @Test
    public void testGenerateConfigForRaftOnSlic3rFine() throws IOException
    {
        String TEMPFILENAME = "output.roboxprofile";
        SlicerConfigWriter configWriter = SlicerConfigWriterFactory.getConfigWriter(
            SlicerType.Slic3r);
        PrinterSettings printerSettings = new PrinterSettings();
        printerSettings.setPrintQuality(PrintQualityEnumeration.FINE);
        printerSettings.setRaftOverride(true);
        String destinationFile = temporaryFolder.getRoot().getAbsolutePath() + File.separator
            + TEMPFILENAME;
        configWriter.generateConfigForSlicer(printerSettings.getSettings(HeadType.SINGLE_MATERIAL_HEAD), destinationFile);
        List<String> outputData = readLines(new File(destinationFile));
        assertTrue(outputData.contains("raft_layers = 3"));
    }

    @Test
    public void testGenerateConfigForNoRaftOnSlic3rFine() throws IOException
    {
        String TEMPFILENAME = "output.roboxprofile";
        SlicerConfigWriter configWriter = SlicerConfigWriterFactory.getConfigWriter(
            SlicerType.Slic3r);
        PrinterSettings printerSettings = new PrinterSettings();
        printerSettings.setPrintQuality(PrintQualityEnumeration.FINE);
        printerSettings.setRaftOverride(false);
        String destinationFile = temporaryFolder.getRoot().getAbsolutePath() + File.separator
            + TEMPFILENAME;
        configWriter.generateConfigForSlicer(printerSettings.getSettings(HeadType.SINGLE_MATERIAL_HEAD), destinationFile);
        List<String> outputData = readLines(new File(destinationFile));
        assertTrue(outputData.contains("raft_layers = 0"));
    }

    @Test
    public void testGenerateConfigForSparseInfillOffCuraFine() throws IOException
    {
        String TEMPFILENAME = "output.roboxprofile";
        SlicerConfigWriter configWriter = SlicerConfigWriterFactory.getConfigWriter(
            SlicerType.Cura);
        PrinterSettings printerSettings = new PrinterSettings();
        printerSettings.setPrintQuality(PrintQualityEnumeration.FINE);
        printerSettings.setFillDensityOverride(0);

        String destinationFile = temporaryFolder.getRoot().getAbsolutePath() + File.separator
            + TEMPFILENAME;
        configWriter.generateConfigForSlicer(printerSettings.getSettings(HeadType.SINGLE_MATERIAL_HEAD), destinationFile);
        List<String> outputData = readLines(new File(destinationFile));
        assertTrue(outputData.contains("sparseInfillLineDistance=-1"));
    }

    @Test
    public void testGenerateConfigForSparseInfillOnCuraFine() throws IOException
    {
        String TEMPFILENAME = "output.roboxprofile";
        SlicerConfigWriter configWriter = SlicerConfigWriterFactory.getConfigWriter(
            SlicerType.Cura);
        PrinterSettings printerSettings = new PrinterSettings();
        printerSettings.setPrintQuality(PrintQualityEnumeration.FINE);
        printerSettings.setFillDensityOverride(0.5f);

        String destinationFile = temporaryFolder.getRoot().getAbsolutePath() + File.separator
            + TEMPFILENAME;
        configWriter.generateConfigForSlicer(printerSettings.getSettings(HeadType.SINGLE_MATERIAL_HEAD), destinationFile);
        List<String> outputData = readLines(new File(destinationFile));
        assertTrue(outputData.contains("sparseInfillLineDistance=800"));
    }

}
