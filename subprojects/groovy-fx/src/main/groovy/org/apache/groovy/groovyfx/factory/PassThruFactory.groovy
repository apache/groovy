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

/**
 * Wraps an existing object so it can take part in a builder tree: the
 * {@code node} form accepts a scene-graph node and may have children, the
 * {@code bean} form accepts any object and is a leaf. The object is given as
 * the value or as an attribute named after the node:
 * <pre>
 * vbox {
 *     node(existingButton)
 *     bean(model, name: 'Groovy')
 * }
 * </pre>
 */
class PassThruFactory extends AbstractFactory {

    final Class restrictedType
    final boolean leaf

    PassThruFactory(Class restrictedType, boolean leaf) {
        this.restrictedType = restrictedType
        this.leaf = leaf
    }

    @Override
    boolean isLeaf() {
        leaf
    }

    @Override
    Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        if (value == null) {
            value = attributes.remove(name)
        }
        if (value != null && restrictedType.isInstance(value)) {
            return value
        }
        throw new IllegalArgumentException("'${name}' needs a value of type ${restrictedType.name}, either as the argument or as an attribute named '${name}'")
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
