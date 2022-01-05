package org.apache.commons.csv.format;

import org.apache.commons.csv.CSVFormat;
import org.junit.jupiter.api.Test;

import static org.apache.commons.csv.Constants.CR;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CSVFormatDelimiterTest {

    @SuppressWarnings("deprecation")
    @Test
    public void testDelimiterSameAsCommentStartThrowsException_Deprecated() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.withDelimiter('!').withCommentMarker('!'));
    }

    @Test
    public void testDelimiterSameAsCommentStartThrowsException1() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.builder().setDelimiter('!').setCommentMarker('!').build());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testDelimiterSameAsEscapeThrowsException_Deprecated() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.withDelimiter('!').withEscape('!'));
    }

    @Test
    public void testDelimiterSameAsEscapeThrowsException1() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.builder().setDelimiter('!').setEscape('!').build());
    }

    @Test
    public void testDelimiterSameAsRecordSeparatorThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.newFormat(CR));
    }

}
