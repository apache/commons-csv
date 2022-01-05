package org.apache.commons.csv.format;

import org.apache.commons.csv.CSVFormat;
import org.junit.jupiter.api.Test;

public class JiraCsv236Test {

    @Test
    public void testJiraCsv236() {
        CSVFormat.DEFAULT.builder().setAllowDuplicateHeaderNames(true).setHeader("CC","VV","VV").build();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testJiraCsv236__Deprecated() {
        CSVFormat.DEFAULT.withAllowDuplicateHeaderNames().withHeader("CC","VV","VV");
    }
}
