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
package groovy.util.logging

import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * Unit test for the commons logging @Log based annotation.
 */
class CommonsTest extends GroovyTestCase {

    PrintStream savedSystemOut
    ByteArrayOutputStream redirectedSystemOut

    void setUp() {
        super.setUp()
        savedSystemOut = System.out
        redirectedSystemOut = new ByteArrayOutputStream()
        System.out = new PrintStream(redirectedSystemOut)
    }

    void tearDown() {
        super.tearDown()
        System.out = savedSystemOut
    }

    void testPrivateFinalStaticLogFieldAppears() {
        Class clazz = new GroovyClassLoader().parseClass('''
              @groovy.util.logging.Commons
              class MyClass {
              } ''')

        assert clazz.declaredFields.find { Field field ->
            field.name == "log" &&
                    Modifier.isPrivate(field.getModifiers()) &&
                    Modifier.isStatic(field.getModifiers()) &&
                    Modifier.isTransient(field.getModifiers()) &&
                    Modifier.isFinal(field.getModifiers())
        }
    }

    void testExplicitPrivateFinalStaticLogFieldAppears() {
        Class clazz = new GroovyClassLoader().parseClass('''
            import static groovy.transform.options.Visibility.*
            @groovy.transform.VisibilityOptions(value = PRIVATE)
            @groovy.util.logging.Commons
            class MyClass {
            }
        ''')

        assert clazz.declaredFields.find { Field field ->
            field.name == "log" &&
                    Modifier.isPrivate(field.getModifiers()) &&
                    Modifier.isStatic(field.getModifiers()) &&
                    Modifier.isTransient(field.getModifiers()) &&
                    Modifier.isFinal(field.getModifiers())
        }
    }

    void testPackagePrivateFinalStaticLogFieldAppears() {
        Class clazz = new GroovyClassLoader().parseClass('''
            import static groovy.transform.options.Visibility.*
            @groovy.transform.VisibilityOptions(value = PACKAGE_PRIVATE)
            @groovy.util.logging.Commons
            class MyClass {
            }
        ''')

        assert clazz.declaredFields.find { Field field ->
            field.name == "log" &&
                    !Modifier.isPrivate(field.getModifiers()) &&
                    !Modifier.isProtected(field.getModifiers()) &&
                    !Modifier.isPublic(field.getModifiers()) &&
                    Modifier.isStatic(field.getModifiers()) &&
                    Modifier.isTransient(field.getModifiers()) &&
                    Modifier.isFinal(field.getModifiers())
        }
    }

    void testProtectedFinalStaticLogFieldAppears() {
        Class clazz = new GroovyClassLoader().parseClass('''
            import static groovy.transform.options.Visibility.*
            @groovy.transform.VisibilityOptions(value = PROTECTED)
            @groovy.util.logging.Commons
            class MyClass {
            }
        ''')

        assert clazz.declaredFields.find { Field field ->
            field.name == "log" &&
                    Modifier.isProtected(field.getModifiers()) &&
                    Modifier.isStatic(field.getModifiers()) &&
                    Modifier.isTransient(field.getModifiers()) &&
                    Modifier.isFinal(field.getModifiers())
        }
    }

    void testPublicFinalStaticLogFieldAppears() {
        Class clazz = new GroovyClassLoader().parseClass('''
            import static groovy.transform.options.Visibility.*
            @groovy.transform.VisibilityOptions(value = PUBLIC)
            @groovy.util.logging.Commons
            class MyClass {
            }
        ''')

        assert clazz.declaredFields.find { Field field ->
            field.name == "log" &&
                    Modifier.isPublic(field.getModifiers()) &&
                    Modifier.isStatic(field.getModifiers()) &&
                    Modifier.isTransient(field.getModifiers()) &&
                    Modifier.isFinal(field.getModifiers())
        }
    }

    void testPrivateFinalStaticNamedLogFieldAppears() {
        Class clazz = new GroovyClassLoader().parseClass('''
              @groovy.util.logging.Commons('logger')
              class MyClass {
              } ''')

        assert clazz.declaredFields.find { Field field ->
            field.name == "logger" &&
                    Modifier.isPrivate(field.getModifiers()) &&
                    Modifier.isStatic(field.getModifiers()) &&
                    Modifier.isTransient(field.getModifiers()) &&
                    Modifier.isFinal(field.getModifiers())
        }
    }

    void testClassAlreadyHasLogField() {
        shouldFail {
            Class clazz = new GroovyClassLoader().parseClass('''
                @groovy.util.logging.Commons
                class MyClass {
                    String log
                } ''')

            assert clazz.newInstance()
        }
    }

    void testClassAlreadyHasNamedLogField() {
        shouldFail {
            Class clazz = new GroovyClassLoader().parseClass('''
                @groovy.util.logging.Commons('logger')
                class MyClass {
                    String logger
                } ''')

            assert clazz.newInstance()
        }
    }

    void testLogLevelDebug() {
        Class clazz = new GroovyClassLoader().parseClass('''
            @groovy.util.logging.Commons
            class MyClass {

                def loggingMethod() {
                    log.error ("error called")
                    log.warn  ("warn called")
                    log.info  ("info called")
                    log.debug ("debug called")
                }
            }
            new MyClass().loggingMethod() ''')

        clazz.newInstance().run()

        String log = redirectedSystemOut.toString()
        assert log.contains("error called")
        assert log.contains("warn called")
        assert log.contains("info called")
        assert log.contains("debug called")
    }

    void testLogFromStaticMethods() {
        Class clazz = new GroovyClassLoader().parseClass("""
            @groovy.util.logging.Commons
            class MyClass {
                static loggingMethod() {
                  log.info   ("(static) info called")
                }
            }
            MyClass.loggingMethod()""")

        clazz.newInstance().run()

        String log = redirectedSystemOut.toString()
        assert log.contains("(static) info called")
    }

    void testNamedLogger() {
        Class clazz = new GroovyClassLoader().parseClass('''
            @groovy.util.logging.Commons('logger')
            class MyClass {

                def loggingMethod() {
                    logger.error ("error called")
                    logger.warn  ("warn called")
                    logger.info  ("info called")
                    logger.debug ("debug called")
                }
            }
            new MyClass().loggingMethod() ''')

        clazz.newInstance().run()

        String log = redirectedSystemOut.toString()
        assert log.contains("error called")
        assert log.contains("warn called")
        assert log.contains("info called")
        assert log.contains("debug called")
    }

    void testLogGuards() {
        Class clazz = new GroovyClassLoader().parseClass('''
            class LogDecorator extends groovy.util.Proxy {
                boolean isTraceEnabled() { false }
            }

            @groovy.util.logging.Commons
            class MyClass {
                boolean traceCalled = false

                def loggingMethod() {
                    overrideLog()
                    log.trace (traceCalled = true)
                }

                def overrideLog() {
                    def field = MyClass.getDeclaredField('log')
                    field.accessible = true
                    def modifiersField = java.lang.reflect.Field.getDeclaredField("modifiers")
                    modifiersField.accessible = true
                    modifiersField.setInt(field, field.modifiers & ~java.lang.reflect.Modifier.FINAL)
                    field.set(null, new LogDecorator().wrap(log) as org.apache.commons.logging.Log)
                }
            }
            def o = new MyClass()
            o.loggingMethod()
            o.traceCalled''')

        Script s = (Script) clazz.newInstance()
        def result = s.run()
        assert !result
    }

    void testDefaultCategory() {
        Class clazz = new GroovyClassLoader().parseClass("""
            @groovy.util.logging.Commons
            class MyClass {
                static loggingMethod() {
                  log.error("error called")
                }
            }""")

        clazz.newInstance().loggingMethod()

        assert redirectedSystemOut.toString().contains('MyClass')
    }

    void testCustomCategory() {
        Class clazz = new GroovyClassLoader().parseClass("""
            @groovy.util.logging.Commons(category='customCategory')
            class MyClass {
                static loggingMethod() {
                  log.error("error called")
                }
            }""")

        clazz.newInstance().loggingMethod()

        assert redirectedSystemOut.toString().contains('customCategory')
    }
}
