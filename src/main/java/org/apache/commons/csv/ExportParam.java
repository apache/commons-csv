package org.apache.commons.csv;

import java.util.Map;

/**
 * @author chengdu
 */
public class ExportParam {
    private String header;

    private String recordSeparator;

    private int sum;

    private int pageSize;

    private Map<String, Object> searchParam;

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Map<String, Object> getSearchParam() {
        return searchParam;
    }

    public void setSearchParam(Map<String, Object> searchParam) {
        this.searchParam = searchParam;
    }

    public String getRecordSeparator() {
        return recordSeparator;
    }

    public void setRecordSeparator(String recordSeparator) {
        this.recordSeparator = recordSeparator;
    }
}
