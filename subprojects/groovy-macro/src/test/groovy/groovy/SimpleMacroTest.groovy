/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.VariableScope
import org.codehaus.groovy.ast.builder.AstAssert
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*

import static org.codehaus.groovy.ast.expr.VariableExpression.*;

/**
 *
 * @author Sergei Egorov <bsideup@gmail.com>
 */
@CompileStatic
class SimpleMacroTest extends GroovyTestCase {
    
    static final String TO_LOWER_CASE_METHOD_NAME = macro { "".toLowerCase() }.getMethodAsString()
    
    public void testMethod() {

        def someVariable = new VariableExpression("someVariable");

        ReturnStatement result = macro {
            return new NonExistingClass($v{someVariable});
        }
        
        def expected = new ReturnStatement(new ConstructorCallExpression(ClassHelper.make("NonExistingClass"), new ArgumentListExpression(someVariable)));

        assertSyntaxTree(expected, result);
    }
    
    public void testAsIs() {
        def expected = new BlockStatement([
                new ExpressionStatement(new MethodCallExpression(THIS_EXPRESSION, "println", new ArgumentListExpression(new ConstantExpression("foo"))))
        ] as List<Statement>, new VariableScope());

        BlockStatement result = macro(true) {
            println "foo"
        }

        assertSyntaxTree(expected, result);
    }

    public void testInception() {

        ConstructorCallExpression result = macro {
            new NonExistingClass($v{macro {someVariable}});
        }

        def expected = new ConstructorCallExpression(ClassHelper.make("NonExistingClass"), new ArgumentListExpression(new VariableExpression("someVariable")));

        assertSyntaxTree(expected, result);
    }
    
    public void testMethodName() {
        // Very useful when you don't want to hardcode method or variable names
        assertEquals("toLowerCase", TO_LOWER_CASE_METHOD_NAME)
        assertEquals("valueOf", macro { String.valueOf() }.getMethodAsString())
    }

    public void testBlock() {

        def result = macro {
            println "foo"
            println "bar"
        }

        def expected = new BlockStatement(
                [
                        new ExpressionStatement(new MethodCallExpression(THIS_EXPRESSION, "println", new ArgumentListExpression(new ConstantExpression("foo")))),
                        new ExpressionStatement(new MethodCallExpression(THIS_EXPRESSION, "println", new ArgumentListExpression(new ConstantExpression("bar")))),
                ] as List<Statement>,
                new VariableScope()
        )

        assertSyntaxTree(expected, result);
    }
    
    protected void assertSyntaxTree(Object expected, Object result) {
        AstAssert.assertSyntaxTree([expected], [result])
    }
}
