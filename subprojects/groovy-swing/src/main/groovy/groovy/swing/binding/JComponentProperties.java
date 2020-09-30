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
        Map<String, TriggerBinding> result = new HashMap<String, TriggerBinding>();
        result.put(JComponent.class.getName() + "#size",
            new TriggerBinding() {
                @Override
                public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                    return new AbstractJComponentBinding((PropertyBinding) source, target, "size") {
                        @Override
                        public void componentResized(ComponentEvent event) {
                            update();
                        }
                    };
                }
            });
        result.put(JComponent.class.getName() + "#width",
            new TriggerBinding() {
                @Override
                public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                    return new AbstractJComponentBinding((PropertyBinding) source, target, "width") {
                        @Override
                        public void componentResized(ComponentEvent event) {
                            update();
                        }
                    };
                }
            });
        result.put(JComponent.class.getName() + "#height",
            new TriggerBinding() {
                @Override
                public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                    return new AbstractJComponentBinding((PropertyBinding) source, target, "height") {
                        @Override
                        public void componentResized(ComponentEvent event) {
                            update();
                        }
                    };
                }
            });
        result.put(JComponent.class.getName() + "#bounds",
            new TriggerBinding() {
                @Override
                public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                    return new AbstractJComponentBinding((PropertyBinding) source, target, "bounds") {
                        @Override
                        public void componentResized(ComponentEvent event) {
                            update();
                        }
                        @Override
                        public void componentMoved(ComponentEvent event) {
                            update();
                        }
                    };
                }
            });
        result.put(JComponent.class.getName() + "#x",
            new TriggerBinding() {
                @Override
                public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                    return new AbstractJComponentBinding((PropertyBinding) source, target, "x") {
                        @Override
                        public void componentMoved(ComponentEvent event) {
                            update();
                        }
                    };
                }
            });
        result.put(JComponent.class.getName() + "#y",
            new TriggerBinding() {
                @Override
                public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                    return new AbstractJComponentBinding((PropertyBinding) source, target, "y") {
                        @Override
                        public void componentMoved(ComponentEvent event) {
                            update();
                        }
                    };
                }
            });
        result.put(JComponent.class.getName() + "#visible",
            new TriggerBinding() {
                @Override
                public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                    return new AbstractJComponentBinding((PropertyBinding) source, target, "visible") {
                        @Override
                        public void componentHidden(ComponentEvent event) {
                            update();
                        }
                        @Override
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

    @Override
    public synchronized void syntheticBind() {
        boundComponent = (JComponent) ((PropertyBinding)sourceBinding).getBean();
        boundComponent.addPropertyChangeListener(propertyName, this);
        boundComponent.addComponentListener(this);
    }

    @Override
    public synchronized void syntheticUnbind() {
        boundComponent.removePropertyChangeListener(propertyName, this);
        boundComponent.removeComponentListener(this);
        boundComponent = null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        update();
        ((JComponent)event.getOldValue()).removeComponentListener(this);
        ((JComponent)event.getNewValue()).addComponentListener(this);
    }

    @Override
    public void componentHidden(ComponentEvent event) {}
    @Override
    public void componentShown(ComponentEvent event) {}
    @Override
    public void componentMoved(ComponentEvent event) {}
    @Override
    public void componentResized(ComponentEvent event) {}
}
