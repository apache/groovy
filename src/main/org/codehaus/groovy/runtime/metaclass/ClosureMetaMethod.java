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

import groovy.lang.Closure;
import groovy.lang.ClosureInvokingMethod;
import org.codehaus.groovy.runtime.NewInstanceMetaMethod;
import org.codehaus.groovy.reflection.ParameterTypes;

import java.lang.reflect.Modifier;

/**
 *
 * A MetaMethod that accepts a closure in the constructor which is invoked when the MetaMethod is called.
 * The delegate of the closure is set to the instance that the MetaMethod is invoked on when called.
 *
 * @author Graeme Rocher
 * @since 1.1
 */
public class ClosureMetaMethod extends NewInstanceMetaMethod implements ClosureInvokingMethod {

	private final Closure callable;
	private Class declaringClass;

	public ClosureMetaMethod(String name, Closure c) {
		this(name, c.getOwner().getClass(), c);
	}

    /**
     *
     * @param name The name of the MetaMethod
     * @param declaringClass The class which declared the MetaMethod
     * @param c The closure that this ClosureMetaMethod will invoke when called
     */
    public ClosureMetaMethod(String name, Class declaringClass,Closure c) {
		super(name, declaringClass, c.getParameterTypes() == null ? new Class[0] : c.getParameterTypes(), Object.class,Modifier.PUBLIC);
        Class[] pt = c.getParameterTypes();
		if(pt == null) {
			pt = new Class[0];
		}
        paramTypes = new ParameterTypes(pt);
		this.callable = c;

		this.declaringClass = declaringClass;
	}


	/* (non-Javadoc)
	 * @see org.codehaus.groovy.runtime.NewInstanceMetaMethod#getDeclaringClass()
	 */
	public Class getDeclaringClass() {
		return declaringClass;
	}

	/* (non-Javadoc)
	 * @see groovy.lang.MetaMethod#invoke(java.lang.Object, java.lang.Object[])
	 */
	public Object invoke(final Object object, final Object[] arguments) {
		Closure cloned = (Closure) callable.clone();
		cloned.setDelegate(object);

		return cloned.call(arguments);
	}

  /**
     * Retrieves the closure that is invoked by this MetaMethod
     *
     * @return The closure
     */
    public Closure getClosure() {
		return callable;
	}
}
