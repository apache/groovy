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

import java.awt.Container
import javax.swing.RootPaneContainer

class LayoutFactory extends BeanFactory {

    def contextProps

    public LayoutFactory(Class klass) {
        super(klass)
    }

    public LayoutFactory(Class klass, boolean leaf) {
        super(klass, leaf)
    }

    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        Object o = super.newInstance(builder, name, value, attributes); //To change body of overridden methods use File | Settings | File Templates.
        addLayoutProperties(builder.getContext());
        return o;
    }

    public void addLayoutProperties(context, Class layoutClass) {
        if (contextProps == null) {
            contextProps = [:]
            layoutClass.fields.each {
                def name = it.name
                if (name.toUpperCase() == name) {
                    contextProps.put(name, layoutClass."$name")
                }
            }
        }

        context.putAll(contextProps)
    }

    public void addLayoutProperties(context) {
        addLayoutProperties(context, beanClass)
    }

    public void setParent(FactoryBuilderSupport builder, Object parent, Object child) {
        if (parent instanceof Container) {
            getLayoutTarget(parent).layout = child
        }
    }

    public static Container getLayoutTarget(Container parent) {
        if (parent instanceof RootPaneContainer) {
            RootPaneContainer rpc = (RootPaneContainer) parent;
            parent = rpc.getContentPane();
        }
        return parent;
    }
}