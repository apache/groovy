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

import org.codehaus.groovy.classgen.BytecodeHelper;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ParameterTypes;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.MetaClassHelper;

import java.lang.reflect.Modifier;

/**
 * Represents a Method on a Java object a little like {@link java.lang.reflect.Method}
 * except without using reflection to invoke the method
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Alex Tkachman
 * @version $Revision$
 */
public abstract class MetaMethod extends ParameterTypes implements Cloneable {
    private String signature;
    private String mopName;

    public MetaMethod() {
    }

    public MetaMethod(Class [] pt) {
        super (pt);
    }

    public abstract int getModifiers();

    public abstract String getName();

    public abstract Class getReturnType();

    public abstract CachedClass getDeclaringClass();

    public abstract Object invoke(Object object, Object[] arguments);

    /**
     * Checks that the given parameters are valid to call this method
     *
     * @param arguments the arguments to check
     * @throws IllegalArgumentException if the parameters are not valid
     */
    public void checkParameters(Class[] arguments) {
        // lets check that the argument types are valid
        if (!MetaClassHelper.isValidMethod(getParameterTypes(), arguments)) {
            throw new IllegalArgumentException(
                    "Parameters to method: "
                    + getName()
                    + " do not match types: "
                    + InvokerHelper.toString(getParameterTypes())
                    + " for arguments: "
                    + InvokerHelper.toString(arguments));
        }
    }
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

    public boolean isAbstract() {
        return (getModifiers() & Modifier.ABSTRACT) != 0;
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

    public String getDescriptor() {
        return BytecodeHelper.getMethodDescriptor(getReturnType(), getNativeParameterTypes());
    }

    public synchronized String getSignature() {
        if (signature == null) {
            CachedClass [] parameters = getParameterTypes();
            final String name = getName();
            StringBuffer buf = new StringBuffer(name.length()+parameters.length*10);
            buf.append(getReturnType().getName());
            //
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
          if ((getModifiers() & (Modifier.PUBLIC| Modifier.PROTECTED)) == 0)
            mopName = new StringBuffer().append("this$").append(declaringClass.getSuperClassDistance()).append("$").append(name).toString();
          else
            mopName = new StringBuffer().append("super$").append(declaringClass.getSuperClassDistance()).append("$").append(name).toString();
        }
        return mopName;
    }

    protected final RuntimeException processDoMethodInvokeException (Exception e, Object object, Object [] argumentArray) {
        if (e instanceof IllegalArgumentException) {
            //TODO: test if this is ok with new MOP, should be changed!
            // we don't want the exception being unwrapped if it is a IllegalArgumentException
            // but in the case it is for example a IllegalThreadStateException, we want the unwrapping
            // from the runtime
            //Note: the reason we want unwrapping sometimes and sometimes not is that the method
            // invokation tries to invoke the method with and then reacts with type transformation
            // if the invokation failed here. This is ok for IllegalArgumentException, but it is
            // possible that a Reflector will be used to execute the call and then an Exception from inside
            // the method is not wrapped in a InvocationTargetException and we will end here.
            boolean setReason = e.getClass() != IllegalArgumentException.class;
            return MetaClassHelper.createExceptionText("failed to invoke method: ", this, object, argumentArray, e, setReason);
        }

        if (e instanceof RuntimeException)
          return (RuntimeException) e;

        return MetaClassHelper.createExceptionText("failed to invoke method: ", this, object, argumentArray, e, true);
    }

    public Object doMethodInvoke(Object object, Object[] argumentArray) {
        argumentArray = coerceArgumentsToClasses(argumentArray);
        try {
            return invoke(object, argumentArray);
        } catch (Exception e) {
            throw processDoMethodInvokeException(e, object, argumentArray);
        }
    }
}
