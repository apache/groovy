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
package org.codehaus.groovy.transform

import groovy.test.GroovyShellTestCase

class ExternalizeMethodsTransformTest extends GroovyShellTestCase {

    void testOk() {
        assertScript """
            import groovy.transform.ExternalizeMethods
            @ExternalizeMethods
            class Person {
              String first, last
              List favItems
              Date since
            }

            def p = new Person(first: "John", last: "Doe", favItems: ["one", "two"], since: Date.parse("yyyy-MM-dd", "2014-12-28"))

            def baos = new ByteArrayOutputStream()
            p.writeExternal(new ObjectOutputStream(baos))

            def p2 = new Person()
            p2.readExternal(new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())))

            assert p2.first == "John"
            assert p2.last == "Doe"
            assert p2.favItems == ["one", "two"]
            assert p2.since == Date.parse("yyyy-MM-dd", "2014-12-28")
        """
    }

    void testExcludesWithInvalidPropertyNameResultsInError() {
        def message = shouldFail {
            evaluate """
                import groovy.transform.ExternalizeMethods

                @ExternalizeMethods(excludes='sirName')
                class Person {
                    String firstName
                    String surName
                }

                new Person(firstName: "John", surName: "Doe")
            """
        }
        assert message.contains("Error during @ExternalizeMethods processing: 'excludes' property 'sirName' does not exist.")
    }

}