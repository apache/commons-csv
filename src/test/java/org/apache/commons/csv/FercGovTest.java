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

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Real world examples from http://www.ferc.gov/docs-filing/eqr/soft-tools/sample-csv.asp
 */
public class FercGovTest {

    private enum ContractColumnNames {
        contract_id, seller_company_name, customer_company_name, customer_duns_number, contract_affiliate,
        FERC_tariff_reference, contract_service_agreement_id, contract_execution_date, contract_commencement_date,
        contract_termination_date, actual_termination_date, extension_provision_description, class_name, term_name,
        increment_name, increment_peaking_name, product_type_name, product_name, quantity, units_for_contract, rate,
        rate_minimum, rate_maximum, rate_description, units_for_rate, point_of_receipt_control_area,
        point_of_receipt_specific_location, point_of_delivery_control_area, point_of_delivery_specific_location,
        begin_date, end_date, time_zone;
    }

    private static final Charset US_ASCII = Charset.forName("US-ASCII");

    @Test
    public void testContractFile() throws IOException {
        final URL contractData = ClassLoader.getSystemClassLoader().getResource("ferc.gov/contract.txt");
        final CSVParser parser = CSVParser.parse(contractData, US_ASCII, CSVFormat.DEFAULT.withHeader());
        try {
            final List<CSVRecord> records = parser.getRecords();
            CSVRecord record = records.get(0);
            Assert.assertEquals(22, records.size());
            // first record
            Assert.assertEquals("C71", record.get(ContractColumnNames.contract_id));
            Assert.assertEquals("The Electric Company", record.get(ContractColumnNames.seller_company_name));
            Assert.assertEquals("ES", record.get(ContractColumnNames.time_zone));
            // last record
            record = records.get(records.size() - 1);
            // first record
            Assert.assertEquals("C78", record.get(ContractColumnNames.contract_id));
            Assert.assertEquals("The Electric Company", record.get(ContractColumnNames.seller_company_name));
            Assert.assertEquals("EP", record.get(ContractColumnNames.time_zone));
        } finally {
            parser.close();
        }
    }

    @Test
    public void testTransactionFile() throws IOException {
        final URL transactionData = ClassLoader.getSystemClassLoader().getResource("ferc.gov/transaction.txt");
        final CSVParser parser = CSVParser.parse(transactionData, US_ASCII,
                CSVFormat.DEFAULT.withHeader());
        try {
            final List<CSVRecord> records = parser.getRecords();
            Assert.assertEquals(24, records.size());
            CSVRecord record = records.get(0);
            // first record
            Assert.assertEquals("T1", record.get("transaction_unique_identifier"));
            Assert.assertEquals("The Electric Company", record.get("seller_company_name"));
            Assert.assertEquals("880386", record.get("transaction_charge"));
            // last record
            record = records.get(records.size() - 1);
            Assert.assertEquals("T15", record.get("transaction_unique_identifier"));
            Assert.assertEquals("The Electric Company", record.get("seller_company_name"));
            Assert.assertEquals("1800", record.get("transaction_charge"));
        } finally {
            parser.close();
        }
    }
}
