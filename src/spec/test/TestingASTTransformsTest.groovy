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
import groovy.test.GroovyTestCase

class TestingASTTransformsTest extends GroovyTestCase {

    void testNotYetImplemented() {
        assertScript '''// tag::notyetimplemented[]
import groovy.test.GroovyTestCase
import groovy.test.NotYetImplemented

class Maths {
    static int fib(int n) {
        // todo: implement later
    }
}

class MathsTest extends GroovyTestCase {
    @NotYetImplemented
    void testFib() {
        def dataTable = [
                1:1,
                2:1,
                3:2,
                4:3,
                5:5,
                6:8,
                7:13
        ]
        dataTable.each { i, r ->
            assert Maths.fib(i) == r
        }
    }
}
// end::notyetimplemented[]

new MathsTest().testFib()'''
    }

    void testASTTest() {
        def err = shouldFail '''// tag::asttest_basic[]
import groovy.transform.ASTTest
import org.codehaus.groovy.ast.ClassNode

@ASTTest(phase=CONVERSION, value={   // <1>
    assert node instanceof ClassNode // <2>
    assert node.name == 'Person'     // <3>
})
class Person {
}
// end::asttest_basic[]
def p = new Person()
'''
        assert err =~ /ASTTest phase must be at least SEMANTIC_ANALYSIS/
    }

    void testASTTestWithPackageScope() {
        assertScript '''
// tag::asttest_packagescope[]
import groovy.transform.ASTTest
import groovy.transform.PackageScope

@ASTTest(phase=SEMANTIC_ANALYSIS, value={
    def nameNode = node.properties.find { it.name == 'name' }
    def ageNode = node.properties.find { it.name == 'age' }
    assert nameNode
    assert ageNode == null // shouldn't be a property anymore
    def ageField = node.getDeclaredField 'age'
    assert ageField.modifiers == 0
})
class Person {
    String name
    @PackageScope int age
}
// end::asttest_packagescope[]
def p = new Person()
'''
    }

    void testASTTestWithForLoop() {
        assertScript '''
// tag::asttest_forloop[]
import groovy.transform.ASTTest
import groovy.transform.PackageScope
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.stmt.ForStatement

class Something {
    @ASTTest(phase=SEMANTIC_ANALYSIS, value={
        def forLoop = lookup('anchor')[0]
        assert forLoop instanceof ForStatement
        def decl = forLoop.collectionExpression.expressions[0]
        assert decl instanceof DeclarationExpression
        assert decl.variableExpression.name == 'i'
        assert decl.variableExpression.originType == ClassHelper.int_TYPE
    })
    void someMethod() {
        int x = 1;
        int y = 10;
        anchor: for (int i=0; i<x+y; i++) {
            println "$i"
        }
    }
}
// end::asttest_forloop[]
def p = new Something()
'''
    }

    void testDumpASTTransforms() {
        assertScript '''
// tag::dump_ast_xforms[]
import groovy.transform.ASTTest
import groovy.transform.CompileStatic
import groovy.transform.Immutable
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.CompilePhase

@ASTTest(value={
    System.err.println "Compile phase: $compilePhase"
    ClassNode cn = node
    System.err.println "Global AST xforms: ${compilationUnit?.ASTTransformationsContext?.globalTransformNames}"
    CompilePhase.values().each {
        def transforms = cn.getTransforms(it)
        if (transforms) {
            System.err.println "Ast xforms for phase $it:"
            transforms.each { map ->
                System.err.println(map)
            }
        }
    }
})
@CompileStatic
@Immutable
class Foo {
}
// end::dump_ast_xforms[]
Foo
'''
    }

    void testVariableMemorize() {
        assertScript '''
// tag::memorize_in_binding[]
import groovy.transform.ASTTest
import groovy.transform.ToString
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.CompilePhase

@ASTTest(value={
    if (compilePhase == CompilePhase.INSTRUCTION_SELECTION) {           // <1>
        println "toString() was added at phase: ${added}"
        assert added == CompilePhase.CANONICALIZATION                   // <2>
    } else {
        if (node.getDeclaredMethods('toString') && added == null) {     // <3>
            added = compilePhase                                        // <4>
        }
    }
})
@ToString
class Foo {
    String name
}
// end::memorize_in_binding[]
Foo
'''
    }
}
