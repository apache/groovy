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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Supplies synthetic binding definitions for {@link JSpinner}.
 *
 * @since Groovy 1.6.4
 */
public class JSpinnerProperties {
    /**
     * Returns the synthetic trigger bindings exposed for {@link JSpinner}.
     *
     * @return the synthetic trigger binding map
     */
    public static Map<String, TriggerBinding> getSyntheticProperties() {
        Map<String, TriggerBinding> result = new HashMap<String, TriggerBinding>();
        result.put(JSpinner.class.getName() + "#value",
            (source, target) -> new JSpinnerValueBinding((PropertyBinding) source, target));
        return result;
    }
}

/**
 * Tracks the synthetic {@code value} property on a {@link JSpinner}.
 */
class JSpinnerValueBinding extends AbstractSyntheticBinding implements PropertyChangeListener, ChangeListener {
    /**
     * The currently bound spinner instance.
     */
    JSpinner boundSlider;


    /**
     * Creates a value binding for a spinner.
     *
     * @param source the source property binding
     * @param target the target binding
     */
    JSpinnerValueBinding(PropertyBinding source, TargetBinding target) {
        super(source, target, JSpinner.class, "value");
    }

    /**
     * Starts listening to the bound spinner and its model.
     */
    @Override
    public synchronized void syntheticBind() {
        boundSlider = (JSpinner) ((PropertyBinding)sourceBinding).getBean();
        boundSlider.addPropertyChangeListener("model", this);
        boundSlider.getModel().addChangeListener(this);
    }

    /**
     * Stops listening to the bound spinner and clears the cached reference.
     */
    @Override
    public synchronized void syntheticUnbind() {
        boundSlider.removePropertyChangeListener("model", this);
        boundSlider.getModel().removeChangeListener(this);
        boundSlider = null;
    }

    /**
     * Refreshes the binding after the spinner model instance changes.
     *
     * @param event the model change event
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        update();
        ((SpinnerModel) event.getOldValue()).removeChangeListener(this);
        ((SpinnerModel) event.getNewValue()).addChangeListener(this);
    }

    /**
     * Refreshes the binding after the spinner value changes.
     *
     * @param e the change event
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        update();
    }
}
