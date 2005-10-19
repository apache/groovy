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

import org.codehaus.groovy.runtime.CurriedClosure;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
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
    private Class[] parameterTypes;
    protected int maximumNumberOfParameters;


    private int directive = 0;
    public static int DONE = 1;
    public static int SKIP = 2;

    public Closure(Object owner) {
        this.owner = owner;
        this.delegate = owner;

        Class closureClass = this.getClass();
        maximumNumberOfParameters = 0;

        final Class clazz = closureClass;
        final Method[] methods = (Method[]) AccessController.doPrivileged(new  PrivilegedAction() {
            public Object run() {
                return clazz.getDeclaredMethods();
            }
        });

        for (int j = 0; j < methods.length; j++) {
            if ("doCall".equals(methods[j].getName()) && methods[j].getParameterTypes().length > maximumNumberOfParameters) {
                parameterTypes = methods[j].getParameterTypes();
                maximumNumberOfParameters = parameterTypes.length;
            }
        }
    }

    public Object getProperty(String property) {
        if ("delegate".equals(property)) {
            return getDelegate();
        } else if ("owner".equals(property)) {
            return getOwner();
        } else if ("getMaximumNumberOfParameters".equals(property)) {
            return new Integer(getMaximumNumberOfParameters());
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
        return call(new Object[]{});
    }
    
    public Object call(Object[] args) {
        try {
            return getMetaClass().invokeMethod(this,"doCall",args);
        } catch (Exception e) {
            return throwRuntimeException(e);
        }
    }
    
    /**
     * Invokes the closure, returning any value if applicable.
     *
     * @param arguments could be a single value or a List of values
     * @return the value if applicable or null if there is no return statement in the closure
     */
    public Object call(final Object arguments) {
        return call(new Object[]{arguments});
    }
    
    protected static Object throwRuntimeException(Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        } else {
            throw new GroovyRuntimeException(throwable.getMessage(), throwable);
        }
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
     * @return the parameter types of the longest doCall method
     * of this closure
     */
    public Class[] getParameterTypes() {
        return this.parameterTypes;
    }

    /**
     * @return the maximum number of parameters a doCall methos
     * of this closure can take
     */
    public int getMaximumNumberOfParameters() {
        return this.maximumNumberOfParameters;
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
        return new CurriedClosure(this,arguments);
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
    
    /**
     * Implementation note: 
     *   This has to be an inner class!
     * 
     * Reason: 
     *   Closure.this.call will call the outer call method, bur
     * with the inner class as executing object. This means any
     * invokeMethod or getProperty call will be called on this 
     * inner class instead of the outer!
     */
    private class WritableClosure extends Closure implements Writable {
        public WritableClosure() {
            super(Closure.this);
        }

        /* (non-Javadoc)
         * @see groovy.lang.Writable#writeTo(java.io.Writer)
         */
        public Writer writeTo(Writer out) throws IOException {
            Closure.this.call(new Object[]{out});

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
         * @see groovy.lang.Closure#getParameterTypes()
         */
        public int getMaximumNumberOfParameters() {
            return Closure.this.getMaximumNumberOfParameters();
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
        
        public Closure curry(final Object arguments[]) {
            return (new CurriedClosure(this,arguments)).asWritable();
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
