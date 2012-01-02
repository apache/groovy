/*
 * Copyright 2003-2008 the original author or authors.
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

import groovy.swing.binding.JTableMetaMethods
import javax.swing.JTable
import javax.swing.table.TableColumn
import javax.swing.table.TableModel

class TableFactory extends BeanFactory {

    public TableFactory() {
        this(JTable)
    }

    public TableFactory(Class klass) {
        super(klass, false)
    }

    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        Object table = super.newInstance(builder, name, value, attributes);
        // insure metaproperties are registered
        JTableMetaMethods.enhanceMetaClass(table)
        return table
    }

    public void setChild(FactoryBuilderSupport builder, Object parent, Object child) {
        if (child instanceof TableColumn) {
            parent.addColumn(child);
        } else if (child instanceof TableModel) {
            parent.model = child;
        }
    }

}

