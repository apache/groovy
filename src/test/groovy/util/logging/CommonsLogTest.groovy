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
class CommonsLogTest extends GroovyTestCase {

    public void testPrivateFinalStaticLogFieldAppears() {

        Class clazz = new GroovyClassLoader().parseClass('''
              @groovy.util.logging.CommonsLog
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

    public void testClassAlreadyHasLogField() {

        shouldFail {

            Class clazz = new GroovyClassLoader().parseClass('''
                @groovy.util.logging.CommonsLog
                class MyClass {
                    String log
                } ''')

            assert clazz.newInstance()
        }
    }

    /**
     * This test output must be observed manually.
     * There is unfortunately no good way to add an appender to Commons Logging. 
     */
    public void testLogInfo_IntegrationTest() {

        Class clazz = new GroovyClassLoader().parseClass('''
            @groovy.util.logging.CommonsLog
            class MyClass {

                def loggingMethod() {
                    log.error ("error called")
                    log.warn  ("warn called")
                    log.info  ("info called")
                    log.debug ("debug called")
                }
            }
            new MyClass().loggingMethod() ''')

        Script s = (Script) clazz.newInstance()
        s.run()
    }

    public void testLogGuards() {
        Class clazz = new GroovyClassLoader().parseClass('''
            def traceCalled = false
            @groovy.util.logging.CommonsLog
            class MyClass {

                def loggingMethod() {
                    log.trace (traceCalled = true)
                }
            }
            new MyClass().loggingMethod()
            return traceCalled''')

        Script s = (Script) clazz.newInstance()
        def result = s.run()
        assert !result
    }
}
