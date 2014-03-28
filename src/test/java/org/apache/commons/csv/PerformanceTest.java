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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;

/**
 * Basic test harness.
 *
 * Requires test file to be downloaded separately.
 *
 * @version $Id$
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

    private static final File BIG_FILE = new File(System.getProperty("java.io.tmpdir"), "worldcitiespop.txt");

    public static void main(final String [] args) throws Exception {
        if (BIG_FILE.exists()) {
            System.out.println(String.format("Found test fixture %s: %,d bytes.", BIG_FILE, BIG_FILE.length()));
        } else {
            System.out.println("Decompressing test fixture " + BIG_FILE + "...");
            final InputStream input = new GZIPInputStream(new FileInputStream("src/test/resources/perf/worldcitiespop.txt.gz"));
            final OutputStream output = new FileOutputStream(BIG_FILE);
            IOUtils.copy(input, output);
            input.close();
            output.close();
            System.out.println(String.format("Decompressed test fixture %s: %,d bytes.", BIG_FILE, BIG_FILE.length()));
        }
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
        for(final String p : PROPS) {
            System.out.println(p+"="+System.getProperty(p));
        }
        System.out.println("Max count: "+max+"\n");

        for(final String test : tests) {
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
        return new BufferedReader(new FileReader(BIG_FILE));
    }

    // Container for basic statistics
    private static class Stats {
        final int count;
        final int fields;
        Stats(final int c, final int f) {
            count=c;
            fields=f;
        }
    }

    // Display end stats; store elapsed for average
    private static void show(final String msg, final Stats s, final long start) {
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
            System.out.printf("%-20s: %5dms%n%n", "Average(not first)", tot/(num-1));
        }
        num=0; // ready for next set
    }

    private static void testReadBigFile(final boolean split) throws Exception {
       for (int i = 0; i < max; i++) {
           final BufferedReader in = getReader();
           final long t0 = System.currentTimeMillis();
           final Stats s = readAll(in, split);
           in.close();
           show(split?"file+split":"file", s, t0);
       }
       show();
   }

   private static Stats readAll(final BufferedReader in, final boolean split) throws IOException {
       int count = 0;
       int fields = 0;
       String record;
       while ((record=in.readLine()) != null) {
           count++;
           fields+= split ? record.split(",").length : 1;
       }
       return new Stats(count, fields);
   }

   private static void testExtendedBuffer(final boolean makeString) throws Exception {
       for (int i = 0; i < max; i++) {
           final ExtendedBufferedReader in = new ExtendedBufferedReader(getReader());
           final long t0 = System.currentTimeMillis();
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
           final CSVParser parser = new CSVParser(reader, format);
           final long t0 = System.currentTimeMillis();
           final Stats s = iterate(parser);
           reader.close();
           show("CSV", s, t0);
           parser.close();
       }
       show();
   }


   private static Constructor<Lexer> getLexerCtor(final String clazz) throws Exception {
       @SuppressWarnings("unchecked")
       final Class<Lexer> lexer = (Class<Lexer>) Class.forName("org.apache.commons.csv." + clazz);
       return lexer.getConstructor(new Class<?>[]{CSVFormat.class, ExtendedBufferedReader.class});
   }

   private static void testCSVLexer(final boolean newToken, final String test) throws Exception {
       Token token = new Token();
       String dynamic = "";
       for (int i = 0; i < max; i++) {
           final ExtendedBufferedReader input = new ExtendedBufferedReader(getReader());
           Lexer lexer = null;
           if (test.startsWith("CSVLexer")) {
               dynamic="!";
               lexer = getLexerCtor(test).newInstance(new Object[]{format, input});
           } else {
               lexer = new Lexer(format, input);
           }
           int count = 0;
           int fields = 0;
           final long t0 = System.currentTimeMillis();
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
                   throw new IOException("invalid parse sequence <"+token.content.toString()+">");
               case TOKEN:
                   fields++;
                   break;
                case COMMENT: // not really expecting these
                    break;
                default:
                    throw new IllegalStateException("Unexpected Token type: " + token.type);
              }

           } while (!token.type.equals(Token.Type.EOF));
           final Stats s = new Stats(count, fields);
           input.close();
           show(lexer.getClass().getSimpleName()+dynamic+" "+(newToken ? "new" : "reset"), s, t0);
       }
       show();
   }

   private static Stats iterate(final Iterable<CSVRecord> it) {
       int count = 0;
       int fields = 0;
       for (final CSVRecord record : it) {
           count++;
           fields+=record.size();
       }
       return new Stats(count, fields);
   }

}