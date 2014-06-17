/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package celtech.utils;

import java.text.ParseException;
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
public class FixedDecimalFloatFormatTest
{

    private double epsilon = 1e-10;

    public FixedDecimalFloatFormatTest()
    {
    }

    @BeforeClass
    public static void setUpClass()
    {
    }

    @AfterClass
    public static void tearDownClass()
    {
    }

    @Before
    public void setUp()
    {
    }

    @After
    public void tearDown()
    {
    }

    /**
     * Test of instantiation of class FixedDecimalFloatFormat.
     */
    @Test
    public void testCreateFixedDecimalFloatFormat()
    {
        System.out.println("create FixedDecimalFloatFormat");
        FixedDecimalFloatFormat result = new FixedDecimalFloatFormat();
        assertNotNull(result);
    }

    /**
     * Test overflow during format (long)
     */
    @Test(expected = NumberFormatException.class)
    public void testFormatLongOverflow()
    {
        System.out.println("FixedDecimalFloatFormat format long overflow");
        FixedDecimalFloatFormat formatter = new FixedDecimalFloatFormat();
        String result = formatter.format(999999999);
    }

    /**
     * Test overflow during format (double)
     */
    @Test(expected = NumberFormatException.class)
    public void testFormatDoubleOverflow()
    {
        System.out.println("FixedDecimalFloatFormat format double overflow");
        FixedDecimalFloatFormat formatter = new FixedDecimalFloatFormat();
        String result = formatter.format(999999999.99);
    }

    /**
     * Test of format handling, of class FixedDecimalFloatFormat.
     */
    @Test
    public void testFormatDoubleLeadingSpace()
    {
        System.out.println("FixedDecimalFloatFormat format double");
        FixedDecimalFloatFormat formatter = new FixedDecimalFloatFormat();
        String result = formatter.format(135.3);

        assertEquals("   135.3", result);
    }

    /**
     * Test of format handling, of class FixedDecimalFloatFormat.
     */
    @Test
    public void testFormatDoubleNoLeadingSpace()
    {
        System.out.println("FixedDecimalFloatFormat format double");
        FixedDecimalFloatFormat formatter = new FixedDecimalFloatFormat();
        String result = formatter.format(135442.4);

        assertEquals("135442.4", result);
    }

    /**
     * Test of decimalFloatFormat handling, of class FixedDecimalFloatFormat.
     */
    @Test
    public void testFormatNegativeDoubleLeadingSpace()
    {
        System.out.println("FixedDecimalFloatFormat format negative double");
        FixedDecimalFloatFormat formatter = new FixedDecimalFloatFormat();
        String result = formatter.format(-130.3);

        assertEquals("  -130.3", result);
    }

    /**
     * Test of decimalFloatFormat handling, of class FixedDecimalFloatFormat.
     */
    @Test
    public void testFormatNegativeDoubleNoLeadingSpace()
    {
        System.out.println("FixedDecimalFloatFormat format negative double");
        FixedDecimalFloatFormat formatter = new FixedDecimalFloatFormat();
        String result = formatter.format(-13078.3);

        assertEquals("-13078.3", result);
    }

    /**
     * Test of parsing, of class FixedDecimalFloatFormat.
     */
    @Test
    public void testParseDouble()
    {
        System.out.println("FixedDecimalFloatFormat parse double");
        FixedDecimalFloatFormat formatter = new FixedDecimalFloatFormat();

        try
        {
            double result = formatter.parse("   127.5").doubleValue();
            assertEquals(127.5, result, epsilon);
        } catch (ParseException ex)
        {
            fail("Parse exception");
        }

    }

    /**
     * Test of parsing, of class FixedDecimalFloatFormat.
     */
    @Test
    public void testParseInt()
    {
        System.out.println("FixedDecimalFloatFormat parse int");
        FixedDecimalFloatFormat formatter = new FixedDecimalFloatFormat();

        try
        {
            int result = formatter.parse("     127").intValue();
            assertEquals(127, result);
        } catch (ParseException ex)
        {
            fail("Unexpected ParseException");
        }
    }

    /**
     * Test overflow during parse
     */
    @Test(expected = NumberFormatException.class)
    public void testParseOverflow()
    {
        System.out.println("FixedDecimalFloatFormat format double");
        FixedDecimalFloatFormat formatter = new FixedDecimalFloatFormat();
        try
        {
            formatter.parse("999999999");
        } catch (ParseException ex)
        {
            fail("Unexpected ParseException");
        }
    }

}
