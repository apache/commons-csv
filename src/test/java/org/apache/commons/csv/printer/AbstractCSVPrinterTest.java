package org.apache.commons.csv.printer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.IOException;
import java.util.Objects;

public abstract class AbstractCSVPrinterTest {

    public static final char DQUOTE_CHAR = '"';

    public final String recordSeparator = CSVFormat.DEFAULT.getRecordSeparator();


    /**
     * Converts an input CSV array into expected output values WRT NULLs. NULL strings are converted to null values
     * because the parser will convert these strings to null.
     */
    public <T> T[] expectNulls(final T[] original, final CSVFormat csvFormat) {
        final T[] fixed = original.clone();
        for (int i = 0; i < fixed.length; i++) {
            if (Objects.equals(csvFormat.getNullString(), fixed[i])) {
                fixed[i] = null;
            }
        }
        return fixed;
    }

    public String[] toFirstRecordValues(final String expected, final CSVFormat format) throws IOException {
        return CSVParser.parse(expected, format).getRecords().get(0).values();
    }
}
