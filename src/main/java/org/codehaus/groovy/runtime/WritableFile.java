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

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * A Writable File.
 */
public class WritableFile extends File implements Writable {
    private static final long serialVersionUID = 4157767752861425917L;
    private final String encoding;

    public WritableFile(final File delegate) {
        this(delegate, null);
    }

    public WritableFile(final File delegate, final String encoding) {
        super(delegate.toURI());
        this.encoding = encoding;
    }

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
