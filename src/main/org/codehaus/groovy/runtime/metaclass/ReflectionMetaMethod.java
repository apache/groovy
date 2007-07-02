/*
 * Copyright 2003-2007 the original author or authors.
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
 */
package org.codehaus.groovy.runtime.metaclass;

import groovy.lang.MetaMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.codehaus.groovy.runtime.InvokerInvocationException;

public class ReflectionMetaMethod extends MetaMethod {
    private Method method;
    boolean alreadySetAccessible;

    public ReflectionMetaMethod(Method method) {
        super(method);
        this.method = method;
    }

    public Object invoke(Object object, Object[] arguments) {
    	if ( !alreadySetAccessible ) {
	    	AccessController.doPrivileged(new PrivilegedAction() {
	    		public Object run() {
	    			method.setAccessible(true);
	                return null;
	    		}
	    	});
	    	alreadySetAccessible = true;
    	}

        //        System.out.println("About to invoke method: " + method);
        //        System.out.println("Object: " + object);
        //        System.out.println("Using arguments: " + InvokerHelper.toString(arguments));

        try {
            return method.invoke(object, arguments);
        } catch (IllegalArgumentException e) {
            throw new InvokerInvocationException(e);
        } catch (IllegalAccessException e) {
            throw new InvokerInvocationException(e);
        } catch (InvocationTargetException e) {
            throw new InvokerInvocationException(e);
        }
    }
}
