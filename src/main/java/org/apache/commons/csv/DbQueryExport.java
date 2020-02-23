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

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * export data from db
 * @author chengdu
 */
public class DbQueryExport {

    private BufferedWriter bufferedWriter;

    private ExportParam exportParam;

    public DbQueryExport(BufferedWriter bufferedWriter, ExportParam exportParam) {
        this.bufferedWriter = bufferedWriter;
        this.exportParam = exportParam;
    }

    /**
     * export data from db
     * @param pageQueryFun query function
     */
    public void exportQueryPage(Function<Map<String, Object>, List<String>> pageQueryFun) {
        try {
            bufferedWriter.append(exportParam.getHeader()).append(exportParam.getRecordSeparator());
            int sum = exportParam.getSum();
            int pageSize = exportParam.getPageSize();
            List<Integer> indexList = calIndexList(sum, pageSize);
            Map<String, Object> searchParam = exportParam.getSearchParam();
            searchParam.put(Constants.PAGE_QUERY_SIZE, pageSize);
            for (Integer index : indexList) {
                searchParam.put(Constants.PADE_QUERY_INDEX, index);
                List<String> queryList = pageQueryFun.apply(searchParam);
                if (queryList != null && queryList.size() > 0) {
                    for (String rowData : queryList) {
                        bufferedWriter.append(rowData).append(exportParam.getRecordSeparator());
                    }
                }
            }
        } catch (Exception e) {
            throw new ExportException("export data error", e);
        }finally{
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * calculate page start index
     * @param sum total number of data
     * @param pageNum page size
     * @return index collection
     */
    public static List<Integer> calIndexList(int sum, int pageNum) {
        List<Integer> list = new ArrayList<>(sum / pageNum);
        Integer startIndex = 0;
        if (sum <= pageNum) {
            list.add(startIndex);
            return list;
        }
        while (startIndex + pageNum < sum) {
            list.add(startIndex);
            startIndex = startIndex + pageNum;
        }
        // the last page
        list.add(startIndex);
        return list;
    }
}
