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

import org.apache.groovy.swing.binding.FullBinding;
import org.apache.groovy.swing.binding.PropertyBinding;
import org.apache.groovy.swing.binding.SourceBinding;
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

public class JTableProperties {
    public static Map<String, TriggerBinding> getSyntheticProperties() {
        Map<String, TriggerBinding> result = new HashMap<String, TriggerBinding>();
        result.put(JTable.class.getName() + "#elements",
                new TriggerBinding() {
                    @Override
                    public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                        return new JTableElementsBinding((PropertyBinding) source, target);
                    }
                });
        result.put(JTable.class.getName() + "#selectedElement",
                new TriggerBinding() {
                    @Override
                    public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                        return new JTableSelectedElementBinding((PropertyBinding) source, target, "selectedElement");
                    }
                });
        result.put(JTable.class.getName() + "#selectedElements",
                new TriggerBinding() {
                    @Override
                    public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                        return new JTableSelectedElementBinding((PropertyBinding) source, target, "selectedElements");
                    }
                });
        return result;
    }
}

class JTableElementsBinding extends AbstractSyntheticBinding implements TableModelListener, PropertyChangeListener {
    JTable boundTable;

    public JTableElementsBinding(PropertyBinding propertyBinding, TargetBinding target) {
        super(propertyBinding, target, JTable.class, "elements");
    }

    @Override
    protected void syntheticBind() {
        boundTable = (JTable) ((PropertyBinding)sourceBinding).getBean();
        boundTable.addPropertyChangeListener("model", this);
        boundTable.getModel().addTableModelListener(this);
    }

    @Override
    protected void syntheticUnbind() {
        boundTable.removePropertyChangeListener("model", this);
        boundTable.getModel().removeTableModelListener(this);
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        update();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        update();
        ((TableModel) event.getOldValue()).removeTableModelListener(this);
        ((TableModel) event.getNewValue()).addTableModelListener(this);
    }
}

class JTableSelectedElementBinding extends AbstractSyntheticBinding implements PropertyChangeListener, ListSelectionListener {
    JTable boundTable;

    protected JTableSelectedElementBinding(PropertyBinding source, TargetBinding target, String propertyName) {
        super(source, target, JTable.class, propertyName);
    }

    @Override
    public synchronized void syntheticBind() {
        boundTable = (JTable) ((PropertyBinding)sourceBinding).getBean();
        boundTable.addPropertyChangeListener("selectionModel", this);
        boundTable.getSelectionModel().addListSelectionListener(this);
    }

    @Override
    public synchronized void syntheticUnbind() {
        boundTable.removePropertyChangeListener("selectionModel", this);
        boundTable.getSelectionModel().removeListSelectionListener(this);
        boundTable = null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        update();
        ((ListSelectionModel) event.getOldValue()).removeListSelectionListener(this);
        ((ListSelectionModel) event.getNewValue()).addListSelectionListener(this);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        update();
    }
}
