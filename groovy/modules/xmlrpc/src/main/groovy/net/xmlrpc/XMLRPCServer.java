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

package groovy.net.xmlrpc;

import groovy.lang.Closure;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.GroovyRuntimeException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.wilson.net.http.MinMLHTTPServer;
import uk.co.wilson.net.xmlrpc.XMLRPCFailException;
import uk.co.wilson.net.xmlrpc.XMLRPCMessageProcessor;

/**
* @author John Wilson (tug@wilson.co.uk)
*
*/
public class XMLRPCServer extends GroovyObjectSupport {
private byte[] base64 = new byte[600];
{
	for (int i = 0; i != this.base64.length; i++) {
		this.base64[i] = (byte)i;
	}
}
public byte[] getBase64() { return this.base64;} // bodge to allow testing

	private static byte[] host;
	static {
		try {
			host  = ("Host: " + InetAddress.getLocalHost().getHostName() +"\r\n").getBytes();
		} catch (UnknownHostException e) {
			host = "Host: unknown\r\n ".getBytes();
		}
	}
	private static final byte[] userAgent = "User-Agent: Groovy XML-RPC\r\n".getBytes();
	private static final byte[] contentTypeXML = "Content-Type: text/xml\r\n".getBytes();
	private static final byte[] contentLength = "Content-Length: ".getBytes();
	private static final byte[] startResponse = ("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + 
												 "<methodResponse>\n" +
												 "\t<params>\n" +
												 "\t\t<param>\n").getBytes();
	private static final byte[] endResponse = ("\n" +
											   "\t\t</param>\n" +
											   "\t</params>\n" +
											   "</methodResponse>").getBytes();
	private static final byte[] startError = ("<?xml version=\"1.0\"?>\n" + 
											  "<methodResponse>\n" +
											  "\t<fault>\n" +
											  "\t\t<value>\n" +
											  "\t\t\t<struct>\n" +
											  "\t\t\t\t<member>\n" +
											  "\t\t\t\t\t<name>faultCode</name>\n" +
											  "\t\t\t\t\t<value><int>").getBytes();
	
	private static final byte[] middleError = ("</int></value>\n" +
											  "\t\t\t\t</member>\n" +
											  "\t\t\t\t<member>\n" +
											  "\t\t\t\t\t<name>faultString</name>\n" +
											  "\t\t\t\t\t<value><string>").getBytes();
	
	private static final byte[] endError = ("</string></value>\n" +
											"\t\t\t\t</member>\n" +
											"\t\t\t</struct>\n" +
											"\t\t</value>\n" +
											"\t</fault>\n" +
											"</methodResponse>\n").getBytes();
	
	private MinMLHTTPServer server = null;
	private Closure defaultMethod = null;
	private final int minWorkers;
	private final int maxWorkers;
	private final int maxKeepAlives;
	private final int workerIdleLife;
	private final int socketReadTimeout;
	private final StringBuffer propertyPrefix = new StringBuffer();
	private final Map registeredMethods = new HashMap();

	/**
	 * @param minWorkers
	 * @param maxWorkers
	 * @param maxKeepAlives
	 * @param workerIdleLife
	 * @param socketReadTimeout
	 */
	public XMLRPCServer(final int minWorkers,
						final int maxWorkers,
						final int maxKeepAlives,
						final int workerIdleLife,
						final int socketReadTimeout)
	{
		this.minWorkers = minWorkers;
		this.maxWorkers = maxWorkers;
		this.maxKeepAlives = maxKeepAlives;
		this.workerIdleLife = workerIdleLife;
		this.socketReadTimeout = socketReadTimeout;
	}
	
	/**
	 * 
	 */
	public XMLRPCServer() {
		this(2, 10, 8, 60000, 60000);
	}
	
	/**
	 * @param serverSocket
	 */
	public void startServer(final ServerSocket serverSocket) throws IOException {
		if (this.server != null) stopServer();
		
		final MinMLHTTPServer server = new MinMLHTTPServer(serverSocket,
								                           this.minWorkers, 
														   this.maxWorkers, 
														   this.maxKeepAlives, 
														   this.workerIdleLife, 
														   this.socketReadTimeout) {

			/* (non-Javadoc)
			 * @see uk.co.wilson.net.MinMLSocketServer#makeNewWorker()
			 */
			protected Worker makeNewWorker() {
				return new HTTPWorker() {
					protected void processPost(final InputStream in,
											   final OutputStream out,
											   final String uri,
											   final String version)
						throws Exception
					{
						
						try {
						final StringBuffer buffer = new StringBuffer();
						final XMLRPCMessageProcessor requestParser = new XMLRPCMessageProcessor();
							
							out.write(version.getBytes());
							out.write(okMessage);
							out.write(userAgent);
							out.write(host);
							out.write(contentTypeXML);
							writeKeepAlive(out);
							out.write(contentLength);
							
							requestParser.parseMessage(in);
							
							final String methodName = requestParser.getMethodname();
							final List params = requestParser.getParams();
							final Closure closure = (Closure)XMLRPCServer.this.registeredMethods.get(methodName);
							Object result = null;
							
							if (closure == null) {
								if (XMLRPCServer.this.defaultMethod == null) {
									throw new GroovyRuntimeException("XML-RPC method " + methodName + " is not supported on this server");
								}
								
								result = XMLRPCServer.this.defaultMethod.call(new Object[] {methodName,params.toArray()});
							} else {
								result = closure.call(params.toArray());
							}
							
							if (result == null) result = new Integer(0);
							
							XMLRPCMessageProcessor.emit(buffer, result);
							
//							System.out.println(buffer.toString());
							
							final byte[] response = buffer.toString().getBytes("ISO-8859-1");
							
							out.write(String.valueOf(startResponse.length + response.length + endResponse.length).getBytes());
							out.write(endOfLine);
							out.write(endOfLine);
							out.write(startResponse);
							out.write(response);
							out.write(endResponse);
						}
						catch (final Throwable e) {
						e.printStackTrace();
						final String message;
						final int codeValue;
							
							if (e instanceof XMLRPCFailException) {
								message = ((XMLRPCFailException)e).getFaultString();
								codeValue = ((XMLRPCFailException)e).getFaultCode();
							} else {
								message = e.getMessage();
								codeValue = 0;
							}
							
							final byte[] error = ((message == null) ? e.getClass().getName() : message).getBytes();
							final byte[] code = String.valueOf(codeValue).getBytes();
							
							out.write(String.valueOf(startError.length + code.length + middleError.length + error.length + endError.length).getBytes());
							out.write(endOfLine);
							out.write(endOfLine);
							out.write(startError);
							out.write(code);
							out.write(middleError);
							out.write(error);
							out.write(endError);
						}
					}
				};
			}
		};
		
		this.server = server;
		
		new Thread() {
			public void run() {
				server.start();
			}
		}.start();
	}
	
	public void stopServer() throws IOException {
		this.server.shutDown();
	}
	
	public void returnFault(String msg, int code) {
		throw new XMLRPCFailException(msg, code);
	}
	
	public void setupDefaultMethod(final Closure defaultMethod) {
		this.defaultMethod = defaultMethod;
	}
	
	/* (non-Javadoc)
	 * @see groovy.lang.GroovyObject#getProperty(java.lang.String)
	 */
	public Object getProperty(final String property) {
		/**
		 * 
		 * Allow server.a.b.c = {...}
		 * This creates a method with the name "a.b.c"
		 * This technique is shamelessly stolen from the Python XML-RPC implementation
		 * Thanks and credit to Fredrik Lundh
		 * 
		 */

		this.propertyPrefix.append(property).append('.');
		
		return this;
	}
	
	/* (non-Javadoc)
	 * @see groovy.lang.GroovyObject#setProperty(java.lang.String, java.lang.Object)
	 */
	public void setProperty(final String property, final Object method) {
	final String methodName = this.propertyPrefix.append(property).toString();
	Closure closure = null;
	
		this.propertyPrefix.setLength(0);
	
		if (method instanceof Closure) {
			//
			// This malarky with the CloneNotSupportedException is to keep the broken sun
			// java compiler from barfing
			//
			try {
				if (false) throw new CloneNotSupportedException();
				closure = (Closure)(((Closure)method).clone());
				closure.setDelegate(this);
			}
			catch (final CloneNotSupportedException e) {
				// never thrown
			}
		} else if (method instanceof Class) {
			closure = null;
		} else {
		//
		// calling a method on an instance of a class
		//
			
		final Method methods[] = method.getClass().getMethods();
		boolean foundMatch = false;
		int numberofParameters = 0;
		
			for (int i = 0; i != methods.length; i++) {
				if (methods[i].getName().equals(methodName)) {
					if (foundMatch) {
						if (numberofParameters != methods[i].getParameterTypes().length)
							;// TODO: throw exception
					} else {
						foundMatch = true;
						numberofParameters = methods[i].getParameterTypes().length;
					}
				}
			}
			
			if (foundMatch) {
				closure = makeObjectProxy(methodName, numberofParameters);
				closure.setDelegate(method);
			} else {
				// TODO: throw execption
				closure = null;
			}
		}
		
		this.registeredMethods.put(methodName, closure);
	}
	
	private Closure makeObjectProxy(final String methodName, final int numberOfParameters) {
	final String paramIn, paramOut;
	
		if (numberOfParameters == 0) {
			paramIn = paramOut = "";
		} else {
		final StringBuffer params = new StringBuffer();
		
			for (int i = 0; i != numberOfParameters; i++) {
				params.append(", p" + i);
			}
			
			paramOut = params.delete(0, 2).toString();
			paramIn = paramOut + " |";
		}
		
	final String generatedCode = "class X { closure = {" + paramIn + " this." + methodName + "(" + paramOut + ") }}";
	System.out.println(generatedCode);
	
		try {
		final InputStream in = new ByteArrayInputStream(generatedCode.getBytes());
		final GroovyObject groovyObject = (GroovyObject)new GroovyClassLoader().parseClass(in, methodName).newInstance();
			
			return (Closure)(groovyObject.getProperty("closure"));
		} catch (Exception e) {
			throw new GroovyRuntimeException("Can't generate proxy for XML-RPC method " + methodName, e);
		}
	}
}
