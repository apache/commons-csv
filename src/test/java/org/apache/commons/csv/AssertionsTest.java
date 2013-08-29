package org.apache.commons.csv;

import org.junit.Test;

/**
 * @version $Id$
 */
public class AssertionsTest {

    @Test
    public void testNotNull() throws Exception {
        Assertions.notNull(new Object(), "object");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotNullNull() throws Exception {
        Assertions.notNull(null, "object");
    }
}
