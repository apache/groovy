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

import org.codehaus.groovy.control.MultipleCompilationErrorsException

import java.lang.reflect.Modifier

class SingletonTransformTest extends GroovyShellTestCase {

    void testSingleton() {
        def res = evaluate("""
            @Singleton
            class X {
                def getHello () {
                    "Hello, World!"
                }
            }
            X.instance.hello
        """)

        assert "Hello, World!" == res
    }

    void testSingletonCompileStatic() {
        def res = evaluate("""
            @Singleton @groovy.transform.CompileStatic
            class X {
                def getHello () { "Hello, World!" }
            }
            X.instance.hello
        """)
        assert "Hello, World!" == res
    }

    void testLazySingleton() {
        def res = evaluate("""
            @Singleton(lazy=true)
            class X {
                def getHello () {
                    "Hello, World!"
                }
            }
            assert X.@instance == null
            X.instance.hello
        """)

        assert "Hello, World!" == res
    }

    void testSingletonInstantiationFails() {
        shouldFail {
            evaluate("""
                @Singleton
                class X {
                    def getHello () {
                        "Hello, World!"
                    }
                }
                new X ()
            """)
        }
    }

    void testSingletonOverrideConstructor() {
        def res = evaluate("""
            @Singleton(strict=false)
            class X {
                static hello = "Bye-bye world"
                X () {
                    hello = "Hello, World!"
                }
            }
            X.instance.hello
        """)

        assert "Hello, World!" == res
    }

    void testSingletonOverrideConstructorFails() {
        def msg = shouldFail(MultipleCompilationErrorsException) {
            evaluate("""
                @Singleton
                class X {
                    X() { }
                }
            """)
        }
        assert msg.contains("@Singleton didn't expect to find one or more additional constructors: remove constructor(s) or set strict=false")
    }

    void testSingletonAdditionalConstructorFails() {
        def msg = shouldFail(MultipleCompilationErrorsException) {
            evaluate("""
                @Singleton
                class X {
                    X(String ignored) { }
                }
            """)
        }
        assert msg.contains("@Singleton didn't expect to find one or more additional constructors: remove constructor(s) or set strict=false")
    }

    void testSingletonCustomPropertyName() {
        def propertyName = 'myProp'
        def getterName = 'getMyProp'
        def className = 'X'
        def defaultPropertyName = 'instance'

        def invoker = new GroovyClassLoader()
        def clazz = invoker.parseClass("""
            @Singleton(property ='$propertyName')
            class $className { }
        """)

        int modifiers = clazz.getDeclaredField(propertyName).modifiers //should be public static final for non-lazy singleton
        int flags = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL
        assert (modifiers & flags) == flags

        def object = clazz.getMethod(getterName).invoke(null)
        assert className == object.class.name

        try {
            clazz.newInstance() //should throw exception here
            fail() //shouldn't get here
        } catch (RuntimeException e) { //for tests run in Groovy (which can access privates)
            assert e.message.contains(propertyName)
        }
        try {
            clazz.getField(defaultPropertyName) //should throw exception here
            fail() //shouldn't get here
        } catch (NoSuchFieldException e) {
            assert e.message.contains(defaultPropertyName)
        }
    }
}