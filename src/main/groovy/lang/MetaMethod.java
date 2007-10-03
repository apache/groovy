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

package groovy.lang;

import org.codehaus.groovy.runtime.*;
import org.codehaus.groovy.reflection.ParameterTypes;
import org.codehaus.groovy.reflection.CachedClass;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Represents a Method on a Java object a little like {@link java.lang.reflect.Method}
 * except without using reflection to invoke the method
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public abstract class MetaMethod implements Cloneable {

    public MetaMethod() {
    }

    public abstract int getModifiers();

    public abstract String getName();

    public abstract Class getReturnType();

    public abstract CachedClass getDeclaringClass();

    public abstract ParameterTypes getParamTypes();

    public abstract Object invoke(Object object, Object[] arguments);

    public final CachedClass [] getParameterTypes() {
        return getParamTypes().getParameterTypes();
    }

    /**
     * Checks that the given parameters are valid to call this method
     *
     * @param arguments the arguments to check
     * @throws IllegalArgumentException if the parameters are not valid
     */
    public void checkParameters(Class[] arguments) {
        // lets check that the argument types are valid
        if (!MetaClassHelper.isValidMethod(getParameterTypes(), arguments, false)) {
            throw new IllegalArgumentException(
                    "Parameters to method: "
                    + getName()
                    + " do not match types: "
                    + InvokerHelper.toString(getParameterTypes())
                    + " for arguments: "
                    + InvokerHelper.toString(arguments));
        }
    }
    public boolean isMethod(Method method) {
        return getName().equals(method.getName())
            && getModifiers() == method.getModifiers()
            && getReturnType().equals(method.getReturnType())
            && equal(getParameterTypes(), method.getParameterTypes());
    }

    protected static boolean equal(CachedClass[] a, Class[] b) {
        if (a.length == b.length) {
            for (int i = 0, size = a.length; i < size; i++) {
                if (!a[i].getCachedClass().equals(b[i])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    protected static boolean equal(CachedClass[] a, CachedClass[] b) {
        if (a.length == b.length) {
            for (int i = 0, size = a.length; i < size; i++) {
                if (a[i] != b[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public String toString() {
        return super.toString()
            + "[name: "
            + getName()
            + " params: "
            + InvokerHelper.toString(getParameterTypes())
            + " returns: "
            + getReturnType()
            + " owner: "
            + getDeclaringClass()
            + "]";
    }

    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new GroovyRuntimeException("This should never happen", e);
        }
    }

    public boolean isStatic() {
        return (getModifiers() & Modifier.STATIC) != 0;
    }

    public final boolean isPrivate() {
        return (getModifiers() & Modifier.PRIVATE) != 0;
    }

    public final boolean isProtected() {
        return (getModifiers() & Modifier.PROTECTED) != 0;
    }

    public final boolean isPublic() {
        return (getModifiers() & Modifier.PUBLIC) != 0;
    }

    /**
     * @param method the method to compare against
     * @return true if the given method has the same name, parameters, return type
     * and modifiers but may be defined on another type
     */
    public final boolean isSame(MetaMethod method) {
        return getName().equals(method.getName())
            && compatibleModifiers(getModifiers(), method.getModifiers())
            && getReturnType().equals(method.getReturnType())
            && equal(getParameterTypes(), method.getParameterTypes());
    }

    private static boolean compatibleModifiers(int modifiersA, int modifiersB) {
        int mask = Modifier.PRIVATE | Modifier.PROTECTED | Modifier.PUBLIC | Modifier.STATIC;
        return (modifiersA & mask) == (modifiersB & mask);
    }

    public boolean isCacheable() {
        return true;
    }

    public final Class[] getNativeParameterTypes() {
        return getParamTypes().getNativeParameterTypes();
    }
}
