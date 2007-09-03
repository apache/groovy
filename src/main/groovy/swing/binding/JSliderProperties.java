/*
 * Copyright 2007 the original author or authors.
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

import org.codehaus.groovy.binding.AbstractFullBinding;
import org.codehaus.groovy.binding.FullBinding;
import org.codehaus.groovy.binding.PropertyBinding;
import org.codehaus.groovy.binding.SourceBinding;
import org.codehaus.groovy.binding.TargetBinding;
import org.codehaus.groovy.binding.TriggerBinding;

import javax.swing.BoundedRangeModel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:shemnon@yahoo.com">Danno Ferrin</a>
 * @version $Revision$
 * @since Groovy 1.1
 */
public class JSliderProperties {
    public static Map/*<String, TriggerBinding>*/ getSyntheticProperties() {
        Map/*<String, TriggerBinding>*/ result = new HashMap/*<String, TriggerBinding>*/();
        result.put(JSlider.class.getName() + "#value",
                new TriggerBinding() {
                    public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                        return new JSliderValueBinding((PropertyBinding) source, target);
                    }
                });
        return result;
    }
}


class JSliderValueBinding extends AbstractFullBinding implements PropertyChangeListener, ChangeListener {
    boolean bound;

    public JSliderValueBinding(PropertyBinding source, TargetBinding target) {
        bound = false;
        setSourceBinding(source);
        setTargetBinding(target);
    }

    public synchronized void bind() {
        if (!bound) {
            JSlider slider = (JSlider) ((PropertyBinding)sourceBinding).getBean();
            try {
                slider.addPropertyChangeListener("model", this);
                slider.getModel().addChangeListener(this);
                bound = true;
            } catch (RuntimeException re) {
                try {
                    slider.removePropertyChangeListener("model", this);
                    slider.getModel().removeChangeListener(this);
                } catch (Exception e) {
                    // ignore as we are re-throwing the original cause
                }
                throw re;
            }
        }
    }

    public synchronized void unbind() {
        if (bound) {
            bound = false;
            // fail dirty, no checks
            JSlider slider = (JSlider) ((PropertyBinding)sourceBinding).getBean();
            slider.removePropertyChangeListener("model", this);
            slider.getModel().removeChangeListener(this);
        }
    }

    public void rebind() {
        unbind();
        bind();
    }

    public void setSourceBinding(SourceBinding source) {
        if (bound) {
            throw new IllegalStateException("Cannot change source while binding is bound");
        }
        if (!(source instanceof PropertyBinding)) {
            throw new IllegalArgumentException("Only PropertySourceBindings are accepted");
        }

        if (!"value".equals(((PropertyBinding) source).getPropertyName())) {
            throw new IllegalArgumentException("PropertyName must be 'value'");
        }
        if (!(((PropertyBinding) source).getBean() instanceof JSlider)) {
            throw new IllegalArgumentException("SourceBean must be an JSlider");
        }
        super.setSourceBinding(source);
    }

    public void setTargetBinding(TargetBinding target) {
        if (bound) {
            throw new IllegalStateException("Cannot change target while binding is bound");
        }
        super.setTargetBinding(target);
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
