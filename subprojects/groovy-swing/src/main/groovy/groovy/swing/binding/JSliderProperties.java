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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

/**
 * @since Groovy 1.1
 */
public class JSliderProperties {
    public static Map<String, TriggerBinding> getSyntheticProperties() {
        Map<String, TriggerBinding> result = new HashMap<>();
        result.put(JSlider.class.getName() + "#value",
                new TriggerBinding() {
                    public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                        return new JSliderValueBinding((PropertyBinding) source, target);
                    }
                });
        return result;
    }
}


class JSliderValueBinding extends AbstractSyntheticBinding implements PropertyChangeListener, ChangeListener {
    JSlider boundSlider;


    public JSliderValueBinding(PropertyBinding source, TargetBinding target) {
        super(source, target, JSlider.class, "value");
    }

    public synchronized void syntheticBind() {
        boundSlider = (JSlider) ((PropertyBinding)sourceBinding).getBean();
        boundSlider.addPropertyChangeListener("model", this);
        boundSlider.getModel().addChangeListener(this);
    }

    public synchronized void syntheticUnbind() {
        boundSlider.removePropertyChangeListener("model", this);
        boundSlider.getModel().removeChangeListener(this);
        boundSlider = null;
    }

    public void propertyChange(PropertyChangeEvent event) {
        update();
        ((BoundedRangeModel) event.getOldValue()).removeChangeListener(this);
        ((BoundedRangeModel) event.getNewValue()).addChangeListener(this);
    }

    public void stateChanged(ChangeEvent e) {
        update();
    }
}
