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

import groovy.transform.Synchronized
import org.apache.groovy.swing.binding.FullBinding
import org.apache.groovy.swing.binding.PropertyBinding
import org.apache.groovy.swing.binding.SourceBinding
import org.apache.groovy.swing.binding.TargetBinding
import org.apache.groovy.swing.binding.TriggerBinding

import javax.swing.JList
import javax.swing.ListModel
import javax.swing.event.ListDataEvent
import javax.swing.event.ListDataListener
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

/**
 * Declares synthetic bindable properties for {@link JList} contents and selection state.
 *
 * @since Groovy 1.7.5
 */
class JListProperties {
    /**
     * Returns trigger bindings keyed by synthetic {@code JList} property name.
     *
     * @return the synthetic trigger bindings supported for {@code JList}
     */
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

/**
 * Tracks changes to the bound list model and propagates updates for the synthetic {@code elements} property.
 */
class JListElementsBinding extends AbstractSyntheticBinding implements ListDataListener, PropertyChangeListener {
    /**
     * List currently supplying element events for this binding.
     */
    JList boundList

    /**
     * Creates a binding for the synthetic {@code elements} property.
     *
     * @param propertyBinding the source property binding
     * @param target the target binding to update
     */
    JListElementsBinding(PropertyBinding propertyBinding, TargetBinding target) {
        super(propertyBinding, target, JList.class, "elements")
    }

    /**
     * Starts listening to the list model and model replacement events.
     */
    protected void syntheticBind() {
        boundList = (JList) ((PropertyBinding) sourceBinding).getBean()
        boundList.addPropertyChangeListener("model", this)
        boundList.getModel().addListDataListener(this)
    }

    /**
     * Stops listening to the list model and model replacement events.
     */
    protected void syntheticUnbind() {
        boundList.removePropertyChangeListener("model", this)
        boundList.getModel().removeListDataListener(this)
    }

    /**
     * Rebinds listeners when the list model instance changes.
     *
     * @param event the model property change event
     */
    void propertyChange(PropertyChangeEvent event) {
        update()
        ((ListModel) event.getOldValue()).removeListDataListener(this)
        ((ListModel) event.getNewValue()).addListDataListener(this)
    }

    /**
     * Updates the target when items are inserted into the model.
     *
     * @param e the list data event
     */
    void intervalAdded(ListDataEvent e) {
        update()
    }

    /**
     * Updates the target when items are removed from the model.
     *
     * @param e the list data event
     */
    void intervalRemoved(ListDataEvent e) {
        update()
    }

    /**
     * Updates the target when existing model items change.
     *
     * @param e the list data event
     */
    void contentsChanged(ListDataEvent e) {
        update()
    }
}

/**
 * Tracks selection-related synthetic {@link JList} properties and forwards updates to a target binding.
 */
class JListSelectedElementBinding extends AbstractSyntheticBinding implements PropertyChangeListener, ListSelectionListener {
    private JList boundList

    /**
     * Returns the list currently bound to this synthetic selection binding.
     *
     * @return the bound list, or {@code null} when unbound
     */
    @Synchronized JList getBoundList() {
        return boundList
    }

    /**
     * Sets the list currently bound to this synthetic selection binding.
     *
     * @param boundList the bound list
     */
    @Synchronized void setBoundList(JList boundList) {
        this.boundList = boundList
    }

    /**
     * Creates a binding for a synthetic selection-related property.
     *
     * @param source the source property binding
     * @param target the target binding to update
     * @param propertyName the synthetic property name to expose
     */
    protected JListSelectedElementBinding(PropertyBinding source, TargetBinding target, String propertyName) {
        super(source, target, JList.class, propertyName)
    }

    /**
     * Starts listening to selection model replacement and selection changes.
     */
    @Synchronized void syntheticBind() {
        boundList = (JList) ((PropertyBinding) sourceBinding).getBean()
        boundList.addPropertyChangeListener("selectionModel", this)
        boundList.addListSelectionListener(this)
    }

    /**
     * Stops listening to selection model replacement and selection changes.
     */
    @Synchronized void syntheticUnbind() {
        boundList.removePropertyChangeListener("selectionModel", this)
        boundList.removeListSelectionListener(this)
        boundList = null
    }

    /**
     * Sets the target binding to receive synthetic selection updates.
     *
     * @param target the target binding
     */
    void setTargetBinding(TargetBinding target) {
        super.setTargetBinding(target)
    }

    /**
     * Updates the target when the list selection model instance changes.
     *
     * @param event the property change event
     */
    void propertyChange(PropertyChangeEvent event) {
        update()
    }

    /**
     * Updates the target when the list selection changes.
     *
     * @param e the selection event
     */
    void valueChanged(ListSelectionEvent e) {
        update()
    }
}
