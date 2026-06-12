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
 * Supplies synthetic binding definitions for {@link JSlider}.
 *
 * @since Groovy 1.1
 */
public class JSliderProperties {
    /**
     * Returns the synthetic trigger bindings exposed for {@link JSlider}.
     *
     * @return the synthetic trigger binding map
     */
    public static Map<String, TriggerBinding> getSyntheticProperties() {
        Map<String, TriggerBinding> result = new HashMap<String, TriggerBinding>();
        result.put(JSlider.class.getName() + "#value",
            (source, target) -> new JSliderValueBinding((PropertyBinding) source, target));
        return result;
    }
}


/**
 * Tracks the synthetic {@code value} property on a {@link JSlider}.
 */
class JSliderValueBinding extends AbstractSyntheticBinding implements PropertyChangeListener, ChangeListener {
    /**
     * The currently bound slider instance.
     */
    JSlider boundSlider;


    /**
     * Creates a value binding for a slider.
     *
     * @param source the source property binding
     * @param target the target binding
     */
    JSliderValueBinding(PropertyBinding source, TargetBinding target) {
        super(source, target, JSlider.class, "value");
    }

    /**
     * Starts listening to the bound slider and its model.
     */
    @Override
    public synchronized void syntheticBind() {
        boundSlider = (JSlider) ((PropertyBinding)sourceBinding).getBean();
        boundSlider.addPropertyChangeListener("model", this);
        boundSlider.getModel().addChangeListener(this);
    }

    /**
     * Stops listening to the bound slider and clears the cached reference.
     */
    @Override
    public synchronized void syntheticUnbind() {
        boundSlider.removePropertyChangeListener("model", this);
        boundSlider.getModel().removeChangeListener(this);
        boundSlider = null;
    }

    /**
     * Refreshes the binding after the slider model instance changes.
     *
     * @param event the model change event
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        update();
        ((BoundedRangeModel) event.getOldValue()).removeChangeListener(this);
        ((BoundedRangeModel) event.getNewValue()).addChangeListener(this);
    }

    /**
     * Refreshes the binding after the slider value changes.
     *
     * @param e the change event
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        update();
    }
}
