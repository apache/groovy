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

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.Layout
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.layout.PatternLayout
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import java.lang.reflect.Field
import java.nio.charset.Charset

import static groovy.test.GroovyAssert.shouldFail
import static java.lang.reflect.Modifier.*

final class Log4j2Test {

    static class Log4j2InterceptingAppender extends AbstractAppender {
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

    private Log4j2InterceptingAppender appender = new Log4j2InterceptingAppender('MyAppender', null, createLayout())

    private static PatternLayout createLayout() {
        return PatternLayout.newBuilder()
                .withPattern('%m')
                .withCharset(Charset.forName('UTF-8'))
                .withAlwaysWriteExceptions(true)
                .withNoConsoleNoAnsi(false)
                .withHeader('')
                .withFooter('')
                .build()
    }

    //--------------------------------------------------------------------------

    @Test
    void testPrivateStaticFinalLogFieldAppears() {
        Class clazz = new GroovyClassLoader().parseClass('''
            @groovy.util.logging.Log4j2
            class MyClass { }
        ''')
        Field field = clazz.declaredFields.find { it.name == 'log' }

        assert field != null
        assert isFinal(field.getModifiers())
        assert isStatic(field.getModifiers())
        assert isPrivate(field.getModifiers())
        assert isTransient(field.getModifiers())
        assert field.type.name == 'org.apache.logging.log4j.Logger' // GROOVY-11798
    }

    @Test
    void testExplicitPrivateStaticFinalLogFieldAppears() {
        Class clazz = new GroovyClassLoader().parseClass('''
            import static groovy.transform.options.Visibility.*
            @groovy.transform.VisibilityOptions(value = PRIVATE)
            @groovy.util.logging.Log4j2
            class MyClass { }
        ''')
        Field field = clazz.declaredFields.find { it.name == 'log' }

        assert field != null
        assert isFinal(field.getModifiers())
        assert isStatic(field.getModifiers())
        assert isPrivate(field.getModifiers())
        assert isTransient(field.getModifiers())
    }

    @Test
    void testPackagePrivateStaticFinalLogFieldAppears() {
        Class clazz = new GroovyClassLoader().parseClass('''
            import static groovy.transform.options.Visibility.*
            @groovy.transform.VisibilityOptions(value = PACKAGE_PRIVATE)
            @groovy.util.logging.Log4j2
            class MyClass { }
        ''')
        Field field = clazz.declaredFields.find { it.name == 'log' }

        assert field != null
        assert  isFinal(field.getModifiers())
        assert  isStatic(field.getModifiers())
        assert !isPublic(field.getModifiers())
        assert !isPrivate(field.getModifiers())
        assert !isProtected(field.getModifiers())
        assert  isTransient(field.getModifiers())
    }

    @Test
    void testProtectedStaticFinalLogFieldAppears() {
        Class clazz = new GroovyClassLoader().parseClass('''
            import static groovy.transform.options.Visibility.*
            @groovy.transform.VisibilityOptions(value = PROTECTED)
            @groovy.util.logging.Log4j2
            class MyClass { }
        ''')
        Field field = clazz.declaredFields.find { it.name == 'log' }

        assert field != null
        assert isFinal(field.getModifiers())
        assert isStatic(field.getModifiers())
        assert isProtected(field.getModifiers())
        assert isTransient(field.getModifiers())
    }

    @Test
    void testPublicStaticFinalLogFieldAppears() {
        Class clazz = new GroovyClassLoader().parseClass('''
            import static groovy.transform.options.Visibility.*
            @groovy.transform.VisibilityOptions(value = PUBLIC)
            @groovy.util.logging.Log4j2
            class MyClass { }
        ''')
        Field field = clazz.declaredFields.find { it.name == 'log' }

        assert field != null
        assert isFinal(field.getModifiers())
        assert isStatic(field.getModifiers())
        assert isPublic(field.getModifiers())
        assert isTransient(field.getModifiers())
    }

    @Test
    void testClassAlreadyHasLogField1() {
        shouldFail '''
            @groovy.util.logging.Log4j2
            class MyClass {
                String log
            }
        '''
    }

    @Test
    void testClassAlreadyHasLogField2() {
        shouldFail '''
            @groovy.util.logging.Log4j2('logger')
            class MyClass {
                String logger
            }
        '''
    }

    @Log4j2
    static class MyClassLogInfo {
        def loggingMethod() {
            log.fatal('fatal called')
            log.error('error called')
            log.warn ('warn called')
            log.info ('info called')
            log.debug('debug called')
            log.trace('trace called')
        }
    }

    @Test
    void testLogInfo() {
        MyClassLogInfo.log.addAppender(appender)
        MyClassLogInfo.log.setLevel(Level.ALL)
        new MyClassLogInfo().loggingMethod()

        int ind = 0
        def events = appender.getEvents()
        assert events.size() == 6
        assert events[ind].level == Level.FATAL
        assert events[ind].message == 'fatal called'
        assert events[++ind].level == Level.ERROR
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

    @Log4j2
    static class MyClassLogFromStaticMethods {
        static loggingMethod() {
            log.info('(static) info called')
        }
    }

    @Test
    void testLogFromStaticMethods() {
        MyClassLogFromStaticMethods.log.addAppender(appender)
        MyClassLogFromStaticMethods.log.setLevel(Level.ALL)
        MyClassLogFromStaticMethods.loggingMethod()

        def events = appender.getEvents()
        assert events.size() == 1
        assert events[0].level == Level.INFO
        assert events[0].message == '(static) info called'
    }

    @Log4j2('logger')
    static class MyClassLogInfoForNamedLogger {
        def loggingMethod() {
            logger.fatal('fatal called')
            logger.error('error called')
            logger.warn ('warn called')
            logger.info ('info called')
            logger.debug('debug called')
            logger.trace('trace called')
        }
    }

    @Test
    void testLogInfoForNamedLogger() {
        MyClassLogInfoForNamedLogger.logger.addAppender(appender)
        MyClassLogInfoForNamedLogger.logger.setLevel(Level.ALL)
        new MyClassLogInfoForNamedLogger().loggingMethod()

        int ind = 0
        def events = appender.getEvents()
        assert events.size() == 6
        assert events[ind].level == Level.FATAL
        assert events[ind].message == 'fatal called'
        assert events[++ind].level == Level.ERROR
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

    @Test // TODO check this is actually working, my suspicion is we have two contexts here
    void testLogGuard() {
        Class clazz = new GroovyClassLoader().parseClass('''
            @groovy.util.logging.Log4j2
            class MyClassLogGuard {
                def loggingMethod() {
                    log.setLevel(org.apache.logging.log4j.Level.OFF)
                    log.fatal(prepareLogMessage())
                    log.error(prepareLogMessage())
                    log.warn (prepareLogMessage())
                    log.info (prepareLogMessage())
                    log.debug(prepareLogMessage())
                    log.trace(prepareLogMessage())
                }

                def prepareLogMessage() {
                    log.appenders['MyAppender']?.isLogGuarded = false
                    return 'formatted log message'
                }
            }
        ''')

        clazz.log.addAppender(appender)
        clazz.log.setLevel(Level.ALL)
        clazz.getConstructor().newInstance().loggingMethod()
        assert appender.isLogGuarded
    }

    @Log4j2
    static class MyClassLogGuard {
        def loggingMethod() {
            log.fatal(prepareLogMessage())
            log.error(prepareLogMessage())
            log.warn (prepareLogMessage())
            log.info (prepareLogMessage())
            log.debug(prepareLogMessage())
            log.trace(prepareLogMessage())
        }

        def prepareLogMessage() {
            log.appenders['MyAppender']?.isLogGuarded = false
            println log.privateConfig.intLevel
            println "" + log.isInfoEnabled() + " " + log.isWarnEnabled() + " " + log.isFatalEnabled() + " " + log.isEnabled(Level.TRACE)
            println 'isLogGuarded = false CALLED!!'
            return 'should never see message'
        }
    }

    @Disabled @Test
    void testLogGuard2() {
        MyClassLogGuard.log.addAppender(appender)
        MyClassLogGuard.log.setLevel(Level.OFF)
        new MyClassLogGuard().loggingMethod()
        println appender.isLogGuarded

        // reset
        log.appenders['MyAppender']?.isLogGuarded = true
        MyClassLogGuard.log.setLevel(Level.ALL)
        new MyClassLogGuard().loggingMethod()
        println !appender.isLogGuarded
    }

    @Log4j2
    static class MyClassDefaultCategory {
        static loggingMethod() {
            log.info('info called')
        }
    }

    @Test
    void testDefaultCategory() {
        MyClassDefaultCategory.log.addAppender(appender)
        MyClassDefaultCategory.log.setLevel(Level.ALL)
        MyClassDefaultCategory.loggingMethod()

        assert appender.getEvents().size() == 1
    }

    @Log4j2(category='customCategory')
    static class MyClassCustomCategory {
        static loggingMethod() {
            log.error('error called')
        }
    }

    @Test
    void testCustomCategory() {
        def appenderForCustomCategory = new Log4j2InterceptingAppender('Appender4CustomCategory', null, createLayout())
        def loggerForCustomCategory = LogManager.getLogger('customCategory')
        loggerForCustomCategory.addAppender(appenderForCustomCategory)
        MyClassCustomCategory.loggingMethod()

        assert appenderForCustomCategory.getEvents().size() == 1
        assert appender.getEvents().size() == 0
    }
}
