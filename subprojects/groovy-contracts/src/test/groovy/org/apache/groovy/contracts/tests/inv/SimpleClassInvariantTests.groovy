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
package org.apache.groovy.contracts.tests.inv

import org.apache.groovy.contracts.ClassInvariantViolation
import org.apache.groovy.contracts.tests.basic.BaseTestClass
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

class SimpleClassInvariantTests extends BaseTestClass {

    def source1 = '''
@Contracted
package tests

import groovy.contracts.*

@Invariant({ property != null })
class A {

  def property

  def A(def someValue)  {
    property = someValue
  }
}
'''

    def source2 = '''
@Contracted
package tests

import groovy.contracts.*

@Invariant({ property != null })
class A {

  private property

  def A(def someValue)  {
    property = someValue
  }
}
'''

    def source3 = '''
@Contracted
package tests

import groovy.contracts.*

@Invariant({ property != null })
class A {

  private property

  def A(def someValue)  {
    property = someValue
  }

  static me = "me"
}
'''

    @Test
    void class_invariant() {
        create_instance_of(source1, ['test'])

        shouldFail AssertionError, {
            create_instance_of(source1, [null])
        }
    }

    @Test
    void class_invariant_with_private_instance_variable() {
        create_instance_of(source2, ['test'])

        shouldFail AssertionError, {
            create_instance_of(source2, [null])
        }
    }

    @Test
    void class_with_constant() {
        create_instance_of(source3, ['test'])
    }


    @Test
    void multiple_return_statements() {

        def source = """
        import groovy.contracts.*

@Invariant({ property != 0 })
class Account {

   def property = 1

   def some_method()  {
     if (true)  {
         property = 0
         return;
     }

     return;
   }
}
    """

        def source2 = """
        import groovy.contracts.*

@Invariant({ property != 0 })
class Account {

   def property = 1

   def some_method()  {
     if (false)  {
         property = 1
         return;
     }

     property = 0
     return;
   }
}
    """

        def a = create_instance_of(source2)
        shouldFail ClassInvariantViolation, {
            a.some_method()
        }
    }

    @Test
    void duplicate_return_statements() {

        def source = '''
        import groovy.contracts.*

        @Invariant({ elements != null })
        class Stack {
             def elements = []

             def push(def item) {
                 elements.push(item)
             }

             def pop()  {
                 elements.pop()
             }
        }
        '''

        def stack = create_instance_of(source)

        stack.push(1)
        stack.push(2)

        assert stack.pop() == 2
        assert stack.pop() == 1
    }

    @Test
    void avoid_invariant_on_read_only_methods() {

        def source = """
import groovy.contracts.*

@Invariant({ speed() >= 0.0 })
class Rocket {

    def speed() { 1.0 }
}

    """

        create_instance_of(source)
    }


    @Test
    void recursive_invariant_with_getter_method() {

        def source = """
    import groovy.contracts.*

    @Invariant({ speed >= 0.0 })
    class Rocket {

        @Requires({ true })
        def getSpeed() { 1.0 }
    }

        """

        create_instance_of(source)
    }

    @Test
    void direct_field_access() {

        def source = """
        import groovy.contracts.*

        @Invariant({ speed >= 0.0 })
        class Rocket {
            def speed = 0.0

            def increase() {
                this.speed -= 1
            }
        }

            """

        def rocket = create_instance_of(source)

        shouldFail(ClassInvariantViolation) {
            rocket.increase()
        }
    }

    @Test
    void direct_field_access_in_class_invariant() {

        add_class_to_classpath """
            import groovy.contracts.*

            @Invariant({ this.speed >= 0.0 })
            class Rocket {
                def speed = 0.0

                def increase() {
                    this.speed -= 1
                }
            }"""
    }

    @Test
    void private_field_access_in_direct_class() {

        def c = add_class_to_classpath """
                import groovy.contracts.*

                @Invariant({ speed >= 0.0 })
                class Rocket {
                    private double speed = 0.0

                    def increase() {
                        this.speed -= 1
                    }
                }"""

        def rocket = c.newInstance()

        shouldFail ClassInvariantViolation, {
            rocket.increase()
        }
    }

    // GROOVY-12066
    @Test
    void class_invariant_on_static_nested_class_without_explicit_constructor() {

        def c = add_class_to_classpath """
            import groovy.contracts.*

            class Outer {
                @Invariant({ value >= 0 })
                static class Inner {
                    int value = 1
                    def decrease() { value -= 2 }
                }
            }
        """

        def inner = c.getDeclaredClasses().find { it.simpleName == 'Inner' }
        def instance = inner.newInstance()

        // and the invariant is actually enforced on the nested class
        shouldFail ClassInvariantViolation, {
            instance.decrease()
        }
    }

    @Test
    void private_field_access_in_descendant_class() {

        def c = add_class_to_classpath """
                    import groovy.contracts.*

                    @Invariant({ speed >= 0.0 })
                    class Rocket {
                        private double speed = 0.0

                        def increase() {
                            this.speed -= 1
                        }
                    }
                    """

        def c2 = add_class_to_classpath """
            class BetterRocket extends Rocket {}
        """

        def betterRocket = c2.newInstance()

        shouldFail ClassInvariantViolation, {
            betterRocket.increase()
        }
    }

    // GROOVY-12083: multiple @Invariant annotations are AND-combined; every one must be evaluated
    // (unlike postconditions, invariants never used inline mode, so they were not affected, but lock it in)
    @Test
    void multiple_class_invariants_second_is_violated() {

        def source = '''
        import groovy.contracts.*

        @Invariant({ a >= 0 })
        @Invariant({ a < 10 })
        class C {
            int a = 0
            void set(int v) { a = v }
        }
        '''

        def c = create_instance_of(source)
        c.set(5)
        shouldFail(ClassInvariantViolation) {
            c.set(42)
        }
    }

    // GROOVY-12083
    @Test
    void multiple_class_invariants_first_is_violated() {

        def source = '''
        import groovy.contracts.*

        @Invariant({ a < 10 })
        @Invariant({ a >= 0 })
        class C {
            int a = 0
            void set(int v) { a = v }
        }
        '''

        def c = create_instance_of(source)
        c.set(5)
        shouldFail(ClassInvariantViolation) {
            c.set(-3)
        }
    }

    @Test
    void invariant_checked_on_trailing_implicit_return_after_early_return() {
        // GROOVY-12463: same gap as for postconditions: the trailing value skipped the invariant
        def source = '''
        import groovy.contracts.*

        @Invariant({ count >= 0 })
        class A {
            int count = 0
            int dec(boolean early) {
                if (early) return count
                count -= 5
                count
            }
        }
        '''

        def a = create_instance_of(source)
        assert a.dec(true) == 0
        shouldFail(ClassInvariantViolation) {
            a.dec(false)
        }
    }

    // GROOVY-12742: a trait forwarder's body is a bare expression statement, not a block; the invariant
    // call was appended after it, so the forwarder's implicit return value was lost
    def trait_forwarder_source = '''
        import groovy.contracts.*

        @Invariant({ count >= 0 })
        class C implements Counting {
            int count = 0
            int add(int n) { count += n; count }
        }

        trait Counting {
            int one() { 1 }
            String greet() { 'hi' }
            void drain() { count = -1 }
            int takeAll() { count = -1; count }
        }
    '''

    // GROOVY-12742
    @Test
    void trait_forwarder_returns_primitive_value() {
        def c = create_instance_of(trait_forwarder_source)
        assert c.one() == 1
    }

    // GROOVY-12742
    @Test
    void trait_forwarder_returns_object_value() {
        def c = create_instance_of(trait_forwarder_source)
        assert c.greet() == 'hi'
    }

    // GROOVY-12742: the invariant is still checked after a void forwarder
    @Test
    void void_trait_forwarder_checks_invariant() {
        def c = create_instance_of(trait_forwarder_source)
        shouldFail(ClassInvariantViolation) {
            c.drain()
        }
    }

    // GROOVY-12742: the invariant is still checked after a forwarder that returns a value
    @Test
    void trait_forwarder_with_value_checks_invariant() {
        def c = create_instance_of(trait_forwarder_source)
        shouldFail(ClassInvariantViolation) {
            c.takeAll()
        }
    }

    // GROOVY-12742: a method the class defines itself is unaffected
    @Test
    void own_method_beside_trait_forwarders() {
        def c = create_instance_of(trait_forwarder_source)
        assert c.add(2) == 2
        shouldFail(ClassInvariantViolation) {
            c.add(-5)
        }
    }

    // GROOVY-12742: the accessors generated for a trait property have the same bare-statement body
    @Test
    void trait_property_accessors_keep_their_values() {
        def source = '''
        import groovy.contracts.*

        @Invariant({ count >= 0 })
        class D implements Named {
            int count = 0
        }

        trait Named {
            String name = 'x'
            int size = 3
        }
        '''

        def d = create_instance_of(source)
        assert d.name == 'x'
        assert d.size == 3
        d.name = 'y'
        d.size = 4
        assert d.name == 'y'
        assert d.size == 4
    }

    // GROOVY-12742: @Synchronized leaves a non-block body too; an explicit return inside it skipped the invariant
    @Test
    void synchronized_method_with_explicit_return_checks_invariant() {
        def source = '''
        import groovy.contracts.*
        import groovy.transform.Synchronized

        @Invariant({ count >= 0 })
        class E {
            int count = 0
            @Synchronized
            int dec() {
                count -= 5
                return count
            }
        }
        '''

        def e = create_instance_of(source)
        shouldFail(ClassInvariantViolation) {
            e.dec()
        }
    }
}
