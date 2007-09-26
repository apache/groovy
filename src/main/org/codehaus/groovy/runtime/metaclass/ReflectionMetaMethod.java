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
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ParameterTypes;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.codehaus.groovy.runtime.InvokerInvocationException;

public class ReflectionMetaMethod extends MetaMethod {
    protected final CachedMethod method;

    protected ReflectionMetaMethod(CachedMethod method) {
          this.method = method;
      }

    public int getModifiers() {
        return method.getModifiers();
    }

    public String getName() {
        return method.getName();
    }

    public Class getReturnType() {
        return method.getReturnType();
    }

    public CachedClass getDeclaringClass() {
        return method.cachedClass;
    }

    public ParameterTypes getParamTypes() {
        return method;
    }

    public Object invoke(Object object, Object[] arguments) {
        try {
            return method.invokeByReflection(object, arguments);
        } catch (IllegalArgumentException e) {
            throw new InvokerInvocationException(e);
        } catch (IllegalAccessException e) {
            throw new InvokerInvocationException(e);
        } catch (InvocationTargetException e) {
            throw new InvokerInvocationException(e);
        }
    }

    private static HashMap cache = new HashMap();
    public synchronized static ReflectionMetaMethod createReflectionMetaMethod(CachedMethod element) {
        ReflectionMetaMethod method = (ReflectionMetaMethod) cache.get(element);
        if (method == null) {
            method = new ReflectionMetaMethod(element);
            cache.put(element, method);
        }
        return method;
    }
}
