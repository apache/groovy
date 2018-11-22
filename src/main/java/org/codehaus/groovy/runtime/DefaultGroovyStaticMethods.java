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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.stream.Collector;
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
        while (rest > 0) {
            try {
                Thread.sleep(rest);
                rest = 0;
            } catch (InterruptedException e) {
                if (closure != null) {
                    if (DefaultTypeTransformation.castToBoolean(closure.call(e))) {
                        return;
                    }
                }
                current = System.currentTimeMillis(); // compensate for closure's time
                rest = millis + start - current;
            }
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

    @Deprecated
    public static Date parse(Date self, String format, String input) throws ParseException {
        return new SimpleDateFormat(format).parse(input);
    }

    @Deprecated
    public static Date parse(Date self, String format, String input, TimeZone zone) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(zone);
        return sdf.parse(input);
    }

    @Deprecated
    public static Date parseToStringDate(Date self, String dateToString) throws ParseException {
        return new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US).parse(dateToString);
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
        return createTempDir(self, "groovy-generated-", "-tmpdir");
    }

    public static File createTempDir(File self, final String prefix, final String suffix) throws IOException {
        final int MAXTRIES = 3;
        int accessDeniedCounter = 0;
        File tempFile=null;
        for (int i=0; i<MAXTRIES; i++) {
            try {
                tempFile = File.createTempFile(prefix, suffix);
                tempFile.delete();
                tempFile.mkdirs();
                break;
            } catch (IOException ioe) {
                if (ioe.getMessage().startsWith("Access is denied")) {
                    accessDeniedCounter++;
                    try { Thread.sleep(100); } catch (InterruptedException e) {}
                }
                if (i==MAXTRIES-1) {
                    if (accessDeniedCounter==MAXTRIES) {
                        String msg =
                                "Access is denied.\nWe tried " +
                                        + accessDeniedCounter+
                                        " times to create a temporary directory"+
                                        " and failed each time. If you are on Windows"+
                                        " you are possibly victim to"+
                                        " http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6325169. "+
                                        " this is no bug in Groovy.";
                        throw new IOException(msg);
                    } else {
                        throw ioe;
                    }
                }
            }
        }
        return tempFile;
    }

    /**
     * Get the current time in seconds
     *
     * @param self   placeholder variable used by Groovy categories; ignored for default static methods
     * @return  the difference, measured in seconds, between
     *          the current time and midnight, January 1, 1970 UTC.
     * @see     System#currentTimeMillis()
     */
    public static long currentTimeSeconds(System self){
    return System.currentTimeMillis() / 1000;
  }

    /**
     * Returns a {@link Collector} that gets the first element.
     *
     * @return a {@link Collector} which implements the first operation
     * @since 3.0.0
     */
    public static <T> Collector<T, ?, Optional<T>> first(Collectors self) {
        return Collectors.reducing((v1, v2) -> v1);
    }

    /**
     * Returns a {@link Collector} that gets the last element.
     *
     * @return a {@link Collector} which implements the last operation
     * @since 3.0.0
     */
    public static <T> Collector<T, ?, Optional<T>> last(Collectors self) {
        return Collectors.reducing((v1, v2) -> v2);
    }

}
