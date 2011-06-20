package org.codehaus.groovy.ast.expr

import org.codehaus.groovy.ast.builder.AstBuilder

class MapExpressionTest extends GroovyTestCase {

    void testGetText_emptyMap() {
        def expression = buildFromString '[:]'
        assert expression.text == '[:]'
    }

    void testGetText_singleEntry() {
        def expression = buildFromString '[x: 1]'
        assert expression.text == '[x:1]'
    }

    void testGetText_multipleEntries() {
        def expression = buildFromString '[x: 1, y: 2]'
        assert expression.text == '[x:1, y:2]'
    }

    private MapExpression buildFromString(String source) {
        def ast = new AstBuilder().buildFromString(source)
        ast[0].statements[0].expression
    }
}
