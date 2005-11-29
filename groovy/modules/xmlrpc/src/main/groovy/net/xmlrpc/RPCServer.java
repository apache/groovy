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
import groovy.lang.GString;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.GroovyRuntimeException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import uk.co.wilson.net.xmlrpc.XMLRPCFailException;

/**
 * @author John Wilson
 *
 */

public class RPCServer extends GroovyObjectSupport {
  protected final Map registeredMethods = Collections.synchronizedMap(new HashMap());
  protected Closure defaultMethod = null;
  protected Closure preCallMethod = null;
  protected Closure postCallMethod = null;
  protected Closure faultMethod = null;

  public Object getProperty(final String property) {
  	return new GroovyObjectSupport() {
  		/**
  		 * 
  		 * Allow server.a.b.c = {....}
  		 * This deefines a remote method with the name "a.b.c"
  		 * This technique is shamelessly stolen from the Python XML-RPC implementation
  		 * Thanks and credit to Fredrik Lundh
  		 * 
  		 */
  				
  		private final StringBuffer propertyPrefix = new StringBuffer(property + ".");
  	
  		public Object getProperty(final String property) {
  			this.propertyPrefix.append(property).append('.');
  			
  			return this;
  		}
  		
  		public void setProperty(final String name, final Object args) {
  			RPCServer.this.setProperty(this.propertyPrefix + name, name, args);
  		}
  	};
  }

  public void setProperty(final String methodName, final Object method) {
  	setProperty(methodName, methodName, method);
  }

  private void setProperty(final String methodName, final String javaMethodName, final Object method) {
  Closure closure = null;
  
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
  	//
  	// calling a static method on a class
  	//
  		
  	final int numberofParameters = getNumberOfParameters(Modifier.PUBLIC | Modifier.STATIC, ((Class)method).getMethods(), javaMethodName);			
  	
  		if (numberofParameters != -1) {
  			closure = makeMethodProxy(javaMethodName, numberofParameters, ((Class)method).getName());
  		} else {
  			throw new GroovyRuntimeException("No static method "
  					                         + javaMethodName
  										    + " on class "
  											+ ((Class)method).getName());
  		}
  	} else {
  	//
  	// calling a method on an instance of a class
  	//
  		
  	final int numberofParameters = getNumberOfParameters(Modifier.PUBLIC, method.getClass().getMethods(), javaMethodName);			
  	
  		if (numberofParameters != -1) {
  			closure = makeMethodProxy(javaMethodName, numberofParameters, "delegate");
  			closure.setDelegate(method);
  		} else {
  			throw new GroovyRuntimeException("No method "
                        + javaMethodName
  				    + " on class "
  					+ method.getClass().getName());
  		}
  	}
  	
  	this.registeredMethods.put(methodName, closure);
  }

  private int getNumberOfParameters(final int type, final Method methods[], final String property) {
  boolean foundMatch = false;
  int numberofParameters = -1;
  
  	for (int i = 0; i != methods.length; i++) {
  		if ((methods[i].getModifiers() & type) == type) {
  			if (methods[i].getName().equals(property)) {
  				if (foundMatch) {
  					if (numberofParameters != methods[i].getParameterTypes().length) {
  						throw new GroovyRuntimeException("More than one methods "
  		                        + property
  							    + " on class "
  								+ methods[i].getDeclaringClass().getName()
  								+ " with different numbers of parameters");
  					}
  				} else {
  					foundMatch = true;
  					numberofParameters = methods[i].getParameterTypes().length;
  				}
  			}
  		}
  	}
  	
  	return numberofParameters;
  }

  private Closure makeMethodProxy(final String methodName, final int numberOfParameters, final String qualifier) {
  final String paramIn, paramOut;
  
  	if (numberOfParameters == 0) {
  		paramIn = paramOut = "";
  	} else {
  	final StringBuffer params = new StringBuffer();
  	
  		for (int i = 0; i != numberOfParameters; i++) {
  			params.append(", p" + i);
  		}
  		
  		paramOut = params.delete(0, 2).toString();
  		paramIn = paramOut + " -> ";
  	}
  	
  final String generatedCode = "class X { public def closure = {" + paramIn + " " + qualifier + "." + methodName + "(" + paramOut + ") }}";
  //     System.out.println(generatedCode);
  
  	try {
  	final InputStream in = new ByteArrayInputStream(generatedCode.getBytes());
  	final GroovyObject groovyObject = (GroovyObject)new GroovyClassLoader().parseClass(in, methodName).newInstance();
  		
  		return (Closure)(groovyObject.getProperty("closure"));
  	} catch (Exception e) {
  		throw new GroovyRuntimeException("Can't generate proxy for XML-RPC method " + methodName, e);
  	}
  }

  /**
   * 
   * Convenience method to be called by closures executing remote calls
   * Called when the closure wants to return a fault
   * The method always throws an exception
   * 
   * @param msg Fault message to be returned to the caller
   * @param code Fault code to be returned to the caller
   */
  public void returnFault(String msg, int code) throws XMLRPCFailException {
  	throw new XMLRPCFailException(msg, code);
  }

  /**
   * 
   * Convenience method to be called by closures executing remote calls
   * Called when the closure wants to return a fault
   * The method always throws an exception
   * 
   * @param msg Fault message to be returned to the caller
   * @param code Fault code to be returned to the caller
   */
  public void returnFault(GString msg, int code) throws XMLRPCFailException {
  	returnFault(msg.toString(), code);	// sometimes Groovy doesn't do the cconversion to String 
  }

  /**
   * Supply a closure to be called if there is no closure supplied to handle the call
   * Typically this logs the bad call and returns a fault by calling returnFault
   * 
   * The closure is called with two parameters - a String containing the method
   * name and an array of Object containing the parameters
   * 
   * @param defaultMethod The closure to be called - if this is null the setting is not changed
   */
  public void setupDefaultMethod(final Closure defaultMethod) {
  	if (defaultMethod != null) this.defaultMethod = defaultMethod;
  }

  /**
   * Supply a closure to be called before the closure which handles the remote call
   *  (or the default closure if there is no handler) is called.
   * 
   * The closure is called with two parameters - a String containing the method
   * name and an array of Object containing the parameters
   * 
   * @param preCallMethod The closure to be called - if this is null the setting is not changed
   */
  public void setupPreCallMethod(final Closure preCallMethod) {
  	if (preCallMethod != null) this.preCallMethod = preCallMethod;
  }

  /**
   * Supply a closure to be called after the closure which handles the remote call
   *  (or the default closure if there is no handler) is called.
   * 
   * The closure is called with two parameters - a String containing the method
   * name and an Object containing the result
   * 
   * @param postCallMethod The closure to be called - if this is null the setting is not changed
   */
  public void setupPostCallMethod(final Closure postCallMethod) {
  	if (postCallMethod != null) this.postCallMethod = postCallMethod;
  }

  /**
   * Supply a closure to be called if the process of executing the remote call throws an exception.
   * 
   * The closure is called with two parameters - a String containing the fault string
   * name and an Integer containing the fault value.
   * The name of the method being called is not passed as the fault could have been 
   * generated before the method name was known.
   * 
   * @param faultMethod The closure to be called - if this is null the setting is not changed
   */
  public void setupFaultMethod(final Closure faultMethod) {
  	if (faultMethod != null) this.faultMethod = faultMethod;
  }

}
