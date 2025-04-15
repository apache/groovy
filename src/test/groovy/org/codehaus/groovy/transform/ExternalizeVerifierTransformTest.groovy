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

class ExternalizeVerifierTransformTest extends GroovyShellTestCase {

    void testCheckPropertyTypes() {
        def message = shouldFail {
            evaluate """
                import groovy.transform.ExternalizeVerifier

                class Foo {}

                @ExternalizeVerifier(checkPropertyTypes=true)
                class Person implements Externalizable {
                    String firstName
                    Foo foo

                    void readExternal(ObjectInput inp) {}
                    void writeExternal(ObjectOutput outp) {}
                }

                new Person()
            """
        }
        assert message.contains("@ExternalizeVerifier: strict type checking is enabled and the non-primitive property (or field) 'foo' in an Externalizable class has the type 'Foo' which isn't Externalizable or Serializable")
    }

    void testExcludes() {
        assertScript """
            import groovy.transform.ExternalizeVerifier

            class Foo {}

            @ExternalizeVerifier(excludes='foo', checkPropertyTypes=true)
            class Person implements Externalizable {
                String firstName
                Foo foo

                void readExternal(ObjectInput inp) {}
                void writeExternal(ObjectOutput outp) {}
            }

            new Person()
        """
    }

    void testExcludesWithInvalidPropertyNameResultsInError() {
        def message = shouldFail {
            evaluate """
                import groovy.transform.ExternalizeVerifier

                @ExternalizeVerifier(excludes='sirName')
                class Person implements Externalizable {
                    String firstName
                    String surName

                    void readExternal(ObjectInput inp) {}
                    void writeExternal(ObjectOutput outp) {}
                }
            """
        }
        assert message.contains("Error during @ExternalizeVerifier processing: 'excludes' property 'sirName' does not exist.")
    }

}
