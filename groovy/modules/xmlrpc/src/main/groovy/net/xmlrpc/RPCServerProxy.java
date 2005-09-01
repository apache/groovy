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

import uk.co.wilson.net.xmlrpc.XMLRPCFailException;
import uk.co.wilson.net.xmlrpc.XMLRPCMessageProcessor;
import groovy.lang.GroovyObjectSupport;

/**
 * @author John Wilson
 *
 */

public class RPCServerProxy extends GroovyObjectSupport {

  public Object getProperty(final String property) {
  	return new GroovyObjectSupport() {
  		/**
  		 * 
  		 * Allow serverProxy.a.b.c(...)
  		 * This invokes a remote method with the name "a.b.c"
  		 * This technique is shamelessly stolen from the Python XML-RPC implementation
  		 * Thanks and credit to Fredrik Lundh
  		 * 
  		 */
  				
  		private final StringBuffer propertyPrefix = new StringBuffer(property + ".");
  	
  		public Object getProperty(final String property) {
  			this.propertyPrefix.append(property).append('.');
  			
  			return this;
  		}
  		
  		public Object invokeMethod(final String name, final Object args) {
  			return RPCServerProxy.this.invokeMethod(this.propertyPrefix + name, args);
  		}
  	};
  }

  protected String createCall(final String name, final Object[] params, final int numberOfparams, final String header) {
  final StringBuffer buffer = new StringBuffer(header).append("<methodCall>\n\t<methodName>");
  
    try {
      XMLRPCMessageProcessor.encodeString(buffer, name).append("</methodName>\n\t<params>\n");
      
      for (int i = 0; i != numberOfparams; i++) {
        final Object param = params[i];
        XMLRPCMessageProcessor.emit(buffer.append("\t\t<param>"), param).append("</param>\n");
      }
    }
    catch (final XMLRPCFailException e) {
      throw new XMLRPCCallFailureException(e.getFaultString(), e.getCause());
    }
    
    return buffer.append("\t</params>\n</methodCall>\n").toString();
    }
}
