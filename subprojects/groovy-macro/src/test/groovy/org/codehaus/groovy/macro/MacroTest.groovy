/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.macro

import groovy.transform.CompileStatic
import org.codehaus.groovy.runtime.metaclass.MethodSelectionException

/**
 *
 * @author Sergei Egorov <bsideup@gmail.com>
 */
@CompileStatic
class MacroTest extends GroovyTestCase {

    void testSimpleCase() {
        assertScript '''
        import org.codehaus.groovy.ast.expr.*;
        import org.codehaus.groovy.ast.stmt.*;
        import org.codehaus.groovy.ast.ClassHelper;
        import org.codehaus.groovy.ast.builder.AstAssert;

        import static org.codehaus.groovy.ast.tools.GeneralUtils.*;

        def someVariable = new VariableExpression("someVariable");

        ReturnStatement result = macro {
            return new NonExistingClass($v{someVariable});
        }

        def expected = returnS(ctorX(ClassHelper.make("NonExistingClass"), args(someVariable)))

        AstAssert.assertSyntaxTree([expected], [result]);
'''
    }

    void testAsIs() {
        assertScript '''
        import org.codehaus.groovy.ast.expr.*;
        import org.codehaus.groovy.ast.stmt.*;
        import org.codehaus.groovy.ast.ClassHelper;
        import org.codehaus.groovy.ast.builder.AstAssert;

        import static org.codehaus.groovy.ast.tools.GeneralUtils.*;

        BlockStatement result = macro(true) {
            println "foo"
        }

        def expected = block(stmt(callThisX("println", args(constX("foo")))))

        AstAssert.assertSyntaxTree([expected], [result]);
'''
    }

    void testInception() {
        assertScript '''
        import org.codehaus.groovy.ast.expr.*;
        import org.codehaus.groovy.ast.stmt.*;
        import org.codehaus.groovy.ast.ClassHelper;
        import org.codehaus.groovy.ast.builder.AstAssert;

        import static org.codehaus.groovy.ast.tools.GeneralUtils.*;

        ConstructorCallExpression result = macro {
            new NonExistingClass($v{macro {someVariable}});
        }

        def expected = ctorX(ClassHelper.make("NonExistingClass"), args(varX("someVariable")))

        AstAssert.assertSyntaxTree([expected], [result]);
'''
    }

    void testMethodNameFromCode() {
        assertScript '''
        // Very useful when you don't want to hardcode method or variable names
        assert "toLowerCase" == macro { "".toLowerCase() }.getMethodAsString()
        assert "valueOf" == macro { String.valueOf() }.getMethodAsString()
'''
    }

    void testBlock() {
        assertScript '''
        import org.codehaus.groovy.ast.expr.*;
        import org.codehaus.groovy.ast.stmt.*;
        import org.codehaus.groovy.ast.ClassHelper;
        import org.codehaus.groovy.ast.builder.AstAssert;

        import static org.codehaus.groovy.ast.tools.GeneralUtils.*;

        def result = macro {
            println "foo"
            println "bar"
        }

        def expected = block(
            stmt(callThisX("println", args(constX("foo")))),
            stmt(callThisX("println", args(constX("bar")))),
        )

        AstAssert.assertSyntaxTree([expected], [result]);
'''
    }

    void testCompilePhase() {
        assertScript '''
        import org.codehaus.groovy.ast.builder.AstAssert
        import org.codehaus.groovy.control.CompilePhase

        import static org.codehaus.groovy.ast.tools.GeneralUtils.*

        def result = macro(CompilePhase.CLASS_GENERATION) {
            println "foo"
            println "bar"
        }

        def expected = block(
            stmt(callThisX("println", args(constX("foo")))),
            // by end of CLASS_GENERATION phase last println will be ReturnStatement
            returnS(callThisX("println", args(constX("bar")))),
        )

        AstAssert.assertSyntaxTree([expected], [result])
        '''
    }

    void testAsIsWithCompilePhase() {
        assertScript '''
        import org.codehaus.groovy.ast.builder.AstAssert
        import org.codehaus.groovy.control.CompilePhase

        import static org.codehaus.groovy.ast.tools.GeneralUtils.*

        def result = macro(CompilePhase.CLASS_GENERATION, true) {
            println "foo"
        }

        def expected = block(
            returnS(callThisX("println", args(constX("foo"))))
        )

        AstAssert.assertSyntaxTree([expected], [result])
        '''
    }

    void testCompileStatic() {
        assertScript '''
        import groovy.transform.CompileStatic
        import org.codehaus.groovy.ast.stmt.ReturnStatement;
        import org.codehaus.groovy.ast.ClassHelper;
        import org.codehaus.groovy.ast.builder.AstAssert;

        import static org.codehaus.groovy.ast.tools.GeneralUtils.*;

        @CompileStatic
        ReturnStatement getReturnStatement() {
            return macro {
                return new NonExistingClass("foo");
            }
        }

        def expected = returnS(ctorX(ClassHelper.make("NonExistingClass"), args(constX("foo"))))

        AstAssert.assertSyntaxTree([expected], [getReturnStatement()]);
'''
    }

    void testMultipleSubstitutions() {
        assertScript '''
        import org.codehaus.groovy.ast.expr.*;
        import org.codehaus.groovy.ast.stmt.*;
        import org.codehaus.groovy.ast.ClassHelper;
        import org.codehaus.groovy.ast.builder.AstAssert;

        import static org.codehaus.groovy.ast.tools.GeneralUtils.*;

        def var1 = new VariableExpression("a")
        def var2 = new VariableExpression("b")
        def var3 = new VariableExpression("c")

        ReturnStatement result = macro {
            return new NonExistingClass($v{var1}, $v{var2}) + $v{var3}
        }

        def expected = returnS(plusX(ctorX(ClassHelper.make("NonExistingClass"), args(var1, var2)), var3))

        AstAssert.assertSyntaxTree([expected], [result])
'''
    }

    void testMacroClass() {
        assertScript '''
        import org.codehaus.groovy.ast.ClassNode
        import org.codehaus.groovy.macro.transform.MacroClass

        def ast1 = new MacroClass() {
            class A { String str }
        }

        assert ast1.getClass() == ClassNode
        assert ast1.name == "A"
        assert ast1.getField("str") != null
'''
    }

    void testNotAMacroCall() {
        // FIXME should fail with "MissingMethodException" because none of MacroGroovy extension methods
        // are defined with "no-args" version
        shouldFail(MethodSelectionException) {
            assertScript 'macro()'
        }
    }
}
