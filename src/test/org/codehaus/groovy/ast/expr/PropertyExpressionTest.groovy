package org.codehaus.groovy.ast.expr

/**
 * 
 * @author Hamlet D'Arcy
 */
class PropertyExpressionTest extends GroovyTestCase {

    void testGetText() {
        PropertyExpression property = new PropertyExpression(new VariableExpression('foo'), 'bar')
        assert 'foo.bar' == property.text

        property.safe = true
        property.spreadSafe = false
        assert 'foo?.bar' == property.text

        property.safe = false
        property.spreadSafe = true
        assert 'foo*.bar' == property.text

        property.safe = true
        property.spreadSafe = true
        assert 'foo*?.bar' == property.text
    }
}
