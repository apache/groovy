/*
 * Copyright 2004-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package org.codehaus.groovy.grails.metaclass;

/**
 * <p>Dynamic method invocation callback interface. Implementation classes
 * can add a persistence functionality to Grails like save and delete.
 * 
 * @author Steven Devijver
 * @since Aug 7, 2005
 */
public interface DynamicMethodInvocation {

	/**
	 * <p>Returns the method name for this method implementation.
	 * 
	 * @return method name
	 */
	public String getMethodName();
	
	/**
	 * <p>Invokes the actual method. The target object and arguments are supplied.
	 * 
	 * @param target the target on which the method is invoked.
	 * @param argumetns the arguments passed in the method call
	 * @param the return value of the dynamic method invocation.
	 */
	public Object invoke(Object target, Object[] arguments);
}
