package org.apache.commons.csv;

import java.util.Properties;

class FormatProperties {
    static final String QUOTE_CHARACTER = "org.apache.commons.csv.format.quoteCharacter";
    static final String QUOTE_MODE = "org.apache.commons.csv.format.quoteMode";
    static final String COMMENT_MARKER = "org.apache.commons.csv.format.commentMarker";
    static final String ESCAPE = "org.apache.commons.csv.format.escape";
    static final String IGNORE_SURROUNDING_SPACES = "org.apache.commons.csv.format.ignoreSurroundingSpaces";
    static final String IGNORE_EMPTY_LINES = "org.apache.commons.csv.format.ignoreEmptyLines";
    static final String RECORD_SEPARATOR = "org.apache.commons.csv.format.recordSeparator";
    static final String NULL_STRING = "org.apache.commons.csv.format.nullString";
    static final String HEADER_COMMENTS = "org.apache.commons.csv.format.headerComments";
    static final String HEADER = "org.apache.commons.csv.format.header";
    static final String SKIP_HEADER_RECORD = "org.apache.commons.csv.format.skipHeaderRecord";
    static final String ALLOW_MISSING_COLUMN_NAMES = "org.apache.commons.csv.format.allowMissingColumnNames";
    static final String DELIMITER = "org.apache.commons.csv.format.delimiter";

    private final Properties properties;

    FormatProperties(Properties properties) {
        this.properties = properties;
    }

    String[] getAsArray(String propertyName) {
        String property = properties.getProperty(propertyName);
        return property == null ? new String[]{} : property.split(",");
    }

    String[] getAsArray(String propertyName, String[] defaultValue) {
        return contains(propertyName)
                ? getAsArray(propertyName)
                : defaultValue;
    }

    boolean getAsBoolean(String propertyName) {
        return Boolean.valueOf(
                properties.getProperty(propertyName)
        );
    }

    boolean getAsBoolean(String propertyName, boolean defaultValue) {
        return contains(propertyName)
                ? getAsBoolean(propertyName)
                : defaultValue;
    }

    Character getAsChar(String propertyName) {
        return firstCharFrom(
                properties.getProperty(propertyName));
    }
    Character getAsChar(String propertyName, Character defaultValue) {
        return contains(propertyName)
                ? getAsChar(propertyName)
                : defaultValue;
    }

    QuoteMode getAsQuoteMode(String propertyName) {
        String property = properties.getProperty(propertyName);
        return property == null ? null : QuoteMode.valueOf(property);
    }

    QuoteMode getAsQuoteMode(String propertyName, QuoteMode defaultValue) {
        return contains(propertyName)
                ? getAsQuoteMode(propertyName)
                : defaultValue;
    }

    private Character firstCharFrom(String string) {
        return string == null ? null : string.charAt(0);
    }

    public String get(String propertyName) {
        return properties.getProperty(propertyName);
    }

    public String get(String propertyName, String defaultValue) {
        return contains(propertyName)
                ? get(propertyName)
                : defaultValue;
    }

    public boolean contains(String propertyName) {
        return properties.getProperty(propertyName) != null;
    }
}
