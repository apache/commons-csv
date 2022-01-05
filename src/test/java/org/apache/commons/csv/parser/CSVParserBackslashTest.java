package org.apache.commons.csv.parser;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.util.Utils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.apache.commons.csv.Constants.CRLF;
import static org.junit.jupiter.api.Assertions.*;

public class CSVParserBackslashTest {


    @Test
    public void testBackslashEscaping() throws IOException {

        // To avoid confusion over the need for escaping chars in java code,
        // We will test with a forward slash as the escape char, and a single
        // quote as the encapsulator.

        final String code = "one,two,three\n" // 0
                + "'',''\n" // 1) empty encapsulators
                + "/',/'\n" // 2) single encapsulators
                + "'/'','/''\n" // 3) single encapsulators encapsulated via escape
                + "'''',''''\n" // 4) single encapsulators encapsulated via doubling
                + "/,,/,\n" // 5) separator escaped
                + "//,//\n" // 6) escape escaped
                + "'//','//'\n" // 7) escape escaped in encapsulation
                + "   8   ,   \"quoted \"\" /\" // string\"   \n" // don't eat spaces
                + "9,   /\n   \n" // escaped newline
                + "";
        final String[][] res = {{"one", "two", "three"}, // 0
                {"", ""}, // 1
                {"'", "'"}, // 2
                {"'", "'"}, // 3
                {"'", "'"}, // 4
                {",", ","}, // 5
                {"/", "/"}, // 6
                {"/", "/"}, // 7
                {"   8   ", "   \"quoted \"\" /\" / string\"   "}, {"9", "   \n   "},};

        final CSVFormat format = CSVFormat.newFormat(',').withQuote('\'').withRecordSeparator(CRLF).withEscape('/')
                .withIgnoreEmptyLines();

        try (final CSVParser parser = CSVParser.parse(code, format)) {
            final List<CSVRecord> records = parser.getRecords();
            assertFalse(records.isEmpty());

            Utils.compare("Records do not match expected result", res, records);
        }
    }

    @Test
    public void testBackslashEscaping2() throws IOException {

        // To avoid confusion over the need for escaping chars in java code,
        // We will test with a forward slash as the escape char, and a single
        // quote as the encapsulator.

        final String code = "" + " , , \n" // 1)
                + " \t ,  , \n" // 2)
                + " // , /, , /,\n" // 3)
                + "";
        final String[][] res = {{" ", " ", " "}, // 1
                {" \t ", "  ", " "}, // 2
                {" / ", " , ", " ,"}, // 3
        };

        final CSVFormat format = CSVFormat.newFormat(',').withRecordSeparator(CRLF).withEscape('/')
                .withIgnoreEmptyLines();

        try (final CSVParser parser = CSVParser.parse(code, format)) {
            final List<CSVRecord> records = parser.getRecords();
            assertFalse(records.isEmpty());

            Utils.compare("", res, records);
        }
    }

    @Test
    @Disabled
    public void testBackslashEscapingOld() throws IOException {
        final String code = "one,two,three\n" + "on\\\"e,two\n" + "on\"e,two\n" + "one,\"tw\\\"o\"\n"
                + "one,\"t\\,wo\"\n" + "one,two,\"th,ree\"\n" + "\"a\\\\\"\n" + "a\\,b\n" + "\"a\\\\,b\"";
        final String[][] res = {{"one", "two", "three"}, {"on\\\"e", "two"}, {"on\"e", "two"}, {"one", "tw\"o"},
                {"one", "t\\,wo"}, // backslash in quotes only escapes a delimiter (",")
                {"one", "two", "th,ree"}, {"a\\\\"}, // backslash in quotes only escapes a delimiter (",")
                {"a\\", "b"}, // a backslash must be returned
                {"a\\\\,b"} // backslash in quotes only escapes a delimiter (",")
        };
        try (final CSVParser parser = CSVParser.parse(code, CSVFormat.DEFAULT)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(res.length, records.size());
            assertFalse(records.isEmpty());
            for (int i = 0; i < res.length; i++) {
                assertArrayEquals(res[i], records.get(i).values());
            }
        }
    }
}
