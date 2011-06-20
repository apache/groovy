package org.codehaus.groovy.ast.expr

import org.codehaus.groovy.ast.builder.AstBuilder

class MapExpressionTest extends GroovyTestCase {

    void testGetText_emptyMap() {
        def expression = buildExpressionFromString '[:]'
        assert expression.text == '[:]'
    }

    void testGetText_singleEntry() {
        def expression = buildExpressionFromString '[x: 1]'
        assert expression.text == '[x:1]'
    }

    void testGetText_multipleEntries() {
        def expression = buildExpressionFromString '[x: 1, y: 2]'
        assert expression.text == '[x:1, y:2]'
    }

    private MapExpression buildExpressionFromString(String source) {
        def ast = new AstBuilder().buildFromString(source)
        ast[0].statements[0].expression
    }
}
