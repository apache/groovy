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

package org.codehaus.groovy.tools.shell

/**
 * Container for input/output handles.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class IO
{
    /** Raw input stream. */
    final InputStream inputStream

    /** Raw output stream. */
    final OutputStream outputStream

    /** Raw error output stream. */
    final OutputStream errorStream

    /** Prefered input reader. */
    final Reader input

    /** Prefered output writer. */
    final PrintWriter output

    /** Prefered error output writer. */
    final PrintWriter error

    /** Flag to indicate that verbose output is expected. */
    boolean verbose

    /** Flag to indicate that quiet output is expected. */
    boolean quiet
    
    /**
     * Construct a new IO container.
     */
    IO(final InputStream inputStream, final OutputStream outputStream, final OutputStream errorStream) {
        assert inputStream
        assert outputStream
        assert errorStream

        this.inputStream = inputStream
        this.outputStream = outputStream
        this.errorStream = errorStream

        this.input = new InputStreamReader(inputStream)
        this.output = new PrintWriter(outputStream, true)
        this.error = new PrintWriter(errorStream, true)
    }

    /**
     * Construct a new IO container using system streams.
     */
    IO() {
        this(System.in, System.out, System.err)
    }

    /**
     * Flush both output streams.
     */
    void flush() {
        output.flush()
        error.flush()
    }

    /**
     * Close all streams.
     */
    void close() {
        input.close()
        output.close()
        error.close()
    }
}
