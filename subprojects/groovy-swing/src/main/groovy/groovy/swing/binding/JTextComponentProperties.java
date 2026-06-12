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

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Supplies synthetic binding definitions for {@link JTextComponent}.
 *
 * @since Groovy 1.1
 */
public class JTextComponentProperties {

    /**
     * Returns the synthetic trigger bindings exposed for {@link JTextComponent}.
     *
     * @return the synthetic trigger binding map
     */
    public static Map<String, TriggerBinding> getSyntheticProperties() {
        Map<String, TriggerBinding> result = new HashMap<String, TriggerBinding>();
        result.put(JTextComponent.class.getName() + "#text",
            (source, target) -> new JTextComponentTextBinding((PropertyBinding) source, target));
        return result;
    }
}


/**
 * Tracks the synthetic {@code text} property on a {@link JTextComponent}.
 */
class JTextComponentTextBinding extends AbstractSyntheticBinding implements PropertyChangeListener, DocumentListener {
    /**
     * The currently bound text component instance.
     */
    JTextComponent boundTextComponent;

    /**
     * Creates a text binding for a text component.
     *
     * @param source the source property binding
     * @param target the target binding
     */
    JTextComponentTextBinding(PropertyBinding source, TargetBinding target) {
        super(source, target, JTextComponent.class, "text");
        source.setNonChangeCheck(true);
    }

    /**
     * Starts listening to the bound text component and its document.
     */
    @Override
    public synchronized void syntheticBind() {
        boundTextComponent = (JTextComponent) ((PropertyBinding)sourceBinding).getBean();
        boundTextComponent.addPropertyChangeListener("document", this);
        boundTextComponent.getDocument().addDocumentListener(this);
    }

    /**
     * Stops listening to the bound text component and clears the cached reference.
     */
    @Override
    public synchronized void syntheticUnbind() {
        boundTextComponent.removePropertyChangeListener("document", this);
        boundTextComponent.getDocument().removeDocumentListener(this);
        boundTextComponent = null;
    }

    /**
     * Refreshes the binding after the document instance changes.
     *
     * @param event the document change event
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        update();
        ((Document)event.getOldValue()).removeDocumentListener(this);
        ((Document)event.getNewValue()).addDocumentListener(this);
    }

    /**
     * Refreshes the binding after document attributes change.
     *
     * @param event the document event
     */
    @Override
    public void changedUpdate(DocumentEvent event) {
        update();
    }

    /**
     * Refreshes the binding after text is inserted.
     *
     * @param event the document event
     */
    @Override
    public void insertUpdate(DocumentEvent event) {
        update();
    }

    /**
     * Refreshes the binding after text is removed.
     *
     * @param event the document event
     */
    @Override
    public void removeUpdate(DocumentEvent event) {
        update();
    }

}
