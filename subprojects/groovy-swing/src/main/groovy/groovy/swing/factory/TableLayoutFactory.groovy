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

import groovy.swing.impl.TableLayoutCell

import java.awt.Component
import java.awt.Window
import groovy.swing.impl.TableLayout
import groovy.swing.impl.TableLayoutRow

public class TableLayoutFactory extends AbstractFactory {
    
    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        if (FactoryBuilderSupport.checkValueIsType(value, name, TableLayout.class)) {
            return value;
        }
        return new TableLayout();
    }

    public void setParent(FactoryBuilderSupport builder, Object parent, Object child) {
        if (builder.getParentFactory()) {
            builder.getParentFactory().setChild (builder, parent, child);
        }
    }
}
    
public class TRFactory extends AbstractFactory {
    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        FactoryBuilderSupport.checkValueIsNull(value, name);
        //TODO we could make the value arg the parent
        Object parent = builder.getCurrent();
        if (parent instanceof TableLayout) {
            return new TableLayoutRow((TableLayout) parent);
        } else {
            throw new RuntimeException("'tr' must be within a 'tableLayout'");
        }
    }

    public void onNodeCompleted(FactoryBuilderSupport builder, Object parent, Object node) {
        node.addComponentsForRow()
    }
}

public class TDFactory extends AbstractFactory {
    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        FactoryBuilderSupport.checkValueIsNull(value, name);
        //TODO we could make the value arg the TR
        Object parent = builder.getCurrent();
        if (parent instanceof TableLayoutRow) {
            return new TableLayoutCell((TableLayoutRow) parent);
        } else {
            throw new RuntimeException("'td' must be within a 'tr'");
        }
    }

    public void setChild(FactoryBuilderSupport builder, Object parent, Object child) {
        if (!(child instanceof Component) || (child instanceof Window)) {
            return;
        }
        parent.addComponent(child)
    }
}
