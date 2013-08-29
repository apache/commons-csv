package org.apache.commons.csv;

/**
 * Utility class for input parameter validation
 *
 * @version $Id$
 */
final class Assertions {

    private Assertions() {
        // can not be instantiated
    }

    public static <T> void notNull(T parameter, String parameterName) {
        if (parameter == null) {
            throw new IllegalArgumentException("Parameter '" + parameterName + "' must not be null!");
        }
    }
}
