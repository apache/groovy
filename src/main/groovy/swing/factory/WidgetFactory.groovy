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

import java.awt.Component
import java.awt.Window
import javax.swing.JComponent
import static groovy.swing.factory.LayoutFactory.DEFAULT_DELEGATE_PROPERTY_CONSTRAINT

public class WidgetFactory extends AbstractFactory {

    final Class restrictedType;
    protected final boolean leaf

    public WidgetFactory(Class restrictedType, boolean leaf) {
        this.restrictedType = restrictedType
        this.leaf = leaf
    }

    boolean isLeaf() {
        return leaf
    }

    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        if (value == null) {
            value = attributes.remove(name);
        }
        if ((value != null) && FactoryBuilderSupport.checkValueIsType(value, name, restrictedType)) {
            return value;
        } else {
            throw new RuntimeException("$name must have either a value argument or an attribute named $name that must be of type $restrictedType.name");
        }
    }

    public void setChild(FactoryBuilderSupport builder, Object parent, Object child) {
        if (!(child instanceof Component) || (child instanceof Window)) {
            return;
        }
        try {
            def constraints = builder.context.constraints
            if (constraints != null) {
                LayoutFactory.getLayoutTarget(parent).add(child, constraints)
                if (child instanceof JComponent) {
                    child.putClientProperty(DEFAULT_DELEGATE_PROPERTY_CONSTRAINT, constraints)
                }
                builder.context.remove('constraints')
            } else {
                LayoutFactory.getLayoutTarget(parent).add(child)
            }
        } catch (MissingPropertyException mpe) {
            LayoutFactory.getLayoutTarget(parent).add(child)
        }
    }

}
