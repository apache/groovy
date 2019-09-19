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
class CoercionTest extends GroovyTestCase {

    void testStringToEnumValue() {
        assertScript '''
        // tag::state_enum[]
        enum State {
            up,
            down
        }
        // end::state_enum[]

        try {
            // tag::enum_coerce_assignment[]
            State st = 'up'
            assert st == State.up
            // end::enum_coerce_assignment[]
        } catch (IllegalArgumentException err) {
            assert false:'Test should not have failed'
        }

        try {
            // tag::enum_coerce_assignment_wrong[]
            State st = 'not an enum value'
            // end::enum_coerce_assignment_wrong[]
        } catch (IllegalArgumentException err) {
            // woot!
        }

        try {
            // tag::enum_coerce_assignment_gstring[]
            def val = "up"
            State st = "${val}"
            assert st == State.up
            // end::enum_coerce_assignment_gstring[]
        } catch (IllegalArgumentException err) {
            assert false:'Test should not have failed'
        }


        // tag::enum_switch_method[]
        State switchState(State st) {
            switch (st) {
                case 'up':
                    return State.down // explicit constant
                case 'down':
                    return 'up' // implicit coercion for return types
            }
        }
        // end::enum_switch_method[]

        // tag::enum_switch_test[]
        assert switchState('up' as State) == State.down
        assert switchState(State.down) == State.up
        // end::enum_switch_test[]

        '''
    }

    void testCustomCoercion() {
        assertScript '''
import groovy.transform.ToString
import static java.lang.Math.*

@ToString(includeNames=true)
// tag::polar_class_header[]
class Polar {
    double r
    double phi
// end::polar_class_header[]
// tag::polar_class_astype[]
    def asType(Class target) {
        if (Cartesian==target) {
            return new Cartesian(x: r*cos(phi), y: r*sin(phi))
        }
    }
// end::polar_class_astype[]
// tag::polar_class_footer[]
}
// end::polar_class_footer[]

@ToString(includeNames=true)
// tag::cartesian_class[]
class Cartesian {
   double x
   double y
}
// end::cartesian_class[]

// tag::polar_astype_assert[]
def sigma = 1E-16
def polar = new Polar(r:1.0,phi:PI/2)
def cartesian = polar as Cartesian
assert abs(cartesian.x-sigma) < sigma
// end::polar_astype_assert[]
'''
    }

    void testCustomCoercionWithExternalAsType() {
        assertScript '''
import groovy.transform.ToString
import static java.lang.Math.*

@ToString(includeNames=true)
class Polar {
    double r
    double phi

}
@ToString(includeNames=true)
class Cartesian {
   double x
   double y
}

// tag::polar_metaclass_astype[]
Polar.metaClass.asType = { Class target ->
    if (Cartesian==target) {
        return new Cartesian(x: r*cos(phi), y: r*sin(phi))
    }
}
// end::polar_metaclass_astype[]

def sigma = 1E-16
def polar = new Polar(r:1.0,phi:PI/2)
def cartesian = polar as Cartesian
assert abs(cartesian.x-sigma) < sigma
'''
    }

    void testExplicitClosureCoercion() {
        assertScript '''
// tag::filter_sam_type[]
interface Predicate<T> {
    boolean accept(T obj)
}
// end::filter_sam_type[]
// tag::greeter_sam_type[]
abstract class Greeter {
    abstract String getName()
    void greet() {
        println "Hello, $name"
    }
}
// end::greeter_sam_type[]
// tag::assertions_explicit_closure_to_sam[]
Predicate filter = { it.contains 'G' } as Predicate
assert filter.accept('Groovy') == true

Greeter greeter = { 'Groovy' } as Greeter
greeter.greet()
// end::assertions_explicit_closure_to_sam[]
'''
    }

    void testImplicitClosureCoercionWithAssignment() {
        assertScript '''
interface Predicate<T> {
    boolean accept(T obj)
}

abstract class Greeter {
    abstract String getName()
    void greet() {
        println "Hello, $name"
    }
}

// tag::assertions_implicit_closure_to_sam[]
Predicate filter = { it.contains 'G' }
assert filter.accept('Groovy') == true

Greeter greeter = { 'Groovy' }
greeter.greet()
// end::assertions_implicit_closure_to_sam[]

'''
    }

    void testImplicitClosureCoercionWithAssignmentAndMethodPointer() {
        assertScript '''
interface Predicate<T> {
    boolean accept(T obj)
}

abstract class Greeter {
    abstract String getName()
    void greet() {
        println "Hello, $name"
    }
}

// tag::assertions_implicit_closure_to_sam_and_method_pointer[]

boolean doFilter(String s) { s.contains('G') }

Predicate filter = this.&doFilter
assert filter.accept('Groovy') == true

Greeter greeter = GroovySystem.&getVersion
greeter.greet()
// end::assertions_implicit_closure_to_sam_and_method_pointer[]

'''
    }

    void testClosureCoercionWithMethodCall() {
        assertScript '''
interface Predicate<T> {
    boolean accept(T obj)
}

// tag::method_accepting_filter[]
public <T> List<T> filter(List<T> source, Predicate<T> predicate) {
    source.findAll { predicate.accept(it) }
}
// end::method_accepting_filter[]

// tag::method_call_with_explicit_coercion[]
assert filter(['Java','Groovy'], { it.contains 'G'} as Predicate) == ['Groovy']
// end::method_call_with_explicit_coercion[]

// tag::method_call_with_implicit_coercion[]
assert filter(['Java','Groovy']) { it.contains 'G'} == ['Groovy']
// end::method_call_with_implicit_coercion[]
'''
    }

    void testClosureCoercionToInterface() {
        assertScript '''
// tag::foobar_interface[]
interface FooBar {
    int foo()
    void bar()
}
// end::foobar_interface[]

// tag::foobar2closure_coercion[]
def impl = { println 'ok'; 123 } as FooBar
// end::foobar2closure_coercion[]

// tag::foobarintf_assertions[]
assert impl.foo() == 123
impl.bar()
// end::foobarintf_assertions[]
        '''
    }

    void testClosureCoercionToClass() {
        assertScript '''
// tag::closure2foobarclass[]
class FooBar {
    int foo() { 1 }
    void bar() { println 'bar' }
}

def impl = { println 'ok'; 123 } as FooBar

assert impl.foo() == 123
impl.bar()
// end::closure2foobarclass[]
'''
    }

    void testCoerceMapToIterator() {
        assertScript '''
// tag::coerce_map_to_iterator[]
def map
map = [
  i: 10,
  hasNext: { map.i > 0 },
  next: { map.i-- },
]
def iter = map as Iterator
// end::coerce_map_to_iterator[]

// tag::use_coerced_iterator[]
while ( iter.hasNext() )
println iter.next()
assert map.i==0
// end::use_coerced_iterator[]
'''
    }

    void testCoerceThrowsNPE() {
        assertScript '''
// tag::define_x_interface[]
interface X {
    void f()
    void g(int n)
    void h(String s, int n)
}

x = [ f: {println "f called"} ] as X
// end::define_x_interface[]

// tag::call_existing_method[]
x.f() // method exists
// end::call_existing_method[]

try {
// tag::call_non_existing_method[]
    x.g() // MissingMethodException here
// end::call_non_existing_method[]
} catch (MissingMethodException e) {
    println "Caught exception"
}
try {
// tag::call_notimplemented_method[]
    x.g(5) // UnsupportedOperationException here
// end::call_notimplemented_method[]
} catch (UnsupportedOperationException e) {
    println "Caught exception"
}
'''
    }

    void testAsVsAsType() {
        assertScript '''
// tag::as_keyword[]
interface Greeter {
    void greet()
}
def greeter = { println 'Hello, Groovy!' } as Greeter // Greeter is known statically
greeter.greet()
// end::as_keyword[]

/*
// tag::clazz_greeter_header[]
Class clazz = Class.forName('Greeter')
// end::clazz_greeter_header[]

// tag::incorrect_as_usage[]
greeter = { println 'Hello, Groovy!' } as clazz
// throws:
// unable to resolve class clazz
// @ line 9, column 40.
//   greeter = { println 'Hello, Groovy!' } as clazz
// end::incorrect_as_usage[]
*/
Class clazz = Greeter
// tag::fixed_as_usage[]
greeter = { println 'Hello, Groovy!' }.asType(clazz)
greeter.greet()
// end::fixed_as_usage[]
'''
    }
}
