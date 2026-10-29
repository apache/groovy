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
 * is. Text is passed to a constructor accepting it when the bean has one;
 * otherwise the bean is created with its no-argument constructor and the text
 * becomes its {@code text} property. Any other value is passed to a matching
 * constructor. Attributes are applied through {@link FXAttributes} and
 * children attached through {@link FXChildren}.
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
        construct(value)
    }

    protected Object construct(Object value) {
        if (value instanceof CharSequence && !hasConstructorAccepting(String)) {
            MetaProperty text = InvokerHelper.getMetaClass(beanClass).getMetaProperty('text')
            if (text != null) {
                Object node = beanClass.getDeclaredConstructor().newInstance()
                text.setProperty(node, value.toString())
                return node
            }
        }
        // a constructor that runs and fails reports its own failure
        InvokerHelper.invokeConstructorOf(beanClass, value)
    }

    private boolean hasConstructorAccepting(Class type) {
        beanClass.constructors.any { it.parameterCount == 1 && it.parameterTypes[0].isAssignableFrom(type) }
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
