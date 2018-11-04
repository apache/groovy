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

import groovy.transform.CompileStatic

import java.util.regex.Pattern

/**
 * Represents an enhanced properties, which supports interpolating in the values and importing other properties in classpath
 *
 * Usage:
 * 1) Interpolating with {...}, e.g.
 * <pre>
 *      # gproperties.properties
 *      groovy.greeting=Hello
 *      some.name=Daniel
 *      greeting.daniel={groovy.greeting},{some.name}
 *
 *      // groovy script
 *      def gp = new GProperties()
 *      gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))
 *      assert 'Hello,Daniel' == gp.getProperty('greeting.daniel')
 * </pre>
 *
 * 2) Importing with import.properties, e.g.
 * <pre>
 *      # gproperties.properties
 *      import.properties=/groovy/util/gproperties_import.properties,/groovy/util/gproperties_import2.properties
 *      greeting.daniel={groovy.greeting},{some.name}
 *
 *      # gproperties_import.properties
 *      groovy.greeting=Hello
 *
 *      # gproperties_import2.properties
 *      some.name=Daniel
 *
 *      // groovy script
 *      def gp = new GProperties()
 *      gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))
 *      assert 'Hello,Daniel' == gp.getProperty('greeting.daniel')
 * </pre>
 *
 * 3) Escaping with {{...}}, e.g.
 * <pre>
 *      # gproperties.properties
 *      greeting.daniel={{groovy.greeting}},{{some.name}}
 *
 *      // groovy script
 *      def gp = new GProperties()
 *      gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))
 *      assert '{groovy.greeting},{some.name}' == gp.getProperty('greeting.daniel')
 * </pre>
 *
 * 4) Getting property with the specified type(integer, long, boolean, etc.), e.g.
 * <pre>
 *      # gproperties.properties
 *      property.integer=1104
 *
 *      // groovy script
 *      def gp = new GProperties()
 *      gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))
 *      assert new Integer(1104) == gp.getInteger('property.integer')
 * </pre>
 *
 * @since 3.0.0
 */
@CompileStatic
class GProperties extends Properties {
    private static final long serialVersionUID = 6112578636029876860L
    public static final String IMPORT_PROPERTIES_KEY = 'import.properties'
    private static final Pattern INTERPOLATE_PATTERN = Pattern.compile(/[{](.+?)[}]/)
    private static final Pattern ESCAPE_PATTERN = Pattern.compile(/[{]([{].+?[}])[}]/)
    private static final String LEFT_CURLY_BRACE = '{'
    private static final String RIGHT_CURLY_BRACE = '}'
    private static final String COMMA = ','
    private final List<GProperties> importPropertiesList = new LinkedList<>()

    GProperties() {
        this((Properties) null)
    }

    GProperties(Reader reader) throws IOException {
        this()
        load(reader)
    }

    GProperties(CharSequence content) {
        this()
        load(new StringReader(content.toString()))
    }

    GProperties(InputStream inStream) throws IOException {
        this()
        load(inStream)
    }

    GProperties(Properties defaults) {
        super(defaults)
    }

    GProperties(Properties defaults, Reader reader) throws IOException {
        this(defaults)
        load(reader)
    }

    GProperties(Properties defaults, CharSequence content) {
        this(defaults)
        load(new StringReader(content.toString()))
    }

    GProperties(Properties defaults, InputStream inStream) throws IOException {
        this(defaults)
        load(inStream)
    }

    @Override
    String getProperty(String key) {
        String value = super.getProperty(key)

        if (null == value) {
            for (GProperties importProperties : importPropertiesList) {
                value = importProperties.getProperty(key)

                if (null != value) {
                    break
                }
            }
        }

        if (null == value) {
            return value
        }

        value = value.replaceAll(INTERPOLATE_PATTERN) { String _0, String _1 ->
            if (_1.startsWith(LEFT_CURLY_BRACE) && _1.endsWith(RIGHT_CURLY_BRACE)) {
                return _0
            }

            def p = this.getProperty(_1.trim())
            null == p ? _0 : p
        }

        value.replaceAll(ESCAPE_PATTERN) { String _0, String _1 ->
            _1
        }
    }

    Character getCharacter(String key) {
        getPropertyWithType(key) { String p ->
            if (!p || p.length() > 1) {
                throw new IllegalArgumentException("Invalid character: ${p}")
            }

            Character.valueOf(p as char)
        }
    }

    Character getCharacter(String key, Character defaultValue) {
        getDefaultIfAbsent(key, defaultValue) {
            getCharacter(key)
        }
    }

    Byte getByte(String key) {
        getPropertyWithType(key) { String p ->
            Byte.valueOf(p)
        }
    }

    Byte getByte(String key, Byte defaultValue) {
        getDefaultIfAbsent(key, defaultValue) {
            getByte(key)
        }
    }

    Short getShort(String key) {
        getPropertyWithType(key) { String p ->
            Short.valueOf(p)
        }
    }

    Short getShort(String key, Short defaultValue) {
        getDefaultIfAbsent(key, defaultValue) {
            getShort(key)
        }
    }

    Integer getInteger(String key) {
        getPropertyWithType(key) { String p ->
            Integer.valueOf(p)
        }
    }

    Integer getInteger(String key, Integer defaultValue) {
        getDefaultIfAbsent(key, defaultValue) {
            getInteger(key)
        }
    }

    Long getLong(String key) {
        getPropertyWithType(key) { String p ->
            Long.valueOf(p)
        }
    }

    Long getLong(String key, Long defaultValue) {
        getDefaultIfAbsent(key, defaultValue) {
            getLong(key)
        }
    }

    Float getFloat(String key) {
        getPropertyWithType(key) { String p ->
            Float.valueOf(p)
        }
    }

    Float getFloat(String key, Float defaultValue) {
        getDefaultIfAbsent(key, defaultValue) {
            getFloat(key)
        }
    }

    Double getDouble(String key) {
        getPropertyWithType(key) { String p ->
            Double.valueOf(p)
        }
    }

    Double getDouble(String key, Double defaultValue) {
        getDefaultIfAbsent(key, defaultValue) {
            getDouble(key)
        }
    }

    Boolean getBoolean(String key) {
        getPropertyWithType(key) { String p ->
            Boolean.valueOf(p)
        }
    }

    Boolean getBoolean(String key, Boolean defaultValue) {
        getDefaultIfAbsent(key, defaultValue) {
            getBoolean(key)
        }
    }

    BigInteger getBigInteger(String key) {
        getPropertyWithType(key) { String p ->
            new BigInteger(p)
        }
    }

    BigInteger getBigInteger(String key, BigInteger defaultValue) {
        getDefaultIfAbsent(key, defaultValue) {
            getBigInteger(key)
        }
    }

    BigDecimal getBigDecimal(String key) {
        getPropertyWithType(key) { String p ->
            new BigDecimal(p)
        }
    }

    BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
        getDefaultIfAbsent(key, defaultValue) {
            getBigDecimal(key)
        }
    }

    @Override
    synchronized void load(Reader reader) throws IOException {
        reader.withReader {
            super.load(it)
            importProperties()
        }
    }

    @Override
    synchronized void load(InputStream inStream) throws IOException {
        inStream.withStream {
            super.load(it)
            importProperties()
        }
    }

    private void importProperties() {
        String importPropertiesPaths = super.getProperty(IMPORT_PROPERTIES_KEY)

        if (!importPropertiesPaths?.trim()) {
            return
        }

        importPropertiesPaths.split(COMMA).collect { it.trim() }.each { String importPropertiesPath ->
            if (!importPropertiesPath) {
                return
            }

            GProperties importProperties = new GProperties()
            def inputstream = GProperties.getResourceAsStream(importPropertiesPath)

            if (!inputstream) {
                throw new IOException("${importPropertiesPath} does not exist")
            }

            inputstream.withStream {
                importProperties.load(it)
            }

            importPropertiesList << importProperties
        }
    }

    private <V> V getPropertyWithType(String key, Closure<V> c) {
        def p = getProperty(key)
        null == p ? null : c(p)
    }

    private <V> V getDefaultIfAbsent(String key, V defaultValue, Closure<V> c) {
        def p = c(key)
        null == p ? defaultValue : p
    }
}
