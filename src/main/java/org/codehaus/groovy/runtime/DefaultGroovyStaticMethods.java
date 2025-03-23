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
package org.codehaus.groovy.runtime;

import groovy.lang.Closure;
import org.codehaus.groovy.reflection.ReflectionUtils;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * This class defines all the new static groovy methods which appear on normal
 * JDK classes inside the Groovy environment. Static methods are used with the
 * first parameter as the destination class.
 */
public class DefaultGroovyStaticMethods {

    /**
     * Start a Thread with the given closure as a Runnable instance.
     *
     * @param self    placeholder variable used by Groovy categories; ignored for default static methods
     * @param closure the Runnable closure
     * @return the started thread
     * @since 1.0
     */
    public static Thread start(Thread self, Closure closure) {
        return createThread(null, false, closure);
    }

    /**
     * Start a Thread with a given name and the given closure
     * as a Runnable instance.
     *
     * @param self    placeholder variable used by Groovy categories; ignored for default static methods
     * @param name    the name to give the thread
     * @param closure the Runnable closure
     * @return the started thread
     * @since 1.6
     */
    public static Thread start(Thread self, String name, Closure closure) {
        return createThread(name, false, closure);
    }

    /**
     * Start a daemon Thread with the given closure as a Runnable instance.
     *
     * @param self    placeholder variable used by Groovy categories; ignored for default static methods
     * @param closure the Runnable closure
     * @return the started thread
     * @since 1.0
     */
    public static Thread startDaemon(Thread self, Closure closure) {
        return createThread(null, true, closure);
    }

    /**
     * Start a daemon Thread with a given name and the given closure as
     * a Runnable instance.
     *
     * @param self    placeholder variable used by Groovy categories; ignored for default static methods
     * @param name    the name to give the thread
     * @param closure the Runnable closure
     * @return the started thread
     * @since 1.6
     */
    public static Thread startDaemon(Thread self, String name, Closure closure) {
        return createThread(name, true, closure);
    }

    private static Thread createThread(String name, boolean daemon, Closure closure) {
        Thread thread = name != null ? new Thread(closure, name) : new Thread(closure);
        if (daemon) thread.setDaemon(true);
        thread.start();
        return thread;
    }

    /**
     * Dump the thread dump of all threads
     *
     * @param self    placeholder variable used by Groovy categories; ignored for default static methods
     * @return the thread dump of all threads
     * @since 3.0.0
     */
    public static String dumpAll(Thread self){
        ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
        return Arrays.stream(threadMxBean.dumpAllThreads(true, true))
                .map(ThreadInfo::toString)
                .collect(Collectors.joining(""));
    }

    /**
     * Get all threads
     *
     * @param self placeholder variable used by Groovy categories; ignored for default static methods
     * @return all threads
     * @since 4.0.16
     */
    public static List<Thread> allThreads(Thread self) {
        ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
        for (ThreadGroup parentGroup; (parentGroup = rootGroup.getParent()) != null; ) {
            rootGroup = parentGroup;
        }

        Thread[] threads = new Thread[rootGroup.activeCount()];
        while (rootGroup.enumerate(threads, true) == threads.length) {
            threads = new Thread[threads.length * 2];
        }

        return Collections.unmodifiableList(
                                Arrays.stream(threads)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList()));
    }

    /**
     * Get the last hidden matcher that the system used to do a match.
     *
     * @param self placeholder variable used by Groovy categories; ignored for default static methods
     * @return the last regex matcher
     * @since 1.0
     */
    public static Matcher getLastMatcher(Matcher self) {
        return RegexSupport.getLastMatcher();
    }

    /**
     * This method is used by both sleep() methods to implement sleeping
     * for the given time even if interrupted
     *
     * @param millis  the number of milliseconds to sleep
     * @param closure optional closure called when interrupted
     *                as long as the closure returns false the sleep continues
     */
    private static void sleepImpl(long millis, Closure closure) {
        long start = System.currentTimeMillis();
        long rest = millis;
        long current;
        boolean interrupted = false;
        try {
            while (rest > 0) {
                try {
                    Thread.sleep(rest);
                    rest = 0;
                } catch (InterruptedException e) {
                    interrupted = true;
                    if (closure != null) {
                        if (DefaultTypeTransformation.castToBoolean(closure.call(e))) {
                            return;
                        }
                    }
                    current = System.currentTimeMillis(); // compensate for closure's time
                    rest = millis + start - current;
                }
            }
        } finally {
            if (interrupted) Thread.currentThread().interrupt();
        }
    }

    /**
     * Sleep for so many milliseconds, even if interrupted.
     *
     * @param self         placeholder variable used by Groovy categories; ignored for default static methods
     * @param milliseconds the number of milliseconds to sleep
     * @since 1.0
     */
    public static void sleep(Object self, long milliseconds) {
        sleepImpl(milliseconds, null);
    }

    /**
     * Sleep for so many milliseconds, using a given closure for interrupt processing.
     *
     * @param self         placeholder variable used by Groovy categories; ignored for default static methods
     * @param milliseconds the number of milliseconds to sleep
     * @param onInterrupt  interrupt handler, InterruptedException is passed to the Closure
     *                     as long as it returns false, the sleep continues
     * @since 1.0
     */
    public static void sleep(Object self, long milliseconds, Closure onInterrupt) {
        sleepImpl(milliseconds, onInterrupt);
    }

    /**
     * Works exactly like ResourceBundle.getBundle(String).  This is needed
     * because the java method depends on a particular stack configuration that
     * is not guaranteed in Groovy when calling the Java method.
     *
     * @param self       placeholder variable used by Groovy categories; ignored for default static methods
     * @param bundleName the name of the bundle.
     * @return the resource bundle
     * @see java.util.ResourceBundle#getBundle(java.lang.String)
     * @since 1.6.0
     */
    public static ResourceBundle getBundle(ResourceBundle self, String bundleName) {
        return getBundle(self, bundleName, Locale.getDefault());
    }

    /**
     * Works exactly like ResourceBundle.getBundle(String, Locale).  This is needed
     * because the java method depends on a particular stack configuration that
     * is not guaranteed in Groovy when calling the Java method.
     *
     * @param self       placeholder variable used by Groovy categories; ignored for default static methods
     * @param bundleName the name of the bundle.
     * @param locale     the specific locale
     * @return the resource bundle
     * @see java.util.ResourceBundle#getBundle(java.lang.String, java.util.Locale)
     * @since 1.6.0
     */
    public static ResourceBundle getBundle(ResourceBundle self, String bundleName, Locale locale) {
        Class c = ReflectionUtils.getCallingClass();
        ClassLoader targetCL = c != null ? c.getClassLoader() : null;
        if (targetCL == null) targetCL = ClassLoader.getSystemClassLoader();
        return ResourceBundle.getBundle(bundleName, locale, targetCL);
    }

    public static File createTempDir(File self) throws IOException {
        return createTempDir(self, "groovy-generated-", "tmpdir-");
    }

    public static File createTempDir(File self, final String prefix) throws IOException {
        return createTempDirNio(prefix);
    }

    public static File createTempDir(File self, final String prefix, final String suffix) throws IOException {
        // more secure Files api doesn't support suffix, so just append it to the prefix
        return createTempDirNio(prefix + suffix);
    }

    private static File createTempDirNio(String prefix) throws IOException {
        Path tempPath = Files.createTempDirectory(prefix);
        return tempPath.toFile();
    }

    /**
     * Get the current time in seconds
     *
     * @param self   placeholder variable used by Groovy categories; ignored for default static methods
     * @return  the difference, measured in seconds, between
     *          the current time and midnight, January 1, 1970 UTC.
     * @see     System#currentTimeMillis()
     */
    public static long currentTimeSeconds(System self) {
        return System.currentTimeMillis() / 1000;
    }
}
