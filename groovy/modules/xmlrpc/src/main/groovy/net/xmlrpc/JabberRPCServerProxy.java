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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import uk.co.wilson.net.xmlrpc.XMLRPCFailException;
import uk.co.wilson.net.xmlrpc.XMLRPCMessageProcessor;
import uk.co.wilson.smackx.packet.JabberRPC;

/**
 * @author John Wilson
 *
 */

public class JabberRPCServerProxy extends RPCServerProxy {
  public JabberRPCServerProxy(final XMPPConnection connection, final String to) {
    this.connection = connection;
    
    final Iterator iter = connection.getRoster().getPresences(to);
    
    int pri = Integer.MIN_VALUE;
    String posTo = to;
    if (iter != null) {
      while(iter.hasNext()) {
      final Presence presence = (Presence)iter.next();
      
        if (presence.getPriority() > pri) {
          posTo = presence.getFrom();
          pri = presence.getPriority();
        }
      }
    }
    this.to = posTo;
  }
  
  /* (non-Javadoc)
   * @see groovy.lang.GroovyObject#invokeMethod(java.lang.String, java.lang.Object)
   */
  public Object invokeMethod(final String name, final Object args) {  
    if ("invokeMethod".equals(name)) return super.invokeMethod(name, args);
    
    final Object[] params = (args instanceof List) ? ((List)args).toArray() : (Object[])args;
    int numberOfparams = params.length;
    
      if (numberOfparams != 0 && params[numberOfparams - 1] instanceof Closure) {
        numberOfparams--; // the closure is not to be passed to the remote method
      }
    
    try {
    final JabberRPC request = new JabberRPC(XMLRPCMessageProcessor.emitCall(new StringBuffer(), name, params, numberOfparams).toString());
    final PacketCollector responseCollector = this.connection.createPacketCollector(new PacketFilter() {
                                                                                        public boolean accept(final Packet packet) {
                                                                                          return packet instanceof JabberRPC &&
                                                                                                 ((JabberRPC)packet).getType() == IQ.Type.RESULT &&
                                                                                                 packet.getPacketID().equals(request.getPacketID());
                                                                                        }
                                                                                      });
    
      request.setType(IQ.Type.SET);
      request.setTo(this.to);
      request.setFrom(this.connection.getUser());
      this.connection.sendPacket(request);
      
      final JabberRPC response = (JabberRPC)responseCollector.nextResult(20000);  // TODO: allow the timeout to be specified
      
      responseCollector.cancel();
      
      if (response == null) throw new XMLRPCCallFailureException("call timed out", new Integer(0));
      
      final XMLRPCMessageProcessor responseParser = new XMLRPCMessageProcessor();

      responseParser.parseMessage(response.getChildElementXML());
      
      final List result = responseParser.getParams();
      
      if (result == null) throw new XMLRPCCallFailureException("Empty response from server", new Integer(0));
      
      if (numberOfparams == params.length) {
        return result.get(0);
      } else {  
        // pass the result of the call to the closure
        final Closure closure = (Closure)params[numberOfparams];
        
        closure.setDelegate(this);
        return closure.call(new Object[] {result.get(0)});
      }
      
    } catch (final IOException e) {
      throw new XMLRPCCallFailureException(e.getMessage(), new Integer(0));
    } catch (final XMLRPCFailException e) {
      throw new XMLRPCCallFailureException(e.getFaultString(), e.getCause());
    }
  }
  
  private final XMPPConnection connection;
  private final String to;
}
