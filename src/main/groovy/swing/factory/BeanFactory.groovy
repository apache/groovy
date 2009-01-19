/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.swing.factory

/**
 * @author <a href="mailto:shemnon@yahoo.com">Danno Ferrin</a>
 * @version $Revision: 7953 $
 * @since Groovy 1.1
 */
class BeanFactory extends AbstractFactory {
    final Class beanClass
    final protected boolean leaf

    public BeanFactory(Class beanClass) {
        this(beanClass, false)
    }

    public BeanFactory(Class beanClass, boolean leaf) {
        this.beanClass = beanClass
        this.leaf = leaf
    }

    public boolean isLeaf() {
        return leaf
    }

    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        if (value instanceof GString) value = value as String
        if (FactoryBuilderSupport.checkValueIsTypeNotString(value, name, beanClass)) {
            return value
        }
        Object bean = beanClass.newInstance()
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
