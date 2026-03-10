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
package org.codehaus.groovy.benchmarks.vm5.b2394;

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilationFailedException;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class Main {

    public static void main(final String[] args) {
        String name = (args == null || args.length == 0) ? "script300.groovy" : args [0];
        int numIter = (args == null || args.length < 2) ? 100000 : Integer.parseInt(args[1]);
        if (args == null || args.length != 3) {
            for (int i = 1; i <= 50; ) {
                new Main(name, (numIter)/i, i);
                new Main(name, (numIter)/i, i);
                new Main(name, (numIter)/i, i);
                new Main(name, (numIter)/i, i);
                if (i < 10) {
                    i += 1;
                } else {
                    if (i < 20) {
                        i += 2;
                    } else {
                        i += 5;
                    }
                }
            }
        } else {
            new Main(args[0], numIter, Integer.parseInt(args[2]));
            new Main(args[0], numIter, Integer.parseInt(args[2]));
            new Main(args[0], numIter, Integer.parseInt(args[2]));
            new Main(args[0], numIter, Integer.parseInt(args[2]));
        }
    }

    private Main(final String scriptName, final int numIter, final int numThreads) {
        System.out.println("Running " + scriptName);
        CountDownLatch latch = new CountDownLatch(numThreads);
        Class<?> scriptClass = loadScript(scriptName);

        long t0 = System.currentTimeMillis();

        // launch threads, each one instantiating the scriptClass and running
        // it numIter times
        var arr = new ScriptLauncher[numThreads];
        for (int i = 0; i < numThreads; i += 1) {
            arr [i] = new ScriptLauncher(scriptClass, numIter, latch);
        }

        for (int i = 0; i < numThreads; i += 1) {
            arr [i].start();
        }

        // wait for the threads to finish
        try {
            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }

        double duration = (System.currentTimeMillis() - t0) / 1000.0;
        double numberOfOperations = numIter * numThreads;

        System.out.println("Test completed: " + numberOfOperations + " scriptExecutions in " + duration + " seconds");
        System.out.println("\t\t\t" + (numberOfOperations / duration) + " scriptExecutions/second with\t" + numThreads + " threads");
    }

    private Class<?> loadScript(String name) {
        name = "src/test/groovy/" + getClass().getPackage().getName().replace(".", "/") + "/" + name;

        Class<?> scriptClass = null;

        try (var gcl = new GroovyClassLoader(this.getClass().getClassLoader())) {
            scriptClass = gcl.parseClass(new File(name));
        } catch (CompilationFailedException e) {
            throw new RuntimeException("Script compilation failed: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException("Script file not found: " + name);
        }

        return scriptClass;
    }
}
