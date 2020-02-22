package org.apache.commons.csv;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * @author chengdu
 */
public class DbQueryExportTest {

    private static final File BASE = new File("src/test/resources/DbQueryExport");

    @Test
    public void testExport() {
        String filePath = BASE + File.separator + "export-table.csv";
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        TestService testService = new TestService();
        try (
         BufferedWriter bufferedWriter = new BufferedWriter( new OutputStreamWriter(
                    new FileOutputStream(filePath, true), StandardCharsets.UTF_8))) {
            ExportParam exportParam = new ExportParam();
            exportParam.setHeader("name,gender,email");
            exportParam.setSum(10000000);
            exportParam.setPageSize(100000);
            exportParam.setRecordSeparator(Constants.CRLF);
            exportParam.setSearchParam(new HashMap<>());
            DbQueryExport DbQueryExport = new DbQueryExport(bufferedWriter, exportParam);
            DbQueryExport.exportQueryPage(testService::queryDbPage);
        } catch (IOException e) {
        }
    }
}
