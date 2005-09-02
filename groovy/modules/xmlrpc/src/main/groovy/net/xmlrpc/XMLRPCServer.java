/*
 * Copyright 2005 John G. Wilson
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
 *
 */

package groovy.net.xmlrpc;


import groovy.lang.Closure;
import groovy.lang.GroovyRuntimeException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.List;

import org.codehaus.groovy.runtime.InvokerInvocationException;

import uk.co.wilson.net.http.MinMLHTTPServer;
import uk.co.wilson.net.xmlrpc.XMLRPCFailException;
import uk.co.wilson.net.xmlrpc.XMLRPCMessageProcessor;

/**
* @author John Wilson (tug@wilson.co.uk)
*
*/
public class XMLRPCServer extends RPCServer {
private byte[] base64 = new byte[600];
{
	for (int i = 0; i != this.base64.length; i++) {
		this.base64[i] = (byte)i;
	}
}
public byte[] getBase64() { return this.base64;} // bodge to allow testing

	static byte[] host;
	static {
		try {
			host  = ("Host: " + InetAddress.getLocalHost().getHostName() +"\r\n").getBytes();
		} catch (UnknownHostException e) {
			host = "Host: unknown\r\n ".getBytes();
		}
	}
	static final byte[] userAgent = "User-Agent: Groovy XML-RPC\r\n".getBytes();
	static final byte[] contentTypeXML = "Content-Type: text/xml\r\n".getBytes();
	static final byte[] contentLength = "Content-Length: ".getBytes();
  static final byte[] xmlDeclaration = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n".getBytes();
  static final byte[] startResponse = ("<methodResponse>\n" +
                           "\t<params>\n" +
                           "\t\t<param>\n").getBytes();
  static final byte[] endResponse = ("\n" +
                           "\t\t</param>\n" +
                           "\t</params>\n" +
                           "</methodResponse>").getBytes();
  static final byte[] startError = ("<methodResponse>\n" +
                          "\t<fault>\n" +
                          "\t\t<value>\n" +
                          "\t\t\t<struct>\n" +
                          "\t\t\t\t<member>\n" +
                          "\t\t\t\t\t<name>faultCode</name>\n" +
                          "\t\t\t\t\t<value><int>").getBytes();
  static final byte[] middleError = ("</int></value>\n" +
                          "\t\t\t\t</member>\n" +
                          "\t\t\t\t<member>\n" +
                          "\t\t\t\t\t<name>faultString</name>\n" +
                          "\t\t\t\t\t<value><string>").getBytes();
  static final byte[] endError = ("</string></value>\n" +
                        "\t\t\t\t</member>\n" +
                        "\t\t\t</struct>\n" +
                        "\t\t</value>\n" +
                        "\t</fault>\n" +
                        "</methodResponse>\n").getBytes();
  
	final int minWorkers;
	final int maxWorkers;
	final int maxKeepAlives;
	final int workerIdleLife;
	final int socketReadTimeout;

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
                
                if (XMLRPCServer.this.preCallMethod != null) {
                  XMLRPCServer.this.preCallMethod.call(new Object[] {methodName, params.toArray()});
                }
                
                if (closure == null) {
                  if (XMLRPCServer.this.defaultMethod == null) {
                    throw new GroovyRuntimeException("Method " + methodName + " is not supported on this server");
                  }
                  
                  result = XMLRPCServer.this.defaultMethod.call(new Object[] {methodName, params.toArray()});
                } else {
                  result = closure.call(params.toArray());
                }
                
                if (result == null) result = new Integer(0);
                
                if (XMLRPCServer.this.postCallMethod != null) {
                  XMLRPCServer.this.postCallMethod.call(new Object[] {methodName, result});
                }
                
                XMLRPCMessageProcessor.emit(buffer, result);
                
  //              System.out.println(buffer.toString());
                
                final byte[] response = buffer.toString().getBytes("ISO-8859-1");
                
                out.write(String.valueOf(xmlDeclaration.length + startResponse.length + response.length + endResponse.length).getBytes());
                out.write(endOfLine);
                out.write(endOfLine);
                out.write(xmlDeclaration);
                out.write(startResponse);
                out.write(response);
                out.write(endResponse);
              }
              catch (Throwable e) {
  //            e.printStackTrace();
              final String message;
              final int codeValue;
              
                if (e instanceof InvokerInvocationException) {
                  e = ((InvokerInvocationException)e).getCause();
                }
                
                if (e instanceof XMLRPCFailException) {
                  message = ((XMLRPCFailException)e).getFaultString();
                  codeValue = ((XMLRPCFailException)e).getFaultCode();
                } else {
                  message = e.getMessage();
                  codeValue = 0;
                }
                
                if (XMLRPCServer.this.faultMethod != null) {
                  try {
                    XMLRPCServer.this.faultMethod.call(new Object[] {message, new Integer(codeValue)});
                  }
                  catch (final Throwable e1) {
                    // swallow this and return the orginal fault
                  }
                }
                
                final byte[] error = ((message == null) ? e.getClass().getName() : message).getBytes();
                final byte[] code = String.valueOf(codeValue).getBytes();
                
                out.write(String.valueOf(xmlDeclaration.length + startError.length + code.length + middleError.length + error.length + endError.length).getBytes());
                out.write(endOfLine);
                out.write(endOfLine);
                out.write(xmlDeclaration);
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
      
      
      final Thread startingThread = new Thread() {
        public void run() {
          server.start();
        }
      };
      
      startingThread.setDaemon(false);
      startingThread.setName("XML-RPC Server main thread");
      startingThread.start();
    }
}
