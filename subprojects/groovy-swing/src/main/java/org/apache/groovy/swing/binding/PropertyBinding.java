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
package org.apache.groovy.swing.binding;

import groovy.beans.DefaultPropertyAccessor;
import groovy.beans.PropertyAccessor;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.MissingMethodException;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import javax.swing.SwingUtilities;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @since Groovy 1.1
 */
public class PropertyBinding implements SourceBinding, TargetBinding, TriggerBinding {
    private static final ExecutorService DEFAULT_EXECUTOR_SERVICE = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static final Logger LOG = Logger.getLogger(PropertyBinding.class.getName());
    private static final Map<Class<?>, Class<? extends PropertyAccessor>> ACCESSORS = new LinkedHashMap<>();
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    static {
        Enumeration<URL> urls = fetchUrlsFor("META-INF/services/" + groovy.beans.PropertyAccessor.class.getName());
        while (urls.hasMoreElements()) {
            try {
                registerPropertyAccessors(ResourceGroovyMethods.readLines(urls.nextElement()));
            } catch (IOException e) {
                // ignore
                // TODO should use a low priority logger
                e.printStackTrace();
            }
        }
    }

    private static void registerPropertyAccessors(List<String> lines) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("#")) return;
            String[] parts = line.split("=");
            if (parts.length == 2) {
                try {
                    ACCESSORS.put(cl.loadClass(parts[0].trim()), getaAccessorClass(cl, parts[1]));
                } catch (ClassNotFoundException e) {
                    // ignore
                    // TODO should use a low priority logger
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends PropertyAccessor> getaAccessorClass(ClassLoader cl, String part) throws ClassNotFoundException {
        return (Class<? extends PropertyAccessor>) cl.loadClass(part.trim());
    }

    private static Enumeration<URL> fetchUrlsFor(String path) {
        try {
            return Thread.currentThread().getContextClassLoader().getResources(path);
        } catch (IOException e) {
            return new Enumeration<URL>() {
                @Override
                public boolean hasMoreElements() {
                    return false;
                }

                @Override
                public URL nextElement() {
                    return null;
                }
            };
        }
    }

    Object bean;
    String propertyName;
    boolean nonChangeCheck;
    UpdateStrategy updateStrategy;
    private final Object[] lock = EMPTY_OBJECT_ARRAY;
    private PropertyAccessor propertyAccessor;

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
        setupPropertyReaderAndWriter();
    }

    private void setupPropertyReaderAndWriter() {
        synchronized (lock) {
            propertyAccessor = fetchPropertyAccessor(bean != null ? bean.getClass() : null);
        }
    }

    private PropertyAccessor propertyAccessor() {
        synchronized (lock) {
            return propertyAccessor;
        }
    }

    private PropertyAccessor fetchPropertyAccessor(Class<?> klass) {
        if (klass == null) {
            return DefaultPropertyAccessor.INSTANCE;
        }

        Class<? extends PropertyAccessor> accessorClass = ACCESSORS.get(klass);
        if (accessorClass == null) {
            for (Class<?> c : klass.getInterfaces()) {
                PropertyAccessor propertyAccessor = fetchPropertyAccessor(c);
                if (propertyAccessor != DefaultPropertyAccessor.INSTANCE) {
                    return propertyAccessor;
                }
            }
            return fetchPropertyAccessor(klass.getSuperclass());
        }

        try {
            return accessorClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            return DefaultPropertyAccessor.INSTANCE;
        }
    }

    public UpdateStrategy getUpdateStrategy() {
        return updateStrategy;
    }

    private static UpdateStrategy pickUpdateStrategy(Object bean, UpdateStrategy updateStrategy) {
        if (bean instanceof Component) {
            return UpdateStrategy.MIXED;
        } else if (updateStrategy != null) {
            return updateStrategy;
        }
        return UpdateStrategy.SAME;
    }

    @Override
    public void updateTargetValue(final Object newValue) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Object sourceValue = getSourceValue();
                // if (isNonChangeCheck()) {
                if ((sourceValue == null && newValue == null) ||
                        DefaultTypeTransformation.compareEqual(sourceValue, newValue)) {
                    // not a change, don't fire it
                    return;
                }
                // }
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
            propertyAccessor().write(bean, propertyName, newValue);
        } catch (InvokerInvocationException iie) {
            if (!(iie.getCause() instanceof PropertyVetoException)) {
                throw iie;
            }
            // ignore veto exceptions, just let the binding fail like a validation does
        }
    }

    public boolean isNonChangeCheck() {
        synchronized (lock) {
            return nonChangeCheck;
        }
    }

    public void setNonChangeCheck(boolean nonChangeCheck) {
        synchronized (lock) {
            this.nonChangeCheck = nonChangeCheck;
        }
    }

    @Override
    public Object getSourceValue() {
        return propertyAccessor().read(bean, propertyName);
    }

    @Override
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

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if (boundToProperty || event.getPropertyName().equals(boundProperty)) {
                update();
            }
        }

        @Override
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

        @Override
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

        @Override
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
        setupPropertyReaderAndWriter();
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
