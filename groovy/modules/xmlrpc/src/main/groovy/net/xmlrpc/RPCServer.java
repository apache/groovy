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
import groovy.lang.GString;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.GroovyRuntimeException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import uk.co.wilson.net.MinMLSocketServer;
import uk.co.wilson.net.xmlrpc.XMLRPCFailException;

/**
 * @author John Wilson
 *
 */

public class RPCServer extends GroovyObjectSupport {
  protected MinMLSocketServer server = null;
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
   * Starts the server shutdown process
   * This will return before the server has shut down completely
   * Full shutdown may take some time
   * 
   * @throws IOException
   */
  public void stopServer() throws IOException {
  	this.server.shutDown();
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
