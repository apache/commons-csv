/*
 * Copyright (C) 2002-2004 by Netcetera AG.
 * All rights reserved.
 *
 * The copyright to the computer program(s) herein is the property of
 * Netcetera AG, Switzerland.  The program(s) may be used and/or copied
 * only with the written permission of Netcetera AG or in accordance
 * with the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * @(#) $Id: CSVPrinter.java,v 1.4 2004/08/10 12:35:27 rgrunder Exp $
 */

package ch.netcetera.wake.core.format.csv;

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

  /** Character used to start comments. (Default is '#') */
  protected char commentStart = '#';
  
  /** Character used to separate entities. (Default is ',') */
  protected char separatorChar = ',';

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


  /**
   * Create a printer that will print values to the given
   * stream. Character to byte conversion is done using
   * the default character encoding.
   *
   * @param out stream to which to print.
   * @param commentStart Character used to start comments.
   */
  public CSVPrinter(OutputStream out, char commentStart) {
    this(out);
    this.commentStart = commentStart;
  }


  /**
   * Create a printer that will print values to the given
   * stream.
   *
   * @param out stream to which to print.
   * @param commentStart Character used to start comments.
   */
  public CSVPrinter(Writer out, char commentStart) {
    this(out);
    this.commentStart = commentStart;
  }

  /**
   * Gets the comment start character.
   * 
   * @return the commentStart character
   */
  public char getCommentStart() {
    return commentStart;
  }
  
  /**
   * Sets the comment start character.
   * 
   * @param commentStart commentStart character to set.
   */
  public void setCommentStart(char commentStart) {
    this.commentStart = commentStart;
  }
  
  /**
   * Gets the separator character.
   * 
   * @return Returns the separatorChar.
   */
  public char getSeparatorChar() {
    return separatorChar;
  }
  /**
   * Sets the separator character.
   * 
   * @param separatorChar The separatorChar to set.
   */
  public void setSeparatorChar(char separatorChar) {
    this.separatorChar = separatorChar;
  }
  
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
    if (!newLine) {
      out.println();
    }
    out.print(commentStart);
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
          out.print(commentStart);
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
        if (c == '"' || c == separatorChar || c == '\n' || c == '\r') {
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
      out.print(separatorChar);
    }
    if (quote) {
      out.print(escapeAndQuote(value));
    } else {
      out.print(value);
    }
    out.flush();
  }


  /**
   * Converts an array of string values into a single CSV line. All
   * <code>null</code> values are converted to the string <code>"null"</code>,
   * all strings equal to <code>"null"</code> will additionally get quotes
   * around.
   *
   * @param values the value array
   * @return the CSV string, will be an empty string if the length of the
   * value array is 0
   */
  public static String printLine(String[] values) {

    // set up a CSVPrinter
    StringWriter csvWriter = new StringWriter();
    CSVPrinter csvPrinter = new CSVPrinter(csvWriter);

    // check for null values an "null" as strings and convert them
    // into the strings "null" and "\"null\""
    for (int i = 0; i < values.length; i++) {
      if (values[i] == null) {
        values[i] = "null";
      } else if (values[i].equals("null")) {
        values[i] = "\"null\"";
      }
    }

    // convert to CSV
    csvPrinter.println(values);

    // as the resulting string has \r\n at the end, we will trim that away
    return csvWriter.toString().trim();
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
