package groovy.util.logging

import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * Unit test for the commons logging @Log based annotation.
 *
 * @author Hamlet D'Arcy
 * @author Matthias Cullmann
 * 
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

    public void testPrivateFinalStaticLogFieldAppears() {
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

    public void testPrivateFinalStaticNamedLogFieldAppears() {
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

    public void testClassAlreadyHasLogField() {
        shouldFail {
            Class clazz = new GroovyClassLoader().parseClass('''
                @groovy.util.logging.Commons
                class MyClass {
                    String log
                } ''')

            assert clazz.newInstance()
        }
    }

    public void testClassAlreadyHasNamedLogField() {
        shouldFail {
            Class clazz = new GroovyClassLoader().parseClass('''
                @groovy.util.logging.Commons('logger')
                class MyClass {
                    String logger
                } ''')

            assert clazz.newInstance()
        }
    }

    public void testLogLevelDebug() {
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

    public void testNamedLogger() {
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

    public void testLogGuards() {
        Class clazz = new GroovyClassLoader().parseClass('''
            class LogDecorator extends groovy.util.Proxy {
                boolean isTraceEnabled() { false }
            }

            @groovy.util.logging.Commons
            class MyClass {
                boolean traceCalled = false
                MyClass() {
                    log = new LogDecorator().wrap(log) as org.apache.commons.logging.Log
                }

                def loggingMethod() {
                    log.trace (traceCalled = true)
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

    public void testCustomCategory() {
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
