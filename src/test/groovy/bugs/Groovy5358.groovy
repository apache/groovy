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

final class Groovy5358 {
    @Test
    void testGetPropertyOverrides() {
        assertScript '''
            class FooWorksAsMap {
                def getProperty(String name) {
                    "OK:FooWorksAsMap.$name"
                }
            }

            class BarWorksAsMap {}
            @Category(BarWorksAsMap) class C {
                def getProperty(String name) {
                    "OK:BarWorksAsMap.$name"
                }
            }
            BarWorksAsMap.mixin C

            class BazWorksAsMap {}
            BazWorksAsMap.metaClass.getProperty = { String name ->
                "OK:BazWorksAsMap.$name"
            }

            //

            def objects = [
                new FooWorksAsMap(),
                new BarWorksAsMap(),
                new BazWorksAsMap(),
                [foo:'OK:LinkedHashMap.foo']
            ]
            for (def name in ['foo']) {
                for (def obj in objects) {
                    def which = "${obj.getClass().getSimpleName()}.$name"
                    try {
                        String value = obj."$name"
                        assert value.startsWith('OK') : "$which -> $value"
                    } catch (any) {
                        assert false : "$which -> FAIL:$any"
                    }
                }
            }
        '''
    }

    @Test
    void testSetPropertyOverrides() {
        assertScript '''
            class FooWorksAsMap {
                def val
                void setProperty(String name, value) {
                    val = "OK:FooWorksAsMap.$value"
                }
            }
            class BarWorksAsMap {
                def val
            }
            @Category(BarWorksAsMap) class C {
                void setProperty(String name, value) {
                    setVal("OK:BarWorksAsMap.$value")
                }
            }
            BarWorksAsMap.mixin C
            class BazWorksAsMap {
                def val
            }
            BazWorksAsMap.metaClass.setProperty = { String name, value ->
                    setVal("OK:BazWorksAsMap.$value")
            }

            def objects = [
                new FooWorksAsMap(),
                new BarWorksAsMap(),
                new BazWorksAsMap(),
                [:]
            ]
            for (def obj in objects) {
                def which = "${obj.getClass().getSimpleName()}.val"
                try {
                    obj.val = which.startsWith('LinkedHashMap') ? "OK:LinkedHashMap.bar" : 'bar'
                    assert obj.val.startsWith('OK:') : "$which -> $obj.val"
                } catch (any) {
                    assert false : "$which -> FAIL:$any"
                }
            }
        '''
    }
}
