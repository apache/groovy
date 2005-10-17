/*
 * $Id$
 *
 * Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.
 *
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met:
 *  1. Redistributions of source code must retain copyright statements and
 * notices. Redistributions must also contain a copy of this document.
 *  2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  3. The name "groovy" must not be used to endorse or promote products
 * derived from this Software without prior written permission of The Codehaus.
 * For written permission, please contact info@codehaus.org.
 *  4. Products derived from this Software may not be called "groovy" nor may
 * "groovy" appear in their names without prior written permission of The
 * Codehaus. "groovy" is a registered trademark of The Codehaus.
 *  5. Due credit should be given to The Codehaus - http://groovy.codehaus.org/
 *
 * THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *
 */
package groovy.lang;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Logger;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.Reflector;

/**
 * Represents a Method on a Java object a little like {@link java.lang.reflect.Method}
 * except without using reflection to invoke the method
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class MetaMethod implements Cloneable {

    private static final Logger log = Logger.getLogger(MetaMethod.class.getName());

    private String name;
    private Class declaringClass;
    private Class interfaceClass;
    private Class[] parameterTypes;
    private Class returnType;
    private int modifiers;
    private Reflector reflector;
    private int methodIndex;
    private Method method;

    public MetaMethod(String name, Class declaringClass, Class[] parameterTypes, Class returnType, int modifiers) {
        this.name = name;
        this.declaringClass = declaringClass;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
        this.modifiers = modifiers;
    }

    public MetaMethod(Method method) {
        this(
            method.getName(),
            method.getDeclaringClass(),
            method.getParameterTypes(),
            method.getReturnType(),
            method.getModifiers());
        this.method = method;
    }

    public MetaMethod(MetaMethod metaMethod) {
        this(metaMethod.method);
    }

    /**
     * Checks that the given parameters are valid to call this method
     * 
     * @param arguments
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
    
    public Object invoke(Object object, Object[] arguments) throws Exception {
        if (reflector != null) {
            return reflector.invoke(this, object, arguments);
        }
        else {
            AccessController.doPrivileged(new PrivilegedAction() {
	    			public Object run() {
	    			    method.setAccessible(true);
	    			    return null;
	    			}
	    		});
            return method.invoke(object, arguments);
        }
    }

    public Class getDeclaringClass() {
        return declaringClass;
    }
    
    public void setDeclaringClass(Class c) {
        declaringClass=c;
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

    public Class[] getParameterTypes() {
        return parameterTypes;
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
            && equal(parameterTypes, method.getParameterTypes());
    }

    protected boolean equal(Class[] a, Class[] b) {
        if (a.length == b.length) {
            for (int i = 0, size = a.length; i < size; i++) {
                if (!a[i].equals(b[i])) {
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
            + InvokerHelper.toString(parameterTypes)
            + " returns: "
            + returnType
            + " owner: "
            + declaringClass
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
     * @return true if the given method has the same name, parameters, return type
     * and modifiers but may be defined on another type
     */
    public boolean isSame(MetaMethod method) {
        return name.equals(method.getName())
            && compatibleModifiers(modifiers, method.getModifiers())
            && returnType.equals(method.getReturnType())
            && equal(parameterTypes, method.getParameterTypes());
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

}
