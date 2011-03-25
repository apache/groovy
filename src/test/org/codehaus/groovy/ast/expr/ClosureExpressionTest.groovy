    package org.codehaus.groovy.ast.expr

import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.syntax.ASTHelper
import org.codehaus.groovy.ast.AstToTextHelper

/**
 * 
 * @author Hamlet D'Arcy
 */
class ClosureExpressionTest extends GroovyTestCase {

    void testGetText_Simple() {

        def ast = new AstBuilder().buildFromString ''' return { it * it } '''
        assert '{ -> ... }' == ast[0].statements[0].expression.text
    }

    void testGetText_Parameter() {

        def ast = new AstBuilder().buildFromString ''' return { x -> x * x } '''
        assert '{ java.lang.Object x -> ... }' == ast[0].statements[0].expression.text
    }

    void testGetText_MultipleParameters() {

        def ast = new AstBuilder().buildFromString ''' return { x, y -> x * y } '''
        assert '{ java.lang.Object x, java.lang.Object y -> ... }' == ast[0].statements[0].expression.text
    }

    void testGetText_TypedParameter() {

        def ast = new AstBuilder().buildFromString ''' return { String x -> x * x } '''
        assert '{ java.lang.String x -> ... }' == ast[0].statements[0].expression.text
    }

    void testGetText_MultipleTypedParameters() {

        def ast = new AstBuilder().buildFromString ''' return { String x, Integer y -> x * y } '''
        assert '{ java.lang.String x, java.lang.Integer y -> ... }' == ast[0].statements[0].expression.text
    }
}
