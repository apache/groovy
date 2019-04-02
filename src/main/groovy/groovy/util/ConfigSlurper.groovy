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
package groovy.util

import org.codehaus.groovy.runtime.InvokerHelper

/**
 * ConfigSlurper is a utility class for reading configuration files defined in the form of Groovy
 * scripts. Configuration settings can be defined using dot notation or scoped using closures:
 *
 * <pre><code>
 * grails.webflow.stateless = true
 * smtp {
 *     mail.host = 'smtp.myisp.com'
 *     mail.auth.user = 'server'
 * }
 * resources.URL = 'http://localhost:80/resources'
 * </code></pre>
 *
 * Settings can either be bound into nested maps or onto a specified JavaBean instance.
 * In the latter case, an error will be thrown if a property cannot be bound.
 *
 * @since 1.5
 */
class ConfigSlurper {
    private static final ENVIRONMENTS_METHOD = 'environments'
    GroovyClassLoader classLoader = new GroovyClassLoader()
    private Map bindingVars = [:]

    private final Map<String, String> conditionValues = [:]
    private final Stack<Map<String, ConfigObject>> conditionalBlocks = new Stack<Map<String,ConfigObject>>()

    ConfigSlurper() {
        this('')
    }

    /**
     * Constructs a new ConfigSlurper instance using the given environment
     *
     * @param env The Environment to use
     */
    ConfigSlurper(String env) {
        conditionValues[ENVIRONMENTS_METHOD] = env
    }

    void registerConditionalBlock(String blockName, String blockValue) {
        if (blockName) {
            if (!blockValue) {
                conditionValues.remove(blockName)
            } else {
                conditionValues[blockName] = blockValue
            }
        }
    }

    Map<String, String> getConditionalBlockValues() {
        Collections.unmodifiableMap(conditionValues)
    }

    String getEnvironment() {
        conditionValues[ENVIRONMENTS_METHOD]
    }

    void setEnvironment(String environment) {
        conditionValues[ENVIRONMENTS_METHOD] = environment
    }

    /**
     * Sets any additional variables that should be placed into the binding when evaluating Config scripts
     */
    void setBinding(Map vars) {
        this.bindingVars = vars
    }

    /**
     * Parses a ConfigObject instances from an instance of java.util.Properties
     *
     * @param The java.util.Properties instance
     */
    ConfigObject parse(Properties properties) {
        ConfigObject config = new ConfigObject()
        for (key in properties.keySet()) {
            parseKey(key, config, properties)
        }
        config
    }

    @SuppressWarnings('Instanceof')
    private void parseKey(key, ConfigObject config, Properties properties) {
        def tokens = key.split(/\./)

        def current = config
        def last
        def lastToken
        def foundBase = false
        for (token in tokens) {
            if (foundBase) {
                // handle not properly nested tokens by ignoring
                // hierarchy below this point
                lastToken += '.' + token
                current = last
            } else {
                last = current
                lastToken = token
                current = current."${token}"
                if (!(current instanceof ConfigObject)) foundBase = true
            }
        }

        if (current instanceof ConfigObject) {
            if (last[lastToken]) {
                def flattened = last.flatten()
                last.clear()
                flattened.each { k2, v2 -> last[k2] = v2 }
                last[lastToken] = properties.get(key)
            } else {
                last[lastToken] = properties.get(key)
            }
        }
    }

    /**
     * Parse the given script as a string and return the configuration object
     *
     * @see ConfigSlurper#parse(groovy.lang.Script)
     */
    ConfigObject parse(String script) {
        parse(classLoader.parseClass(script))
    }

    /**
     * Create a new instance of the given script class and parse a configuration object from it
     *
     * @see ConfigSlurper#parse(groovy.lang.Script)
     */
    ConfigObject parse(Class scriptClass) {
        parse(scriptClass.newInstance())
    }

    /**
     * Parse the given script into a configuration object (a Map)
     * (This method creates a new class to parse the script each time it is called.)
     *
     * @param script The script to parse
     * @return A Map of maps that can be navigating with dot de-referencing syntax to obtain configuration entries
     */
    ConfigObject parse(Script script) {
        parse(script, null)
    }

    /**
     * Parses a Script represented by the given URL into a ConfigObject
     *
     * @param scriptLocation The location of the script to parse
     * @return The ConfigObject instance
     */
    ConfigObject parse(URL scriptLocation) {
        parse(classLoader.parseClass(scriptLocation.text).newInstance(), scriptLocation)
    }

    /**
     * Parses the passed groovy.lang.Script instance using the second argument to allow the ConfigObject
     * to retain an reference to the original location other Groovy script
     *
     * @param script The groovy.lang.Script instance
     * @param location The original location of the Script as a URL
     * @return The ConfigObject instance
     */
    @SuppressWarnings('Instanceof')
    ConfigObject parse(Script script, URL location) {
        Stack<String> currentConditionalBlock = new Stack<String>()
        def config = location ? new ConfigObject(location) : new ConfigObject()
        GroovySystem.metaClassRegistry.removeMetaClass(script.class)
        def mc = script.class.metaClass
        def prefix = ''
        LinkedList stack = new LinkedList()
        stack << [config: config, scope: [:]]
        def pushStack = { co ->
            stack << [config: co, scope: stack.last.scope.clone()]
        }
        def assignName = { name, co ->
            def current = stack.last
            current.config[name] = co
            current.scope[name] = co
        }
        mc.getProperty = { String name ->
            def current = stack.last
            def result
            if (current.config.get(name)) {
                result = current.config.get(name)
            } else if (current.scope[name]) {
                result = current.scope[name]
            } else {
                try {
                    result = InvokerHelper.getProperty(this, name)
                } catch (GroovyRuntimeException e) {
                    result = new ConfigObject()
                    assignName.call(name, result)
                }
            }
            result
        }

        ConfigObject overrides = new ConfigObject()
        mc.invokeMethod = { String name, args ->
            def result
            if (args.length == 1 && args[0] instanceof Closure) {
                if (name in conditionValues.keySet()) {
                    try {
                        currentConditionalBlock.push(name)
                        conditionalBlocks.push([:])
                        args[0].call()
                    } finally {
                        currentConditionalBlock.pop()
                        for (entry in conditionalBlocks.pop().entrySet()) {
                            def c = stack.last.config
                            (c != config? c : overrides).merge(entry.value)
                        }
                    }
                } else if (currentConditionalBlock.size() > 0) {
                    String conditionalBlockKey = currentConditionalBlock.peek()
                    if (name == conditionValues[conditionalBlockKey]) {
                        def co = new ConfigObject()
                        conditionalBlocks.peek()[conditionalBlockKey] = co

                        pushStack.call(co)
                        try {
                            currentConditionalBlock.pop()
                            args[0].call()
                        } finally {
                            currentConditionalBlock.push(conditionalBlockKey)
                        }
                        stack.removeLast()
                    }
                } else {
                    def co
                    if (stack.last.config.get(name) instanceof ConfigObject) {
                        co = stack.last.config.get(name)
                    } else {
                        co = new ConfigObject()
                    }

                    assignName.call(name, co)
                    pushStack.call(co)
                    args[0].call()
                    stack.removeLast()
                }
            } else if (args.length == 2 && args[1] instanceof Closure) {
                try {
                    prefix = name + '.'
                    assignName.call(name, args[0])
                    args[1].call()
                } finally { prefix = '' }
            } else {
                MetaMethod mm = mc.getMetaMethod(name, args)
                if (mm) {
                    result = mm.invoke(delegate, args)
                } else {
                    throw new MissingMethodException(name, getClass(), args)
                }
            }
            result
        }
        script.metaClass = mc

        def setProperty = { String name, value ->
            assignName.call(prefix + name, value)
        }
        def binding = new ConfigBinding(setProperty)
        if (this.bindingVars) {
            binding.variables.putAll(this.bindingVars)
        }
        script.binding = binding

        script.run()

        config.merge(overrides)

        config
    }
}

/**
 * Since Groovy Script doesn't support overriding setProperty, we use a trick with the Binding to provide this
 * functionality
 */
class ConfigBinding extends Binding {
    def callable
    ConfigBinding(Closure c) {
        this.callable = c
    }

    void setVariable(String name, Object value) {
        callable(name, value)
    }
}
