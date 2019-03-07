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
import java.util.logging.*
import groovy.mock.interceptor.MockFor
import org.codehaus.groovy.control.MultipleCompilationErrorsException

/**
 * Test to make sure the @Log annotation is working correctly. 
 */
class LogTest extends GroovyTestCase {

    void testPrivateFinalStaticLogFieldAppears() {

        Class clazz = new GroovyClassLoader().parseClass("""
          @groovy.util.logging.Log
          class MyClass {
          } """)

        assert clazz.declaredFields.find { Field field ->
            field.name == "log" &&
                    Modifier.isPrivate(field.getModifiers()) &&
                    Modifier.isStatic(field.getModifiers()) &&
                    Modifier.isTransient(field.getModifiers()) &&
                    Modifier.isFinal(field.getModifiers())
        }
    }

    void testPrivateFinalStaticNamedLogFieldAppears() {

        Class clazz = new GroovyClassLoader().parseClass("""
          @groovy.util.logging.Log('logger')
          class MyClass {
          } """)

        assert clazz.declaredFields.find { Field field ->
            field.name == "logger" &&
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
            @groovy.util.logging.Log
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
            @groovy.util.logging.Log
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
            @groovy.util.logging.Log
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
            @groovy.util.logging.Log
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

            Class clazz = new GroovyClassLoader().parseClass("""
              @groovy.util.logging.Log
              class MyClass {
                  String log
              } """)

            assert clazz.newInstance()
        }
    }

    void testClassAlreadyHasNamedLogField() {

        shouldFail {

            Class clazz = new GroovyClassLoader().parseClass("""
              @groovy.util.logging.Log('logger')
              class MyClass {
                  String logger
              } """)

            assert clazz.newInstance()
        }
    }

    void testLogFromStaticMethods() {
        Class clazz = new GroovyClassLoader().parseClass("""
            @groovy.util.logging.Log
            class MyClass {
                static loggingMethod() {
                  log.info   ("info    called")
                }
            } """)

        def logSpy = new LoggerSpy()
        def logger = new MockFor(Logger)
        logger.demand.getLogger { logSpy }
        logger.use {
            clazz.loggingMethod()
        }

        assert logSpy.infoParameter == 'info    called'
    }

    void testLogInfo() {

        Class clazz = new GroovyClassLoader().parseClass("""
          @groovy.util.logging.Log
          class MyClass {

              def loggingMethod() {
                log.severe ("severe  called")
                log.warning("warning called")
                log.info   ("info    called")
                log.fine   ("fine    called")
                log.finer  ("finer   called")
                log.finest ("finest  called")
              }
          } """)

        def logSpy = new LoggerSpy()
        def logger = new MockFor(Logger)
        logger.demand.getLogger { logSpy }
        logger.use {
            def s = clazz.newInstance()
            s.loggingMethod()
        }

        assert logSpy.severeParameter == 'severe  called'
        assert logSpy.warningParameter == 'warning called'
        assert logSpy.infoParameter == 'info    called'
        assert logSpy.fineParameter == 'fine    called'
        assert logSpy.finerParameter == 'finer   called'
        assert logSpy.finestParameter == 'finest  called'
    }

    void testLogInfoWithName() {

        Class clazz = new GroovyClassLoader().parseClass("""
          @groovy.util.logging.Log('logger')
          class MyClass {

              def loggingMethod() {
                logger.severe ("severe  called")
                logger.warning("warning called")
                logger.info   ("info    called")
                logger.fine   ("fine    called")
                logger.finer  ("finer   called")
                logger.finest ("finest  called")
              }
          }  """)

        def logSpy = new LoggerSpy()
        def logger = new MockFor(Logger)
        logger.demand.getLogger { logSpy }
        logger.use {
            def s = clazz.newInstance()
            s.loggingMethod()
        }

        assert logSpy.severeParameter == 'severe  called'
        assert logSpy.warningParameter == 'warning called'
        assert logSpy.infoParameter == 'info    called'
        assert logSpy.fineParameter == 'fine    called'
        assert logSpy.finerParameter == 'finer   called'
        assert logSpy.finestParameter == 'finest  called'
    }

    void testLogGuard() {
        Class clazz = new GroovyClassLoader().parseClass("""
               @groovy.util.logging.Log
               class MyClass {
                   def loggingMethod() {
                       log.setLevel(java.util.logging.Level.OFF)
                       log.severe(prepareLogMessage())
                       log.warning(prepareLogMessage())
                       log.info   (prepareLogMessage())
                       log.fine   (prepareLogMessage())
                       log.finer  (prepareLogMessage())
                       log.finest (prepareLogMessage())
                   }

                   def prepareLogMessage() {
                     return "formatted log message"
                   }

               }  """)

        def logSpy = new LoggerSpy()
        def logger = new MockFor(Logger)
        logger.demand.getLogger { logSpy }
        logger.use {
            def s = clazz.newInstance()
            s.loggingMethod()
        }

        assert !logSpy.severeParameter
        assert !logSpy.warningParameter
        assert !logSpy.infoParameter
        assert !logSpy.fineParameter
        assert !logSpy.finerParameter
        assert !logSpy.finestParameter
    }

    void testInheritance() {

        def clazz = new GroovyShell().evaluate("""
            class MyParent {
                private log
            }

            @groovy.util.logging.Log
            class MyClass extends MyParent {

                def loggingMethod() {
                    log.severe(prepareLogMessage())
                    log.warning(prepareLogMessage())
                    log.info   (prepareLogMessage())
                    log.fine   (prepareLogMessage())
                    log.finer  (prepareLogMessage())
                    log.finest (prepareLogMessage())
                }
                def prepareLogMessage() {
                    "formatted log message"
                }
            }

            return MyClass
            """)

        assert clazz.declaredFields.find { Field field ->
            field.name == "log" &&
                    Modifier.isPrivate(field.getModifiers()) &&
                    Modifier.isStatic(field.getModifiers()) &&
                    Modifier.isTransient(field.getModifiers()) &&
                    Modifier.isFinal(field.getModifiers())
        }
    }

    void testInheritance_ProtectedShadowing() {

        shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass("""
                class MyParent {
                    protected log
                }

                @groovy.util.logging.Log
                class MyClass extends MyParent {
                } """)
        }
    }

    void testInheritance_PublicShadowing() {

        shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass("""
                class MyParent {
                    public log
                }

                @groovy.util.logging.Log
                class MyClass extends MyParent {
                } """)
        }
    }

    void testDefaultCategory() {
        Class clazz = new GroovyClassLoader().parseClass("""
            @groovy.util.logging.Log
            class MyClass {
                static loggingMethod() {
                  log.info("info called")
                }
            }""")
        LogFormatterSpy logFormatterSpy = registerLogFormatterSpy('MyClass')

        clazz.newInstance().loggingMethod()

        assert logFormatterSpy.messageReceived
    }

    void testCustomCategory() {
        String categoryName = 'customCategory'
        Class clazz = new GroovyClassLoader().parseClass("""
            @groovy.util.logging.Log(category='$categoryName')
            class MyClass {
                static loggingMethod() {
                  log.info("info called")
                }
            }""")
        LogFormatterSpy logFormatterSpy = registerLogFormatterSpy(categoryName)

        clazz.newInstance().loggingMethod()

        assert logFormatterSpy.messageReceived
    }

    private LogFormatterSpy registerLogFormatterSpy(String loggerName) {
        Logger logger = Logger.getLogger(loggerName)
        ConsoleHandler handler = new ConsoleHandler()
        LogFormatterSpy loggerNameSpy = new LogFormatterSpy()
        handler.setFormatter(loggerNameSpy)
        logger.addHandler(handler)
        return loggerNameSpy
    }
}

@groovy.transform.PackageScope
class LoggerSpy extends Logger {

    String severeParameter = null
    String warningParameter = null
    String infoParameter = null
    String fineParameter = null
    String finerParameter = null
    String finestParameter = null

    LoggerSpy() {
        super(null, null)
    }

    @Override
    void severe(String s) {
        if (severeParameter) throw new AssertionError("Severe already called once with parameter $severeParameter")
        severeParameter = s
    }

    @Override
    void warning(String s) {
        if (warningParameter) throw new AssertionError("Warning already called once with parameter $warningParameter")
        warningParameter = s
    }

    @Override
    void info(String s) {
        if (infoParameter) throw new AssertionError("Info already called once with parameter $infoParameter")
        infoParameter = s
    }

    @Override
    void fine(String s) {
        if (fineParameter) throw new AssertionError("Fine already called once with parameter $fineParameter")
        fineParameter = s
    }

    @Override
    void finer(String s) {
        if (finerParameter) throw new AssertionError("Finer already called once with parameter $finerParameter")
        finerParameter = s
    }

    @Override
    void finest(String s) {
        if (finestParameter) throw new AssertionError("Finest already called once with parameter $finestParameter")
        finestParameter = s
    }
}

class LogFormatterSpy extends Formatter {

    boolean messageReceived = false

    @Override
    String format(LogRecord record) {
        messageReceived = true
        return record.message
    }
}