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
package semantics

import groovy.test.GroovyTestCase
import groovy.xml.XmlSlurper

class GPathTest extends GroovyTestCase {

    //tag::gpath_on_reflection_1[]
    void aMethodFoo() { println "This is aMethodFoo." } // <0>
    //end::gpath_on_reflection_1[]

    void testGPathOnReflection() {
        //tag::gpath_on_reflection_2[]
        assert ['aMethodFoo'] == this.class.methods.name.grep(~/.*Foo/)
        //end::gpath_on_reflection_2[]
    }

    //tag::gpath_on_reflection_3[]
    void aMethodBar() { println "This is aMethodBar." } // <1>
    void anotherFooMethod() { println "This is anotherFooMethod." } // <2>
    void aSecondMethodBar() { println "This is aSecondMethodBar." } // <3>
    //end::gpath_on_reflection_3[]

    void testGPathOnReflectionWithBarMethods() {
        //tag::gpath_on_reflection_4[]
        assert ['aMethodBar', 'aSecondMethodBar'] as Set == this.class.methods.name.grep(~/.*Bar/) as Set
        //end::gpath_on_reflection_4[]
    }

    void testGPathArrayAccess() {
        //tag::gpath_array_access_1[]
        assert 'aSecondMethodBar' == this.class.methods.name.grep(~/.*Bar/).sort()[1]
        //end::gpath_array_access_1[]
    }

    void testGPathOnXml() {
        //tag::gpath_on_xml_1[]
        def xmlText = """
                      | <root>
                      |   <level>
                      |      <sublevel id='1'>
                      |        <keyVal>
                      |          <key>mykey</key>
                      |          <value>value 123</value>
                      |        </keyVal>
                      |      </sublevel>
                      |      <sublevel id='2'>
                      |        <keyVal>
                      |          <key>anotherKey</key>
                      |          <value>42</value>
                      |        </keyVal>
                      |        <keyVal>
                      |          <key>mykey</key>
                      |          <value>fizzbuzz</value>
                      |        </keyVal>
                      |      </sublevel>
                      |   </level>
                      | </root>
                      """
        def root = new XmlSlurper().parseText(xmlText.stripMargin())
        assert root.level.size() == 1 // <1>
        assert root.level.sublevel.size() == 2 // <2>
        assert root.level.sublevel.findAll { it.@id == 1 }.size() == 1 // <3>
        assert root.level.sublevel[1].keyVal[0].key.text() == 'anotherKey' // <4>
        //end::gpath_on_xml_1[]
    }

}
