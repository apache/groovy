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

import groovy.test.GroovyTestCase

import java.lang.reflect.*
import org.apache.log4j.AppenderSkeleton
import org.apache.log4j.spi.LoggingEvent
import org.apache.log4j.Level
import org.apache.log4j.Logger

/**
 * Tests for Log4j AST transformation
 */
class Log4jTest extends GroovyTestCase {

    Log4jInterceptingAppender appender
    Logger logger

    protected void setUp() {
        super.setUp()
        appender = new Log4jInterceptingAppender()
        appender.setName('MyAppender')
        logger = Logger.getLogger('MyClass')
        logger.addAppender(appender)
        logger.setLevel(Level.ALL)
    }

    protected void tearDown() {
        super.tearDown()
        logger.removeAllAppenders()
    }

    void testPrivateFinalStaticLogFieldAppears() {

        Class clazz = new GroovyClassLoader().parseClass('''
              @groovy.util.logging.Log4j
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
            @groovy.util.logging.Log4j
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

    void testPackagePrivateFinalStaticLogFieldAppears() {
        Class clazz = new GroovyClassLoader().parseClass('''
            import static groovy.transform.options.Visibility.*
            @groovy.transform.VisibilityOptions(value = PACKAGE_PRIVATE)
            @groovy.util.logging.Log4j
            class MyClass {
            } ''')

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
            @groovy.util.logging.Log4j
            class MyClass {
            } ''')

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
            @groovy.util.logging.Log4j
            class MyClass {
            } ''')

        assert clazz.declaredFields.find { Field field ->
            field.name == "log" &&
                    Modifier.isPublic(field.getModifiers()) &&
                    Modifier.isStatic(field.getModifiers()) &&
                    Modifier.isTransient(field.getModifiers()) &&
                    Modifier.isFinal(field.getModifiers())
        }
    }

    void testClassAlreadyHasLogField() {

        shouldFail {

            Class clazz = new GroovyClassLoader().parseClass('''
                @groovy.util.logging.Log4j()
                class MyClass {
                    String log
                } ''')

            assert clazz.newInstance()
        }
    }

    void testClassAlreadyHasNamedLogField() {

        shouldFail {

            Class clazz = new GroovyClassLoader().parseClass('''
                @groovy.util.logging.Log4j('logger')
                class MyClass {
                    String logger
                } ''')

            assert clazz.newInstance()
        }
    }

    void testLogInfo() {

        Class clazz = new GroovyClassLoader().parseClass('''
            @groovy.util.logging.Log4j
            class MyClass {

                def loggingMethod() {
                    log.fatal ("fatal called")
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

        int ind = 0
        def events = appender.getEvents()
        assert events.size() == 6
        assert events[ind].level == Level.FATAL
        assert events[ind].message == "fatal called"
        assert events[++ind].level == Level.ERROR
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
            @groovy.util.logging.Log4j
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

    void testLogInfoForNamedLogger() {

        Class clazz = new GroovyClassLoader().parseClass('''
            @groovy.util.logging.Log4j('logger')
            class MyClass {

                def loggingMethod() {
                    logger.fatal ("fatal called")
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

        int ind = 0
        def events = appender.getEvents()
        assert events.size() == 6
        assert events[ind].level == Level.FATAL
        assert events[ind].message == "fatal called"
        assert events[++ind].level == Level.ERROR
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

    void testLogGuard() {
        Class clazz = new GroovyClassLoader().parseClass('''
            @groovy.util.logging.Log4j
            class MyClass {
                def loggingMethod() {
                    log.setLevel(org.apache.log4j.Level.OFF)
                    log.fatal (prepareLogMessage())
                    log.error (prepareLogMessage())
                    log.warn  (prepareLogMessage())
                    log.info  (prepareLogMessage())
                    log.debug (prepareLogMessage())
                    log.trace (prepareLogMessage())
                }

                def prepareLogMessage() {
                  log.getAppender('MyAppender')?.isLogGuarded = false
                  return "formatted log message"
                }
            }
            new MyClass().loggingMethod() ''')

        Script s = (Script) clazz.newInstance()
        s.run()

        assert appender.isLogGuarded == true
    }

    void testDefaultCategory() {
        Class clazz = new GroovyClassLoader().parseClass("""
            @groovy.util.logging.Log4j
            class MyClass {
                static loggingMethod() {
                  log.info("info called")
                }
            }""")

        def s = clazz.newInstance()
        s.loggingMethod()

        assert appender.getEvents().size() == 1
    }

    void testCustomCategory() {
        Log4jInterceptingAppender appenderForCustomCategory = new Log4jInterceptingAppender()

        Logger loggerForCustomCategory = Logger.getLogger('customCategory')
        loggerForCustomCategory.addAppender(appenderForCustomCategory)

        Class clazz = new GroovyClassLoader().parseClass("""
            @groovy.util.logging.Log4j(category='customCategory')
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

class Log4jInterceptingAppender extends AppenderSkeleton {
    List<LoggingEvent> events
    boolean isLogGuarded = true

    Log4jInterceptingAppender() {
        this.events = new ArrayList<LoggingEvent>()
    }

    List<LoggingEvent> getEvents() {
        return events
    }

    protected void append(LoggingEvent logEvent) {
        events.add(logEvent)
    }

    void close() {
    }

    boolean requiresLayout() {
        return false
    }
}

