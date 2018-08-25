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
package groovy.util;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.DelegatingMetaClass;
import groovy.lang.GroovyClassLoader;
import groovy.lang.MetaClass;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import groovy.lang.Reference;
import groovy.lang.Script;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.metaclass.MissingMethodExceptionNoStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.groovy.util.BeanUtils.capitalize;

/**
 * Mix of BuilderSupport and SwingBuilder's factory support.
 *
 * Warning: this implementation is not thread safe and should not be used
 * across threads in a multi-threaded environment.  A locking mechanism
 * should be implemented by the subclass if use is expected across
 * multiple threads.
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author <a href="mailto:aalmiray@users.sourceforge.com">Andres Almiray</a>
 * @author Danno Ferrin
 */
public abstract class FactoryBuilderSupport extends Binding {
    public static final String CURRENT_FACTORY = "_CURRENT_FACTORY_";
    public static final String PARENT_FACTORY = "_PARENT_FACTORY_";
    public static final String PARENT_NODE = "_PARENT_NODE_";
    public static final String CURRENT_NODE = "_CURRENT_NODE_";
    public static final String PARENT_CONTEXT = "_PARENT_CONTEXT_";
    public static final String PARENT_NAME = "_PARENT_NAME_";
    public static final String CURRENT_NAME = "_CURRENT_NAME_";
    public static final String OWNER = "owner";
    public static final String PARENT_BUILDER = "_PARENT_BUILDER_";
    public static final String CURRENT_BUILDER = "_CURRENT_BUILDER_";
    public static final String CHILD_BUILDER = "_CHILD_BUILDER_";
    public static final String SCRIPT_CLASS_NAME = "_SCRIPT_CLASS_NAME_";
    private static final Logger LOG = Logger.getLogger(FactoryBuilderSupport.class.getName());
    private static final Comparator<Method> METHOD_COMPARATOR = new Comparator<Method>() {
        public int compare(final Method o1, final Method o2) {
            int cmp = o1.getName().compareTo(o2.getName());
            if (cmp != 0) return cmp;
            cmp = o1.getParameterTypes().length - o2.getParameterTypes().length;
            return cmp;
        }
    };

    /**
     * Throws an exception if value is null.
     *
     * @param value the node's value
     * @param name  the node's name
     */
    public static void checkValueIsNull(Object value, Object name) {
        if (value != null) {
            throw new RuntimeException("'" + name + "' elements do not accept a value argument.");
        }
    }

    /**
     * Checks type of value against builder type
     *
     * @param value the node's value
     * @param name  the node's name
     * @param type  a Class that may be assignable to the value's class
     * @return true if type is assignable to the value's class, false if value
     *         is null.
     */
    public static boolean checkValueIsType(Object value, Object name, Class type) {
        if (value != null) {
            if (type.isAssignableFrom(value.getClass())) {
                return true;
            } else {
                throw new RuntimeException("The value argument of '" + name + "' must be of type "
                        + type.getName() + ". Found: " + value.getClass());
            }
        } else {
            return false;
        }
    }

    /**
     * Checks values against factory's type
     *
     * @param value the node's value
     * @param name  the node's name
     * @param type  a Class that may be assignable to the value's class
     * @return Returns true if type is assignable to the value's class, false if value is
     *         null or a String.
     */
    public static boolean checkValueIsTypeNotString(Object value, Object name, Class type) {
        if (value != null) {
            if (type.isAssignableFrom(value.getClass())) {
                return true;
            } else if (value instanceof String) {
                return false;
            } else {
                throw new RuntimeException("The value argument of '" + name + "' must be of type "
                        + type.getName() + " or a String. Found: " + value.getClass());
            }
        } else {
            return false;
        }
    }

    private final ThreadLocal<LinkedList<Map<String, Object>>> contexts = new ThreadLocal<>();
    protected LinkedList<Closure> attributeDelegates = new LinkedList<>(); //
    private final List<Closure> disposalClosures = new ArrayList<>(); // because of reverse iteration use ArrayList
    private final Map<String, Factory> factories = new HashMap<>();
    private Closure nameMappingClosure;
    private final ThreadLocal<FactoryBuilderSupport> localProxyBuilder = new ThreadLocal<>();
    private FactoryBuilderSupport globalProxyBuilder;
    protected LinkedList<Closure> preInstantiateDelegates = new LinkedList<>();
    protected LinkedList<Closure> postInstantiateDelegates = new LinkedList<>();
    protected LinkedList<Closure> postNodeCompletionDelegates = new LinkedList<>();
    protected Closure methodMissingDelegate;
    protected Closure propertyMissingDelegate;
    protected Map<String, Closure[]> explicitProperties = new HashMap<>();
    protected Map<String, Closure> explicitMethods = new HashMap<>();
    protected Map<String, Set<String>> registrationGroup = new HashMap<>();
    protected String registrationGroupName = ""; // use binding to store?

    protected boolean autoRegistrationRunning = false;
    protected boolean autoRegistrationComplete = false;

    public FactoryBuilderSupport() {
        this(false);
    }

    public FactoryBuilderSupport(boolean init) {
        globalProxyBuilder = this;
        registrationGroup.put(registrationGroupName, new TreeSet<>());
        if (init) {
            autoRegisterNodes();
        }
    }

    private Set<String> getRegistrationGroup(String name) {
        Set<String> group = registrationGroup.get(name);
        if (group == null ) {
            group = new TreeSet<>();
            registrationGroup.put(name, group);
        }
        return group;
    }

    /**
     * Ask the nodes to be registered
     */
    public void autoRegisterNodes() {
        // if java did atomic blocks, this would be one
        synchronized (this) {
            if (autoRegistrationRunning || autoRegistrationComplete) {
                // registration already done or in process, abort
                return;
            }
        }
        autoRegistrationRunning = true;
        try {
            callAutoRegisterMethods(getClass());
        } finally {
            autoRegistrationComplete = true;
            autoRegistrationRunning = false;
        }
    }

    private void callAutoRegisterMethods(Class declaredClass) {
        if (declaredClass == null) {
            return;
        }
        callAutoRegisterMethods(declaredClass.getSuperclass());

        Method[] declaredMethods = declaredClass.getDeclaredMethods();
        Arrays.sort(declaredMethods, METHOD_COMPARATOR);
        for (Method method : declaredMethods) {
            if (method.getName().startsWith("register") && method.getParameterTypes().length == 0) {
                registrationGroupName = method.getName().substring("register".length());
                registrationGroup.put(registrationGroupName, new TreeSet<>());
                try {
                    if (Modifier.isPublic(method.getModifiers())) {
                        method.invoke(this);
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Could not init " + getClass().getName() + " because of an access error in " + declaredClass.getName() + "." + method.getName(), e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException("Could not init " + getClass().getName() + " because of an exception in " + declaredClass.getName() + "." + method.getName(), e);
                } finally {
                    registrationGroupName = "";
                }
            }
        }
    }

    /**
     * @param name the name of the variable to lookup
     * @return the variable value
     */
    public Object getVariable(String name) {
        try {
            return getProxyBuilder().doGetVariable(name);
        } catch(MissingPropertyException mpe) {
            if(mpe.getProperty().equals(name) && propertyMissingDelegate != null) {
                return propertyMissingDelegate.call(new Object[]{name});
            }
            throw mpe;
        }
    }

    private Object doGetVariable(String name) {
        return super.getVariable(name);
    }

    /**
     * Sets the value of the given variable
     *
     * @param name  the name of the variable to set
     * @param value the new value for the given variable
     */
    public void setVariable(String name, Object value) {
        getProxyBuilder().doSetVariable(name, value);
    }

    private void doSetVariable(String name, Object value) {
        super.setVariable(name, value);
    }

    public Map getVariables() {
        return getProxyBuilder().doGetVariables();
    }

    private Map doGetVariables() {
        return super.getVariables();
    }

    /**
     * Overloaded to make variables appear as bean properties or via the subscript operator
     */
    public Object getProperty(String property) {
        try {
            return getProxyBuilder().doGetProperty(property);
        } catch (MissingPropertyException mpe) {
            if ((getContext() != null) && (getContext().containsKey(property))) {
                return getContext().get(property);
            } else {
                try {
                    return getMetaClass().getProperty(this, property);
                } catch(MissingPropertyException mpe2) {
                    if(mpe2.getProperty().equals(property) && propertyMissingDelegate != null) {
                        return propertyMissingDelegate.call(new Object[]{property});
                    }
                    throw mpe2;
                }
            }
        }
    }

    private Object doGetProperty(String property) {
        Closure[] accessors = resolveExplicitProperty(property);
        if (accessors != null) {
            if (accessors[0] == null) {
                // write only property
                throw new MissingPropertyException(property + " is declared as write only");
            } else {
                return accessors[0].call();
            }
        } else {
            return super.getProperty(property);
        }
    }

    /**
     * Overloaded to make variables appear as bean properties or via the subscript operator
     */
    public void setProperty(String property, Object newValue) {
        getProxyBuilder().doSetProperty(property, newValue);
    }

    private void doSetProperty(String property, Object newValue) {
        Closure[] accessors = resolveExplicitProperty(property);
        if (accessors != null) {
            if (accessors[1] == null) {
                // read only property
                throw new MissingPropertyException(property + " is declared as read only");
            } else {
                accessors[1].call(newValue);
            }
        } else {
            super.setProperty(property, newValue);
        }
    }

    /**
     * @return the factory map (Unmodifiable Map).
     */
    public Map<String, Factory> getFactories() {
        return Collections.unmodifiableMap(getProxyBuilder().factories);
    }

    /**
     * @return the explicit methods map (Unmodifiable Map).
     */
    public Map<String, Closure> getExplicitMethods() {
        return Collections.unmodifiableMap(getProxyBuilder().explicitMethods);
    }

    /**
     * @return the explicit properties map (Unmodifiable Map).
     */
    public Map<String, Closure[]> getExplicitProperties() {
        return Collections.unmodifiableMap(getProxyBuilder().explicitProperties);
    }

    /**
     * @return the factory map (Unmodifiable Map).
     */
    public Map<String, Factory> getLocalFactories() {
        return Collections.unmodifiableMap(factories);
    }

    /**
     * @return the explicit methods map (Unmodifiable Map).
     */
    public Map<String, Closure> getLocalExplicitMethods() {
        return Collections.unmodifiableMap(explicitMethods);
    }

    /**
     * @return the explicit properties map (Unmodifiable Map).
     */
    public Map<String, Closure[]> getLocalExplicitProperties() {
        return Collections.unmodifiableMap(explicitProperties);
    }

    public Set<String> getRegistrationGroups() {
        return Collections.unmodifiableSet(registrationGroup.keySet());
    }

    public Set<String> getRegistrationGroupItems(String group) {
        Set<String> groupSet = registrationGroup.get(group);
        if (groupSet != null) {
            return Collections.unmodifiableSet(groupSet);
        } else {
            return Collections.emptySet();
        }
    }

    public List<Closure> getAttributeDelegates() {
        return Collections.unmodifiableList(attributeDelegates);
    }

    public List<Closure> getPreInstantiateDelegates() {
        return Collections.unmodifiableList(preInstantiateDelegates);
    }

    public List<Closure> getPostInstantiateDelegates() {
        return Collections.unmodifiableList(postInstantiateDelegates);
    }

    public List<Closure> getPostNodeCompletionDelegates() {
        return Collections.unmodifiableList(postNodeCompletionDelegates);
    }

    public Closure getMethodMissingDelegate() {
        return methodMissingDelegate;
    }

    public void setMethodMissingDelegate(Closure delegate) {
        methodMissingDelegate = delegate;
    }

    public Closure getPropertyMissingDelegate() {
        return propertyMissingDelegate;
    }

    public void setPropertyMissingDelegate(Closure delegate) {
        propertyMissingDelegate = delegate;
    }

    /**
     * @return the context of the current node.
     */
    public Map<String, Object> getContext() {
        LinkedList<Map<String, Object>> contexts = getProxyBuilder().contexts.get();
        if (contexts != null && !contexts.isEmpty()) {
            return contexts.getFirst();
        }
        return null;
    }

    /**
     * @return the current node being built.
     */
    public Object getCurrent() {
        return getContextAttribute(CURRENT_NODE);
    }

    /**
     * @return the factory that built the current node.
     */
    public Factory getCurrentFactory() {
        return (Factory) getContextAttribute(CURRENT_FACTORY);
    }

    /**
     * @return the factory of the parent of the current node.
     */
    public String getCurrentName() {
        return (String) getContextAttribute(CURRENT_NAME);
    }

    /**
     * @return the builder that built the current node.
     */
    public FactoryBuilderSupport getCurrentBuilder() {
        return (FactoryBuilderSupport) getContextAttribute(CURRENT_BUILDER);
    }

    /**
     * @return the node of the parent of the current node.
     */
    public Object getParentNode() {
        return getContextAttribute(PARENT_NODE);
    }

    /**
     * @return the factory of the parent of the current node.
     */
    public Factory getParentFactory() {
        return (Factory) getContextAttribute(PARENT_FACTORY);
    }

    /**
     * @return the context of the parent of the current node.
     */
    public Map getParentContext() {
        return (Map) getContextAttribute(PARENT_CONTEXT);
    }

    /**
     * @return the name of the parent of the current node.
     */
    public String getParentName() {
        return (String) getContextAttribute(PARENT_NAME);
    }

    public FactoryBuilderSupport getChildBuilder() {
        return (FactoryBuilderSupport) getContextAttribute(CHILD_BUILDER);
    }

    public Object getContextAttribute(String key) {
        Map context = getContext();
        if (context != null) {
            return context.get(key);
        }
        return null;
    }

    /**
     * Convenience method when no arguments are required
     *
     * @param methodName the name of the method to invoke
     * @return the result of the call
     */
    public Object invokeMethod(String methodName) {
        return getProxyBuilder().invokeMethod(methodName, null);
    }

    public Object invokeMethod(String methodName, Object args) {
        Object name = getProxyBuilder().getName(methodName);
        Object result;
        Object previousContext = getProxyBuilder().getContext();
        try {
            result = getProxyBuilder().doInvokeMethod(methodName, name, args);
        } catch (RuntimeException e) {
            // remove contexts created after we started
            if (getContexts().contains(previousContext)) {
                Map<String, Object> context = getProxyBuilder().getContext();
                while (context != null && context != previousContext) {
                    getProxyBuilder().popContext();
                    context = getProxyBuilder().getContext();
                }
            }
            throw e;
        }
        return result;
    }

    /**
     * Add an attribute delegate so it can intercept attributes being set.
     * Attribute delegates are fired in a FILO pattern, so that nested delegates
     * get first crack.
     *
     * @param attrDelegate the closure to be called
     * @return attrDelegate
     */
    public Closure addAttributeDelegate(Closure attrDelegate) {
        getProxyBuilder().attributeDelegates.addFirst(attrDelegate);
        return attrDelegate;
    }

    /**
     * Remove the most recently added instance of the attribute delegate.
     *
     * @param attrDelegate the instance of the closure to be removed
     */
    public void removeAttributeDelegate(Closure attrDelegate) {
        getProxyBuilder().attributeDelegates.remove(attrDelegate);
    }

    /**
     * Add a preInstantiate delegate so it can intercept nodes before they are
     * created. PreInstantiate delegates are fired in a FILO pattern, so that
     * nested delegates get first crack.
     *
     * @param delegate the closure to invoke
     * @return delegate
     */
    public Closure addPreInstantiateDelegate(Closure delegate) {
        getProxyBuilder().preInstantiateDelegates.addFirst(delegate);
        return delegate;
    }

    /**
     * Remove the most recently added instance of the preInstantiate delegate.
     *
     * @param delegate the closure to invoke
     */
    public void removePreInstantiateDelegate(Closure delegate) {
        getProxyBuilder().preInstantiateDelegates.remove(delegate);
    }

    /**
     * Add a postInstantiate delegate so it can intercept nodes after they are
     * created. PostInstantiate delegates are fired in a FILO pattern, so that
     * nested delegates get first crack.
     *
     * @param delegate the closure to invoke
     * @return delegate
     */
    public Closure addPostInstantiateDelegate(Closure delegate) {
        getProxyBuilder().postInstantiateDelegates.addFirst(delegate);
        return delegate;
    }

    /**
     * Remove the most recently added instance of the postInstantiate delegate.
     *
     * @param delegate the closure to invoke
     */
    public void removePostInstantiateDelegate(Closure delegate) {
        getProxyBuilder().postInstantiateDelegates.remove(delegate);
    }

    /**
     * Add a nodeCompletion delegate so it can intercept nodes after they done
     * with building. NodeCompletion delegates are fired in a FILO pattern, so
     * that nested delegates get first crack.
     *
     * @param delegate the closure to invoke
     * @return delegate
     */
    public Closure addPostNodeCompletionDelegate(Closure delegate) {
        getProxyBuilder().postNodeCompletionDelegates.addFirst(delegate);
        return delegate;
    }

    /**
     * Remove the most recently added instance of the nodeCompletion delegate.
     *
     * @param delegate the closure to be removed
     */
    public void removePostNodeCompletionDelegate(Closure delegate) {
        getProxyBuilder().postNodeCompletionDelegates.remove(delegate);
    }

    public void registerExplicitProperty(String name, Closure getter, Closure setter) {
        registerExplicitProperty(name, registrationGroupName, getter, setter);
    }

    public void registerExplicitProperty(String name, String groupName, Closure getter, Closure setter) {
        // set the delegate to FBS so the closure closes over the builder
        if (getter != null) getter.setDelegate(this);
        if (setter != null) setter.setDelegate(this);
        explicitProperties.put(name, new Closure[]{getter, setter});
        String methodNameBase = capitalize(name);
        if (getter != null) {
            getRegistrationGroup(groupName).add("get" + methodNameBase);
        }
        if (setter != null) {
            getRegistrationGroup(groupName).add("set" + methodNameBase);
        }
    }

    public void registerExplicitMethod(String name, Closure closure) {
        registerExplicitMethod(name, registrationGroupName, closure);
    }

    public void registerExplicitMethod(String name, String groupName, Closure closure) {
        // set the delegate to FBS so the closure closes over the builder
        closure.setDelegate(this);
        explicitMethods.put(name, closure);
        getRegistrationGroup(groupName).add(name);
    }

    /**
     * Registers a factory for a JavaBean.<br>
     * The JavaBean class should have a no-args constructor.
     *
     * @param theName   name of the node
     * @param beanClass the factory to handle the name
     */
    public void registerBeanFactory(String theName, Class beanClass) {
        registerBeanFactory(theName, registrationGroupName, beanClass);
    }

    /**
     * Registers a factory for a JavaBean.<br>
     * The JavaBean class should have a no-args constructor.
     *
     * @param theName   name of the node
     * @param groupName thr group to register this node in
     * @param beanClass the factory to handle the name
     */
    public void registerBeanFactory(String theName, String groupName, final Class beanClass) {
        getProxyBuilder().registerFactory(theName, new AbstractFactory() {
            public Object newInstance(FactoryBuilderSupport builder, Object name, Object value,
                                      Map properties) throws InstantiationException, IllegalAccessException {
                if (checkValueIsTypeNotString(value, name, beanClass)) {
                    return value;
                } else {
                    return beanClass.newInstance();
                }
            }
        });
        getRegistrationGroup(groupName).add(theName);
    }

    /**
     * Registers a factory for a node name.
     *
     * @param name    the name of the node
     * @param factory the factory to return the values
     */
    public void registerFactory(String name, Factory factory) {
        registerFactory(name, registrationGroupName, factory);
    }

    /**
     * Registers a factory for a node name.
     *
     * @param name      the name of the node
     * @param groupName thr group to register this node in
     * @param factory   the factory to return the values
     */
    public void registerFactory(String name, String groupName, Factory factory) {
        getProxyBuilder().factories.put(name, factory);
        getRegistrationGroup(groupName).add(name);
        factory.onFactoryRegistration(this, name, groupName);
    }

    /**
     * This method is responsible for instantiating a node and configure its
     * properties.
     *
     * @param name       the name of the node
     * @param attributes the attributes for the node
     * @param value      the value arguments for the node
     * @return the object return from the factory
     */
    protected Object createNode(Object name, Map attributes, Object value) {
        Object node;

        Factory factory = getProxyBuilder().resolveFactory(name, attributes, value);
        if (factory == null) {
            LOG.log(Level.WARNING, "Could not find match for name '" + name + "'");
            throw new MissingMethodExceptionNoStack((String) name, Object.class, new Object[]{attributes, value});
            //return null;
        }
        getProxyBuilder().getContext().put(CURRENT_FACTORY, factory);
        getProxyBuilder().getContext().put(CURRENT_NAME, String.valueOf(name));
        getProxyBuilder().preInstantiate(name, attributes, value);
        try {
            node = factory.newInstance(getProxyBuilder().getChildBuilder(), name, value, attributes);
            if (node == null) {
                LOG.log(Level.WARNING, "Factory for name '" + name + "' returned null");
                return null;
            }

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("For name: " + name + " created node: " + node);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create component for '" + name + "' reason: "
                    + e, e);
        }
        getProxyBuilder().postInstantiate(name, attributes, node);
        getProxyBuilder().handleNodeAttributes(node, attributes);
        return node;
    }

    /**
     * This is a hook for subclasses to plugin a custom strategy for mapping
     * names to factories.
     *
     * @param name       the name of the factory
     * @param attributes the attributes from the node
     * @param value      value arguments from te node
     * @return the Factory associated with name.<br>
     */
    protected Factory resolveFactory(Object name, Map attributes, Object value) {
        getProxyBuilder().getContext().put(CHILD_BUILDER, getProxyBuilder());
        return getProxyBuilder().getFactories().get(name);
    }

    /**
     * This is a hook for subclasses to plugin a custom strategy for mapping
     * names to explicit methods.
     *
     * @param methodName the name of the explicit method
     * @param args       the arguments for the method
     * @return the closure for the matched explicit method.<br>
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected Closure resolveExplicitMethod(String methodName, Object args) {
        return getExplicitMethods().get(methodName);
    }

    /**
     * This is a hook for subclasses to plugin a custom strategy for mapping
     * names to property methods.
     *
     * @param propertyName the name of the explicit method
     * @return the get and set closures (in that order) for the matched explicit property.<br>
     */
    protected Closure[] resolveExplicitProperty(String propertyName) {
        return getExplicitProperties().get(propertyName);
    }

    /**
     * This method is the workhorse of the builder.
     *
     * @param methodName the name of the method being invoked
     * @param name       the name of the node
     * @param args       the arguments passed into the node
     * @return the object from the factory
     */
    private Object doInvokeMethod(String methodName, Object name, Object args) {
        Reference explicitResult = new Reference();
        if (checkExplicitMethod(methodName, args, explicitResult)) {
            return explicitResult.get();
        } else {
            try {
                return dispatchNodeCall(name, args);
            } catch(MissingMethodException mme) {
                if(mme.getMethod().equals(methodName) && methodMissingDelegate != null) {
                    return methodMissingDelegate.call(methodName, args);
                }
                throw mme;
            }
        }
    }

    protected boolean checkExplicitMethod(String methodName, Object args, Reference result) {
        Closure explicitMethod = resolveExplicitMethod(methodName, args);
        if (explicitMethod != null) {
            if (args instanceof Object[]) {
                result.set(explicitMethod.call((Object[]) args));
            } else {
                //todo push through InvokerHelper.asList?
                result.set(explicitMethod.call(args));
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Use {@link FactoryBuilderSupport#dispatchNodeCall(Object, Object)} instead.
     */
    @Deprecated
    protected Object dispathNodeCall(Object name, Object args) {
        return dispatchNodeCall(name, args);
    }

    protected Object dispatchNodeCall(Object name, Object args) {
        Object node;
        Closure closure = null;
        List list = InvokerHelper.asList(args);

        final boolean needToPopContext;
        if (getProxyBuilder().getContexts().isEmpty()) {
            // should be called on first build method only
            getProxyBuilder().newContext();
            needToPopContext = true;
        } else {
            needToPopContext = false;
        }

        try {
            Map namedArgs = Collections.EMPTY_MAP;

            // the arguments come in like [named_args?, args..., closure?]
            // so peel off a hashmap from the front, and a closure from the
            // end and presume that is what they meant, since there is
            // no way to distinguish node(a:b,c,d) {..} from
            // node([a:b],[c,d], {..}), i.e. the user can deliberately confuse
            // the builder and there is nothing we can really do to prevent
            // that

            if ((!list.isEmpty())
                    && (list.get(0) instanceof LinkedHashMap)) {
                namedArgs = (Map) list.get(0);
                list = list.subList(1, list.size());
            }
            if ((!list.isEmpty())
                    && (list.get(list.size() - 1) instanceof Closure)) {
                closure = (Closure) list.get(list.size() - 1);
                list = list.subList(0, list.size() - 1);
            }
            Object arg;
            if (list.isEmpty()) {
                arg = null;
            } else if (list.size() == 1) {
                arg = list.get(0);
            } else {
                arg = list;
            }
            node = getProxyBuilder().createNode(name, namedArgs, arg);

            Object current = getProxyBuilder().getCurrent();
            if (current != null) {
                getProxyBuilder().setParent(current, node);
            }

            if (closure != null) {
                Factory parentFactory = getProxyBuilder().getCurrentFactory();
                if (parentFactory.isLeaf()) {
                    throw new RuntimeException("'" + name + "' doesn't support nesting.");
                }
                boolean processContent = true;
                if (parentFactory.isHandlesNodeChildren()) {
                    processContent = parentFactory.onNodeChildren(this, node, closure);
                }
                if (processContent) {
                    // push new node on stack
                    String parentName = getProxyBuilder().getCurrentName();
                    Map parentContext = getProxyBuilder().getContext();
                    getProxyBuilder().newContext();
                    try {
                        getProxyBuilder().getContext().put(OWNER, closure.getOwner());
                        getProxyBuilder().getContext().put(CURRENT_NODE, node);
                        getProxyBuilder().getContext().put(PARENT_FACTORY, parentFactory);
                        getProxyBuilder().getContext().put(PARENT_NODE, current);
                        getProxyBuilder().getContext().put(PARENT_CONTEXT, parentContext);
                        getProxyBuilder().getContext().put(PARENT_NAME, parentName);
                        getProxyBuilder().getContext().put(PARENT_BUILDER, parentContext.get(CURRENT_BUILDER));
                        getProxyBuilder().getContext().put(CURRENT_BUILDER, parentContext.get(CHILD_BUILDER));
                        // lets register the builder as the delegate
                        getProxyBuilder().setClosureDelegate(closure, node);
                        closure.call();
                    } finally {
                        getProxyBuilder().popContext();
                    }
                }
            }

            getProxyBuilder().nodeCompleted(current, node);
            node = getProxyBuilder().postNodeCompletion(current, node);
        } finally {
            if (needToPopContext) {
                // pop the first context
                getProxyBuilder().popContext();
            }
        }
        return node;
    }

    /**
     * A hook to allow names to be converted into some other object such as a
     * QName in XML or ObjectName in JMX.
     *
     * @param methodName the name of the desired method
     * @return the object representing the name
     */
    public Object getName(String methodName) {
        if (getProxyBuilder().nameMappingClosure != null) {
            return getProxyBuilder().nameMappingClosure.call(methodName);
        }
        return methodName;
    }

    /**
     * Proxy builders are useful for changing the building context, thus
     * enabling mix &amp; match builders.
     *
     * @return the current builder that serves as a proxy.<br>
     */
    protected FactoryBuilderSupport getProxyBuilder() {
        FactoryBuilderSupport proxy = localProxyBuilder.get();
        if (proxy == null) {
            return globalProxyBuilder;
        } else {
            return proxy;
        }
    }

    /**
     * Sets the builder to be used as a proxy.
     *
     * @param proxyBuilder the new proxy
     */
    protected void setProxyBuilder(FactoryBuilderSupport proxyBuilder) {
        globalProxyBuilder = proxyBuilder;
    }

    public Closure getNameMappingClosure() {
        return nameMappingClosure;
    }

    public void setNameMappingClosure(Closure nameMappingClosure) {
        this.nameMappingClosure = nameMappingClosure;
    }

    /**
     * Assigns any existing properties to the node.<br>
     * It will call attributeDelegates before passing control to the factory
     * that built the node.
     *
     * @param node       the object returned by tne node factory
     * @param attributes the attributes for the node
     */
    protected void handleNodeAttributes(Object node, Map attributes) {
        // first, short circuit
        if (node == null) {
            return;
        }

        for (Closure attrDelegate : getProxyBuilder().getAttributeDelegates()) {
            FactoryBuilderSupport builder = this;
            if (attrDelegate.getOwner() instanceof FactoryBuilderSupport) {
                builder = (FactoryBuilderSupport) attrDelegate.getOwner();
            } else if (attrDelegate.getDelegate() instanceof FactoryBuilderSupport) {
                builder = (FactoryBuilderSupport) attrDelegate.getDelegate();
            }

            attrDelegate.call(builder, node, attributes);
        }

        if (getProxyBuilder().getCurrentFactory().onHandleNodeAttributes(getProxyBuilder().getChildBuilder(), node, attributes)) {
            getProxyBuilder().setNodeAttributes(node, attributes);
        }
    }

    /**
     * Pushes a new context on the stack.
     */
    protected void newContext() {
        getContexts().addFirst(new HashMap<>());
    }

    /**
     * A hook to allow nodes to be processed once they have had all of their
     * children applied.
     *
     * @param node   the current node being processed
     * @param parent the parent of the node being processed
     */
    protected void nodeCompleted(Object parent, Object node) {
        getProxyBuilder().getCurrentFactory().onNodeCompleted(getProxyBuilder().getChildBuilder(), parent, node);
    }

    /**
     * Removes the last context from the stack.
     *
     * @return the content just removed
     */
    protected Map<String, Object> popContext() {
        if (!getProxyBuilder().getContexts().isEmpty()) {
            return getProxyBuilder().getContexts().removeFirst();
        }
        return null;
    }

    /**
     * A hook after the factory creates the node and before attributes are set.<br>
     * It will call any registered postInstantiateDelegates, if you override
     * this method be sure to call this impl somewhere in your code.
     *
     * @param name       the name of the node
     * @param attributes the attributes for the node
     * @param node       the object created by the node factory
     */
    protected void postInstantiate(Object name, Map attributes, Object node) {
        for (Closure postInstantiateDelegate : getProxyBuilder().getPostInstantiateDelegates()) {
            (postInstantiateDelegate).call(this, attributes, node);
        }
    }

    /**
     * A hook to allow nodes to be processed once they have had all of their
     * children applied and allows the actual node object that represents the
     * Markup element to be changed.<br>
     * It will call any registered postNodeCompletionDelegates, if you override
     * this method be sure to call this impl at the end of your code.
     *
     * @param node   the current node being processed
     * @param parent the parent of the node being processed
     * @return the node, possibly new, that represents the markup element
     */
    protected Object postNodeCompletion(Object parent, Object node) {
        for (Closure postNodeCompletionDelegate : getProxyBuilder().getPostNodeCompletionDelegates()) {
            (postNodeCompletionDelegate).call(this, parent, node);
        }

        return node;
    }

    /**
     * A hook before the factory creates the node.<br>
     * It will call any registered preInstantiateDelegates, if you override this
     * method be sure to call this impl somewhere in your code.
     *
     * @param name       the name of the node
     * @param attributes the attributes of the node
     * @param value      the value argument(s) of the node
     */
    protected void preInstantiate(Object name, Map attributes, Object value) {
        for (Closure preInstantiateDelegate : getProxyBuilder().getPreInstantiateDelegates()) {
            (preInstantiateDelegate).call(this, attributes, value);
        }
    }

    /**
     * Clears the context stack.
     */
    protected void reset() {
        getProxyBuilder().getContexts().clear();
    }

    /**
     * A strategy method to allow derived builders to use builder-trees and
     * switch in different kinds of builders. This method should call the
     * setDelegate() method on the closure which by default passes in this but
     * if node is-a builder we could pass that in instead (or do something wacky
     * too)
     *
     * @param closure the closure on which to call setDelegate()
     * @param node    the node value that we've just created, which could be a
     *                builder
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected void setClosureDelegate(Closure closure, Object node) {
        closure.setDelegate(this);
    }

    /**
     * Maps attributes key/values to properties on node.
     *
     * @param node       the object from the node
     * @param attributes the attributes to be set
     */
    protected void setNodeAttributes(Object node, Map attributes) {
        // set the properties
        //noinspection unchecked
        for (Map.Entry entry : (Set<Map.Entry>) attributes.entrySet()) {
            String property = entry.getKey().toString();
            Object value = entry.getValue();
            InvokerHelper.setProperty(node, property, value);
        }
    }

    /**
     * Strategy method to establish parent/child relationships.
     *
     * @param parent the object from the parent node
     * @param child  the object from the child node
     */
    protected void setParent(Object parent, Object child) {
        getProxyBuilder().getCurrentFactory().setParent(getProxyBuilder().getChildBuilder(), parent, child);
        Factory parentFactory = getProxyBuilder().getParentFactory();
        if (parentFactory != null) {
            parentFactory.setChild(getProxyBuilder().getCurrentBuilder(), parent, child);
        }
    }

    /**
     * @return the stack of available contexts.
     */
    protected LinkedList<Map<String, Object>> getContexts() {
        LinkedList<Map<String, Object>> contexts = getProxyBuilder().contexts.get();
        if (contexts == null) {
            contexts = new LinkedList<>();
            getProxyBuilder().contexts.set(contexts);
        }
        return contexts;
    }

    /**
     * Stores the thread local states in a Map that can be passed across threads
     * @return the map
     */
    protected Map<String, Object> getContinuationData() {
        Map<String, Object> data = new HashMap<>();
        data.put("proxyBuilder", localProxyBuilder.get());
        data.put("contexts", contexts.get());
        return data;
    }

    /**
     * Restores the state of the current builder to the same state as an older build.
     * 
     * Caution, this will destroy rather than merge the current build context if there is any,
     * @param data the data retrieved from a compatible getContinuationData call
     */
    protected void restoreFromContinuationData(Map<String, Object> data) {
        //noinspection unchecked
        localProxyBuilder.set((FactoryBuilderSupport) data.get("proxyBuilder"));
        //noinspection unchecked
        contexts.set((LinkedList<Map<String, Object>>) data.get("contexts"));
    }

    public Object build(Class viewClass) {
        if (Script.class.isAssignableFrom(viewClass)) {
            Script script = InvokerHelper.createScript(viewClass, this);
            return build(script);
        } else {
            throw new RuntimeException("Only scripts can be executed via build(Class)");
        }
    }

    public Object build(Script script) {
        // this used to be synchronized, but we also used to remove the
        // metaclass.  Since adding the metaclass is now a side effect, we
        // don't need to ensure the meta-class won't be observed and don't
        // need to hide the side effect.
        MetaClass scriptMetaClass = script.getMetaClass();
        script.setMetaClass(new FactoryInterceptorMetaClass(scriptMetaClass, this));
        script.setBinding(this);
        Object oldScriptName = getProxyBuilder().getVariables().get(SCRIPT_CLASS_NAME);
        try {
            getProxyBuilder().setVariable(SCRIPT_CLASS_NAME, script.getClass().getName());
            return script.run();
        } finally {
            if(oldScriptName != null) {
                getProxyBuilder().setVariable(SCRIPT_CLASS_NAME, oldScriptName);
            } else {
                getProxyBuilder().getVariables().remove(SCRIPT_CLASS_NAME);
            }
        }
    }

    public Object build(final String script, GroovyClassLoader loader) {
        return build(loader.parseClass(script));
    }

    /**
     * Switches the builder's proxyBuilder during the execution of a closure.<br>
     * This is useful to temporary change the building context to another builder
     * without the need for a contrived setup. It will also take care of restoring
     * the previous proxyBuilder when the execution finishes, even if an exception
     * was thrown from inside the closure.
     *
     * @param builder the temporary builder to switch to as proxyBuilder.
     * @param closure the closure to be executed under the temporary builder.
     * @return the execution result of the closure.
     * @throws RuntimeException - any exception the closure might have thrown during
     *                          execution.
     */
    public Object withBuilder(FactoryBuilderSupport builder, Closure closure) {
        if (builder == null || closure == null) {
            return null;
        }

        Object result = null;
        Object previousContext = getProxyBuilder().getContext();
        FactoryBuilderSupport previousProxyBuilder = localProxyBuilder.get();
        try {
            localProxyBuilder.set(builder);
            closure.setDelegate(builder);
            result = closure.call();
        }
        catch (RuntimeException e) {
            // remove contexts created after we started
            localProxyBuilder.set(previousProxyBuilder);
            if (getProxyBuilder().getContexts().contains(previousContext)) {
                Map<String, Object> context = getProxyBuilder().getContext();
                while (context != null && context != previousContext) {
                    getProxyBuilder().popContext();
                    context = getProxyBuilder().getContext();
                }
            }
            throw e;
        }
        finally {
            localProxyBuilder.set(previousProxyBuilder);
        }

        return result;
    }

    /**
     * Switches the builder's proxyBuilder during the execution of a closure.<br>
     * This is useful to temporary change the building context to another builder
     * without the need for a contrived setup. It will also take care of restoring
     * the previous proxyBuilder when the execution finishes, even if an exception
     * was thrown from inside the closure. Additionally it will use the closure's
     * result as the value for the node identified by 'name'.
     *
     * @param builder the temporary builder to switch to as proxyBuilder.
     * @param name    the node to build on the 'parent' builder.
     * @param closure the closure to be executed under the temporary builder.
     * @return a node that responds to value of name with the closure's result as its
     *         value.
     * @throws RuntimeException - any exception the closure might have thrown during
     *                          execution.
     */
    public Object withBuilder(FactoryBuilderSupport builder, String name, Closure closure) {
        if (name == null) {
            return null;
        }
        Object result = getProxyBuilder().withBuilder(builder, closure);
        return getProxyBuilder().invokeMethod(name, new Object[]{result});
    }

    /**
     * Switches the builder's proxyBuilder during the execution of a closure.<br>
     * This is useful to temporary change the building context to another builder
     * without the need for a contrived setup. It will also take care of restoring
     * the previous proxyBuilder when the execution finishes, even if an exception
     * was thrown from inside the closure. Additionally it will use the closure's
     * result as the value for the node identified by 'name' and assign any attributes
     * that might have been set.
     *
     * @param attributes additional properties for the node on the parent builder.
     * @param builder    the temporary builder to switch to as proxyBuilder.
     * @param name       the node to build on the 'parent' builder.
     * @param closure    the closure to be executed under the temporary builder.
     * @return a node that responds to value of name with the closure's result as its
     *         value.
     * @throws RuntimeException - any exception the closure might have thrown during
     *                          execution.
     */
    public Object withBuilder(Map attributes, FactoryBuilderSupport builder, String name, Closure closure) {
        if (name == null) {
            return null;
        }
        Object result = getProxyBuilder().withBuilder(builder, closure);
        return getProxyBuilder().invokeMethod(name, new Object[]{attributes, result});
    }

    public void addDisposalClosure(Closure closure) {
        disposalClosures.add(closure);
    }

    public List<Closure> getDisposalClosures() {
        return Collections.unmodifiableList(disposalClosures);
    }

    public void dispose() {
        for (int i = disposalClosures.size() - 1; i >= 0; i--) {
            disposalClosures.get(i).call();
        }
    }
}

class FactoryInterceptorMetaClass extends DelegatingMetaClass {

    FactoryBuilderSupport builder;

    public FactoryInterceptorMetaClass(MetaClass delegate, FactoryBuilderSupport builder) {
        super(delegate);
        this.builder = builder;
    }

    public Object invokeMethod(Object object, String methodName, Object arguments) {
        try {
            return delegate.invokeMethod(object, methodName, arguments);
        } catch (MissingMethodException mme) {
            // attempt builder resolution
            try {
                if (builder.getMetaClass().respondsTo(builder, methodName).isEmpty()) {
                    // dispatch to factories if it is not a literal method
                    return builder.invokeMethod(methodName, arguments);
                } else {
                    return InvokerHelper.invokeMethod(builder, methodName, arguments);
                }
            } catch (MissingMethodException mme2) {
                // chain secondary exception
                Throwable root = mme;
                while (root.getCause() != null) {
                    root = root.getCause();
                }
                root.initCause(mme2);
                // throw original
                throw mme;
            }
        }
    }

    public Object invokeMethod(Object object, String methodName, Object[] arguments) {
        try {
            return delegate.invokeMethod(object, methodName, arguments);
        } catch (MissingMethodException mme) {
            // attempt builder resolution
            try {
                if (builder.getMetaClass().respondsTo(builder, methodName).isEmpty()) {
                    // dispatch to factories if it is not a literal method
                    return builder.invokeMethod(methodName, arguments);
                } else {
                    return InvokerHelper.invokeMethod(builder, methodName, arguments);
                }
            } catch (MissingMethodException mme2) {
                // chain secondary exception
                Throwable root = mme;
                while (root.getCause() != null) {
                    root = root.getCause();
                }
                root.initCause(mme2);
                // throw original
                throw mme;
            }
        }
    }
}
