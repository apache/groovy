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
import org.codehaus.groovy.runtime.FormatHelper;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.codehaus.groovy.syntax.Types;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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

    /** Groovy keywords that require quoting when rendered. */
    static final Collection<String> KEYWORDS = Types.getKeywords();

    /** Single indentation unit used for formatted output. */
    static final String TAB_CHARACTER = "\t";

    /**
     * The config file that was used when parsing this ConfigObject
     */
    private URL configFile;

    private HashMap delegateMap = new LinkedHashMap();

    /**
     * Creates a config object associated with the supplied source.
     *
     * @param file the parsed config resource, or {@code null}
     */
    public ConfigObject(URL file) {
        this.configFile = file;
    }

    /**
     * Creates an empty config object with no associated source.
     */
    public ConfigObject() {
        this(null);
    }

    /**
     * Returns the config resource that produced this object.
     *
     * @return the originating config resource, or {@code null}
     */
    public URL getConfigFile() {
        return configFile;
    }

    /**
     * Updates the config resource associated with this object.
     *
     * @param configFile the originating config resource
     */
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
     * Converts this ConfigObject into the java.util.Properties format, flattening the tree structure beforehand
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

            if (v instanceof ConfigObject value) {

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
                            key = renderKey(key);
                            String writePrefix = prefix + key + "." + renderKey(String.valueOf(firstKey)) + ".";
                            writeConfig(writePrefix, (ConfigObject) firstValue, out, tab, true);
                        } else if (!DefaultGroovyMethods.asBoolean(dotsInKeys) && firstValue instanceof ConfigObject) {
                            writeNode(key, space, tab, value, out);
                        } else {
                            for (Object j : value.keySet()) {
                                Object v2 = value.get(j);
                                Object k2 = renderKey((String) j);
                                if (v2 instanceof ConfigObject) {
                                    key = renderKey(key);
                                    writeConfig(prefix + key, (ConfigObject) v2, out, tab, false);
                                } else {
                                    writeValue(renderKey(key) + "." + k2, space, prefix, v2, out);
                                }
                            }
                        }
                    } else {
                        writeNode(key, space, tab, value, out);
                    }
                }
            } else {
                writeValue(renderKey(key), space, prefix, v, out);
            }
        }
    }

    /**
     * Writes one entry, given a key path whose components have already been rendered.
     *
     * @param keyPath the rendered key path, such as {@code foo} or {@code foo.'a b'}
     */
    private static void writeValue(String keyPath, String space, String prefix, Object value, BufferedWriter out) throws IOException {
        // A quoted key cannot open a statement on its own, so it needs a receiver, exactly as a
        // keyword key has always done. The statement opens with the prefix when there is one.
        String statementStart = StringGroovyMethods.asBoolean(prefix) ? prefix : keyPath;
        if (statementStart.startsWith("'")) prefix = "this." + prefix;
        out.append(space).append(prefix).append(keyPath).append('=').append(renderValue(value));
        out.newLine();
    }

    private void writeNode(String key, String space, int tab, ConfigObject value, BufferedWriter out) throws IOException {
        out.append(space).append(renderKey(key)).append(" {");
        out.newLine();
        writeConfig("", value, out, tab + 1, true);
        out.append(space).append('}');
        out.newLine();
    }

    /**
     * Renders a key as it must appear in the written configuration: bare when it is a plain
     * identifier, and as a quoted literal otherwise. A key which is not an identifier would
     * otherwise be written as though it were source, and read back as whatever it happened to
     * parse as.
     *
     * @param key the key to render
     * @return the key as it should be written
     */
    private static String renderKey(String key) {
        return isIdentifier(key) ? key : FormatHelper.inspect(key);
    }

    private static boolean isIdentifier(String key) {
        if (key == null || key.isEmpty() || KEYWORDS.contains(key)) return false;
        if (!Character.isJavaIdentifierStart(key.charAt(0))) return false;
        for (int i = 1, n = key.length(); i < n; i += 1) {
            if (!Character.isJavaIdentifierPart(key.charAt(i))) return false;
        }
        return true;
    }

    /**
     * Renders a value as a literal which reads back as the same data.
     *
     * @param value the value to render
     * @return the value as it should be written
     */
    private static String renderValue(Object value) {
        return FormatHelper.inspect(asWritableData(value));
    }

    /**
     * Converts a value into something {@link FormatHelper#inspect} renders as inert data.
     * <p>
     * A {@link CharSequence} which is not a {@code String} is rendered as a double quoted
     * literal, in which a dollar is live, so its text is carried over to a {@code String} and
     * rendered single quoted instead. An array has no literal form, so it is carried over to a
     * {@code List}, element by element, and reads back as one. A value of any other type
     * without a literal form would be written as a bare {@code toString()}, which is not data
     * at all, so its text is carried over in the same way. Numbers and booleans already write
     * as themselves.
     *
     * @param value the value to convert
     * @return a value whose rendering is data
     */
    private static Object asWritableData(Object value) {
        if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean) {
            return value;
        }
        if (value instanceof CharSequence) {
            return value.toString();
        }
        if (value instanceof Map<?, ?> map) {
            Map<Object, Object> converted = new LinkedHashMap<>(map.size());
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                converted.put(asWritableData(entry.getKey()), asWritableData(entry.getValue()));
            }
            return converted;
        }
        if (value instanceof Collection<?> collection) {
            List<Object> converted = new ArrayList<>(collection.size());
            for (Object element : collection) {
                converted.add(asWritableData(element));
            }
            return converted;
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            List<Object> converted = new ArrayList<>(length);
            for (int i = 0; i < length; i += 1) {
                converted.add(asWritableData(Array.get(value, i)));
            }
            return converted;
        }
        return value.toString();
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

    /** {@inheritDoc} */
    @Override
    public int size() {
        return delegateMap.size();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return delegateMap.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsKey(Object key) {
        return delegateMap.containsKey(key);
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsValue(Object value) {
        return delegateMap.containsValue(value);
    }

    /** {@inheritDoc} */
    @Override
    public Object get(Object key) {
        return delegateMap.get(key);
    }

    /** {@inheritDoc} */
    @Override
    public Object put(Object key, Object value) {
        return delegateMap.put(key, value);
    }

    /** {@inheritDoc} */
    @Override
    public Object remove(Object key) {
        return delegateMap.remove(key);
    }

    /** {@inheritDoc} */
    @Override
    public void putAll(Map m) {
        delegateMap.putAll(m);
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        delegateMap.clear();
    }

    /** {@inheritDoc} */
    @Override
    public Set keySet() {
        return delegateMap.keySet();
    }

    /** {@inheritDoc} */
    @Override
    public Collection values() {
        return delegateMap.values();
    }

    /** {@inheritDoc} */
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
     * <pre class="language-groovy groovyTestCase">
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

    /**
     * Renders this config object using config-script syntax.
     *
     * @return a formatted config representation
     */
    public String prettyPrint() {
        Writer sw = new StringBuilderWriter();
        try {
            writeTo(sw);
        } catch (IOException e) {
            throw new GroovyRuntimeException(e);
        }

        return sw.toString();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        Writer sw = new StringBuilderWriter();
        try {
            FormatHelper.write(sw, this);
        } catch (IOException e) {
            throw new GroovyRuntimeException(e);
        }

        return sw.toString();
    }
}
