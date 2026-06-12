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
package org.codehaus.groovy.runtime.metaclass;

import groovy.lang.DelegatingMetaClass;
import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;
import groovy.lang.MetaProperty;
import org.codehaus.groovy.ast.ClassNode;

import java.util.List;

/**
 * An abstract delegating MetaClass that delegates calls to an owner object's MetaClass.
 * Subclasses must provide an owner object via the {@link #getOwner()} method.
 * This is used in Groovy for features like mixins that require delegation to an owner's
 * metaclass for method and property access.
 * <p>
 * This class is for internal use by the Groovy runtime.
 */
public abstract class OwnedMetaClass extends DelegatingMetaClass {
    /**
     * Constructs a new OwnedMetaClass.
     *
     * @param delegate the MetaClass to delegate to
     */
    public OwnedMetaClass(final MetaClass delegate) {
        super(delegate);
    }

    /**
     * Retrieves the value of an attribute on the owner's MetaClass.
     *
     * @param object the object (ignored; the owner is used instead)
     * @param attribute the attribute name
     * @return the attribute value
     */
    @Override
    public Object getAttribute(Object object, String attribute) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getAttribute(owner, attribute);
    }

    /**
     * Returns the owner object whose MetaClass should be used for delegated operations.
     *
     * @return the owner object
     */
    protected abstract Object getOwner();

    /**
     * Returns the AST ClassNode for the owner's class.
     *
     * @return the ClassNode of the owner's metaclass
     */
    @Override
    public ClassNode getClassNode() {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getClassNode();
    }

    /**
     * Returns all metamethods from the owner's MetaClass.
     *
     * @return a list of all metamethods
     */
    @Override
    public List<MetaMethod> getMetaMethods() {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getMetaMethods();
    }

    /**
     * Returns all methods from the owner's MetaClass.
     *
     * @return a list of all methods
     */
    @Override
    public List<MetaMethod> getMethods() {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getMethods();
    }

    /**
     * Returns all metamethods that respond to the given method name and argument types.
     *
     * @param obj the object (ignored; the owner is used instead)
     * @param name the method name
     * @param argTypes the argument types
     * @return a list of matching metamethods
     */
    @Override
    public List<MetaMethod> respondsTo(Object obj, String name, Object[] argTypes) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.respondsTo(owner, name, argTypes);
    }

    /**
     * Returns all metamethods that respond to the given method name.
     *
     * @param obj the object (ignored; the owner is used instead)
     * @param name the method name
     * @return a list of matching metamethods
     */
    @Override
    public List<MetaMethod> respondsTo(Object obj, String name) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.respondsTo(owner, name);
    }

    /**
     * Retrieves the MetaProperty with the given name from the owner's MetaClass.
     *
     * @param obj the object (ignored; the owner is used instead)
     * @param name the property name
     * @return the MetaProperty or null if not found
     */
    @Override
    public MetaProperty hasProperty(Object obj, String name) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.hasProperty(owner, name);
    }

    /**
     * Returns all properties from the owner's MetaClass.
     *
     * @return a list of all properties
     */
    @Override
    public List<MetaProperty> getProperties() {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getProperties();
    }

    /**
     * Gets a property value from the owner's MetaClass.
     *
     * @param object the object (ignored; the owner is used instead)
     * @param property the property name
     * @return the property value
     */
    @Override
    public Object getProperty(Object object, String property) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getProperty(owner, property);
    }

    /**
     * Invokes a constructor on the owner's MetaClass.
     *
     * @param arguments the constructor arguments
     * @return the newly constructed instance
     */
    @Override
    public Object invokeConstructor(Object[] arguments) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.invokeConstructor(arguments);
    }

    /**
     * Invokes a method on the owner's MetaClass with the specified arguments.
     *
     * @param object the object (ignored; the owner is used instead)
     * @param methodName the method name
     * @param arguments the method arguments (as a single object or varargs)
     * @return the method result
     */
    @Override
    public Object invokeMethod(Object object, String methodName, Object arguments) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.invokeMethod(owner, methodName, arguments);
    }

    /**
     * Invokes a method on the owner's MetaClass with the specified array of arguments.
     *
     * @param object the object (ignored; the owner is used instead)
     * @param methodName the method name
     * @param arguments the method arguments as an array
     * @return the method result
     */
    @Override
    public Object invokeMethod(Object object, String methodName, Object[] arguments) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.invokeMethod(owner, methodName, arguments);
    }

    /**
     * Returns the MetaClass of the owner object.
     *
     * @param owner the owner object
     * @return the owner's MetaClass
     */
    protected abstract MetaClass getOwnerMetaClass(Object owner);

    /**
     * Invokes a static method on the owner's MetaClass.
     *
     * @param object the class object
     * @param methodName the method name
     * @param arguments the method arguments
     * @return the method result
     */
    @Override
    public Object invokeStaticMethod(Object object, String methodName, Object[] arguments) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.invokeStaticMethod(object, methodName, arguments);
    }

    /**
     * Sets the value of an attribute on the owner's MetaClass.
     *
     * @param object the object (ignored; the owner is used instead)
     * @param attribute the attribute name
     * @param newValue the new attribute value
     */
    @Override
    public void setAttribute(Object object, String attribute, Object newValue) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        ownerMetaClass.setAttribute(object, attribute, newValue);
    }

    /**
     * Sets a property value on the owner's MetaClass.
     *
     * @param object the object (ignored; the owner is used instead)
     * @param property the property name
     * @param newValue the new property value
     */
    @Override
    public void setProperty(Object object, String property, Object newValue) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        ownerMetaClass.setProperty(object, property, newValue);
    }

    /**
     * Compares this OwnedMetaClass with another object using the delegate's equality.
     *
     * @param obj the object to compare with
     * @return true if equal to the delegate
     */
    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    /**
     * Returns the hash code of the delegate MetaClass.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    /**
     * Returns a string representation of this OwnedMetaClass.
     *
     * @return a string describing this metaclass and its delegate
     */
    @Override
    public String toString() {
        return super.toString() + "[" + delegate.toString()+ "]";
    }

    /**
     * Gets an attribute from the owner's MetaClass with caller context.
     *
     * @param sender the class making the request
     * @param receiver the receiver object (ignored; the owner is used instead)
     * @param messageName the attribute name
     * @param useSuper whether to use the super method
     * @return the attribute value
     */
    @Override
    public Object getAttribute(Class sender, Object receiver, String messageName, boolean useSuper) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getAttribute(sender, receiver, messageName, useSuper);
    }

    /**
     * Gets a property from the owner's MetaClass with caller context.
     *
     * @param sender the class making the request
     * @param receiver the receiver object (ignored; the owner is used instead)
     * @param messageName the property name
     * @param useSuper whether to use the super method
     * @param fromInsideClass whether this is called from inside the class
     * @return the property value
     */
    @Override
    public Object getProperty(Class sender, Object receiver, String messageName, boolean useSuper, boolean fromInsideClass) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getProperty(sender, receiver, messageName, useSuper, fromInsideClass);
    }

    /**
     * Gets the MetaProperty with the given name from the owner's MetaClass.
     *
     * @param name the property name
     * @return the MetaProperty or null if not found
     */
    @Override
    public MetaProperty getMetaProperty(String name) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getMetaProperty(name);
    }

    /**
     * Gets a static metamethod from the owner's MetaClass by name and argument objects.
     *
     * @param name the method name
     * @param args the argument objects
     * @return the MetaMethod or null if not found
     */
    @Override
    public MetaMethod getStaticMetaMethod(String name, Object[] args) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getStaticMetaMethod(name, args);
    }

    /**
     * Gets a static metamethod from the owner's MetaClass by name and argument types.
     *
     * @param name the method name
     * @param argTypes the argument types
     * @return the MetaMethod or null if not found
     */
    @Override
    public MetaMethod getStaticMetaMethod(String name, Class[] argTypes) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getStaticMetaMethod(name, argTypes);
    }

    /**
     * Gets a metamethod from the owner's MetaClass by name and argument objects.
     *
     * @param name the method name
     * @param args the argument objects
     * @return the MetaMethod or null if not found
     */
    @Override
    public MetaMethod getMetaMethod(String name, Object[] args) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getMetaMethod(name, args);
    }

    /**
     * Gets a metamethod from the owner's MetaClass by name and argument types.
     *
     * @param name the method name
     * @param argTypes the argument types
     * @return the MetaMethod or null if not found
     */
    public MetaMethod getMetaMethod(String name, Class[] argTypes) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getMetaMethod(name, argTypes);
    }

    /**
     * Returns the class that this MetaClass represents.
     *
     * @return the owner's class
     */
    @Override
    public Class getTheClass() {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getTheClass();
    }

    /**
     * Invokes a method on the owner's MetaClass with caller context.
     *
     * @param sender the class making the request
     * @param receiver the receiver object (ignored; the owner is used instead)
     * @param methodName the method name
     * @param arguments the method arguments
     * @param isCallToSuper whether this is a super method call
     * @param fromInsideClass whether this is called from inside the class
     * @return the method result
     */
    @Override
    public Object invokeMethod(Class sender, Object receiver, String methodName, Object[] arguments, boolean isCallToSuper, boolean fromInsideClass) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.invokeMethod(sender, owner, methodName, arguments, isCallToSuper, fromInsideClass);
    }

    /**
     * Invokes a missing method on the owner's MetaClass.
     *
     * @param instance the instance (ignored; the owner is used instead)
     * @param methodName the method name that was missing
     * @param arguments the method arguments
     * @return the method result
     */
    @Override
    public Object invokeMissingMethod(Object instance, String methodName, Object[] arguments) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.invokeMissingMethod(owner, methodName, arguments);
    }

    /**
     * Invokes a missing property on the owner's MetaClass.
     *
     * @param instance the instance (ignored; the owner is used instead)
     * @param propertyName the property name that was missing
     * @param optionalValue the optional value (for setters)
     * @param isGetter whether this is a getter operation
     * @return the property value (or null for setters)
     */
    @Override
    public Object invokeMissingProperty(Object instance, String propertyName, Object optionalValue, boolean isGetter) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.invokeMissingProperty(owner, propertyName, optionalValue, isGetter);
    }

    /**
     * Checks if the owner's class is a GroovyObject.
     *
     * @return true if the owner's class implements GroovyObject
     */
    @Override
    public boolean isGroovyObject() {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return GroovyObject.class.isAssignableFrom(ownerMetaClass.getTheClass());
    }

    /**
     * Sets an attribute on the owner's MetaClass with caller context.
     *
     * @param sender the class making the request
     * @param receiver the receiver object (ignored; the owner is used instead)
     * @param messageName the attribute name
     * @param messageValue the new attribute value
     * @param useSuper whether to use the super method
     * @param fromInsideClass whether this is called from inside the class
     */
    @Override
    public void setAttribute(Class sender, Object receiver, String messageName, Object messageValue, boolean useSuper, boolean fromInsideClass) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        ownerMetaClass.setAttribute(sender, owner, messageName, messageValue, useSuper, fromInsideClass);
    }

    /**
     * Sets a property on the owner's MetaClass with caller context.
     *
     * @param sender the class making the request
     * @param receiver the receiver object (ignored; the owner is used instead)
     * @param messageName the property name
     * @param messageValue the new property value
     * @param useSuper whether to use the super method
     * @param fromInsideClass whether this is called from inside the class
     */
    @Override
    public void setProperty(Class sender, Object receiver, String messageName, Object messageValue, boolean useSuper, boolean fromInsideClass) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        ownerMetaClass.setProperty(sender, owner, messageName, messageValue, useSuper, fromInsideClass);
    }

    /**
     * Selects a constructor and transforms arguments for the owner's metaclass.
     *
     * @param numberOfConstructors the number of constructors available
     * @param arguments the constructor arguments to transform
     * @return the selected constructor index
     */
    @Override
    public int selectConstructorAndTransformArguments(int numberOfConstructors, Object[] arguments) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.selectConstructorAndTransformArguments(numberOfConstructors, arguments);
    }
}
