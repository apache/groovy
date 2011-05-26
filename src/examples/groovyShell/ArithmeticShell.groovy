import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.customizers.SecureASTCustomizer
import org.codehaus.groovy.control.messages.ExceptionMessage
import static org.codehaus.groovy.syntax.Types.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.UnaryMinusExpression
import org.codehaus.groovy.ast.expr.UnaryPlusExpression
import org.codehaus.groovy.ast.expr.PrefixExpression
import org.codehaus.groovy.ast.expr.PostfixExpression
import org.codehaus.groovy.ast.expr.TernaryExpression
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.ClassExpression

/**
 * The arithmetic shell is similar to a GroovyShell in that it can evaluate text as
 * code and return a result. It is not a subclass of GroovyShell because it does not widen
 * the contract of GroovyShell, instead it narrows it. Using one of these shells like a
 * GroovyShell would result in many runtime errors.
 *
 * @author Hamlet D'Arcy (hamletdrc@gmail.com)
 * @author Guillaume Laforge
 */
class ArithmeticShell {

    /**
     * Compiles the text into a Groovy object and then executes it, returning the result.
     * @param text
     *       the script to evaluate typed as a string
     * @throws SecurityException
     *       most likely the script is doing something other than arithmetic
     * @throws IllegalStateException
     *       if the script returns something other than a number
     */
    Number evaluate(String text) {
        try {
            final ImportCustomizer imports = new ImportCustomizer().addStaticStars('java.lang.Math') // add static import of java.lang.Math
            final SecureASTCustomizer secure = new SecureASTCustomizer()
            secure.with {
                closuresAllowed = false
                methodDefinitionAllowed = false
                
                importsWhitelist = []
                staticImportsWhitelist = []
                staticStarImportsWhitelist = ['java.lang.Math'] // only java.lang.Math is allowed

                tokensWhitelist = [
                        PLUS,
                        MINUS,
                        MULTIPLY,
                        DIVIDE,
                        MOD,
                        POWER,
                        PLUS_PLUS,
                        MINUS_MINUS,
                        COMPARE_EQUAL,
                        COMPARE_NOT_EQUAL,
                        COMPARE_LESS_THAN,
                        COMPARE_LESS_THAN_EQUAL,
                        COMPARE_GREATER_THAN,
                        COMPARE_GREATER_THAN_EQUAL,
                ].asImmutable()

                constantTypesClassesWhiteList = [
                        Integer,
                        Float,
                        Long,
                        Double,
                        BigDecimal,
                        Integer.TYPE,
                        Long.TYPE,
                        Float.TYPE,
                        Double.TYPE
                ].asImmutable()

                receiversClassesWhiteList = [
                        Math,
                        Integer,
                        Float,
                        Double,
                        Long,
                        BigDecimal
                ].asImmutable()

                statementsWhitelist = [
                        BlockStatement,
                        ExpressionStatement
                ].asImmutable()

                expressionsWhitelist = [
                        BinaryExpression,
                        ConstantExpression,
                        MethodCallExpression,
                        StaticMethodCallExpression,
                        ArgumentListExpression,
                        PropertyExpression,
                        UnaryMinusExpression,
                        UnaryPlusExpression,
                        PrefixExpression,
                        PostfixExpression,
                        TernaryExpression,
                        ElvisOperatorExpression,
                        BooleanExpression,
                        // ClassExpression needed for processing of MethodCallExpression, PropertyExpression
                        // and StaticMethodCallExpression
                  ClassExpression
                ].asImmutable()

            }
            CompilerConfiguration config = new CompilerConfiguration()
            config.addCompilationCustomizers(imports, secure)
            GroovyClassLoader loader = new GroovyClassLoader(this.class.classLoader, config)
            Class clazz = loader.parseClass(text)
            Script script = (Script) clazz.newInstance();
            Object result = script.run()
            if (!(result instanceof Number)) throw new IllegalStateException("Script returned a non-number: $result");
            return (Number) result
        } catch (SecurityException ex) {
            throw new SecurityException("Could not evaluate script: $text", ex)
        } catch (MultipleCompilationErrorsException mce) {
            //this allows compilation errors to be seen by the user       
            mce.errorCollector.errors.each {
                if (it instanceof ExceptionMessage && it.cause instanceof SecurityException) {
                    throw it.cause
                }
            }
            throw mce
        }
    }
}
