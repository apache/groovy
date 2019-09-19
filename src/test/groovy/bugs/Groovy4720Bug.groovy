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

/**
 * Groovy-4720: Method overriding with ExpandoMetaClass is partially broken
 */
class Groovy4720Bug extends GroovyTestCase {

    void testBug() {
        def instanceMethods = [DummyApi1.getMethod('test', java.io.Serializable), DummyApi2.getMethod('test', java.io.Serializable)]

        Dummy4720.metaClass {
            for (method in instanceMethods) {
                def apiInstance = method.getDeclaringClass().newInstance()
                def methodName = method.name
                def parameterTypes = method.parameterTypes
                if (parameterTypes) {
                    "$methodName"(new Closure(this) {
                        def call(Object[] args) {
                            apiInstance."$methodName"(* args)
                        }
                        Class[] getParameterTypes() { parameterTypes }
                    })
                }
            }
        }

        assert new Dummy4720().test(1) == "overrided"
    }
}

class Dummy4720 {}

class DummyApi1 {
    def test(Serializable id) {
        "original"
    }
}

class DummyApi2 {
    def test(Serializable id) {
        "overrided"
    }
}
