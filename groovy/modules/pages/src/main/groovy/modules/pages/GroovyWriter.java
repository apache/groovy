package groovy.modules.pages;

import javax.servlet.ServletResponse;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * Author: Troy Heninger
 * Date: Jan 13, 2004
 * A buffered writer that won't commit the response until the buffer has reached the high
 * water mark, or until flush() or close() is called.
 */
public class GroovyWriter extends PrintWriter {
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
	private GroovyWriter(ServletResponse response, CharArrayWriter out, int max) {
		super(out);
		this.response = response;
		this.out0 = out;
		this.max = max;
	} // GroovyWriter

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
//		System.out.println("GroovyWriter.close()");
		if (!response.isCommitted()) {
			response.setContentLength(out0.size());
		System.out.println("Content Length = " + out0.size());
		}
		flush();
		super.close();
	} // close()

	/**
	 * Flush the stream.
	 * @see #checkError()
	 */
	public synchronized void flush() {
//		System.out.println("GroovyWriter.flush()");
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
	 * @return
	 */
	static GroovyWriter getInstance(ServletResponse response, int max) {
		return new GroovyWriter(response, new CharArrayWriter(max), max);
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

} // GroovyWriter
