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
package gdk

import groovy.test.GroovyTestCase

class ConfigSlurperTest extends GroovyTestCase {
    void testWithArbitraryTypes() {
        // tag::arbitrary_types[]
        def config = new ConfigSlurper().parse('''
            app.date = new Date()  // <1>
            app.age  = 42
            app {                  // <2>
                name = "Test${42}"
            }
        ''')

        assert config.app.date instanceof Date
        assert config.app.age == 42
        assert config.app.name == 'Test42'
        // end::arbitrary_types[]
    }

    void testConfigSlurperMustNotReturnNull() {
        // tag::never_null[]
        def config = new ConfigSlurper().parse('''
            app.date = new Date()
            app.age  = 42
            app.name = "Test${42}"
        ''')

        assert config.test != null   // <1>
        // end::never_null[]
    }

    void testEscapeDot() {
        // tag::escape_dot[]
        def config = new ConfigSlurper().parse('''
            app."person.age"  = 42
        ''')

        assert config.app."person.age" == 42
        // end::escape_dot[]
    }

    void testEnvironments() {
        // tag::environments[]
        def config = new ConfigSlurper('development').parse('''
          environments {
               development {
                   app.port = 8080
               }

               test {
                   app.port = 8082
               }

               production {
                   app.port = 80
               }
          }
        ''')

        assert config.app.port == 8080
        // end::environments[]
    }

    void testCustomEnvironment() {
        // tag::custom_environments[]
        def slurper = new ConfigSlurper()
        slurper.registerConditionalBlock('myProject', 'developers')   // <1>

        def config = slurper.parse('''
          sendMail = true

          myProject {
               developers {
                   sendMail = false
               }
          }
        ''')

        assert !config.sendMail
        // end::custom_environments[]
    }

    void testProperties() {
        // tag::properties[]
        def config = new ConfigSlurper().parse('''
            app.date = new Date()
            app.age  = 42
            app {
                name = "Test${42}"
            }
        ''')

        def properties = config.toProperties()

        assert properties."app.date" instanceof String
        assert properties."app.age" == '42'
        assert properties."app.name" == 'Test42'
        // end::properties[]
    }
}
