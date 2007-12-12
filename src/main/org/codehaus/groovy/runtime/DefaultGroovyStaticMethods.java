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

import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

/**
 * This class defines all the new static groovy methods which appear on normal JDK
 * classes inside the Groovy environment. Static methods are used with the
 * first parameter as the destination class.
 *
 * @author Guillaume Laforge
 * @author Dierk Koenig
 * @author Joachim Baumann
 * @version $Revision$
 */
public class DefaultGroovyStaticMethods {

    /**
     * Start a Thread with the given closure as a Runnable instance.
     *
     * @param self the thread on which the method is called
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
     * @param self the thread on which the method is called
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
     * This method is used by both sleep() methods to imlement sleeping
     * for the given time even if interrupted
     * @param object receiver
     * @param millis the number of milliseconds to sleep
     * @param closure optional closure called when interrupted
     *                as long as the closure returns false the sleep continues
     */
    protected static void sleepImpl(Object object, long millis, Closure closure) {
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
     * @param object receiver
     * @param milliseconds the number of milliseconds to sleep
     */
    public static void sleep(Object object, long milliseconds) {
	sleepImpl(object, milliseconds, null);
    }

    /**
     * Sleep for so many milliseconds
     * 
     * @param object receiver
     * @param milliseconds the number of milliseconds to sleep
     * @param onInterrupt interrupt handler, InterruptedException is passed to the Closure
     *                    as long as it returns false, the sleep continues
     */
    public static void sleep(Object object, long milliseconds, Closure onInterrupt){
	sleepImpl(object, milliseconds, onInterrupt);
    }
}
