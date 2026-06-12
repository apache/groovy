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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Supplies synthetic binding definitions for {@link AbstractButton}.
 *
 * @since Groovy 1.1
 */
public class AbstractButtonProperties {
    /**
     * Returns the synthetic trigger bindings exposed for {@link AbstractButton}.
     *
     * @return the synthetic trigger binding map
     */
    public static Map<String, TriggerBinding> getSyntheticProperties() {
        Map<String, TriggerBinding> result = new HashMap<String, TriggerBinding>();
        result.put(AbstractButton.class.getName() + "#selected",
            (source, target) -> new AbstractButtonSelectedBinding((PropertyBinding) source, target));
        return result;
    }
}


/**
 * Tracks the synthetic {@code selected} property on an {@link AbstractButton}.
 */
class AbstractButtonSelectedBinding extends AbstractSyntheticBinding implements PropertyChangeListener, ItemListener {
    /**
     * The currently bound button instance.
     */
    AbstractButton boundButton;

    /**
     * Creates a selected-property binding for an abstract button.
     *
     * @param source the source property binding
     * @param target the target binding
     */
    AbstractButtonSelectedBinding(PropertyBinding source, TargetBinding target) {
        super(source, target, AbstractButton.class, "selected");
    }

    /**
     * Starts listening to the bound button and its current model.
     */
    @Override
    public synchronized void syntheticBind() {
            boundButton = (AbstractButton) ((PropertyBinding) sourceBinding).getBean();
                boundButton.addPropertyChangeListener("model", this);
                boundButton.getModel().addItemListener(this);
    }

    /**
     * Stops listening to the bound button and clears the cached reference.
     */
    @Override
    public synchronized void syntheticUnbind() {
            boundButton.removePropertyChangeListener("model", this);
            boundButton.getModel().removeItemListener(this);
            boundButton = null;
    }

    /**
     * Refreshes the binding after the button model changes and reattaches listeners.
     *
     * @param event the model change event
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        update();
        ((ButtonModel)event.getOldValue()).removeItemListener(this);
        ((ButtonModel)event.getNewValue()).addItemListener(this);
    }

    /**
     * Refreshes the binding after the button selection state changes.
     *
     * @param e the item event describing the selection change
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        update();
    }
}
