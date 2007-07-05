/* Copyright 2006-2007 Graeme Rocher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.util

import java.beans.Introspector
import java.beans.BeanInfo

/**
* <p>
* ConfigSlurper is a utility class for reading configuration files defined in the form of Groovy
* scripts. Configuration settings can be defined using dot notation or scoped using closures
*
* <pre><code>
*   grails.webflow.stateless = true
*    smtp {
*        mail.host = 'smtp.myisp.com'
*        mail.auth.user = 'server'
*    }
*    resources.URL = "http://localhost:80/resources"
* </pre></code>
*
* <p>Settings can either be bound into nested maps or onto a specified JavaBean instance. In the case
* of the latter an error will be thrown if a property cannot be bound.
*
* @author Graeme Rocher
* @since 1.1
*
*        <p/>
*        Created: Jun 19, 2007
*        Time: 3:53:48 PM
*/

class ConfigSlurper { 

    private static final ENV_METHOD = "environments"
    static final ENV_SETTINGS = '__env_settings__'
    //private BeanInfo bean
    //private instance
    GroovyClassLoader classLoader = new GroovyClassLoader()
    String environment
    private envMode = false

    ConfigSlurper() { }

    /**
     * Constructs a new ConfigSlurper instance using the given environment
     * @param env The Environment to use
     */
    ConfigSlurper(String env) {
        this.environment = env
    }

    /**
     * Parses a ConfigObject instances from an instance of java.util.Properties
     * @param The java.util.Properties instance
     */
    ConfigObject parse(Properties properties) {   
        ConfigObject config = new ConfigObject()
        for(key in properties.keySet()) {
            def tokens = key.split(/\./)
            
            def current = config
            def currentToken
            def last
            def lastToken
            for(token in tokens) {
                last = current
                lastToken = token

                current = current."${token}"
                if(!(current instanceof ConfigObject)) break
            }

            if(current instanceof ConfigObject) {
                if(last[lastToken]) {
                    def flattened = last.flatten()
                    last.clear()
                    flattened.each { k2, v2 -> last[k2] = v2 }
                    last[lastToken] = properties.get(key)
                }
                else {                    
                    last[lastToken] = properties.get(key)
                }
            }
            current = config
        }
        return config
    }
    /**
     * Parse the given script as a string and return the configuration object
     *
     * @see ConfigSlurper#parse(groovy.lang.Script)
     */
    ConfigObject parse(String script) {
        return parse(classLoader.parseClass(script))
    }

    /**
     * Create a new instance of the given script class and parse a configuration object from it
     *
     * @see ConfigSlurper#parse(groovy.lang.Script)
     */
    ConfigObject parse(Class scriptClass) {
        return parse(scriptClass.newInstance())
    }

    /**
     * Parse the given script into a configuration object (a Map)
     * @param script The script to parse
     * @return A Map of maps that can be navigating with dot de-referencing syntax to obtain configuration entries
     */
    ConfigObject parse(Script script) {
         return parse(script, null)
    }

    /**
     * Parses a Script represented by the given URL into a ConfigObject
     *
     * @param scriptLocation The location of the script to parse
     * @return The ConfigObject instance
     */
    ConfigObject parse(URL scriptLocation) {
        return parse(classLoader.parseClass(scriptLocation.text).newInstance(), scriptLocation)
    }

    /**
     * Parses the passed groovy.lang.Script instance using the second argument to allow the ConfigObject
     * to retain an reference to the original location other Groovy script
     *
     * @param script The groovy.lang.Script instance
     * @param location The original location of the Script as a URL
     * @return The ConfigObject instance
     */
    ConfigObject parse(Script script, URL location) {
        def config = location ? new ConfigObject(location) : new ConfigObject()
        GroovySystem.metaClassRegistry.removeMetaClass(script.class)
        def mc = script.class.metaClass
        def prefix = ""
        Stack stack = new Stack()
        mc.getProperty = { String name ->
            def result
            def current
            if(stack) current = stack.peek()
            else {
                current = config
            }

            if(current[name]) {
                result = current[name]
            }
            else {
                result = new ConfigObject()
                current[name] = result
            }
            return result
        }
        mc.invokeMethod = { String name, args ->
            def result
            if(args.length == 1 && args[0] instanceof Closure) {
                if(name == ENV_METHOD) {
                    try {
                        envMode = true
                        args[0].call()
                    }
                    finally {
                        envMode = false
                    }
                }
                else if(envMode) {
                    if(name == environment) {
                        def co = new ConfigObject()
                        config[ENV_SETTINGS] = co

                        stack.push(co)
                        try {
                            envMode = false
                            args[0].call()
                        } finally {
                            envMode = true
                        }
                        stack.pop()
                    }
                }
                else {
                    def co = new ConfigObject()
                    if(stack) {
                        stack.peek()[name] = co
                    }
                    else {
                        config[name] = co
                    }
                    stack.push(co)
                    args[0].call()
                    stack.pop()
                }
            }
            else if(args.length == 2 && args[1] instanceof Closure) {
                try {
                   prefix = name +'.'
                    def conf = stack ? stack.peek() : config
                    conf[name] = args[0]
                    args[1].call()
                }  finally { prefix = "" }
            }
            else {
                MetaMethod mm = mc.getMetaMethod(name, args)
                if(mm)result = mm.invoke(delegate, args)
                else {
                    throw new MissingMethodException(name, getClass(), args)
                }
            }
            result
        }
        script.metaClass = mc

        def setProperty = { String name, value ->
            def current
            if(stack) current = stack.peek()
            else {
                current = config
            }
            current[prefix+name] = value
        }                     
        script.binding = new ConfigBinding(setProperty)


        script.run()

        def envSettings = config.remove(ENV_SETTINGS)
        if(envSettings) {
            config.merge(envSettings)
        }

        return config
    }

}
/**
 * Since Groovy Script don't support overriding setProperty, we have to using a trick with the Binding to provide this
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