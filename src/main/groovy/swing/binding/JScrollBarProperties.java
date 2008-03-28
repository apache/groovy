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
import javax.swing.JScrollBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:shemnon@yahoo.com">Danno Ferrin</a>
 * @version $Revision: 7953 $
 * @since Groovy 1.1
 */
public class JScrollBarProperties {
    public static Map<String, TriggerBinding> getSyntheticProperties() {
        Map<String, TriggerBinding> result = new HashMap<String, TriggerBinding>();
        result.put(JScrollBar.class.getName() + "#value",
                new TriggerBinding() {
                    public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                        return new JScrollBarValueBinding((PropertyBinding) source, target);
                    }
                });
        return result;
    }
}


class JScrollBarValueBinding extends AbstractFullBinding implements PropertyChangeListener, ChangeListener {
    boolean bound;
    JScrollBar boundSlider;


    public JScrollBarValueBinding(PropertyBinding source, TargetBinding target) {
        bound = false;
        setSourceBinding(source);
        setTargetBinding(target);
    }

    public synchronized void bind() {
        if (!bound) {
            boundSlider = (JScrollBar) ((PropertyBinding)sourceBinding).getBean();
            try {
                boundSlider.addPropertyChangeListener("model", this);
                boundSlider.getModel().addChangeListener(this);
                bound = true;
            } catch (RuntimeException re) {
                try {
                    boundSlider.removePropertyChangeListener("model", this);
                    boundSlider.getModel().removeChangeListener(this);
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
            boundSlider.removePropertyChangeListener("model", this);
            boundSlider.getModel().removeChangeListener(this);
            boundSlider = null;
        }
    }

    public void rebind() {
        if (bound) {
            unbind();
            bind();
        }
    }

    public void setSourceBinding(SourceBinding source) {
        if (!(source instanceof PropertyBinding)) {
            throw new IllegalArgumentException("Only PropertySourceBindings are accepted");
        }

        if (!"value".equals(((PropertyBinding) source).getPropertyName())) {
            throw new IllegalArgumentException("PropertyName must be 'value'");
        }
        if (!(((PropertyBinding) source).getBean() instanceof JScrollBar)) {
            throw new IllegalArgumentException("SourceBean must be an JScrollBar");
        }
        super.setSourceBinding(source);
    }

    public void setTargetBinding(TargetBinding target) {
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
