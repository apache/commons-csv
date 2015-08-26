package org.apache.commons.csv;

import org.junit.Before;
import org.junit.Test;

import java.util.Properties;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class FormatPropertiesTest {

    private static final String SOME_KEY = "key";
    FormatProperties formatProperties;

    Properties mockProperties;

    @Before
    public void setUp() {
        mockProperties = new Properties();
        formatProperties = new FormatProperties(mockProperties);
    }

    @Test
     public void shouldGetEmptyArrayForNonexistentKey() throws Exception {
        String[] array = formatProperties.getAsArray(SOME_KEY);
        assertThat(array.length, is(equalTo(0)));
    }

    @Test
    public void shouldGetDefaultArrayForNonexistentKey() throws Exception {
        String[] array = {"Uno", "Duo"};
        assertThat(formatProperties.getAsArray(SOME_KEY, array), is(equalTo(array)));
    }

    @Test
    public void shouldCreateArrayOfStrings() throws Exception {
        mockProperties.setProperty(SOME_KEY, "Comma,separated,value");
        String[] array = formatProperties.getAsArray(SOME_KEY);
        assertThat(array, is(equalTo(
                new String[]{
                        "Comma",
                        "separated",
                        "value"
                }))
        );
    }

    @Test
    public void shouldReturnFalseForNonexistentKey() throws Exception {
        assertThat(formatProperties.getAsBoolean(SOME_KEY), is(false));
    }

    @Test
    public void shouldReturnDefaultBooleanForNonexistentKey() throws Exception {
        assertThat(formatProperties.getAsBoolean(SOME_KEY, true), is(true));
    }

    @Test
    public void shouldReturnBoolean() throws Exception {
        mockProperties.setProperty(SOME_KEY, Boolean.TRUE.toString());
        assertThat(formatProperties.getAsBoolean(SOME_KEY), is(true));
    }

    @Test
    public void shouldGetNullCharForNonexistentKey() throws Exception {
        assertThat(formatProperties.getAsChar(SOME_KEY), is(nullValue()));
    }

    @Test
    public void shouldGetDefaultCharForNonexistentKey() throws Exception {
        assertThat(formatProperties.getAsChar(SOME_KEY, '^'), is('^'));
    }

    @Test
    public void shouldReturnChar() throws Exception {
        mockProperties.setProperty(SOME_KEY, "F");

        assertThat(formatProperties.getAsChar(SOME_KEY), is(equalTo('F')));
    }

    @Test
    public void shouldGetNullQuoteModeForNonexistentKey() throws Exception {
        assertThat(formatProperties.getAsQuoteMode(SOME_KEY), is(nullValue()));
    }

    @Test
    public void shouldGetDefaultQuoteModeForNonexistentKey() throws Exception {
        assertThat(formatProperties.getAsQuoteMode(SOME_KEY, QuoteMode.NON_NUMERIC), is(QuoteMode.NON_NUMERIC));
    }

    @Test
    public void shouldReturnQuoteMode() throws Exception {
        QuoteMode quoteMode = QuoteMode.MINIMAL;
        mockProperties.setProperty(SOME_KEY, quoteMode.toString());

        assertThat(formatProperties.getAsQuoteMode(SOME_KEY), is(equalTo(quoteMode)));
    }

    @Test
    public void shouldGetNullStringForNonexistentKey() throws Exception {
        assertThat(formatProperties.get(SOME_KEY), is(nullValue()));
    }

    @Test
    public void shouldGetDefaultStringForNonexistentKey() throws Exception {
        String defaultValue = UUID.randomUUID().toString();
        assertThat(formatProperties.get(SOME_KEY, defaultValue), is(defaultValue));
    }

    @Test
    public void shouldReturnProperty() throws Exception {
        String expectedValue = UUID.randomUUID().toString();
        mockProperties.setProperty(SOME_KEY, expectedValue);

        assertThat(formatProperties.get(SOME_KEY), is(equalTo(expectedValue)));
    }

    @Test
    public void shouldAdmitThatContainsProperty() throws Exception {
        mockProperties.setProperty(SOME_KEY, "Val");

        assertThat(formatProperties.contains(SOME_KEY), is(true));
    }

    @Test
    public void shouldAdmitThatDoesNotContainProperty() throws Exception {
        assertThat(formatProperties.contains(SOME_KEY), is(false));
    }
}