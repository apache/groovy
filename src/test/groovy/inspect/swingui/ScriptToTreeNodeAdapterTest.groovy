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

    ScriptToTreeNodeAdapter adapter

    protected void setUp() {
        adapter = new ScriptToTreeNodeAdapter()
    }

    public void testCompile_HelloWorld() {

        def script = "\"Hello World\""
        ScriptToTreeNodeAdapter adapter = new ScriptToTreeNodeAdapter()
        TreeNode root = adapter.compile(script, Phases.SEMANTIC_ANALYSIS)

        def result = root.children()?.find {
            it.toString() == 'BlockStatement'
        }?.children()?.find {
            it.toString() == 'BlockStatement'
        }?.children()?.find {
            it.toString() == 'ExpressionStatement'
        }?.children()?.find {
            it.toString() == 'Constant - Hello World : java.lang.String'
        }
        assertNotNull('Could not locate ConstantExpression in AST', result)
    }

    public void testCompile_SimpleClass() {
        def script = " class Foo { public aField } "
        ScriptToTreeNodeAdapter adapter = new ScriptToTreeNodeAdapter()
        TreeNode root = adapter.compile(script, Phases.SEMANTIC_ANALYSIS)

        def result = root.children()?.find {
            it.toString() == 'ClassNode - Foo'
        }?.children()?.find {
            it.toString() == 'Fields'
        }?.children()?.find {
            it.toString() == 'FieldNode - aField : java.lang.Object'
        }
        assertNotNull('Could not locate ClassExpression in AST', result)
    }


    public void testCompile_MethodWithParameter() {
        def script = " def foo(String bar) { println bar } "
        ScriptToTreeNodeAdapter adapter = new ScriptToTreeNodeAdapter()
        TreeNode root = adapter.compile(script, Phases.SEMANTIC_ANALYSIS)

        def result = root.children()?.find {
            it.toString().startsWith('ClassNode - script')
        }?.children()?.find {
            it.toString() == 'Methods'
        }?.children()?.find {
            it.toString() == 'MethodNode - foo'
        }?.children()?.find {
            it.toString() == 'Parameter - bar'
        }
        assertNotNull('Could not locate ClassExpression in AST', result)
    }

    public void testCompile_MethodWithParameterAndInitialValue() {
        def script = """ def foo(String bar = "some_value") { println bar } """
        ScriptToTreeNodeAdapter adapter = new ScriptToTreeNodeAdapter()
        TreeNode root = adapter.compile(script, Phases.SEMANTIC_ANALYSIS)

        def result = root.children()?.find {
            it.toString().startsWith('ClassNode - script')
        }?.children()?.find {
            it.toString() == 'Methods'
        }?.children()?.find {
            it.toString() == 'MethodNode - foo'
        }?.children()?.find {
            it.toString() == 'Parameter - bar'
        }?.children()?.find {
            it.toString() == 'Constant - some_value : java.lang.String'
        }
        assertNotNull('Could not locate ClassExpression in AST', result)
    }

    public void testCompile_ClosureParameters() {

        def script = " def x = { parm1 ->  println parm1 } "
        ScriptToTreeNodeAdapter adapter = new ScriptToTreeNodeAdapter()
        TreeNode root = adapter.compile(script, Phases.SEMANTIC_ANALYSIS)
        def result = root.children()?.find {
            it.toString() == 'BlockStatement'
        }?.children()?.find {
            it.toString() == 'BlockStatement'
        }?.children()?.find {
            it.toString() == 'ExpressionStatement'
        }?.children()?.find {
            it.toString().startsWith('Declaration - (x =')
        }?.children()?.find {
            it.toString() == 'ClosureExpression'
        }?.children()?.find {
            it.toString() == 'Parameter - parm1'
        }
        assertNotNull('Could not locate ClassExpression in AST', result)
    }

    public void testCompile_ClosureParametersWithInitialValue() {

        def script = """ def x = { parm1 = "some_value" ->  println parm1 } """
        ScriptToTreeNodeAdapter adapter = new ScriptToTreeNodeAdapter()
        TreeNode root = adapter.compile(script, Phases.SEMANTIC_ANALYSIS)
        def result = root.children()?.find {
            it.toString() == 'BlockStatement'
        }?.children()?.find {
            it.toString() == 'BlockStatement'
        }?.children()?.find {
            it.toString() == 'ExpressionStatement'
        }?.children()?.find {
            it.toString().startsWith('Declaration - (x =')
        }?.children()?.find {
            it.toString() == 'ClosureExpression'
        }?.children()?.find {
            it.toString() == 'Parameter - parm1'
        }?.children()?.find {
            it.toString() == 'Constant - some_value : java.lang.String'
        }
        assertNotNull('Could not locate ClassExpression in AST', result)
    }

    public void testNamedArgumentListExpression() {
        def script = "new String(foo: 'bar', baz: 'qux')"
        ScriptToTreeNodeAdapter adapter = new ScriptToTreeNodeAdapter()
        TreeNode root = adapter.compile(script, Phases.SEMANTIC_ANALYSIS)

        def namedArgList = root.children()?.find {
            it.toString() == 'BlockStatement'
        }?.children()?.find {
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
     * Helper method to print out the TreeNode to a test form in system out.
     * Warning, this uses recursion. 
     */
    def printnode(TreeNode node, String prefix = "") {
        println prefix + node
        node.children().each {
            printnode(it, prefix + "  ")
        }
    }
}
