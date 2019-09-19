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
package org.codehaus.groovy.classgen

import groovy.test.GroovyTestCase

class MetaClassTest extends GroovyTestCase {

    void testMetaClass() {
        test(this)
        test { print(it) }
    }

    protected def test(object) {
        def metaClass = object.metaClass
        assert metaClass != null

        println(metaClass)

        def classNode = metaClass.getClassNode()
        assert classNode != null

        println(classNode)

        def name = object.getClass().getName()
        assert classNode.name == name
    }

    void testMetaClassDefinition() {
        assertScript """
            class Foo {
                MetaClass metaClass
            } 
            def foo = new Foo()
            assert foo.@metaClass != null
            """
    }
}
