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

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * Represents any closure object in Groovy.
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public abstract class Closure extends GroovyObjectSupport implements Cloneable, Runnable {

    private static final Object noParameters[] = new Object[] { null };

    private Object delegate;
    private Object owner;
    private MetaMethod doCallMethod;

    public Closure(Object delegate) {
        this.delegate = delegate;
        this.owner = delegate;
    }

    public Object invokeMethod(String method, Object arguments) {
        return doInvokeMethod(method, arguments, this);
    }

    private static Object doInvokeMethod(String method, Object arguments, Closure me) {
        if ("doCall".equals(method)) {
            return me.call(arguments);
        }
        else if ("call".equals(method)) {
            return me.call(arguments);
        }
        else if ("curry".equals(method)) {
            return me.curry(arguments);
        }
        else {
            try {
                return me.getMetaClass().invokeMethod(me, method, arguments);
            }
            catch (MissingMethodException e) {
                if (me.owner != me) {
                    try {
                        // lets try invoke method on delegate
                        return InvokerHelper.invokeMethod(me.owner, method, arguments);
                    }
                    catch (GroovyRuntimeException e3) {
                    }

                    // ignore, we'll throw e
                    Object aDelegate = me.delegate;
                    if (aDelegate != null && aDelegate != me.owner && aDelegate != me) {
                        try {
                            // lets try invoke method on delegate
                            return InvokerHelper.invokeMethod(aDelegate, method, arguments);
                        }
                        catch (GroovyRuntimeException e2) {
                            // ignore, we'll throw e
                        }
                    }
                }
                throw e;
            }
        }

    }

    public Object getProperty(String property) {
        try {
            return getMetaClass().getProperty(this, property);
        }
        catch (GroovyRuntimeException e) {
            try {
                // lets try invoke method on delegate
                return InvokerHelper.getProperty(owner, property);
            }
            catch (GroovyRuntimeException e3) {
                // ignore, we'll throw e
                //                        System.out.println("Caught: " + e3);
                //                        e3.printStackTrace();
                if (delegate != null && delegate != this && delegate != owner) {
                    try {
                        // lets try invoke method on delegate
                        return InvokerHelper.getProperty(delegate, property);
                    }
                    catch (GroovyRuntimeException e2) {
                        //                System.out.println("Caught: " + e2);
                        //                e2.printStackTrace();

                    }
                }
            }
            throw e;
        }
    }

    public void setProperty(String property, Object newValue) {
        try {
            getMetaClass().setProperty(this, property, newValue);
            return;
        }
        catch (GroovyRuntimeException e) {
            try {
                // lets try invoke method on delegate
                InvokerHelper.setProperty(owner, property, newValue);
            }
            catch (GroovyRuntimeException e3) {
                // ignore, we'll throw e
                if (delegate != null & delegate != this && delegate != owner) {
                    try {
                        // lets try invoke method on delegate
                        InvokerHelper.setProperty(delegate, property, newValue);
                        return;
                    }
                    catch (GroovyRuntimeException e2) {
                        System.out.println("Caught e2: " + e2);
                    }
                }
                throw e;
            }
        }
    }

    /**
     * Invokes the closure without any parameters, returning any value if applicable.
     * 
     * @return the value if applicable or null if there is no return statement in the closure
     */
    public Object call() {
        return call(null);
    }

    /**
     * Invokes the closure, returning any value if applicable.
     * 
     * @param arguments could be a single value or a List of values
     * @return the value if applicable or null if there is no return statement in the closure
     */
    public Object call(Object arguments) {
        MetaMethod method = getDoCallMethod();
        Object[] parameters = getParameters(arguments);

        try {
            method.checkParameters(parameters);
            return method.invoke(this, parameters);
        }
        catch (IllegalArgumentException e) {
            throw new IncorrectClosureArgumentsException(this, arguments, method.getParameterTypes());
        }
        catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            return throwRuntimeException(cause);
        }
    }

    protected static Object[] getParameters(Object arguments) {
        if (arguments instanceof Object[]) {
            if (arguments == null || ((Object[]) arguments).length == 0) {
                return noParameters;
            }
            else {
                return (Object[]) arguments;
            }
        }
        else {
            return new Object[] { arguments };
        }
    }

    protected MetaMethod getDoCallMethod() {
        if (doCallMethod == null) {
            MetaClass metaClass = getMetaClass();
            if (metaClass == null) {
                /** @todo warning - why do we need this */
                metaClass = InvokerHelper.getMetaClass(this);
            }
            List list = metaClass.getMethods("doCall");
            if (!list.isEmpty()) {
                doCallMethod = (MetaMethod) list.get(0);
            }

            if (doCallMethod == null) {
                throw new MissingMethodException("doCall", getClass(), noParameters);
            }
        }
        return doCallMethod;
    }

    protected static Object throwRuntimeException(Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            RuntimeException re = (RuntimeException) throwable;
            throw re;
        }
        else {
            throw new GroovyRuntimeException(throwable.getMessage(), throwable);
        }
    }

    /**
     * @return the delegate Object to which method calls will go which is
     * typically the outer class when the closure is constructed
     */
    public Object getDelegate() {
        return delegate;
    }

    /**
     * Allows the delegate to be changed such as when performing markup building
     * @param delegate
     */
    public void setDelegate(Object delegate) {
        this.delegate = delegate;
    }

    /**
     * @return the parameter types of this closure
     */
    public Class[] getParameterTypes() {
        return getDoCallMethod().getParameterTypes();
    }

    /**
     * @return a version of this closure which implements Writable
     */
    public Closure asWritable() {
        return new WritableClosure(this);
    }

    public void run() {
        call();
    }

    /**
     * Support for closure currying
     * @param arguments
     */
    public Closure curry(final Object arguments) {
        return new CurriedClosure(this, arguments);
    }

    /**
     * Allows the closure to be cloned
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    private static class DelegatingClosure extends Closure {
        protected final Closure closure;
        /**
         * @param closure
         */
        public DelegatingClosure(Closure closure) {
            super(closure.delegate);

            this.closure = closure;
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#call()
         */
        public Object call() {
            return this.closure.call();
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#call(java.lang.Object)
         */
        public Object call(Object arguments) {
            return this.closure.call(arguments);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#clone()
         */
        public Object clone() throws CloneNotSupportedException {
            return this.closure.clone();
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#getDelegate()
         */
        public Object getDelegate() {
            return this.closure.getDelegate();
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#getDoCallMethod()
         */
        protected MetaMethod getDoCallMethod() {
            return this.closure.getDoCallMethod();
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#getParameterTypes()
         */
        public Class[] getParameterTypes() {
            return this.closure.getParameterTypes();
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#getProperty(java.lang.String)
         */
        public Object getProperty(String property) {
            return this.closure.getProperty(property);
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#invokeMethod(java.lang.String, java.lang.Object)
         */
        public Object invokeMethod(String method, Object arguments) {
            return doInvokeMethod(method, arguments, this);
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#setDelegate(java.lang.Object)
         */
        public void setDelegate(Object delegate) {
            this.closure.setDelegate(delegate);
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#setProperty(java.lang.String, java.lang.Object)
         */
        public void setProperty(String property, Object newValue) {
            this.closure.setProperty(property, newValue);
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#asWritable()
         */
        public Closure asWritable() {
            return this.closure.asWritable();
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#curry(java.lang.Object)
         */
        public Closure curry(Object arguments) {
            return (Closure) this.closure.invokeMethod("curry", arguments);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object arg0) {
            return this.closure.equals(arg0);
        }

        /* (non-Javadoc)
         * @see groovy.lang.GroovyObjectSupport#getMetaClass()
         */
        public MetaClass getMetaClass() {
            return this.closure.getMetaClass();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
            return this.closure.hashCode();
        }

        /* (non-Javadoc)
         * @see groovy.lang.GroovyObjectSupport#setMetaClass(groovy.lang.MetaClass)
         */
        public void setMetaClass(MetaClass metaClass) {
            this.closure.setMetaClass(metaClass);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return this.closure.toString();
        }
    }

    private static class CurriedClosure extends DelegatingClosure {
        protected final Object[] curried_args;

        /**
         * @param closure
         * @param arguments
         */
        public CurriedClosure(Closure closure, Object arguments) {
            super(closure);

            this.curried_args = getParameters(arguments);
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#call()
         */
        public Object call() {
            return this.closure.call(this.curried_args);
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#call(java.lang.Object)
         */
        public Object call(Object args) {
            Object[] new_args = (Object[]) args;
            Object[] all_args = new Object[new_args.length + this.curried_args.length];

            System.arraycopy(this.curried_args, 0, all_args, 0, this.curried_args.length);
            System.arraycopy(new_args, 0, all_args, this.curried_args.length, new_args.length);

            return this.closure.call(all_args);
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#curry(java.lang.Object)
         */
        public Closure curry(Object arguments) {
            Object[] args = getParameters(arguments);
            Object[] new_curried_args = new Object[curried_args.length + args.length];

            System.arraycopy(this.curried_args, 0, new_curried_args, 0, this.curried_args.length);
            System.arraycopy(args, 0, new_curried_args, this.curried_args.length, args.length);

            return new CurriedClosure(this.closure, new_curried_args);
        }

        /**
         * @return a version of this closure which implements Writable
         */
        public Closure asWritable() {
            return new CurriedWritableClosure(this.closure, this.curried_args);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#clone()
         */
        public Object clone() throws CloneNotSupportedException {
            return new CurriedClosure((Closure) this.closure.clone(), this.curried_args.clone());
        }
    }

    private static class WritableClosure extends DelegatingClosure implements Writable {
        /**
         * @param delegate
         */
        public WritableClosure(Closure closure) {
            super(closure);
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#asWritable()
         */
        public Closure asWritable() {
            return this;
        }

        /* (non-Javadoc)
         * @see groovy.lang.Writable#writeTo(java.io.Writer)
         */
        public Writer writeTo(Writer out) throws IOException {
            call(new Object[] { out });
            return out;
        }

        /**
         * Support for closure currying
         * @param arguments
         */
        public Closure curry(final Object arguments) {
            return new CurriedWritableClosure(this.closure, arguments);
        }
    }

    private static class CurriedWritableClosure extends CurriedClosure implements Writable {
        /**
         * @param delegate
         */
        public CurriedWritableClosure(Closure closure, Object arguments) {
            super(closure, arguments);
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#asWritable()
         */
        public Closure asWritable() {
            return this;
        }

        /* (non-Javadoc)
         * @see groovy.lang.Writable#writeTo(java.io.Writer)
         */
        public Writer writeTo(Writer out) throws IOException {
            call(new Object[] { out });
            return out;
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#curry(java.lang.Object)
         */
        public Closure curry(Object arguments) {
            Object[] args = (Object[]) arguments;
            Object[] new_curried_args = new Object[curried_args.length + args.length];

            System.arraycopy(this.curried_args, 0, new_curried_args, 0, this.curried_args.length);
            System.arraycopy(args, 0, new_curried_args, this.curried_args.length, args.length);

            return new CurriedWritableClosure(this.closure, new_curried_args);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#clone()
         */
        public Object clone() throws CloneNotSupportedException {
            return new CurriedWritableClosure((Closure) this.closure.clone(), this.curried_args.clone());
        }
    }
}
