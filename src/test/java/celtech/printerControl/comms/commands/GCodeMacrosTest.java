package celtech.printerControl.comms.commands;

import celtech.JavaFXConfiguredTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Ian
 */
public class GCodeMacrosTest extends JavaFXConfiguredTest
{
    /**
     * Test of getMacroContents method, of class GCodeMacros.
     */
    @Test
    public void testScoreMacroFilename() throws Exception
    {
        System.out.println("scoreMacroFilename");

        String filename1 = "before_print.gcode";
        int score1 = GCodeMacros.scoreMacroFilename(filename1,
                null,
                GCodeMacros.NozzleUseIndicator.DONT_CARE,
                GCodeMacros.SafetyIndicator.DONT_CARE);
        assertEquals(0, score1);

        String filename2 = "before_print#RBX01-DM.gcode";
        int score2 = GCodeMacros.scoreMacroFilename(filename2,
                null,
                GCodeMacros.NozzleUseIndicator.DONT_CARE,
                GCodeMacros.SafetyIndicator.DONT_CARE);
        assertEquals(-1, score2);

        String filename3 = "before_print#RBX01-DM#N0.gcode";
        int score3 = GCodeMacros.scoreMacroFilename(filename3,
                null,
                GCodeMacros.NozzleUseIndicator.DONT_CARE,
                GCodeMacros.SafetyIndicator.DONT_CARE);
        assertEquals(-2, score3);

        String filename4 = "before_print#RBX01-DM#N0.gcode";
        int score4 = GCodeMacros.scoreMacroFilename(filename4,
                "RBX01-DM",
                GCodeMacros.NozzleUseIndicator.DONT_CARE,
                GCodeMacros.SafetyIndicator.DONT_CARE);
        assertEquals(0, score4);

        String filename5 = "before_print#RBX01-DM#N0.gcode";
        int score5 = GCodeMacros.scoreMacroFilename(filename5,
                "RBX01-DM",
                GCodeMacros.NozzleUseIndicator.NOZZLE_0,
                GCodeMacros.SafetyIndicator.DONT_CARE);
        assertEquals(2, score5);

        String filename6 = "before_print#RBX01-DM#N0.gcode";
        int score6 = GCodeMacros.scoreMacroFilename(filename6,
                "RBX01-SM",
                GCodeMacros.NozzleUseIndicator.DONT_CARE,
                GCodeMacros.SafetyIndicator.DONT_CARE);
        assertEquals(-2, score6);

        String filename7 = "before_print.gcode";
        int score7 = GCodeMacros.scoreMacroFilename(filename7,
                "RBX01-SM",
                GCodeMacros.NozzleUseIndicator.DONT_CARE,
                GCodeMacros.SafetyIndicator.DONT_CARE);
        assertEquals(-1, score7);

        String filename8 = "before_print#RBX01-SM.gcode";
        int score8 = GCodeMacros.scoreMacroFilename(filename8,
                "RBX01-SM",
                GCodeMacros.NozzleUseIndicator.DONT_CARE,
                GCodeMacros.SafetyIndicator.DONT_CARE);
        assertEquals(1, score8);
    }
}