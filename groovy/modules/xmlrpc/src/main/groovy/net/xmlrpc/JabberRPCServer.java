/*

Copyright 2005 (C) John Wilson. All Rights Reserved.

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
import java.util.List;

import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;

import uk.co.wilson.net.MinMLJabberPacketServer;
import uk.co.wilson.net.xmlrpc.XMLRPCFailException;
import uk.co.wilson.net.xmlrpc.XMLRPCMessageProcessor;
import uk.co.wilson.smackx.packet.JabberRPC;

/**
 * @author John Wilson
 *
 */

public class JabberRPCServer extends RPCServer {
  static final String startError = ("<methodResponse>\n" +
                          "\t<fault>\n" +
                          "\t\t<value>\n" +
                          "\t\t\t<struct>\n" +
                          "\t\t\t\t<member>\n" +
                          "\t\t\t\t\t<name>faultCode</name>\n" +
                          "\t\t\t\t\t<value><int>");
  static final String middleError = ("</int></value>\n" +
                          "\t\t\t\t</member>\n" +
                          "\t\t\t\t<member>\n" +
                          "\t\t\t\t\t<name>faultString</name>\n" +
                          "\t\t\t\t\t<value><string>");
  static final String endError = ("</string></value>\n" +
                        "\t\t\t\t</member>\n" +
                        "\t\t\t</struct>\n" +
                        "\t\t</value>\n" +
                        "\t</fault>\n" +
                        "</methodResponse>\n");
  
  public JabberRPCServer(final int minWorkers,
                         final int maxWorkers,
                         final int workerIdleLife)
  {
    this.minWorkers = minWorkers;
    this.maxWorkers = maxWorkers;
    this.workerIdleLife = workerIdleLife;
  }

  public JabberRPCServer() {
    this(2, 10, 60000);
  }
  public void startServer(final XMPPConnection connection) throws IOException {
    this.connection = connection;
    
    if (this.server != null) stopServer();
    
    this.server = new MinMLJabberPacketServer(connection.createPacketCollector(new PacketFilter() {
                                                    public boolean accept(final Packet packet) {
                                                      return packet instanceof JabberRPC &&
                                                      ((JabberRPC)packet).getType() == IQ.Type.SET;
                                                 }
                                              }),
                                              this.minWorkers, 
                                              this.maxWorkers, 
                                              this.workerIdleLife) {
      
      protected Worker makeNewWorker() {
        return new JabberPacketWorker() {
          protected void process(final Object resource) throws Exception {
          final JabberRPC request = (JabberRPC)resource;
          final StringBuffer buffer = new StringBuffer();
          
            try {
            final XMLRPCMessageProcessor requestParser = new XMLRPCMessageProcessor();
              
              requestParser.parseMessage(request.getChildElementXML());
            
              final String methodName = requestParser.getMethodname();
              final List params = requestParser.getParams();
              final Closure closure = (Closure)JabberRPCServer.this.registeredMethods.get(methodName);
              Object result = null;
              
              if (JabberRPCServer.this.preCallMethod != null) {
                JabberRPCServer.this.preCallMethod.call(new Object[] {methodName, params.toArray()});
              }
              
              if (closure == null) {
                if (JabberRPCServer.this.defaultMethod == null) {
                  throw new GroovyRuntimeException("Method " + methodName + " is not supported on this server");
                }
                
                result = JabberRPCServer.this.defaultMethod.call(new Object[] {methodName, params.toArray()});
              } else {
                result = closure.call(params.toArray());
              }
              
              if (result == null) result = new Integer(0);
              
              if (JabberRPCServer.this.postCallMethod != null) {
                JabberRPCServer.this.postCallMethod.call(new Object[] {methodName, result});
              }
              
              XMLRPCMessageProcessor.emitResult(buffer, result);
              
              sendResponse(request, buffer.toString());
              
//              System.out.println(buffer.toString());
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
              
              if (JabberRPCServer.this.faultMethod != null) {
                try {
                  JabberRPCServer.this.faultMethod.call(new Object[] {message, new Integer(codeValue)});
                }
                catch (final Throwable e1) {
                  // swallow this and return the orginal fault
                }
              }
              
              buffer.setLength(0);
              buffer.append(startError);
              buffer.append(String.valueOf(codeValue));
              buffer.append(middleError);
              buffer.append((message == null) ? e.getClass().getName() : message);
              buffer.append(endError);
              
              sendResponse(request, buffer.toString());
            }
          }
          
          private void sendResponse(final JabberRPC request, final String response) {
          final IQ responsePacket =  new JabberRPC(response.toString());
          
            responsePacket.setFrom(request.getTo());
            responsePacket.setTo(request.getFrom());
            responsePacket.setPacketID(request.getPacketID());
            responsePacket.setType(IQ.Type.RESULT);
            
            JabberRPCServer.this.connection.sendPacket(responsePacket);
          }
        };
      }
    };
    
    final Thread startingThread = new Thread() {
      public void run() {
        server.start();
      }
    };
    
    startingThread.setDaemon(false);
    startingThread.setName("Jabber-RPC Server main thread");
    startingThread.start();
  }
  
  private XMPPConnection connection;
  private MinMLJabberPacketServer server = null;
  
  private final int minWorkers;
  private final int maxWorkers;
  private final int workerIdleLife;
}
