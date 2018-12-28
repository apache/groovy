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

import org.codehaus.groovy.binding.FullBinding;
import org.codehaus.groovy.binding.PropertyBinding;
import org.codehaus.groovy.binding.SourceBinding;
import org.codehaus.groovy.binding.TargetBinding;
import org.codehaus.groovy.binding.TriggerBinding;

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
 * @since Groovy 1.1
 */
public class JComboBoxProperties {
    public static Map<String, TriggerBinding> getSyntheticProperties() {
        Map<String, TriggerBinding> result = new HashMap<String, TriggerBinding>();

        // to match property name
        result.put(JComboBox.class.getName() + "#selectedItem", new TriggerBinding() {
            public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                return new JComboBoxSelectedElementBinding((PropertyBinding) source, target, "selectedItem");
            }
        });
        // to match JSR-295
        result.put(JComboBox.class.getName() + "#selectedElement", new TriggerBinding() {
            public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                return new JComboBoxSelectedElementBinding((PropertyBinding) source, target, "selectedElement");
            }
        });

        result.put(JComboBox.class.getName() + "#selectedIndex", new TriggerBinding() {
            public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                return new JComboBoxSelectedIndexBinding((PropertyBinding) source, target);
            }
        });


        // to match JSR-295
        result.put(JComboBox.class.getName() + "#elements", new TriggerBinding() {
            public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                return new JComboBoxElementsBinding((PropertyBinding) source, target);
            }
        });

        return result;
    }
}


class JComboBoxSelectedElementBinding extends AbstractSyntheticBinding implements PropertyChangeListener, ItemListener {
    JComboBox boundComboBox;

    public JComboBoxSelectedElementBinding(PropertyBinding source, TargetBinding target, String propertyName) {
        super(source, target, JComboBox.class, propertyName);
    }

    public synchronized void syntheticBind() {
        boundComboBox = (JComboBox) ((PropertyBinding)sourceBinding).getBean();
        boundComboBox.addPropertyChangeListener("model", this);
        boundComboBox.addItemListener(this);
    }

    public synchronized void syntheticUnbind() {
        boundComboBox.removePropertyChangeListener("model", this);
        boundComboBox.removeItemListener(this);
        boundComboBox = null;
    }

    public void setTargetBinding(TargetBinding target) {
        super.setTargetBinding(target);
    }

    public void propertyChange(PropertyChangeEvent event) {
        update();
    }

    public void itemStateChanged(ItemEvent e) {
        update();
    }

}

class JComboBoxSelectedIndexBinding extends AbstractSyntheticBinding implements PropertyChangeListener, ItemListener {
    JComboBox boundComboBox;

    public JComboBoxSelectedIndexBinding(PropertyBinding source, TargetBinding target) {
        super(source, target, JComboBox.class, "selectedIndex");
    }

    public synchronized void syntheticBind() {
        boundComboBox = (JComboBox) ((PropertyBinding)sourceBinding).getBean();
        boundComboBox.addPropertyChangeListener("model", this);
        boundComboBox.addItemListener(this);
    }

    public synchronized void syntheticUnbind() {
        boundComboBox.removePropertyChangeListener("model", this);
        boundComboBox.removeItemListener(this);
        boundComboBox = null;
    }

    public void setTargetBinding(TargetBinding target) {
        super.setTargetBinding(target);
    }

    public void propertyChange(PropertyChangeEvent event) {
        update();
    }

    public void itemStateChanged(ItemEvent e) {
        update();
    }

}

class JComboBoxElementsBinding extends AbstractSyntheticBinding implements ListDataListener, PropertyChangeListener {
    JComboBox boundComboBox;

    public JComboBoxElementsBinding(PropertyBinding propertyBinding, TargetBinding target) {
        super(propertyBinding, target, JComboBox.class, "elements");
    }

    protected void syntheticBind() {
        boundComboBox = (JComboBox) ((PropertyBinding)sourceBinding).getBean();
        boundComboBox.addPropertyChangeListener("model", this);
        boundComboBox.getModel().addListDataListener(this);
    }

    protected void syntheticUnbind() {
        boundComboBox.removePropertyChangeListener("model", this);
        boundComboBox.getModel().removeListDataListener(this);
    }

    public void propertyChange(PropertyChangeEvent event) {
        update();
        ((ComboBoxModel) event.getOldValue()).removeListDataListener(this);
        ((ComboBoxModel) event.getNewValue()).addListDataListener(this);
    }

    public void intervalAdded(ListDataEvent e) {
        update();
    }

    public void intervalRemoved(ListDataEvent e) {
        update();
    }

    public void contentsChanged(ListDataEvent e) {
        update();
    }
}

