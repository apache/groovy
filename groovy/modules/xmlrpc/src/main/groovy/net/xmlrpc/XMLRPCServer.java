/*

Copyright 2004, 2005 (C) John Wilson. All Rights Reserved.

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
  
  static final String xmlDeclaration = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n";
	static final byte[] userAgent = "User-Agent: Groovy XML-RPC\r\n".getBytes();
	static final byte[] contentTypeXML = "Content-Type: text/xml\r\n".getBytes();
	static final byte[] contentLength = "Content-Length: ".getBytes();
  
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
                                
                final byte[] response = XMLRPCMessageProcessor.emitResult(new StringBuffer(xmlDeclaration), result).toString().getBytes("ISO-8859-1");
                
                out.write(String.valueOf(response.length).getBytes());
                out.write(endOfLine);
                out.write(endOfLine);
                out.write(response);
              }
              catch (Throwable e) {
//              e.printStackTrace();
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
                
                final byte[] error = XMLRPCMessageProcessor.emitError(new StringBuffer(xmlDeclaration),
                                                                      codeValue,
                                                                      (message == null) ? e.getClass().getName() : message).toString().getBytes("ISO-8859-1");
               
                out.write(String.valueOf(error.length).getBytes());
                out.write(endOfLine);
                out.write(endOfLine);
                out.write(error);
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
