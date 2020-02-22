package org.apache.commons.csv;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
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
     * @param pageQueryFun
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

    private static List<Integer> calIndexList(int sum, int pageNum) {
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
