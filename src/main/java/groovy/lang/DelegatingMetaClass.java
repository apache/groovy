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

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.lang.reflect.Method;
import java.util.List;

/**
 * {@link MetaClass} implementation that forwards all operations to another meta class.
 */
public class DelegatingMetaClass implements MetaClass, MutableMetaClass, GroovyObject {
    /**
     * Wrapped meta class that receives delegated operations.
     */
    protected MetaClass delegate;

    /**
     * Creates a delegating meta class for the supplied adaptee.
     *
     * @param delegate the meta class to delegate to
     */
    public DelegatingMetaClass(final MetaClass delegate) {
        this.delegate = delegate;
    }

    /**
     * Creates a delegating meta class for the current meta class of the supplied type.
     *
     * @param theClass the Groovy type whose meta class should be wrapped
     */
    public DelegatingMetaClass(final Class theClass) {
        this(GroovySystem.getMetaClassRegistry().getMetaClass(theClass));
    }

    /** {@inheritDoc} */
    @Override
    public boolean isModified() {
        return this.delegate instanceof MutableMetaClass && ((MutableMetaClass) this.delegate).isModified();
    }

    /** {@inheritDoc} */
    @Override
    public void addNewInstanceMethod(Method method) {
        if (delegate instanceof MutableMetaClass)
            ((MutableMetaClass) delegate).addNewInstanceMethod(method);
    }

    /** {@inheritDoc} */
    @Override
    public void addNewStaticMethod(Method method) {
        if (delegate instanceof MutableMetaClass)
            ((MutableMetaClass) delegate).addNewStaticMethod(method);
    }

    /** {@inheritDoc} */
    @Override
    public void addMetaMethod(MetaMethod metaMethod) {
        if (delegate instanceof MutableMetaClass)
            ((MutableMetaClass) delegate).addMetaMethod(metaMethod);
    }

    /** {@inheritDoc} */
    @Override
    public void addMetaBeanProperty(MetaBeanProperty metaBeanProperty) {
        if (delegate instanceof MutableMetaClass)
            ((MutableMetaClass) delegate).addMetaBeanProperty(metaBeanProperty);
    }

    /** {@inheritDoc} */
    @Override
    public void initialize() {
        delegate.initialize();
    }

    /** {@inheritDoc} */
    @Override
    public Object getAttribute(Object object, String attribute) {
        return delegate.getAttribute(object, attribute);
    }

    /** {@inheritDoc} */
    @Override
    public ClassNode getClassNode() {
        return delegate.getClassNode();
    }

    /** {@inheritDoc} */
    @Override
    public List<MetaMethod> getMetaMethods() {
        return delegate.getMetaMethods();
    }

    /** {@inheritDoc} */
    @Override
    public List<MetaMethod> getMethods() {
        return delegate.getMethods();
    }

    /** {@inheritDoc} */
    @Override
    public List<MetaMethod> respondsTo(Object obj, String name, Object[] argTypes) {
        return delegate.respondsTo(obj, name, argTypes);
    }

    /** {@inheritDoc} */
    @Override
    public List<MetaMethod> respondsTo(Object obj, String name) {
        return delegate.respondsTo(obj, name);
    }

    /** {@inheritDoc} */
    @Override
    public MetaProperty hasProperty(Object obj, String name) {
        return delegate.hasProperty(obj, name);
    }

    /** {@inheritDoc} */
    @Override
    public List<MetaProperty> getProperties() {
        return delegate.getProperties();
    }

    /** {@inheritDoc} */
    @Override
    public Object getProperty(Object object, String property) {
        return delegate.getProperty(object, property);
    }

    /** {@inheritDoc} */
    @Override
    public Object invokeConstructor(Object[] arguments) {
        return delegate.invokeConstructor(arguments);
    }

    /** {@inheritDoc} */
    @Override
    public Object invokeMethod(Object object, String methodName, Object arguments) {
        return delegate.invokeMethod(object, methodName, arguments);
    }

    /** {@inheritDoc} */
    @Override
    public Object invokeMethod(Object object, String methodName, Object[] arguments) {
        return delegate.invokeMethod(object, methodName, arguments);
    }

    /** {@inheritDoc} */
    @Override
    public Object invokeStaticMethod(Object object, String methodName, Object[] arguments) {
        return delegate.invokeStaticMethod(object, methodName, arguments);
    }

    /** {@inheritDoc} */
    @Override
    public void setAttribute(Object object, String attribute, Object newValue) {
        delegate.setAttribute(object, attribute, newValue);
    }

    /** {@inheritDoc} */
    @Override
    public void setProperty(Object object, String property, Object newValue) {
        delegate.setProperty(object, property, newValue);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return super.toString() + "[" + delegate.toString() + "]";
    }

    /**
     * @deprecated
     */
    @Override
    @Deprecated
    public MetaMethod pickMethod(String methodName, Class[] arguments) {
        return delegate.pickMethod(methodName, arguments);
    }

    /** {@inheritDoc} */
    @Override
    public Object getAttribute(Class sender, Object receiver, String messageName, boolean useSuper) {
        return this.delegate.getAttribute(sender, receiver, messageName, useSuper);
    }

    /** {@inheritDoc} */
    @Override
    public Object getProperty(Class sender, Object receiver, String messageName, boolean useSuper, boolean fromInsideClass) {
        return this.delegate.getProperty(sender, receiver, messageName, useSuper, fromInsideClass);
    }

    /** {@inheritDoc} */
    @Override
    public MetaProperty getMetaProperty(String name) {
        return this.delegate.getMetaProperty(name);
    }

    /** {@inheritDoc} */
    @Override
    public MetaMethod getStaticMetaMethod(String name, Object[] args) {
        return this.delegate.getStaticMetaMethod(name, args);
    }

    /**
     * Finds a static meta method using explicit parameter types.
     *
     * @param name the method name
     * @param argTypes the parameter types to match
     * @return the matching static meta method, or {@code null} if none matches
     */
    public MetaMethod getStaticMetaMethod(String name, Class[] argTypes) {
        return this.delegate.getStaticMetaMethod(name, argTypes);
    }

    /** {@inheritDoc} */
    @Override
    public MetaMethod getMetaMethod(String name, Object[] args) {
        return this.delegate.getMetaMethod(name, args);
    }

    /** {@inheritDoc} */
    @Override
    public Class getTheClass() {
        return this.delegate.getTheClass();
    }

    /** {@inheritDoc} */
    @Override
    public Object invokeMethod(Class sender, Object receiver, String methodName, Object[] arguments, boolean isCallToSuper, boolean fromInsideClass) {
        return this.delegate.invokeMethod(sender, receiver, methodName, arguments, isCallToSuper, fromInsideClass);
    }

    /** {@inheritDoc} */
    @Override
    public Object invokeMissingMethod(Object instance, String methodName, Object[] arguments) {
        return this.delegate.invokeMissingMethod(instance, methodName, arguments);
    }

    /** {@inheritDoc} */
    @Override
    public Object invokeMissingProperty(Object instance, String propertyName, Object optionalValue, boolean isGetter) {
        return this.delegate.invokeMissingProperty(instance, propertyName, optionalValue, isGetter);
    }

    /**
     * Indicates whether the wrapped type implements {@link GroovyObject}.
     *
     * @return {@code true} if the adaptee represents a Groovy object type
     */
    public boolean isGroovyObject() {
        return GroovyObject.class.isAssignableFrom(this.delegate.getTheClass());
    }

    /** {@inheritDoc} */
    @Override
    public void setAttribute(Class sender, Object receiver, String messageName, Object messageValue, boolean useSuper, boolean fromInsideClass) {
        this.delegate.setAttribute(sender, receiver, messageName, messageValue, useSuper, fromInsideClass);
    }

    /** {@inheritDoc} */
    @Override
    public void setProperty(Class sender, Object receiver, String messageName, Object messageValue, boolean useSuper, boolean fromInsideClass) {
        this.delegate.setProperty(sender, receiver, messageName, messageValue, useSuper, fromInsideClass);
    }

    /** {@inheritDoc} */
    @Override
    public int selectConstructorAndTransformArguments(int numberOfConstructors, Object[] arguments) {
        return this.delegate.selectConstructorAndTransformArguments(numberOfConstructors, arguments);
    }

    /**
     * Replaces the wrapped meta class.
     *
     * @param adaptee the new meta class to delegate to
     */
    public void setAdaptee(MetaClass adaptee) {
        this.delegate = adaptee;
    }

    /**
     * Returns the wrapped meta class.
     *
     * @return the current adaptee
     */
    public MetaClass getAdaptee() {
        return this.delegate;
    }

    /** {@inheritDoc} */
    @Override
    public Object invokeMethod(String name, Object args) {
        try {
            return getMetaClass().invokeMethod(this, name, args);
        }
        catch (MissingMethodException e) {
            if (delegate instanceof GroovyObject)
                return ((GroovyObject) delegate).invokeMethod(name, args);
            else
                throw e;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Object getProperty(String property) {
        try {
            return getMetaClass().getProperty(this, property);
        }
        catch (MissingPropertyException e) {
            if (delegate instanceof GroovyObject)
                return ((GroovyObject) delegate).getProperty(property);
            else
                throw e;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setProperty(String property, Object newValue) {
        try {
            getMetaClass().setProperty(this, property, newValue);
        }
        catch (MissingPropertyException e) {
            if (delegate instanceof GroovyObject)
                ((GroovyObject) delegate).setProperty(property, newValue);
            else
                throw e;
        }
    }

    /** {@inheritDoc} */
    @Override
    public MetaClass getMetaClass() {
        return InvokerHelper.getMetaClass(getClass());
    }

    /** {@inheritDoc} */
    @Override
    public void setMetaClass(MetaClass metaClass) {
        throw new UnsupportedOperationException();
    }
}
