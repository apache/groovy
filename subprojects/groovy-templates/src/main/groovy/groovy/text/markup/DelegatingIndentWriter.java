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
package groovy.text.markup;

import java.io.IOException;
import java.io.Writer;

/**
 * A writer which delegates to another writer and supports an additional indenting level.
 */
public class DelegatingIndentWriter extends Writer {
    /**
     * Four-space indentation unit.
     */
    public static final String SPACES = "    ";

    /**
     * Tab indentation unit.
     */
    public static final String TAB = "\t";

    private final Writer delegate;
    private final String indentString;
    private int level;

    /**
     * Creates an indenting writer that uses {@link #SPACES} for each indentation level.
     *
     * @param delegate writer that receives the rendered output
     */
    public DelegatingIndentWriter(final Writer delegate) {
        this(delegate, SPACES);
    }

    /**
     * Creates an indenting writer that delegates output to another writer.
     *
     * @param delegate writer that receives the rendered output
     * @param indentString indentation unit written for each nesting level
     */
    public DelegatingIndentWriter(final Writer delegate, final String indentString) {
        this.delegate = delegate;
        this.indentString = indentString;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final int c) throws IOException {
        delegate.write(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final char[] cbuf) throws IOException {
        delegate.write(cbuf);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final char[] cbuf, final int off, final int len) throws IOException {
        delegate.write(cbuf, off, len);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final String str) throws IOException {
        delegate.write(str);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final String str, final int off, final int len) throws IOException {
        delegate.write(str, off, len);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Writer append(final CharSequence csq) throws IOException {
        return delegate.append(csq);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Writer append(final CharSequence csq, final int start, final int end) throws IOException {
        return delegate.append(csq, start, end);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Writer append(final char c) throws IOException {
        return delegate.append(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        delegate.close();
    }

    /**
     * Increases the current indentation level.
     *
     * @return the new indentation level
     */
    public int next() {
        return ++level;
    }

    /**
     * Decreases the current indentation level.
     *
     * @return the new indentation level
     */
    public int previous() {
        return --level;
    }

    /**
     * Writes the current indentation prefix to the delegate writer.
     *
     * @throws IOException if writing the indentation fails
     */
    public void writeIndent() throws IOException {
        for (int i=0;i<level;i++) {
            delegate.write(indentString);
        }
    }
}
