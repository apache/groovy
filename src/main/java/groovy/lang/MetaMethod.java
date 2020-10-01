/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.lang;

import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ParameterTypes;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.MetaClassHelper;

import java.lang.reflect.Modifier;

/**
 * Represents a Method on a Java object a little like {@link java.lang.reflect.Method}
 * except without using reflection to invoke the method
 */
public abstract class MetaMethod extends ParameterTypes implements Cloneable {
    public static final MetaMethod[] EMPTY_ARRAY = new MetaMethod[0];
    private String signature;
    private String mopName;

    /**
     * Constructor for a metamethod with an empty parameter list
     */
    public MetaMethod() {
    }

    /**
     *Constructor wit a list of parameter classes
     *
     * @param pt A list of parameters types
     */
    public MetaMethod(Class [] pt) {
        super (pt);
    }

    /**
     *Returns the modifiers for this method
     *
     * @return modifiers as an int.
     */
    public abstract int getModifiers();

    /**
     * Returns the name of the method represented by this class
     * 
     * @return name of this method
     */
    public abstract String getName();

    /**
     * Access the return type for this method
     *
     *@return the return type of this method
     */
    public abstract Class getReturnType();

    /**
     * Gets the class where this method is declared
     *
     * @return class of this method
     */
    public abstract CachedClass getDeclaringClass();

    /**
     * Invoke this method
     *
     * @param object The object this method should be invoked on
     * @param arguments The arguments for the method if applicable
     * @return The return value of the invocation
     */
    public abstract Object invoke(Object object, Object[] arguments);

    /**
     * Checks that the given parameters are valid to call this method
     *
     * @param arguments the arguments to check
     * @throws IllegalArgumentException if the parameters are not valid
     */
    public void checkParameters(Class[] arguments) {
        // lets check that the argument types are valid
        if (!isValidMethod(arguments)) {
            throw new IllegalArgumentException(
                    "Parameters to method: "
                    + getName()
                    + " do not match types: "
                    + InvokerHelper.toString(getParameterTypes())
                    + " for arguments: "
                    + InvokerHelper.toString(arguments));
        }
    }

    /**
     *Returns true if this this metamethod represents the same method as the argument.
     *
     * @param method A metaMethod instance
     * @return true if method is for the same method as this method, false otherwise.
     */
    public boolean isMethod(MetaMethod method) {
        return getName().equals(method.getName())
            && getModifiers() == method.getModifiers()
            && getReturnType().equals(method.getReturnType())
            && equal(getParameterTypes(), method.getParameterTypes());
    }

    protected static boolean equal(CachedClass[] a, Class[] b) {
        if (a.length == b.length) {
            for (int i = 0, size = a.length; i < size; i++) {
                if (!a[i].getTheClass().equals(b[i])) {
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

    /**
     * Returns a string representation of this method
     */
    @Override
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

    @Override
    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new GroovyRuntimeException("This should never happen", e);
        }
    }

    /**
     * Returns whether or not this method is static.
     * @return true if this method is static
     */
    public boolean isStatic() {
        return (getModifiers() & Modifier.STATIC) != 0;
    }

    /**
     * Returns whether or not this method is abstract.
     * @return true if this method is abstract
     */
    public boolean isAbstract() {
        return (getModifiers() & Modifier.ABSTRACT) != 0;
    }

    /**
     * Returns whether or not this method is private.
     * @return true if this method is private
     */
    public final boolean isPrivate() {
        return (getModifiers() & Modifier.PRIVATE) != 0;
    }

    /**
     * Returns whether or not this method is protected.
     * @return true if this method is protected
     */
    public final boolean isProtected() {
        return (getModifiers() & Modifier.PROTECTED) != 0;
    }

    /**
     * Returns whether or not this method is public.
     * @return true if this method is public
     */
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

    /**
     * Checks the compatibility between two modifier masks. Checks that they are equal
     * with regards to access and static modifier.
     *
     * @return true if the modifiers are compatible
     */
    private static boolean compatibleModifiers(int modifiersA, int modifiersB) {
        int mask = Modifier.PRIVATE | Modifier.PROTECTED | Modifier.PUBLIC | Modifier.STATIC;
        return (modifiersA & mask) == (modifiersB & mask);
    }

    /**
     * Returns whether this object is cacheable
     */
    public boolean isCacheable() {
        return true;
    }

    /**
     * Return a descriptor of this method based on the return type and parameters of this method.
     */
    public String getDescriptor() {
        return BytecodeHelper.getMethodDescriptor(getReturnType(), getNativeParameterTypes());
    }

    /**
     * Returns the signature of this method
     *
     * @return The signature of this method
     */
    public synchronized String getSignature() {
        if (signature == null) {
            CachedClass [] parameters = getParameterTypes();
            final String name = getName();
            StringBuilder buf = new StringBuilder(name.length()+parameters.length*10);
            buf.append(getReturnType().getName());
            
            buf.append(' ');
            buf.append(name);
            buf.append('(');
            for (int i = 0; i < parameters.length; i++) {
                if (i > 0) {
                    buf.append(", ");
                }
                buf.append(parameters[i].getName());
            }
            buf.append(')');
            signature = buf.toString();
        }
        return signature;
    }

    
    public String getMopName() {
        if (mopName == null) {
          String name = getName();
          CachedClass declaringClass = getDeclaringClass();
          if (Modifier.isPrivate(getModifiers()))
            mopName = "this$" + declaringClass.getSuperClassDistance() + "$" + name;
          else 
            mopName = "super$" + declaringClass.getSuperClassDistance() + "$" + name;
        }
        return mopName;
    }

    /**
     * This method is called when an exception occurs while invoking this method.
     */
    public final RuntimeException processDoMethodInvokeException (Exception e, Object object, Object [] argumentArray) {
//        if (e instanceof IllegalArgumentException) {
//            //TODO: test if this is OK with new MOP, should be changed!
//            // we don't want the exception being unwrapped if it is a IllegalArgumentException
//            // but in the case it is for example a IllegalThreadStateException, we want the unwrapping
//            // from the runtime
//            //Note: the reason we want unwrapping sometimes and sometimes not is that the method
//            // invocation tries to invoke the method with and then reacts with type transformation
//            // if the invocation failed here. This is OK for IllegalArgumentException, but it is
//            // possible that a Reflector will be used to execute the call and then an Exception from inside
//            // the method is not wrapped in a InvocationTargetException and we will end here.
//            boolean setReason = e.getClass() != IllegalArgumentException.class || this instanceof org.codehaus.groovy.reflection.GeneratedMetaMethod;
//            return MetaClassHelper.createExceptionText("failed to invoke method: ", this, object, argumentArray, e, setReason);
//        }

        if (e instanceof RuntimeException)
          return (RuntimeException) e;

        return MetaClassHelper.createExceptionText("failed to invoke method: ", this, object, argumentArray, e, true);
    }

    /**
     * Invokes the method this object represents. This method is not final but it should be overloaded very carefully and only by generated methods
     * there is no guarantee that it will be called
     *
     * @param object The object the method is to be called at.
     * @param argumentArray Arguments for the method invocation.
     * @return The return value of the invoked method.
     */
    public Object doMethodInvoke(Object object, Object[] argumentArray) {
        argumentArray = coerceArgumentsToClasses(argumentArray);
        try {
            return invoke(object, argumentArray);
        } catch (Exception e) {
            throw processDoMethodInvokeException(e, object, argumentArray);
        }
    }
}
