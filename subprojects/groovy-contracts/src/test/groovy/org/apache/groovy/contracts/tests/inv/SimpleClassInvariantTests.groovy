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

import org.apache.groovy.contracts.tests.basic.BaseTestClass
import org.junit.Test
import org.apache.groovy.contracts.ClassInvariantViolation

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
}
