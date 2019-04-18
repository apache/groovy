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
package groovy.inspect.swingui

import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.Phases

import javax.swing.tree.TreeNode
import junit.framework.AssertionFailedError

/**
 * Unit test for ScriptToTreeNodeAdapter.
 *
 * The assertions in this test case often assert against the toString() representation of
 * an object. Normally, this is bad form. However, the class under test is meant to display
 * toString() forms in a user interface. So in this case it is appropriate. 
 */
class ScriptToTreeNodeAdapterTest extends GroovyTestCase {

     private final classLoader = new GroovyClassLoader()
     
     private createAdapter(showScriptFreeForm, showScriptClass, showClosureClasses) {
        def nodeMaker = new SwingTreeNodeMaker()
        new ScriptToTreeNodeAdapter(classLoader, showScriptFreeForm, showScriptClass, showClosureClasses, nodeMaker)
     }
     
    /**
     * Asserts that a given script produces the expected tree like
     * structure.
     */
     private assertTreeStructure(String script, List<Closure> specification) {
         ScriptToTreeNodeAdapter adapter = createAdapter(true, true, true)
         assertTreeStructure(script, specification, adapter)
     }
     
     private assertTreeStructure(String script, List<Closure> specification, ScriptToTreeNodeAdapter adapter) {
         assertTreeStructure(script, CompilePhase.SEMANTIC_ANALYSIS, specification, adapter)
     }

    private assertTreeStructure(String script, CompilePhase compilePhase, List<Closure> specification, ScriptToTreeNodeAdapter adapter) {
         TreeNode root = adapter.compile(script, compilePhase.phaseNumber)
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
        def start = fullyQualified.indexOf('$_') + 2
        def end = fullyQualified.indexOf('_', start)
        fullyQualified[start..end]
    }

    /**
     * Helper method to assert a map entry element.
     */
    private assertMapEntry(mapEntry, expectedKey, expectedValue) {
        assertNotNull('Could not locate 1st MapEntryExpression in AST', mapEntry)
        def children = mapEntry.children().toList()
        assertEquals('Wrong # map entries', 2, children.size())
        assertEquals('Wrong key', expectedKey, children[0].toString())
        assertEquals('Wrong value', expectedValue, children[1].toString())
    }

    /**
     * Returns a function that tests for Groovy Truth equality.
     */
    private eq(String target) {
        return { it.toString() == target }
    }

    /**
     * Helper method to print out the TreeNode to a test form in system out.
     * Warning, this uses recursion.
     */
    private printnode(TreeNode node, String prefix = '') {
        def buffer = new StringBuffer()
        buffer << '\n' << prefix << node <<
        node.children().each {
            buffer << printnode(it, prefix + '  ')
        }
        buffer
    }

    /**
     * Returns a function that acts much like String#startsWith for Objects.
     */
    private startsWith(String target) {
        return { it.toString().startsWith(target) }
    }

    /**
     * Returns a function that acts much like String#contains for Objects.
     */
    private contains(String target)  {
        return {
            it.toString().contains(target)
        }
    }

    void testHelloWorld() {

        assertTreeStructure(
                '"Hello World"',
                [
                        eq('BlockStatement - (1)'),
                        startsWith('ExpressionStatement'),
                        startsWith('Constant - Hello World : java.lang.String')
                ])
    }

    void testSimpleClass() {

        assertTreeStructure(
                ' class Foo { public aField } ',
                [
                        eq('ClassNode - Foo'),
                        eq('Fields'),
                        eq('FieldNode - aField : java.lang.Object'),
                ]
        )
    }

    void testMethodWithParameter() {

        assertTreeStructure(
                ' def foo(String bar) { println bar } ',
                [
                    startsWith('ClassNode - script'),
                    eq('Methods'),
                    eq('MethodNode - foo'),
                    eq('Parameter - bar'),
                ]
            )
    }

    void testMethodWithParameterAndInitialValue() {

        assertTreeStructure(
                ' def foo(String bar = "some_value") { println bar } ',
                [
                        startsWith('ClassNode - script'),
                        eq('Methods'),
                        eq('MethodNode - foo'),
                        eq('Parameter - bar'),
                        eq('Constant - some_value : java.lang.String'),
                ]
            )
    }

    void testClosureParameters() {

        assertTreeStructure(
                ' def x = { parm1 ->  println parm1 } ',
                [
                        eq('BlockStatement - (1)'),
                        startsWith('ExpressionStatement'),
                        startsWith('Declaration - (x ='),
                        startsWith('ClosureExpression'),
                        startsWith('Parameter - parm1'),
                ]
        )
    }

    void testClosureParametersWithInitialValue() {

        assertTreeStructure(
                ' def x = { parm1 = "some_value" ->  println parm1 } ',
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

    void testNamedArgumentListExpression() {
        def script = "new String(foo: 'bar', baz: 'qux')"
        ScriptToTreeNodeAdapter adapter = createAdapter(true, true, true)
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

        def children = namedArgList.children().toList()
        assertEquals('Wrong # named arguments', 2, children.size())

        assertMapEntry(children[0], 'Constant - foo : java.lang.String', 'Constant - bar : java.lang.String')
        assertMapEntry(children[1], 'Constant - baz : java.lang.String', 'Constant - qux : java.lang.String')
    }

    void testDynamicVariable() {

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

    void testVariableParameters() {
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

    void testMultipleAssignments() {
        assertTreeStructure(
                ' def (x, y) = [1, 2] ',
                [
                        startsWith('BlockStatement'),
                        startsWith('ExpressionStatement'),
                        eq('Declaration - ((x, y) = [1, 2])'),
                        eq('ArgumentList - (x, y)'),
                ]
        )
    }

    void testEnum() {
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

    void testExpression_DuplicateDoesNotAppear() {
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

    void testInnerClass() {
        assertTreeStructure(
                '''class Outer {
                    private class Inner1 {

                    }
                    private class Inner2 {

                    }
                }''',
                [
                        startsWith('InnerClassNode - Outer\$Inner2'),
                ]
        )
    }

    void testInnerClassWithMethods() {
        assertTreeStructure(
                '''class Outer {
                    private class Inner {
                        def someMethod() {}
                    }
                }''',
                [
                        startsWith('InnerClassNode - Outer\$Inner'),
                        eq('Methods'),
                        startsWith('MethodNode - someMethod'),
                        startsWith('BlockStatement'),
                ]
        )
    }

    void testInnerClassWithFields() {
        assertTreeStructure(
                '''class Outer {
                    private class Inner {
                        String field
                    }
                }''',
                [
                        startsWith('InnerClassNode - Outer\$Inner'),
                        eq('Fields'),
                        startsWith('FieldNode - field'),
                ]
        )
    }

    void testInnerClassWithAnnotations() {
        assertTreeStructure(
                '''class Outer {
                    @Singleton
                    private class Inner {
                    }
                }''',
                [
                        startsWith('InnerClassNode - Outer\$Inner'),
                        eq('Annotations'),
                        startsWith('AnnotationNode - groovy.lang.Singleton'),
                ]
        )
    }

    void testInnerClassWithProperties() {
        assertTreeStructure(
                '''class Outer {
                    private class Inner {
                        def property
                    }
                }''',
                [
                        startsWith('InnerClassNode - Outer\$Inner'),
                        eq('Properties'),
                        startsWith('PropertyNode - property'),
                ]
        )
    }

    void testScriptWithMethods() {
        // verify the free form script
        assertTreeStructure(
                'def foo(String bar) {}',
                [
                    eq('Methods'),
                    eq('MethodNode - foo'),
                    eq('Parameter - bar'),
                ]
            )

        // verify the script's class
        assertTreeStructure(
                'def foo(String bar) {}',
                [
                    startsWith('ClassNode - script'),
                    eq('Methods'),
                    eq('MethodNode - foo'),
                    eq('Parameter - bar'),
                ]
            )
    }

    void testScriptWithAdapterThatLoadsClassButNotFreeForm() {
        ScriptToTreeNodeAdapter adapter = createAdapter(false, true, true)

        // since free standing script is not being loaded, it should fail
        shouldFail(AssertionFailedError) {
            assertTreeStructure(
                    'def foo(String bar) {}',
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
                'def foo(String bar) {}',
                [
                    startsWith('ClassNode - script'),
                    eq('Methods'),
                    eq('MethodNode - foo'),
                    eq('Parameter - bar'),
                ],
                adapter
            )
    }

    void testScriptWithAdapterThatLoadsFreeFormButNotClass() {
        ScriptToTreeNodeAdapter adapter = createAdapter(true, false, false)

        // since free standing script is being loaded, it should go through
        assertTreeStructure(
                'def foo(String bar) {}',
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
                    'def foo(String bar) {}',
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

   void testScriptWithAdapterThatLoadsNitherFreeFormNorClass() {
        ScriptToTreeNodeAdapter adapter = createAdapter(false, false, false)

        // since free standing script is not being loaded, it should fail
        shouldFail(AssertionFailedError) {
            assertTreeStructure(
                    'def foo(String bar) {}',
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
                    'def foo(String bar) {}',
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

    void testScriptWithAdapterThatAddsDescriptorToMethodNodeProperties() {
        ScriptToTreeNodeAdapter adapter = createAdapter(true, false, false)
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

    void testScriptWithAdapterThatLoadsGeneratedClosureClass() {
        ScriptToTreeNodeAdapter adapter = createAdapter(false, true, true)

        assertTreeStructure(
                "def c = { println 'hello world' }", CompilePhase.CLASS_GENERATION,
                [
                    contains('closure1'),
                    eq('Methods'),
                    eq('MethodNode - doCall'),
                ],
                adapter
            )
    }

    void testScriptWithAdapterThatLoadsMultipleGeneratedClosureClasses() {
        ScriptToTreeNodeAdapter adapter = createAdapter(false, true, true)

        def source = '''
            class Controller {

                def show = { println 'show' }
                def edit = { println 'edit' }
                def delete = { println 'delete' }

            }
            '''

        assertTreeStructure(source, CompilePhase.CLASS_GENERATION,
                [
                        startsWith('ClassNode - Controller')
                ],
                adapter)

        assertTreeStructure(source, CompilePhase.CLASS_GENERATION,
                [
                        contains('closure1'),
                        eq('Methods'),
                        eq('MethodNode - doCall'),
                ],
                adapter)

        assertTreeStructure(source, CompilePhase.CLASS_GENERATION,
                [
                        contains('closure2'),
                        eq('Methods'),
                        eq('MethodNode - doCall'),
                ],
                adapter)

        assertTreeStructure(source, CompilePhase.CLASS_GENERATION,
                [
                        contains('closure3'),
                        eq('Methods'),
                        eq('MethodNode - doCall'),
                ],
                adapter)
    }

    // GROOVY-4636
    void testScriptWithObjectInitializers() {
        ScriptToTreeNodeAdapter adapter = createAdapter(false, true, true)

        def source = '''
            class A {
                int i = 0
                A() {
                    i = 5
                }
                {
                    i *= 2
                }
            }
            '''

        assertTreeStructure(source, CompilePhase.CONVERSION,
                [
                        startsWith('ClassNode - A'),
                        eq('Object Initializers'),
                        contains('BlockStatement'),
                        contains('BlockStatement'),
                        contains('ExpressionStatement')
                ],
                adapter)

    }

    void testTraitObjectInitializers() {
        ScriptToTreeNodeAdapter adapter = createAdapter(false, true, true)

        def source = '''
            trait Interceptor {
                final Collection<String> matchers = new ArrayList<String>()
                void matchAll() {
                    matchers << 'foo'
                }
            }

            class TestInterceptor implements Interceptor { }
            '''

        assertTreeStructure(source, CompilePhase.CANONICALIZATION,
                [
                        startsWith('ClassNode - TestInterceptor'),
                        eq('Object Initializers'),
                        eq('ExpressionStatement - MethodCallExpression')
                ],
                adapter)
    }

    void testCompileIndyBytecode() {
        ScriptToTreeNodeAdapter adapter = createAdapter(true, false, false)
        TreeNode root = adapter.compile('''
            class Test {
                void test() {}
            }

        ''', Phases.CLASS_GENERATION, true) as TreeNode

        def classNodeTest = root.children().find { it.toString() == 'ClassNode - Test' }
        def methods = classNodeTest.children().find { it.toString() == 'Methods' }
        def methodNodeTest = methods.children().find { it.toString() == 'MethodNode - test' }

        assert classNodeTest
        assert methods
        assert methodNodeTest
    }

}
