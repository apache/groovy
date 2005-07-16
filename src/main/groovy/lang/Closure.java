/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package groovy.lang;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;

import java.util.*;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Represents any closure object in Groovy.
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author <a href="mailto:tug@wilson.co.uk">John Wilson</a>
 * @version $Revision$
 */
public abstract class Closure extends GroovyObjectSupport implements Cloneable, Runnable {

    private static final Object noParameters[] = new Object[]{null};
    private static final Object emptyArray[] = new Object[0];
    private static final Object emptyArrayParameter[] = new Object[]{emptyArray};

    private Object delegate;
    private final Object owner;
    private final Method doCallMethod;
    private final HashMap callsMap;
    private final boolean supportsVarargs;
    private final Class[] parameterTypes;
    private final int numberOfParameters;
    private Object curriedParams[] = emptyArray;


    private int directive = 0;
    public static int DONE = 1;
    public static int SKIP = 2;

    public Closure(Object delegate) {
        this.delegate = delegate;
        this.owner = delegate;

        Class closureClass = this.getClass();
        callsMap = new HashMap();
        int paramLenTemp = -1;
        Method doCallTemp = null;

        while (true) {
            final Class clazz = closureClass;
            final Method[] methods = (Method[]) AccessController.doPrivileged(new  PrivilegedAction() {
                public Object run() {
                    return clazz.getDeclaredMethods();
                }
            });

            int i = 0;

            for (int j = 0; j < methods.length; j++) {
                 if ("doCall".equals(methods[j].getName())) {
                     callsMap.put(new Integer(methods[j].getParameterTypes().length), methods[j]);
                     if (methods[j].getParameterTypes().length > paramLenTemp) {
                         doCallTemp = methods[j];
                         paramLenTemp = methods[j].getParameterTypes().length;
                     }
                 }
            }

            if (!callsMap.isEmpty()) {
                break;
            }

            closureClass = closureClass.getSuperclass();
        }

        this.doCallMethod = doCallTemp;

        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                for (Iterator iter = callsMap.values().iterator(); iter.hasNext(); ) {
                   ((Method) iter.next()).setAccessible(true);
                }
                return null;
            }
        });

        this.parameterTypes = this.doCallMethod.getParameterTypes();
        this.numberOfParameters = this.parameterTypes.length;

        if (this.numberOfParameters > 0) {
            this.supportsVarargs = this.parameterTypes[this.numberOfParameters - 1].equals(Object[].class);
        } else {
            this.supportsVarargs = false;
        }
    }

    public Object invokeMethod(String method, Object arguments) {
        if ("doCall".equals(method) || "call".equals(method)) {
            if (arguments instanceof Object[]) {
                Object[] objs = (Object[]) arguments;
                if ((objs != null) && (objs.length > 1) && (objs[0] instanceof Object[])) {
                   boolean allNull = true;
                   for (int j = 1; j < objs.length; j++) {
                       if (objs[j] != null) {
                           allNull = false;
                           break;
                       }
                   }
                   if (allNull)
                       return callViaReflection((Object[]) (objs[0]));
                }
            }
            return callSpecial(arguments);
        } else if ("curry".equals(method)) {
            return curry((Object[]) arguments);
        } else {
            try {
                return getMetaClass().invokeMethod(this, method, arguments);
            } catch (MissingMethodException e) {
                if (owner != this) {
                    try {
                        // lets try invoke method on the owner
                        return InvokerHelper.invokeMethod(this.owner, method, arguments);
                    } catch (InvokerInvocationException iie) {
                        throw iie;
                    } catch (GroovyRuntimeException e1) {
                        if (this.delegate != null && this.delegate != this && this.delegate != this.owner) {
                            // lets try invoke method on the delegate
                            try {
                                return InvokerHelper.invokeMethod(this.delegate, method, arguments);
                            } catch (MissingMethodException mme) {
                                throw new InvokerInvocationException(mme);
                            } catch (GroovyRuntimeException gre) {
                                throw new InvokerInvocationException(gre.getCause());
                            }
                        }
                    }
                }
                throw e;
            }
        }

    }

    public Object getProperty(String property) {
        if ("delegate".equals(property)) {
            return getDelegate();
        } else if ("owner".equals(property)) {
            return getOwner();
        } else if ("method".equals(property)) {
            return getMethod();
        } else if ("parameterTypes".equals(property)) {
            return getParameterTypes();
        } else if ("metaClass".equals(property)) {
            return getMetaClass();
        } else if ("class".equals(property)) {
            return getClass();
        } else {
            try {
// lets try getting the property on the owner
                return InvokerHelper.getProperty(this.owner, property);
            } catch (GroovyRuntimeException e1) {
                if (this.delegate != null && this.delegate != this && this.delegate != this.owner) {
                    try {
// lets try getting the property on the delegate
                        return InvokerHelper.getProperty(this.delegate, property);
                    } catch (GroovyRuntimeException e2) {
// ignore, we'll throw e1
                    }
                }

                throw e1;
            }
        }
    }

    public void setProperty(String property, Object newValue) {
        if ("delegate".equals(property)) {
            setDelegate(newValue);
        } else if ("metaClass".equals(property)) {
            setMetaClass((MetaClass) newValue);
        } else {
            try {
// lets try setting the property on the owner
                InvokerHelper.setProperty(this.owner, property, newValue);
                return;
            } catch (GroovyRuntimeException e1) {
                if (this.delegate != null && this.delegate != this && this.delegate != this.owner) {
                    try {
// lets try setting the property on the delegate
                        InvokerHelper.setProperty(this.delegate, property, newValue);
                        return;
                    } catch (GroovyRuntimeException e2) {
// ignore, we'll throw e1
                    }
                }

                throw e1;
            }
        }
    }

    public boolean isCase(Object candidate){
        return InvokerHelper.asBool(call(candidate));
    }

    /**
     * Invokes the closure without any parameters, returning any value if applicable.
     *
     * @return the value if applicable or null if there is no return statement in the closure
     */
    public Object call() {
        return call(emptyArray);
    }
    
    /**
     * Invokes the closure, returning any value if applicable.
     *
     * @param arguments could be a single value or a List of values
     * @return the value if applicable or null if there is no return statement in the closure
     */
    public Object call(final Object arguments) {
        final Object params[];

        if (this.curriedParams.length != 0) {
            final Object args[];

            if (arguments instanceof Object[]) {
                args = (Object[]) arguments;
            } else {
                args = new Object[]{arguments};
            }

            params = new Object[this.curriedParams.length + args.length];

            System.arraycopy(this.curriedParams, 0, params, 0, this.curriedParams.length);
            System.arraycopy(args, 0, params, this.curriedParams.length, args.length);
        } else {
            if (arguments instanceof Object[]) {
                params = (Object[]) arguments;
            } else {
                return doCall(arguments);
            }
        }

        final int lastParam = this.numberOfParameters - 1;

        if (this.supportsVarargs && !(this.numberOfParameters == params.length && (params[lastParam] == null || params[lastParam].getClass() == Object[].class))) {
            final Object actualParameters[] = new Object[this.numberOfParameters];

            //
            // We have a closure which supports variable arguments and we haven't got actual
            // parameters which have exactly the right number of parameters and ends with a null or an Object[]
            //
            if (params.length < lastParam) {
                //
                // Not enough parameters throw exception
                //
                // Note we allow there to be one fewer actual parameter than the number of formal parameters
                // in this case we pass an zero length Object[] as the last parameter
                //
                throw new IncorrectClosureArgumentsException(this, params, this.parameterTypes);
            } else {
                final Object rest[] = new Object[params.length - lastParam];	 // array used to pass the rest of the paraters

                // fill the parameter array up to but not including the last one
                System.arraycopy(params, 0, actualParameters, 0, lastParam);

                // put the rest of the parameters in the overflow araay
                System.arraycopy(params, lastParam, rest, 0, rest.length);

                // pass the overflow array as the last parameter
                actualParameters[lastParam] = rest;

                return callViaReflection(actualParameters);
            }
        }

        if (params.length == 0) {
            return doCall();
        } else if (params.length == 1) {
            return doCall(params[0]);
        } else if (params.length == 2) {
            return doCall(params[0], params[1]);
        } else {
            return callViaReflection(params);
        }
    }

    public Object callSpecial(final Object arguments) {
        final Object params[];

        if (this.curriedParams.length > 0) {
            final Object args[];

            if (arguments instanceof Object[]) {
                args = (Object[]) arguments;
            } else {
                args = new Object[]{arguments};
            }

            params = new Object[this.curriedParams.length + args.length];

            System.arraycopy(this.curriedParams, 0, params, 0, this.curriedParams.length);
            System.arraycopy(args, 0, params, this.curriedParams.length, args.length);
        } else {
            Object[] tmpParams = null;
            if (arguments instanceof Object[]) {
                tmpParams = (Object[]) arguments;

                if ((tmpParams != null) && (tmpParams.length > 1)) {
                    boolean allNull = true;
                    for (int j = 1; j < tmpParams.length; j++) {
                        if (tmpParams[j] != null) {
                            allNull = false;
                            break;
                        }
                    }
                    if (allNull) {
                        if (tmpParams[0] instanceof Object[])
                            tmpParams = (Object[]) (tmpParams[0]);
                        else
                            throw new IncorrectClosureArgumentsException(this, new Object[] { tmpParams[0] }, this.parameterTypes);
                    }
                }
                params = tmpParams;

            } else {
                return doCall(arguments);
            }
        }

        final int lastParam = this.numberOfParameters - 1;

        if (this.supportsVarargs && !(this.numberOfParameters == params.length && (params.length > lastParam) && (params[lastParam] == null || params[lastParam].getClass() == Object[].class))) {
            final Object actualParameters[] = new Object[this.numberOfParameters];

            //
            // We have a closure which supports variable arguments and we haven't got actual
            // parameters which have exactly the right number of parameters and ends with a null or an Object[]
            //
            if (params.length < lastParam) {
                //
                // Not enough parameters throw exception
                //
                // Note we allow there to be one fewer actual parameter than the number of formal parameters
                // in this case we pass an zero length Object[] as the last parameter
                //
                throw new IncorrectClosureArgumentsException(this, params, this.parameterTypes);
            } else {
                final Object rest[] = new Object[params.length - lastParam];	 // array used to pass the rest of the paraters

                // fill the parameter array up to but not including the last one
                System.arraycopy(params, 0, actualParameters, 0, lastParam);

                // put the rest of the parameters in the overflow araay
                System.arraycopy(params, lastParam, rest, 0, rest.length);

                // pass the overflow array as the last parameter
                actualParameters[lastParam] = rest;

                return callViaReflection(actualParameters);
            }
        }

        if (params.length == 0) {
            return doCall();
        } else if (params.length == 1) {
            return doCall(params[0]);
        } else if (params.length == 2) {
            return doCall(params[0], params[1]);
        } else {
            return callViaReflection(params);
        }
    }

    protected static Object throwRuntimeException(Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        } else {
            throw new GroovyRuntimeException(throwable.getMessage(), throwable);
        }
    }

    /**
     * An attempt to optimise calling closures with one parameter
     * If the closure has one untyped parameter then it will overload this function
     * If not this will be called ans will use reflection to deal with the case of a
     * single typed parameter
     *
     * @param p1
     * @return the result of calling the closure
     */
    protected Object doCall(final Object p1) {
        return callViaReflection(new Object[]{p1});
    }
    
    /**
     * An attempt to optimise calling closures with no parameter
     * This method only calls doCall(Object) and will be called by call(Object)
     * if the parameter given to call is an empty Object array
     *
     * @return the result of calling the closure
     */
    protected Object doCall() {
        return doCall((Object)null);
    }
    

    /**
     * An attempt to optimise calling closures with two parameters
     * If the closure has two untyped parameters then it will overload this function
     * If not this will be called ans will use reflection to deal with the case of one
     * or two typed parameters
     *
     * @param p1
     * @return the result of calling the closure
     */
    protected Object doCall(final Object p1, final Object p2) {
         return callViaReflection(new Object[]{p1, p2});
    }

    private Object callViaReflection(final Object params[]) {
        try {
            // invoke the closure
            return ((Method) callsMap.get(new Integer(params.length))).invoke(this, params);
        } catch (final IllegalArgumentException e) {
            throw new IncorrectClosureArgumentsException(this, params, this.parameterTypes);
        } catch (final IllegalAccessException e) {
            final Throwable cause = e.getCause();

            return throwRuntimeException((cause == null) ? e : cause);
        } catch (final InvocationTargetException e) {
            final Throwable cause = e.getCause();

            return throwRuntimeException((cause == null) ? e : cause);
        }
    }

    /**
     * Used when a closure wraps a method on a class
     *
     * @return empty string
     */
    public String getMethod() {
        return "";
    }

    /**
     * @return the owner Object to which method calls will go which is
     *         typically the outer class when the closure is constructed
     */
    public Object getOwner() {
        return this.owner;
    }

    /**
     * @return the delegate Object to which method calls will go which is
     *         typically the outer class when the closure is constructed
     */
    public Object getDelegate() {
        return this.delegate;
    }

    /**
     * Allows the delegate to be changed such as when performing markup building
     *
     * @param delegate
     */
    public void setDelegate(Object delegate) {
        this.delegate = delegate;
    }

    /**
     * @return the parameter types of this closure
     */
    public Class[] getParameterTypes() {
        return this.parameterTypes;
    }

    /**
     * @return a version of this closure which implements Writable
     */
    public Closure asWritable() {
        return new WritableClosure();
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        call();
    }

    /**
     * Support for closure currying
     *
     * @param arguments
     */
    public Closure curry(final Object arguments[]) {
        final Closure curriedClosure = (Closure) this.clone();
        final Object newCurriedParams[] = new Object[curriedClosure.curriedParams.length + arguments.length];

        System.arraycopy(curriedClosure.curriedParams, 0, newCurriedParams, 0, curriedClosure.curriedParams.length);
        System.arraycopy(arguments, 0, newCurriedParams, curriedClosure.curriedParams.length, arguments.length);

        curriedClosure.curriedParams = newCurriedParams;

        return curriedClosure;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (final CloneNotSupportedException e) {
            return null;
        }
    }

    private class WritableClosure extends Closure implements Writable {
        public WritableClosure() {
            super(null);
        }

        /* (non-Javadoc)
     * @see groovy.lang.Writable#writeTo(java.io.Writer)
     */
        public Writer writeTo(Writer out) throws IOException {
            Closure.this.call(out);

            return out;
        }

        /* (non-Javadoc)
         * @see groovy.lang.GroovyObject#invokeMethod(java.lang.String, java.lang.Object)
         */
        public Object invokeMethod(String method, Object arguments) {
            if ("clone".equals(method)) {
                return clone();
            } else if ("curry".equals(method)) {
                return curry((Object[]) arguments);
            } else if ("asWritable".equals(method)) {
                return asWritable();
            } else {
                return Closure.this.invokeMethod(method, arguments);
            }
        }

        /* (non-Javadoc)
         * @see groovy.lang.GroovyObject#getProperty(java.lang.String)
         */
        public Object getProperty(String property) {
            return Closure.this.getProperty(property);
        }

        /* (non-Javadoc)
         * @see groovy.lang.GroovyObject#setProperty(java.lang.String, java.lang.Object)
         */
        public void setProperty(String property, Object newValue) {
            Closure.this.setProperty(property, newValue);
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#call()
         */
        public Object call() {
            return Closure.this.call();
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#call(java.lang.Object)
         */
        public Object call(Object arguments) {
            return Closure.this.call(arguments);
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#doCall(java.lang.Object)
         */
        protected Object doCall(Object p1) {
            return Closure.this.doCall(p1);
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#doCall(java.lang.Object, java.lang.Object)
         */
        protected Object doCall(Object p1, Object p2) {
            return Closure.this.doCall(p1, p2);
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#getDelegate()
         */
        public Object getDelegate() {
            return Closure.this.getDelegate();
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#setDelegate(java.lang.Object)
         */
        public void setDelegate(Object delegate) {
            Closure.this.setDelegate(delegate);
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#getParameterTypes()
         */
        public Class[] getParameterTypes() {
            return Closure.this.getParameterTypes();
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#asWritable()
         */
        public Closure asWritable() {
            return this;
        }

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        public void run() {
            Closure.this.run();
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#curry(java.lang.Object[])
         */
        public Closure curry(Object[] arguments) {
            return Closure.this.curry(arguments).asWritable();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#clone()
         */
        public Object clone() {
            return ((Closure) Closure.this.clone()).asWritable();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
            return Closure.this.hashCode();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object arg0) {
            return Closure.this.equals(arg0);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            final StringWriter writer = new StringWriter();

            try {
                writeTo(writer);
            } catch (IOException e) {
                return null;
            }

            return writer.toString();
        }
    }

    /**
     * @return Returns the directive.
     */
    public int getDirective() {
        return directive;
    }

    /**
     * @param directive The directive to set.
     */
    public void setDirective(int directive) {
        this.directive = directive;
    }
}
