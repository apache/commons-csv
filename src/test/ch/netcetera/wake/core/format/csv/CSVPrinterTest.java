/*
* The copyright to the computer program(s) herein is the property of
* Netcetera AG, Switzerland. The program(s) may be used and/or copied
* only with the written permission of Netcetera AG or in accordance
* with the terms and conditions stipulated in the agreement/contract
* under which the program(s) have been supplied.
*
* @(#) $Id: CSVPrinterTest.java,v 1.1 2004/08/10 12:38:45 rgrunder Exp $
*/
package ch.netcetera.wake.core.format.csv;

import java.io.StringWriter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * CSVPrinterTest
 */
public class CSVPrinterTest extends TestCase {
  
  String lineSeparator = null;
  
  /**
   * Constructor for CSVPrinterTest.
   */
  public CSVPrinterTest(String name) {
    super(name);
    this.lineSeparator = System.getProperty("line.separator");
  }

  public static Test suite() {
    return new TestSuite(CSVPrinterTest.class);
  }

  public void testPrinter1() {
    StringWriter sw = new StringWriter();
    CSVPrinter printer = new CSVPrinter(sw);
    String[] line1 = {"a", "b"};
    printer.println(line1);
    assertEquals("a,b" + lineSeparator, sw.toString());
  }

  public void testPrinter2() {
    StringWriter sw = new StringWriter();
    CSVPrinter printer = new CSVPrinter(sw);
    String[] line1 = {"a,b", "b"};
    printer.println(line1);
    assertEquals("\"a,b\",b" + lineSeparator, sw.toString());
  }

  public void testPrinter3() {
    StringWriter sw = new StringWriter();
    CSVPrinter printer = new CSVPrinter(sw);
    String[] line1 = {"a, b", "b "};
    printer.println(line1);
    assertEquals("\"a, b\",\"b \"" + lineSeparator, sw.toString());
  }

  public void testExcelPrinter1() {
    StringWriter sw = new StringWriter();
    CSVPrinter printer = new CSVPrinter(sw);
    printer.setSeparatorChar(';');
    String[] line1 = {"a", "b"};
    printer.println(line1);
    assertEquals("a;b" + lineSeparator, sw.toString());
  }

  public void testExcelPrinter2() {
    StringWriter sw = new StringWriter();
    CSVPrinter printer = new CSVPrinter(sw);
    printer.setSeparatorChar(';');
    String[] line1 = {"a;b", "b"};
    printer.println(line1);
    assertEquals("\"a;b\";b" + lineSeparator, sw.toString());
  }

}
