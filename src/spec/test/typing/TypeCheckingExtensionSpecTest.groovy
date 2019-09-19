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

import groovy.$Temp
import groovy.test.GroovyAssert
import groovy.test.GroovyTestCase
import groovy.transform.TypeChecked
import groovy.xml.MarkupBuilder
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer

import static asciidoctor.Utils.stripAsciidocMarkup

class TypeCheckingExtensionSpecTest extends GroovyTestCase {

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
        assert err.contains(stripAsciidocMarkup('''
// tag::example_robot_expected_err[]
[Static type checking] - The variable [robot] is undeclared.
// end::example_robot_expected_err[]
'''))
    }

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

    void testSetup() {
        assertScriptWithExtension('setup.groovy', '''
            1+1
        ''')
    }

    void testFinish() {
        assertScriptWithExtension('finish.groovy', '''
            1+1
        ''')
    }

    void testUnresolvedVariable() {
        assertScriptWithExtension('unresolvedvariable.groovy', '''
            assert people.size() == 2
        ''') {
            it.setVariable('people', ['John','Meg'])
        }
    }

    void testUnresolvedProperty() {
        use (SpecSupport) {
            assertScriptWithExtension('unresolvedproperty.groovy', '''
            assert 'string'.longueur == 6
        ''')
        }
    }

    void testUnresolvedAttribute() {
        try {
            assertScriptWithExtension('unresolvedattribute.groovy', '''
            assert 'string'.@longueur == 6
        ''')
            assert false
        } catch (MissingFieldException mfe) {
            // ok
        }
    }

    void testBeforeMethodCall() {
        try {
            assertScriptWithExtension('beforemethodcall.groovy', '''
            'string'.toUpperCase()
        ''')
            assert false
        } catch (MultipleCompilationErrorsException err) {
            assert err.message.contains('[Static type checking] - Not allowed')
        }
    }

    void testAfterMethodCall() {
        try {
            assertScriptWithExtension('aftermethodcall.groovy', '''
            'string'.toUpperCase()
        ''')
            assert false
        } catch (MultipleCompilationErrorsException err) {
            assert err.message.contains('[Static type checking] - Not allowed')
        }
    }

    void testOnMethodSelection() {
        try {
            assertScriptWithExtension('onmethodselection.groovy', '''
            'string'.toUpperCase()
            'string 2'.toLowerCase()
            'string 3'.length()
        ''')
            assert false
        } catch (MultipleCompilationErrorsException err) {
            assert err.message.contains('[Static type checking] - You can use only 2 calls on String in your source code')
        }
    }

    void testMethodNotFound() {
        use (SpecSupport) {
            assertScriptWithExtension('methodnotfound.groovy', '''
            assert 'string'.longueur() == 6
        ''')
        }
    }

    void testBeforeVisitMethod() {
        use (SpecSupport) {
            assertScriptWithExtension('beforevisitmethod.groovy', '''
            void skipIt() {
                'blah'.doesNotExist()
            }
            skipIt()
        ''')
        }
    }

    void testAfterVisitMethod() {
        try {
            assertScriptWithExtension('aftervisitmethod.groovy', '''
            void foo() {
               'string'.toUpperCase()
               'string 2'.toLowerCase()
               'string 3'.length()
            }
            foo()
        ''')
            assert false
        } catch (MultipleCompilationErrorsException err) {
            assert err.message.contains('[Static type checking] - Method foo contains more than 2 method calls')
        }
    }

    void testBeforeVisitClass() {
        try {
            assertScriptWithExtension('beforevisitclass.groovy', '''
            class someclass {
            }
        ''')
            assert false
        } catch (MultipleCompilationErrorsException err) {
            assert err.message.contains("[Static type checking] - Class 'someclass' doesn't start with an uppercase letter")
        }
    }

    void testAfterVisitClass() {
        try {
            assertScriptWithExtension('aftervisitclass.groovy', '''
            class someclass {
            }
        ''')
            assert false
        } catch (MultipleCompilationErrorsException err) {
            assert err.message.contains("[Static type checking] - Class 'someclass' doesn't start with an uppercase letter")
        }
    }

    void testIncompatibleAssignment() {
        use (SpecSupport) {
            assertScriptWithExtension('incompatibleassignment.groovy', '''import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode

@TypeChecked(TypeCheckingMode.SKIP)
class Point {
    int x, y = 1

    void setProperty(String name, value) {
        def v = value instanceof Closure ? value() : value
        this.@"$name" *= v
    }
}

def p = new Point(x: 3, y: 4)
p.x = { 2 }
assert p.x == 6
        ''')
        }
    }

    void testAmbiguousMethods() {
        def err = shouldFail {
            assertScriptWithExtension('ambiguousmethods.groovy', '''
            int foo(Integer x) { 1 }
            int foo(String s) { 2 }
            int foo(Date d) { 3 }
            assert foo(null) == 2
        ''')
        }
        assert err.contains(/Cannot resolve which method to invoke for [null] due to overlapping prototypes/)
    }

    void testSupportMethods() {
        assertScriptWithExtension('selfcheck.groovy','''
            class Foo {}
            1+1
        ''')
    }

    void testNewMethod() {
        assertScriptWithExtension('newmethod.groovy','''
            class Foo {
                def methodMissing(String name, args) { this }
            }
            def f = new Foo()
            f.foo().bar()
        ''')
    }

    void testScopingMethods() {
        assertScriptWithExtension('scoping.groovy','''
            1+1
        ''')
        assertScriptWithExtension('scoping_alt.groovy','''
            1+1
        ''')
    }

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
'''))
    }

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

    void testDelegateResolutionToPropertyWhenReadingUsingDelegateOnly() {
        doDelegateResolutionForPropertyReadTest("Closure.DELEGATE_ONLY", "delegate")
    }

    void testDelegateResolutionToPropertyWhenReadingUsingDelegateFirst() {
        doDelegateResolutionForPropertyReadTest("Closure.DELEGATE_FIRST", "delegate")
    }

    void testDelegateResolutionToPropertyWhenReadingUsingOwnerOnly() {
        doDelegateResolutionForPropertyReadTest("Closure.OWNER_ONLY", "owner")
    }

    void testDelegateResolutionToPropertyWhenReadingUsingOwnerFirst() {
        doDelegateResolutionForPropertyReadTest("Closure.OWNER_FIRST", "owner")
    }

    void testDelegateResolutionToPropertyWhenWritingUsingDelegateOnly() {
        doDelegateResolutionForPropertyWriteTest("Closure.DELEGATE_ONLY", "delegate")
    }

    void testDelegateResolutionToPropertyWhenWritingUsingDelegateFirst() {
        doDelegateResolutionForPropertyWriteTest("Closure.DELEGATE_FIRST", "delegate")
    }

    void testDelegateResolutionToPropertyWhenWritingUsingOwnerOnly() {
        doDelegateResolutionForPropertyWriteTest("Closure.OWNER_ONLY", "owner")
    }

    void testDelegateResolutionToPropertyWhenWritingUsingOwnerFirst() {
        doDelegateResolutionForPropertyWriteTest("Closure.OWNER_FIRST", "owner")
    }

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

    void testDelegateVariableFromDifferentOwningClass() {
        assertScript '''
        @groovy.transform.CompileStatic
        class A {
            static private int MAX_LINES = 2
            static class B {
                @Delegate
                private Map<String, Object> delegate = [:]
                void m(int c) {
                    if (c > MAX_LINES) {
                        return
                    }
                }
            }
        }
        null
        '''
    }

    private static class SpecSupport {
        static int getLongueur(String self) { self.length() }
        static int longueur(String self) { self.length() }
        static void doesNotExist(String self) {}
    }

    private def assertScriptWithExtension(String extensionName, String code, Closure<Void> configurator=null) {
        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(
                new ASTTransformationCustomizer(TypeChecked, extensions:[extensionName]))
        def binding = new Binding()
        def shell = new GroovyShell(binding,config)
        if (configurator) {
            configurator.call(binding)
        }
        shell.evaluate(code)
    }
}
