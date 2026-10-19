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
package org.apache.groovy.groovyfx.factory

import org.codehaus.groovy.runtime.InvokerHelper

/**
 * Reflective factory for JavaFX beans and scene-graph nodes.
 * <p>
 * An existing instance of the bean class passed as the node value is used as
 * is. Any other value is passed to a matching constructor; when there is none
 * and the value is text, the bean is created with its no-argument constructor
 * and the value becomes its {@code text}. Attributes are applied through
 * {@link FXAttributes} and children attached through {@link FXChildren}.
 */
class NodeFactory extends AbstractFactory {

    final Class beanClass
    final boolean leaf

    NodeFactory(Class beanClass, boolean leaf = false) {
        this.beanClass = beanClass
        this.leaf = leaf
    }

    @Override
    boolean isLeaf() {
        leaf
    }

    @Override
    Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        if (value == null) {
            return beanClass.getDeclaredConstructor().newInstance()
        }
        if (beanClass.isInstance(value)) {
            return value
        }
        construct(name, value)
    }

    protected Object construct(Object name, Object value) {
        try {
            return InvokerHelper.invokeConstructorOf(beanClass, value)
        } catch (GroovyRuntimeException e) {
            Object node = beanClass.getDeclaredConstructor().newInstance()
            MetaProperty text = InvokerHelper.getMetaClass(node).hasProperty(node, 'text')
            if (value instanceof CharSequence && text != null) {
                text.setProperty(node, value.toString())
                return node
            }
            throw new IllegalArgumentException("Cannot create '${name}' from ${value}: ${beanClass.name} has no matching constructor", e)
        }
    }

    @Override
    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, Object node, Map attributes) {
        FXAttributes.apply(node, attributes)
        false
    }

    @Override
    void setChild(FactoryBuilderSupport builder, Object parent, Object child) {
        FXChildren.attach(parent, child)
    }
}
