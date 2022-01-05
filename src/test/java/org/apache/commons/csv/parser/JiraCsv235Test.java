package org.apache.commons.csv.parser;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class JiraCsv235Test {


    @Test
    public void testCSV235() throws IOException {
        final String dqString = "\"aaa\",\"b\"\"bb\",\"ccc\""; // "aaa","b""bb","ccc"
        final Iterator<CSVRecord> records = CSVFormat.RFC4180.parse(new StringReader(dqString)).iterator();
        final CSVRecord record = records.next();
        assertFalse(records.hasNext());
        assertEquals(3, record.size());
        assertEquals("aaa", record.get(0));
        assertEquals("b\"bb", record.get(1));
        assertEquals("ccc", record.get(2));
    }
}
