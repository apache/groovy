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

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

/**
 * @since Groovy 1.1
 */
public class JTextComponentProperties {

    public static Map<String, TriggerBinding> getSyntheticProperties() {
        Map<String, TriggerBinding> result = new HashMap<String, TriggerBinding>();
        result.put(JTextComponent.class.getName() + "#text",
            new TriggerBinding() {
                public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                    return new JTextComponentTextBinding((PropertyBinding) source, target);
                }
            });
        return result;
    }
}


class JTextComponentTextBinding extends AbstractSyntheticBinding implements PropertyChangeListener, DocumentListener {
    JTextComponent boundTextComponent;

    public JTextComponentTextBinding(PropertyBinding source, TargetBinding target) {
        super(source, target, JTextComponent.class, "text");
        source.setNonChangeCheck(true);
    }

    public synchronized void syntheticBind() {
        boundTextComponent = (JTextComponent) ((PropertyBinding)sourceBinding).getBean();
        boundTextComponent.addPropertyChangeListener("document", this);
        boundTextComponent.getDocument().addDocumentListener(this);
    }

    public synchronized void syntheticUnbind() {
        boundTextComponent.removePropertyChangeListener("document", this);
        boundTextComponent.getDocument().removeDocumentListener(this);
        boundTextComponent = null;
    }

    public void propertyChange(PropertyChangeEvent event) {
        update();
        ((Document)event.getOldValue()).removeDocumentListener(this);
        ((Document)event.getNewValue()).addDocumentListener(this);
    }

    public void changedUpdate(DocumentEvent event) {
        update();
    }

    public void insertUpdate(DocumentEvent event) {
        update();
    }

    public void removeUpdate(DocumentEvent event) {
        update();
    }

}
