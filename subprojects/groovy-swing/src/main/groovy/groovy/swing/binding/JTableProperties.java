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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Supplies synthetic binding definitions for {@link JTable}.
 */
public class JTableProperties {
    /**
     * Returns the synthetic trigger bindings exposed for {@link JTable}.
     *
     * @return the synthetic trigger binding map
     */
    public static Map<String, TriggerBinding> getSyntheticProperties() {
        Map<String, TriggerBinding> result = new HashMap<String, TriggerBinding>();
        result.put(JTable.class.getName() + "#elements",
            (source, target) -> new JTableElementsBinding((PropertyBinding) source, target));
        result.put(JTable.class.getName() + "#selectedElement",
            (source, target) -> new JTableSelectedElementBinding((PropertyBinding) source, target, "selectedElement"));
        result.put(JTable.class.getName() + "#selectedElements",
            (source, target) -> new JTableSelectedElementBinding((PropertyBinding) source, target, "selectedElements"));
        return result;
    }
}

/**
 * Tracks the synthetic {@code elements} property on a {@link JTable}.
 */
class JTableElementsBinding extends AbstractSyntheticBinding implements TableModelListener, PropertyChangeListener {
    /**
     * The currently bound table instance.
     */
    JTable boundTable;

    /**
     * Creates an elements binding for a table.
     *
     * @param propertyBinding the source property binding
     * @param target the target binding
     */
    JTableElementsBinding(PropertyBinding propertyBinding, TargetBinding target) {
        super(propertyBinding, target, JTable.class, "elements");
    }

    /**
     * Starts listening to the bound table model.
     */
    @Override
    protected void syntheticBind() {
        boundTable = (JTable) ((PropertyBinding)sourceBinding).getBean();
        boundTable.addPropertyChangeListener("model", this);
        boundTable.getModel().addTableModelListener(this);
    }

    /**
     * Stops listening to the bound table model.
     */
    @Override
    protected void syntheticUnbind() {
        boundTable.removePropertyChangeListener("model", this);
        boundTable.getModel().removeTableModelListener(this);
    }

    /**
     * Refreshes the binding after the table model contents change.
     *
     * @param e the table-model event
     */
    @Override
    public void tableChanged(TableModelEvent e) {
        update();
    }

    /**
     * Refreshes the binding after the table model instance changes.
     *
     * @param event the model change event
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        update();
        ((TableModel) event.getOldValue()).removeTableModelListener(this);
        ((TableModel) event.getNewValue()).addTableModelListener(this);
    }
}

/**
 * Tracks the synthetic selected-element properties on a {@link JTable}.
 */
class JTableSelectedElementBinding extends AbstractSyntheticBinding implements PropertyChangeListener, ListSelectionListener {
    /**
     * The currently bound table instance.
     */
    JTable boundTable;

    /**
     * Creates a selected-element binding for a table.
     *
     * @param source the source property binding
     * @param target the target binding
     * @param propertyName the synthetic property name to observe
     */
    protected JTableSelectedElementBinding(PropertyBinding source, TargetBinding target, String propertyName) {
        super(source, target, JTable.class, propertyName);
    }

    /**
     * Starts listening to the bound table selection model.
     */
    @Override
    public synchronized void syntheticBind() {
        boundTable = (JTable) ((PropertyBinding)sourceBinding).getBean();
        boundTable.addPropertyChangeListener("selectionModel", this);
        boundTable.getSelectionModel().addListSelectionListener(this);
    }

    /**
     * Stops listening to the bound table selection model and clears the cached reference.
     */
    @Override
    public synchronized void syntheticUnbind() {
        boundTable.removePropertyChangeListener("selectionModel", this);
        boundTable.getSelectionModel().removeListSelectionListener(this);
        boundTable = null;
    }

    /**
     * Refreshes the binding after the table selection model instance changes.
     *
     * @param event the selection-model change event
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        update();
        ((ListSelectionModel) event.getOldValue()).removeListSelectionListener(this);
        ((ListSelectionModel) event.getNewValue()).addListSelectionListener(this);
    }

    /**
     * Refreshes the binding after the table selection changes.
     *
     * @param e the selection event
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        update();
    }
}
