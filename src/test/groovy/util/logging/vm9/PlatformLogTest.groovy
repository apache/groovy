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
package groovy.util.logging.vm9

import groovy.test.GroovyTestCase
import groovy.util.logging.PlatformLog
import org.codehaus.groovy.control.MultipleCompilationErrorsException

import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * Test to make sure the @Log annotation is working correctly. 
 */
class PlatformLogTest extends GroovyTestCase {
    void testPrivateFinalStaticLogFieldAppears() {
        Class clazz = new GroovyClassLoader().parseClass('''
            @groovy.util.logging.PlatformLog
            class MyClassPrivateFinalStaticLogFieldAppears { }
        ''')

        assert clazz.declaredFields.find { Field field ->
            field.name == 'log' &&
                    Modifier.isPrivate(field.getModifiers()) &&
                    Modifier.isStatic(field.getModifiers()) &&
                    Modifier.isTransient(field.getModifiers()) &&
                    Modifier.isFinal(field.getModifiers())
        }
    }

    void testPrivateFinalStaticNamedLogFieldAppears() {
        Class clazz = new GroovyClassLoader().parseClass('''
            @groovy.util.logging.PlatformLog('logger')
            class MyClassPrivateFinalStaticNamedLogFieldAppears { }
        ''')

        assert clazz.declaredFields.find { Field field ->
            field.name == 'logger' &&
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
            @groovy.util.logging.PlatformLog
            class MyClassExplicitPrivateFinalStaticLogFieldAppears { }
        ''')

        assert clazz.declaredFields.find { Field field ->
            field.name == 'log' &&
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
            @groovy.util.logging.PlatformLog
            class MyClassPackagePrivateFinalStaticLogFieldAppears { }
        ''')

        assert clazz.declaredFields.find { Field field ->
            field.name == 'log' &&
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
            @groovy.util.logging.PlatformLog
            class MyClassProtectedFinalStaticLogFieldAppears { }
        ''')

        assert clazz.declaredFields.find { Field field ->
            field.name == 'log' &&
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
            @groovy.util.logging.PlatformLog
            class MyClassPublicFinalStaticLogFieldAppears { }
        ''')

        assert clazz.declaredFields.find { Field field ->
            field.name == 'log' &&
                    Modifier.isPublic(field.getModifiers()) &&
                    Modifier.isStatic(field.getModifiers()) &&
                    Modifier.isTransient(field.getModifiers()) &&
                    Modifier.isFinal(field.getModifiers())
        }
    }

    void testClassAlreadyHasLogField() {
        def msg = shouldFail {
            Class clazz = new GroovyClassLoader().parseClass('''
                @groovy.util.logging.PlatformLog
                class MyClassAlreadyHasLogField {
                    String log
                }
            ''')

            assert clazz.getConstructor().newInstance()
        }
        assert msg.contains('cannot have log field declared')
    }

    void testClassAlreadyHasNamedLogField() {
        def msg = shouldFail {
            Class clazz = new GroovyClassLoader().parseClass('''
                @groovy.util.logging.PlatformLog('logger')
                class MyClassAlreadyHasNamedLogField {
                    String logger
                }
            ''')
            assert clazz.getConstructor().newInstance()
        }
        assert msg.contains('cannot have log field declared')
    }

    @PlatformLog
    class MyClassLogFromStaticMethods {
        static loggingMethod() {
            log.info ('info    called')
        }
    }

    void testLogFromStaticMethods() {
        MyClassLogFromStaticMethods.loggingMethod()
        def finder = System.LoggerFinder.getLoggerFinder()
        assert finder instanceof LoggerSpyFinder
        def logSpy = finder.spy
        assert logSpy.infoParameter    == 'info    called'
    }

    void testLogInfo() {
        Class clazz = new GroovyClassLoader().parseClass('''
            @groovy.util.logging.PlatformLog
            class MyClassLogInfo {
                def loggingMethod() {
                    log.error('error   called')
                    log.warn ('warning called')
                    log.info ('info    called')
                    log.debug('debug   called')
                    log.trace('trace   called')
                }
            }
        ''')
        def s = clazz.getConstructor().newInstance()
        s.loggingMethod()
        def finder = System.LoggerFinder.getLoggerFinder()
        assert finder instanceof LoggerSpyFinder
        def logSpy = finder.spy
        assert logSpy.warningParameter == 'warning called'
        assert logSpy.infoParameter    == 'info    called'
        assert logSpy.debugParameter   == 'debug   called'
        assert logSpy.traceParameter   == 'trace   called'
        assert logSpy.errorParameter   == 'error   called'
        logSpy.reset()
    }

    @PlatformLog('logger')
    static class MyClassLogInfoWithName {
        def loggingMethod() {
            logger.error('error   called')
            logger.warn ('warning called')
            logger.info ('info    called')
            logger.debug('debug   called')
            logger.trace('trace   called')
        }
    }

    void testLogInfoWithName() {
        new MyClassLogInfoWithName().loggingMethod()
        def finder = System.LoggerFinder.getLoggerFinder()
        assert finder instanceof LoggerSpyFinder
        def logSpy = finder.spy
        assert logSpy.warningParameter == 'warning called'
        assert logSpy.infoParameter    == 'info    called'
        assert logSpy.debugParameter   == 'debug   called'
        assert logSpy.traceParameter   == 'trace   called'
        assert logSpy.errorParameter   == 'error   called'
    }

    void testInheritancePrivateNoShadowingIssue() {
        def clazz = new GroovyShell().evaluate('''
            class MyParentTestInheritance {
                private log
            }

            @groovy.util.logging.PlatformLog
            class MyClassTestInheritance extends MyParentTestInheritance {
                def loggingMethod() {
                    log.info   (prepareLogMessage())
                }
                def prepareLogMessage() {
                    'formatted log message'
                }
            }

            return MyClassTestInheritance
        ''')

        assert clazz.declaredFields.find { Field field ->
            field.name == 'log' &&
                    Modifier.isPrivate(field.getModifiers()) &&
                    Modifier.isStatic(field.getModifiers()) &&
                    Modifier.isTransient(field.getModifiers()) &&
                    Modifier.isFinal(field.getModifiers())
        }
    }

    void testInheritanceProtectedShadowing() {
        def msg = shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                class MyParentProtectedShadowing {
                    protected log
                }

                @groovy.util.logging.PlatformLog
                class MyClassProtectedShadowing extends MyParentProtectedShadowing { }
            ''')
        }
        assert msg.contains('cannot have log field declared because the field exists in the parent class')
    }

    void testInheritancePublicShadowing() {
        def msg = shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                class MyParentPublicShadowing {
                    public log
                }

                @groovy.util.logging.PlatformLog
                class MyClassPublicShadowing extends MyParentPublicShadowing {
                }
            ''')
        }
        assert msg.contains('cannot have log field declared because the field exists in the parent class')
    }
}

