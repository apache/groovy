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

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.LoggingEvent
import ch.qos.logback.core.OutputStreamAppender
import ch.qos.logback.core.layout.EchoLayout
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.slf4j.LoggerFactory

import java.lang.reflect.Modifier

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests for {@link groovy.util.logging.Slf4j Slf4j} AST transformation.
 */
final class Slf4jTest {

    private static class LogbackInterceptingAppender<E> extends OutputStreamAppender<E> {

        private List<LoggingEvent> events = []

        List<LoggingEvent> getEvents() {
            return events
        }

        protected void append(E event) {
            if (event instanceof LoggingEvent) {
                events.add(event)
                super.append(event)
            } else {
                throw new RuntimeException('Unable to intercept logging events - probably API has changed')
            }
        }
    }

    private LogbackInterceptingAppender appender
    private Logger logger

    @Before
    void setUp() {
        appender = new LogbackInterceptingAppender()
        appender.outputStream = new ByteArrayOutputStream()
        appender.context = (LoggerContext) LoggerFactory.getILoggerFactory()
        appender.layout = new EchoLayout()
        appender.name = 'MyAppender'
        appender.start()

        logger = LoggerFactory.getLogger('MyClass')
        logger.addAppender(appender)
        logger.level = Level.ALL
    }

    @After
    void tearDown() {
        logger.detachAppender(appender)
    }

    @Test
    void testPrivateFinalStaticLogFieldAppears() {
        Class clazz = new GroovyClassLoader().parseClass('''
            @groovy.util.logging.Slf4j
            class MyClass {
            }
        ''')

        clazz.getDeclaredField('log').modifiers.with { int modifiers ->
            assert Modifier.isPrivate(modifiers)
            assert Modifier.isStatic(modifiers)
            assert Modifier.isTransient(modifiers)
            assert Modifier.isFinal(modifiers)
        }
    }

    @Test
    void testExplicitPrivateFinalStaticLogFieldAppears() {
        Class clazz = new GroovyClassLoader().parseClass('''
            import static groovy.transform.options.Visibility.*
            @groovy.transform.VisibilityOptions(value = PRIVATE)
            @groovy.util.logging.Slf4j
            class MyClass {
            }
        ''')

        clazz.getDeclaredField('log').modifiers.with { int modifiers ->
            assert Modifier.isPrivate(modifiers)
            assert Modifier.isStatic(modifiers)
            assert Modifier.isTransient(modifiers)
            assert Modifier.isFinal(modifiers)
        }
    }

    @Test
    void testPackagePrivateFinalStaticLogFieldAppears() {
        Class clazz = new GroovyClassLoader().parseClass('''
            import static groovy.transform.options.Visibility.*
            @groovy.transform.VisibilityOptions(value = PACKAGE_PRIVATE)
            @groovy.util.logging.Slf4j
            class MyClass {
            }
        ''')

        clazz.getDeclaredField('log').modifiers.with { int modifiers ->
            assert !Modifier.isPrivate(modifiers)
            assert !Modifier.isProtected(modifiers)
            assert !Modifier.isPublic(modifiers)
            assert Modifier.isStatic(modifiers)
            assert Modifier.isTransient(modifiers)
            assert Modifier.isFinal(modifiers)
        }
    }

    @Test
    void testProtectedFinalStaticLogFieldAppears() {
        Class clazz = new GroovyClassLoader().parseClass('''
            import static groovy.transform.options.Visibility.*
            @groovy.transform.VisibilityOptions(value = PROTECTED)
            @groovy.util.logging.Slf4j
            class MyClass {
            }
        ''')

        clazz.getDeclaredField('log').modifiers.with { int modifiers ->
            assert Modifier.isProtected(modifiers)
            assert Modifier.isStatic(modifiers)
            assert Modifier.isTransient(modifiers)
            assert Modifier.isFinal(modifiers)
        }
    }

    @Test
    void testPublicFinalStaticLogFieldAppears() {
        Class clazz = new GroovyClassLoader().parseClass('''
            import static groovy.transform.options.Visibility.*
            @groovy.transform.VisibilityOptions(value = PUBLIC)
            @groovy.util.logging.Slf4j
            class MyClass {
            }
        ''')

        clazz.getDeclaredField('log').modifiers.with { int modifiers ->
            assert Modifier.isPublic(modifiers)
            assert Modifier.isStatic(modifiers)
            assert Modifier.isTransient(modifiers)
            assert Modifier.isFinal(modifiers)
        }
    }

    @Test
    void testPrivateFinalStaticNamedLogFieldAppears() {
        Class clazz = new GroovyClassLoader().parseClass('''
            @groovy.util.logging.Slf4j('logger')
            class MyClass {
            }
        ''')

        clazz.getDeclaredField('logger').modifiers.with { int modifiers ->
            assert Modifier.isPrivate(modifiers)
            assert Modifier.isStatic(modifiers)
            assert Modifier.isTransient(modifiers)
            assert Modifier.isFinal(modifiers)
        }
    }

    @Test
    void testClassAlreadyHasLogField() {
        shouldFail '''
            @groovy.util.logging.Slf4j
            class MyClass {
                String log
            }
        '''
    }

    @Test
    void testClassAlreadyHasNamedLogField() {
        shouldFail '''
            @groovy.util.logging.Slf4j('logger')
            class MyClass {
                String logger
            }
        '''
    }

    @Test
    void testLogInfo() {
        Class clazz = new GroovyClassLoader().parseClass('''
            @groovy.util.logging.Slf4j
            class MyClass {
                def loggingMethod() {
                    log.error ('error called')
                    log.warn  ('warn called')
                    log.info  ('info called')
                    log.debug ('debug called')
                    log.trace ('trace called')
                }
            }
            new MyClass().loggingMethod()
        ''')

        Script s = (Script) clazz.newInstance()
        s.run()

        def events = appender.events
        int ind = 0
        assert events.size() == 5
        assert events[ind].level == Level.ERROR
        assert events[ind].message == 'error called'
        assert events[++ind].level == Level.WARN
        assert events[ind].message == 'warn called'
        assert events[++ind].level == Level.INFO
        assert events[ind].message == 'info called'
        assert events[++ind].level == Level.DEBUG
        assert events[ind].message == 'debug called'
        assert events[++ind].level == Level.TRACE
        assert events[ind].message == 'trace called'
    }

    @Test
    void testLogFromStaticMethods() {
        Class clazz = new GroovyClassLoader().parseClass('''
            @groovy.util.logging.Slf4j
            class MyClass {
                static loggingMethod() {
                    log.info('(static) info called')
                }
            }
            MyClass.loggingMethod()
        ''')

        Script s = (Script) clazz.newInstance()
        s.run()

        def events = appender.events
        assert events.size() == 1
        assert events[0].level == Level.INFO
        assert events[0].message == '(static) info called'
    }

    @Test
    void testLogInfoWithNamedLogger() {
        Class clazz = new GroovyClassLoader().parseClass('''
          @groovy.util.logging.Slf4j('logger')
          class MyClass {
              def loggingMethod() {
                  logger.error ('error called')
                  logger.warn  ('warn called')
                  logger.info  ('info called')
                  logger.debug ('debug called')
                  logger.trace ('trace called')
              }
          }
          new MyClass().loggingMethod() ''')

        Script s = (Script) clazz.newInstance()
        s.run()

        def events = appender.getEvents()
        int ind = 0
        assert events.size() == 5
        assert events[ind].level == Level.ERROR
        assert events[ind].message == 'error called'
        assert events[++ind].level == Level.WARN
        assert events[ind].message == 'warn called'
        assert events[++ind].level == Level.INFO
        assert events[ind].message == 'info called'
        assert events[++ind].level == Level.DEBUG
        assert events[ind].message == 'debug called'
        assert events[++ind].level == Level.TRACE
        assert events[ind].message == 'trace called'
    }

    @Test // GROOVY-6373
    void testLogWithInnerClasses() {
        Class clazz = new GroovyClassLoader().parseClass('''
            @groovy.util.logging.Slf4j('logger')
            class MyClass {
                def loggingMethod() {
                    logger.info('outer called')
                }
                static class MyInnerClass {
                    def loggingMethod() {
                        logger.info('inner called')
                    }
                }
            }
            new MyClass().loggingMethod()
            new MyClass.MyInnerClass().loggingMethod()
        ''')

        Script s = (Script) clazz.newInstance()
        s.run()

        def events = appender.events
        int ind = 0
        assert events.size() == 2
        assert events[ind].level == Level.INFO
        assert events[ind].message == 'outer called'
        assert events[++ind].level == Level.INFO
        assert events[ind].message == 'inner called'
    }

    @Test // GROOVY-6834
    void testLogTransformInteractionWithAnonInnerClass() {
        assertScript '''
            @groovy.util.logging.Slf4j
            class MyClass {
                static myMethod() {
                    String message = 'hello'
                    String audience = 'world'
                    String result
                    new Runnable() {
                        void run() {
                            result = "$message $audience"
                        }
                    }.run()
                    result
                }
            }
            assert MyClass.myMethod() == 'hello world'
        '''
    }

    @Test // GROOVY-6873
    void testLogTransformInteractionWithAnonInnerClass2() {
        Class clazz = new GroovyClassLoader().parseClass('''
            @groovy.util.logging.Slf4j
            class Channel {
                private void someMethod(String folder)  {
                    final includeHidden = false
                    new Runnable() {
                        @Override
                        public void run() {
                            if (includeHidden) {
                            }
                        }
                    }
                }

                void otherMethod() {
                    def folder // "The current scope already contains a variable of the name folder"
                }
            }
        ''')
    }

    @Test
    void testLogGuard() {
        Class clazz = new GroovyClassLoader().parseClass('''
           @groovy.util.logging.Slf4j
            class MyClass {
                def loggingMethod() {
                    def isSet = false
                    log.setLevel(ch.qos.logback.classic.Level.ERROR)
                    log.trace (isSet = true)
                    return isSet
                }
            }
            new MyClass().loggingMethod()
        ''')

        Script s = (Script) clazz.newInstance()
        assert s.run() == false
    }

    @Test
    void testDefaultCategory() {
        Class clazz = new GroovyClassLoader().parseClass('''
            @groovy.util.logging.Slf4j
            class MyClass {
                static loggingMethod() {
                  log.info('info called')
                }
            }
        ''')

        def s = clazz.newInstance()
        s.loggingMethod()

        assert appender.events.size() == 1
    }

    @Test
    void testCustomCategory() {
        LogbackInterceptingAppender appenderForCustomCategory = new LogbackInterceptingAppender()
        appenderForCustomCategory.outputStream = new ByteArrayOutputStream()
        appenderForCustomCategory.layout = new EchoLayout()
        appenderForCustomCategory.start()

        Logger loggerForCustomCategory = LoggerFactory.getLogger('customCategory')
        loggerForCustomCategory.addAppender(appenderForCustomCategory)

        Class clazz = new GroovyClassLoader().parseClass('''
            @groovy.util.logging.Slf4j(category='customCategory')
            class MyClass {
                static loggingMethod() {
                  log.error('error called')
                }
            }
        ''')
        def s = clazz.newInstance()
        s.loggingMethod()

        assert appenderForCustomCategory.events.size() == 1
        assert appender.events.isEmpty()
    }
}
