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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;

/**
 * Basic test harness.
 *
 * Requires test file to be downloaded separately.
 *
 */
@SuppressWarnings("boxing")
public class PerformanceTest {

    private static final String[] PROPS = {
        "java.version",                  // Java Runtime Environment version
        "java.vendor",                   // Java Runtime Environment vendor
//        "java.vm.specification.version", // Java Virtual Machine specification version
//        "java.vm.specification.vendor",  // Java Virtual Machine specification vendor
//        "java.vm.specification.name",    // Java Virtual Machine specification name
        "java.vm.version",               // Java Virtual Machine implementation version
//        "java.vm.vendor",                // Java Virtual Machine implementation vendor
        "java.vm.name",                  // Java Virtual Machine implementation name
//        "java.specification.version",    // Java Runtime Environment specification version
//        "java.specification.vendor",     // Java Runtime Environment specification vendor
//        "java.specification.name",       // Java Runtime Environment specification name

        "os.name",                       // Operating system name
        "os.arch",                       // Operating system architecture
        "os.version",                    // Operating system version

    };

    private static int max = 10;

    private static int num = 0; // number of elapsed times recorded
    private static long[] elapsedTimes = new long[max];

    private static final CSVFormat format = CSVFormat.EXCEL;

    public static void main(String [] args) throws Exception {
        final int argc = args.length;
        String tests[];
        if (argc > 0) {
            max=Integer.parseInt(args[0]);
        }
        if (argc > 1) {
            tests = new String[argc-1];
            for (int i = 1; i < argc; i++) {
                tests[i-1]=args[i];
            }
        } else {
            tests=new String[]{"file", "split", "extb", "exts", "csv", "lexreset", "lexnew"};
        }
        for(String p : PROPS) {
            System.out.println(p+"="+System.getProperty(p));
        }
        System.out.println("Max count: "+max+"\n");

        for(String test : tests) {
            if ("file".equals(test)) {
                testReadBigFile(false);
            } else if ("split".equals(test)) {
                testReadBigFile(true);
            } else if ("csv".equals(test)) {
                testParseCommonsCSV();
            } else if ("lexreset".equals(test)) {
                testCSVLexer(false, test);
            } else if ("lexnew".equals(test)) {
                testCSVLexer(true, test);
            } else if (test.startsWith("CSVLexer")) {
                testCSVLexer(false, test);
            } else if ("extb".equals(test)) {
                testExtendedBuffer(false);
            } else if ("exts".equals(test)) {
                testExtendedBuffer(true);
            } else {
                System.out.println("Invalid test name: "+test);
            }
        }
    }

    private static BufferedReader getReader() throws IOException {
        return new BufferedReader(new FileReader("worldcitiespop.txt"));
    }

    // Container for basic statistics
    private static class Stats {
        final int count;
        final int fields;
        Stats(int c, int f) {
            count=c;
            fields=f;
        }
    }

    // Display end stats; store elapsed for average
    private static void show(String msg, Stats s, long start) {
        final long elapsed = System.currentTimeMillis() - start;
        System.out.printf("%-20s: %5dms " + s.count + " lines "+ s.fields + " fields%n",msg,elapsed);
        elapsedTimes[num++]=elapsed;
    }

    // calculate and show average
    private static void show(){
        long tot = 0;
        if (num > 1) {
            for(int i=1; i < num; i++) { // skip first test
                tot += elapsedTimes[i];
            }
            System.out.printf("%-20s: %5dms%n%n", "Average(not first)", (tot/(num-1)));
        }
        num=0; // ready for next set
    }

    private static void testReadBigFile(boolean split) throws Exception {
       for (int i = 0; i < max; i++) {
           BufferedReader in = getReader();
           long t0 = System.currentTimeMillis();
           Stats s = readAll(in, split);
           in.close();
           show(split?"file+split":"file", s, t0);
       }
       show();
   }

   private static Stats readAll(BufferedReader in, boolean split) throws IOException {
       int count = 0;
       int fields = 0;
       String record;
       while ((record=in.readLine()) != null) {
           count++;
           fields+= split ? record.split(",").length : 1;
       }
       return new Stats(count, fields);
   }

   private static void testExtendedBuffer(boolean makeString) throws Exception {
       for (int i = 0; i < max; i++) {
           ExtendedBufferedReader in = new ExtendedBufferedReader(getReader());
           long t0 = System.currentTimeMillis();
           int read;
           int fields = 0;
           int lines = 0;
           if (makeString) {
               StringBuilder sb = new StringBuilder();
               while((read=in.read()) != -1) {
                   sb.append((char)read);
                   if (read == ',') { // count delimiters
                       sb.toString();
                       sb = new StringBuilder();
                       fields++;
                   } else if (read == '\n') {
                       sb.toString();
                       sb = new StringBuilder();
                       lines++;
                   }
               }
           } else {
               while((read=in.read()) != -1) {
                   if (read == ',') { // count delimiters
                       fields++;
                   } else if (read == '\n') {
                       lines++;
                   }
               }
           }
           fields += lines; // EOL is a delimiter too
           in.close();
           show("Extended"+(makeString?" toString":""), new Stats(lines, fields), t0);
       }
       show();
   }

   private static void testParseCommonsCSV() throws Exception {
       for (int i = 0; i < max; i++) {
           final BufferedReader reader = getReader();
           CSVParser parser = new CSVParser(reader, format);
           long t0 = System.currentTimeMillis();
           Stats s = iterate(parser);
           reader.close();
           show("CSV", s, t0);
       }
       show();
   }


   private static Constructor<Lexer> getLexerCtor(String clazz) throws Exception {
       @SuppressWarnings("unchecked")
       Class<Lexer> lexer = (Class<Lexer>) Class.forName("org.apache.commons.csv."+clazz);
       Constructor<Lexer> ctor = lexer.getConstructor(new Class<?>[]{CSVFormat.class, ExtendedBufferedReader.class});
       return ctor;
   }

   private static void testCSVLexer(final boolean newToken, String test) throws Exception {
       Token token = new Token();
       String dynamic = "";
       for (int i = 0; i < max; i++) {
           final ExtendedBufferedReader input = new ExtendedBufferedReader(getReader());
           Lexer lexer = null;
           if (test.startsWith("CSVLexer")) {
               dynamic="!";
               lexer = getLexerCtor(test).newInstance(new Object[]{format, input});
           } else {
               lexer = new CSVLexer(format, input);
           }
           int count = 0;
           int fields = 0;
           long t0 = System.currentTimeMillis();
           do {
               if (newToken) {
                   token = new Token();
               } else {
                   token.reset();
               }
               lexer.nextToken(token);
               switch(token.type) {
               case EOF:
                   break;
               case EORECORD:
                   fields++;
                   count++;
                   break;
               case INVALID:
                   throw new IOException("invalid parse sequence");
               case TOKEN:
                   fields++;
                   break;
              }

           } while (!token.type.equals(Token.Type.EOF));
           Stats s = new Stats(count, fields);
           input.close();
           show(lexer.getClass().getSimpleName()+dynamic+" "+(newToken ? "new" : "reset"), s, t0);
       }
       show();
   }

   private static Stats iterate(Iterable<CSVRecord> it) {
       int count = 0;
       int fields = 0;
       for (CSVRecord record : it) {
           count++;
           fields+=record.size();
       }
       return new Stats(count, fields);
   }

}