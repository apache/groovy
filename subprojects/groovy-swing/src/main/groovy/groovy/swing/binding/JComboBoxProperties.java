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
package groovy.swing.binding;

import org.apache.groovy.swing.binding.PropertyBinding;
import org.apache.groovy.swing.binding.TargetBinding;
import org.apache.groovy.swing.binding.TriggerBinding;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Supplies synthetic binding definitions for {@link JComboBox}.
 *
 * @since Groovy 1.1
 */
public class JComboBoxProperties {
    /**
     * Returns the synthetic trigger bindings exposed for {@link JComboBox}.
     *
     * @return the synthetic trigger binding map
     */
    public static Map<String, TriggerBinding> getSyntheticProperties() {
        Map<String, TriggerBinding> result = new HashMap<String, TriggerBinding>();

        // to match property name
        result.put(JComboBox.class.getName() + "#selectedItem", (source, target) -> new JComboBoxSelectedElementBinding((PropertyBinding) source, target, "selectedItem"));
        // to match JSR-295
        result.put(JComboBox.class.getName() + "#selectedElement", (source, target) -> new JComboBoxSelectedElementBinding((PropertyBinding) source, target, "selectedElement"));

        result.put(JComboBox.class.getName() + "#selectedIndex", (source, target) -> new JComboBoxSelectedIndexBinding((PropertyBinding) source, target));


        // to match JSR-295
        result.put(JComboBox.class.getName() + "#elements", (source, target) -> new JComboBoxElementsBinding((PropertyBinding) source, target));

        return result;
    }
}


/**
 * Tracks the synthetic selected-element properties on a {@link JComboBox}.
 */
@SuppressWarnings("rawtypes")
class JComboBoxSelectedElementBinding extends AbstractSyntheticBinding implements PropertyChangeListener, ItemListener {
    /**
     * The currently bound combo box instance.
     */
    JComboBox boundComboBox;

    /**
     * Creates a selected-element binding for a combo box.
     *
     * @param source the source property binding
     * @param target the target binding
     * @param propertyName the synthetic property name to observe
     */
    JComboBoxSelectedElementBinding(PropertyBinding source, TargetBinding target, String propertyName) {
        super(source, target, JComboBox.class, propertyName);
    }

    /**
     * Starts listening to the bound combo box and its model.
     */
    @Override
    public synchronized void syntheticBind() {
        boundComboBox = (JComboBox) ((PropertyBinding)sourceBinding).getBean();
        boundComboBox.addPropertyChangeListener("model", this);
        boundComboBox.addItemListener(this);
    }

    /**
     * Stops listening to the bound combo box and clears the cached reference.
     */
    @Override
    public synchronized void syntheticUnbind() {
        boundComboBox.removePropertyChangeListener("model", this);
        boundComboBox.removeItemListener(this);
        boundComboBox = null;
    }

    /**
     * Stores the target binding that receives selected-item updates.
     *
     * @param target the target binding
     */
    @Override
    public void setTargetBinding(TargetBinding target) {
        super.setTargetBinding(target);
    }

    /**
     * Refreshes the binding after a combo-box model change.
     *
     * @param event the model change event
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        update();
    }

    /**
     * Refreshes the binding after a combo-box selection change.
     *
     * @param e the item event describing the selection change
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        update();
    }

}

/**
 * Tracks the synthetic {@code selectedIndex} property on a {@link JComboBox}.
 */
@SuppressWarnings("rawtypes")
class JComboBoxSelectedIndexBinding extends AbstractSyntheticBinding implements PropertyChangeListener, ItemListener {
    /**
     * The currently bound combo box instance.
     */
    JComboBox boundComboBox;

    /**
     * Creates a selected-index binding for a combo box.
     *
     * @param source the source property binding
     * @param target the target binding
     */
    JComboBoxSelectedIndexBinding(PropertyBinding source, TargetBinding target) {
        super(source, target, JComboBox.class, "selectedIndex");
    }

    /**
     * Starts listening to the bound combo box and its model.
     */
    @Override
    public synchronized void syntheticBind() {
        boundComboBox = (JComboBox) ((PropertyBinding)sourceBinding).getBean();
        boundComboBox.addPropertyChangeListener("model", this);
        boundComboBox.addItemListener(this);
    }

    /**
     * Stops listening to the bound combo box and clears the cached reference.
     */
    @Override
    public synchronized void syntheticUnbind() {
        boundComboBox.removePropertyChangeListener("model", this);
        boundComboBox.removeItemListener(this);
        boundComboBox = null;
    }

    /**
     * Stores the target binding that receives selected-index updates.
     *
     * @param target the target binding
     */
    @Override
    public void setTargetBinding(TargetBinding target) {
        super.setTargetBinding(target);
    }

    /**
     * Refreshes the binding after a combo-box model change.
     *
     * @param event the model change event
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        update();
    }

    /**
     * Refreshes the binding after a combo-box selection change.
     *
     * @param e the item event describing the selection change
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        update();
    }

}

/**
 * Tracks the synthetic {@code elements} property on a {@link JComboBox}.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
class JComboBoxElementsBinding extends AbstractSyntheticBinding implements ListDataListener, PropertyChangeListener {
    /**
     * The currently bound combo box instance.
     */
    JComboBox boundComboBox;

    /**
     * Creates an elements binding for a combo box.
     *
     * @param propertyBinding the source property binding
     * @param target the target binding
     */
    JComboBoxElementsBinding(PropertyBinding propertyBinding, TargetBinding target) {
        super(propertyBinding, target, JComboBox.class, "elements");
    }

    /**
     * Starts listening to the bound combo-box model.
     */
    @Override
    protected void syntheticBind() {
        boundComboBox = (JComboBox) ((PropertyBinding)sourceBinding).getBean();
        boundComboBox.addPropertyChangeListener("model", this);
        boundComboBox.getModel().addListDataListener(this);
    }

    /**
     * Stops listening to the bound combo-box model.
     */
    @Override
    protected void syntheticUnbind() {
        boundComboBox.removePropertyChangeListener("model", this);
        boundComboBox.getModel().removeListDataListener(this);
    }

    /**
     * Refreshes the binding after the combo-box model instance changes.
     *
     * @param event the model change event
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        update();
        ((ComboBoxModel) event.getOldValue()).removeListDataListener(this);
        ((ComboBoxModel) event.getNewValue()).addListDataListener(this);
    }

    /**
     * Refreshes the binding after items are inserted into the model.
     *
     * @param e the list-data event
     */
    @Override
    public void intervalAdded(ListDataEvent e) {
        update();
    }

    /**
     * Refreshes the binding after items are removed from the model.
     *
     * @param e the list-data event
     */
    @Override
    public void intervalRemoved(ListDataEvent e) {
        update();
    }

    /**
     * Refreshes the binding after existing items in the model change.
     *
     * @param e the list-data event
     */
    @Override
    public void contentsChanged(ListDataEvent e) {
        update();
    }
}
