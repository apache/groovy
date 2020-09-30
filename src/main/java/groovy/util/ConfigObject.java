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

import groovy.lang.GroovyObjectSupport;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.Writable;
import org.apache.groovy.io.StringBuilderWriter;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.codehaus.groovy.syntax.Types;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * A ConfigObject at a simple level is a Map that creates configuration entries (other ConfigObjects) when referencing them.
 * This means that navigating to foo.bar.stuff will not return null but nested ConfigObjects which are of course empty maps
 * The Groovy truth can be used to check for the existence of "real" entries.
 *
 * @since 1.5
 */
public class ConfigObject extends GroovyObjectSupport implements Writable, Map, Cloneable {

    static final Collection<String> KEYWORDS = Types.getKeywords();

    static final String TAB_CHARACTER = "\t";

    /**
     * The config file that was used when parsing this ConfigObject
     */
    private URL configFile;

    private HashMap delegateMap = new LinkedHashMap();

    public ConfigObject(URL file) {
        this.configFile = file;
    }

    public ConfigObject() {
        this(null);
    }

    public URL getConfigFile() {
        return configFile;
    }

    public void setConfigFile(URL configFile) {
        this.configFile = configFile;
    }

    /**
     * Writes this config object into a String serialized representation which can later be parsed back using the parse()
     * method
     *
     * @see groovy.lang.Writable#writeTo(java.io.Writer)
     */
    @Override
    public Writer writeTo(Writer outArg) throws IOException {
        BufferedWriter out = new BufferedWriter(outArg);
        try {
            writeConfig("", this, out, 0, false);
        } finally {
            out.flush();
        }

        return outArg;
    }


    /**
     * Overrides the default getProperty implementation to create nested ConfigObject instances on demand
     * for non-existent keys
     */
    @Override
    public Object getProperty(String name) {
        if ("configFile".equals(name))
            return this.configFile;

        if (!containsKey(name)) {
            ConfigObject prop = new ConfigObject(this.configFile);
            put(name, prop);

            return prop;
        }

        return get(name);
    }

    /**
     * A ConfigObject is a tree structure consisting of nested maps. This flattens the maps into
     * a single level structure like a properties file
     */
    public Map flatten() {
        return flatten(null);
    }

    /**
     * Flattens this ConfigObject populating the results into the target Map
     *
     * @see ConfigObject#flatten()
     */
    public Map flatten(Map target) {
        if (target == null)
            target = new ConfigObject();
        populate("", target, this);

        return target;
    }

    /**
     * Merges the given map with this ConfigObject overriding any matching configuration entries in this ConfigObject
     *
     * @param other The ConfigObject to merge with
     * @return The result of the merge
     */
    public Map merge(ConfigObject other) {
        return doMerge(this, other);
    }


    /**
     * Converts this ConfigObject into a the java.util.Properties format, flattening the tree structure beforehand
     *
     * @return A java.util.Properties instance
     */
    public Properties toProperties() {
        Properties props = new Properties();
        flatten(props);

        props = convertValuesToString(props);

        return props;
    }

    /**
     * Converts this ConfigObject ino the java.util.Properties format, flatten the tree and prefixing all entries with the given prefix
     *
     * @param prefix The prefix to append before property entries
     * @return A java.util.Properties instance
     */
    public Properties toProperties(String prefix) {
        Properties props = new Properties();
        populate(prefix + ".", props, this);

        props = convertValuesToString(props);

        return props;
    }

    private Map doMerge(Map config, Map other) {
        for (Object o : other.entrySet()) {
            Map.Entry next = (Map.Entry) o;
            Object key = next.getKey();
            Object value = next.getValue();

            Object configEntry = config.get(key);

            if (configEntry == null) {
                config.put(key, value);

            } else {
                if (configEntry instanceof Map && !((Map) configEntry).isEmpty() && value instanceof Map) {
                    // recur
                    doMerge((Map) configEntry, (Map) value);
                } else {
                    config.put(key, value);
                }
            }
        }

        return config;
    }

    private void writeConfig(String prefix, ConfigObject map, BufferedWriter out, int tab, boolean apply) throws IOException {
        String space = apply ? StringGroovyMethods.multiply(TAB_CHARACTER, tab) : "";

        for (Object o1 : map.keySet()) {
            String key = (String) o1;
            Object v = map.get(key);

            if (v instanceof ConfigObject) {
                ConfigObject value = (ConfigObject) v;

                if (!value.isEmpty()) {

                    Object dotsInKeys = null;
                    for (Object o : value.entrySet()) {
                        Entry e = (Entry) o;
                        String k = (String) e.getKey();
                        if (k.indexOf('.') > -1) {
                            dotsInKeys = e;
                            break;
                        }
                    }

                    int configSize = value.size();
                    Object firstKey = value.keySet().iterator().next();
                    Object firstValue = value.values().iterator().next();

                    int firstSize;
                    if (firstValue instanceof ConfigObject) {
                        firstSize = ((ConfigObject) firstValue).size();
                    } else {
                        firstSize = 1;
                    }

                    if (configSize == 1 || DefaultGroovyMethods.asBoolean(dotsInKeys)) {
                        if (firstSize == 1 && firstValue instanceof ConfigObject) {
                            key = KEYWORDS.contains(key) ? InvokerHelper.inspect(key) : key;
                            String writePrefix = prefix + key + "." + firstKey + ".";
                            writeConfig(writePrefix, (ConfigObject) firstValue, out, tab, true);
                        } else if (!DefaultGroovyMethods.asBoolean(dotsInKeys) && firstValue instanceof ConfigObject) {
                            writeNode(key, space, tab, value, out);
                        } else {
                            for (Object j : value.keySet()) {
                                Object v2 = value.get(j);
                                Object k2 = ((String) j).indexOf('.') > -1 ? InvokerHelper.inspect(j) : j;
                                if (v2 instanceof ConfigObject) {
                                    key = KEYWORDS.contains(key) ? InvokerHelper.inspect(key) : key;
                                    writeConfig(prefix + key, (ConfigObject) v2, out, tab, false);
                                } else {
                                    writeValue(key + "." + k2, space, prefix, v2, out);
                                }
                            }
                        }
                    } else {
                        writeNode(key, space, tab, value, out);
                    }
                }
            } else {
                writeValue(key, space, prefix, v, out);
            }
        }
    }

    private static void writeValue(String key, String space, String prefix, Object value, BufferedWriter out) throws IOException {
//        key = key.indexOf('.') > -1 ? InvokerHelper.inspect(key) : key;
        boolean isKeyword = KEYWORDS.contains(key);
        key = isKeyword ? InvokerHelper.inspect(key) : key;

        if (!StringGroovyMethods.asBoolean(prefix) && isKeyword) prefix = "this.";
        out.append(space).append(prefix).append(key).append('=').append(InvokerHelper.inspect(value));
        out.newLine();
    }

    private void writeNode(String key, String space, int tab, ConfigObject value, BufferedWriter out) throws IOException {
        key = KEYWORDS.contains(key) ? InvokerHelper.inspect(key) : key;
        out.append(space).append(key).append(" {");
        out.newLine();
        writeConfig("", value, out, tab + 1, true);
        out.append(space).append('}');
        out.newLine();
    }

    private static Properties convertValuesToString(Map props) {
        Properties newProps = new Properties();

        for (Object o : props.entrySet()) {
            Map.Entry next = (Map.Entry) o;
            Object key = next.getKey();
            Object value = next.getValue();

            newProps.put(key, value != null ? value.toString() : null);
        }

        return newProps;
    }

    private void populate(String suffix, Map config, Map map) {
        for (Object o : map.entrySet()) {
            Map.Entry next = (Map.Entry) o;
            Object key = next.getKey();
            Object value = next.getValue();

            if (value instanceof Map) {
                populate(suffix + key + ".", config, (Map) value);
            } else {
                try {
                    config.put(suffix + key, value);
                } catch (NullPointerException e) {
                    // it is idiotic story but if config map doesn't allow null values (like Hashtable)
                    // we can't do too much
                }
            }
        }
    }

    @Override
    public int size() {
        return delegateMap.size();
    }

    @Override
    public boolean isEmpty() {
        return delegateMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegateMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegateMap.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return delegateMap.get(key);
    }

    @Override
    public Object put(Object key, Object value) {
        return delegateMap.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return delegateMap.remove(key);
    }

    @Override
    public void putAll(Map m) {
        delegateMap.putAll(m);
    }

    @Override
    public void clear() {
        delegateMap.clear();
    }

    @Override
    public Set keySet() {
        return delegateMap.keySet();
    }

    @Override
    public Collection values() {
        return delegateMap.values();
    }

    @Override
    public Set entrySet() {
        return delegateMap.entrySet();
    }

    /**
     * Returns a shallow copy of this ConfigObject, keys and configuration entries are not cloned.
     * @return a shallow copy of this ConfigObject
     */
    @Override
    public ConfigObject clone() {
        try {
            ConfigObject clone = (ConfigObject) super.clone();
            clone.configFile = configFile;
            clone.delegateMap = (LinkedHashMap) delegateMap.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    /**
     * Checks if a config option is set. Example usage:
     * <pre class="groovyTestCase">
     * def config = new ConfigSlurper().parse("foo { password='' }")
     * assert config.foo.isSet('password')
     * assert config.foo.isSet('username') == false
     * </pre>
     *
     * The check works <b>only</v> for options <b>one</b> block below the current block.
     * E.g. <code>config.isSet('foo.password')</code> will always return false.
     *
     * @param option The name of the option
     * @return <code>true</code> if the option is set <code>false</code> otherwise
     * @since 2.3.0
     */
    public Boolean isSet(String option) {
        if (delegateMap.containsKey(option)) {
            Object entry = delegateMap.get(option);
            if (!(entry instanceof ConfigObject) || !((ConfigObject) entry).isEmpty()) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public String prettyPrint() {
        Writer sw = new StringBuilderWriter();
        try {
            writeTo(sw);
        } catch (IOException e) {
            throw new GroovyRuntimeException(e);
        }

        return sw.toString();
    }

    @Override
    public String toString() {
        Writer sw = new StringBuilderWriter();
        try {
            InvokerHelper.write(sw, this);
        } catch (IOException e) {
            throw new GroovyRuntimeException(e);
        }

        return sw.toString();
    }
}
