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
            events.add([level: ev.level, message: ev.message.formattedMessage,
                        source: ev.source, marker: ev.marker, thrown: ev.thrown])
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

    // GROOVY-12378 -------------------------------------------------------------

    /** line number (1-based) of the first line of {@code source} containing {@code needle} */
    private static int lineOf(String source, String needle) {
        int idx = source.readLines().findIndexOf { it.contains(needle) }
        assert idx >= 0 : "no line contains $needle"
        idx + 1
    }

    /** each test compiles its own class: Log4j2 loggers are cached by name, and a
     *  second appender registered under an existing name is ignored */
    private static String staticLocationSource(String className) { '''
        @groovy.util.logging.Log4j2(staticLocation = true)
        class CLASSNAME {
            static int evaluations = 0
            static String expensive() { evaluations++; 'expensive' }

            def instanceMethod() {
                log.info('plain')                       // L1 simple arguments: no guard
                log.warn("interpolated ${expensive()}") // L2 guarded
                [1].each {
                    log.error('from closure')            // L3 inside a closure
                }
            }
            static void staticMethod() {
                log.debug('static {}', 42)               // L4 parameterised
            }
            def withThrowable() {
                try { throw new IllegalStateException('boom') } catch (e) { log.error('failed', e) }
            }
            def withMarker(org.apache.logging.log4j.Marker m) {
                log.info(m, 'marked {}', 'x')
            }
            def withMarkerAndThrowable(org.apache.logging.log4j.Marker m, Throwable t) {
                log.warn(m, 'both {}', 'y', t)
            }
        }
    '''.replace('CLASSNAME', className) }

    @Test
    void testStaticLocationSuppliesCompileTimeLocations() {
        String source = staticLocationSource('LocatedA')
        Class clazz = new GroovyClassLoader().parseClass(source, 'LocatedA.groovy')
        clazz.log.addAppender(appender)
        clazz.log.setLevel(Level.ALL)
        clazz.newInstance().instanceMethod()
        clazz.staticMethod()

        def events = appender.events
        assert events*.message == ['plain', 'interpolated expensive', 'from closure', 'static 42']
        assert events*.source*.className == ['LocatedA'] * 4
        assert events*.source*.fileName == ['LocatedA.groovy'] * 4
        assert events*.source*.methodName == ['instanceMethod', 'instanceMethod', 'instanceMethod', 'staticMethod']
        assert events*.source*.lineNumber == ['L1', 'L2', 'L3', 'L4'].collect { lineOf(source, it) }

        // one synthetic static final field per logging statement
        def locations = clazz.declaredFields.findAll { it.name.startsWith('$log$loc$') }
        assert locations.size() == 7
        assert locations.every { isStatic(it.modifiers) && isFinal(it.modifiers) && it.synthetic && it.type == StackTraceElement }
    }

    @Test
    void testStaticLocationKeepsGuardForNonSimpleArguments() {
        Class clazz = new GroovyClassLoader().parseClass(staticLocationSource('LocatedB'), 'LocatedB.groovy')
        clazz.log.addAppender(appender)
        clazz.log.setLevel(Level.ERROR)
        clazz.newInstance().instanceMethod()

        assert clazz.evaluations == 0 : 'a disabled level must not evaluate the interpolated argument'
        assert appender.events*.message == ['from closure']
    }

    @Test
    void testStaticLocationThrowableAndMarker() {
        Class clazz = new GroovyClassLoader().parseClass(staticLocationSource('LocatedC'), 'LocatedC.groovy')
        clazz.log.addAppender(appender)
        clazz.log.setLevel(Level.ALL)
        def marker = org.apache.logging.log4j.MarkerManager.getMarker('GROOVY12378')
        def instance = clazz.newInstance()
        instance.withThrowable()
        instance.withMarker(marker)
        instance.withMarkerAndThrowable(marker, new IllegalArgumentException('bad'))

        def events = appender.events
        assert events.size() == 3
        assert events[0].message == 'failed'
        assert events[0].thrown instanceof IllegalStateException
        assert events[0].source.methodName == 'withThrowable'
        assert events[1].message == 'marked x'
        assert events[1].marker == marker
        assert events[1].thrown == null
        assert events[1].source.methodName == 'withMarker'
        assert events[2].message == 'both y'
        assert events[2].marker == marker
        assert events[2].thrown instanceof IllegalArgumentException
        assert events[2].source.methodName == 'withMarkerAndThrowable'
    }

    @Test
    void testStaticLocationWithCompileStatic() {
        Class clazz = new GroovyClassLoader().parseClass('''
            @groovy.transform.CompileStatic
            @groovy.util.logging.Log4j2(staticLocation = true)
            class LocatedStatic {
                void run(String who) {
                    log.info("hello $who")
                    log.warn('plain')
                }
            }
        ''', 'LocatedStatic.groovy')
        clazz.log.addAppender(appender)
        clazz.log.setLevel(Level.ALL)
        clazz.newInstance().run('world')

        def events = appender.events
        assert events*.message == ['hello world', 'plain']
        assert events*.source*.className == ['LocatedStatic', 'LocatedStatic']
        assert events*.source*.methodName == ['run', 'run']
        assert events*.source*.lineNumber == [6, 7]
    }

    @Test
    void testStaticLocationOffLeavesCallsAlone() {
        Class clazz = new GroovyClassLoader().parseClass('''
            @groovy.util.logging.Log4j2
            class NotLocated {
                def run() { log.info('plain') }
            }
        ''', 'NotLocated.groovy')
        assert !clazz.declaredFields.any { it.name.startsWith('$log$loc$') }
    }

    @Test
    void testStaticLocationRejectedByStrategyWithoutSupport() {
        // a Log-family annotation whose strategy does not support compile-time locations
        def err = shouldFail(org.codehaus.groovy.control.MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                @groovy.util.logging.Log4j2Test.NoLocationLog(staticLocation = true)
                class Unsupported {
                    def run() { log.info('plain') }
                }
            ''')
        }
        assert err.message.contains('staticLocation is not supported by ' + NoLocationStrategy.name + ': locations are looked up at run time here')
    }

    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.SOURCE)
    @java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE)
    @org.codehaus.groovy.transform.GroovyASTTransformationClass('org.codehaus.groovy.transform.LogASTTransformation')
    static @interface NoLocationLog {
        String value() default 'log'
        String category() default org.codehaus.groovy.transform.LogASTTransformation.DEFAULT_CATEGORY_NAME
        String visibilityId() default groovy.transform.Undefined.STRING
        Class<? extends org.codehaus.groovy.transform.LogASTTransformation.LoggingStrategy> loggingStrategy() default NoLocationStrategy
        boolean staticLocation() default false
    }

    static class NoLocationStrategy extends Log4j2.Log4j2LoggingStrategy {
        NoLocationStrategy(GroovyClassLoader loader) { super(loader) }
        @Override String staticLocationUnsupportedReason() { 'locations are looked up at run time here' }
    }
}
