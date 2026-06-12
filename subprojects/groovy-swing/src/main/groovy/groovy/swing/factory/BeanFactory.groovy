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
package groovy.swing.factory

/**
 * @since Groovy 1.1
 */
class BeanFactory extends AbstractFactory {
    /**
     * Bean type instantiated by this factory.
     */
    final Class beanClass
    /**
     * Whether created nodes are treated as leaves.
     */
    final protected boolean leaf

    /**
     * Creates a new base factory that instantiates bean types for SwingBuilder
     *
     * @param beanClass the bean type created by this factory
     */
    BeanFactory(Class beanClass) {
        this(beanClass, false)
    }

    /**
     * Creates a new base factory that instantiates bean types for SwingBuilder
     *
     * @param beanClass the bean type created by this factory
     * @param leaf whether created nodes are leaf nodes
     */
    BeanFactory(Class beanClass, boolean leaf) {
        this.beanClass = beanClass
        this.leaf = leaf
    }

    /**
     * Indicates whether nodes created by this factory accept children.
     *
     * @return true if child nodes are not expected
     */
    boolean isLeaf() {
        return leaf
    }

    /**
     * Creates the node handled by this factory.
     *
     * @param builder the factory builder
     * @param name the node name
     * @param value the node value
     * @param attributes the node attributes
     * @return the created or reused node
     */
    Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        if (value instanceof GString) value = value as String
        if (FactoryBuilderSupport.checkValueIsTypeNotString(value, name, beanClass)) {
            return value
        }
        Object bean = beanClass.getConstructor().newInstance()
        if (value instanceof String) {
            try {
                bean.text = value
            } catch (MissingPropertyException mpe) {
                throw new RuntimeException("In $name value argument of type String cannot be applied to property text:");
            }
        }
        return bean
    }
}
