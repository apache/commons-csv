/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Print values as a comma separated list.
 */
public class CSVPrinter {

  /** The place that the values get written. */
  protected PrintWriter out;

  /** True if we just began a new line. */
  protected boolean newLine = true;

  private CSVStrategy strategy = CSVStrategy.DEFAULT_STRATEGY;

  /**
   * Create a printer that will print values to the given
   * stream. Character to byte conversion is done using
   * the default character encoding. Comments will be
   * written using the default comment character '#'.
   *
   * @param out stream to which to print.
   */
  public CSVPrinter(OutputStream out) {
    this.out = new PrintWriter(out);
  }


  /**
   * Create a printer that will print values to the given
   * stream. Comments will be
   * written using the default comment character '#'.
   *
   * @param out stream to which to print.
   */
  public CSVPrinter(Writer out) {
    if (out instanceof PrintWriter) {
      this.out = (PrintWriter) out;
    } else {
      this.out = new PrintWriter(out);
    }
  }


  // ======================================================
  //  strategies
  // ======================================================
  
  /**
   * Sets the specified CSV Strategy
   *
   * @return current instance of CSVParser to allow chained method calls
   */
  public CSVPrinter setStrategy(CSVStrategy strategy) {
    this.strategy = strategy;
    return this;
  }
  
  /**
   * Obtain the specified CSV Strategy
   * 
   * @return strategy currently being used
   */
  public CSVStrategy getStrategy() {
    return this.strategy;
  }
  
  // ======================================================
  //  printing implementation
  // ======================================================

  /**
   * Print the string as the last value on the line. The value
   * will be quoted if needed.
   *
   * @param value value to be outputted.
   */
  public void println(String value) {
    print(value);
    out.println();
    out.flush();
    newLine = true;
  }


  /**
   * Output a blank line
   */
  public void println() {
    out.println();
    out.flush();
    newLine = true;
  }


  /**
   * Print a single line of comma separated values.
   * The values will be quoted if needed.  Quotes and
   * newLine characters will be escaped.
   *
   * @param values values to be outputted.
   */
  public void println(String[] values) {
    for (int i = 0; i < values.length; i++) {
      print(values[i]);
    }
    out.println();
    out.flush();
    newLine = true;
  }


  /**
   * Print several lines of comma separated values.
   * The values will be quoted if needed.  Quotes and
   * newLine characters will be escaped.
   *
   * @param values values to be outputted.
   */
  public void println(String[][] values) {
    for (int i = 0; i < values.length; i++) {
      println(values[i]);
    }
    if (values.length == 0) {
      out.println();
    }
    out.flush();
    newLine = true;
  }


  /**
   * Put a comment among the comma separated values.
   * Comments will always begin on a new line and occupy a
   * least one full line. The character specified to star
   * comments and a space will be inserted at the beginning of
   * each new line in the comment.
   *
   * @param comment the comment to output
   */
  public void printlnComment(String comment) {
    if(this.strategy.isCommentingDisabled()) {
        return;
    }
    if (!newLine) {
      out.println();
    }
    out.print(this.strategy.getCommentStart());
    out.print(' ');
    for (int i = 0; i < comment.length(); i++) {
      char c = comment.charAt(i);
      switch (c) {
        case '\r' :
          if (i + 1 < comment.length() && comment.charAt(i + 1) == '\n') {
            i++;
          }
          // break intentionally excluded.
        case '\n' :
          out.println();
          out.print(this.strategy.getCommentStart());
          out.print(' ');
          break;
        default :
          out.print(c);
          break;
      }
    }
    out.println();
    out.flush();
    newLine = true;
  }


  /**
   * Print the string as the next value on the line. The value
   * will be quoted if needed.
   *
   * @param value value to be outputted.
   */
  public void print(String value) {
    boolean quote = false;
    if (value.length() > 0) {
      char c = value.charAt(0);
      if (newLine
        && (c < '0'
          || (c > '9' && c < 'A')
          || (c > 'Z' && c < 'a')
          || (c > 'z'))) {
        quote = true;
      }
      if (c == ' ' || c == '\f' || c == '\t') {
        quote = true;
      }
      for (int i = 0; i < value.length(); i++) {
        c = value.charAt(i);
        if (c == '"' || c == this.strategy.getDelimiter() || c == '\n' || c == '\r') {
          quote = true;
        }
      }
      if (c == ' ' || c == '\f' || c == '\t') {
        quote = true;
      }
    } else if (newLine) {
      // always quote an empty token that is the first
      // on the line, as it may be the only thing on the
      // line. If it were not quoted in that case,
      // an empty line has no tokens.
      quote = true;
    }
    if (newLine) {
      newLine = false;
    } else {
      out.print(this.strategy.getDelimiter());
    }
    if (quote) {
      out.print(escapeAndQuote(value));
    } else {
      out.print(value);
    }
    out.flush();
  }


  /**
   * Enclose the value in quotes and escape the quote
   * and comma characters that are inside.
   *
   * @param value needs to be escaped and quoted
   * @return the value, escaped and quoted
   */
  private static String escapeAndQuote(String value) {
    // the initial count is for the preceding and trailing quotes
    int count = 2;
    for (int i = 0; i < value.length(); i++) {
      switch (value.charAt(i)) {
        case '\"' :
        case '\n' :
        case '\r' :
        case '\\' :
          count++;
          break;
        default:
          break;
      }
    }
    StringBuffer sb = new StringBuffer(value.length() + count);
    sb.append('"');
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      switch (c) {
        case '\"' :
          sb.append("\\\"");
          break;
        case '\n' :
          sb.append("\\n");
          break;
        case '\r' :
          sb.append("\\r");
          break;
        case '\\' :
          sb.append("\\\\");
          break;
        default :
          sb.append(c);
      }
    }
    sb.append('"');
    return sb.toString();
  }

}
