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
import groovy.lang.GroovyObjectSupport;
import groovy.lang.GroovyRuntimeException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.xml.sax.SAXException;

import uk.co.wilson.net.xmlrpc.XMLRPCMessageProcessor;

/**
 * @author John Wilson (tug@wilson.co.uk)
 */
public class XMLRPCServerProxy extends GroovyObjectSupport {
	private URL serverURL;
	private StringBuffer propertyPrefix = new StringBuffer();
	
	public XMLRPCServerProxy(final String serverURL) throws MalformedURLException {
		this.serverURL = new URL(serverURL);
	}
	
	/* (non-Javadoc)
	 * @see groovy.lang.GroovyObject#getProperty(java.lang.String)
	 */
	public Object getProperty(final String property) {
		/**
		 * 
		 * Allow serverProxy.a.b.c(...)
		 * This invokes a remote method with the name "a.b.c"
		 * This technique is shamelessly stolen from the Python XML-RPC implementation
		 * Thanks and credit to Fredrik Lundh
		 * 
		 */

		this.propertyPrefix.append(property).append('.');
		
		return this;
	}
	
	/* (non-Javadoc)
	 * @see groovy.lang.GroovyObject#invokeMethod(java.lang.String, java.lang.Object)
	 */
	public Object invokeMethod(final String name, final Object args) {
	final String methodName = this.propertyPrefix.append(name).toString();
	
		this.propertyPrefix.setLength(0);
	
		if ("invokeMethod".equals(methodName)) return super.invokeMethod(methodName, args);
		
		try {
		final Object[] params = (args instanceof List) ? ((List)args).toArray() : (Object[])args;
		int numberOfparams = params.length;
		
			if (numberOfparams != 0 && params[numberOfparams - 1] instanceof Closure) {
				numberOfparams--;	// the closure is not to be passed to the remote method
			}
			
			final StringBuffer buffer = new StringBuffer("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<methodCall>\n\t<methodName>");
		
			XMLRPCMessageProcessor.encodeString(buffer, methodName).append("</methodName>\n\t<params>\n");
			
			for (int i = 0; i != numberOfparams; i++) {
			final Object param = params[i];
				
				XMLRPCMessageProcessor.emit(buffer.append("\t\t<param>"), param).append("</param>\n");
			}
			
			buffer.append("\t</params>\n</methodCall>\n");
			
//			System.out.println(buffer.toString());
			
			byte [] request = buffer.toString().getBytes("ISO-8859-1");
			final URLConnection connection = this.serverURL.openConnection();
			
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setAllowUserInteraction(false);
			connection.setRequestProperty("Content-Length", Integer.toString(request.length));
			connection.setRequestProperty("Content-Type", "text/xml");
			
			final OutputStream requestStream = connection.getOutputStream();
			requestStream.write(request);
			requestStream.flush();
			requestStream.close();
			
			final XMLRPCMessageProcessor responseParser = new XMLRPCMessageProcessor();
			
			responseParser.parseMessage(connection.getInputStream());
			
			final List response = responseParser.getParams();
			
			if (response == null) throw new GroovyRuntimeException("Empty response from server");
			
			if (numberOfparams == params.length) {
				return response.get(0);
			} else {	
				// pass the result of the call to the closure
				final Closure closure = (Closure)params[numberOfparams];
				
				closure.setDelegate(this);
				return closure.call(new Object[] {response.get(0)});
			}
			
		} catch (final IOException e) {
			e.printStackTrace();
			
			return null;
			
		} catch (final SAXException e) {
			e.printStackTrace();
			
			return null;
		}
	}
}
