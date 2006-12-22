/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.csv;

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
    printer.setStrategy(CSVStrategy.EXCEL_STRATEGY);
    String[] line1 = {"a", "b"};
    printer.println(line1);
    assertEquals("a,b" + lineSeparator, sw.toString());
  }

  public void testExcelPrinter2() {
    StringWriter sw = new StringWriter();
    CSVPrinter printer = new CSVPrinter(sw);
    printer.setStrategy(CSVStrategy.EXCEL_STRATEGY);
    String[] line1 = {"a,b", "b"};
    printer.println(line1);
    assertEquals("\"a,b\",b" + lineSeparator, sw.toString());
  }

}
