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
package groovy.console.ui

import org.codehaus.groovy.ast.VariableScope
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.DoWhileStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.jupiter.api.Test

import static org.codehaus.groovy.ast.tools.GeneralUtils.args
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt

/**
 * Unit test for {@link AstNodeToScriptAdapter}.
 *
 * The assertions in this test case often assert against the toString() representation of
 * an object. Normally, this is bad form. However, the class under test is meant to display
 * toString() forms in a user interface. So in this case it is appropriate.
 */
final class AstNodeToScriptAdapterTest {

    private static String compileToScript(String script, CompilePhase phase = CompilePhase.SEMANTIC_ANALYSIS) {
        new AstNodeToScriptAdapter().compileToScript(script, phase.phaseNumber)
    }

    @Test
    void testScript() {
        String result = compileToScript('true')

        assert result =~ /public class script[0-9]* extends groovy\.lang\.Script \{/
        assert result =~ /public script[0-9]*\(\) \{\s*\}/
        assert result =~ /public script[0-9]*\(final groovy.lang.Binding context\) \{\s*super\(context\)\s*\}/
        assert result =~ /public java.lang.Object run\(\) \{\s*true\s*\}/
    }

    @Test
    void testStringEscaping() {
        String script = ''' if (_out.toString().endsWith('\\n\\n')) { }  '''
        String result = compileToScript(script)

        assert result.contains("_out.toString().endsWith('\\n\\n')")
    }

    @Test
    void testStringEscapingHandlesBackslashesQuotesAndControlChars() {
        // Dollar-slashy input keeps these readable: inside it '\\' is a literal backslash and
        // '\n'/'\t' are literal backslash-letter pairs, so the *compiler* turns them into a
        // single backslash / a real newline / a real tab in each constant's value.
        String script = $/
            def backslash = 'back\\slash'
            def winPath   = 'C:\\Users\\name'
            def quoted    = 'quote\'inside'
            def tabbed    = 'tab\there'
            def newlined  = 'multi\nline'
        /$
        String result = compileToScript(script)

        // Each value must be re-emitted in a form that parses back to the same value.
        assert result.contains($/'back\\slash'/$)       // literal backslash -> \\
        assert result.contains($/'C:\\Users\\name'/$)
        assert result.contains($/'quote\'inside'/$)     // single quote -> \'
        assert result.contains($/'tab\there'/$)         // real tab -> \t
        assert result.contains($/'multi\nline'/$)       // real newline -> \n

        // The old naive escaping emitted a bare backslash (e.g. 'back\slash'), which either
        // changes the value or fails to compile; the emitted script must parse cleanly.
        assert new GroovyShell().parse(result)
    }

    @Test
    void testGStringRoundTripsToSameValue() {
        // Each snippet ends in a GString; both the original and the adapter's reconstruction
        // must evaluate to the same value. These are dollar-slashy literals, which themselves
        // interpolate, so a literal '$' meant for the *snippet* is written '$$' and '\' is kept
        // verbatim (dollar-slashy has no backslash escapes).
        def snippets = [
            'plain interpolation'   : $/def name = 'Bob'; "hi $$name"/$,
            'interp string const'   : $/"$${'y'}"/$,
            'double-quote inside'   : $/def n = 'X'; "say \"hi\" to $$n"/$,
            'backslash / win path'  : $/def f = 'F'; "C:\\tmp $$f"/$,
            'literal dollar'        : $/def item = 'I'; "price \$$5 for $$item"/$,
            'escaped interpolation' : $/def real = 'R'; "\$${notInterp} and $$real"/$,
            'string args in interp' : $/"n=$${['a', 'b'].join(',')}"/$,
            'slashy regex gstring'  : $/def c = 5; /\d+ = $$c//$,
            'lazy closure interp'   : $/"$${-> 'lz'}"/$,
            'list-in-interp'        : $/def a = 'A'; "items $${['x', 'y']}"/$,
            'multi-line triple'     : $/def v = 'V'; """a
b $$v"""/$,
        ]

        snippets.each { name, src ->
            def expected = new GroovyShell().evaluate(src)
            // free-form output (showScriptClass = false) so the reconstruction is directly evaluable
            String rebuilt = new AstNodeToScriptAdapter().compileToScript(
                    src, CompilePhase.SEMANTIC_ANALYSIS.phaseNumber, null, true, false)
            def actual
            try {
                actual = new GroovyShell().evaluate(rebuilt)
            } catch (Throwable t) {
                assert false, "[$name] reconstruction did not evaluate: ${t.message}\n--- rebuilt ---\n$rebuilt"
            }
            assert expected?.toString() == actual?.toString(),
                    "[$name] value changed\n  expected: ${expected?.toString()?.inspect()}\n  actual:   ${actual?.toString()?.inspect()}\n--- rebuilt ---\n$rebuilt"
        }
    }

    @Test
    void testPackage() {
        String script = ''' package foo.bar
                            true '''
        String result = compileToScript(script)

        assert result.contains('package foo.bar\n')
    }

    @Test
    void testSubScriptOperator() {
        String script = '''
            def file = new File((String)args[0])
            println "File $args[0] cannot be found."
        '''
        String result = compileToScript(script)

        assert result.contains('println("File $args[0] cannot be found.")')
        assert result.contains('file = new java.io.File(((java.lang.String) args[0]))')
    }

    @Test
    void testMethods() {
        String script = '''
            def method1() {}
            private Object method2() {}
            public String method3() throws Exception, RuntimeException {}
            protected void method4() {}
            @Package def method5(parm1) {}
            static def method6(String parm1) {}
            native method7(parm1, final parm2) {};
            synchronized def method8(String parm1, final String parm2) {}
            def method9(String parm1 = getValue(), String parm2 = "somevalue") {}
            Integer[] method10(String[] parm1, Object[] parm2) {}
        '''
        String result = compileToScript(script)

        assert result.contains('public java.lang.Object method1()')
        assert result.contains('private java.lang.Object method2()')
        assert result.contains('public java.lang.String method3() throws java.lang.Exception, java.lang.RuntimeException')
        assert result.contains('protected void method4()')
        assert result.contains('public java.lang.Object method5(java.lang.Object parm1)')
        assert result.contains('public static java.lang.Object method6(java.lang.String parm1)')
        assert result.contains('public native java.lang.Object method7(java.lang.Object parm1, final java.lang.Object parm2)')
        assert result.contains('public synchronized java.lang.Object method8(java.lang.String parm1, final java.lang.String parm2)')
        assert result.contains("public java.lang.Object method9(java.lang.String parm1 = this.getValue(), java.lang.String parm2 = 'somevalue')")
        assert result.contains('public java.lang.Integer[] method10(java.lang.String[] parm1, java.lang.Object[] parm2)')
    }

    @Test
    void testGenerics() {
        String script = '''
            public class MyList<E> extends AbstractList<E> implements List<E> {
               E get(int x) {}
               int size() {}
            }
        '''
        String result = compileToScript(script)

        assert result.contains('public class MyList<E> extends java.util.AbstractList<E> implements java.util.List<E> {')
    }

    @Test
    void testGenericBoundsOnClass() {
        String script = '''import java.util.concurrent.Callable
            abstract class MyClass<T extends String & Callable<String>, U extends Integer> extends AbstractList<String> implements Callable<? super Number> { }
        '''
        String result = compileToScript(script)

        assert result.contains('MyClass<T extends java.lang.String & java.util.concurrent.Callable<String>, U extends java.lang.Integer> ' +
                'extends java.util.AbstractList<String> ' +
                'implements java.util.concurrent.Callable<? super java.lang.Number> {')
    }

    @Test
    void testGenericsInVariables() {
        // you think you know Java generics? Just contemplate a load of this mess:
        String script = '''
            public class Tree<V> { }
            Tree<Number> t = new Tree<Number>(0);
            t.addBranch(new Tree<Integer>(1));
            Tree<? extends java.lang.Number> b = t.getBranch(0);
            Tree<?> b2 = t.getBranch(0);
            Tree<java.lang.Number> b3 = t.getBranch(0);
            Number value = b.getValue();
            b.setValue(3.0);
            b.addBranch(new Tree<java.lang.Double>(Math.PI));
        '''
        String result = compileToScript(script)

        assert result.contains('Tree<Number> t = new Tree<Number>(0)')
        assert result.contains('t.addBranch(new Tree<Integer>(1))')
        assert result.contains('Tree<? extends java.lang.Number> b = t.getBranch(0)')
        assert result.contains('Tree<?> b2 = t.getBranch(0)')
        assert result.contains('Tree<java.lang.Number> b3 = t.getBranch(0)')
        assert result.contains('Number value = b.getValue()')
        assert result.contains('b.setValue(3.0)')
        assert result.contains('b.addBranch(new Tree<java.lang.Double>(java.lang.Math.PI))')
    }

    @Test
    void testGenericsInMethods() {
        // you think you know Java generics? Just contemplate a load of this mess:
        String script = '''
            public class Tree<V> {
                V value;
                List<Tree<? extends V>> branches = new ArrayList<Tree<? extends V>>();
                public Tree(V value) { this.value = value; }
                V getValue() { return value; }
                void setValue(V value) { this.value = value; }
                int getNumBranches() { return branches.size(); }
                Tree<? extends V> getBranch(int n) { return branches.get(n); }
                void addBranch(Tree<? extends V> branch) { branches.add(branch); }
            }
        '''
        String result = compileToScript(script, CompilePhase.CLASS_GENERATION)

        assert result.contains('public class Tree<V> extends java.lang.Object implements groovy.lang.GroovyObject')
        assert result.contains('private java.lang.Object<V> value') // todo: is Object<V> correct? How do you know?
        assert result.contains('private java.util.List<Tree<? extends java.lang.Object<V>>> branches')
        assert result.contains('branches = new java.util.ArrayList<Tree<? extends java.lang.Object<V>>>()')
        assert result.contains('public Tree(java.lang.Object<V> value)') // again, is this correct?
        assert result.contains(' public java.lang.Object<V> getValue()') // is this correct?
        assert result.contains('public void setValue(java.lang.Object<V> value)')
        assert result.contains('Tree<? extends java.lang.Object<V>> getBranch(int n)')  // is this correct?
        assert result.contains('void addBranch(Tree<? extends java.lang.Object<V>> branch)')
    }

    @Test
    void testBitwiseNegation() {
        String script = '''
            def foo = { it }
            def bar = 2
            ~foo(~bar)
        '''
        String result = compileToScript(script)

        assert result.contains('~(foo.call(~( bar ) ))')
    }

    @Test
    void testRangeExpression() {
        String script = '''
            (1..4).each { println it }
            ('1'..'4').collect { it -> it }
        '''
        String result = compileToScript(script)

        assert result.contains('(1..4).each')
        assert result.contains("('1'..'4').collect({ java.lang.Object it ->")
    }

    @Test
    void testUnaryOperators() {
        String script = '''
            def x = 1
            def y = 2
            (boolean) !(-x + (+y--))
        '''
        String result = compileToScript(script)
        assert result.contains('((boolean) !(-x + +(y--)))')

        script = '''
            boolean x = false
            !x
        '''
        result = compileToScript(script)
        assert result.contains('!x')

        script = '''
            int x = 0
            -x
        '''
        result = compileToScript(script)
        assert result.contains('-x')

        script = '''
            int x = 0
            +x
        '''
        result = compileToScript(script)
        assert result.contains('+x')
    }

    @Test
    void testArrayHandling() {
        String script = '''
            String[] x = [] as String[]
            class MyClass {
                private final String[] arr = new String[0]
            }
        '''
        String result = compileToScript(script, CompilePhase.CLASS_GENERATION)

        assert result.contains('java.lang.String[] x = (([]) as java.lang.String[])')
        assert result.contains('private final java.lang.String[] arr')
        assert result.contains('arr = new java.lang.String[0]')
    }

    @Test
    void testArrayHandling_Multidimension() {
        String script = '''
            String[][] x = [] as String[][]
            String[][][] y = [] as String[][][]
            class MyClass {
                private final String[][] arr = new String[xSize()][3]
            }
        '''
        String result = compileToScript(script, CompilePhase.CLASS_GENERATION)

        assert result.contains('java.lang.String[][] x = (([]) as java.lang.String[][])')
        assert result.contains('java.lang.String[][][] y = (([]) as java.lang.String[][][])')
        assert result.contains('private final java.lang.String[][] arr')
        assert result.contains('arr = new java.lang.String[this.xSize(), 3]')
    }

    @Test
    void testForLoop() {
        String script = '''
            for (int x = 0; x < 10;( x )++) {
                println x
                continue
            }
        '''
        String result = compileToScript(script)

        assert result.contains('for (java.lang.Integer x = 0; x < 10; x++) {')
        assert result.contains('continue')
    }

    @Test
    void testForLoopEmptyParameter() {
        String script = '''
            int y = 0
            for (;;) {
                println y++
                if (y > 10) break
            }
        '''
        String result = compileToScript(script)

        assert result.contains('java.lang.Integer y = 0')
        assert result.contains('for (;;) {')
    }

    @Test
    void testPreAndPostFix() {
        String script = '''
            def x = 1, y = 2
            x++ + --y - --x++
        '''
        String result = compileToScript(script)

        assert result.contains('x++ + --y - --(x++)')
    }

    @Test
    void testMultipleAssignments() {
        String script = '''
            def (a, b, c, d) = [1, 2, 3, 4]
            def (int e, String f, int g, String h) = [1, '2', 3, '4']
            def (_, month, year) = "18th June 2009".split()
            Object y, z = [1, 2]
        '''
        String result = compileToScript(script)

        assert result.contains('def (java.lang.Object a, java.lang.Object b, java.lang.Object c, java.lang.Object d) = [1, 2, 3, 4]')
        assert result.contains("def (java.lang.Integer e, java.lang.String f, java.lang.Integer g, java.lang.String h) = [1, '2', 3, '4']")
        assert result.contains("def (java.lang.Object _, java.lang.Object month, java.lang.Object year) = '18th June 2009'.split()")
        assert result.contains('java.lang.Object y')
        assert result.contains('java.lang.Object z = [1, 2]')
    }

    @Test
    void testListsAndMaps() {
        String script = '''
            def (bar, x, bif, qux) = [1, 2, 3, 4]
            ['foo', "$bar", x, Math.min(5, 3)]
            ['foo', "$bar"] as String []
            ['foo': 'bar', 'baz': bif + qux]
        '''
        String result = compileToScript(script)

        assert result.contains("['foo', \"\$bar\", x, java.lang.Math.min(5, 3)]")
        assert result.contains("((['foo', \"\$bar\"]) as java.lang.String[])")
        assert result.contains("['foo': 'bar', 'baz': bif + qux ]")
    }

    @Test
    void testForEachLoop() {
        String script = '''
            for (int x : (1..10)) {
                println x
            }
        '''
        String result = compileToScript(script)

        assert result.contains('for (int x : (1..10)) {')
    }

    @Test
    void testSwitchStatements() {
        String script = '''import java.awt.Color
            switch (someMethod()) {
              case 1:
              case 2:                       break
              case "3": println '3'
              case "$four": println "$four";break
              case Color.red: print 'red';  break
              case foo(): 4+2;              break
              case bar: true;               break
              default:                      break
            }

            switch (testDefault) {
              case 1: 'foo'
            }
        '''
        String result = compileToScript(script)

        assert result.contains('switch (this.someMethod())')
        assert result =~ /case 1:\s*case 2:\s*break/

        script = '''\
            switch (1) {
                case 1: break;
            }
        '''
        result = compileToScript(script)

        assert !result.contains('default:')
    }

    @Test
    void testLogAnnotation() {
        String script = '''
            @groovy.util.logging.Log
            class Event {
                def logMethod() {
                    log.fine(someMethod())
                }
            }
        '''
        String result = compileToScript(script, CompilePhase.CLASS_GENERATION)

        assert result.contains('private static final transient java.util.logging.Logger log')
        assert result.contains("log = java.util.logging.Logger.getLogger('Event')")
        assert result.contains('return log.isLoggable(java.util.logging.Level.@FINE) ? log.fine(this.someMethod()) : null')
    }

    @Test
    void testFieldDeclarationWithValue() {
        String script = '''
           class Event {
               String foo = 'xyz'
               static String bar = '123'
           }
        '''
        String result = compileToScript(script, CompilePhase.CLASS_GENERATION)

        assert result.contains('private java.lang.String foo')
        assert result.contains('private static java.lang.String bar')
        assert result.contains("foo = 'xyz'")
        assert result.contains("bar = '123'")
    }

    @Test
    void testClassAndProperties() {
        String script = '''
            class Event {
                String title
            }
        '''
        String result = compileToScript(script, CompilePhase.CLASS_GENERATION)

        assert result.contains('public void setTitle(java.lang.String value) {')
        assert result.contains('public java.lang.String getTitle() {')
        assert result.contains('private transient groovy.lang.MetaClass metaClass')
        assert result.contains('metaClass = /*BytecodeExpression*/')
    }

    @Test
    void testClassAnnotations() {
        String script = '''import org.codehaus.groovy.transform.*
            @SuppressWarnings
            @SuppressWarnings('some parameter')
            @SuppressWarnings(['p1', 'p2'])
            @MyAnnotation(classes = [String, Integer])
            @groovy.transform.ToString(includeFields = true, includeNames = false)
            class Event {
            }

            @interface MyAnnotation {
                Class[] classes() default [];
            }
        '''
        String result = compileToScript(script)

        assert result.contains('@java.lang.SuppressWarnings')
        assert result.contains("@java.lang.SuppressWarnings(value = 'some parameter')")
        assert result.contains("@java.lang.SuppressWarnings(value = ['p1', 'p2'])")
        assert result.contains('@MyAnnotation(classes = [java.lang.String, java.lang.Integer])')
        assert result.contains('@groovy.transform.ToString(') &&
                result.contains('includeFields = true') &&
                result.contains('includeNames = false')
    }

    @Test
    void testMethodAnnotations() {
        String script = '''import org.codehaus.groovy.transform.*
            class Event {
                @SuppressWarnings
                @SuppressWarnings('some parameter')
                @SuppressWarnings(['p1', 'p2'])
                @MyAnnotation(classes = [String, Integer])
                @MyAnnotation(p1 = true, p2 = false)
                def method() {}
            }

            @interface MyAnnotation {
                Class[] classes() default [];
                boolean p1() default false;
                boolean p2() default false;
            }
        '''
        String result = compileToScript(script)

        assert result.contains('@java.lang.SuppressWarnings')
        assert result.contains("@java.lang.SuppressWarnings(value = 'some parameter')")
        assert result.contains("@java.lang.SuppressWarnings(value = ['p1', 'p2'])")
        assert result.contains('@MyAnnotation(classes = [java.lang.String, java.lang.Integer])')
        assert result.contains('@MyAnnotation(') && result.contains('p2 = false') && result.contains('p1 = true')
    }

    @Test
    void testPropertyAnnotations() {
        String script = '''import org.codehaus.groovy.transform.*
            class Event {
                @SuppressWarnings
                @SuppressWarnings('some parameter')
                @SuppressWarnings(['p1', 'p2'])
                @MyAnnotation(classes = [String, Integer])
                @MyAnnotation(p1 = true, p2 = false)
                String myString
            }

            @interface MyAnnotation {
                Class[] classes() default [];
                boolean p1() default false;
                boolean p2() default false;
            }
        '''
        String result = compileToScript(script)

        assert result.contains('@java.lang.SuppressWarnings')
        assert result.contains("@java.lang.SuppressWarnings(value = 'some parameter')")
        assert result.contains("@java.lang.SuppressWarnings(value = ['p1', 'p2'])")
        assert result.contains('@MyAnnotation(classes = [java.lang.String, java.lang.Integer])')
        assert result.contains('@MyAnnotation(') && result.contains('p2 = false') && result.contains('p1 = true')
    }

    @Test
    void testPackageAnnotations() {
        String script = '''
            @SuppressWarnings
            @SuppressWarnings('some parameter')
            @SuppressWarnings(['p1', 'p2'])
            @MyAnnotation(classes = [String, Integer])
            @MyAnnotation(p1 = true, p2 = false)
            package foo.bar.baz

            'hello world'

            @interface MyAnnotation {
                Class[] classes() default [];
                boolean p1() default false;
                boolean p2() default false;
            }
        '''
        String result = compileToScript(script)

        assert result.contains('@java.lang.SuppressWarnings')
        assert result.contains("@java.lang.SuppressWarnings(value = 'some parameter')")
        assert result.contains("@java.lang.SuppressWarnings(value = ['p1', 'p2'])")
        assert result.contains('@foo.bar.baz.MyAnnotation(classes = [java.lang.String, java.lang.Integer])')
        assert result.contains('@foo.bar.baz.MyAnnotation(') && result.contains('p2 = false') && result.contains('p1 = true')
        assert result.contains('package foo.bar.baz')
    }

    @Test
    void testImportStatements() {
        String script = '''
            @SuppressWarnings
            @SuppressWarnings('some parameter')
            import java.lang.String

            @SuppressWarnings(['p1', 'p2'])
            import static java.lang.Integer.MAX_VALUE

            @SuppressWarnings
            import java.util.concurrent.*

            @SuppressWarnings
            import static java.lang.Float.*

            @SuppressWarnings
            import static java.lang.Double.POSITIVE_INFINITY

            @SuppressWarnings
            import static java.lang.Math.PI as PLOP

            @SuppressWarnings
            import java.lang.Double as BadaBing

            'hello world'
        '''
        String result = compileToScript(script)

        assert result.contains("@java.lang.SuppressWarnings(value = ['p1', 'p2'])")
        assert result.contains('import static java.lang.Integer.MAX_VALUE')
        assert result.contains('@java.lang.SuppressWarnings')
        assert result.contains('import static java.lang.Double.POSITIVE_INFINITY')
        assert result.contains('@java.lang.SuppressWarnings')
        assert result.contains('import static java.lang.Math.PI as PLOP')
        assert result.contains('@java.lang.SuppressWarnings')
        assert result.contains('import static java.lang.Float.*')
        assert result.contains('@java.lang.SuppressWarnings')
        assert result.contains('import java.lang.Double as BadaBing')
        assert result.contains('@java.lang.SuppressWarnings')
        assert result.contains("@java.lang.SuppressWarnings(value = 'some parameter')")
        assert result.contains('import java.lang.String')
        assert result.contains('@java.lang.SuppressWarnings')
        assert result.contains('import java.util.concurrent.*')
    }

    @Test
    void testParameterAnnotations() {
        String script = '''
            def method(@SuppressWarnings @SuppressWarnings('foo') parameter) {
            }
        '''
        String result = compileToScript(script)

        assert result.contains("public java.lang.Object method(@java.lang.SuppressWarnings @java.lang.SuppressWarnings(value = 'foo') java.lang.Object parameter) {")
    }

    @Test
    void testParenthesisInArgumentList() {
        String script = '''
            public java.lang.String toString() {
                _result = new java.lang.StringBuffer()
                _result.append(Event)
                _result.append('(')
                _result.append(')')
                return _result.toString()
            }
        '''
        String result = compileToScript(script, CompilePhase.CLASS_GENERATION)

        assert result.contains("_result.append('(')")
        assert result.contains("_result.append(')')")
    }

    @Test
    void testStaticMethods() {
        String script = "Math.min(5, Math.min('str', 2))"
        String result = compileToScript(script)

        assert result.contains("java.lang.Math.min(5, java.lang.Math.min('str', 2))")
    }

    @Test
    void testAtImmutableClass() {
        String script = '@groovy.transform.Immutable class Event { }'
        String result = compileToScript(script, CompilePhase.CANONICALIZATION)

        assert result.contains('private int $hash$code')
        assert result.contains('public Event(java.util.Map args)')
        // assert toString()... quotations marks were a hassle
        assert result.contains('public java.lang.String toString()')
        assert result.contains("_result.append('Event(')")
        assert result.contains("_result.append(')')")
        assert result.contains('return $to$string')
    }

    @Test
    void testToStringClassAndStaticMethodCallExpression() {
        String script = '@groovy.transform.ToString class Event { Date when }'
        String result = compileToScript(script, CompilePhase.CANONICALIZATION)

        // we had problems with the ast transform passing a VariableExpression as StaticMethodCallExpression arguments
        assert result.contains("_result.append(org.codehaus.groovy.runtime.FormatHelper.toString(this.getWhen())")
    }

    @Test
    void testAtImmutableClassWithProperties() {
        String script = '''
            import groovy.transform.Immutable
            @Immutable class Event {
                String title
                Date when
                java.awt.Color color
            }
        '''
        String result = compileToScript(script, CompilePhase.CLASS_GENERATION)

        assert result.contains('private final java.lang.String title')
        assert result.contains('private final java.util.Date when')
        assert result.contains('private final java.awt.Color color')

        // assert hashCode
        assert result.contains('public int hashCode()')
        assert result.contains('java.lang.Integer _result = org.codehaus.groovy.util.HashCodeHelper.initHash()')
        assert result.contains("_result = org.codehaus.groovy.util.HashCodeHelper.updateHash(_result, this.getTitle())")
        assert result.contains("_result = org.codehaus.groovy.util.HashCodeHelper.updateHash(_result, this.getWhen())")
        assert result.contains("_result = org.codehaus.groovy.util.HashCodeHelper.updateHash(_result, this.getColor())")

        // assert clones
        if (CompilerConfiguration.DEFAULT.isIndyEnabled()) {
            assert result =~ /this.when = \(\(java.util.Date\)\s*when ..BytecodeExpression../
        } else {
            assert result.contains("((org.codehaus.groovy.runtime.InvokerHelper.invokeMethod(when, 'clone', null)) as java.util.Date)")
        }
    }

    @Test
    void testAnonymousInnerClass() {
        String script = '''
            new Object() {
                public String toString() { 'foo' }
            }
        '''
        String result = compileToScript(script, CompilePhase.CANONICALIZATION)

        assert result =~ /new script[0-9]+\$1\(this\)/
        assert result =~ /class script[0-9]+\$1/
        assert result =~ /public java\.lang\.String toString\(\)/
    }

    @Test
    void testLazyAnnotation() {
        String script = '''
            class Event {
                @Lazy ArrayList speakers
            }
        '''
        String result = compileToScript(script, CompilePhase.CANONICALIZATION)

        assert result =~ /Lazy\s*(?:@groovy\.transform\.Internal\s*)?private java\.util\.ArrayList .*speakers /
        assert result.contains('public java.util.ArrayList getSpeakers() {')
        assert result.contains('if ($speakers != null') || result.contains('if (null != $speakers')
        assert result.contains('$speakers = new java.util.ArrayList()')
    }

    @Test
    void testDelegateClass() {
        String script = '''
            class Event {
                @Delegate Date when
            }
        '''
        String result = compileToScript(script, CompilePhase.CLASS_GENERATION)

        assert result =~ /groovy\.lang\.Delegate\s*private java\.util\.Date when/
        assert result =~ /public int compareTo\(java\.util\.Date (param|arg)0\)/
        assert result.contains('public void setWhen(java.util.Date value) {')
        assert result.contains('public java.util.Date getWhen() {')
    }

    @Test
    void testAnnotationWithValueClass() {
        String script = '''
            class Event {
                @SuppressWarnings('unchecked')
                @Delegate(interfaces=false, deprecated=true) Date when
            }
        '''
        String result = compileToScript(script)

        assert result.contains("@java.lang.SuppressWarnings(value = 'unchecked')")
        assert result.contains('@groovy.lang.Delegate(deprecated = true, interfaces = false)') || result.contains('@groovy.lang.Delegate(interfaces = false, deprecated = true)')
        assert result.contains('private java.util.Date when ')
    }

    @Test
    void testTryCatch() {
        String script = '''\
            try {
                throw new RuntimeException('message')
            } catch (RuntimeException e) {
                e.printStackTrace()
            } catch (Exception e) {
                e.printStackTrace()
            } finally {
                true
            }
        '''
        String result = compileToScript(script)

        assert result.contains('try {')
        assert result.contains("throw new java.lang.RuntimeException('message')")
        assert result.contains('catch (java.lang.Exception e) {')
        assert result.contains('catch (java.lang.RuntimeException e) {')
        assert result.contains('finally {')

        script = '''\
            try {
                println 123
            } catch (e) {
                println 234
            }'''
        result = compileToScript(script)

        assert !result.contains('finally {')
    }

    @Test
    void testSuperAndThisCalls() {
        String script = '''
            class MyClass {
                MyClass() {
                    this('foo')
                }
                MyClass(foo) {
                    super(foo)
                }
            }
        '''
        String result = compileToScript(script)

        assert result.contains("this ('foo')")
        assert result.contains('super(foo)')
        assert result.contains('public MyClass(java.lang.Object foo) {')
    }

    @Test
    void testTryCatchFinally() {
        String script = '''\
            try {
                false
            } finally {
                true
            }
        '''
        String result = compileToScript(script)

        assert result =~ /try\s*\{\s*false\s*\}\s*finally\s*\{\s*true\s*\}/
    }

    @Test
    void testSynchronizedBlock() {
        String script = '''\
            synchronized(this) {
                synchronized(that) {
                    true
                }
            }
        '''
        String result = compileToScript(script)

        assert result.contains('synchronized ( this ) {')
        assert result.contains('    synchronized ( that ) {')
        assert result.contains('        true')
        assert result.contains('        true')
        assert result.contains('    }')
        assert result.contains('}')
    }

    @Test
    void testTernaryOperaters() {
        String script = """true || false ? 'y' : 'n'
                            foo ?: 'y'"""
        String result = compileToScript(script)
        assert result.contains("true || false ? 'y' : 'n'")
        assert result.contains("foo ?: 'y'")
    }

    @Test
    void testWhileLoop() {
        String script = '''while ("foo") println 5
                            while ("foo") {println 5; println 5; break; }'''

        String result = compileToScript(script)
        assert result.contains("while ('foo') {")
    }

    @Test
    void testDoWhileLoop() {
        def doWhile = new DoWhileStatement(
                new BooleanExpression(constX(true)),
                new BlockStatement(
                        [stmt(callThisX('println', args(constX('value'))))],
                        new VariableScope()
                ))

        StringWriter writer = new StringWriter()
        new AstNodeToScriptVisitor(writer).visitDoWhileLoop doWhile
        String result = writer.toString()

        assert result.contains('do {')
        assert result.contains('} while (true)')
    }

    @Test
    void testAssertStatement() {
        String script = '''
            assert 1 == 1; assert true == foo() : 'message'
        '''
        String result = compileToScript(script)

        assert result.contains('assert 1 == 1 : null')
        assert result.contains("assert true == this.foo() : 'message'")
    }

    @Test
    void testMethodPointer() {
        String script = '''
            class Event {
                static def staticMethod(it) { it * it }
                def instanceMethod(it) { it / it }
            }

            def m1 = Event.&'staticMethod'
            assert 25 == m1(5)
            def m2 = new Event().&'instanceMethod'
            assert 1 == m2(5)
            def e = new Event()
            def m3 = e.&'instanceMethod'
            assert 1 == m3(6)
        '''
        String result = compileToScript(script)

        assert result.contains("java.lang.Object m1 = Event.&'staticMethod'")
        assert result.contains('assert 25 == m1.call(5) : null')
        assert result.contains("java.lang.Object m2 = new Event().&'instanceMethod'")
        assert result.contains('assert 1 == m2.call(5) : null')
        assert result.contains('java.lang.Object e = new Event()')
        assert result.contains("java.lang.Object m3 = e .&'instanceMethod'")
        assert result.contains('assert 1 == m3.call(6) : null')
    }

    @Test
    void testAssertRegexExpression() {
        String script = '''
            assert "abc.def" =~ /[a-z]b[a-z]\\.def/
            assert "cheesecheese" =~ "cheese"
        '''
        String result = compileToScript(script)

        // the backslash in the value is now properly escaped so the literal round-trips
        assert result.contains("assert 'abc.def' =~ '[a-z]b[a-z]\\\\.def' : null")
        assert result.contains("assert 'cheesecheese' =~ 'cheese' : null")
    }

    @Test
    void testArrayExpression() {
        String script = '''
            def x = [4, 5, 6] as String[]
            [1, 2, 3] << new Integer[x.length]
        '''
        String result = compileToScript(script)

        assert result.contains('java.lang.Object x = (([4, 5, 6]) as java.lang.String[])')
        assert result.contains('[1, 2, 3] << new java.lang.Integer[x.length]')
    }

    @Test
    void testSpreadDot() {
        String script = '''
            def x = [ ['a':11, 'b':12], ['a':21, 'b':22] ]
            assert x.a == [11, 21] //GPath notation
            assert x*.a == [11, 21] //spread dot notation
        '''
        String result = compileToScript(script)

        assert result.contains("java.lang.Object x = [['a': 11, 'b': 12], ['a': 21, 'b': 22]]")
        assert result.contains('assert x.a == [11, 21] : null')
        assert result.contains('assert x*.a == [11, 21] : null')
    }

    @Test
    void testSpreadNotationNullHandling() {
        String script = '''
            def x = [ ['a':11, 'b':12], ['a':21, 'b':22], null ]
            assert x*.a == [11, 21, null] //caters for null values
            assert x*.a == x.collect{ it?.a } //equivalent notation
        '''
        String result = compileToScript(script)

        assert result.contains("java.lang.Object x = [['a': 11, 'b': 12], ['a': 21, 'b': 22], null]")
        assert result.contains('assert x*.a == [11, 21, null] : null')
        assert result.contains('assert x*.a == x.collect({ ')
        assert result.contains('it?.a')
        assert result.contains('}) : null')
    }

    @Test
    void testSpreadNotationAdvanced() {
        String script = '''
            class MyClass{ def getA(){ 'abc' } }
            def x = [ ['a':21, 'b':22], null, new MyClass() ]
            assert x*.a == [21, null, 'abc'] //properties treated like map subscripting
        '''
        String result = compileToScript(script)

        assert result.contains("java.lang.Object x = [['a': 21, 'b': 22], null, new MyClass()]")
        assert result.contains("assert x*.a == [21, null, 'abc'] : null")
    }

    @Test
    void testSpreadNotationForMethodsOnLists() {
        String script = '''
            class MyClass{ def getA(){ 'abc' } }
            def c1= new MyClass(), c2= new MyClass()
            assert [c1, c2]*.getA() == [c1.getA(), c2.getA()]
             //spread dot also works for method calls
            assert [c1, c2]*.getA() == ['abc', 'abc']
        '''
        String result = compileToScript(script)

        assert result.contains('java.lang.Object c1 = new MyClass()')
        assert result.contains('java.lang.Object c2 = new MyClass()')
        assert result.contains('assert [c1, c2]*.getA() == [c1.getA(), c2.getA()] : null')
        assert result.contains("assert [c1, c2]*.getA() == ['abc', 'abc'] : null")
    }

    @Test
    void testVisitSafeMethodCall() {
        String script = 'someMethod()?.somethingElse()*.sum()'
        String result = compileToScript(script)

        assert result.contains('someMethod()?.somethingElse()*.sum()')
    }

    @Test
    void testSpreadNotationInMapDefinition() {
        String script = '''
            assert ['z':900, *:['a':100, 'b':200], 'a':300] == ['a':300, 'b':200, 'z':900]
            assert [ *:[3:3, *:[5:5] ], 7:7] == [3:3, 5:5, 7:7]
        '''
        String result = compileToScript(script)

        assert result.contains("assert ['z': 900, *: ['a': 100, 'b': 200], 'a': 300] == ['a': 300, 'b': 200, 'z': 900] : null")
        assert result.contains('assert [*: [3: 3, *: [5: 5]], 7: 7] == [3: 3, 5: 5, 7: 7] : null')
    }

    @Test
    void testSpreadNotationInClosure() {
        String script = '''
            def f(){ [ 1:'u', 2:'v', 3:'w' ] }
            assert [*:f(), 10:'zz'] == [1:'u', 10:'zz', 2:'v', 3:'w']
            def f(m){ m.c }
            assert f(*:['a':10, 'b':20, 'c':30], 'e':50) == 30

            def f(m, i, j, k){ [m, i, j, k] }
            assert f('e':100, *[4, 5], *:['a':10, 'b':20, 'c':30], 6) == [ ["e":100, "b":20, "c":30, "a":10], 4, 5, 6 ]
        '''
        String result = compileToScript(script)

        assert result.contains("assert [*: this.f(), 10: 'zz'] == [1: 'u', 10: 'zz', 2: 'v', 3: 'w'] : null")
        assert result.contains("assert this.f([*: ['a': 10, 'b': 20, 'c': 30], 'e': 50]) == 30 : null")
        assert result.contains("assert this.f(['e': 100, *: ['a': 10, 'b': 20, 'c': 30]], *[4, 5], 6) == [['e': 100, 'b': 20, 'c': 30, 'a': 10], 4, 5, 6] : null")
    }

    // GROOVY-4636
    @Test
    void testObjectInitializers() {
        String script = '''
            class A {
                def v
                A() { v = 1 }
                { v = 2 }
            }
        '''
        String result = compileToScript(script)
        assert result =~ /\{\s*v = 2\s*\}/

        // During class gen the initializers should have been merged in with the ctors
        result = compileToScript(script, CompilePhase.CLASS_GENERATION)
        assert result =~ /(?s)public A\(\) \{.*?v = 2\s*v = 1\s*\}/
    }

    @Test
    void testStatementLabels() {
        String script = '''
            label1:
            label1a:
            for (int x : (1..10)) {
                if (x == 5) {
                    break label1
                }
                if (x == 6) {
                    continue label1a
                }
                println x
            }

            label2:
            while (true) {
                switch(hashCode()) {
                    case 0: break label2
                    case 1: break
                }
                break
            }

            label3:
            if (hashCode() > 0) {
                for (x in [1,2,3]) {
                    if (x > 2) {
                        break label3
                    }
                }
            }

            label4:
            switch (hashCode()) { }

            label5:
            try { } catch (any) { }

            label6:
            synchronized (this) { }
        '''
        String result = compileToScript(script)

        assert result.contains('label1a:\nlabel1:\nfor (int x : (1..10)) {')
        assert result.contains('break label1')
        assert result.contains('continue label1a')
        assert result.contains('label2:\nwhile (true)')
        assert result.contains('break label2')
        assert result.contains('label3:\nif (this.hashCode() > 0)')
        assert result.contains('label4:\nswitch (this.hashCode())')
        assert result.contains('label5:\ntry {')
        assert result.contains('label6:\nsynchronized')
    }

    @Test
    void testStatementLabelsForDoWhileLoop() {
        def doWhile = new DoWhileStatement(
                new BooleanExpression(constX(true)),
                new BlockStatement(
                        [stmt(callThisX('println', args(constX('value'))))],
                        new VariableScope()
                ))
        doWhile.addStatementLabel('label1')

        StringWriter writer = new StringWriter()
        new AstNodeToScriptVisitor(writer).visitDoWhileLoop doWhile
        String result = writer.toString()

        assert result.contains('label1:\ndo {')
        assert result.contains('} while (true)')
    }

    @Test
    void testNestedObjectInitializers() {
        String script = '''
            class A {
                def v
                A() { v = 1 }
                {
                    v = 2
                    // nested block must be labeled to avoid appearing as a closure
                    oi:
                    {
                        v += 2
                    }
                }
            }
        '''
        String result = compileToScript(script)

        assert result =~ /\{\s*v = 2/
        assert result =~ /(?s)oi:.*?\{.*?v \+= 2.*?\}/
    }

    @Test
    void testVisitIfElse() {
        String script = '''
            String a = 'foo'
            if (a instanceof String) {}
        '''
        String result = compileToScript(script)

        assert result.contains('if (a instanceof java.lang.String) {')
    }

    @Test
    void testVisitCastExpression() {
        String script = '''
            String a = 'foo'
            a as String
        '''
        String result = compileToScript(script)

        assert result.contains('(a as java.lang.String)')
    }

    // GROOVY-12144
    @Test
    void testNestedGenerics() {
        String script = '''
            Map<String, List<Integer>> nested() { null }
        '''
        String result = compileToScript(script)

        assert result.contains('java.util.Map<String, List<Integer>> nested()')
    }

    // GROOVY-12144
    @Test
    void testRangeExpressionExclusiveBounds() {
        String script = '''
            def a = 1..5
            def b = 1..<5
            def c = 1<..5
            def d = 1<..<5
        '''
        String result = compileToScript(script)

        assert result.contains('(1..5)')
        assert result.contains('(1..<5)')
        assert result.contains('(1<..5)')
        assert result.contains('(1<..<5)')
    }

    // GROOVY-12144
    @Test
    void testElvisOperator() {
        String script = '''
            def a = c ?: d
        '''
        String result = compileToScript(script)

        assert result.contains('c ?: d')
        assert !result.contains('c ? c : d')
    }

    // GROOVY-12144
    @Test
    void testAttributeExpression() {
        String script = '''
            def a = other.@order
            def b = foos*.@order
            def c = other?.@order
        '''
        String result = compileToScript(script)

        assert result.contains('other.@order')
        assert result.contains('foos*.@order')
        assert result.contains('other?.@order')
    }

    // GROOVY-12144
    @Test
    void testClosureParameterArrow() {
        String script = '''
            def implicitIt = { it * 2 }
            def noArgs = { -> 'x' }
        '''
        String result = compileToScript(script)

        // explicit zero-argument closure keeps its arrow (otherwise it would gain an implicit 'it')
        assert result.contains('{ ->')
        // an implicit-'it' closure has no arrow
        assert result =~ /\{\s*it \* 2/
    }

    // GROOVY-12144
    @Test
    void testSafeIndexAccess() {
        String script = '''
            def a = list?[0]
            def b = map?['key']
            list?[1] = 42
        '''
        String result = compileToScript(script)

        assert result.contains('list?[0]')
        assert result.contains("map?['key']")
        assert result.contains('list?[1] = 42')
    }

    // GROOVY-12144
    @Test
    void testNumericLiteralSuffixes() {
        String script = '''
            def a = 42L
            def b = 2.5f
            def c = 3.5d
            def d = 10G
            def e = 42
            def f = 2.5
        '''
        String result = compileToScript(script)

        assert result.contains('= 42L')
        assert result.contains('= 2.5F')
        assert result.contains('= 3.5D')
        assert result.contains('= 10G')
        // Integer and BigDecimal are the literal defaults and carry no suffix
        assert result.contains('= 42\n')
        assert result.contains('= 2.5\n')
    }

    // GROOVY-12144
    @Test
    void testExplicitMethodTypeArguments() {
        String script = '''
            def a = Collections.<String>emptyList()
        '''
        String result = compileToScript(script)

        assert result.contains('java.util.Collections.<String>emptyList()')
    }

    @Test
    void testClosureShowsGeneratedClassAtClassGeneration() {
        String script = '''
            class Demo {
                def outer() { [1, 2].each { a -> [3, 4].each { b -> a + b } } }
            }
        '''
        String result = compileToScript(script, CompilePhase.CLASS_GENERATION)

        // each closure literal is annotated with the synthetic class it was compiled to,
        // including the nesting of an inner closure within its enclosing closure class
        assert result.contains('/* => Demo$_outer_closure1 */')
        assert result.contains('/* => Demo$_outer_closure1$_closure2 */')
    }

    @Test
    void testClosureGeneratedClassMarkAbsentBeforeClassGeneration() {
        String script = '''
            class Demo {
                def outer() { [1, 2].each { it } }
            }
        '''
        // the closure class does not exist yet, so nothing is rendered
        assert !compileToScript(script, CompilePhase.SEMANTIC_ANALYSIS).contains('/* => ')
    }
}
