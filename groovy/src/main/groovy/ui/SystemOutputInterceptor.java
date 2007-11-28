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

package groovy.ui;

import groovy.lang.Closure;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Intercepts System.out. Implementation helper for Console.groovy.
 *
 * @version $Id$
 */
public class SystemOutputInterceptor extends FilterOutputStream {

    private Closure callback;

    /**
     * Constructor
     * 
     * @param callback
     *            accepts a string to be sent to std out and returns a Boolean.
     *            If the return value is true, output will be sent to
     *            System.out, otherwise it will not.
     */
    public SystemOutputInterceptor(final Closure callback) {
        super(System.out);
        
        assert callback != null;
        
        this.callback = callback;
    }

    /**
     * Starts intercepting System.out
     */
    public void start() {
        System.setOut(new PrintStream(this));
    }

    /**
     * Stops intercepting System.out, sending output to whereever it was
     * going when this interceptor was created.
     */
    public void stop() {
        System.setOut((PrintStream) out);
    }

    /**
     * Intercepts output - moret common case of byte[]
     */
    public void write(byte[] b, int off, int len) throws IOException {
        Boolean result = (Boolean) callback.call(new String(b, off, len));
        if (result.booleanValue()) {
            out.write(b, off, len);
        }
    }

    /**
     * Intercepts output - single characters
     */
    public void write(int b) throws IOException {
        Boolean result = (Boolean) callback.call(String.valueOf((char) b));
        if (result.booleanValue()) {
            out.write(b);
        }
    }
}
