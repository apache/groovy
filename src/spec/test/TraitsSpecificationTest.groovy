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
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode

/**
 * Specification tests for the traits feature
 */
class TraitsSpecificationTest extends GroovyTestCase {
    void testTraitDeclaration() {
        assertScript '''// tag::flying_simple[]
trait FlyingAbility {                           // <1>
        String fly() { "I'm flying!" }          // <2>
}
// end::flying_simple[]

// tag::bird[]
class Bird implements FlyingAbility {}          // <1>
def b = new Bird()                              // <2>
assert b.fly() == "I'm flying!"                 // <3>
// end::bird[]

'''
    }

    void testAbstractMethodInTrait() {
        assertScript '''// tag::greetable[]
trait Greetable {
    abstract String name()                              // <1>
    String greeting() { "Hello, ${name()}!" }           // <2>
}
// end::greetable[]

// tag::greetable_person[]
class Person implements Greetable {                     // <1>
    String name() { 'Bob' }                             // <2>
}

def p = new Person()
assert p.greeting() == 'Hello, Bob!'                    // <3>
// end::greetable_person[]
'''
    }

    void testTraitImplementingInterface() {
        assertScript '''// tag::trait_implementing_interface[]
interface Named {                                       // <1>
    String name()
}
trait Greetable implements Named {                      // <2>
    String greeting() { "Hello, ${name()}!" }
}
class Person implements Greetable {                     // <3>
    String name() { 'Bob' }                             // <4>
}

def p = new Person()
assert p.greeting() == 'Hello, Bob!'                    // <5>
assert p instanceof Named                               // <6>
assert p instanceof Greetable                           // <7>
// end::trait_implementing_interface[]
'''
    }

    void testTraitWithProperty() {
        assertScript '''// tag::trait_with_property[]
trait Named {
    String name                             // <1>
}
class Person implements Named {}            // <2>
def p = new Person(name: 'Bob')             // <3>
assert p.name == 'Bob'                      // <4>
assert p.getName() == 'Bob'                 // <5>
// end::trait_with_property[]
'''
    }

    void testCompositionOfTraits() {
        assertScript '''trait FlyingAbility {
    String fly() { "I'm flying!" }
}

// tag::speaking_simple[]
trait SpeakingAbility {
    String speak() { "I'm speaking!" }
}
// end::speaking_simple[]

// tag::speakingduck[]
class Duck implements FlyingAbility, SpeakingAbility {} // <1>

def d = new Duck()                                      // <2>
assert d.fly() == "I'm flying!"                         // <3>
assert d.speak() == "I'm speaking!"                     // <4>
// end::speakingduck[]
'''
    }

    void testOverridableMethod() {
        assertScript '''trait FlyingAbility {
    String fly() { "I'm flying!" }
}

trait SpeakingAbility {
    String speak() { "I'm speaking!" }
}

// tag::quackingduck[]
class Duck implements FlyingAbility, SpeakingAbility {
    String quack() { "Quack!" }                         // <1>
    String speak() { quack() }                          // <2>
}

def d = new Duck()
assert d.fly() == "I'm flying!"                         // <3>
assert d.quack() == "Quack!"                            // <4>
assert d.speak() == "Quack!"                            // <5>
// end::quackingduck[]
'''
    }

    void testPrivateMethodInTrait() {
        assertScript '''// tag::private_method_in_trait[]
trait Greeter {
    private String greetingMessage() {                      // <1>
        'Hello from a private method!'
    }
    String greet() {
        def m = greetingMessage()                           // <2>
        println m
        m
    }
}
class GreetingMachine implements Greeter {}                 // <3>
def g = new GreetingMachine()
assert g.greet() == "Hello from a private method!"          // <4>
try {
    assert g.greetingMessage()                              // <5>
} catch (MissingMethodException e) {
    println "greetingMessage is private in trait"
}
// end::private_method_in_trait[]
'''
    }

    void testTraitWithPrivateField() {
        assertScript '''// tag::trait_with_private_field[]
trait Counter {
    private int count = 0                   // <1>
    int count() { count += 1; count }       // <2>
}
class Foo implements Counter {}             // <3>
def f = new Foo()
assert f.count() == 1                       // <4>
assert f.count() == 2
// end::trait_with_private_field[]
'''
    }

    void testTraitWithPublicField() {
        assertScript '''// tag::trait_with_public_field[]
trait Named {
    public String name                      // <1>
}
class Person implements Named {}            // <2>
def p = new Person()                        // <3>
p.Named__name = 'Bob'                       // <4>
// end::trait_with_public_field[]
'''
    }

    void testRemappedName() {
        def clazz = new ClassNode("my.package.Foo", 0, ClassHelper.OBJECT_TYPE)
        assert org.codehaus.groovy.transform.trait.Traits.remappedFieldName(clazz, "bar") == 'my_package_Foo__bar'
    }

    void testDuckTyping() {
        assertScript '''// tag::ducktyping[]
trait SpeakingDuck {
    String speak() { quack() }                      // <1>
}
class Duck implements SpeakingDuck {
    String methodMissing(String name, args) {
        "${name.capitalize()}!"                     // <2>
    }
}
def d = new Duck()
assert d.speak() == 'Quack!'                        // <3>
// end::ducktyping[]
'''
    }

    void testTraitInheritance() {
        assertScript '''// tag::trait_inherit[]
trait Named {
    String name                                     // <1>
}
trait Polite extends Named {                        // <2>
    String introduce() { "Hello, I am $name" }      // <3>
}
class Person implements Polite {}
def p = new Person(name: 'Alice')                   // <4>
assert p.introduce() == 'Hello, I am Alice'         // <5>
// end::trait_inherit[]
'''
    }

    void testMultipleTraitInheritance() {
        assertScript '''// tag::trait_multiple_inherit[]
trait WithId {                                      // <1>
    Long id
}
trait WithName {                                    // <2>
    String name
}
trait Identified implements WithId, WithName {}     // <3>
// end::trait_multiple_inherit[]
class Bean implements Identified {}
def b = new Bean(id: 123, name: 'Foo')
'''
    }

    void testMethodMissingInTrait() {
        assertScript '''// tag::dynamicobject[]
trait DynamicObject {                               // <1>
    private Map props = [:]
    def methodMissing(String name, args) {
        name.toUpperCase()
    }
    def propertyMissing(String prop) {
        props[prop]
    }
    void setProperty(String prop, Object value) {
        props[prop] = value
    }
}

class Dynamic implements DynamicObject {
    String existingProperty = 'ok'                  // <2>
    String existingMethod() { 'ok' }                // <3>
}
def d = new Dynamic()
assert d.existingProperty == 'ok'                   // <4>
assert d.foo == null                                // <5>
d.foo = 'bar'                                       // <6>
assert d.foo == 'bar'                               // <7>
assert d.existingMethod() == 'ok'                   // <8>
assert d.someMethod() == 'SOMEMETHOD'               // <9>
// end::dynamicobject[]
'''
    }

    void testDefaultMultipleInheritanceResolution() {
        assertScript '''// tag::multiple_inherit_default[]
trait A {
    String exec() { 'A' }               // <1>
}
trait B {
    String exec() { 'B' }               // <2>
}
class C implements A,B {}               // <3>
// end::multiple_inherit_default[]

// tag::multiple_inherit_default_assert[]
def c = new C()
assert c.exec() == 'B'
// end::multiple_inherit_default_assert[]
'''
    }

    void testUserMultipleInheritanceResolution() {
        assertScript '''trait A {
    String exec() { 'A' }
}
trait B {
    String exec() { 'B' }
}
// tag::multiple_inherit_user[]
class C implements A,B {
    String exec() { A.super.exec() }    // <1>
}
def c = new C()
assert c.exec() == 'A'                  // <2>
// end::multiple_inherit_user[]
'''
    }

    void testRuntimeCoercion() {
        assertScript '''
// tag::runtime_header[]
trait Extra {
    String extra() { "I'm an extra method" }            // <1>
}
class Something {                                       // <2>
    String doSomething() { 'Something' }                // <3>
}
// end::runtime_header[]

try {
// tag::runtime_fail[]
def s = new Something()
s.extra()
// end::runtime_fail[]
} catch (MissingMethodException e) {}

// tag::runtime_success[]
def s = new Something() as Extra                        // <1>
s.extra()                                               // <2>
s.doSomething()                                         // <3>
// end::runtime_success[]
'''
    }

    void testWithTraits() {
        assertScript '''// tag::withtraits_header[]
trait A { void methodFromA() {} }
trait B { void methodFromB() {} }

class C {}

def c = new C()
// end::withtraits_header[]
try {
// tag::withtraits_fail[]
c.methodFromA()                     // <1>
c.methodFromB()                     // <2>
// end::withtraits_fail[]
} catch (MissingMethodException e) {}
// tag::withtraits_success[]
def d = c.withTraits A, B           // <3>
d.methodFromA()                     // <4>
d.methodFromB()                     // <5>
// end::withtraits_success[]
'''
    }

    void testSAMCoercion() {
        assertScript '''import org.codehaus.groovy.runtime.Greeter

// tag::sam_trait[]
trait Greeter {
    String greet() { "Hello $name" }        // <1>
    abstract String getName()               // <2>
}
// end::sam_trait[]

// tag::sam_trait_assignment[]
Greeter greeter = { 'Alice' }               // <1>
// end::sam_trait_assignment[]

// tag::sam_trait_method[]
void greet(Greeter g) { println g.greet() } // <1>
greet { 'Alice' }                           // <2>
// end::sam_trait_method[]
'''
    }

    void testTraitOverrideBehavior() {
        assertScript '''
// tag::forceoverride_header[]
import groovy.test.GroovyTestCase
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.customizers.ImportCustomizer

class SomeTest extends GroovyTestCase {
    def config
    def shell

    void setup() {
        config = new CompilerConfiguration()
        shell = new GroovyShell(config)
    }
    void testSomething() {
        assert shell.evaluate('1+1') == 2
    }
    void otherTest() { /* ... */ }
}
// end::forceoverride_header[]

class SomeTest2 extends SomeTest {}

/*
// tag::forceoverride_extends[]
class AnotherTest extends SomeTest {
    void setup() {
        config = new CompilerConfiguration()
        config.addCompilationCustomizers( ... )
        shell = new GroovyShell(config)
    }
}
// end::forceoverride_extends[]

// tag::forceoverride_extends2[]
class YetAnotherTest extends SomeTest {
    void setup() {
        config = new CompilerConfiguration()
        config.addCompilationCustomizers( ... )
        shell = new GroovyShell(config)
    }
}
// end::forceoverride_extends2[]
*/

// tag::forceoverride_trait[]
trait MyTestSupport {
    void setup() {
        config = new CompilerConfiguration()
        config.addCompilationCustomizers( new ASTTransformationCustomizer(CompileStatic) )
        shell = new GroovyShell(config)
    }
}
// end::forceoverride_trait[]

// tag::forceoverride_miss_header[]
class AnotherTest extends SomeTest implements MyTestSupport {}
class YetAnotherTest extends SomeTest2 implements MyTestSupport {}
// end::forceoverride_miss_header[]

def t = new AnotherTest()
t.setup()
assert !t.config.compilationCustomizers.empty
'''
    }


    void testRuntimeOverride() {
        assertScript '''// tag::runtime_forceoverride[]
class Person {
    String name                                         // <1>
}
trait Bob {
    String getName() { 'Bob' }                          // <2>
}

def p = new Person(name: 'Alice')
assert p.name == 'Alice'                                // <3>
def p2 = p as Bob                                       // <4>
assert p2.name == 'Bob'                                 // <5>
// end::runtime_forceoverride[]
'''
    }

    void testDifferenceWithMixin() {
        assertScript '''// tag::diff_mixin[]
class A { String methodFromA() { 'A' } }        // <1>
class B { String methodFromB() { 'B' } }        // <2>
A.metaClass.mixin B                             // <3>
def o = new A()
assert o.methodFromA() == 'A'                   // <4>
assert o.methodFromB() == 'B'                   // <5>
assert o instanceof A                           // <6>
assert !(o instanceof B)                        // <7>
// end::diff_mixin[]
'''
    }

    void testMeaningOfThis() {
        assertScript '''// tag::meaningofthis_header[]
trait Introspector {
    def whoAmI() { this }
}
class Foo implements Introspector {}
def foo = new Foo()
// end::meaningofthis_header[]
// tag::meaningofthis_snippet[]
foo.whoAmI()
// end::meaningofthis_snippet[]
// tag::meaningofthis_assert[]
assert foo.whoAmI().is(foo)
// end::meaningofthis_assert[]
'''
    }

    void testPublicStaticFieldInTrait() {
        assertScript '''// tag::staticfield_header[]
trait TestHelper {
    public static boolean CALLED = false        // <1>
    static void init() {                        // <2>
        CALLED = true                           // <3>
    }
}
class Foo implements TestHelper {}
Foo.init()                                      // <4>
assert Foo.TestHelper__CALLED                   // <5>
// end::staticfield_header[]

try {
// tag::staticfield_notontrait[]
    Foo.CALLED = true
// end::staticfield_notontrait[]
} catch (Exception e){}

// tag::staticfield_distinct[]
class Bar implements TestHelper {}              // <1>
class Baz implements TestHelper {}              // <2>
Bar.init()                                      // <3>
assert Bar.TestHelper__CALLED                   // <4>
assert !Baz.TestHelper__CALLED                  // <5>
// end::staticfield_distinct[]
'''
    }

    void testPrePostfixIsDisallowed() {
        def message = shouldFail '''
// tag::prefix_postfix[]
trait Counting {
    int x
    void inc() {
        x++                             // <1>
    }
    void dec() {
        --x                             // <2>
    }
}
class Counter implements Counting {}
def c = new Counter()
c.inc()
// end::prefix_postfix[]
        '''
        assert message.contains('Postfix expressions on trait fields/properties  are not supported')
        assert message.contains('Prefix expressions on trait fields/properties are not supported')
    }

    void testStackableTraits() {
        assertScript '''import TraitsSpecificationTest.PrintCategory as PC

String filterTags(String src) {
    StringBuilder out = new StringBuilder()
    src.eachLine {

        if (!it.startsWith('//')) {
            if (out) { out.append('\\n')}
            out.append(it)
        }
    }
    out
}

// tag::messagehandler[]
interface MessageHandler {
    void on(String message, Map payload)
}
// end::messagehandler[]

// tag::defaulthandler[]
trait DefaultHandler implements MessageHandler {
    void on(String message, Map payload) {
        println "Received $message with payload $payload"
    }
}
// end::defaulthandler[]

// tag::logginghandler[]
trait LoggingHandler implements MessageHandler {                            // <1>
    void on(String message, Map payload) {
        println "Seeing $message with payload $payload"                     // <2>
        super.on(message, payload)                                          // <3>
    }
}
// end::logginghandler[]

// tag::handlerwithlogger[]
class HandlerWithLogger implements DefaultHandler, LoggingHandler {}
// end::handlerwithlogger[]
use(PC) {
    // tag::handlerwithlogger_assert[]
    def loggingHandler = new HandlerWithLogger()
    loggingHandler.on('test logging', [:])
    // end::handlerwithlogger_assert[]
    def expect = """// tag::handlerwithlogger_assert_output[]
Seeing test logging with payload [:]
Received test logging with payload [:]
// end::handlerwithlogger_assert_output[]
"""
    assert PC.BUFFER.toString() == filterTags(expect)
    PC.reset()
}
// tag::sayhandler[]
trait SayHandler implements MessageHandler {
    void on(String message, Map payload) {
        if (message.startsWith("say")) {                                    // <1>
            println "I say ${message - 'say'}!"
        } else {
            super.on(message, payload)                                      // <2>
        }
    }
}
// end::sayhandler[]

// tag::simplehandler[]
class SimpleHandler implements DefaultHandler {}
// end::simplehandler[]

// tag::simplehandlerwithlogging[]
class SimpleHandlerWithLogging implements DefaultHandler {
    void on(String message, Map payload) {                                  // <1>
        println "Seeing $message with payload $payload"                     // <2>
        DefaultHandler.super.on(message, payload)                           // <3>
    }
}
// end::simplehandlerwithlogging[]

// tag::implementinghandler[]
class Handler implements DefaultHandler, SayHandler, LoggingHandler {}
// end::implementinghandler[]
use (PC) {
    // tag::implementinghandler_assert[]
    def h = new Handler()
    h.on('foo', [:])
    h.on('sayHello', [:])
    // end::implementinghandler_assert[]
    def expect = """// tag::implementinghandler_output[]
Seeing foo with payload [:]
Received foo with payload [:]
Seeing sayHello with payload [:]
I say Hello!
// end::implementinghandler_output[]
"""
    assert PC.BUFFER.toString() == filterTags(expect)
    PC.reset()
}

// tag::alternatehandler[]
class AlternateHandler implements DefaultHandler, LoggingHandler, SayHandler {}
// end::alternatehandler[]
use (PC) {
// tag::alternatehandler_assert[]
h = new AlternateHandler()
h.on('foo', [:])
h.on('sayHello', [:])
// end::alternatehandler_assert[]
def expect = """// tag::alternatehandler_output[]
Seeing foo with payload [:]
Received foo with payload [:]
I say Hello!
// end::alternatehandler_output[]
"""
    assert PC.BUFFER.toString() == filterTags(expect)
    PC.reset()
PC.reset()
}

'''
    }

    void testDecorateFinalClass() {
        assertScript '''
// tag::decoratefinalclass[]
trait Filtering {                                       // <1>
    StringBuilder append(String str) {                  // <2>
        def subst = str.replace('o','')                 // <3>
        super.append(subst)                             // <4>
    }
    String toString() { super.toString() }              // <5>
}
def sb = new StringBuilder().withTraits Filtering       // <6>
sb.append('Groovy')
assert sb.toString() == 'Grvy'                          // <7>
// end::decoratefinalclass[]
'''
    }

    void testInheritanceOfState() {
        assertScript '''// tag::intcouple[]
trait IntCouple {
    int x = 1
    int y = 2
    int sum() { x+y }
}
// end::intcouple[]

// tag::intcouple_impl[]
class BaseElem implements IntCouple {
    int f() { sum() }
}
def base = new BaseElem()
assert base.f() == 3
// end::intcouple_impl[]



// tag::intcouple_impl_override[]
class Elem implements IntCouple {
    int x = 3                                       // <1>
    int y = 4                                       // <2>
    int f() { sum() }                               // <3>
}
def elem = new Elem()
// end::intcouple_impl_override[]
// tag::intcouple_impl_override_assert[]
assert elem.f() == 3
// end::intcouple_impl_override_assert[]

'''
        assertScript '''
// tag::intcouple_impl_override_directgetter[]
trait IntCouple {
    int x = 1
    int y = 2
    int sum() { getX()+getY() }
}

class Elem implements IntCouple {
    int x = 3
    int y = 4
    int f() { sum() }
}
def elem = new Elem()
assert elem.f() == 7
// end::intcouple_impl_override_directgetter[]
'''
    }

    void testSelfType() {
        assertScript '''
// tag::selftype_intro[]
class CommunicationService {
    static void sendMessage(String from, String to, String message) {       // <1>
        println "$from sent [$message] to $to"
    }
}

class Device { String id }                                                  // <2>

trait Communicating {
    void sendMessage(Device to, String message) {
        CommunicationService.sendMessage(id, to.id, message)                // <3>
    }
}

class MyDevice extends Device implements Communicating {}                   // <4>

def bob = new MyDevice(id:'Bob')
def alice = new MyDevice(id:'Alice')
bob.sendMessage(alice,'secret')                                             // <5>
// end::selftype_intro[]

// tag::selftype_securityservice[]
class SecurityService {
    static void check(Device d) { if (d.id==null) throw new SecurityException() }
}
// end::selftype_securityservice[]
'''


        assertScript '''import groovy.transform.SelfType
import groovy.transform.CompileStatic

class CommunicationService {
    static void sendMessage(String from, String to, String message) {
        println "$from sent [$message] to $to"
    }
}

class Device { String id }

// tag::selftype_fixed[]
@SelfType(Device)
@CompileStatic
trait Communicating {
    void sendMessage(Device to, String message) {
        SecurityService.check(this)
        CommunicationService.sendMessage(id, to.id, message)
    }
}
// end::selftype_fixed[]

class MyDevice extends Device implements Communicating {}

def bob = new MyDevice(id:'Bob')
def alice = new MyDevice(id:'Alice')
bob.sendMessage(alice,'secret')

class SecurityService {
    static void check(Device d) { if (d.id==null) throw new SecurityException() }
}

'''

        def message = shouldFail '''import groovy.transform.SelfType
import groovy.transform.CompileStatic

class CommunicationService {
    static void sendMessage(String from, String to, String message) {
        println "$from sent [$message] to $to"
    }
}

class Device { String id }

@SelfType(Device)
@CompileStatic
trait Communicating {
    void sendMessage(Device to, String message) {
        SecurityService.check(this)
        CommunicationService.sendMessage(id, to.id, message)
    }
}

// tag::selftype_compiletimeerror[]
class MyDevice implements Communicating {} // forgot to extend Device
// end::selftype_compiletimeerror[]

class SecurityService {
    static void check(Device d) { if (d.id==null) throw new SecurityException() }
}

'''
        assert message.contains("class 'MyDevice' implements trait 'Communicating' but does not extend self type class 'Device'")
    }

    static class PrintCategory {
        static StringBuilder BUFFER = new StringBuilder()
        static void println(Object self, String message) {
            if (BUFFER.length()>0) BUFFER.append('\n')
            BUFFER.append(message)
        }
        static void reset() { BUFFER.setLength(0) }
    }
}
