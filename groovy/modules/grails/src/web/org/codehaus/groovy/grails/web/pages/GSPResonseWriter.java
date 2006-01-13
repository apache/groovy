/*
 * Copyright 2004-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.grails.web.pages;

import javax.servlet.ServletResponse;
import java.io.*;

/**
 * NOTE: Based on work done by on the GSP standalone project (https://gsp.dev.java.net/)
 *
 * A buffered writer that won't commit the response until the buffer has reached the high
 * water mark, or until flush() or close() is called.
 *
 * @author Troy Heninger
 * @author Graeme Rocher
 *
 * Date: Jan 10, 2004
 *
 */
public class GSPResonseWriter extends PrintWriter {
	private ServletResponse response;
	private CharArrayWriter out0 = new CharArrayWriter();
	private Writer out1;
	private int max;
	private boolean trouble = false;

	/**
	 * Private constructor.  Use getInstance() instead.
	 * @param response
	 * @param out
	 * @param max
	 */
	private GSPResonseWriter(ServletResponse response, CharArrayWriter out, int max) {
		super(out);
		this.response = response;
		this.out0 = out;
		this.max = max;
	} // GSPResonseWriter

	/**
	 * Private constructor.  Use getInstance() instead.
	 * @param writer The target writer to write to
	 * @param out
	 * @param max
	 */
	private GSPResonseWriter(Writer writer, CharArrayWriter out, int max) {
		super(out);
		this.out0 = out;
        this.out1 = writer;
        this.max = max;
	}

    /**
	 * Make sure streams get closed eventually.
	 * @throws Throwable
	 */
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	} // finalize()

	/**
	 * Flush the stream if it's not closed and check its error state.
	 * Errors are cumulative; once the stream encounters an error, this
	 * routine will return true on all successive calls.
	 *
	 * @return True if the print stream has encountered an error, either on the
	 * underlying output stream or during a format conversion.
	 */
	public boolean checkError() {
		if (super.checkError()) return true;
		return trouble;
	} // checkError()

	/**
	 * Close the stream.
	 * @see #checkError()
	 */
	public void close() {
		if (!response.isCommitted()) {
			response.setContentLength(out0.size());
		}
		flush();
		super.close();
	} // close()

	/**
	 * Flush the stream.
	 * @see #checkError()
	 */
	public synchronized void flush() {
		if (trouble) return;
		super.flush();
		if (out1 == null) {
			try {
				out1 = response.getWriter();
			} catch (IOException e) {
				trouble = true;
				return;
			}
		}
		try {
			out1.write(out0.toCharArray());
			out0.reset();
		} catch (IOException e) {
			trouble = true;
		}
	} // flush()

	/**
	 * Static factory method to create the writer.
	 * @param response
	 * @param max
	 * @return  A GSPResonseWriter instance
	 */
	static GSPResonseWriter getInstance(ServletResponse response, int max) {
		return new GSPResonseWriter(response, new CharArrayWriter(max), max);
	} // getInstance()

	/**
	 * Static factory method to create the writer.
	 * @param target The target writer to write too
	 * @param max
	 * @return  A GSPResonseWriter instance
	 */
	static GSPResonseWriter getInstance(Writer target, int max) {
		return new GSPResonseWriter(target, new CharArrayWriter(max), max);
	} // getInstance()

    /**
	 * Print an object.  The string produced by the <code>{@link
	 * java.lang.String#valueOf(Object)}</code> method is translated into bytes
	 * according to the platform's default character encoding, and these bytes
	 * are written in exactly the manner of the <code>{@link #write(int)}</code>
	 * method.
	 *
	 * @param      obj   The <code>Object</code> to be printed
	 * @see        java.lang.Object#toString()
	 */
	public void print(Object obj) {
		if (obj == null) obj = "";
		write(String.valueOf(obj));
	}

	/**
	 * Print a string.  If the argument is <code>null</code> then the string
	 * <code>""</code> is printed.  Otherwise, the string's characters are
	 * converted into bytes according to the platform's default character
	 * encoding, and these bytes are written in exactly the manner of the
	 * <code>{@link #write(int)}</code> method.
	 *
	 * @param      s   The <code>String</code> to be printed
	 */
	public void print(String s) {
		if (s == null) s = "";
		write(s);
	} // print()

	/**
	 * Write a single character.
	 * @param c int specifying a character to be written.
	 */
	public void write(int c) {
		if (trouble) return;
		super.write(c);
		if (out0.size() >= max) {
			flush();
		}
	} // write()

	/**
	 * Write a portion of an array of characters.
	 * @param buf Array of characters
	 * @param off Offset from which to start writing characters
	 * @param len Number of characters to write
	 */
	public void write(char buf[], int off, int len) {
		if (trouble || buf == null || len == 0) return;
		super.write(buf, off, len);
		if (out0.size() >= max) {
			flush();
		}
	} // write()

	/**
	 * Write a portion of a string.
	 * @param s A String
	 * @param off Offset from which to start writing characters
	 * @param len Number of characters to write
	 */
	public void write(String s, int off, int len) {
		if (trouble || s == null || s.length() == 0) return;
		super.write(s, off, len);
		if (out0.size() >= max) {
			flush();
		}
	} // write()

} // GSPResonseWriter
