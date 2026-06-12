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
package groovy.io;

import org.codehaus.groovy.runtime.FormatHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * A PrintWriter that outputs objects in Groovy style.
 * That means print(Object) uses InvokerHelper.toString(Object)
 * to produce the same results as Writer.print(Object).
 *
 * @since 1.6
 */
public class GroovyPrintWriter extends PrintWriter {
    /**
     * Creates a writer for the supplied file.
     *
     * @param file the target file
     * @throws FileNotFoundException if the file cannot be opened
     */
    public GroovyPrintWriter(File file) throws FileNotFoundException {
        super(file);
    }

    /**
     * Creates a writer for the supplied file using the given charset name.
     *
     * @param file the target file
     * @param csn the charset name
     * @throws FileNotFoundException if the file cannot be opened
     * @throws UnsupportedEncodingException if the charset is unsupported
     */
    public GroovyPrintWriter(File file, String csn)
            throws FileNotFoundException, UnsupportedEncodingException {
        super(file, csn);
    }

    /**
     * Creates a writer that delegates to the supplied writer.
     *
     * @param out the underlying writer
     */
    public GroovyPrintWriter(Writer out) {
        super(out);
    }

    /**
     * Creates a writer that delegates to the supplied writer.
     *
     * @param out the underlying writer
     * @param autoflush whether println operations flush automatically
     */
    public GroovyPrintWriter(Writer out, boolean autoflush) {
        super(out, autoflush);
    }

    /**
     * Creates a writer for the supplied output stream.
     *
     * @param out the underlying output stream
     */
    public GroovyPrintWriter(OutputStream out) {
        super(out);
    }

    /**
     * Creates a writer for the supplied output stream.
     *
     * @param out the underlying output stream
     * @param autoflush whether println operations flush automatically
     */
    public GroovyPrintWriter(OutputStream out, boolean autoflush) {
        super(out, autoflush);
    }

    /**
     * Creates a writer for the supplied file name.
     *
     * @param filename the target file name
     * @throws FileNotFoundException if the file cannot be opened
     */
    public GroovyPrintWriter(String filename) throws FileNotFoundException {
        super(filename);
    }

    /**
     * Creates a writer for the supplied file name using the given charset name.
     *
     * @param filename the target file name
     * @param csn the charset name
     * @throws FileNotFoundException if the file cannot be opened
     * @throws UnsupportedEncodingException if the charset is unsupported
     */
    public GroovyPrintWriter(String filename, String csn)
            throws FileNotFoundException, UnsupportedEncodingException {
        super(filename, csn);
    }

    /**
     * Prints an object using Groovy formatting.
     *
     * @param x the value to print
     */
    @Override
    public void print(Object x) {
        write(FormatHelper.toString(x));
    }

    /**
     * Prints an object followed by a line separator using Groovy formatting.
     *
     * @param x the value to print
     */
    @Override
    public void println(Object x) {
        // JDK 1.6 has changed the implementation to do a
        // String.valueOf(x) rather than call print(x).
        // Probably to improve performance by doing the conversion outside the lock.
        // This will do the same thing for us, and we don't have to have access to the lock.
        println(FormatHelper.toString(x));
    }
}
