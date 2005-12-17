/*
 * Copyright (C) 2004 by Netcetera AG.
 * All rights reserved.
 *
 * The copyright to the computer program(s) herein is the property of
 * Netcetera AG, Switzerland.  The program(s) may be used and/or copied
 * only with the written permission of Netcetera AG or in accordance
 * with the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * @(#) $Id: FormatTests.java,v 1.3 2004/08/10 12:38:45 rgrunder Exp $
 */
package ch.netcetera.wake.core.format;

import junit.framework.Test;
import junit.framework.TestSuite;

import ch.netcetera.wake.core.format.csv.CSVParserTest;
import ch.netcetera.wake.core.format.csv.CSVPrinterTest;
import ch.netcetera.wake.core.format.csv.ExtendedBufferedReaderTest;


public final class FormatTests {

  protected FormatTests() {
    // empty
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  public static Test suite() {
    TestSuite suite = new TestSuite("wake.core.format tests");
    suite.addTest(ExtendedBufferedReaderTest.suite());
    suite.addTest(CSVParserTest.suite());
    suite.addTest(CSVPrinterTest.suite());
    return suite;
  }
}
