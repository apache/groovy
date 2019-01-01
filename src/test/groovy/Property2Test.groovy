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
package groovy

/**
 * Tests the use of getMetaPropertyValues() and getProperties() for Beans and Expandos.
 */
class Property2Test extends GroovyTestCase {

    void testGetPropertiesBeanCheckingValues() {
        def foo = new Foo()

        // these are the properties and their values that should be there
        def props = ['name': 'James', 'count': 1, 'location': 'London', 'blah': 9]
        foo.properties.each { name, value ->
            // GROOVY-996 - We should see protected properties, but not  private ones.
            assert name != "invisible"

            def pvalue = props[name]
            if (pvalue != null)
                assert pvalue == value

            // remove this one from the map
            props.remove(name)
        }

        // make sure there are none left over
        assert props.size() == 0
    }

    void testMetaPropertyValuesFromObject() {
        def foo = new Foo()
        def metaProps = foo.metaPropertyValues
        assert metaProps[0] instanceof PropertyValue
        assertNotNull metaProps[0].name
        assertNotNull metaProps[0].value
        assertNotNull metaProps[0].type
    }

    void testGetPropertiesExpando() {
        def foo = new Expando()

        foo.name = 'John'
        foo.location = 'Colorado'
        foo.count = 23
        foo.blah = true

        // these are the properties that should be there
        def props = ['name', 'count', 'location', 'blah']
        foo.properties.each { name, value -> props -= [name] }

        // there should be none left
        assert props.size() == 0
    }
}

