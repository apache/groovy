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
package org.apache.groovy.contracts.compability

import groovy.test.GroovyShellTestCase

class SynchronizedTests extends GroovyShellTestCase {

    void test_Synchronized_on_methods() {

        def source = """
            import groovy.contracts.*

            class A {

                @groovy.transform.Synchronized
                @Requires({ a >= 0 })
                def m(int a) { return a}

            }

            def a = new A()
            a.m(12)
        """

        evaluate source
    }
}
