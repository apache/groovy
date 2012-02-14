package groovy.util.logging

import java.lang.reflect.*
import java.util.logging.*
import groovy.mock.interceptor.MockFor
import org.junit.Assert
import org.codehaus.groovy.control.MultipleCompilationErrorsException

/**
 * Test to make sure the @Log annotation is working correctly. 
 *
 * @author Guillaume Laforge
 * @author Jochen Theodorou
 * @author Dinko Srkoc
 * @author Hamlet D'Arcy
 * @author Raffaele Cigni
 * @author Alberto Vilches Raton
 * @author Tomasz Bujok
 */
class LogTest extends GroovyTestCase {

    public void testPrivateFinalStaticLogFieldAppears() {

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

    public void testPrivateFinalStaticNamedLogFieldAppears() {

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

    public void testClassAlreadyHasLogField() {

        shouldFail {

            Class clazz = new GroovyClassLoader().parseClass("""
              @groovy.util.logging.Log
              class MyClass {
                  String log
              } """)

            assert clazz.newInstance()
        }
    }

    public void testClassAlreadyHasNamedLogField() {

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

    public void testLogInfo() {

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

    public void testLogInfoWithName() {

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

    public void testLogGuard() {
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

    public void testInheritance() {

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

    public void testInheritance_ProtectedShadowing() {

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

    public void testInheritance_PublicShadowing() {

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

}

@groovy.transform.PackageScope class LoggerSpy extends Logger {

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