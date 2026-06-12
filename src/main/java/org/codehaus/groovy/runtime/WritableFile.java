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

import groovy.lang.Writable;

import java.io.*;

/**
 * A file wrapper implementing the {@link Writable} interface to support writing file contents.
 * Allows a file to be written to any output stream, optionally with a specified character encoding.
 */
public class WritableFile extends File implements Writable {
    @Serial private static final long serialVersionUID = 4157767752861425917L;
    /** Optional character encoding for reading the file. */
    private final String encoding;

    /**
     * Constructs a WritableFile from a delegate File with default character encoding.
     *
     * @param delegate the underlying {@link File} to wrap
     */
    public WritableFile(final File delegate) {
        this(delegate, null);
    }

    /**
     * Constructs a WritableFile from a delegate File with optional character encoding.
     *
     * @param delegate the underlying {@link File} to wrap
     * @param encoding the character encoding to use when reading the file, or {@code null} for default
     */
    public WritableFile(final File delegate, final String encoding) {
        super(delegate.toURI());
        this.encoding = encoding;
    }

    /**
     * Writes this file's contents to the specified Writer.
     * If encoding was specified, uses that encoding when reading the file.
     *
     * @param out the {@link Writer} to write the file contents to
     * @return the same {@link Writer} for method chaining
     * @throws IOException if an I/O error occurs during reading or writing
     */
    @Override
    public Writer writeTo(final Writer out) throws IOException {

        try (Reader reader = (this.encoding == null)
                ? ResourceGroovyMethods.newReader(this)
                : ResourceGroovyMethods.newReader(this, this.encoding)) {
            int c = reader.read();

            while (c != -1) {
                out.write(c);
                c = reader.read();
            }
        }
        return out;
    }
}
