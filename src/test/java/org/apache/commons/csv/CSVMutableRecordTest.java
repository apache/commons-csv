package org.apache.commons.csv;

import org.junit.Assert;

public class CSVMutableRecordTest extends CSVRecordTest {

    @Override
    protected CSVFormat createCommaFormat() {
        return super.createCommaFormat().withMutableRecords(true);
    }

    @Override
    protected CSVFormat createDefaultFormat() {
        return super.createDefaultFormat().withMutableRecords(true);
    }

    @Override
    protected CSVRecord newRecord() {
        return new CSVMutableRecord(values, null, null, 0, -1);
    }

    @Override
    protected CSVRecord newRecordWithHeader() {
        return new CSVMutableRecord(values, header, null, 0, -1);
    }

    @Override
    protected void validate(final CSVRecord anyRecord) {
        Assert.assertEquals(CSVMutableRecord.class, anyRecord.getClass());
    }

}
