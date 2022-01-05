package org.apache.commons.csv.parser;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class CSVParserPropertiesTest {


    private void parseFully(final CSVParser parser) {
        for (final CSVRecord csvRecord : parser) {
            assertNotNull(csvRecord);
        }
    }

    @Test
    public void testParse() throws Exception {
        final ClassLoader loader = ClassLoader.getSystemClassLoader();
        final URL url = loader.getResource("org/apache/commons/csv/CSVFileParser/test.csv");
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("A", "B", "C", "D");
        final Charset charset = StandardCharsets.UTF_8;

        try (final CSVParser parser = CSVParser.parse(new InputStreamReader(url.openStream(), charset), format)) {
            parseFully(parser);
        }
        try (final CSVParser parser = CSVParser.parse(new String(Files.readAllBytes(Paths.get(url.toURI())), charset),
                format)) {
            parseFully(parser);
        }
        try (final CSVParser parser = CSVParser.parse(new File(url.toURI()), charset, format)) {
            parseFully(parser);
        }
        try (final CSVParser parser = CSVParser.parse(url.openStream(), charset, format)) {
            parseFully(parser);
        }
        try (final CSVParser parser = CSVParser.parse(Paths.get(url.toURI()), charset, format)) {
            parseFully(parser);
        }
        try (final CSVParser parser = CSVParser.parse(url, charset, format)) {
            parseFully(parser);
        }
        try (final CSVParser parser = new CSVParser(new InputStreamReader(url.openStream(), charset), format)) {
            parseFully(parser);
        }
        try (final CSVParser parser = new CSVParser(new InputStreamReader(url.openStream(), charset), format,
                /* characterOffset= */0, /* recordNumber= */1)) {
            parseFully(parser);
        }
    }

    @Test
    public void testParseFileNullFormat() {
        assertThrows(NullPointerException.class,
                () -> CSVParser.parse(new File("CSVFileParser/test.csv"), Charset.defaultCharset(), null));
    }

    @Test
    public void testParseNullFileFormat() {
        assertThrows(NullPointerException.class,
                () -> CSVParser.parse((File) null, Charset.defaultCharset(), CSVFormat.DEFAULT));
    }

    @Test
    public void testParseNullPathFormat() {
        assertThrows(NullPointerException.class,
                () -> CSVParser.parse((Path) null, Charset.defaultCharset(), CSVFormat.DEFAULT));
    }

    @Test
    public void testParseNullStringFormat() {
        assertThrows(NullPointerException.class, () -> CSVParser.parse((String) null, CSVFormat.DEFAULT));
    }

    @Test
    public void testParseNullUrlCharsetFormat() {
        assertThrows(NullPointerException.class,
                () -> CSVParser.parse((URL) null, Charset.defaultCharset(), CSVFormat.DEFAULT));
    }

    @Test
    public void testParserUrlNullCharsetFormat() {
        assertThrows(NullPointerException.class,
                () -> CSVParser.parse(new URL("https://commons.apache.org"), null, CSVFormat.DEFAULT));
    }

    @Test
    public void testParseStringNullFormat() {
        assertThrows(NullPointerException.class, () -> CSVParser.parse("csv data", (CSVFormat) null));
    }

    @Test
    public void testParseUrlCharsetNullFormat() {
        assertThrows(NullPointerException.class,
                () -> CSVParser.parse(new URL("https://commons.apache.org"), Charset.defaultCharset(), null));
    }

    @Test
    public void testParseWithDelimiterStringWithEscape() throws IOException {
        final String source = "a![!|!]b![|]c[|]xyz\r\nabc[abc][|]xyz";
        final CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setDelimiter("[|]").setEscape('!').build();
        try (CSVParser csvParser = csvFormat.parse(new StringReader(source))) {
            CSVRecord csvRecord = csvParser.nextRecord();
            assertEquals("a[|]b![|]c", csvRecord.get(0));
            assertEquals("xyz", csvRecord.get(1));
            csvRecord = csvParser.nextRecord();
            assertEquals("abc[abc]", csvRecord.get(0));
            assertEquals("xyz", csvRecord.get(1));
        }
    }

    @Test
    public void testParseWithDelimiterStringWithQuote() throws IOException {
        final String source = "'a[|]b[|]c'[|]xyz\r\nabc[abc][|]xyz";
        final CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setDelimiter("[|]").setQuote('\'').build();
        try (CSVParser csvParser = csvFormat.parse(new StringReader(source))) {
            CSVRecord csvRecord = csvParser.nextRecord();
            assertEquals("a[|]b[|]c", csvRecord.get(0));
            assertEquals("xyz", csvRecord.get(1));
            csvRecord = csvParser.nextRecord();
            assertEquals("abc[abc]", csvRecord.get(0));
            assertEquals("xyz", csvRecord.get(1));
        }
    }

    @Test
    public void testParseWithDelimiterWithEscape() throws IOException {
        final String source = "a!,b!,c,xyz";
        final CSVFormat csvFormat = CSVFormat.DEFAULT.withEscape('!');
        try (CSVParser csvParser = csvFormat.parse(new StringReader(source))) {
            final CSVRecord csvRecord = csvParser.nextRecord();
            assertEquals("a,b,c", csvRecord.get(0));
            assertEquals("xyz", csvRecord.get(1));
        }
    }

    @Test
    public void testParseWithDelimiterWithQuote() throws IOException {
        final String source = "'a,b,c',xyz";
        final CSVFormat csvFormat = CSVFormat.DEFAULT.withQuote('\'');
        try (CSVParser csvParser = csvFormat.parse(new StringReader(source))) {
            final CSVRecord csvRecord = csvParser.nextRecord();
            assertEquals("a,b,c", csvRecord.get(0));
            assertEquals("xyz", csvRecord.get(1));
        }
    }

    @Test
    public void testParseWithQuoteThrowsException() {
        final CSVFormat csvFormat = CSVFormat.DEFAULT.withQuote('\'');
        assertThrows(IOException.class, () -> csvFormat.parse(new StringReader("'a,b,c','")).nextRecord());
        assertThrows(IOException.class, () -> csvFormat.parse(new StringReader("'a,b,c'abc,xyz")).nextRecord());
        assertThrows(IOException.class, () -> csvFormat.parse(new StringReader("'abc'a,b,c',xyz")).nextRecord());
    }

    @Test
    public void testParseWithQuoteWithEscape() throws IOException {
        final String source = "'a?,b?,c?d',xyz";
        final CSVFormat csvFormat = CSVFormat.DEFAULT.withQuote('\'').withEscape('?');
        try (CSVParser csvParser = csvFormat.parse(new StringReader(source))) {
            final CSVRecord csvRecord = csvParser.nextRecord();
            assertEquals("a,b,c?d", csvRecord.get(0));
            assertEquals("xyz", csvRecord.get(1));
        }
    }


}
