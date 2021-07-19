<!---
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->

Apache Commons CSV Benchmark
===================

Apache Commons CSV includes a Java Microbenchmark Harness (JMH) for testing various implementations
of CSV processing.

Prerequisite
-------------

The Skife CSV implementation is not available in Maven Central and therefore must be manually
installed into the local Maven repository. Run the falling script to download and install
the JAR file (~1MB).

```shell
./benchmark-prereq.sh
```

Benchmarks
-------------

Benchmark Name  | CSV Parser      | Description
--------------- | --------------- | -------------
read            | Java JDK        | Use BufferedReader to perform a line count
scan            | Java JDK        | Use Scanner to perform a line count
split           | Java JDK        | Use BufferedReader to split each line on a delimiter
parseCommonsCSV | commons-csv     | Use CSVFormat to split each line on a delimiter
parseGenJavaCSV | generation-java | Use CsvReader to split each line on a delimiter
parseJavaCSV    | java-csv        | Use CsvReader to split each line on a delimiter
parseOpenCSV    | open-csv        | Use CSVReader to split each line on a delimiter
parseSkifeCSV   | skife-csv       | Use CSVReader to split each line on a delimiter
parseSuperCSV   | super-csv       | Use CsvListReader to split each line on a delimiter

Running the Tests
-------------

```shell
# Run all benchmark tests
mvn test -Pbenchmark

# Run a specific benchmark test
mvn test -Pbenchmark -Dbenchmark=<name>

# Example of running basic "read" benchmark
mvn test -Pbenchmark -Dbenchmark=read
```

Performance Test
-------------

Apache Commons CSV includes a stand-alone performance test which only covers commons-csv.

```shell
# Run the performance test
mvn test -Dtest=PerformanceTest
```

> :warning: This performance test does not use JMH; it uses simple timing metrics.

Performance Test Harness
-------------

CSV offers a secondary performance test harness located at: `org.apache.commons.csv.PerformanceTest`
