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
public abstract class Closure extends GroovyObjectSupport implements Cloneable {

    private static final Object noParameters[] = new Object[] { null };

    private Object delegate;
    private Object owner;
    private MetaMethod doCallMethod;

    public Closure(Object delegate) {
        this.delegate = delegate;
        this.owner = delegate;
    }

    public Object invokeMethod(String method, Object arguments) {
        if ("doCall".equals(method)) {
            return call(arguments);
        }
        else if ("call".equals(method)) {
            return call(arguments);
        }
        else {
            try {
                return getMetaClass().invokeMethod(this, method, arguments);
            }
            catch (MissingMethodException e) {
                Object aDelegate = (delegate != null && delegate != this) ? delegate : owner;
                try {
                    // lets try invoke method on delegate
                    return InvokerHelper.invokeMethod(aDelegate, method, arguments);
                }
                catch (GroovyRuntimeException e2) {
                    if (owner != aDelegate) {
                        try {
                            // lets try invoke method on delegate
                            return InvokerHelper.invokeMethod(owner, method, arguments);
                        }
                        catch (GroovyRuntimeException e3) {
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
            Object aDelegate = (delegate != null && delegate != this) ? delegate : owner;
            try {
                // lets try invoke method on delegate
                return InvokerHelper.getProperty(aDelegate, property);
            }
            catch (GroovyRuntimeException e2) {
                if (owner != aDelegate) {
                    try {
                        // lets try invoke method on delegate
                        return InvokerHelper.getProperty(owner, property);
                    }
                    catch (GroovyRuntimeException e3) {
                        // ignore, we'll throw e
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
            Object aDelegate = (delegate != null && delegate != this) ? delegate : owner;
            try {
                // lets try invoke method on delegate
                InvokerHelper.setProperty(aDelegate, property, newValue);
                return;
            }
            catch (GroovyRuntimeException e2) {
                if (owner != aDelegate) {
                    try {
                        // lets try invoke method on delegate
                        InvokerHelper.setProperty(owner, property, newValue);
                    }
                    catch (GroovyRuntimeException e3) {
                        // ignore, we'll throw e
                    }
                }
            }
            throw e;
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
        try {
            Object[] parameters = null;
            if (arguments instanceof Object[]) {
                parameters = (Object[]) arguments;
                if (parameters == null || parameters.length == 0) {
                    parameters = noParameters;
                }
            }
            else {
                parameters = new Object[] { arguments };
            }
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

    protected Object throwRuntimeException(Throwable throwable) {
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
        return new WritableClosure(this.delegate);
    }

    /**
     * Allows the closure to be cloned
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    private class WritableClosure extends Closure implements Writable {
        /**
         * @param delegate
         */
        public WritableClosure(Object delegate) {
            super(delegate);
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#asWritable()
         */
        public Closure asWritable() {
            return this;
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
         * @see java.lang.Object#clone()
         */
        public Object clone() throws CloneNotSupportedException {
            return Closure.this.clone();
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#getDelegate()
         */
        public Object getDelegate() {
            return Closure.this.getDelegate();
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#getDoCallMethod()
         */
        protected MetaMethod getDoCallMethod() {
            return Closure.this.getDoCallMethod();
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#getParameterTypes()
         */
        public Class[] getParameterTypes() {
            return Closure.this.getParameterTypes();
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#getProperty(java.lang.String)
         */
        public Object getProperty(String property) {
            return Closure.this.getProperty(property);
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#invokeMethod(java.lang.String, java.lang.Object)
         */
        public Object invokeMethod(String name, Object args) {
            return Closure.this.invokeMethod(name, args);
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#setDelegate(java.lang.Object)
         */
        public void setDelegate(Object delegate) {
            Closure.this.setDelegate(delegate);
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#setProperty(java.lang.String, java.lang.Object)
         */
        public void setProperty(String property, Object newValue) {
            Closure.this.setProperty(property, newValue);
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#throwRuntimeException(java.lang.Throwable)
         */
        protected Object throwRuntimeException(Throwable throwable) {
            return Closure.this.throwRuntimeException(throwable);
        }

        /* (non-Javadoc)
         * @see groovy.lang.Writable#writeTo(java.io.Writer)
         */
        public void writeTo(Writer out) throws IOException {
            call(new Object[] { out });
        }
    }
}
