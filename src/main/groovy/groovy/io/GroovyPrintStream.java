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

import org.codehaus.groovy.runtime.InvokerHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * A PrintStream that outputs objects in Groovy style.
 * That means print(Object) uses InvokerHelper.toString(Object)
 * to produce the same results as Writer.print(Object).
 *
 * @since 1.6
 */
public class GroovyPrintStream extends PrintStream {
    /**
     * Creates a new print stream.  This stream will not flush automatically.
     *
     * @see java.io.PrintStream#PrintStream(java.io.OutputStream)
     */
    public GroovyPrintStream(OutputStream out) {
        super(out, false);
    }

    /**
     * Creates a new print stream.
     *
     * @see java.io.PrintStream#PrintStream(java.io.OutputStream, boolean)
     */
    public GroovyPrintStream(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
    }

    /**
     * Creates a new print stream.
     *
     * @see java.io.PrintStream#PrintStream(java.io.OutputStream, boolean, String)
     */
    public GroovyPrintStream(OutputStream out, boolean autoFlush, String encoding)
            throws UnsupportedEncodingException {
        super(out, autoFlush, encoding);
    }

    /**
     * Creates a new print stream, without automatic line flushing, with the
     * specified file name.
     *
     * @see java.io.PrintStream#PrintStream(String)
     */
    public GroovyPrintStream(String fileName) throws FileNotFoundException {
        super(fileName);
    }

    /**
     * Creates a new print stream, without automatic line flushing, with the
     * specified file name and charset.
     *
     * @see java.io.PrintStream#PrintStream(String, String)
     */
    public GroovyPrintStream(String fileName, String csn)
            throws FileNotFoundException, UnsupportedEncodingException {
        super(fileName, csn);
    }

    /**
     * Creates a new print stream, without automatic line flushing, with the
     * specified file.
     *
     * @see java.io.PrintStream#PrintStream(File)
     */
    public GroovyPrintStream(File file) throws FileNotFoundException {
        super(file);
    }

    /**
     * Creates a new print stream, without automatic line flushing, with the
     * specified file and charset.
     *
     * @see java.io.PrintStream#PrintStream(File, String)
     */
    public GroovyPrintStream(File file, String csn)
            throws FileNotFoundException, UnsupportedEncodingException {
        super(file, csn);
    }

    /**
     * Prints an object Groovy style.
     *
     * @param obj The <code>Object</code> to be printed
     */
    public void print(Object obj) {
        print(InvokerHelper.toString(obj));
    }

    /**
     * Prints an object Groovy style followed by a newline.
     *
     * @param obj The <code>Object</code> to be printed
     */
    public void println(Object obj) {
        println(InvokerHelper.toString(obj));
    }

}
