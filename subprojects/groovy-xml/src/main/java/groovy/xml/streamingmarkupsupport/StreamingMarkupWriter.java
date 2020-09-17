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
package groovy.xml.streamingmarkupsupport;

import groovy.io.EncodingAwareBufferedWriter;
import groovy.xml.markupsupport.DoubleQuoteFilter;
import groovy.xml.markupsupport.SingleQuoteFilter;
import groovy.xml.markupsupport.StandardXmlAttributeFilter;
import groovy.xml.markupsupport.StandardXmlFilter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Optional;
import java.util.function.Function;

public class StreamingMarkupWriter extends Writer {
    protected final Writer writer;
    protected final String encoding;
    protected boolean encodingKnown;
    protected final CharsetEncoder encoder;
    protected boolean writingAttribute = false;
    protected boolean haveHighSurrogate = false;
    protected StringBuilder surrogatePair = new StringBuilder(2);
    private final Function<Character, Optional<String>> stdFilter = new StandardXmlFilter();
    private final Function<Character, Optional<String>> attrFilter = new StandardXmlAttributeFilter();
    private final Function<Character, Optional<String>> quoteFilter;
    private final Writer escapedWriter = new Writer() {
        /* (non-Javadoc)
        * @see java.io.Writer#close()
        */
        public void close() throws IOException {
            StreamingMarkupWriter.this.close();
        }

        /* (non-Javadoc)
        * @see java.io.Writer#flush()
        */
        public void flush() throws IOException {
            StreamingMarkupWriter.this.flush();
        }

        /* (non-Javadoc)
        * @see java.io.Writer#write(int)
        */
        public void write(final int c) throws IOException {
            Optional<String> transformed = stdFilter.apply((char) c);
            if (transformed.isPresent()) {
                StreamingMarkupWriter.this.writer.write(transformed.get());
                return;
            }
            StreamingMarkupWriter.this.write(c);
        }

        /* (non-Javadoc)
        * @see java.io.Writer#write(char[], int, int)
        */
        public void write(final char[] cbuf, int off, int len) throws IOException {
            while (len-- > 0) {
                write(cbuf[off++]);
            }
        }

        public void setWritingAttribute(final boolean writingAttribute) {
            StreamingMarkupWriter.this.writingAttribute = writingAttribute;
        }

        public Writer escaped() {
            return escapedWriter;
        }

        public Writer unescaped() {
            return StreamingMarkupWriter.this;
        }
    };

    public StreamingMarkupWriter(final Writer writer, final String encoding) {
        this(writer, encoding, false);
    }

    public StreamingMarkupWriter(final Writer writer, final String encoding, boolean useDoubleQuotes) {
        this.quoteFilter = useDoubleQuotes ? new DoubleQuoteFilter() : new SingleQuoteFilter();
        this.writer = writer;

        if (encoding != null) {
            this.encoding = encoding;
            this.encodingKnown = true;
        } else if (writer instanceof OutputStreamWriter) {
            this.encoding = getNormalizedEncoding(((OutputStreamWriter) writer).getEncoding());
            this.encodingKnown = true;
        } else if (writer instanceof EncodingAwareBufferedWriter) {
            this.encoding = getNormalizedEncoding(((EncodingAwareBufferedWriter) writer).getEncoding());
            this.encodingKnown = true;
        } else {
            this.encoding = "US-ASCII";
            this.encodingKnown = false;
        }

        this.encoder = Charset.forName(this.encoding).newEncoder();
    }

    private static String getNormalizedEncoding(String unnormalized) {
        return Charset.forName(unnormalized).name();
    }

    public StreamingMarkupWriter(final Writer writer) {
        this(writer, null);
    }

    /* (non-Javadoc)
    * @see java.io.Writer#close()
    */
    public void close() throws IOException {
        this.writer.close();
    }

    /* (non-Javadoc)
    * @see java.io.Writer#flush()
    */
    public void flush() throws IOException {
        this.writer.flush();
    }

    /* (non-Javadoc)
    * @see java.io.Writer#write(int)
    */
    public void write(final int c) throws IOException {
        if (c >= 0XDC00 && c <= 0XDFFF) {
            // Low surrogate
            this.surrogatePair.append((char) c);

            if (this.encoder.canEncode(this.surrogatePair)) {
                this.writer.write(this.surrogatePair.toString());
            } else {
                this.writer.write("&#x");
                this.writer.write(Integer.toHexString(0X10000 + ((this.surrogatePair.charAt(0) & 0X3FF) << 10) + (c & 0X3FF)));
                this.writer.write(';');
            }

            this.haveHighSurrogate = false;
            this.surrogatePair.setLength(0);
        } else {
            if (this.haveHighSurrogate) {
                this.haveHighSurrogate = false;
                this.surrogatePair.setLength(0);
                throw new IOException("High Surrogate not followed by Low Surrogate");
            }

            if (c >= 0XD800 && c <= 0XDBFF) {
                // High surrogate
                this.surrogatePair.append((char) c);
                this.haveHighSurrogate = true;

            } else if (!this.encoder.canEncode((char) c)) {
                this.writer.write("&#x");
                this.writer.write(Integer.toHexString(c));
                this.writer.write(';');
                return;
            }
            if (this.writingAttribute) {
                Optional<String> transformed = attrFilter.apply((char) c);
                if (transformed.isPresent()) {
                    this.writer.write(transformed.get());
                    return;
                }
                transformed = quoteFilter.apply((char) c);
                if (transformed.isPresent()) {
                    this.writer.write(transformed.get());
                    return;
                }
            }
            this.writer.write(c);
        }
    }

    /* (non-Javadoc)
    * @see java.io.Writer#write(char[], int, int)
    */
    public void write(final char[] cbuf, int off, int len) throws IOException {
        while (len-- > 0) {
            write(cbuf[off++]);
        }
    }

    public void setWritingAttribute(final boolean writingAttribute) {
        this.writingAttribute = writingAttribute;
    }

    public Writer escaped() {
        return this.escapedWriter;
    }

    public Writer unescaped() {
        return this;
    }

    public String getEncoding() {
        return this.encoding;
    }

    public boolean getEncodingKnown() {
        return this.encodingKnown;
    }
}
