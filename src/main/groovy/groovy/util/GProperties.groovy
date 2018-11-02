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
 * 3) Evaluating with ${...}, e.g.
 * <pre>
 *      # gproperties.properties
 *      greeting.daniel=Hello,Daniel in ${new java.text.SimpleDateFormat('yyyyMMdd').format(new Date())}
 *
 *      // groovy script
 *      def gp = new GProperties(true) // Note: we should enable evaluating script manually
 *      gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))
 *      assert 'Hello,Daniel in 2018' == gp.getProperty('greeting.daniel') // Given running the script in 2018
 * </pre>
 *
 * 4) Escaping with {{...}}, ${{...}}, e.g.
 * <pre>
 *      # gproperties.properties
 *      greeting.daniel={{groovy.greeting}},{{some.name}} in ${{new java.text.SimpleDateFormat('yyyyMMdd').format(new Date())}}
 *
 *      // groovy script
 *      def gp = new GProperties(true)
 *      gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))
 *      assert '{groovy.greeting},{some.name} in ${new java.text.SimpleDateFormat('yyyyMMdd').format(new Date())}' == gp.getProperty('greeting.daniel')
 * </pre>
 *
 * @since 3.0.0
 */
@CompileStatic
class GProperties extends Properties {
    public static final String IMPORT_PROPERTIES_KEY = 'import.properties'
    public static final String VAR_KEY = 'key'
    public static final String VAR_PROPERTIES = 'properties'
    private static final Pattern GROOVY_SCRIPT_PATTERN = Pattern.compile(/[$][{]\s*(.+?)\s*[}]/)
    private static final Pattern INTERPOLATE_PATTERN = Pattern.compile(/[{]\s*(.+?)\s*[}]/)
    private static final Pattern ESCAPE_PATTERN = Pattern.compile(/[{]([{].+?[}])[}]/)
    private static final String LEFT_CURLY_BRACE = '{'
    private static final String RIGHT_CURLY_BRACE = '}'
    private static final String COMMA = ','
    private final boolean evaluateScriptEnabled
    private final GroovyShell groovyShell
    private final List<GProperties> importPropertiesList = new ArrayList<>()

    GProperties(boolean evaluateScriptEnabled=false, GroovyShell groovyShell=new GroovyShell()) {
        this(null, evaluateScriptEnabled, groovyShell)
    }

    GProperties(Properties defaults, boolean evaluateScriptEnabled=false, GroovyShell groovyShell=new GroovyShell()) {
        super(defaults)
        this.evaluateScriptEnabled = evaluateScriptEnabled
        this.groovyShell = groovyShell
    }

    @Override
    String getProperty(String key) {
        String value = super.getProperty(key)

        if (!value) {
            for (GProperties importProperties : importPropertiesList) {
                value = importProperties.getProperty(key)

                if (value) {
                    break
                }
            }
        }

        if (!value) {
            return value
        }

        if (evaluateScriptEnabled) {
            value = value.replaceAll(GROOVY_SCRIPT_PATTERN) { String _0, String _1 ->
                if (_1.startsWith(LEFT_CURLY_BRACE) && _1.endsWith(RIGHT_CURLY_BRACE)) {
                    return _0
                }
                processImplicitVariables(key) {
                    groovyShell.evaluate(_1)
                }
            }
        }

        value = value.replaceAll(INTERPOLATE_PATTERN) { String _0, String _1 ->
            if (_1.startsWith(LEFT_CURLY_BRACE) && _1.endsWith(RIGHT_CURLY_BRACE)) {
                return _0
            }

            this.getProperty(_1) ?: _0
        }

        value.replaceAll(ESCAPE_PATTERN) { String _0, String _1 ->
            _1
        }
    }

    @Override
    synchronized void load(Reader reader) throws IOException {
        reader.withReader { it ->
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

    private synchronized Object processImplicitVariables(String key, Closure c) {
        groovyShell.setVariable(VAR_KEY, key)
        groovyShell.setVariable(VAR_PROPERTIES, this)
        def v = c()
        groovyShell.removeVariable(VAR_PROPERTIES)
        groovyShell.removeVariable(VAR_KEY)

        return v
    }
}
