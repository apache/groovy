package groovy.transform.stc

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import groovy.transform.StaticTypes
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import junit.framework.AssertionFailedError
import org.codehaus.groovy.control.ErrorCollector

/**
 * Support class for static type checking test cases.
 *
 * @author Cedric Champeau
 */
abstract class StaticTypeCheckingTestCase extends GroovyTestCase {
    protected CompilerConfiguration config
    protected GroovyShell shell

    @Override
    protected void setUp() {
        super.setUp()
        config = new CompilerConfiguration()
        config.addCompilationCustomizers(new ASTTransformationCustomizer(StaticTypes))
        shell = new GroovyShell(config)
    }

    @Override
    protected void tearDown() {
        super.tearDown()
        shell = null
        config = null
    }

    @Override
    protected void assertScript(String script) {
        shell.evaluate(script, getTestClassName())
    }

    protected void assertClass(String classCode) {
        GroovyClassLoader loader = new GroovyClassLoader(this.class.classLoader, config)
        loader.parseClass(classCode)
    }

    protected void shouldFailWithMessages(final String code, final String... messages) {
        boolean success = false
        try {
            shell.evaluate(code, getTestClassName())
        } catch (MultipleCompilationErrorsException mce) {
            mce.errorCollector.errors.each {
                messages.each { message ->
                    success = success || (it instanceof SyntaxErrorMessage && it.cause.message.contains(message))
                }
            }
            if (!success) throw mce;
            if (success && mce.errorCollector.errorCount!=messages.length) {
                throw new AssertionError("Expected error message was found, but compiler thrown more than one error : "+mce.toString())
            }
        }
        if (!success) throw new AssertionError("Test should have failed with message [$message]")
    }

}
