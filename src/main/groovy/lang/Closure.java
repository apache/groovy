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

import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.reflection.stdclasses.CachedClosureClass;
import org.codehaus.groovy.runtime.CurriedClosure;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Represents any closure object in Groovy.
 * <p/>
 * Groovy allows instances of Closures to be called in a
 * short form. For example:
 * <pre>
 *   def a = 1
 *   def c = {a}
 *   assert c() == 1
 * </pre>
 * To be able to use a Closure in this way with your own
 * subclass, you need to provide a doCall method with any
 * signature you want to. This ensures that 
 * {@link #getMaximumNumberOfParameters()} and 
 * {@link #getParameterTypes()} will work too without any 
 * additional code. If no doCall method is provided a
 * closure must be used in its long form like
 * <pre>
 *   def a = 1
 *   def c = {a}
 *   assert c.call() == 1
 * </pre>
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author <a href="mailto:tug@wilson.co.uk">John Wilson</a>
 * @author <a href="mailto:blackdrag@gmx.org">Jochen Theodorou</a>
 * @author Graeme Rocher
 *
 * @version $Revision$
 */
public abstract class Closure extends GroovyObjectSupport implements Cloneable, Runnable, Serializable  {

    /**
     * With this resolveStrategy set the closure will attempt to resolve property references to the
     * owner first
     */
    public static final int OWNER_FIRST = 0;
    /**
     * With this resolveStrategy set the closure will attempt to resolve property references to the
     * delegate first
     */
    public static final int DELEGATE_FIRST = 1;
    /**
     * With this resolveStrategy set the closure will resolve property references to the owner only
     * and not call the delegate at all
     */
    public static final int OWNER_ONLY = 2;
    /**
     * With this resolveStrategy set the closure will resolve property references to the delegate
     * only and entirely bypass the owner
     */
    public static final int DELEGATE_ONLY = 3;
    /**
     * With this resolveStrategy set the closure will resolve property references to itself and go
     * through the usual MetaClass look-up process. This allows the developer to override getProperty
     * using ExpandoMetaClass of the closure itself
     */
    public static final int TO_SELF = 4;

    public static final int DONE = 1, SKIP = 2;
    private static final Object[] EMPTY_OBJECT_ARRAY = {};
    
    private Object delegate;
    private Object owner;
    private Object thisObject;
    private int resolveStrategy = OWNER_FIRST;
    private int directive;
    protected Class[] parameterTypes;
    protected int maximumNumberOfParameters;
    private static final long serialVersionUID = 4368710879820278874L;

    public Closure(Object owner, Object thisObject) {
        this.owner = owner;
        this.delegate = owner;
        this.thisObject = thisObject;

        final CachedClosureClass cachedClass = (CachedClosureClass) ReflectionCache.getCachedClass(getClass());
        parameterTypes = cachedClass.getParameterTypes();
        maximumNumberOfParameters = cachedClass.getMaximumNumberOfParameters();
    }
    
    public Closure(Object owner) {
        this(owner,null);
    }

    /**
     * Sets the strategy which the closure uses to resolve property references. The default is Closure.OWNER_FIRST
     *
     * @param resolveStrategy The resolve strategy to set
     *
     * @see groovy.lang.Closure#DELEGATE_FIRST
     * @see groovy.lang.Closure#DELEGATE_ONLY
     * @see groovy.lang.Closure#OWNER_FIRST
     * @see groovy.lang.Closure#OWNER_ONLY
     * @see groovy.lang.Closure#TO_SELF
     */
    public void setResolveStrategy(int resolveStrategy) {
        this.resolveStrategy = resolveStrategy;
    }

    /**
     * Gets the strategy which the closure users to resolve methods and properties
     *
     * @return The resolve strategy
     * 
     * @see groovy.lang.Closure#DELEGATE_FIRST
     * @see groovy.lang.Closure#DELEGATE_ONLY
     * @see groovy.lang.Closure#OWNER_FIRST
     * @see groovy.lang.Closure#OWNER_ONLY
     * @see groovy.lang.Closure#TO_SELF
     */
    public int getResolveStrategy() {
        return resolveStrategy;
    }

    public Object getThisObject(){
        return thisObject;
    }

    public Object getProperty(final String property) {
        if ("delegate".equals(property)) {
            return getDelegate();
        } else if ("owner".equals(property)) {
            return getOwner();
        } else if ("maximumNumberOfParameters".equals(property)) {
            return Integer.valueOf(getMaximumNumberOfParameters());
        } else if ("parameterTypes".equals(property)) {
            return getParameterTypes();
        } else if ("metaClass".equals(property)) {
            return getMetaClass();
        } else if ("class".equals(property)) {
            return getClass();
        } else if ("directive".equals(property)) {
            return Integer.valueOf(getDirective());
        } else {
            switch(resolveStrategy) {
                case DELEGATE_FIRST:
                    return getPropertyDelegateFirst(property);
                case DELEGATE_ONLY:
                    return InvokerHelper.getProperty(this.delegate, property);
                case OWNER_ONLY:
                    return InvokerHelper.getProperty(this.owner, property);
                case TO_SELF:
                    return super.getProperty(property);
                default:
                    return getPropertyOwnerFirst(property);
            }
        }
    }

    private Object getPropertyDelegateFirst(String property) {
        if(delegate == null) return getPropertyOwnerFirst(property);
        return getPropertyTryThese(property, this.delegate, this.owner);

    }

    private Object getPropertyTryThese(String property, Object firstTry, Object secondTry) {
        try {
            // lets try getting the property on the owner
            return InvokerHelper.getProperty(firstTry, property);
        } catch (MissingPropertyException e1) {
            if (secondTry != null && firstTry != this && firstTry != secondTry) {
                try {
                    // lets try getting the property on the delegate
                    return InvokerHelper.getProperty(secondTry, property);
                } catch (GroovyRuntimeException e2) {
                    // ignore, we'll throw e1
                }
            }

            throw e1;
        }
    }

    private Object getPropertyOwnerFirst(String property) {
        return getPropertyTryThese(property, this.owner, this.delegate);
    }

    public void setProperty(String property, Object newValue) {
        if ("delegate".equals(property)) {
            setDelegate(newValue);
        } else if ("metaClass".equals(property)) {
            setMetaClass((MetaClass) newValue);
        } else if ("resolveStrategy".equals(property)) {
            setResolveStrategy(((Number)newValue).intValue());
        }
        else {
            switch(resolveStrategy) {
                case DELEGATE_FIRST:
                    setPropertyDelegateFirst(property, newValue);
                break;
                case DELEGATE_ONLY:
                    InvokerHelper.setProperty(this.delegate, property, newValue);
                break;
                case OWNER_ONLY:
                    InvokerHelper.setProperty(this.owner, property, newValue);
                break;
                case TO_SELF:
                    super.setProperty(property, newValue);
                break;
                default:
                    setPropertyOwnerFirst(property, newValue);
            }
        }
    }

    private void setPropertyDelegateFirst(String property, Object newValue) {
        if(delegate == null) setPropertyOwnerFirst(property, newValue); 
        else
            setPropertyTryThese(property, newValue, this.delegate, this.owner);
    }

    private void setPropertyTryThese(String property, Object newValue, Object firstTry, Object secondTry) {
        try {
            // lets try setting the property on the owner
            InvokerHelper.setProperty(firstTry, property, newValue);
        } catch (GroovyRuntimeException e1) {
            if (firstTry != null && firstTry != this && firstTry != secondTry) {
                try {
                    // lets try setting the property on the delegate
                    InvokerHelper.setProperty(secondTry, property, newValue);
                    return;
                } catch (GroovyRuntimeException e2) {
                    // ignore, we'll throw e1
                }
            }

            throw e1;
        }
    }

    private void setPropertyOwnerFirst(String property, Object newValue) {
        setPropertyTryThese(property, newValue, this.owner, this.delegate);
    }

    public boolean isCase(Object candidate){
        return DefaultTypeTransformation.castToBoolean(call(candidate));
    }

    /**
     * Invokes the closure without any parameters, returning any value if applicable.
     *
     * @return the value if applicable or null if there is no return statement in the closure
     */
    public Object call() {
        final Object[] NOARGS = EMPTY_OBJECT_ARRAY;
        return call(NOARGS);
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
     * @param delegate the new delegate
     */
    public void setDelegate(Object delegate) {
        this.delegate = delegate;
    }
    
    /**
     * @return the parameter types of the longest doCall method
     * of this closure
     */
    public Class[] getParameterTypes() {
        return parameterTypes;
    }

    /**
     * @return the maximum number of parameters a doCall methos
     * of this closure can take
     */
    public int getMaximumNumberOfParameters() {
        return maximumNumberOfParameters;
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
     * @param arguments the arguments to bind
     * @return the new closure with its arguments bound
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
     *   Closure.this.call will call the outer call method, but
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
            return ((Closure) getOwner()).call();
        }

        /* (non-Javadoc)
         * @see groovy.lang.Closure#call(java.lang.Object)
         */
        public Object call(Object arguments) {
            return ((Closure) getOwner()).call(arguments);
        }
        
        public Object call(Object[] args) {
            return ((Closure) getOwner()).call(args);
        }

        public Object doCall(Object[] args) {
            return call(args);
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

        public void setResolveStrategy(int resolveStrategy) {
            Closure.this.setResolveStrategy(resolveStrategy);
        }
        
        public int getResolveStrategy() {
            return Closure.this.getResolveStrategy();
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
