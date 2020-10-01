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

    @Override
    public Object getAttribute(Object object, String attribute) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getAttribute(owner, attribute);
    }

    protected abstract Object getOwner();

    @Override
    public ClassNode getClassNode() {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getClassNode();
    }

    @Override
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

    @Override
    public List<MetaMethod> respondsTo(Object obj, String name, Object[] argTypes) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.respondsTo(owner, name, argTypes);
    }

    @Override
    public List<MetaMethod> respondsTo(Object obj, String name) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.respondsTo(owner, name);
    }

    @Override
    public MetaProperty hasProperty(Object obj, String name) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.hasProperty(owner, name);
    }

    @Override
    public List<MetaProperty> getProperties() {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getProperties();
    }

    @Override
    public Object getProperty(Object object, String property) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getProperty(owner, property);
    }

    @Override
    public Object invokeConstructor(Object[] arguments) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.invokeConstructor(arguments);
    }

    @Override
    public Object invokeMethod(Object object, String methodName, Object arguments) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.invokeMethod(owner, methodName, arguments);
    }

    @Override
    public Object invokeMethod(Object object, String methodName, Object[] arguments) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.invokeMethod(owner, methodName, arguments);
    }

    protected abstract MetaClass getOwnerMetaClass(Object owner);

    @Override
    public Object invokeStaticMethod(Object object, String methodName, Object[] arguments) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.invokeStaticMethod(object, methodName, arguments);
    }

    @Override
    public void setAttribute(Object object, String attribute, Object newValue) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        ownerMetaClass.setAttribute(object, attribute, newValue);
    }

    @Override
    public void setProperty(Object object, String property, Object newValue) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        ownerMetaClass.setProperty(object, property, newValue);
    }

    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return super.toString() + "[" + delegate.toString()+ "]";
    }

    @Override
    public Object getAttribute(Class sender, Object receiver, String messageName, boolean useSuper) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getAttribute(sender, receiver, messageName, useSuper);
    }

    @Override
    public Object getProperty(Class sender, Object receiver, String messageName, boolean useSuper, boolean fromInsideClass) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getProperty(sender, receiver, messageName, useSuper, fromInsideClass);
    }

    @Override
    public MetaProperty getMetaProperty(String name) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getMetaProperty(name);
    }

    @Override
    public MetaMethod getStaticMetaMethod(String name, Object[] args) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getStaticMetaMethod(name, args);
    }

    @Override
    public MetaMethod getStaticMetaMethod(String name, Class[] argTypes) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getStaticMetaMethod(name, argTypes);
    }

    @Override
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

    @Override
    public Class getTheClass() {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.getTheClass();
    }

    @Override
    public Object invokeMethod(Class sender, Object receiver, String methodName, Object[] arguments, boolean isCallToSuper, boolean fromInsideClass) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.invokeMethod(sender, owner, methodName, arguments, isCallToSuper, fromInsideClass);
    }

    @Override
    public Object invokeMissingMethod(Object instance, String methodName, Object[] arguments) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.invokeMissingMethod(owner, methodName, arguments);
    }

    @Override
    public Object invokeMissingProperty(Object instance, String propertyName, Object optionalValue, boolean isGetter) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.invokeMissingProperty(owner, propertyName, optionalValue, isGetter);
    }

    @Override
    public boolean isGroovyObject() {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return GroovyObject.class.isAssignableFrom(ownerMetaClass.getTheClass());
    }

    @Override
    public void setAttribute(Class sender, Object receiver, String messageName, Object messageValue, boolean useSuper, boolean fromInsideClass) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        ownerMetaClass.setAttribute(sender, owner, messageName, messageValue, useSuper, fromInsideClass);
    }

    @Override
    public void setProperty(Class sender, Object receiver, String messageName, Object messageValue, boolean useSuper, boolean fromInsideClass) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        ownerMetaClass.setProperty(sender, owner, messageName, messageValue, useSuper, fromInsideClass);
    }

    @Override
    public int selectConstructorAndTransformArguments(int numberOfConstructors, Object[] arguments) {
        final Object owner = getOwner();
        final MetaClass ownerMetaClass = getOwnerMetaClass(owner);
        return ownerMetaClass.selectConstructorAndTransformArguments(numberOfConstructors, arguments);
    }
}
