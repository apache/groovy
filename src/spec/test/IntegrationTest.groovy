import groovy.test.GroovyTestCase

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
class IntegrationTest extends GroovyTestCase {
    void testEval() {
        assertScript '''// tag::eval_me[]
import groovy.util.Eval

assert Eval.me('33*3') == 99
assert Eval.me('"foo".toUpperCase()') == 'FOO'
// end::eval_me[]

// tag::eval_x[]
assert Eval.x(4, '2*x') == 8                // <1>
assert Eval.me('k', 4, '2*k') == 8          // <2>
assert Eval.xy(4, 5, 'x*y') == 20           // <3>
assert Eval.xyz(4, 5, 6, 'x*y+z') == 26     // <4>
// end::eval_x[]

'''
    }

    void testGroovyShell() {
        assertScript '''// tag::groovyshell_simple[]
def shell = new GroovyShell()                           // <1>
def result = shell.evaluate '3*5'                       // <2>
def result2 = shell.evaluate(new StringReader('3*5'))   // <3>
assert result == result2
def script = shell.parse '3*5'                          // <4>
assert script instanceof groovy.lang.Script
assert script.run() == 15                               // <5>
// end::groovyshell_simple[]
'''
        assertScript '''// tag::groovyshell_binding[]
def sharedData = new Binding()                          // <1>
def shell = new GroovyShell(sharedData)                 // <2>
def now = new Date()
sharedData.setProperty('text', 'I am shared data!')     // <3>
sharedData.setProperty('date', now)                     // <4>

String result = shell.evaluate('"At $date, $text"')     // <5>

assert result == "At $now, I am shared data!"
// end::groovyshell_binding[]
'''

        assertScript '''// tag::groovyshell_binding_output[]
def sharedData = new Binding()                          // <1>
def shell = new GroovyShell(sharedData)                 // <2>

shell.evaluate('foo=123')                               // <3>

assert sharedData.getProperty('foo') == 123             // <4>
// end::groovyshell_binding_output[]
'''

        assertScript '''// tag::groovyshell_binding_localvariable[]
def sharedData = new Binding()
def shell = new GroovyShell(sharedData)

shell.evaluate('int foo=123')

try {
    assert sharedData.getProperty('foo')
} catch (MissingPropertyException e) {
    println "foo is defined as a local variable"
}
// end::groovyshell_binding_localvariable[]
'''

        assertScript '''// tag::groovyshell_parse_binding[]
def shell = new GroovyShell()

def b1 = new Binding(x:3)                       // <1>
def b2 = new Binding(x:4)                       // <2>
def script = shell.parse('x = 2*x')
script.binding = b1
script.run()
script.binding = b2
script.run()
assert b1.getProperty('x') == 6
assert b2.getProperty('x') == 8
assert b1 != b2
// end::groovyshell_parse_binding[]
'''

        assertScript '''// tag::groovyshell_threadsafe[]
def shell = new GroovyShell()

def b1 = new Binding(x:3)
def b2 = new Binding(x:4)
def script1 = shell.parse('x = 2*x')            // <1>
def script2 = shell.parse('x = 2*x')            // <2>
assert script1 != script2
script1.binding = b1                            // <3>
script2.binding = b2                            // <4>
def t1 = Thread.start { script1.run() }         // <5>
def t2 = Thread.start { script2.run() }         // <6>
[t1,t2]*.join()                                 // <7>
assert b1.getProperty('x') == 6
assert b2.getProperty('x') == 8
assert b1 != b2
// end::groovyshell_threadsafe[]
'''
    }

    void testGroovyShellCustomScript() {
        assertScript '''// tag::custom_script_imports[]
import org.codehaus.groovy.control.CompilerConfiguration
// end::custom_script_imports[]

// tag::custom_script_scriptclass[]
abstract class MyScript extends Script {
    String name

    String greet() {
        "Hello, $name!"
    }
}
// end::custom_script_scriptclass[]

// tag::custom_script_usage[]
def config = new CompilerConfiguration()                                    // <1>
config.scriptBaseClass = 'MyScript'                                         // <2>

def shell = new GroovyShell(this.class.classLoader, new Binding(), config)  // <3>
def script = shell.parse('greet()')                                         // <4>
assert script instanceof MyScript
script.setName('Michel')
assert script.run() == 'Hello, Michel!'
// end::custom_script_usage[]
'''
    }

    void testGroovyClassLoader() {
        assertScript '''// tag::gcl[]
import groovy.lang.GroovyClassLoader

def gcl = new GroovyClassLoader()                                           // <1>
def clazz = gcl.parseClass('class Foo { void doIt() { println "ok" } }')    // <2>
assert clazz.name == 'Foo'                                                  // <3>
def o = clazz.newInstance()                                                 // <4>
o.doIt()                                                                    // <5>
// end::gcl[]
'''

        assertScript '''// tag::gcl_distinct_classes[]
import groovy.lang.GroovyClassLoader

def gcl = new GroovyClassLoader()
def clazz1 = gcl.parseClass('class Foo { }')                                // <1>
def clazz2 = gcl.parseClass('class Foo { }')                                // <2>
assert clazz1.name == 'Foo'                                                 // <3>
assert clazz2.name == 'Foo'
assert clazz1 != clazz2                                                     // <4>
// end::gcl_distinct_classes[]
'''

        assertScript '''import groovy.lang.GroovyClassLoader
def file = File.createTempFile("Foo",".groovy")
file << "class Foo { }"
// tag::gcl_same_classes[]
def gcl = new GroovyClassLoader()
def clazz1 = gcl.parseClass(file)                                           // <1>
def clazz2 = gcl.parseClass(new File(file.absolutePath))                    // <2>
assert clazz1.name == 'Foo'                                                 // <3>
assert clazz2.name == 'Foo'
assert clazz1 == clazz2                                                     // <4>
// end::gcl_same_classes[]
file.delete()
'''
    }

    void testGroovyScriptEngine() {
        assertScript '''def tmpDir = File.createTempDir("reloading", "gse")

def copyResource = { String source, String dest ->
    File dst = new File(tmpDir, "${dest}.groovy")
    if (dst.exists()) {
        dst.text = ''
        Thread.sleep(1000)
    }
    dst << this.class.classLoader.getResourceAsStream("reloading/${source}.groovy")
}

try {
    // tag::gse_init[]
    def binding = new Binding()
    def engine = new GroovyScriptEngine([tmpDir.toURI().toURL()] as URL[])          // <1>
    // end::gse_init[]

    copyResource('source1', 'ReloadingTest')
    /*
    // tag::gse_script_fakeloop_begin[]
    while (true) {
    // end::gse_script_fakeloop_begin[]
    // tag::gse_script_fakeloop_end[]
        Thread.sleep(1000)
    }
    // end::gse_script_fakeloop_end[]
    */
    // tag::gse_script1[]
    def greeter = engine.run('ReloadingTest.groovy', binding)                   // <2>
    println greeter.sayHello()                                                  // <3>
    // end::gse_script1[]
    assert greeter.sayHello() == 'Hello, world!'

    copyResource('source2', 'ReloadingTest')

    greeter = engine.run('ReloadingTest.groovy', binding)
    println greeter.sayHello()
    assert greeter.sayHello() == 'Hello, Groovy!'

    copyResource('source3', 'ReloadingTest')
    copyResource('dependency1', 'Dependency')

    greeter = engine.run('ReloadingTest.groovy', binding)
    println greeter.sayHello()
    assert greeter.sayHello() == 'Hello, dependency 1'

    copyResource('dependency2', 'Dependency')

    greeter = engine.run('ReloadingTest.groovy', binding)
    println greeter.sayHello()
    assert greeter.sayHello() == 'Hello, dependency 2'
} finally {
    tmpDir.deleteDir()
}
'''
    }

}
