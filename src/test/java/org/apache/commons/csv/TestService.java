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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * mock the query method of the database
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
