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
package org.apache.groovy.contracts.tests.interfaces

import org.apache.groovy.contracts.PostconditionViolation
import org.apache.groovy.contracts.PreconditionViolation
import org.apache.groovy.contracts.tests.basic.BaseTestClass
import org.junit.Test

class InterfaceAbstractClassMixturesTests extends BaseTestClass {

    @Test
    void class_with_abstract_class_and_interface() {

        def s1 = '''
        @Contracted
        package tests

        import groovy.contracts.*

        interface Stackable {
            @Ensures({ result != null })
            def pop()
        }

        abstract class StackableAbstract implements Stackable {
            abstract def pop()
        }
        '''

        def s2 = '''
        @Contracted
        package tests

        import groovy.contracts.*

        class Stack extends StackableAbstract {
            def pop() { return null }
        }
        '''

        add_class_to_classpath(s1)
        def stack = create_instance_of(s2)

        shouldFail PostconditionViolation, {
            stack.pop()
        }
    }

    @Test
    void interface_and_abstract_class_both_contain_abstract_methods() {

        def s1 = '''
        @Contracted
        package tests

        import groovy.contracts.*

        interface Stackable {
            @Ensures({ result != null })
            def pop()
        }

        abstract class StackableAbstract implements Stackable {
            @Requires({ item != null })
            abstract def push(def item)
        }
        '''

        def s2 = '''
        @Contracted
        package tests

        import groovy.contracts.*

        class Stack extends StackableAbstract {
            def pop() { return null }
            def push(def item) {}
        }
        '''

        add_class_to_classpath(s1)
        def stack = create_instance_of(s2)

        shouldFail PostconditionViolation.class, {
            stack.pop()
        }

        shouldFail PreconditionViolation.class, {
            stack.push(null)
        }
    }
}
