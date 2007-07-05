/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.runtime;

import groovy.lang.Closure;

import java.util.regex.Matcher;

/**
 * This class defines all the new static groovy methods which appear on normal JDK
 * classes inside the Groovy environment. Static methods are used with the
 * first parameter as the destination class.
 *
 * @author Guillaume Laforge
 * @author Dierk Koenig
 * @version $Revision$
 */
public class DefaultGroovyStaticMethods {

    /**
     * Start a Thread with the given closure as a Runnable instance.
     *
     * @param closure the Runnable closure
     * @return the started thread
     */
    public static Thread start(Thread self, Closure closure) {
        Thread thread = new Thread(closure);
        thread.start();
        return thread;
    }

    /**
     * Start a daemon Thread with the given closure as a Runnable instance.
     *
     * @param closure the Runnable closure
     * @return the started thread
     */
    public static Thread startDaemon(Thread self, Closure closure) {
        Thread thread = new Thread(closure);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    /**
     * Get the last hidden matcher that system used to do a match.
     * 
     * @param matcher
     * @return the last regex matcher
     */
    public static Matcher getLastMatcher(Matcher matcher) {
        return RegexSupport.getLastMatcher();
    }

    /**
     * Sleep for so many milliseconds, even if interrupted.
     * @param object receiver
     * @param milliseconds the number of milliseconds to sleep
     */
    public static void sleep(Object object, long milliseconds){
        sleepImpl(object, milliseconds);
    }

    protected static void sleepImpl(Object object, long millis) {
        long start = System.currentTimeMillis();
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            long slept = System.currentTimeMillis() - start;
            long rest  = millis - slept;
            if (rest > 0) sleepImpl(object, rest);    // recursion to sleep the rest
        }
    }

    /**
     * Sleep for so many milliseconds
     * @param object receiver
     * @param milliseconds the number of milliseconds to sleep
     * @param onInterrupt interrupt handler, InterruptedException is passed to the Closure
     */
    public static void sleep(Object object, long milliseconds, Closure onInterrupt){
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            onInterrupt.call(e);
        }
    }
}
