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
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

/**
 * @since Groovy 1.6
 */
public class JComponentProperties {
    public static Map<String, TriggerBinding> getSyntheticProperties() {
        Map<String, TriggerBinding> result = new HashMap<>();
        result.put(JComponent.class.getName() + "#size",
            new TriggerBinding() {
                public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                    return new AbstractJComponentBinding((PropertyBinding) source, target, "size") {
                        public void componentResized(ComponentEvent event) {
                            update();
                        }
                    };
                }
            });
        result.put(JComponent.class.getName() + "#width",
            new TriggerBinding() {
                public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                    return new AbstractJComponentBinding((PropertyBinding) source, target, "width") {
                        public void componentResized(ComponentEvent event) {
                            update();
                        }
                    };
                }
            });
        result.put(JComponent.class.getName() + "#height",
            new TriggerBinding() {
                public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                    return new AbstractJComponentBinding((PropertyBinding) source, target, "height") {
                        public void componentResized(ComponentEvent event) {
                            update();
                        }
                    };
                }
            });
        result.put(JComponent.class.getName() + "#bounds",
            new TriggerBinding() {
                public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                    return new AbstractJComponentBinding((PropertyBinding) source, target, "bounds") {
                        public void componentResized(ComponentEvent event) {
                            update();
                        }
                        public void componentMoved(ComponentEvent event) {
                            update();
                        }
                    };
                }
            });
        result.put(JComponent.class.getName() + "#x",
            new TriggerBinding() {
                public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                    return new AbstractJComponentBinding((PropertyBinding) source, target, "x") {
                        public void componentMoved(ComponentEvent event) {
                            update();
                        }
                    };
                }
            });
        result.put(JComponent.class.getName() + "#y",
            new TriggerBinding() {
                public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                    return new AbstractJComponentBinding((PropertyBinding) source, target, "y") {
                        public void componentMoved(ComponentEvent event) {
                            update();
                        }
                    };
                }
            });
        result.put(JComponent.class.getName() + "#visible",
            new TriggerBinding() {
                public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                    return new AbstractJComponentBinding((PropertyBinding) source, target, "visible") {
                        public void componentHidden(ComponentEvent event) {
                            update();
                        }
                        public void componentShown(ComponentEvent event) {
                            update();
                        }
                    };
                }
            });
        return result;
    }
}

abstract class AbstractJComponentBinding extends AbstractSyntheticBinding implements PropertyChangeListener, ComponentListener {
    JComponent boundComponent;
    String propertyName;

    public AbstractJComponentBinding(PropertyBinding source, TargetBinding target, String propertyName) {
        super(source, target, JComponent.class, propertyName);
        source.setNonChangeCheck(true);
    }

    public synchronized void syntheticBind() {
        boundComponent = (JComponent) ((PropertyBinding)sourceBinding).getBean();
        boundComponent.addPropertyChangeListener(propertyName, this);
        boundComponent.addComponentListener(this);
    }

    public synchronized void syntheticUnbind() {
        boundComponent.removePropertyChangeListener(propertyName, this);
        boundComponent.removeComponentListener(this);
        boundComponent = null;
    }

    public void propertyChange(PropertyChangeEvent event) {
        update();
        ((JComponent)event.getOldValue()).removeComponentListener(this);
        ((JComponent)event.getNewValue()).addComponentListener(this);
    }

    public void componentHidden(ComponentEvent event) {}
    public void componentShown(ComponentEvent event) {}
    public void componentMoved(ComponentEvent event) {}
    public void componentResized(ComponentEvent event) {}
}
