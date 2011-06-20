package org.codehaus.groovy.ast.expr

import org.codehaus.groovy.ast.builder.AstBuilder

/**
 * 
 * @author Hamlet D'Arcy
 */
class ClosureExpressionTest extends GroovyTestCase {

    void testGetText_Simple() {
        def expression = buildFromString 'return { it * it }'
        assert expression.text == '{ -> ... }'
    }

    void testGetText_Parameter() {
        def expression = buildFromString 'return { x -> x * x }'
        assert expression.text == '{ java.lang.Object x -> ... }'
    }

    void testGetText_MultipleParameters() {
        def expression = buildFromString 'return { x, y -> x * y }'
        assert expression.text == '{ java.lang.Object x, java.lang.Object y -> ... }'
    }

    void testGetText_TypedParameter() {
        def expression = buildFromString 'return { String x -> x * x }'
        assert expression.text == '{ java.lang.String x -> ... }'
    }

    void testGetText_MultipleTypedParameters() {
        def expression = buildFromString 'return { String x, Integer y -> x * y }'
        assert expression.text == '{ java.lang.String x, java.lang.Integer y -> ... }'
    }

    private Expression buildFromString(String source) {
        def ast = new AstBuilder().buildFromString(source)
        ast[0].statements[0].expression
    }
}
