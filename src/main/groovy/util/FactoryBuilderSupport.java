/*
 * Copyright 2003-2007 the original author or authors.
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

package groovy.util;

import groovy.lang.*;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.metaclass.MissingMethodExceptionNoStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mix of BuilderSupport and SwingBuilder's factory support.
 *
 * Warning: this implementation is not thread safe and should not be used
 * across threads in a multi-threaded environment.  A locking mechanism
 * should be implemented by the subclass if use is expected across
 * multiple threads.
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Andres Almiray <aalmiray@users.sourceforge.com>
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
    private static final Logger LOG = Logger.getLogger(FactoryBuilderSupport.class.getName());

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
     * Checks type of value against buidler type
     *
     * @param value the node's value
     * @param name  the node's name
     * @param type  a Class that may be assignable to the value's class
     * @return true if type is assignalbe to the value's class, false if value
     *         is null.
     */
    public static boolean checkValueIsType(Object value, Object name, Class type) {
        if (value != null) {
            if (type.isAssignableFrom(value.getClass())) {
                return true;
            } else {
                throw new RuntimeException("The value argument of '" + name + "' must be of type "
                        + type.getName());
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
     * @return Returns true if type is assignale to the value's class, false if value is
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
                        + type.getName() + " or a String.");
            }
        } else {
            return false;
        }
    }

    private LinkedList<Map<String, Object>> contexts = new LinkedList<Map<String, Object>>();
    protected LinkedList<Closure> attributeDelegates = new LinkedList<Closure>(); //
    private List<Closure> disposalClosures = new ArrayList<Closure>(); // because of reverse iteration use ArrayList
    private Map<String, Factory> factories = new HashMap<String, Factory>();
    private Closure nameMappingClosure;
    private FactoryBuilderSupport proxyBuilder;
    protected LinkedList<Closure> preInstantiateDelegates = new LinkedList<Closure>();
    protected LinkedList<Closure> postInstantiateDelegates = new LinkedList<Closure>();
    protected LinkedList<Closure> postNodeCompletionDelegates = new LinkedList<Closure>();
    protected Map<String, Closure[]> explicitProperties = new HashMap<String, Closure[]>();
    protected Map<String, Closure> explicitMethods = new HashMap<String, Closure>();
    protected Map<String, Set<String>> registrationGroup = new HashMap<String, Set<String>>();
    protected String registringGroupName = ""; // use binding to store?

    protected boolean autoRegistrationRunning = false;
    protected boolean autoRegistrationComplete = false;

    public FactoryBuilderSupport() {
        this(false);
    }

    public FactoryBuilderSupport(boolean init) {
        this.proxyBuilder = this;
        registrationGroup.put(registringGroupName, new TreeSet<String>());
        if (init) {
            autoRegisterNodes();
        }
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
            for (Method method : getClass().getMethods()) {
                if (method.getName().startsWith("register") && method.getParameterTypes().length == 0) {
                    registringGroupName = method.getName().substring("register".length());
                    registrationGroup.put(registringGroupName, new TreeSet<String>());
                    try {
                        method.invoke(this);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Cound not init " + getClass().getName() + " because of an access error in " + method.getName(), e);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException("Cound not init " + getClass().getName() + " because of an exception in " + method.getName(), e);
                    } finally {
                        registringGroupName = "";
                    }
                }
            }
        } finally {
            autoRegistrationComplete = true;
            autoRegistrationRunning = false;
        }
    }

    @Deprecated
    public FactoryBuilderSupport(Closure nameMappingClosure) {
        this.proxyBuilder = this;
        this.nameMappingClosure = nameMappingClosure;
    }

    /**
     * @param name the name of the variable to lookup
     * @return the variable value
     */
    public Object getVariable(String name) {
        return proxyBuilder.doGetVariable(name);
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
        proxyBuilder.doSetVariable(name, value);
    }

    private void doSetVariable(String name, Object value) {
        super.setVariable(name, value);
    }

    public Map getVariables() {
        return proxyBuilder.doGetVariables();
    }

    private Map doGetVariables() {
        return super.getVariables();
    }

    /**
     * Overloaded to make variables appear as bean properties or via the subscript operator
     */
    public Object getProperty(String property) {
        try {
            return proxyBuilder.doGetProperty(property);
        } catch (MissingPropertyException mpe) {
            if ((getContext() != null) && (getContext().containsKey(property))) {
                return getContext().get(property);
            } else {
                return getMetaClass().getProperty(this, property);
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
        proxyBuilder.doSetProperty(property, newValue);
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
        return Collections.unmodifiableMap(proxyBuilder.factories);
    }

    /**
     * @return the explicit methods map (Unmodifiable Map).
     */
    public Map<String, Closure> getExplicitMethods() {
        return Collections.unmodifiableMap(proxyBuilder.explicitMethods);
    }

    /**
     * @return the explicit properties map (Unmodifiable Map).
     */
    public Map<String, Closure[]> getExplicitProperties() {
        return Collections.unmodifiableMap(proxyBuilder.explicitProperties);
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

    /**
     * @return the context of the current node.
     */
    public Map<String, Object> getContext() {
        if (!proxyBuilder.contexts.isEmpty()) {
            return proxyBuilder.contexts.getFirst();
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
        if (!proxyBuilder.contexts.isEmpty()) {
            Map context = proxyBuilder.contexts.getFirst();
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
        return proxyBuilder.invokeMethod(methodName, null);
    }

    public Object invokeMethod(String methodName, Object args) {
        Object name = proxyBuilder.getName(methodName);
        Object result;
        Object previousContext = proxyBuilder.getContext();
        try {
            result = proxyBuilder.doInvokeMethod(methodName, name, args);
        } catch (RuntimeException e) {
            // remove contexts created after we started
            if (proxyBuilder.contexts.contains(previousContext)) {
                Map<String, Object> context = proxyBuilder.getContext();
                while (context != null && context != previousContext) {
                    proxyBuilder.popContext();
                    context = proxyBuilder.getContext();
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
        proxyBuilder.attributeDelegates.addFirst(attrDelegate);
        return attrDelegate;
    }

    /**
     * Remove the most recently added instance of the attribute delegate.
     *
     * @param attrDelegate the instance of the closure to be removed
     */
    public void removeAttributeDelegate(Closure attrDelegate) {
        proxyBuilder.attributeDelegates.remove(attrDelegate);
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
        proxyBuilder.preInstantiateDelegates.addFirst(delegate);
        return delegate;
    }

    /**
     * Remove the most recently added instance of the preInstantiate delegate.
     *
     * @param delegate the closure to invoke
     */
    public void removePreInstantiateDelegate(Closure delegate) {
        proxyBuilder.preInstantiateDelegates.remove(delegate);
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
        proxyBuilder.postInstantiateDelegates.addFirst(delegate);
        return delegate;
    }

    /**
     * Remove the most recently added instance of the postInstantiate delegate.
     *
     * @param delegate the closure to invoke
     */
    public void removePostInstantiateDelegate(Closure delegate) {
        proxyBuilder.postInstantiateDelegates.remove(delegate);
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
        proxyBuilder.postNodeCompletionDelegates.addFirst(delegate);
        return delegate;
    }

    /**
     * Remove the most recently added instance of the nodeCompletion delegate.
     *
     * @param delegate the closure to be removed
     */
    public void removePostNodeCompletionDelegate(Closure delegate) {
        proxyBuilder.postNodeCompletionDelegates.remove(delegate);
    }

    public void registerExplicitProperty(String name, Closure getter, Closure setter) {
        registerExplicitProperty(name, registringGroupName, getter, setter);
    }

    public void registerExplicitProperty(String name, String groupName, Closure getter, Closure setter) {
        // set the delegate to FBS so the closure closes over the builder
        if (getter != null) getter.setDelegate(this);
        if (setter != null) setter.setDelegate(this);
        explicitProperties.put(name, new Closure[]{getter, setter});
        String methodNameBase = MetaClassHelper.capitalize(name);
        if (getter != null) {
            registrationGroup.get(groupName).add("get" + methodNameBase);
        }
        if (setter != null) {
            registrationGroup.get(groupName).add("set" + methodNameBase);
        }
    }

    public void registerExplicitMethod(String name, Closure closure) {
        registerExplicitMethod(name, registringGroupName, closure);
    }

    public void registerExplicitMethod(String name, String groupName, Closure closure) {
        // set the delegate to FBS so the closure closes over the builder
        closure.setDelegate(this);
        explicitMethods.put(name, closure);
        registrationGroup.get(groupName).add(name);
    }

    /**
     * Registers a factory for a JavaBean.<br>
     * The JavaBean clas should have a no-args constructor.
     *
     * @param theName   name of the node
     * @param beanClass the factory to handle the name
     */
    public void registerBeanFactory(String theName, Class beanClass) {
        registerBeanFactory(theName, registringGroupName, beanClass);
    }

    /**
     * Registers a factory for a JavaBean.<br>
     * The JavaBean clas should have a no-args constructor.
     *
     * @param theName   name of the node
     * @param groupName thr group to register this node in
     * @param beanClass the factory to handle the name
     */
    public void registerBeanFactory(String theName, String groupName, final Class beanClass) {
        proxyBuilder.registerFactory(theName, new AbstractFactory() {
            public Object newInstance(FactoryBuilderSupport builder, Object name, Object value,
                                      Map properties) throws InstantiationException, IllegalAccessException {
                if (checkValueIsTypeNotString(value, name, beanClass)) {
                    return value;
                } else {
                    return beanClass.newInstance();
                }
            }

        });
        registrationGroup.get(groupName).add(theName);
    }

    /**
     * Registers a factory for a node name.
     *
     * @param name    the name of the node
     * @param factory the factory to return the values
     */
    public void registerFactory(String name, Factory factory) {
        registerFactory(name, registringGroupName, factory);
    }

    /**
     * Registers a factory for a node name.
     *
     * @param name      the name of the node
     * @param groupName thr group to register this node in
     * @param factory   the factory to return the values
     */
    public void registerFactory(String name, String groupName, Factory factory) {
        proxyBuilder.factories.put(name, factory);
        registrationGroup.get(groupName).add(name);
        factory.onFactoryRegistration(this, name, groupName);
    }

    /**
     * This method is responsible for instanciating a node and configure its
     * properties.
     *
     * @param name       the name of the node
     * @param attributes the attributes for the node
     * @param value      the value arguments for the node
     * @return the object return from the factory
     */
    protected Object createNode(Object name, Map attributes, Object value) {
        Object node;

        Factory factory = proxyBuilder.resolveFactory(name, attributes, value);
        if (factory == null) {
            LOG.log(Level.WARNING, "Could not find match for name '" + name + "'");
            throw new MissingMethodExceptionNoStack((String) name, Object.class, new Object[]{attributes, value});
            //return null;
        }
        proxyBuilder.getContext().put(CURRENT_FACTORY, factory);
        proxyBuilder.getContext().put(CURRENT_NAME, String.valueOf(name));
        proxyBuilder.preInstantiate(name, attributes, value);
        try {
            node = factory.newInstance(proxyBuilder.getChildBuilder(), name, value, attributes);
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
        proxyBuilder.postInstantiate(name, attributes, node);
        proxyBuilder.handleNodeAttributes(node, attributes);
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
        proxyBuilder.getContext().put(CHILD_BUILDER, proxyBuilder);
        return proxyBuilder.factories.get(name);
    }

    /**
     * This is a hook for subclasses to plugin a custom strategy for mapping
     * names to explicit methods.
     *
     * @param methodName the name of the explicit method
     * @param args       the arguments for the method
     * @return the closure for the matched explicit method.<br>
     */
    protected Closure resolveExplicitMethod(String methodName, Object args) {
        return explicitMethods.get(methodName);
    }

    /**
     * This is a hook for subclasses to plugin a custom strategy for mapping
     * names to property methods.
     *
     * @param propertyName the name of the explicit method
     * @return the get and set closures (in that order) for the matched explicit property.<br>
     */
    protected Closure[] resolveExplicitProperty(String propertyName) {
        return explicitProperties.get(propertyName);
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
            return dispathNodeCall(name, args);
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

    protected Object dispathNodeCall(Object name, Object args) {
        Object node;
        Closure closure = null;
        List list = InvokerHelper.asList(args);

        final boolean needToPopContext;
        if (proxyBuilder.getContexts().isEmpty()) {
            // should be called on first build method only
            proxyBuilder.newContext();
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
            // node([a:b],[c,d], {..}), i.e. the user can deliberatly confuse
            // the builder and there is nothing we can really do to prevent
            // that

            if ((list.size() > 0)
                    && (list.get(0) instanceof LinkedHashMap)) {
                namedArgs = (Map) list.get(0);
                list = list.subList(1, list.size());
            }
            if ((list.size() > 0)
                    && (list.get(list.size() - 1) instanceof Closure)) {
                closure = (Closure) list.get(list.size() - 1);
                list = list.subList(0, list.size() - 1);
            }
            Object arg;
            if (list.size() == 0) {
                arg = null;
            } else if (list.size() == 1) {
                arg = list.get(0);
            } else {
                arg = list;
            }
            node = proxyBuilder.createNode(name, namedArgs, arg);

            Object current = proxyBuilder.getCurrent();
            if (current != null) {
                proxyBuilder.setParent(current, node);
            }

            if (closure != null) {
                Factory parentFactory = proxyBuilder.getCurrentFactory();
                if (parentFactory.isLeaf()) {
                    throw new RuntimeException("'" + name + "' doesn't support nesting.");
                }
                boolean processContent = true;
                if (parentFactory.isHandlesNodeChildren()) {
                    processContent = parentFactory.onNodeChildren(this, node, closure);
                }
                if (processContent) {
                    // push new node on stack
                    String parentName = proxyBuilder.getCurrentName();
                    Map parentContext = proxyBuilder.getContext();
                    proxyBuilder.newContext();
                    try {
                        proxyBuilder.getContext().put(OWNER, closure.getOwner());
                        proxyBuilder.getContext().put(CURRENT_NODE, node);
                        proxyBuilder.getContext().put(PARENT_FACTORY, parentFactory);
                        proxyBuilder.getContext().put(PARENT_NODE, current);
                        proxyBuilder.getContext().put(PARENT_CONTEXT, parentContext);
                        proxyBuilder.getContext().put(PARENT_NAME, parentName);
                        proxyBuilder.getContext().put(PARENT_BUILDER, parentContext.get(CURRENT_BUILDER));
                        proxyBuilder.getContext().put(CURRENT_BUILDER, parentContext.get(CHILD_BUILDER));
                        // lets register the builder as the delegate
                        proxyBuilder.setClosureDelegate(closure, node);
                        closure.call();
                    } finally {
                        proxyBuilder.popContext();
                    }
                }
            }

            proxyBuilder.nodeCompleted(current, node);
            node = proxyBuilder.postNodeCompletion(current, node);
        } finally {
            if (needToPopContext) {
                // pop the first context
                proxyBuilder.popContext();
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
        if (proxyBuilder.nameMappingClosure != null) {
            return proxyBuilder.nameMappingClosure.call(methodName);
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
        return proxyBuilder;
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

        for (Closure attrDelegate : proxyBuilder.attributeDelegates) {
            FactoryBuilderSupport builder = this;
            if (attrDelegate.getOwner() instanceof FactoryBuilderSupport) {
                builder = (FactoryBuilderSupport) attrDelegate.getOwner();
            } else if (attrDelegate.getDelegate() instanceof FactoryBuilderSupport) {
                builder = (FactoryBuilderSupport) attrDelegate.getDelegate();
            }

            attrDelegate.call(new Object[]{builder, node, attributes});
        }

        if (proxyBuilder.getCurrentFactory().onHandleNodeAttributes(proxyBuilder.getChildBuilder(), node, attributes)) {
            proxyBuilder.setNodeAttributes(node, attributes);
        }
    }

    /**
     * Pushes a new context on the stack.
     */
    protected void newContext() {
        proxyBuilder.contexts.addFirst(new HashMap<String, Object>());
    }

    /**
     * A hook to allow nodes to be processed once they have had all of their
     * children applied.
     *
     * @param node   the current node being processed
     * @param parent the parent of the node being processed
     */
    protected void nodeCompleted(Object parent, Object node) {
        proxyBuilder.getCurrentFactory().onNodeCompleted(proxyBuilder.getChildBuilder(), parent, node);
    }

    /**
     * Removes the last context from the stack.
     *
     * @return the contet just removed
     */
    protected Map<String, Object> popContext() {
        if (!proxyBuilder.contexts.isEmpty()) {
            return proxyBuilder.contexts.removeFirst();
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
     * @param node       the object created by teh node factory
     */
    protected void postInstantiate(Object name, Map attributes, Object node) {
        for (Closure postInstantiateDelegate : proxyBuilder.postInstantiateDelegates) {
            (postInstantiateDelegate).call(new Object[]{this, attributes, node});
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
        for (Closure postNodeCompletionDelegate : proxyBuilder.postNodeCompletionDelegates) {
            (postNodeCompletionDelegate).call(new Object[]{this, parent, node});
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
        for (Closure preInstantiateDelegate : proxyBuilder.preInstantiateDelegates) {
            (preInstantiateDelegate).call(new Object[]{this, attributes, value});
        }
    }

    /**
     * Clears the context stack.
     */
    protected void reset() {
        proxyBuilder.contexts.clear();
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
    protected void setClosureDelegate(Closure closure, Object node) {
        closure.setDelegate(this);
    }

    /**
     * Maps attributes key/values to properties on node.
     *
     * @param node       the object from the node
     * @param attributes the attributtes to be set
     */
    protected void setNodeAttributes(Object node, Map attributes) {
        // set the properties
        for (Map.Entry entry : (Set<Map.Entry>) attributes.entrySet()) {
            String property = entry.getKey().toString();
            Object value = entry.getValue();
            InvokerHelper.setProperty(node, property, value);
        }
    }

    /**
     * Strategy method to stablish parent/child relationships.
     *
     * @param parent the object from the parent node
     * @param child  the object from the child node
     */
    protected void setParent(Object parent, Object child) {
        proxyBuilder.getCurrentFactory().setParent(proxyBuilder.getChildBuilder(), parent, child);
        Factory parentFactory = proxyBuilder.getParentFactory();
        if (parentFactory != null) {
            parentFactory.setChild(proxyBuilder.getCurrentBuilder(), parent, child);
        }
    }

    /**
     * Sets the builder to be used as a proxy.
     *
     * @param proxyBuilder the new proxy
     */
    protected void setProxyBuilder(FactoryBuilderSupport proxyBuilder) {
        this.proxyBuilder = proxyBuilder;
    }

    /**
     * @return the stack of available contexts.
     */
    protected LinkedList<? extends Map<String, Object>> getContexts() {
        return proxyBuilder.contexts;
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
        return script.run();
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
        Object previousContext = proxyBuilder.getContext();
        FactoryBuilderSupport previousProxyBuilder = proxyBuilder;
        try {
            proxyBuilder = builder;
            closure.setDelegate(builder);
            result = closure.call();
        }
        catch (RuntimeException e) {
            // remove contexts created after we started
            proxyBuilder = previousProxyBuilder;
            if (proxyBuilder.contexts.contains(previousContext)) {
                Map<String, Object> context = proxyBuilder.getContext();
                while (context != null && context != previousContext) {
                    proxyBuilder.popContext();
                    context = proxyBuilder.getContext();
                }
            }
            throw e;
        }
        finally {
            proxyBuilder = previousProxyBuilder;
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
        Object result = proxyBuilder.withBuilder(builder, closure);
        return proxyBuilder.invokeMethod(name, new Object[]{result});
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
        Object result = proxyBuilder.withBuilder(builder, closure);
        return proxyBuilder.invokeMethod(name, new Object[]{attributes, result});
    }

    public void addDisposalClosure(Closure closure) {
        disposalClosures.add(closure);
    }

    public void dispose() {
        for (int i = disposalClosures.size() - 1; i >= 0; i--) {
            disposalClosures.get(i).call();
        }
    }
}

class FactoryInterceptorMetaClass extends DelegatingMetaClass {

    FactoryBuilderSupport factory;

    public FactoryInterceptorMetaClass(MetaClass delegate, FactoryBuilderSupport factory) {
        super(delegate);
        this.factory = factory;
    }

    public Object invokeMethod(Object object, String methodName, Object arguments) {
        try {
            return delegate.invokeMethod(object, methodName, arguments);
        } catch (MissingMethodException mme) {
            // attempt factory resolution
            try {
                if (factory.getMetaClass().respondsTo(factory, methodName).isEmpty()) {
                    // dispatch to factories if it is not a literal method
                    return factory.invokeMethod(methodName, arguments);
                } else {
                    return InvokerHelper.invokeMethod(factory, methodName, arguments);
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
            // attempt factory resolution
            try {
                if (factory.getMetaClass().respondsTo(factory, methodName).isEmpty()) {
                    // dispatch to factories if it is not a literal method
                    return factory.invokeMethod(methodName, arguments);
                } else {
                    return InvokerHelper.invokeMethod(factory, methodName, arguments);
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
