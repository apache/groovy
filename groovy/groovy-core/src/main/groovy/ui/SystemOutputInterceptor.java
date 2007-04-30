package groovy.ui;

import groovy.lang.Closure;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Intercepts System.out. Implementation helper for Console.groovy.
 */
class SystemOutputInterceptor extends FilterOutputStream {

	private Closure callback;

	/**
	 * Constructor
	 * 
	 * @param callback
	 *            accepts a string to be sent to std out and returns a Boolean.
	 *            If the return value is true, output will be sent to
	 *            System.out, otherwise it will not.
	 */
	public SystemOutputInterceptor(Closure callback) {
		super(System.out);
		this.callback = callback;
	}

	/**
	 * Starts intercepting System.out
	 */
	public void start() {
		System.setOut(new PrintStream(this));
	}

	/**
	 * Stops intercepting System.out, sending output to whereever it was
	 * going when this interceptor was created.
	 */
	public void stop() {
		System.setOut((PrintStream) out);
	}

	/**
	 * Intercepts output - moret common case of byte[]
	 */
	public void write(byte[] b, int off, int len) throws IOException {
		Boolean result = (Boolean) callback.call(new String(b, off, len));
		if (result.booleanValue()) {
			out.write(b, off, len);
		}
	}

	/**
	 * Intercepts output - single characters
	 */
	public void write(int b) throws IOException {
		Boolean result = (Boolean) callback.call(String.valueOf((char) b));
		if (result.booleanValue()) {
			out.write(b);
		}
	}
}
