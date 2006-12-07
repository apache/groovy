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
	private final Writer bodyWriter =  new Writer() {
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
												if (!StreamingMarkupWriter.this.encoder.canEncode((char)c)) {
													StreamingMarkupWriter.this.writer.write("&#x");
													StreamingMarkupWriter.this.writer.write(Integer.toHexString(c));
													StreamingMarkupWriter.this.writer.write(';');
												} else if (c == '<') {
													StreamingMarkupWriter.this.writer.write("&lt;");
												} else if (c == '>') {
													StreamingMarkupWriter.this.writer.write("&gt;");
												} else if (c == '&') {
													StreamingMarkupWriter.this.writer.write("&amp;");
												} else {
													StreamingMarkupWriter.this.writer.write(c);
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
											
											public Writer attributeValue() {
												return StreamingMarkupWriter.this.attributeWriter;
											}
											
											public Writer bodyText() {
												return bodyWriter;
											}
											
											public Writer unescaped() {
												return StreamingMarkupWriter.this;
											}
										};
	
	private final Writer attributeWriter =  new Writer() {
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
													if (c == '\'') {
														StreamingMarkupWriter.this.writer.write("&apos;");
													} else {
														StreamingMarkupWriter.this.bodyWriter.write(c);
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
												
												public Writer attributeValue() {
													return attributeWriter;
												}
												
												public Writer bodyText() {
													return StreamingMarkupWriter.this.bodyWriter;
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
            if (!this.encoder.canEncode((char)c)) {
                this.writer.write("&#x");
                this.writer.write(Integer.toHexString(c));
                this.writer.write(';');
            } else {
                this.writer.write(c);
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
		
		public Writer attributeValue() {
			return this.attributeWriter;
		}
		
		public Writer bodyText() {
			return this.bodyWriter;
		}
		
		public Writer unescaped() {
			return this;
		}
        
        public String getEncoding() {
            return this.encoding;
        }
	}
