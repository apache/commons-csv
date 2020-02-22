package org.apache.commons.csv;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author chengdu
 */
public class TestService {

    public List<String> queryDbPage(Map<String, Object> requestParam) {
        int startIndex = (int) requestParam.get(Constants.PADE_QUERY_INDEX);
        int pageSize = (int) requestParam.get(Constants.PAGE_QUERY_SIZE);
        String rowData = "chengdu,male,3281328128@qq.com";
        List<String> list = new ArrayList<>(pageSize);
        for (int i = 0; i < pageSize; i++) {
            int num = startIndex + i;
            list.add(num + " " + rowData);
        }
        return list;
    }
}
