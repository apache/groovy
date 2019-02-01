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

public abstract class OwnedMetaClass extends DelegatingMetaClass {
    public OwnedMetaClass(final MetaClass delegate) {
        super(delegate);
    }

    public Object getAttribute(Object object, String attribute) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getAttribute(owner, attribute);
    }

    protected abstract Object getOwner();

    public ClassNode getClassNode() {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getClassNode();
    }

    public List<MetaMethod> getMetaMethods() {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getMetaMethods();
    }

    @Override
    public List<MetaMethod> getMethods() {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getMethods();
    }

    public List<MetaMethod> respondsTo(Object obj, String name, Object[] argTypes) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.respondsTo(owner, name, argTypes);
    }

    public List<MetaMethod> respondsTo(Object obj, String name) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.respondsTo(owner, name);
    }

    public MetaProperty hasProperty(Object obj, String name) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.hasProperty(owner, name);
    }

    public List<MetaProperty> getProperties() {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getProperties();
    }

    public Object getProperty(Object object, String property) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getProperty(owner, property);
    }

    public Object invokeConstructor(Object[] arguments) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.invokeConstructor(arguments);
    }

    public Object invokeMethod(Object object, String methodName, Object arguments) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.invokeMethod(owner, methodName, arguments);
    }

    public Object invokeMethod(Object object, String methodName, Object[] arguments) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.invokeMethod(owner, methodName, arguments);
    }

    protected abstract MetaClass getOwnerMetaClass(Object owner);

    public Object invokeStaticMethod(Object object, String methodName, Object[] arguments) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.invokeStaticMethod(object, methodName, arguments);
    }

    public void setAttribute(Object object, String attribute, Object newValue) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        ownerMetaClass.setAttribute(object, attribute, newValue);
    }

    public void setProperty(Object object, String property, Object newValue) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        ownerMetaClass.setProperty(object, property, newValue);
    }

    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    public int hashCode() {
        return delegate.hashCode();
    }

    public String toString() {
        return super.toString() + "[" + delegate.toString()+ "]";
    }

    public Object getAttribute(Class sender, Object receiver, String messageName, boolean useSuper) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getAttribute(sender, receiver, messageName, useSuper);
    }

    public Object getProperty(Class sender, Object receiver, String messageName, boolean useSuper, boolean fromInsideClass) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getProperty(sender, receiver, messageName, useSuper, fromInsideClass);
    }

    public MetaProperty getMetaProperty(String name) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getMetaProperty(name);
    }

    public MetaMethod getStaticMetaMethod(String name, Object[] args) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getStaticMetaMethod(name, args);
    }

    public MetaMethod getStaticMetaMethod(String name, Class[] argTypes) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getStaticMetaMethod(name, argTypes);
    }

    public MetaMethod getMetaMethod(String name, Object[] args) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getMetaMethod(name, args);
    }

    public MetaMethod getMetaMethod(String name, Class[] argTypes) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getMetaMethod(name, argTypes);
    }

    public Class getTheClass() {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getTheClass();
    }

    public Object invokeMethod(Class sender, Object receiver, String methodName, Object[] arguments, boolean isCallToSuper, boolean fromInsideClass) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.invokeMethod(sender, owner, methodName, arguments, isCallToSuper, fromInsideClass);
    }

    public Object invokeMissingMethod(Object instance, String methodName, Object[] arguments) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.invokeMissingMethod(owner, methodName, arguments);
    }

    public Object invokeMissingProperty(Object instance, String propertyName, Object optionalValue, boolean isGetter) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.invokeMissingProperty(owner, propertyName, optionalValue, isGetter);
    }

    public boolean isGroovyObject() {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return GroovyObject.class.isAssignableFrom(ownerMetaClass.getTheClass());
    }

    public void setAttribute(Class sender, Object receiver, String messageName, Object messageValue, boolean useSuper, boolean fromInsideClass) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        ownerMetaClass.setAttribute(sender, owner, messageName, messageValue, useSuper, fromInsideClass);
    }

    public void setProperty(Class sender, Object receiver, String messageName, Object messageValue, boolean useSuper, boolean fromInsideClass) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        ownerMetaClass.setProperty(sender, owner, messageName, messageValue, useSuper, fromInsideClass);
    }

    public int selectConstructorAndTransformArguments(int numberOfConstructors, Object[] arguments) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.selectConstructorAndTransformArguments(numberOfConstructors, arguments);
    }
}
