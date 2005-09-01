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
import java.io.StringReader;
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
              
              requestParser.parse(new StringReader(request.getChildElementXML()));
            
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
              
              buffer.append(startResponse);
              XMLRPCMessageProcessor.emit(buffer, result);
              buffer.append(endResponse);
              
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
          final Packet responsePacket =  new JabberRPC(response.toString());
          
            responsePacket.setFrom(request.getTo());
            responsePacket.setTo(request.getFrom());
            responsePacket.setPacketID(request.getPacketID());
            
            JabberRPCServer.this.connection.sendPacket(responsePacket);
          }
        };
      }
    };
    
    new Thread() {
      public void run() {
        server.start();
      }
    }.start();
  }
  
  private XMPPConnection connection;
  private MinMLJabberPacketServer server = null;
  
  private final int minWorkers;
  private final int maxWorkers;
  private final int workerIdleLife;
}
