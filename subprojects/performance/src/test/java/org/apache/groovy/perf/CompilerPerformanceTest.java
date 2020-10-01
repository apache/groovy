/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.groovy.perf;

import groovy.lang.GroovySystem;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompilerPerformanceTest {
    private final static String GROOVY_VERSION = GroovySystem.getVersion();
    private final static int WARMUP = 200;
    private final static int REPEAT = 1000;

    public static void main(String[] args) throws Exception {
        List<File> sources = new ArrayList<>();
        List<URL> classpath = new ArrayList<>();
        boolean isCp = false;
        List<String> argsLeft = new ArrayList<>();
        Collections.addAll(argsLeft, args);
        String outputFile = argsLeft.remove(0);
        for (String arg : argsLeft) {
            if ("-cp".equals(arg)) {
                isCp = true;
            } else if (isCp) {
                for (String s : arg.split(":")) {
                    classpath.add(new File(s).toURI().toURL());
                }
                isCp = false;
            } else {
                sources.add(new File(arg));
            }
        }
        ScriptCompilationExecuter executer = new ScriptCompilationExecuter(
                sources.toArray(new File[sources.size()]),
                classpath
        );
        System.out.println("Using Groovy " + GROOVY_VERSION);

        DescriptiveStatistics stats = new DescriptiveStatistics();

        for (int i=0;i<WARMUP+REPEAT;i++) {
            if (i%10 == 0) {
                if (i < WARMUP) {
                    System.out.println("Warmup #" + (i + 1));
                } else {
                    System.out.println("Round #" + (i - WARMUP));
                }
            }
            long dur = executer.execute();
            System.gc();
            System.out.printf("Compile time = %dms%n", dur);
            if (i>=WARMUP) {
                stats.addValue((double) dur);
            }
        }

        System.out.println("Compilation took " + stats.getMean() + "ms ± " + stats.getStandardDeviation() + "ms");
        FileWriter wrt = new FileWriter(new File(outputFile), false);
        wrt.append(String.format("%s;%s;%s\n", GROOVY_VERSION, stats.getMean(), stats.getStandardDeviation()));
        wrt.close();
    }
}
