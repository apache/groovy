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
        def gp = new GProperties()
        gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))

        assert '/groovy/util/gproperties_import.properties,/groovy/util/gproperties_import3.properties' == gp.getProperty('import.properties')
        assert 'Daniel' == gp.getProperty('some.name')
        assert 'Hello' == gp.getProperty('greeting.word')
        assert 'Hi' == gp.getProperty('greeting.word2')
    }

    void testInterpolate() {
        def gp = new GProperties()
        gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))

        assert 'Hello,Daniel' == gp.getProperty('groovy.greeting')
    }

    void testInterpolate2() {
        def gp = new GProperties()
        gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))

        assert 'Hello,Daniel :)' == gp.getProperty('groovy.greeting.with.smile')
    }

    void testInterpolate3() {
        def gp = new GProperties()
        gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))

        assert 'Hello,{none} {0}' == gp.getProperty('groovy.greeting.with.missing')
    }

    void testInterpolate4() {
        def gp = new GProperties()
        gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))

        assert 'Hello,Daniel' == gp.getProperty('greeting.daniel')
    }

    void testInterpolate5() {
        def gp = new GProperties()
        gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))

        assert '''Hello,Daniel''' == gp.getProperty('greeting.daniel')
    }

    void testEscape() {
        def gp = new GProperties()
        gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))

        assert 'Hello,{some.name}' == gp.getProperty('groovy.greeting.with.escapes')
    }

    void testGetCharacter() {
        def gp = new GProperties()
        gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))

        assert new Character('a' as char) == gp.getCharacter('property.character')
        assert null == gp.getCharacter('property.character.missing')

        try {
            gp.getCharacter('property.character.invalid')
            assert false
        } catch (IllegalArgumentException e) {
            assert e.message.contains('Invalid character')
        }
    }

    void testGetCharacterWithDefault() {
        def gp = new GProperties()
        gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))

        assert new Character('a' as char) == gp.getCharacter('property.character', 'b' as char)
        assert new Character('b' as char) == gp.getCharacter('property.character.missing', 'b' as char)
    }

    void testGetShort() {
        def gp = new GProperties()
        gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))

        assert new Short(126 as short) == gp.getShort('property.short')
        assert null == gp.getShort('property.short.missing')
    }

    void testGetShortWithDefault() {
        def gp = new GProperties()
        gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))

        assert new Short(126 as short) == gp.getShort('property.short', 125 as short)
        assert new Short(125 as short) == gp.getShort('property.short.missing', 125 as short)
    }

    void testGetInteger() {
        def gp = new GProperties()
        gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))

        assert new Integer(1104) == gp.getInteger('property.integer')
        assert null == gp.getInteger('property.integer.missing')
    }

    void testGetIntegerWithDefault() {
        def gp = new GProperties()
        gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))

        assert new Integer(1104) == gp.getInteger('property.integer', 1101)
        assert new Integer(1101) == gp.getInteger('property.integer.missing', 1101)
    }

    void testGetLong() {
        def gp = new GProperties()
        gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))

        assert new Long(181104) == gp.getLong('property.long')
        assert null == gp.getLong('property.long.missing')
    }

    void testGetLongWithDefault() {
        def gp = new GProperties()
        gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))

        assert new Long(181104) == gp.getLong('property.long', 181101)
        assert new Long(181101) == gp.getLong('property.long.missing', 181101)
    }

    void testGetFloat() {
        def gp = new GProperties()
        gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))

        assert new Float(18.1104f) == gp.getFloat('property.float')
        assert null == gp.getFloat('property.float.missing')
    }

    void testGetFloatWithDefault() {
        def gp = new GProperties()
        gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))

        assert new Float(18.1104f) == gp.getFloat('property.float', 18.1101f)
        assert new Float(18.1101f) == gp.getFloat('property.float.missing', 18.1101f)
    }

    void testGetDouble() {
        def gp = new GProperties()
        gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))

        assert new Double(18.1104d) == gp.getDouble('property.double')
        assert null == gp.getDouble('property.double.missing')
    }

    void testGetDoubleWithDefault() {
        def gp = new GProperties()
        gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))

        assert new Double(18.1104d) == gp.getDouble('property.double', 18.1101d)
        assert new Double(18.1101d) == gp.getDouble('property.double.missing', 18.1101d)
    }

    void testGetBoolean() {
        def gp = new GProperties()
        gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))

        assert new Boolean(true) == gp.getBoolean('property.boolean')
        assert null == gp.getBoolean('property.boolean.missing')
    }

    void testGetBooleanWithDefault() {
        def gp = new GProperties()
        gp.load(GPropertiesTest.getResourceAsStream('/groovy/util/gproperties.properties'))

        assert new Boolean(true) == gp.getBoolean('property.boolean', false)
        assert new Boolean(false) == gp.getBoolean('property.boolean.missing', false)
    }
}
