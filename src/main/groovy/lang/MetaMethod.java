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
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.reflection.CachedMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Represents a Method on a Java object a little like {@link java.lang.reflect.Method}
 * except without using reflection to invoke the method
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class MetaMethod implements Cloneable {

    private String name;
    private Class callClass;
    private CachedClass declaringClass;
    private Class interfaceClass;
    private Class returnType;
    private int modifiers;
    private Reflector reflector;
    private int methodIndex;
    private CachedMethod method;

    protected ParameterTypes paramTypes;

    public MetaMethod(String name, Class declaringClass, CachedClass[] parameterTypes, Class returnType, int modifiers) {
        this.name = name;
        this.callClass = declaringClass;
        this.declaringClass = ReflectionCache.getCachedClass(declaringClass);
        this.returnType = returnType;
        this.modifiers = modifiers;
        paramTypes = new ParameterTypes(parameterTypes);
    }

    public MetaMethod(CachedMethod method, CachedClass[] parameterTypes) {
        this(
            method.getName(),
            method.getDeclaringClass(),
            parameterTypes,
            method.getReturnType(),
            method.getModifiers());
        this.method = method;
    }

    public MetaMethod(MetaMethod metaMethod) {
        this(metaMethod.method, metaMethod.getParameterTypes());
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

    public Object invoke(Object object, Object[] arguments) {
        try {
            if (reflector != null) {
                return reflector.invoke(this, object, arguments);
            } else {
                AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        method.cachedMethod.setAccessible(true);
                        return null;
                    }
                });
                return method.cachedMethod.invoke(object, arguments);
            }
        } catch (InvocationTargetException ite) {
            throw new InvokerInvocationException(ite.getCause());
        } catch (Exception e) {
            throw new InvokerInvocationException(e);
        }
    }

    public Class getCallClass() {
        return callClass;
    }

    public void setCallClass(Class c) {
        callClass=c;
    }

    public int getMethodIndex() {
        return methodIndex;
    }

    public void setMethodIndex(int methodIndex) {
        this.methodIndex = methodIndex;
    }

    public int getModifiers() {
        return modifiers;
    }

    public String getName() {
        return name;
    }

    public Class getReturnType() {
        return returnType;
    }

    public Reflector getReflector() {
        return reflector;
    }

    public void setReflector(Reflector reflector) {
        this.reflector = reflector;
    }

    public boolean isMethod(Method method) {
        return name.equals(method.getName())
            && modifiers == method.getModifiers()
            && returnType.equals(method.getReturnType())
            && equal(getParameterTypes(), method.getParameterTypes());
    }

    protected boolean equal(CachedClass[] a, Class[] b) {
        if (a.length == b.length) {
            for (int i = 0, size = a.length; i < size; i++) {
                if (!a[i].cachedClass.equals(b[i])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    protected boolean equal(CachedClass[] a, CachedClass[] b) {
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
            + name
            + " params: "
            + InvokerHelper.toString(getParameterTypes())
            + " returns: "
            + returnType
            + " owner: "
            + callClass
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
        return (modifiers & Modifier.STATIC) != 0;
    }

    public boolean isPrivate() {
        return (modifiers & Modifier.PRIVATE) != 0;
    }

    public boolean isProtected() {
        return (modifiers & Modifier.PROTECTED) != 0;
    }

    public boolean isPublic() {
        return (modifiers & Modifier.PUBLIC) != 0;
    }

    /**
     * @param method the method to compare against
     * @return true if the given method has the same name, parameters, return type
     * and modifiers but may be defined on another type
     */
    public boolean isSame(MetaMethod method) {
        return name.equals(method.getName())
            && compatibleModifiers(modifiers, method.getModifiers())
            && returnType.equals(method.getReturnType())
            && equal(getParameterTypes(), method.getParameterTypes());
    }

    protected boolean compatibleModifiers(int modifiersA, int modifiersB) {
        int mask = Modifier.PRIVATE | Modifier.PROTECTED | Modifier.PUBLIC | Modifier.STATIC;
        return (modifiersA & mask) == (modifiersB & mask);
    }

    public Class getInterfaceClass() {
        return interfaceClass;
    }

    public void setInterfaceClass(Class interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public boolean isCacheable() {
        return true;
    }

    public CachedClass getDeclaringClass() {
        return declaringClass;
    }

    public final CachedClass [] getParameterTypes() {
      return paramTypes.getParameterTypes();
    }

    public final ParameterTypes getParamTypes() {
      return paramTypes;
    }

    public Class[] getNativeParameterTypes() {
        return paramTypes.getNativeParameterTypes();
    }
}
