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
import gls.CompilableTestSupport

import java.util.regex.Matcher
import java.util.regex.Pattern

class OperatorsTest extends CompilableTestSupport {

    void testArithmeticOperators() {
        // tag::binary_arith_ops[]
        assert  1  + 2 == 3
        assert  4  - 3 == 1
        assert  3  * 5 == 15
        assert  3  / 2 == 1.5
        assert 10  % 3 == 1
        assert  2 ** 3 == 8
        // end::binary_arith_ops[]

        // tag::unary_plus_minus[]
        assert +3 == 3
        assert -4 == 0 - 4

        assert -(-1) == 1  // <1>
        // end::unary_plus_minus[]

        // tag::plusplus_minusminus[]
        def a = 2
        def b = a++ * 3             // <1>

        assert a == 3 && b == 6

        def c = 3
        def d = c-- * 2             // <2>

        assert c == 2 && d == 6

        def e = 1
        def f = ++e + 3             // <3>

        assert e == 2 && f == 5

        def g = 4
        def h = --g + 1             // <4>

        assert g == 3 && h == 4
        // end::plusplus_minusminus[]
    }

    void testArithmeticOperatorsWithAssignment() {
        // tag::binary_assign_operators[]
        def a = 4
        a += 3

        assert a == 7

        def b = 5
        b -= 3

        assert b == 2

        def c = 5
        c *= 3

        assert c == 15

        def d = 10
        d /= 2

        assert d == 5

        def e = 10
        e %= 3

        assert e == 1

        def f = 3
        f **= 2

        assert f == 9
        // end::binary_assign_operators[]
    }

    void testSimpleRelationalOperators() {
        // tag::simple_relational_op[]
        assert 1 + 2 == 3
        assert 3 != 4

        assert -2 < 3
        assert 2 <= 2
        assert 3 <= 4

        assert 5 > 1
        assert 5 >= -2
        // end::simple_relational_op[]
    }

    void testLogicalOperators() {
        // tag::logical_op[]
        assert !false           // <1>
        assert true && true     // <2>
        assert true || false    // <3>
        // end::logical_op[]
    }

    void testBitwiseOperators() {
        // tag::bitwise_op[]
        int a = 0b00101010
        assert a == 42
        int b = 0b00001000
        assert b == 8
        assert (a & a) == a                     // <1>
        assert (a & b) == b                     // <2>
        assert (a | a) == a                     // <3>
        assert (a | b) == a                     // <4>

        int mask = 0b11111111                   // <5>
        assert ((a ^ a) & mask) == 0b00000000   // <6>
        assert ((a ^ b) & mask) == 0b00100010   // <7>
        assert ((~a) & mask)    == 0b11010101   // <8>
        // end::bitwise_op[]
    }

    void testLogicalOperatorPrecedence() {
        // tag::logical_precendence_1[]
        assert (!false && false) == false   // <1>
        // end::logical_precendence_1[]

        // tag::logical_precendence_2[]
        assert true || true && false        // <1>
        // end::logical_precendence_2[]
    }

    void testLogicalShortCircuit() {
        assertScript '''
	        // tag::logical_shortcircuit[]
	        boolean checkIfCalled() {   // <1>
	            called = true
	        }

	        called = false
	        true || checkIfCalled()
	        assert !called              // <2>

	        called = false
	        false || checkIfCalled()
	        assert called               // <3>

	        called = false
	        false && checkIfCalled()
	        assert !called              // <4>

	        called = false
	        true && checkIfCalled()
	        assert called               // <5>
	        // end::logical_shortcircuit[]
        '''
    }

    void testConditionalOperators() {
        // tag::conditional_op_not[]
        assert (!true)    == false                      // <1>
        assert (!'foo')   == false                      // <2>
        assert (!'')      == true                       // <3>
        // end::conditional_op_not[]
        def result
        def string = 'some string'
        // tag::conditional_op_ternary_if[]
        if (string!=null && string.length()>0) {
            result = 'Found'
        } else {
            result = 'Not found'
        }
        // end::conditional_op_ternary_if[]
        assert result == 'Found'
        result = null
        // tag::conditional_op_ternary_ternary[]
        result = (string!=null && string.length()>0) ? 'Found' : 'Not found'
        // end::conditional_op_ternary_ternary[]
        assert result == 'Found'

        // tag::conditional_op_ternary_groovytruth[]
        result = string ? 'Found' : 'Not found'
        // end::conditional_op_ternary_groovytruth[]
        assert result == 'Found'

        def user = [name: 'Bob']
        def displayName
        // tag::conditional_op_elvis[]
        displayName = user.name ? user.name : 'Anonymous'   // <1>
        displayName = user.name ?: 'Anonymous'              // <2>
        // end::conditional_op_elvis[]

    }

    void testNullSafeOperator() {
        // tag::nullsafe[]
        def person = Person.find { it.id == 123 }    // <1>
        def name = person?.name                      // <2>
        assert name == null                          // <3>
        // end::nullsafe[]
    }

    OperatorsTest() {
    }

    void testDirectFieldAccess() {
        assertScript '''
// tag::direct_field_class[]
class User {
    public final String name                 // <1>
    User(String name) { this.name = name}
    String getName() { "Name: $name" }       // <2>
}
def user = new User('Bob')
assert user.name == 'Name: Bob'              // <3>
// end::direct_field_class[]
// tag::direct_field_op[]
assert user.@name == 'Bob'                   // <1>
// end::direct_field_op[]
'''
    }

    void testMethodReference() {
        // tag::method_reference[]
        def str = 'example of method reference'            // <1>
        def fun = str.&toUpperCase                         // <2>
        def upper = fun()                                  // <3>
        assert upper == str.toUpperCase()                  // <4>
        // end::method_reference[]
        assert fun instanceof Closure

        assertScript '''
            class Person {
                String name
                int age
            }
            // tag::method_reference_strategy[]
            def transform(List elements, Closure action) {                    // <1>
                def result = []
                elements.each {
                    result << action(it)
                }
                result
            }
            String describe(Person p) {                                       // <2>
                "$p.name is $p.age"
            }
            def action = this.&describe                                       // <3>
            def list = [
                new Person(name: 'Bob',   age: 42),
                new Person(name: 'Julia', age: 35)]                           // <4>
            assert transform(list, action) == ['Bob is 42', 'Julia is 35']    // <5>

            // end::method_reference_strategy[]
        '''

        assertScript '''
            // tag::method_reference_dispatch[]
            def doSomething(String str) { str.toUpperCase() }    // <1>
            def doSomething(Integer x) { 2*x }                   // <2>
            def reference = this.&doSomething                    // <3>
            assert reference('foo') == 'FOO'                     // <4>
            assert reference(123)   == 246                       // <5>
            // end::method_reference_dispatch[]
        '''
    }

    void testRegularExpressionOperators() {
        def pattern = 'foo'
        // tag::pattern_op[]
        def p = ~/foo/
        assert p instanceof Pattern
        // end::pattern_op[]
        // tag::pattern_op_variants[]
        p = ~'foo'                                                        // <1>
        p = ~"foo"                                                        // <2>
        p = ~$/dollar/slashy $ string/$                                   // <3>
        p = ~"${pattern}"                                                 // <4>
        // end::pattern_op_variants[]

        // tag::pattern_matcher_op[]
        def text = "some text to match"
        def m = text =~ /match/                                           // <1>
        assert m instanceof Matcher                                       // <2>
        if (!m) {                                                         // <3>
            throw new RuntimeException("Oops, text not found!")
        }
        // end::pattern_matcher_op[]

        // tag::pattern_matcher_strict_op[]
        m = text ==~ /match/                                              // <1>
        assert m instanceof Boolean                                       // <2>
        if (m) {                                                          // <3>
            throw new RuntimeException("Should not reach that point!")
        }
        // end::pattern_matcher_strict_op[]
    }

    void testSpreadDotOperator() {
        assertScript '''
// tag::spreaddot[]
class Car {
    String make
    String model
}
def cars = [
       new Car(make: 'Peugeot', model: '508'),
       new Car(make: 'Renault', model: 'Clio')]       // <1>
def makes = cars*.make                                // <2>
assert makes == ['Peugeot', 'Renault']                // <3>
// end::spreaddot[]
// tag::spreaddot_nullsafe[]
cars = [
   new Car(make: 'Peugeot', model: '508'),
   null,                                              // <1>
   new Car(make: 'Renault', model: 'Clio')]
assert cars*.make == ['Peugeot', null, 'Renault']     // <2>
assert null*.make == null                             // <3>
// end::spreaddot_nullsafe[]
'''
        assertScript '''
// tag::spreaddot_iterable[]
class Component {
    Long id
    String name
}
class CompositeObject implements Iterable<Component> {
    def components = [
        new Component(id: 1, name: 'Foo'),
        new Component(id: 2, name: 'Bar')]

    @Override
    Iterator<Component> iterator() {
        components.iterator()
    }
}
def composite = new CompositeObject()
assert composite*.id == [1,2]
assert composite*.name == ['Foo','Bar']
// end::spreaddot_iterable[]
'''
        assertScript '''
import groovy.transform.Canonical

// tag::spreaddot_multilevel[]
class Make {
    String name
    List<Model> models
}

@Canonical
class Model {
    String name
}

def cars = [
    new Make(name: 'Peugeot',
             models: [new Model('408'), new Model('508')]),
    new Make(name: 'Renault',
             models: [new Model('Clio'), new Model('Captur')])
]

def makes = cars*.name
assert makes == ['Peugeot', 'Renault']

def models = cars*.models*.name
assert models == [['408', '508'], ['Clio', 'Captur']]
assert models.sum() == ['408', '508', 'Clio', 'Captur'] // flatten one level
assert models.flatten() == ['408', '508', 'Clio', 'Captur'] // flatten all levels (one in this case)
// end::spreaddot_multilevel[]
'''
        assertScript '''
// tag::spreaddot_alternative[]
class Car {
    String make
    String model
}
def cars = [
   [
       new Car(make: 'Peugeot', model: '408'),
       new Car(make: 'Peugeot', model: '508')
   ], [
       new Car(make: 'Renault', model: 'Clio'),
       new Car(make: 'Renault', model: 'Captur')
   ]
]
def models = cars.collectNested{ it.model }
assert models == [['408', '508'], ['Clio', 'Captur']]
// end::spreaddot_alternative[]
'''
    }

    void testSpreadMethodArguments() {
        assertScript '''
// tag::spreadmethodargs_method[]
int function(int x, int y, int z) {
    x*y+z
}
// end::spreadmethodargs_method[]
// tag::spreadmethodargs_args[]
def args = [4,5,6]
// end::spreadmethodargs_args[]
// tag::spreadmethodargs_assert[]
assert function(*args) == 26
// end::spreadmethodargs_assert[]
// tag::spreadmethodargs_mixed[]
args = [4]
assert function(*args,5,6) == 26
// end::spreadmethodargs_mixed[]
'''
    }

    void testSpreadList() {
        // tag::spread_list[]
        def items = [4,5]                      // <1>
        def list = [1,2,3,*items,6]            // <2>
        assert list == [1,2,3,4,5,6]           // <3>
        // end::spread_list[]
    }

    void testSpreadMap() {
        assertScript '''
        // tag::spread_map[]
        def m1 = [c:3, d:4]                   // <1>
        def map = [a:1, b:2, *:m1]            // <2>
        assert map == [a:1, b:2, c:3, d:4]    // <3>
        // end::spread_map[]
        '''

        assertScript '''
        // tag::spread_map_position[]
        def m1 = [c:3, d:4]                   // <1>
        def map = [a:1, b:2, *:m1, d: 8]      // <2>
        assert map == [a:1, b:2, c:3, d:8]    // <3>
        // end::spread_map_position[]
        '''

    }

    void testRangeOperator() {
        assertScript '''
        // tag::intrange[]
        def range = 0..5                                    // <1>
        assert (0..5).collect() == [0, 1, 2, 3, 4, 5]       // <2>
        assert (0..<5).collect() == [0, 1, 2, 3, 4]         // <3>
        assert (0..5) instanceof List                       // <4>
        assert (0..5).size() == 6                           // <5>
        // end::intrange[]
        '''
        assertScript '''
        // tag::charrange[]
        assert ('a'..'d').collect() == ['a','b','c','d']
        // end::charrange[]
        '''
    }

    void testSpaceshipOperator() {
        assertScript '''
        // tag::spaceship[]
        assert (1 <=> 1) == 0
        assert (1 <=> 2) == -1
        assert (2 <=> 1) == 1
        assert ('a' <=> 'z') == -1
        // end::spaceship[]
'''
    }

    void testSubscriptOperator() {
        assertScript '''
        // tag::subscript_op[]
        def list = [0,1,2,3,4]
        assert list[2] == 2                         // <1>
        list[2] = 4                                 // <2>
        assert list[0..2] == [0,1,4]                // <3>
        list[0..2] = [6,6,6]                        // <4>
        assert list == [6,6,6,3,4]                  // <5>
        // end::subscript_op[]
        '''

        assertScript '''
        // tag::subscript_destructuring[]
        class User {
            Long id
            String name
            def getAt(int i) {                                             // <1>
                switch (i) {
                    case 0: return id
                    case 1: return name
                }
                throw new IllegalArgumentException("No such element $i")
            }
            void putAt(int i, def value) {                                 // <2>
                switch (i) {
                    case 0: id = value; return
                    case 1: name = value; return
                }
                throw new IllegalArgumentException("No such element $i")
            }
        }
        def user = new User(id: 1, name: 'Alex')                           // <3>
        assert user[0] == 1                                                // <4>
        assert user[1] == 'Alex'                                           // <5>
        user[1] = 'Bob'                                                    // <6>
        assert user.name == 'Bob'                                          // <7>
        // end::subscript_destructuring[]
        '''
    }

    void testMembershipOperator() {
        // tag::membership_op[]
        def list = ['Grace','Rob','Emmy']
        assert ('Emmy' in list)                     // <1>
        // end::membership_op[]
    }

    void testIdentityOperator() {
        // tag::identity_op[]
        def list1 = ['Groovy 1.8','Groovy 2.0','Groovy 2.3']        // <1>
        def list2 = ['Groovy 1.8','Groovy 2.0','Groovy 2.3']        // <2>
        assert list1 == list2                                       // <3>
        assert !list1.is(list2)                                     // <4>
        // end::identity_op[]
    }

    void testCoercionOperator() {
        try {
            // tag::coerce_op_cast[]
            Integer x = 123
            String s = (String) x                                   // <1>
            // end::coerce_op_cast[]
        } catch (ClassCastException e) {
            // tag::coerce_op[]
            Integer x = 123
            String s = x as String                                  // <1>
            // end::coerce_op[]
            assert s == '123'
        }
        assertScript '''
        // tag::coerce_op_custom[]
        class Identifiable {
            String name
        }
        class User {
            Long id
            String name
            def asType(Class target) {                                              // <1>
                if (target == Identifiable) {
                    return new Identifiable(name: name)
                }
                throw new ClassCastException("User cannot be coerced into $target")
            }
        }
        def u = new User(name: 'Xavier')                                            // <2>
        def p = u as Identifiable                                                   // <3>
        assert p instanceof Identifiable                                            // <4>
        assert !(p instanceof User)                                                 // <5>
        // end::coerce_op_custom[]
        '''
    }

    void testDiamondOperator() {
        // tag::diamond_op[]
        List<String> strings = new LinkedList<>()
        // end::diamond_op[]
    }

    void testCallOperator() {
        assertScript '''
        // tag::call_op[]
        class MyCallable {
            int call(int x) {           // <1>
                2*x
            }
        }

        def mc = new MyCallable()
        assert mc.call(2) == 4          // <2>
        assert mc(2) == 4               // <3>
        // end::call_op[]
        '''
    }

    void testOperatorOverloading() {
        assertScript '''
// tag::operator_overload_class[]
class Bucket {
    int size

    Bucket(int size) { this.size = size }

    Bucket plus(Bucket other) {                     // <1>
        return new Bucket(this.size + other.size)
    }
}
// end::operator_overload_class[]
// tag::operator_overload_op[]
def b1 = new Bucket(4)
def b2 = new Bucket(11)
assert (b1 + b2).size == 15                         // <1>
// end::operator_overload_op[]
'''
    }
    void testOperatorOverloadingWithDifferentArgumentType() {
        assertScript '''
class Bucket {
    int size

    Bucket(int size) { this.size = size }

// tag::operator_overload_mixed_class[]
    Bucket plus(int capacity) {
        return new Bucket(this.size + capacity)
    }
// end::operator_overload_mixed_class[]
}
def b1 = new Bucket(4)
// tag::operator_overload_mixed_op[]
assert (b1 + 11).size == 15
// end::operator_overload_mixed_op[]
'''
    }

    private static class Person {
        Long id
        String name
        static Person find(Closure c) { null }
    }

    void testGStringEquals() {
        assertScript '''
            w = 'world'
            str1 = "Hello $w"
            str1 += "!"
            str2 = "Hello $w!"
            str3 = 'Hello world!'

            assert str1 == str3
            assert str2 == str3
            assert str1 == str2
            '''
    }

    void testBooleanOr() {
        assertScript '''
boolean trueValue1 = true, trueValue2 = true, trueValue3 = true
boolean falseValue1 = false, falseValue2 = false, falseValue3 = false

assert (trueValue1 |= true)
assert (trueValue2 |= false)
assert (trueValue3 |= null)
assert (falseValue1 |= true)
assert !(falseValue2 |= false)
assert !(falseValue3 |= null)
'''
    }

    void testBooleanAnd() {
        assertScript '''
boolean trueValue1 = true, trueValue2 = true, trueValue3 = true
boolean falseValue1 = false, falseValue2 = false, falseValue3 = false

assert (trueValue1 &= true)
assert !(trueValue2 &= false)
assert !(trueValue3 &= null)
assert !(falseValue1 &= true)
assert !(falseValue2 &= false)
assert !(falseValue3 &= null)
'''
    }

    void testBooleanXor() {
        assertScript '''
boolean trueValue1 = true, trueValue2 = true, trueValue3 = true
boolean falseValue1 = false, falseValue2 = false, falseValue3 = false

assert !(trueValue1 ^= true)
assert (trueValue2 ^= false)
assert (trueValue3 ^= null)
assert (falseValue1 ^= true)
assert !(falseValue2 ^= false)
assert !(falseValue3 ^= null)
'''
    }
}