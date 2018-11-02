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

class GPropertiesTest extends GroovyTestCase {
    void testImportProperties() {
        def gp = new GProperties(true)
        gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))

        assert '/groovy/util/gproperties_import.properties,/groovy/util/gproperties_import3.properties' == gp.getProperty('import.properties')
        assert 'Daniel' == gp.getProperty('some.name')
        assert 'Hello' == gp.getProperty('greeting.word')
        assert 'Hi' == gp.getProperty('greeting.word2')
    }

    void testInterpolate() {
        def gp = new GProperties(true)
        gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))

        assert 'Hello,Daniel' == gp.getProperty('groovy.greeting')
    }

    void testInterpolate2() {
        def gp = new GProperties(true)
        gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))

        assert 'Hello,Daniel :)' == gp.getProperty('groovy.greeting.with.smile')
    }

    void testInterpolate3() {
        def gp = new GProperties()
        gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))

        assert 'Hello,{none} {0}' == gp.getProperty('groovy.greeting.with.missing')
    }

    void testInterpolate4() {
        def gp = new GProperties(true)
        gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))

        assert 'Hello,Daniel' == gp.getProperty('greeting.daniel')
    }

    void testInterpolate5() {
        def gp = new GProperties()
        gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))

        assert '''H${'ell'}o,Daniel''' == gp.getProperty('greeting.daniel')
    }

    void testEscape() {
        def gp = new GProperties()
        gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))

        assert 'Hello,{some.name}' == gp.getProperty('groovy.greeting.with.escapes')
    }

    void testEscape2() {
        def gp = new GProperties()
        gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))

        assert '''Hello,${properties.getProperty('some.name')}''' == gp.getProperty('groovy.greeting.with.escapes2')
    }

    void testInterpolateGroovyScript() {
        def gp = new GProperties(true)
        gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))

        assert "Hello,Daniel :) in ${new java.text.SimpleDateFormat('yyyyMMdd').format(new Date())}" == gp.getProperty('groovy.greeting.with.time')
    }

    void testInterpolateGroovyScript2() {
        def gp = new GProperties(true)
        gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))

        assert "Hello,Daniel" == gp.getProperty('groovy.greeting.with.script')
    }

    void testInterpolateGroovyScript3() {
        def gp = new GProperties(true)
        gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))

        assert "Hello,groovy.greeting.with.key" == gp.getProperty('groovy.greeting.with.key')
    }
}
