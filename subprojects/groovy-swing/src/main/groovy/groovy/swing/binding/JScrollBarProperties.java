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


class JScrollBarValueBinding extends AbstractSyntheticBinding implements PropertyChangeListener, ChangeListener {
    JScrollBar boundScrollBar;


    public JScrollBarValueBinding(PropertyBinding source, TargetBinding target) {
        super(source, target, JScrollBar.class, "value");
    }

    public synchronized void syntheticBind() {
        boundScrollBar = (JScrollBar) ((PropertyBinding)sourceBinding).getBean();
        boundScrollBar.addPropertyChangeListener("model", this);
        boundScrollBar.getModel().addChangeListener(this);
    }

    public synchronized void syntheticUnbind() {
        boundScrollBar.removePropertyChangeListener("model", this);
        boundScrollBar.getModel().removeChangeListener(this);
        boundScrollBar = null;
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
