/*
 * Copyright 2003-2012 the original author or authors.
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
import junit.framework.AssertionFailedError

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

     def classLoader = new GroovyClassLoader()
     
     def createAdapter(showScriptFreeForm, showScriptClass) {
        def nodeMaker = new SwingTreeNodeMaker()
        new ScriptToTreeNodeAdapter(classLoader, showScriptFreeForm, showScriptClass, nodeMaker)
     }
     
    /**
     * Asserts that a given script produces the expected tree like
     * structure.
     */

     def assertTreeStructure(String script, List<Closure> specification) {
         ScriptToTreeNodeAdapter adapter = createAdapter(true, true)
         assertTreeStructure(script, specification, adapter)
     }
     
     def assertTreeStructure(String script, List<Closure> specification, ScriptToTreeNodeAdapter adapter) {
         TreeNode root = adapter.compile(script, Phases.SEMANTIC_ANALYSIS)
         def original = root
         def lastSpec = 0
         specification.each { spec ->
             if (root) lastSpec++
             root = root?.children()?.find { spec(it) }
         }
         if (!root) {
             fail("Could not locate Expression in AST.${printSpecs(specification, lastSpec)}\nAST: ${printnode(original)}")
         }
     }

    private printSpecs(specification, index) {
        def len = specification.size()
        (index > 1 ? '\nPassed: ' : '') + specification[0..<index].collect{ extractSpecType(it.class.name) }.join(', ') +
        '\nFailed: ' + extractSpecType(specification[index-1].class.name) +
        (index < len ? '\nIgnored: ' + specification[index..-1].collect{ extractSpecType(it.class.name) }.join(', ') : '')
    }

    private extractSpecType(fullyQualified) {
        def start = fullyQualified.indexOf('$_')
        def end = fullyQualified.indexOf('_', start+2)
        fullyQualified.substring(start+2, end)
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
        def buffer = new StringBuffer()
        buffer << '\n' << prefix << node <<
        node.children().each {
            buffer << printnode(it, prefix + "  ")
        }
        buffer
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
                        eq('BlockStatement - (1)'),
                        startsWith('ExpressionStatement'),
                        startsWith('Constant - Hello World : java.lang.String')
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
                        eq('BlockStatement - (1)'),
                        startsWith('ExpressionStatement'),
                        startsWith('Declaration - (x ='),
                        startsWith('ClosureExpression'),
                        startsWith('Parameter - parm1'),
                ]
        )
    }

    public void testClosureParametersWithInitialValue() {

        assertTreeStructure(
                """ def x = { parm1 = "some_value" ->  println parm1 } """,
                [
                        eq('BlockStatement - (1)'),
                        startsWith('ExpressionStatement'),
                        startsWith('Declaration - (x ='),
                        eq('ClosureExpression'),
                        startsWith('Parameter - parm1'),
                        startsWith('Constant - some_value : java.lang.String'),
                ]
        )
    }

    public void testNamedArgumentListExpression() {
        def script = "new String(foo: 'bar', baz: 'qux')"
        ScriptToTreeNodeAdapter adapter = createAdapter(true, true)
        TreeNode root = adapter.compile(script, Phases.SEMANTIC_ANALYSIS)

        def namedArgList = root.children()?.find {
            it.toString().startsWith('BlockStatement')
        }?.children()?.find {
            it.toString().startsWith 'ExpressionStatement'
        }?.children()?.find {
            it.toString().startsWith 'ConstructorCall'
        }?.children()?.find {
            it.toString().startsWith 'Tuple'
        }?.children()?.find {
            it.toString().startsWith 'NamedArgumentListExpression'
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
                        eq('BlockStatement - (1)'),
                        eq('ExpressionStatement - BinaryExpression'),
                        eq('Binary - (foo = bar)'),
                        eq('Variable - foo : java.lang.Object'),
                        eq('DynamicVariable - foo'),
                ]
            )
    }

    public void testVariableParameters() {
        assertTreeStructure(
                " 'foo' ",
                [
                        startsWith('ClassNode'),
                        eq('Methods'),
                        startsWith('MethodNode - run'),
                        startsWith('BlockStatement'),
                        startsWith('ExpressionStatement'),
                        startsWith('Constant - foo'),
                ]
        )
    }

    public void testMultipleAssignments() {
        assertTreeStructure(
                " def (x, y) = [1, 2] ",
                [
                        startsWith('BlockStatement'),
                        startsWith('ExpressionStatement'),
                        eq('Declaration - ((x, y) = [1, 2])'),
                        eq('ArgumentList - (x, y)'),
                ]
        )
    }

    public void testEnum() {
        assertTreeStructure(
                '''enum MyEnum {
                      FOO,
                      BAR;
                    }''',
                [
                        eq('ClassNode - MyEnum'),
                        eq('Fields'),
                        startsWith('FieldNode - FOO : MyEnum'),
                ])
    }

    public void testExpression_DuplicateDoesNotAppear() {
        assertTreeStructure(
                " 'foo' ",
                [
                        startsWith('ClassNode'),
                        eq('Methods'),
                        eq('MethodNode - main'),
                        startsWith('ExpressionStatement'),  //notice, there is only one ExpressionStatement
                        startsWith('MethodCall'),
                ]
        )
    }

    public void testInnerClass() {
        assertTreeStructure(
                """class Outer {
                    private class Inner1 {

                    }
                    private class Inner2 {

                    }
                }""",
                [
                        startsWith('InnerClassNode - Outer\$Inner2'),
                ]
        )
    }

    public void testInnerClassWithMethods() {
        assertTreeStructure(
                """class Outer {
                    private class Inner {
                        def someMethod() {}
                    }
                }""",
                [
                        startsWith('InnerClassNode - Outer\$Inner'),
                        eq('Methods'),
                        startsWith('MethodNode - someMethod'),
                        startsWith('BlockStatement'),
                ]
        )
    }

    public void testInnerClassWithFields() {
        assertTreeStructure(
                """class Outer {
                    private class Inner {
                        String field
                    }
                }""",
                [
                        startsWith('InnerClassNode - Outer\$Inner'),
                        eq('Fields'),
                        startsWith('FieldNode - field'),
                ]
        )
    }

    public void testInnerClassWithAnnotations() {
        assertTreeStructure(
                """class Outer {
                    @Singleton
                    private class Inner {
                    }
                }""",
                [
                        startsWith('InnerClassNode - Outer\$Inner'),
                        eq('Annotations'),
                        startsWith('AnnotationNode - groovy.lang.Singleton'),
                ]
        )
    }

    public void testInnerClassWithProperties() {
        assertTreeStructure(
                """class Outer {
                    private class Inner {
                        def property
                    }
                }""",
                [
                        startsWith('InnerClassNode - Outer\$Inner'),
                        eq('Properties'),
                        startsWith('PropertyNode - property'),
                ]
        )
    }

    public void testScriptWithMethods() {
        // verify the free form script
        assertTreeStructure(
                "def foo(String bar) {}",
                [
                    eq('Methods'),
                    eq('MethodNode - foo'),
                    eq('Parameter - bar'),
                ]
            )

        // verify the script's class
        assertTreeStructure(
                "def foo(String bar) {}",
                [
                    startsWith('ClassNode - script'),
                    eq('Methods'),
                    eq('MethodNode - foo'),
                    eq('Parameter - bar'),
                ]
            )
    }

    public void testScriptWithAdapterThatLoadsClassButNotFreeForm() {
        ScriptToTreeNodeAdapter adapter = createAdapter(false, true)

        // since free standing script is not being loaded, it should fail
        shouldFail(AssertionFailedError) {
            assertTreeStructure(
                    "def foo(String bar) {}",
                    [
                        eq('Methods'),
                        eq('MethodNode - foo'),
                        eq('Parameter - bar'),
                    ],
                    adapter
                )
        }
        
        // since script class is being loaded, it should go through
        assertTreeStructure(
                "def foo(String bar) {}",
                [
                    startsWith('ClassNode - script'),
                    eq('Methods'),
                    eq('MethodNode - foo'),
                    eq('Parameter - bar'),
                ],
                adapter
            )
    }

    public void testScriptWithAdapterThatLoadsFreeFormButNotClass() {
        ScriptToTreeNodeAdapter adapter = createAdapter(true, false)

        // since free standing script is being loaded, it should go through
        assertTreeStructure(
                "def foo(String bar) {}",
                [
                    eq('Methods'),
                    eq('MethodNode - foo'),
                    eq('Parameter - bar'),
                ],
                adapter
            )

        // since script class is not being loaded, it should fail
        shouldFail(AssertionFailedError) {
            assertTreeStructure(
                    "def foo(String bar) {}",
                    [
                        startsWith('ClassNode - script'),
                        eq('Methods'),
                        eq('MethodNode - foo'),
                        eq('Parameter - bar'),
                    ],
                    adapter
                )
        }
    }

   public void testScriptWithAdapterThatLoadsNitherFreeFormNorClass() {
        ScriptToTreeNodeAdapter adapter = createAdapter(false, false)

        // since free standing script is not being loaded, it should fail
        shouldFail(AssertionFailedError) {
            assertTreeStructure(
                    "def foo(String bar) {}",
                    [
                        eq('Methods'),
                        eq('MethodNode - foo'),
                        eq('Parameter - bar'),
                    ],
                    adapter
                )
        }

        // since script class is not being loaded, it should fail
        shouldFail(AssertionFailedError) {
            assertTreeStructure(
                    "def foo(String bar) {}",
                    [
                        startsWith('ClassNode - script'),
                        eq('Methods'),
                        eq('MethodNode - foo'),
                        eq('Parameter - bar'),
                    ],
                    adapter
                )
        }
    }

    public void testScriptWithAdapterThatAddsDescriptorToMethodNodeProperties() {
        ScriptToTreeNodeAdapter adapter = createAdapter(true, false)
        TreeNode root = adapter.compile('''
            class Test {
                void test() {}
            }

        ''', Phases.SEMANTIC_ANALYSIS) as TreeNode

        def classNodeTest = root.children().find { it.toString() == 'ClassNode - Test' }
        def methods = classNodeTest.children().find { it.toString() == 'Methods' }
        def methodNodeTest = methods.children().find { it.toString() == 'MethodNode - test' }

        assert methodNodeTest.properties.any { name, value, type -> name == 'descriptor' && value == '()V' && type == 'String' }
    }
    
}
