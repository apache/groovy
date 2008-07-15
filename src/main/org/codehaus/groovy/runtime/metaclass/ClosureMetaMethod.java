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
import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.reflection.CachedMethod;

import java.lang.reflect.Modifier;

/**
 *
 * A MetaMethod that accepts a closure in the constructor which is invoked when the MetaMethod is called.
 * The delegate of the closure is set to the instance that the MetaMethod is invoked on when called.
 *
 * @author Graeme Rocher
 * @since 1.1
 */
public class ClosureMetaMethod extends MetaMethod implements ClosureInvokingMethod {

	private final Closure callable;
    private final String name;
    private final CachedClass declaringClass;

    public ClosureMetaMethod(String name, Closure c) {
		this(name, c.getOwner().getClass(), c);
	}

    public ClosureMetaMethod(String name, Class declaringClass,Closure c) {
        super (c.getParameterTypes());
        this.name = name;
        callable = c;
        this.declaringClass = ReflectionCache.getCachedClass(declaringClass);
        c.setResolveStrategy(Closure.DELEGATE_FIRST);
    }


    public int getModifiers() {
        return Modifier.PUBLIC;
    }

    public String getName() {
        return name;
    }

    public Class getReturnType() {
        return Object.class;
    }

	public CachedClass getDeclaringClass() {
		return declaringClass;
	}

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
