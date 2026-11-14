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

    /**
     * Remembers {@code beanClass}. {@code leaf} defaults to {@code false}
     * and is what {@link #isLeaf()} reports.
     *
     * @param beanClass the JavaFX type this factory builds
     * @param leaf whether the node accepts children
     */
    NodeFactory(Class beanClass, boolean leaf = false) {
        this.beanClass = beanClass
        this.leaf = leaf
    }

    /**
     * Reports the leaf flag stored by the constructor.
     *
     * @return {@code true} when the node does not accept children
     */
    @Override
    boolean isLeaf() {
        leaf
    }

    /**
     * Returns {@code value} when it is already an instance of the bean class.
     * A null value is created with the no-argument constructor. Any other
     * value is passed to {@link #construct(Object)}.
     *
     * @param builder the builder, unused
     * @param name the node name, unused
     * @param value an existing instance, text, or constructor argument
     * @param attributes node attributes, unused here
     * @return the bean instance
     */
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

    /**
     * Builds a node from {@code value}. A character sequence is written to
     * the bean's {@code text} property when that property exists and no
     * constructor accepts a {@code String}. Otherwise the bean class is
     * constructed with {@code value} through {@link InvokerHelper}.
     *
     * @param value text or a constructor argument
     * @return the new bean
     */
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

    /**
     * Applies {@code attributes} through {@code FXAttributes.apply} and
     * returns {@code false} so the builder does not apply them again.
     *
     * @param builder the builder, unused
     * @param node the bean created for this node
     * @param attributes attributes from the builder
     * @return {@code false}
     */
    @Override
    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, Object node, Map attributes) {
        FXAttributes.apply(node, attributes)
        false
    }

    /**
     * Attaches {@code child} to {@code parent} through {@code FXChildren.attach}.
     *
     * @param builder the builder, unused
     * @param parent the node that receives the child
     * @param child the child node
     */
    @Override
    void setChild(FactoryBuilderSupport builder, Object parent, Object child) {
        FXChildren.attach(parent, child)
    }
}
