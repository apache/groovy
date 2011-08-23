/*
 * Copyright 2007-2009 the original author or authors.
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
package org.codehaus.groovy.binding;

import groovy.lang.GroovyRuntimeException;
import groovy.lang.MissingMethodException;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author <a href="mailto:shemnon@yahoo.com">Danno Ferrin</a>
 * @version $Revision$
 * @since Groovy 1.1
 */
public class PropertyBinding implements SourceBinding, TargetBinding, TriggerBinding {
    private static final ExecutorService DEFAULT_EXECUTOR_SERVICE = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static final Logger LOG = Logger.getLogger(PropertyBinding.class.getName());

    Object bean;
    String propertyName;
    boolean nonChangeCheck;
    UpdateStrategy updateStrategy;

    public PropertyBinding(Object bean, String propertyName) {
        this(bean, propertyName, (UpdateStrategy) null);
    }

    public PropertyBinding(Object bean, String propertyName, String updateStrategy) {
        this(bean, propertyName, UpdateStrategy.of(updateStrategy));
    }

    public PropertyBinding(Object bean, String propertyName, UpdateStrategy updateStrategy) {
        this.bean = bean;
        this.propertyName = propertyName;
        this.updateStrategy = pickUpdateStrategy(bean, updateStrategy);
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Updating with " + this.updateStrategy + " property '" + propertyName + "' of bean " + bean);
        }
    }

    public UpdateStrategy getUpdateStrategy() {
        return updateStrategy;
    }

    private UpdateStrategy pickUpdateStrategy(Object bean, UpdateStrategy updateStrategy) {
        if (bean instanceof Component) {
            return UpdateStrategy.MIXED;
        } else if (updateStrategy != null) {
            return updateStrategy;
        }
        return UpdateStrategy.SAME;
    }

    public void updateTargetValue(final Object newValue) {
        if (nonChangeCheck) {
            if (DefaultTypeTransformation.compareEqual(getSourceValue(), newValue)) {
                // not a change, don't fire it
                return;
            }
        }

        Runnable runnable = new Runnable() {
            public void run() {
                setBeanProperty(newValue);
            }
        };

        switch (updateStrategy) {
            case MIXED:
                if (SwingUtilities.isEventDispatchThread()) {
                    runnable.run();
                } else {
                    SwingUtilities.invokeLater(runnable);
                }
                break;
            case ASYNC:
                SwingUtilities.invokeLater(runnable);
                break;
            case SYNC:
                if (SwingUtilities.isEventDispatchThread()) {
                    runnable.run();
                } else {
                    try {
                        SwingUtilities.invokeAndWait(runnable);
                    } catch (InterruptedException e) {
                        LOG.log(Level.WARNING, "Error notifying propertyChangeListener", e);
                        throw new GroovyRuntimeException(e);
                    } catch (InvocationTargetException e) {
                        LOG.log(Level.WARNING, "Error notifying propertyChangeListener", e.getTargetException());
                        throw new GroovyRuntimeException(e.getTargetException());
                    }
                }
                break;
            case SAME:
                runnable.run();
                break;
            case OUTSIDE:
                if (SwingUtilities.isEventDispatchThread()) {
                    DEFAULT_EXECUTOR_SERVICE.submit(runnable);
                } else {
                    runnable.run();
                }
                break;
            case DEFER:
                DEFAULT_EXECUTOR_SERVICE.submit(runnable);
        }
    }

    private void setBeanProperty(Object newValue) {
        try {
            InvokerHelper.setProperty(bean, propertyName, newValue);
        } catch (InvokerInvocationException iie) {
            if (!(iie.getCause() instanceof PropertyVetoException)) {
                throw iie;
            }
            // ignore veto exceptions, just let the binding fail like a validation does
        }
    }

    public boolean isNonChangeCheck() {
        return nonChangeCheck;
    }

    public void setNonChangeCheck(boolean nonChangeCheck) {
        this.nonChangeCheck = nonChangeCheck;
    }

    public Object getSourceValue() {
        return InvokerHelper.getPropertySafe(bean, propertyName);
    }

    public FullBinding createBinding(SourceBinding source, TargetBinding target) {
        return new PropertyFullBinding(source, target);
    }

    class PropertyFullBinding extends AbstractFullBinding implements PropertyChangeListener {

        Object boundBean;
        Object boundProperty;
        boolean bound;
        boolean boundToProperty;

        PropertyFullBinding(SourceBinding source, TargetBinding target) {
            setSourceBinding(source);
            setTargetBinding(target);
        }

        public void propertyChange(PropertyChangeEvent event) {
            if (boundToProperty || event.getPropertyName().equals(boundProperty)) {
                update();
            }
        }

        public void bind() {
            if (!bound) {
                bound = true;
                boundBean = bean;
                boundProperty = propertyName;
                try {
                    InvokerHelper.invokeMethodSafe(boundBean, "addPropertyChangeListener", new Object[]{boundProperty, this});
                    boundToProperty = true;
                } catch (MissingMethodException mme) {
                    try {
                        boundToProperty = false;
                        InvokerHelper.invokeMethodSafe(boundBean, "addPropertyChangeListener", new Object[]{this});
                    } catch (MissingMethodException mme2) {
                        throw new RuntimeException("Properties in beans of type " + bean.getClass().getName() + " are not observable in any capacity (no PropertyChangeListener support).");
                    }
                }
            }
        }

        public void unbind() {
            if (bound) {
                if (boundToProperty) {
                    try {
                        InvokerHelper.invokeMethodSafe(boundBean, "removePropertyChangeListener", new Object[]{boundProperty, this});
                    } catch (MissingMethodException mme) {
                        // ignore, too bad so sad they don't follow conventions, we'll just leave the listener attached
                    }
                } else {
                    try {
                        InvokerHelper.invokeMethodSafe(boundBean, "removePropertyChangeListener", new Object[]{this});
                    } catch (MissingMethodException mme2) {
                        // ignore, too bad so sad they don't follow conventions, we'll just leave the listener attached
                    }
                }
                boundBean = null;
                boundProperty = null;
                bound = false;
            }
        }

        public void rebind() {
            if (bound) {
                unbind();
                bind();
            }
        }
    }

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public enum UpdateStrategy {
        MIXED, ASYNC, SYNC, SAME, OUTSIDE, DEFER;

        public static UpdateStrategy of(String str) {
            if ("mixed".equalsIgnoreCase(str)) {
                return MIXED;
            } else if ("async".equalsIgnoreCase(str)) {
                return ASYNC;
            } else if ("sync".equalsIgnoreCase(str)) {
                return SYNC;
            } else if ("same".equalsIgnoreCase(str)) {
                return SAME;
            } else if ("outside".equalsIgnoreCase(str)) {
                return OUTSIDE;
            } else if ("defer".equalsIgnoreCase(str)) {
                return DEFER;
            }
            return null;
        }
    }
}
