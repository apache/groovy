package groovy.util.logging

import java.lang.reflect.*
import java.util.logging.*
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.control.*
import org.codehaus.groovy.tools.ast.*
import org.codehaus.groovy.transform.*

/**
 * @author Guillaume Laforge
 * @author Jochen Theodorou
 * @author Dinko Srkoc
 * @author Hamlet D'Arcy
 * @author Raffaele Cigni
 * @author Alberto Vilches Raton
 */
class LogASTTransformTest extends GroovyTestCase {

    def logObserver = new LoggingObserver()

    protected void setUp() {

        super.setUp();
        Logger logger = Logger.getLogger("MyClass")
        logger.setLevel Level.FINEST
        logger.addHandler logObserver

    }

    protected void tearDown() {
        super.tearDown();
        def logger = Logger.getLogger("MyClass")
        logger.removeHandler logObserver
    }


    public void testPrivateFinalStaticLogFieldAppears() {

        Class clazz = createAndTransformClass("""
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

    public void testClassAlreadyHasLogField() {

        shouldFail {

            Class clazz = createAndTransformClass("""
                @groovy.util.logging.Log
                class MyClass {
                    String log
                } """)

            assert clazz.newInstance()
        }
    }


    public void testLogInfo() {

        Class clazz = createAndTransformClass("""
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
            }
            new MyClass().loggingMethod() """)

        Script s = (Script) clazz.newInstance()
        s.run()

        assert logObserver.entries.size() == 6

        assert logObserver.entries[0].message == "severe  called"
        assert logObserver.entries[1].message == "warning called"
        assert logObserver.entries[2].message == "info    called"
        assert logObserver.entries[3].message == "fine    called"
        assert logObserver.entries[4].message == "finer   called"
        assert logObserver.entries[5].message == "finest  called"

        assert logObserver.entries[0].level == Level.SEVERE
        assert logObserver.entries[1].level == Level.WARNING
        assert logObserver.entries[2].level == Level.INFO
        assert logObserver.entries[3].level == Level.FINE
        assert logObserver.entries[4].level == Level.FINER 
        assert logObserver.entries[5].level == Level.FINEST
    }

    private Class createAndTransformClass(String code) {

        def transform = new ASTTransformation() {
            void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
            }
        }
        def phase = CompilePhase.CANONICALIZATION
        def invoker = new TransformTestHelper(transform, phase)
        Class clazz = invoker.parse(code)
        clazz
    }

    class LoggingObserver extends Handler {

        def entries = []

        void publish(LogRecord record) {
            entries << record
        }

        void flush() {
        }

        void close() {
        }
    }


}
