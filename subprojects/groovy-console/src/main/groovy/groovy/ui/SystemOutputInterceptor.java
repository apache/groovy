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
package groovy.ui;

import groovy.lang.Closure;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Intercepts System.out/System.err. Implementation helper for Console.groovy.
 */
@Deprecated
public class SystemOutputInterceptor extends FilterOutputStream {

    private Closure callback;
    private boolean output;

    private static final ThreadLocal<Integer> consoleId = new InheritableThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return 0;
        }
    };

    /**
     * Constructor
     *
     * @param callback accepts the id of the target Console instance and a
     *                 string to be sent to std out and returns a Boolean.
     *                 If the return value is true, output will be sent to
     *                 System.out, otherwise it will not.
     */
    public SystemOutputInterceptor(final Closure callback) {
        this(callback, true);
    }

    /**
     * Constructor
     *
     * @param callback accepts the id of the target Console instance and a
     *                 string to be sent to std out and returns a Boolean.
     *                 If the return value is true, output will be sent to
     *                 System.out/System.err, otherwise it will not.
     * @param output   flag that tells whether System.out needs capturing ot System.err
     */
    public SystemOutputInterceptor(final Closure callback, boolean output) {
        super(output ? System.out : System.err);

        assert callback != null;

        this.callback = callback;
        this.output = output;
    }

    /**
     * Starts intercepting System.out/System.err
     */
    public void start() {
        if (output) {
            System.setOut(new PrintStream(this));
        } else {
            System.setErr(new PrintStream(this));
        }
    }

    /**
     * Stops intercepting System.out/System.err, sending output to wherever it was
     * going when this interceptor was created.
     */
    public void stop() {
        if (output) {
            System.setOut((PrintStream) out);
        } else {
            System.setErr((PrintStream) out);
        }
    }

    /**
     * Intercepts output - more common case of byte[]
     */
    public void write(byte[] b, int off, int len) throws IOException {
        Boolean result = (Boolean) callback.call(consoleId.get(), new String(b, off, len));
        if (result) {
            out.write(b, off, len);
        }
    }

    /**
     * Intercepts output - single characters
     */
    public void write(int b) throws IOException {
        Boolean result = (Boolean) callback.call(consoleId.get(), String.valueOf((char) b));
        if (result) {
            out.write(b);
        }
    }

    /**
     * Threads executing a script should call this method at the start of execution
     * in order to set the id of the console that is hosting the thread of execution.  This
     * should be called prior to any output that is generated.  The consoleId will
     * be passed to the callback.
     *
     * @param consoleId id of the Console instance executing the script
     */
    public void setConsoleId(int consoleId) {
        this.consoleId.set(consoleId);
    }

    /**
     * Threads executing a script should call this method after
     * execution completes in order to unregister the consoleId.
     */
    public void removeConsoleId() {
        this.consoleId.remove();
    }
}
