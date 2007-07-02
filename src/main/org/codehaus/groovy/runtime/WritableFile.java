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

import groovy.lang.Writable;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * A Writable File.
 *
 * @author John Wilson
 *
 */
public class WritableFile extends File implements Writable {
    private final String encoding;

    public WritableFile(final File delegate) {
        this(delegate, null);
    }

    public WritableFile(final File delegate, final String encoding) {
        super(delegate.toURI());
        this.encoding = encoding;
    }

    public Writer writeTo(final Writer out) throws IOException {
        final Reader reader =
            (this.encoding == null)
                ? DefaultGroovyMethods.newReader(this)
                : DefaultGroovyMethods.newReader(this, this.encoding);

        try {
            int c = reader.read();

            while (c != -1) {
                out.write(c);
                c = reader.read();
            }
        }
        finally {
            reader.close();
        }
        return out;
    }
}
