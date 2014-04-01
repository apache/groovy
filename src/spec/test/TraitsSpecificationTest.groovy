import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode

/*
 * Copyright 2003-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
// end::trait_implementing_interface[]'''
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
// end::trait_with_property[]'''
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
// end::quackingduck[]'''
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
    int count() { ++count }                 // <2>
}
class Foo implements Counter {}             // <3>
def f = new Foo()
assert f.count() == 1                       // <4>
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
// end::trait_with_public_field[]'''
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
// end::ducktyping[]'''
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
}
