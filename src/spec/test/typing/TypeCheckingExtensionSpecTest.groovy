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
package typing

import groovy.test.GroovyAssert
import groovy.transform.TypeChecked
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.xml.MarkupBuilder
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.junit.jupiter.api.Test

import static asciidoctor.Utils.stripAsciidocMarkup
import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class TypeCheckingExtensionSpecTest {

    @Test
    void testIntro() {
        def out = new PrintWriter(new ByteArrayOutputStream())
        // tag::intro_stc_extensions[]
        def builder = new MarkupBuilder(out)
        builder.html {
            head {
                // ...
            }
            body {
                p 'Hello, world!'
            }
        }
        // end::intro_stc_extensions[]
    }

    @Test
    void testSetup() {
        assertScriptWithExtension 'setup.groovy', '''
            1+1
        '''
    }

    @Test
    void testFinish() {
        assertScriptWithExtension 'finish.groovy', '''
            1+1
        '''
    }

    @Test
    void testUnresolvedVariable() {
        assertScriptWithExtension 'unresolvedvariable.groovy', '''
            assert people.size() == 2
        ''', {
            it.setVariable('people', ['John','Meg'])
        }
    }

    @Test
    void testUnresolvedProperty() {
        use (SpecSupport) {
            assertScriptWithExtension 'unresolvedproperty.groovy', '''
                assert 'string'.longueur == 6
            '''
        }
    }

    @Test
    void testUnresolvedAttribute() {
        try {
            assertScriptWithExtension 'unresolvedattribute.groovy', '''
                assert 'string'.@longueur == 6
            '''
            assert false
        } catch (MissingFieldException mfe) {
            // ok
        }
    }

    @Test
    void testBeforeMethodCall() {
        try {
            assertScriptWithExtension 'beforemethodcall.groovy', '''
                'string'.toUpperCase()
            '''
            assert false
        } catch (MultipleCompilationErrorsException err) {
            assert err.message.contains('[Static type checking] - Not allowed')
        }
    }

    @Test
    void testAfterMethodCall() {
        try {
            assertScriptWithExtension 'aftermethodcall.groovy', '''
                'string'.toUpperCase()
            '''
            assert false
        } catch (MultipleCompilationErrorsException err) {
            assert err.message.contains('[Static type checking] - Not allowed')
        }
    }

    @Test
    void testOnMethodSelection() {
        try {
            assertScriptWithExtension 'onmethodselection.groovy', '''
                'string'.toUpperCase()
                'string 2'.toLowerCase()
                'string 3'.length()
            '''
            assert false
        } catch (MultipleCompilationErrorsException err) {
            assert err.message.contains('[Static type checking] - You can use only 2 calls on String in your source code')
        }
    }

    @Test
    void testMethodNotFound() {
        use (SpecSupport) {
            assertScriptWithExtension 'methodnotfound.groovy', '''
                assert 'string'.longueur() == 6
            '''
        }
    }

    @Test
    void testBeforeVisitMethod() {
        use (SpecSupport) {
            assertScriptWithExtension 'beforevisitmethod.groovy', '''
                void skipIt() {
                    'blah'.doesNotExist()
                }
                skipIt()
            '''
        }
    }

    @Test
    void testAfterVisitMethod() {
        try {
            assertScriptWithExtension 'aftervisitmethod.groovy', '''
                void foo() {
                   'string'.toUpperCase()
                   'string 2'.toLowerCase()
                   'string 3'.length()
                }
                foo()
            '''
            assert false
        } catch (MultipleCompilationErrorsException err) {
            assert err.message.contains('[Static type checking] - Method foo contains more than 2 method calls')
        }
    }

    @Test
    void testBeforeVisitClass() {
        try {
            assertScriptWithExtension 'beforevisitclass.groovy', '''
                class someclass {
                }
            '''
            assert false
        } catch (MultipleCompilationErrorsException err) {
            assert err.message.contains("[Static type checking] - Class 'someclass' doesn't start with an uppercase letter")
        }
    }

    @Test
    void testAfterVisitClass() {
        try {
            assertScriptWithExtension 'aftervisitclass.groovy', '''
                class someclass {
                }
            '''
            assert false
        } catch (MultipleCompilationErrorsException err) {
            assert err.message.contains("[Static type checking] - Class 'someclass' doesn't start with an uppercase letter")
        }
    }

    @Test
    void testIncompatibleAssignment() {
        assertScriptWithExtension 'incompatibleassignment.groovy', '''
            import groovy.transform.TypeChecked
            import groovy.transform.TypeCheckingMode

            @TypeChecked(TypeCheckingMode.SKIP)
            class Point {
                int x, y

                void setProperty(String name, value) {
                    def v = value instanceof Closure ? value() : value
                    this.@(name) *= v // set field to prevent recursion
                }
            }

            def p = new Point(x: 3, y: 4)
            p.x = { 2 } // allowed by setProperty
            assert p.x == 6
        '''
    }

    @Test
    void testIncompatibleReturnType() {
        assertScriptWithExtension 'incompatiblereturntype.groovy', '''
            Closure<Date> c = { '1' }
            Date m() { '1' }
        '''
    }

    @Test
    void testAmbiguousMethods() {
        def err = shouldFail {
            assertScriptWithExtension 'ambiguousmethods.groovy', '''
                int foo(Integer x) { 1 }
                int foo(String s) { 2 }
                int foo(Date d) { 3 }
                assert foo(null) == 2
            '''
        }
        assert err.message =~ /Cannot resolve which method to invoke for \[null\] due to overlapping prototypes/
    }

    @Test
    void testSupportMethods() {
        assertScriptWithExtension 'selfcheck.groovy', '''
            class Foo {}
            1+1
        '''
    }

    @Test
    void testNewMethod() {
        assertScriptWithExtension 'newmethod.groovy','''
            class Foo {
                def methodMissing(String name, args) { this }
            }
            def f = new Foo()
            f.foo().bar()
        '''
    }

    @Test
    void testScopingMethods() {
        assertScriptWithExtension 'scoping.groovy','''
            1+1
        '''
        assertScriptWithExtension 'scoping_alt.groovy','''
            1+1
        '''
    }

    //--------------------------------------------------------------------------

    @Test
    void testRobotExample() {
        def err = shouldFail(MultipleCompilationErrorsException, '''import groovy.transform.TypeChecked
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import typing.Robot

def script = """
// tag::example_robot_script[]
robot.move 100
// end::example_robot_script[]
"""

// tag::example_robot_setup[]
def config = new CompilerConfiguration()
config.addCompilationCustomizers(
    new ASTTransformationCustomizer(TypeChecked)            // <1>
)
def shell = new GroovyShell(config)                         // <2>
def robot = new Robot()
shell.setVariable('robot', robot)
shell.evaluate(script)                                      // <3>
// end::example_robot_setup[]
''')
        assert err.message.contains(stripAsciidocMarkup('''
// tag::example_robot_expected_err[]
[Static type checking] - The variable [robot] is undeclared.
// end::example_robot_expected_err[]
'''))
    }

    @Test
    void testRobotExampleFixed() {
        assertScript '''import groovy.transform.TypeChecked
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import typing.Robot

def script = """
robot.move 100
"""

def config = new CompilerConfiguration()
// tag::example_robot_fixed_conf[]
config.addCompilationCustomizers(
    new ASTTransformationCustomizer(
        TypeChecked,
        extensions:['robotextension.groovy'])
)
// end::example_robot_fixed_conf[]
def shell = new GroovyShell(config)
def robot = new Robot()
shell.setVariable('robot', robot)
shell.evaluate(script)
'''
    }

    @Test
    void testRobotExamplePassWithCompileStatic() {
        assertScript '''import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import typing.Robot

def script = """
robot.move 100
"""

// tag::example_robot_setup_compilestatic[]
def config = new CompilerConfiguration()
config.addCompilationCustomizers(
    new ASTTransformationCustomizer(
        CompileStatic,                                      // <1>
        extensions:['robotextension.groovy'])               // <2>
)
def shell = new GroovyShell(config)
def robot = new Robot()
shell.setVariable('robot', robot)
shell.evaluate(script)
// end::example_robot_setup_compilestatic[]
'''
    }

    @Test
    void testRobotExampleDelegatingScript() {
        assertScript '''import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import typing.Robot

def script = """
// tag::example_robot_script_direct[]
move 100
// end::example_robot_script_direct[]
"""

// tag::example_robot_setup_dynamic[]
def config = new CompilerConfiguration()
config.scriptBaseClass = 'groovy.util.DelegatingScript'     // <1>
def shell = new GroovyShell(config)
def runner = shell.parse(script)                            // <2>
runner.setDelegate(new Robot())                             // <3>
runner.run()                                                // <4>
// end::example_robot_setup_dynamic[]
'''
    }

    @Test
    void testRobotExampleFailsWithCompileStatic() {
        def err = GroovyAssert.shouldFail '''import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import typing.Robot

def script = """
move 100
"""

def config = new CompilerConfiguration()
config.scriptBaseClass = 'groovy.util.DelegatingScript'
// tag::example_robot_setup_compilestatic2[]
config.addCompilationCustomizers(
    new ASTTransformationCustomizer(
        CompileStatic,                                      // <1>
        extensions:['robotextension2.groovy'])              // <2>
)
// end::example_robot_setup_compilestatic2[]
def shell = new GroovyShell(config)
def runner = shell.parse(script)
runner.setDelegate(new Robot())
runner.run()
'''
        err = "${err.class.name}: ${err.message}"
        assert err.contains(stripAsciidocMarkup('''
// tag::robot_runtime_error_cs[]
java.lang.NoSuchMethodError: java.lang.Object.move()Ltyping/Robot;
// end::robot_runtime_error_cs[]
''')) || err.contains('java.lang.NoSuchMethodError: \'typing.Robot java.lang.Object.move()\'')
    }

    @Test
    void testRobotExamplePassesWithCompileStatic() {
        assertScript '''import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import typing.Robot

def script = """
move 100
"""

def config = new CompilerConfiguration()
config.scriptBaseClass = 'groovy.util.DelegatingScript'
config.addCompilationCustomizers(
    new ASTTransformationCustomizer(
        CompileStatic,
        extensions:['robotextension3.groovy'])
)
def shell = new GroovyShell(config)
def runner = shell.parse(script)
runner.setDelegate(new Robot())
runner.run()
'''
    }

    @Test
    void testPrecompiledExtensions() {
        assertScript '''import groovy.transform.TypeChecked
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import typing.Robot

def script = """
robot.move 100
"""

def config = new CompilerConfiguration()
// tag::setup_precompiled[]
config.addCompilationCustomizers(
    new ASTTransformationCustomizer(
        TypeChecked,
        extensions:['typing.PrecompiledExtension'])
)
// end::setup_precompiled[]
def shell = new GroovyShell(config)
def robot = new Robot()
shell.setVariable('robot', robot)
shell.evaluate(script)
'''

        assertScript '''import groovy.transform.TypeChecked
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import typing.Robot

def script = """
robot.move 100
"""

def config = new CompilerConfiguration()
config.addCompilationCustomizers(
    new ASTTransformationCustomizer(
        TypeChecked,
        extensions:['typing.PrecompiledJavaExtension'])
)
def shell = new GroovyShell(config)
def robot = new Robot()
shell.setVariable('robot', robot)
shell.evaluate(script)
'''
    }

    //--------------------------------------------------------------------------

    void doDelegateResolutionForPropertyReadTest(String strategy, String expected) {
        assertScript """import groovy.transform.CompileStatic
class ADelegate {
    def x = "delegate"
}

@CompileStatic
class AClass {
    public <T> T closureExecuter(
            ADelegate d,
            @DelegatesTo(value = ADelegate, strategy = $strategy) Closure<T> c) {
        c.resolveStrategy = $strategy
        c.delegate = d
        return c()
    }

    def x = "owner"

    def test() {
        def theDelegate = new ADelegate()
        def res = closureExecuter(theDelegate) {
            return x
        }

        return res
    }
}
assert new AClass().test() == "$expected"
"""
    }

    void doDelegateResolutionForPropertyWriteTest(String strategy, String expected) {
        assertScript """import groovy.transform.CompileStatic
class ADelegate {
    def x = "delegate"
}

@CompileStatic
class AClass {
    public <T> T closureExecuter(
            ADelegate d,
            @DelegatesTo(value = ADelegate, strategy = $strategy) Closure<T> c) {
        c.resolveStrategy = $strategy
        c.delegate = d
        return c()
    }

    def x = "owner"

    def test() {
        def theDelegate = new ADelegate()
        def res = closureExecuter(theDelegate) {
            x = "changed"
        }

        return [theDelegate.x, this.x].toSet()
    }
}
def result = new AClass().test()
def expected = (["owner", "delegate", "changed"] - ["$expected"]).toSet()
assert expected == result
"""
    }

    @Test
    void testDelegateResolutionToPropertyWhenReadingUsingDelegateOnly() {
        doDelegateResolutionForPropertyReadTest("Closure.DELEGATE_ONLY", "delegate")
    }

    @Test
    void testDelegateResolutionToPropertyWhenReadingUsingDelegateFirst() {
        doDelegateResolutionForPropertyReadTest("Closure.DELEGATE_FIRST", "delegate")
    }

    @Test
    void testDelegateResolutionToPropertyWhenReadingUsingOwnerOnly() {
        doDelegateResolutionForPropertyReadTest("Closure.OWNER_ONLY", "owner")
    }

    @Test
    void testDelegateResolutionToPropertyWhenReadingUsingOwnerFirst() {
        doDelegateResolutionForPropertyReadTest("Closure.OWNER_FIRST", "owner")
    }

    @Test
    void testDelegateResolutionToPropertyWhenWritingUsingDelegateOnly() {
        doDelegateResolutionForPropertyWriteTest("Closure.DELEGATE_ONLY", "delegate")
    }

    @Test
    void testDelegateResolutionToPropertyWhenWritingUsingDelegateFirst() {
        doDelegateResolutionForPropertyWriteTest("Closure.DELEGATE_FIRST", "delegate")
    }

    @Test
    void testDelegateResolutionToPropertyWhenWritingUsingOwnerOnly() {
        doDelegateResolutionForPropertyWriteTest("Closure.OWNER_ONLY", "owner")
    }

    @Test
    void testDelegateResolutionToPropertyWhenWritingUsingOwnerFirst() {
        doDelegateResolutionForPropertyWriteTest("Closure.OWNER_FIRST", "owner")
    }

    @Test
    void testDelegateResolutionToPropertyWhenWritingInsideWith() {
        // Failing example provided by Jan Hackel (@jhunovis) in groovy slack
        // https://groovy-community.slack.com/files/U9CM8G6AJ/FAR1PJT1U/behavior_of__with__and___compilestatic.groovy

        assertScript '''import groovy.transform.CompileStatic
class DelegateTest {

  @CompileStatic
  private static class Person {
    String name
    int age

    Person copyWithName(String newName) {
      return new Person().with {
        name = newName
        age = this.age
        it
      }
    }
  }

  void delegate() {
    def oldTim = new Person().with {
      name = 'Tim Old'
      age = 20
      it
    }
    def newTim = new Person().with {
      name = 'Tim New'
      age = 20
      it
    }
    def copiedTim = oldTim.copyWithName('Tim New')
    assert oldTim.name == 'Tim Old'
    assert copiedTim.name == newTim.name
    assert copiedTim.age == newTim.age
  }
}
new DelegateTest().delegate()
'''
    }

    //--------------------------------------------------------------------------

    private static assertScriptWithExtension(String extensionName, String script,
            @ClosureParams(value=SimpleType, options='groovy.lang.Binding') Closure<Void> configurator=null) {
        def shell = GroovyShell.withConfig {
            ast(TypeChecked, extensions: [extensionName])
        }
        if (configurator) {
            configurator.call(shell.context)
        }
        shell.evaluate(script)
    }

    private static class SpecSupport {
        static int getLongueur(String self) { self.length() }
        static int longueur(String self) { self.length() }
        static void doesNotExist(String self) {}
    }
}
