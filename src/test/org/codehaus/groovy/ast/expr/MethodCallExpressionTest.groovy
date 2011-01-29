package org.codehaus.groovy.ast.expr

/**
 * 
 * @author Hamlet D'Arcy
 */
class MethodCallExpressionTest extends GroovyTestCase {

    public void testGetText() {

        MethodCallExpression method = new MethodCallExpression(
                new VariableExpression('foo'),
                'bar',
                new ArgumentListExpression(new ConstantExpression('baz'))
        )

        assert "foo.bar(baz)" == method.text
        method.safe = true
        method.spreadSafe = false
        assert "foo?.bar(baz)" == method.text

        method.safe = false
        method.spreadSafe = true
        assert "foo*.bar(baz)" == method.text

        method.safe = true
        method.spreadSafe = true
        assert "foo*?.bar(baz)" == method.text
    }
}
