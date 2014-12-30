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

import org.apache.log4j.spi.Filter
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Layout
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.layout.PatternLayout
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Logger

import java.nio.charset.Charset

class Log4j2Test extends GroovyTestCase {

    class Log4j2InterceptingAppender extends AbstractAppender {
        List<Map> events
        boolean isLogGuarded = true

        Log4j2InterceptingAppender(String name, Filter filter, Layout<String> layout) {
            super(name, filter, layout)
            this.events = new ArrayList<Map>()
        }

        @Override
        void append(LogEvent ev) {
            // Log4j2 re-cycles log events so extract and store the relevant info
            events.add([level: ev.level, message: ev.message.formattedMessage])
        }
    }

    Log4j2InterceptingAppender appender
    Logger logger

    protected void setUp() {
        super.setUp()

        PatternLayout layout = createLayout('%m', Charset.forName('UTF-8'))
        appender = new Log4j2InterceptingAppender('MyAppender', null, layout)
        logger = LogManager.getLogger('MyClass')
        logger.addAppender(appender)
        logger.setLevel(Level.ALL)
    }

    private static PatternLayout createLayout(String pattern, Charset charset) {
        return PatternLayout.newBuilder()
                .withPattern(pattern)
                .withCharset(charset)
                .withAlwaysWriteExceptions(true)
                .withNoConsoleNoAnsi(false)
                .withHeader('')
                .withFooter('')
                .build();
    }

    protected void tearDown() {
        super.tearDown()
        logger.removeAppender(appender)
    }

    void testPrivateFinalStaticLogFieldAppears() {
        Class clazz = new GroovyClassLoader().parseClass('''
              @groovy.util.logging.Log4j2
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
            @groovy.util.logging.Log4j2
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
            @groovy.util.logging.Log4j2
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
            @groovy.util.logging.Log4j2
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
            @groovy.util.logging.Log4j2
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

    void testUnknownAccessPrivateFinalStaticLogFieldAppears() {
        Class clazz = new GroovyClassLoader().parseClass('''
            @groovy.util.logging.Log4j2
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

    void testClassAlreadyHasLogField() {
        shouldFail(RuntimeException) {
            Class clazz = new GroovyClassLoader().parseClass('''
                @groovy.util.logging.Log4j2()
                class MyClass {
                    String log
                } ''')

            assert clazz.newInstance()
        }
    }

    void testClassAlreadyHasNamedLogField() {
        shouldFail(RuntimeException) {
            Class clazz = new GroovyClassLoader().parseClass('''
                @groovy.util.logging.Log4j2('logger')
                class MyClass {
                    String logger
                } ''')

            assert clazz.newInstance()
        }
    }

    void testLogInfo() {
        Class clazz = new GroovyClassLoader().parseClass('''
            @groovy.util.logging.Log4j2
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

        clazz.newInstance().run()

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
            @groovy.util.logging.Log4j2
            class MyClass {
                static loggingMethod() {
                  log.info   ("(static) info called")
                }
            }
            MyClass.loggingMethod()""")

        clazz.newInstance().run()

        def events = appender.getEvents()
        assert events.size() == 1
        assert events[0].level == Level.INFO
        assert events[0].message == "(static) info called"
    }

    void testLogInfoForNamedLogger() {
        Class clazz = new GroovyClassLoader().parseClass('''
            @groovy.util.logging.Log4j2('logger')
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

        clazz.newInstance().run()

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
            @groovy.util.logging.Log4j2
            class MyClass {
                def loggingMethod() {
                    log.setLevel(org.apache.logging.log4j.Level.OFF)
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

        clazz.newInstance().run()

        assert appender.isLogGuarded == true
    }

    void testDefaultCategory() {
        Class clazz = new GroovyClassLoader().parseClass("""
                @groovy.util.logging.Log4j2
                class MyClass {
                    static loggingMethod() {
                      log.info("info called")
                    }
                }""")

        clazz.newInstance().loggingMethod()

        assert appender.getEvents().size() == 1
    }

    void testCustomCategory() {
        PatternLayout layout = createLayout('%m', Charset.forName('UTF-8'))
        Log4j2InterceptingAppender appenderForCustomCategory = new Log4j2InterceptingAppender('Appender4CustomCategory', null, layout)
        def loggerForCustomCategory = LogManager.getLogger('customCategory')
        loggerForCustomCategory.addAppender(appenderForCustomCategory)

        Class clazz = new GroovyClassLoader().parseClass("""
                @groovy.util.logging.Log4j2(category='customCategory')
                class MyClass {
                    static loggingMethod() {
                      log.error("error called")
                    }
                }""")

        clazz.newInstance().loggingMethod()

        assert appenderForCustomCategory.getEvents().size() == 1
        assert appender.getEvents().size() == 0
    }
}
