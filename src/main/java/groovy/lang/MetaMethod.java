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
import org.codehaus.groovy.runtime.FormatHelper;
import org.codehaus.groovy.runtime.MetaClassHelper;

import java.lang.reflect.Modifier;

/**
 * Represents a Method on a Java object a little like {@link java.lang.reflect.Method}
 * except without using reflection to invoke the method
 */
public abstract class MetaMethod extends ParameterTypes implements MetaMember, Cloneable {

    public static final MetaMethod[] EMPTY_ARRAY = new MetaMethod[0];
    private String signature;
    private String mopName;

    /**
     * Constructor for a metamethod with an empty parameter list.
     */
    public MetaMethod() {
    }

    /**
     * Constructor with a list of parameter classes.
     *
     * @param pt A list of parameters types
     */
    public MetaMethod(Class [] pt) {
        super (pt);
    }

    /**
     * Returns the modifiers of this method.
     *
     * @return modifiers as an int.
     */
    public abstract int getModifiers();

    /**
     * Returns the name of this method.
     *
     * @return name of this method
     */
    public abstract String getName();

    /**
     * Returns the return type for this method.
     *
     *@return the return type of this method
     */
    public abstract Class getReturnType();

    /**
     * Gets the class where this method is declared.
     *
     * @return class of this method
     */
    public abstract CachedClass getDeclaringClass();

    /**
     * Checks that the given parameters are valid to call this method.
     *
     * @param arguments the arguments to check
     * @throws IllegalArgumentException if the parameters are not valid
     * @deprecated
     */
    @Deprecated
    public void checkParameters(Class[] arguments) {
        // let's check that the argument types are valid
        if (!isValidMethod(arguments)) {
            throw new IllegalArgumentException(
                    "Parameters to method: "
                    + getName()
                    + " do not match types: "
                    + FormatHelper.toString(getParameterTypes())
                    + " for arguments: "
                    + FormatHelper.toString(arguments));
        }
    }

    /**
     * Returns true if this metamethod represents the same method as the argument.
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

    @Override
    public String toString() {
        return super.toString()
            + "[name: "
            + getName()
            + " params: "
            + FormatHelper.toString(getParameterTypes())
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
     * Returns whether this method is abstract.
     * @return true if this method is abstract
     */
    public boolean isAbstract() {
        return (getModifiers() & Modifier.ABSTRACT) != 0;
    }

    /**
     * Returns whether this method is interface-default.
     * @return true if this method is default
     */
    public boolean isDefault() {
        return (getModifiers() & (Modifier.ABSTRACT | Modifier.PUBLIC | Modifier.STATIC)) == Modifier.PUBLIC && getDeclaringClass().isInterface;
    }

    /**
     * Determines if the given method has the same name, parameters, return type
     * and modifiers but may be defined on another type.
     *
     * @param method the method to compare against
     */
    public final boolean isSame(MetaMethod method) {
        return getName().equals(method.getName())
            && compatibleModifiers(getModifiers(), method.getModifiers())
            && getReturnType().equals(method.getReturnType())
            && equal(getParameterTypes(), method.getParameterTypes());
    }

    /**
     * Checks the compatibility between two modifier masks. Checks that they are
     * equal in regard to access and static modifier.
     *
     * @return true if the modifiers are compatible
     */
    private static boolean compatibleModifiers(int modifiersA, int modifiersB) {
        int mask = Modifier.PRIVATE | Modifier.PROTECTED | Modifier.PUBLIC | Modifier.STATIC;
        return (modifiersA & mask) == (modifiersB & mask);
    }

    /**
     * Returns whether this object is cacheable.
     */
    public boolean isCacheable() {
        return true;
    }

    /**
     * Returns a descriptor of this method based on the return type and parameters of this method.
     */
    public String getDescriptor() {
        return BytecodeHelper.getMethodDescriptor(getReturnType(), getNativeParameterTypes());
    }

    /**
     * Returns the signature of this method.
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
            mopName = (isPrivate() ? "this" : "super") + '$' + getDeclaringClass().getSuperClassDistance() + '$' + getName();
        }
        return mopName;
    }

    /**
     * Invokes this method.
     *
     * @param object The object this method should be invoked on
     * @param arguments The arguments for the method if applicable
     * @return The return value of the invocation
     */
    public abstract Object invoke(Object object, Object[] arguments);

    /**
     * Invokes the method this object represents.
     * <p>
     * This method is not final but it should be overloaded very carefully and
     * only by generated methods there is no guarantee that it will be called.
     *
     * @param object The object the method is to be called at.
     * @param arguments Arguments for the method invocation.
     * @return The return value of the invoked method.
     */
    public Object doMethodInvoke(final Object object, Object[] arguments) {
        arguments = coerceArgumentsToClasses(arguments);
        try {
            return invoke(object, arguments);
        } catch (Exception e) {
            throw processDoMethodInvokeException(e, object, arguments);
        }
    }

    /**
     * Called when an exception occurs while invoking this method.
     */
    public final RuntimeException processDoMethodInvokeException(final Exception e, final Object object, final Object [] arguments) {
        /*
        if (e instanceof IllegalArgumentException) {
            //TODO: test if this is OK with new MOP, should be changed!
            // we don't want the exception being unwrapped if it is a IllegalArgumentException
            // but in the case it is for example a IllegalThreadStateException, we want the unwrapping
            // from the runtime
            //Note: the reason we want unwrapping sometimes and sometimes not is that the method
            // invocation tries to invoke the method with and then reacts with type transformation
            // if the invocation failed here. This is OK for IllegalArgumentException, but it is
            // possible that a Reflector will be used to execute the call and then an Exception from inside
            // the method is not wrapped in a InvocationTargetException and we will end here.
            boolean setReason = e.getClass() != IllegalArgumentException.class || this instanceof org.codehaus.groovy.reflection.GeneratedMetaMethod;
            return MetaClassHelper.createExceptionText("failed to invoke method: ", this, object, argumentArray, e, setReason);
        }
        */
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return MetaClassHelper.createExceptionText("failed to invoke method: ", this, object, arguments, e, true);
    }
}
