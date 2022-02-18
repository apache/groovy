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

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class Groovy5364 {

    @Test
    void testStaticScriptMethodAsProperty1() {
        assertScript '''
            static getStaticProperty() {
                'x'
            }

            assert 'x' == getStaticProperty()
            assert 'x' == staticProperty
        '''
    }

    @Test
    void testStaticScriptMethodAsProperty2() {
        assertScript '''
            static getSomething() { 'x' }
            something = 'y' // script var

            assert 'x' == getSomething()
            assert 'y' == something
        '''
    }

    @Test
    void testStaticScriptMethodAsProperty3() {
        new GroovyShell(new Binding(something: 'y')).evaluate '''
            static getSomething() { 'x' }

            assert 'x' == getSomething()
            assert 'y' == something
        '''
    }

    @Test
    void testStaticScriptMethodAsProperty4() {
        assertScript '''
            static getStaticProperty() {
                'x'
            }

            void test() {
                assert 'x' == getStaticProperty()
                assert 'x' == staticProperty
            }
            test()
        '''
    }

    @Test
    void testStaticScriptMethodAsProperty5() {
        assertScript '''
            static getStaticProperty() {
                'x'
            }

            static void test() {
                assert 'x' == getStaticProperty()
                assert 'x' == staticProperty // Apparent variable 'staticProperty' was found in a static scope but doesn't refer to a local variable, static field or class
            }
            test()
        '''
    }

    @Test
    void testStaticScriptMethodAsProperty6() {
        def err = shouldFail '''
            def getNonStaticProperty() {
                'x'
            }

            static void test() {
                nonStaticProperty
            }
        '''
        assert err =~ /Apparent variable 'nonStaticProperty' was found in a static scope but doesn't refer to a local variable, static field or class/
    }
}
