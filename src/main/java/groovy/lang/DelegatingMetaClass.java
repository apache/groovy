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

public class DelegatingMetaClass implements MetaClass, MutableMetaClass, GroovyObject {
    protected MetaClass delegate;

    public DelegatingMetaClass(final MetaClass delegate) {
        this.delegate = delegate;
    }

    public DelegatingMetaClass(final Class theClass) {
        this(GroovySystem.getMetaClassRegistry().getMetaClass(theClass));
    }

    public boolean isModified() {
        return this.delegate instanceof MutableMetaClass && ((MutableMetaClass) this.delegate).isModified();
    }

    /* (non-Javadoc)
    * @see groovy.lang.MetaClass#addNewInstanceMethod(java.lang.reflect.Method)
    */
    public void addNewInstanceMethod(Method method) {
        if (delegate instanceof MutableMetaClass)
            ((MutableMetaClass) delegate).addNewInstanceMethod(method);
    }

    /* (non-Javadoc)
    * @see groovy.lang.MetaClass#addNewStaticMethod(java.lang.reflect.Method)
    */
    public void addNewStaticMethod(Method method) {
        if (delegate instanceof MutableMetaClass)
            ((MutableMetaClass) delegate).addNewStaticMethod(method);
    }

    public void addMetaMethod(MetaMethod metaMethod) {
        if (delegate instanceof MutableMetaClass)
            ((MutableMetaClass) delegate).addMetaMethod(metaMethod);
    }

    public void addMetaBeanProperty(MetaBeanProperty metaBeanProperty) {
        if (delegate instanceof MutableMetaClass)
            ((MutableMetaClass) delegate).addMetaBeanProperty(metaBeanProperty);
    }

    /* (non-Javadoc)
    * @see groovy.lang.MetaClass#initialize()
    */
    public void initialize() {
        delegate.initialize();
    }

    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#getAttribute(java.lang.Object, java.lang.String)
     */
    public Object getAttribute(Object object, String attribute) {
        return delegate.getAttribute(object, attribute);
    }

    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#getClassNode()
     */
    public ClassNode getClassNode() {
        return delegate.getClassNode();
    }

    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#getMetaMethods()
     */
    public List<MetaMethod> getMetaMethods() {
        return delegate.getMetaMethods();
    }

    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#getMethods()
     */
    public List<MetaMethod> getMethods() {
        return delegate.getMethods();
    }

    public List<MetaMethod> respondsTo(Object obj, String name, Object[] argTypes) {
        return delegate.respondsTo(obj, name, argTypes);
    }

    public List<MetaMethod> respondsTo(Object obj, String name) {
        return delegate.respondsTo(obj, name);
    }

    public MetaProperty hasProperty(Object obj, String name) {
        return delegate.hasProperty(obj, name);
    }

    /* (non-Javadoc)
    * @see groovy.lang.MetaClass#getProperties()
    */
    public List<MetaProperty> getProperties() {
        return delegate.getProperties();
    }

    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#getProperty(java.lang.Object, java.lang.String)
     */
    public Object getProperty(Object object, String property) {
        return delegate.getProperty(object, property);
    }

    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#invokeConstructor(java.lang.Object[])
     */
    public Object invokeConstructor(Object[] arguments) {
        return delegate.invokeConstructor(arguments);
    }

    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#invokeMethod(java.lang.Object, java.lang.String, java.lang.Object)
     */
    public Object invokeMethod(Object object, String methodName, Object arguments) {
        return delegate.invokeMethod(object, methodName, arguments);
    }

    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#invokeMethod(java.lang.Object, java.lang.String, java.lang.Object[])
     */
    public Object invokeMethod(Object object, String methodName, Object[] arguments) {
        return delegate.invokeMethod(object, methodName, arguments);
    }

    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#invokeStaticMethod(java.lang.Object, java.lang.String, java.lang.Object[])
     */
    public Object invokeStaticMethod(Object object, String methodName, Object[] arguments) {
        return delegate.invokeStaticMethod(object, methodName, arguments);
    }

    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#setAttribute(java.lang.Object, java.lang.String, java.lang.Object)
     */
    public void setAttribute(Object object, String attribute, Object newValue) {
        delegate.setAttribute(object, attribute, newValue);
    }

    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#setProperty(java.lang.Object, java.lang.String, java.lang.Object)
     */
    public void setProperty(Object object, String property, Object newValue) {
        delegate.setProperty(object, property, newValue);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return delegate.hashCode();
    }

    public String toString() {
        return super.toString() + "[" + delegate.toString() + "]";
    }

    /**
     * @deprecated
     */
    @Deprecated
    public MetaMethod pickMethod(String methodName, Class[] arguments) {
        return delegate.pickMethod(methodName, arguments);
    }

    public Object getAttribute(Class sender, Object receiver, String messageName, boolean useSuper) {
        return this.delegate.getAttribute(sender, receiver, messageName, useSuper);
    }

    public Object getProperty(Class sender, Object receiver, String messageName, boolean useSuper, boolean fromInsideClass) {
        return this.delegate.getProperty(sender, receiver, messageName, useSuper, fromInsideClass);
    }

    public MetaProperty getMetaProperty(String name) {
        return this.delegate.getMetaProperty(name);
    }

    public MetaMethod getStaticMetaMethod(String name, Object[] args) {
        return this.delegate.getStaticMetaMethod(name, args);
    }

    public MetaMethod getStaticMetaMethod(String name, Class[] argTypes) {
        return this.delegate.getStaticMetaMethod(name, argTypes);
    }

    public MetaMethod getMetaMethod(String name, Object[] args) {
        return this.delegate.getMetaMethod(name, args);
    }

    public Class getTheClass() {
        return this.delegate.getTheClass();
    }

    public Object invokeMethod(Class sender, Object receiver, String methodName, Object[] arguments, boolean isCallToSuper, boolean fromInsideClass) {
        return this.delegate.invokeMethod(sender, receiver, methodName, arguments, isCallToSuper, fromInsideClass);
    }

    public Object invokeMissingMethod(Object instance, String methodName, Object[] arguments) {
        return this.delegate.invokeMissingMethod(instance, methodName, arguments);
    }

    public Object invokeMissingProperty(Object instance, String propertyName, Object optionalValue, boolean isGetter) {
        return this.delegate.invokeMissingProperty(instance, propertyName, optionalValue, isGetter);
    }

    public boolean isGroovyObject() {
        return GroovyObject.class.isAssignableFrom(this.delegate.getTheClass());
    }

    public void setAttribute(Class sender, Object receiver, String messageName, Object messageValue, boolean useSuper, boolean fromInsideClass) {
        this.delegate.setAttribute(sender, receiver, messageName, messageValue, useSuper, fromInsideClass);
    }

    public void setProperty(Class sender, Object receiver, String messageName, Object messageValue, boolean useSuper, boolean fromInsideClass) {
        this.delegate.setProperty(sender, receiver, messageName, messageValue, useSuper, fromInsideClass);
    }

    public int selectConstructorAndTransformArguments(int numberOfConstructors, Object[] arguments) {
        return this.delegate.selectConstructorAndTransformArguments(numberOfConstructors, arguments);
    }

    public void setAdaptee(MetaClass adaptee) {
        this.delegate = adaptee;
    }

    public MetaClass getAdaptee() {
        return this.delegate;
    }

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

    public MetaClass getMetaClass() {
        return InvokerHelper.getMetaClass(getClass());
    }

    public void setMetaClass(MetaClass metaClass) {
        throw new UnsupportedOperationException();
    }
}
