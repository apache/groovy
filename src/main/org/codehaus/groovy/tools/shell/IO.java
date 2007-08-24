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

package org.codehaus.groovy.tools.shell;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.codehaus.groovy.tools.shell.util.ANSI.RenderWriter;

/**
 * Container for input/output handles.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class IO
{
    /** Raw input stream. */
    public final InputStream inputStream;

    /** Raw output stream. */
    public final OutputStream outputStream;

    /** Raw error output stream. */
    public final OutputStream errorStream;

    /** Prefered input reader. */
    public final Reader in;

    /** Prefered output writer. */
    public final PrintWriter out;

    /** Prefered error output writer. */
    public final PrintWriter err;

    /** Flag to indicate that verbose output is expected. */
    public boolean verbose;

    /** Flag to indicate that quiet output is expected. */
    public boolean quiet;
    
    /**
     * Construct a new IO container.
     */
    public IO(final InputStream inputStream, final OutputStream outputStream, final OutputStream errorStream) {
        assert inputStream != null;
        assert outputStream != null;
        assert errorStream != null;

        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.errorStream = errorStream;
        
        this.in = new InputStreamReader(inputStream);

        //
        // TODO: Once all user output is in i18n, then it would be more efficent to have the MessageSource
        //       be ANSI-aware instead of this...
        //
        
        this.out = new RenderWriter(outputStream, true);
        this.err = new RenderWriter(errorStream, true);
    }

    /**
     * Construct a new IO container using system streams.
     */
    public IO() {
        this(System.in, System.out, System.err);
    }

    /**
     * Flush both output streams.
     */
    public void flush() throws IOException {
        out.flush();
        err.flush();
    }

    /**
     * Close all streams.
     */
    public void close() throws IOException {
        in.close();
        out.close();
        err.close();
    }
}
