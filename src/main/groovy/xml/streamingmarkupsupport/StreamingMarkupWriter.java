package groovy.xml.streamingmarkupsupport;
/*

Copyright 2004 (C) John Wilson. All Rights Reserved.

Redistribution and use of this software and associated documentation
("Software"), with or without modification, are permitted provided
that the following conditions are met:

1. Redistributions of source code must retain copyright
   statements and notices.  Redistributions must also contain a
   copy of this document.

2. Redistributions in binary form must reproduce the
   above copyright notice, this list of conditions and the
   following disclaimer in the documentation and/or other
   materials provided with the distribution.

3. The name "groovy" must not be used to endorse or promote
   products derived from this Software without prior written
   permission of The Codehaus.  For written permission,
   please contact info@codehaus.org.

4. Products derived from this Software may not be called "groovy"
   nor may "groovy" appear in their names without prior written
   permission of The Codehaus. "groovy" is a registered
   trademark of The Codehaus.

5. Due credit should be given to The Codehaus -
   http://groovy.codehaus.org/

THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE.

*/

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class StreamingMarkupWriter extends Writer {
	protected final Writer writer;
    protected final String encoding;
    protected final CharsetEncoder encoder;
    protected boolean writingAttribute = false;
    protected boolean haveHighSurrogate = false;
    protected StringBuffer surrogatePair = new StringBuffer(2);
	private final Writer escapedWriter =  new Writer() {
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
												if (c == '<') {
													StreamingMarkupWriter.this.writer.write("&lt;");
												} else if (c == '>') {
													StreamingMarkupWriter.this.writer.write("&gt;");
                                                   } else if (c == '&') {
                                                       StreamingMarkupWriter.this.writer.write("&amp;");
                                                   } else {
													StreamingMarkupWriter.this.write(c);
												}
											}
											
											/* (non-Javadoc)
											 * @see java.io.Writer#write(char[], int, int)
											 */
											public void write(final char[] cbuf, int off, int len) throws IOException {
												while (len-- > 0){
													write(cbuf[off++]);
												}
											}
                                            
                                               public void setWritingAttribute(final boolean writingAttribute) {
                                                   StreamingMarkupWriter.this.writingAttribute = writingAttribute;
                                               }
											
											public Writer excaped() {
												return escapedWriter;
											}
											
											public Writer unescaped() {
												return StreamingMarkupWriter.this;
											}
										};

    public StreamingMarkupWriter(final Writer writer, final String encoding) {
        this.writer = writer;
        
        if (encoding != null) {
            this.encoding = encoding;
        } else if (writer instanceof OutputStreamWriter) {
            this.encoding = ((OutputStreamWriter)writer).getEncoding();
        } else {
            this.encoding = "US-ASCII";
        }
        
        this.encoder = Charset.forName(this.encoding).newEncoder();
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
            this.surrogatePair.append((char)c);
            
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
                this.surrogatePair.append((char)c);
                this.haveHighSurrogate = true;
            
            } else if (!this.encoder.canEncode((char)c)) {
                this.writer.write("&#x");
                this.writer.write(Integer.toHexString(c));
                this.writer.write(';');
            } else if (c == '\'' && this.writingAttribute) {
                this.writer.write("&apos;");
            } else {
                this.writer.write(c);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see java.io.Writer#write(char[], int, int)
     */
    public void write(final char[] cbuf, int off, int len) throws IOException {
        while (len-- > 0){
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
}
