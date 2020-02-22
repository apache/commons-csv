/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.csv;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author chengdu
 */
public class DbQueryExportTest {

    private static final File BASE = new File("src/test/resources/DbQueryExport");

    @Test
    public void testCalIndexList() {
        List<Integer> indexList = DbQueryExport.calIndexList(10000000, 100000);
        assertEquals(100, indexList.size());
        assertEquals(0, indexList.get(0));
    }

    @Test
    public void testParam() {
        ExportParam exportParam = new ExportParam();
        exportParam.setHeader("name,gender,email");
        exportParam.setSum(10000000);
        exportParam.setPageSize(100000);
        exportParam.setRecordSeparator(Constants.CRLF);
        Map<String, Object> searchParam = new HashMap<>(16);
        exportParam.setSearchParam(searchParam);
        assertEquals(10000000, exportParam.getSum());
        assertEquals(100000, exportParam.getPageSize());
    }

    @Test
    public void testExport() {
        String filePath = BASE + File.separator + "export-table.csv";
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        TestService testService = new TestService();
        try (BufferedWriter bufferedWriter = new BufferedWriter( new OutputStreamWriter(
                    new FileOutputStream(filePath, true), StandardCharsets.UTF_8))) {
            ExportParam exportParam = new ExportParam();
            exportParam.setHeader("name,gender,email");
            exportParam.setSum(10000000);
            exportParam.setPageSize(100000);
            exportParam.setRecordSeparator(Constants.CRLF);
            Map<String, Object> searchParam = new HashMap<>(16);
            exportParam.setSearchParam(searchParam);
            DbQueryExport DbQueryExport = new DbQueryExport(bufferedWriter, exportParam);
            DbQueryExport.exportQueryPage(testService::queryDbPage);
            assertEquals(2, searchParam.size());
            assertEquals(9900000, searchParam.get(Constants.PADE_QUERY_INDEX));
        } catch (IOException e) {
        }
    }

    @Test
    public void testErrorParam() {
        String filePath = BASE + File.separator + "export-table.csv";
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        Throwable throwable = assertThrows(ExportException.class, () -> {
            TestService testService = new TestService();
            ExportParam exportParam = new ExportParam();
            exportParam.setHeader("name,gender,email");
            exportParam.setSum(10000000);
            exportParam.setPageSize(100000);
            exportParam.setRecordSeparator(Constants.CRLF);
            exportParam.setSearchParam(null);
            DbQueryExport DbQueryExport = new DbQueryExport(null, exportParam);
            DbQueryExport.exportQueryPage(testService::queryDbPage);
        });
        assertEquals("export data error", throwable.getMessage());
    }

    @Test
    public void testException() {
        Throwable throwable = assertThrows(ExportException.class, () -> {
            try {
                int i = 1 / 0;
            } catch (Exception e) {
                throw new ExportException("cal error", e);
            }
        });
        assertEquals("cal error", throwable.getMessage());
    }
}
