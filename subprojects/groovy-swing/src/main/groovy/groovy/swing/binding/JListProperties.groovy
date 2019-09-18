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
package groovy.swing.binding

import org.apache.groovy.swing.binding.FullBinding
import org.apache.groovy.swing.binding.PropertyBinding
import org.apache.groovy.swing.binding.SourceBinding
import org.apache.groovy.swing.binding.TargetBinding
import org.apache.groovy.swing.binding.TriggerBinding

import javax.swing.*
import javax.swing.event.ListDataEvent
import javax.swing.event.ListDataListener
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

/**
 * @since Groovy 1.7.5
 */
class JListProperties {
    static Map<String, TriggerBinding> getSyntheticProperties() {
        Map<String, TriggerBinding> result = new HashMap<String, TriggerBinding>()

        // to match property name
        result.put(JList.class.getName() + "#selectedValue", new TriggerBinding() {
            FullBinding createBinding(SourceBinding source, TargetBinding target) {
                return new JListSelectedElementBinding((PropertyBinding) source, target, "selectedValue")
            }
        })

        // to match JSR-295
        result.put(JList.class.getName() + "#selectedElement", new TriggerBinding() {
            FullBinding createBinding(SourceBinding source, TargetBinding target) {
                return new JListSelectedElementBinding((PropertyBinding) source, target, "selectedElement")
            }
        })

        // to match property name
        result.put(JList.class.getName() + "#selectedValues", new TriggerBinding() {
            FullBinding createBinding(SourceBinding source, TargetBinding target) {
                return new JListSelectedElementBinding((PropertyBinding) source, target, "selectedValues")
            }
        })

        // to match JSR-295
        result.put(JList.class.getName() + "#selectedElements", new TriggerBinding() {
            FullBinding createBinding(SourceBinding source, TargetBinding target) {
                return new JListSelectedElementBinding((PropertyBinding) source, target, "selectedElements")
            }
        })

        result.put(JList.class.getName() + "#selectedIndex", new TriggerBinding() {
            FullBinding createBinding(SourceBinding source, TargetBinding target) {
                return new JListSelectedElementBinding((PropertyBinding) source, target, "selectedIndex")
            }
        })

        result.put(JList.class.getName() + "#selectedIndices", new TriggerBinding() {
            FullBinding createBinding(SourceBinding source, TargetBinding target) {
                return new JListSelectedElementBinding((PropertyBinding) source, target, "selectedIndices")
            }
        })

        // to match JSR-295
        result.put(JList.class.getName() + "#elements", new TriggerBinding() {
            FullBinding createBinding(SourceBinding source, TargetBinding target) {
                return new JListElementsBinding((PropertyBinding) source, target)
            }
        })

        return result
    }
}

class JListElementsBinding extends AbstractSyntheticBinding implements ListDataListener, PropertyChangeListener {
    JList boundList

    JListElementsBinding(PropertyBinding propertyBinding, TargetBinding target) {
        super(propertyBinding, target, JList.class, "elements")
    }

    protected void syntheticBind() {
        boundList = (JList) ((PropertyBinding) sourceBinding).getBean()
        boundList.addPropertyChangeListener("model", this)
        boundList.getModel().addListDataListener(this)
    }

    protected void syntheticUnbind() {
        boundList.removePropertyChangeListener("model", this)
        boundList.getModel().removeListDataListener(this)
    }

    void propertyChange(PropertyChangeEvent event) {
        update()
        ((ListModel) event.getOldValue()).removeListDataListener(this)
        ((ListModel) event.getNewValue()).addListDataListener(this)
    }

    void intervalAdded(ListDataEvent e) {
        update()
    }

    void intervalRemoved(ListDataEvent e) {
        update()
    }

    void contentsChanged(ListDataEvent e) {
        update()
    }
}

class JListSelectedElementBinding extends AbstractSyntheticBinding implements PropertyChangeListener, ListSelectionListener {
    JList boundList

    protected JListSelectedElementBinding(PropertyBinding source, TargetBinding target, String propertyName) {
        super(source, target, JList.class, propertyName)
    }

    synchronized void syntheticBind() {
        boundList = (JList) ((PropertyBinding) sourceBinding).getBean()
        boundList.addPropertyChangeListener("selectionModel", this)
        boundList.addListSelectionListener(this)
    }

    synchronized void syntheticUnbind() {
        boundList.removePropertyChangeListener("selectionModel", this)
        boundList.removeListSelectionListener(this)
        boundList = null
    }

    void setTargetBinding(TargetBinding target) {
        super.setTargetBinding(target)
    }

    void propertyChange(PropertyChangeEvent event) {
        update()
    }

    void valueChanged(ListSelectionEvent e) {
        update()
    }
}
