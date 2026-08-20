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

import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * GROOVY-12281 acceptance spike (manual, not CI): does a dropped Groovy
 * runtime's class loader become collectable under each global ClassValue mode?
 * This reproduces the container topology from GROOVY-12142: a Groovy copy is
 * loaded in a child loader, runs a script whose dynamic dispatch creates
 * ClassValue associations on immortal platform classes (String, Integer,
 * ArrayList, ...), and is then dropped.
 *
 * <pre>
 *   java -Xmx256m Groovy12281LoaderSpike.java build/libs/groovy-6.0.0-SNAPSHOT.jar true   # expect PINNED
 *   java -Xmx256m Groovy12281LoaderSpike.java build/libs/groovy-6.0.0-SNAPSHOT.jar soft   # expect UNPINNED
 *   java -Xmx256m Groovy12281LoaderSpike.java build/libs/groovy-6.0.0-SNAPSHOT.jar false  # expect UNPINNED (map control)
 * </pre>
 *
 * Soft references are only guaranteed cleared before OOME, so the spike
 * applies allocation pressure to its own heap after dropping the loader;
 * "collectedBeforePressure" records whether plain GCs sufficed.
 */
public final class Groovy12281LoaderSpike {

    private Groovy12281LoaderSpike() {
    }

    public static void main(String[] args) throws Exception {
        Path jar = Path.of(args[0]).toAbsolutePath();
        String mode = args.length > 1 ? args[1] : "true";
        // Set before any child class initializes; the child copy's
        // GroovyClassValueFactory reads it during class initialization.
        System.setProperty("groovy.use.classvalue", mode);

        WeakReference<ClassLoader> loaderRef = loadRunAndDrop(jar);

        gc(10);
        boolean collectedBeforePressure = loaderRef.get() == null;

        applySoftClearingPressure();
        gc(20);
        boolean collected = loaderRef.get() == null;

        System.out.println("mode=" + mode
                + " collectedBeforePressure=" + collectedBeforePressure
                + " collectedAfterPressure=" + collected);
        System.out.println(collected ? "UNPINNED" : "PINNED");
    }

    private static WeakReference<ClassLoader> loadRunAndDrop(Path jar) throws Exception {
        URLClassLoader child = new URLClassLoader("groovy-under-test",
                new URL[]{jar.toUri().toURL()}, ClassLoader.getPlatformClassLoader());
        Class<?> shellClass = Class.forName("groovy.lang.GroovyShell", true, child);
        Object shell = shellClass.getConstructor().newInstance();
        Object result = shellClass.getMethod("evaluate", String.class).invoke(shell,
                // platform-receiver-heavy dispatch: String, Integer, Range, ArrayList, GString
                "def s = 'abc'.reverse()\n"
                + "def total = (1..5).collect { it * 2 }.sum()\n"
                + "def m = [a: 1, b: 2]\n"
                + "\"${s}:${total}:${m.a + m.b}\".toString()");
        if (!"cba:30:3".equals(result)) {
            throw new IllegalStateException("unexpected script result: " + result);
        }
        System.out.println("script result: " + result + " (child Groovy active)");
        child.close();
        return new WeakReference<>(child);
    }

    private static void gc(int rounds) throws InterruptedException {
        for (int i = 0; i < rounds; i++) {
            System.gc();
            Thread.sleep(50);
        }
    }

    /**
     * Allocates until OutOfMemoryError, forcing the collector to clear all
     * soft references first (JLS guarantee), then releases everything.
     */
    private static void applySoftClearingPressure() {
        List<byte[]> hog = new ArrayList<>();
        try {
            while (true) {
                hog.add(new byte[1 << 20]);
            }
        } catch (OutOfMemoryError expected) {
            hog.clear();
        }
        System.out.println("soft-clearing pressure applied");
    }
}
