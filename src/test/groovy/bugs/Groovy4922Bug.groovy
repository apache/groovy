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
package groovy.bugs

import groovy.test.GroovyTestCase

class Groovy4922Bug extends GroovyTestCase {
    void testShouldNotThrowStackOverflow() {
        assertScript """
            package groovy.bugs

            import groovy.transform.PackageScope

            public class Groovy4922BugSupport {
                protected String support;
                @PackageScope
                void someMethod(String value) {
                    support = value;
                }
            }
            class Groovy4922BugChild extends Groovy4922BugSupport {
                void someMethod(String parameter) {
                    super.someMethod(parameter)
                }
            }
            def child = new Groovy4922BugChild()
            child.someMethod("value")
            assert child.support == "value"
        """
    }
}
