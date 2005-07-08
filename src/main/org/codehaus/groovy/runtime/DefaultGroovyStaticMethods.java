/*
 * $Id$
 *
 * Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.
 *
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met:
 *  1. Redistributions of source code must retain copyright statements and
 * notices. Redistributions must also contain a copy of this document.
 *  2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  3. The name "groovy" must not be used to endorse or promote products
 * derived from this Software without prior written permission of The Codehaus.
 * For written permission, please contact info@codehaus.org.
 *  4. Products derived from this Software may not be called "groovy" nor may
 * "groovy" appear in their names without prior written permission of The
 * Codehaus. "groovy" is a registered trademark of The Codehaus.
 *  5. Due credit should be given to The Codehaus - http://groovy.codehaus.org/
 *
 * THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *
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
     * Sleep for so many seconds, even if interrupted.
     * @param object receiver
     * @param seconds the number of seconds to sleep
     */
    public static void sleep(Object object, long seconds){
        long millis = seconds * 1000;
        sleepImpl(object, millis);
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
     * Sleep for so many seconds
     * @param object receiver
     * @param seconds the number of seconds to sleep
     * @param onInterrupt interrupt handler, InterruptedException is passed to the Closure
     */
    public static void sleep(Object object, long seconds, Closure onInterrupt){
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            onInterrupt.call(e);
        }
    }
}
