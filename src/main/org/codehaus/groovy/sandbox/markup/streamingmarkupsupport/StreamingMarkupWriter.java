package org.codehaus.groovy.sandbox.markup.streamingmarkupsupport;
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
import java.io.Writer;

public class StreamingMarkupWriter extends Writer {
	protected final Writer delegate;
	private final int encodingLimit = 127; // initally encode everything that's not US ASCII
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
											public void write(int c) throws IOException {
												if (c > StreamingMarkupWriter.this.encodingLimit) {
													StreamingMarkupWriter.this.delegate.write("&#x");
													StreamingMarkupWriter.this.delegate.write(Integer.toHexString(c));
													StreamingMarkupWriter.this.delegate.write(';');
												} else if (c == '<') {
													StreamingMarkupWriter.this.delegate.write("&lt;");
												} else if (c == '>') {
													StreamingMarkupWriter.this.delegate.write("&gt;");
												} else if (c == '&') {
													StreamingMarkupWriter.this.delegate.write("&amp;");
												} else {
													StreamingMarkupWriter.this.delegate.write(c);
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
												public void write(int c) throws IOException {
													if (c == '\'') {
														StreamingMarkupWriter.this.delegate.write("&apos;");
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

		public StreamingMarkupWriter(final Writer delegate) {
			this.delegate = delegate;
		}
	
		/* (non-Javadoc)
		 * @see java.io.Writer#close()
		 */
		public void close() throws IOException {
			this.delegate.close();
		}

		/* (non-Javadoc)
		 * @see java.io.Writer#flush()
		 */
		public void flush() throws IOException {
			this.delegate.flush();
		}
		
		/* (non-Javadoc)
		 * @see java.io.Writer#write(char[], int, int)
		 */
		public void write(final char[] cbuf, int off, int len) throws IOException {
			this.delegate.write(cbuf, off, len);
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
	}
