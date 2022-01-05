package org.apache.commons.csv.format;

import org.apache.commons.csv.CSVFormat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class CSVFormatCommentTest {

    @Test
    public void testEscapeSameAsCommentStartThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.builder().setEscape('!').setCommentMarker('!').build());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testEscapeSameAsCommentStartThrowsException_Deprecated() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.withEscape('!').withCommentMarker('!'));
    }

    @Test
    public void testEscapeSameAsCommentStartThrowsExceptionForWrapperType() {
        // Cannot assume that callers won't use different Character objects
        assertThrows(
                IllegalArgumentException.class,
                () -> CSVFormat.DEFAULT.builder().setEscape(Character.valueOf('!')).setCommentMarker(Character.valueOf('!')).build());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testEscapeSameAsCommentStartThrowsExceptionForWrapperType_Deprecated() {
        // Cannot assume that callers won't use different Character objects
        assertThrows(
                IllegalArgumentException.class,
                () -> CSVFormat.DEFAULT.withEscape(Character.valueOf('!')).withCommentMarker(Character.valueOf('!')));
    }
}
