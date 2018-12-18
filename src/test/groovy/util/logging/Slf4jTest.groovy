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
import org.slf4j.LoggerFactory

import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * Tests for Slf4j AST transformation
 */

class Slf4jTest extends GroovyTestCase {

    LogbackInterceptingAppender appender
    Logger logger

    protected void setUp() {
        super.setUp()
        logger = LoggerFactory.getLogger("MyClass")

        appender = new LogbackInterceptingAppender()
        appender.setOutputStream(new ByteArrayOutputStream())
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory()
        appender.setContext(lc)
        appender.setName("MyAppender")
        appender.setLayout(new EchoLayout())
        appender.start()

        logger.addAppender(appender)
        logger.setLevel(Level.ALL)
    }

    protected void tearDown() {
        super.tearDown()
        logger.detachAppender(appender)
    }

    void testPrivateFinalStaticLogFieldAppears() {
        Class clazz = new GroovyClassLoader().parseClass('''
                @groovy.util.logging.Slf4j
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

    void testPrivateFinalStaticNamedLogFieldAppears() {
        Class clazz = new GroovyClassLoader().parseClass('''
                @groovy.util.logging.Slf4j('logger')
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
                @groovy.util.logging.Slf4j
                class MyClass {
                    String log
                } ''')

            assert clazz.newInstance()
        }
    }

    void testClassAlreadyHasNamedLogField() {
        shouldFail {
            Class clazz = new GroovyClassLoader().parseClass('''
                @groovy.util.logging.Slf4j('logger')
                class MyClass {
                    String logger
                } ''')

            assert clazz.newInstance()
        }
    }

    void testLogInfo() {
        Class clazz = new GroovyClassLoader().parseClass('''
          @groovy.util.logging.Slf4j
          class MyClass {

              def loggingMethod() {
                  log.error ("error called")
                  log.warn  ("warn called")
                  log.info  ("info called")
                  log.debug ("debug called")
                  log.trace ("trace called")
              }
          }
          new MyClass().loggingMethod() ''')

        Script s = (Script) clazz.newInstance()
        s.run()

        def events = appender.getEvents()
        int ind = 0
        assert events.size() == 5
        assert events[ind].level == Level.ERROR
        assert events[ind].message == "error called"
        assert events[++ind].level == Level.WARN
        assert events[ind].message == "warn called"
        assert events[++ind].level == Level.INFO
        assert events[ind].message == "info called"
        assert events[++ind].level == Level.DEBUG
        assert events[ind].message == "debug called"
        assert events[++ind].level == Level.TRACE
        assert events[ind].message == "trace called"
    }

    void testLogFromStaticMethods() {
        Class clazz = new GroovyClassLoader().parseClass("""
            @groovy.util.logging.Slf4j
            class MyClass {
                static loggingMethod() {
                  log.info   ("(static) info called")
                }
            }
            MyClass.loggingMethod()""")

        Script s = (Script) clazz.newInstance()
        s.run()

        def events = appender.getEvents()
        assert events.size() == 1
        assert events[0].level == Level.INFO
        assert events[0].message == "(static) info called"
    }

    void testLogInfoWithNamedLogger() {
        Class clazz = new GroovyClassLoader().parseClass('''
          @groovy.util.logging.Slf4j('logger')
          class MyClass {

              def loggingMethod() {
                  logger.error ("error called")
                  logger.warn  ("warn called")
                  logger.info  ("info called")
                  logger.debug ("debug called")
                  logger.trace ("trace called")
              }
          }
          new MyClass().loggingMethod() ''')

        Script s = (Script) clazz.newInstance()
        s.run()

        def events = appender.getEvents()
        int ind = 0
        assert events.size() == 5
        assert events[ind].level == Level.ERROR
        assert events[ind].message == "error called"
        assert events[++ind].level == Level.WARN
        assert events[ind].message == "warn called"
        assert events[++ind].level == Level.INFO
        assert events[ind].message == "info called"
        assert events[++ind].level == Level.DEBUG
        assert events[ind].message == "debug called"
        assert events[++ind].level == Level.TRACE
        assert events[ind].message == "trace called"
    }

    void testLogTransformInteractionWithAIC_groovy6834() {
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

    void testLogWithInnerClasses_groovy6373() {
        Class clazz = new GroovyClassLoader().parseClass('''
            @groovy.util.logging.Slf4j('logger')
            class MyClass {
                def loggingMethod() {
                    logger.info  ("outer called")
                }
                static class MyInnerClass {
                    def loggingMethod() {
                        logger.info  ("inner called")
                    }
                }
            }
            new MyClass().loggingMethod()
            new MyClass.MyInnerClass().loggingMethod()
        ''')

        Script s = (Script) clazz.newInstance()
        s.run()

        def events = appender.getEvents()
        int ind = 0
        assert events.size() == 2
        assert events[ind].level == Level.INFO
        assert events[ind].message == "outer called"
        assert events[++ind].level == Level.INFO
        assert events[ind].message == "inner called"
    }

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
            new MyClass().loggingMethod() ''')

        Script s = (Script) clazz.newInstance()
        assert s.run() == false
    }

    void testDefaultCategory() {
        Class clazz = new GroovyClassLoader().parseClass("""
            @groovy.util.logging.Slf4j
            class MyClass {
                static loggingMethod() {
                  log.info("info called")
                }
            }""")

        def s = clazz.newInstance()
        s.loggingMethod()

        assert appender.getEvents().size() == 1
    }

    void testGroovy6873Regression() {
        Class clazz = new GroovyClassLoader().parseClass("""
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
                    def folder
                }
            }""")
    }

    void testCustomCategory() {
        LogbackInterceptingAppender appenderForCustomCategory = new LogbackInterceptingAppender()
        appenderForCustomCategory.setOutputStream(new ByteArrayOutputStream())
        appenderForCustomCategory.setLayout(new EchoLayout())
        appenderForCustomCategory.start()

        Logger loggerForCustomCategory = LoggerFactory.getLogger("customCategory")
        loggerForCustomCategory.addAppender(appenderForCustomCategory)

        Class clazz = new GroovyClassLoader().parseClass("""
            @groovy.util.logging.Slf4j(category='customCategory')
            class MyClass {
                static loggingMethod() {
                  log.error("error called")
                }
            }""")
        def s = clazz.newInstance()

        s.loggingMethod()

        assert appenderForCustomCategory.getEvents().size() == 1
        assert appender.getEvents().size() == 0
    }
}

class LogbackInterceptingAppender<E> extends OutputStreamAppender<E> {

    private List<LoggingEvent> events = new ArrayList<LoggingEvent>()

    List<LoggingEvent> getEvents() {
        return events
    }

    protected void append(E event) {
        if (event instanceof LoggingEvent) {
            events.add(event)
        } else {
            throw new RuntimeException("Unable to intercept logging events - probably API has changed")
        }
        super.append(event)
    }
}
