/*
 * Copyright 2003-2009 the original author or authors.
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
package groovy.inspect.swingui

import org.codehaus.groovy.control.Phases
import javax.swing.tree.TreeNode

/**
 * Unit test for ScriptToTreeNodeAdapter.
 *
 * The assertions in this test case ofter assert against the toString() representation of
 * an object. Normally, this is bad form. However, the class under test is meant to display
 * toString() forms in a user interface. So in this case it is appropriate. 
 *
 * @author Hamlet D'Arcy
 */
public class ScriptToTreeNodeAdapterTest extends GroovyTestCase {

    /**
     * Asserts that a given script produces the expected tree like
     * structure. 
     */

    def assertTreeStructure(String script, List<Closure> specification) {
        ScriptToTreeNodeAdapter adapter = new ScriptToTreeNodeAdapter()
        TreeNode root = adapter.compile(script, Phases.SEMANTIC_ANALYSIS)
        specification.each { spec ->
            root = root?.children()?.find {
                spec(it)
            }
        }
        assertNotNull('Could not locate Expression in AST', root)
    }


    /**
     * Helper method to assert a map entry element.
     */
    private def assertMapEntry(mapEntry, expectedKey, expectedValue) {
        assertNotNull('Could not locate 1st MapEntryExpression in AST', mapEntry)
        assertEquals('Wrong # map entries', 2, mapEntry.children.size())
        assertEquals('Wrong key', expectedKey, mapEntry.children[0].toString())
        assertEquals('Wrong value', expectedValue, mapEntry.children[1].toString())
    }

    /**
     * Returns a function that tests for Groovy Truth equality. 
     */
    def eq(String target) {
        return { it.toString() == target }
    }

    /**
     * Helper method to print out the TreeNode to a test form in system out.
     * Warning, this uses recursion.
     */
    def printnode(TreeNode node, String prefix = "") {
        println prefix + node
        node.children().each {
            printnode(it, prefix + "  ")
        }
    }

    /**
     * Returns a function that acts much like String#startsWith for Objects.
     */
    def startsWith(String target) {
        return { it.toString().startsWith(target) }
    }

    public void testHelloWorld() {

        assertTreeStructure(
                "\"Hello World\"",
                [
                        eq('BlockStatement'),
                        eq('ExpressionStatement'),
                        eq('Constant - Hello World : java.lang.String')
                ])
    }

    public void testSimpleClass() {

        assertTreeStructure(
                " class Foo { public aField } ",
                [
                        eq('ClassNode - Foo'),
                        eq('Fields'),
                        eq('FieldNode - aField : java.lang.Object'),
                ]
        )
    }

    public void testMethodWithParameter() {

        assertTreeStructure(
                " def foo(String bar) { println bar } ",
                [
                    startsWith('ClassNode - script'),
                    eq('Methods'),
                    eq('MethodNode - foo'),
                    eq('Parameter - bar'),
                ]
            )
    }

    public void testMethodWithParameterAndInitialValue() {

        assertTreeStructure(
                """ def foo(String bar = "some_value") { println bar } """,
                [
                        startsWith('ClassNode - script'),
                        eq('Methods'),
                        eq('MethodNode - foo'),
                        eq('Parameter - bar'),
                        eq('Constant - some_value : java.lang.String'),
                ]
            )
    }

    public void testClosureParameters() {

        assertTreeStructure(
                " def x = { parm1 ->  println parm1 } ",
                [
                        eq('BlockStatement'),
                        eq('ExpressionStatement'),
                        startsWith('Declaration - (x ='),
                        eq('ClosureExpression'),
                        eq('Parameter - parm1'),
                ]
        )
    }

    public void testClosureParametersWithInitialValue() {

        assertTreeStructure(
                """ def x = { parm1 = "some_value" ->  println parm1 } """,
                [
                        eq('BlockStatement'),
                        eq('ExpressionStatement'),
                        startsWith('Declaration - (x ='),
                        eq('ClosureExpression'),
                        eq('Parameter - parm1'),
                        eq('Constant - some_value : java.lang.String'),
                ]
        )
    }

    public void testNamedArgumentListExpression() {
        def script = "new String(foo: 'bar', baz: 'qux')"
        ScriptToTreeNodeAdapter adapter = new ScriptToTreeNodeAdapter()
        TreeNode root = adapter.compile(script, Phases.SEMANTIC_ANALYSIS)

        def namedArgList = root.children()?.find {
            it.toString() == 'BlockStatement'
        }?.children()?.find {
            it.toString() == 'ExpressionStatement'
        }?.children()?.find {
            it.toString() == 'ConstructorCallExpression'
        }?.children()?.find {
            it.toString() == 'TupleExpression'
        }?.children()?.find {
            it.toString() == 'NamedArgumentListExpression'
        }
        assertNotNull('Could not locate NamedArgumentListExpression in AST', namedArgList)

        assertEquals('Wrong # named arguments', 2, namedArgList.children.size())

        assertMapEntry(namedArgList.children[0], 'Constant - foo : java.lang.String', 'Constant - bar : java.lang.String')
        assertMapEntry(namedArgList.children[1], 'Constant - baz : java.lang.String', 'Constant - qux : java.lang.String')
    }

    public void testDynamicVariable() {

        assertTreeStructure(
                " foo = 'bar' ",
                [
                        eq('BlockStatement'),
                        eq('ExpressionStatement'),
                        startsWith('Binary'),
                        startsWith('Variable'),
                        eq('DynamicVariable'),
                ]
            )
    }

    public void testVariableParameters() {
        assertTreeStructure(
                " 'foo' ",
                [
                        startsWith('ClassNode'),
                        eq('Methods'),
                        startsWith('MethodNode - this$dist$invoke$'),
                        eq('BlockStatement'),
                        eq('ReturnStatement'),
                        eq('MethodCallExpression'),
                        eq('ArgumentListExpression'),
                        eq('SpreadExpression'),
                        startsWith('Variable'),
                        eq('Parameter - args'),
                ]
        )
    }

    public void testExpression_DuplicateDoesNotAppear() {
        assertTreeStructure(
                " 'foo' ",
                [
                        startsWith('ClassNode'),
                        eq('Methods'),
                        eq('MethodNode - main'),
                        eq('ExpressionStatement'),  //notice, there is only one ExpressionStatement
                        eq('MethodCallExpression'),
                ]
        )
    }
}