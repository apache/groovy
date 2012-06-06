/*
 * Copyright 2007-2008 the original author or authors.
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
package groovy.swing.binding;

import org.codehaus.groovy.binding.*;

import javax.swing.*;
import javax.swing.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Andres Almiray
 * @since Groovy 1.7.5
 */
public class JListProperties {
    public static Map<String, TriggerBinding> getSyntheticProperties() {
        Map<String, TriggerBinding> result = new HashMap<String, TriggerBinding>();

        // to match property name
        result.put(JList.class.getName() + "#selectedValue", new TriggerBinding() {
            public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                return new JListSelectedElementBinding((PropertyBinding) source, target, "selectedValue");
            }
        });

        // to match JSR-295
        result.put(JList.class.getName() + "#selectedElement", new TriggerBinding() {
            public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                return new JListSelectedElementBinding((PropertyBinding) source, target, "selectedElement");
            }
        });

        // to match property name
        result.put(JList.class.getName() + "#selectedValues", new TriggerBinding() {
            public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                return new JListSelectedElementBinding((PropertyBinding) source, target, "selectedValues");
            }
        });

        // to match JSR-295
        result.put(JList.class.getName() + "#selectedElements", new TriggerBinding() {
            public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                return new JListSelectedElementBinding((PropertyBinding) source, target, "selectedElements");
            }
        });

        result.put(JList.class.getName() + "#selectedIndex", new TriggerBinding() {
            public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                return new JListSelectedElementBinding((PropertyBinding) source, target, "selectedIndex");
            }
        });

        result.put(JList.class.getName() + "#selectedIndices", new TriggerBinding() {
            public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                return new JListSelectedElementBinding((PropertyBinding) source, target, "selectedIndices");
            }
        });

        // to match JSR-295
        result.put(JList.class.getName() + "#elements", new TriggerBinding() {
            public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                return new JListElementsBinding((PropertyBinding) source, target);
            }
        });

        return result;
    }
}

class JListElementsBinding extends AbstractSyntheticBinding implements ListDataListener, PropertyChangeListener {
    JList boundList;

    public JListElementsBinding(PropertyBinding propertyBinding, TargetBinding target) {
        super(propertyBinding, target, JList.class, "elements");
    }

    protected void syntheticBind() {
        boundList = (JList) ((PropertyBinding)sourceBinding).getBean();
        boundList.addPropertyChangeListener("model", this);
        boundList.getModel().addListDataListener(this);
    }

    protected void syntheticUnbind() {
        boundList.removePropertyChangeListener("model", this);
        boundList.getModel().removeListDataListener(this);
    }

    public void propertyChange(PropertyChangeEvent event) {
        update();
        ((ListModel) event.getOldValue()).removeListDataListener(this);
        ((ListModel) event.getNewValue()).addListDataListener(this);
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

class JListSelectedElementBinding extends AbstractSyntheticBinding implements PropertyChangeListener, ListSelectionListener {
    JList boundList;

    protected JListSelectedElementBinding(PropertyBinding source, TargetBinding target, String propertyName) {
        super(source, target, JList.class, propertyName);
    }

    public synchronized void syntheticBind() {
        boundList = (JList) ((PropertyBinding)sourceBinding).getBean();
        boundList.addPropertyChangeListener("selectionModel", this);
        boundList.addListSelectionListener(this);
    }

    public synchronized void syntheticUnbind() {
        boundList.removePropertyChangeListener("selectionModel", this);
        boundList.removeListSelectionListener(this);
        boundList = null;
    }

    public void setTargetBinding(TargetBinding target) {
        super.setTargetBinding(target);
    }

    public void propertyChange(PropertyChangeEvent event) {
        update();
    }

    public void valueChanged(ListSelectionEvent e) {
        update();
    }
}
