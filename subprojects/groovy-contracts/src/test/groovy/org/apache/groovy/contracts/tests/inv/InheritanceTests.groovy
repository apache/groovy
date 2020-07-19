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

class InheritanceTests extends BaseTestClass {

    def source1 = '''
@Contracted
package tests

import groovy.contracts.*

@Invariant({ property != null })
class A {
  def property

  def A(def value) { property = value }
}
'''

    def source2 = '''
@Contracted
package tests


import groovy.contracts.*

@Invariant({ property2 != null })
class B extends A {
  def property2

  def B(def value, def value2) { super(value); property2 = value2 }

  def set_values(def value, def value2)  {
    property = value
    property2 = value2
  }
}

'''

    def source3 = '''
@Contracted
package tests


import groovy.contracts.*

@Invariant({ property2 != null })
class C extends B {
  def property3

  def C(def value, def value2, def value3) { super(value, value2); property3 = value3 }
}

'''

    def source11 = '''
@Contracted
package tests


import groovy.contracts.*

@Invariant({ property?.size() > 0 })
class A {
   private String property

   def A(String value) { property = value }
}

'''

    def source12 = '''
@Contracted
package tests


import groovy.contracts.*

class B extends A {
   def B(String value) { super(value) }
}

'''

    def source21 = '''
@Contracted
package tests

import groovy.contracts.*

@Invariant({ prop1 != null && prop2 != null })
class PrivateConstructor {

  def prop1
  def prop2

  def PrivateConstructor(def arg1, def arg2)  {
    prop1 = arg1
    prop2 = arg2
  }

  private PrivateConstructor()  {}
}
'''

    def source31 = '''
@Contracted
package tests

import groovy.contracts.*

@Invariant({ isAvailable() == true })
abstract class A {

  abstract boolean isAvailable()
}

'''

    def source32 = '''
@Contracted
package tests

import groovy.contracts.*

class B extends A {
  boolean isAvailable() { return true }
}

'''

    def source41 = '''
@Contracted
package tests

import groovy.contracts.*

@Invariant({ isAvailable() == true })
class A {
    def boolean isAvailable() { return true }
}
'''

    def source51 = '''
@Contracted
package tests

import groovy.contracts.*

@Invariant({ property1 != null })
class A {

  def property1 = "test"

  def A() {}

  def set_values(def prop) { property1 = prop }
}
'''

    def source52 = '''
@Contracted
package tests

import groovy.contracts.*

@Invariant({ property2 != null })
class B extends A {

  def property2 = "test"

  def B() {}

  def set_values(def prop) { property2 = prop }
}
'''

    def source61 = '''
@Contracted
package tests

import groovy.contracts.*

@Invariant({ property1 != null })
class A {

  private def property1 = "test"

  def A() {}

  def set_values(def prop) { property1 = prop }
}
'''

    def source62 = '''
@Contracted
package tests

import groovy.contracts.*

@Invariant({ property2 != null })
class B extends A {

  def property2 = "test"

  def B() {}

  def set_values(def prop) { property2 = prop }
}
'''

    def source71 = '''
package tests

import groovy.contracts.*

@Invariant({ getBalance() >= 0.0 })
class Account {

    protected BigDecimal balance = 0.0

    def Account( def amount = 0.0 )
    {
        balance = amount
    }

    @Requires({ amount >= 0.0 })
    @Ensures({ balance == old.balance + amount })
    void deposit( def amount )
    {
        balance += amount
    }

    @Requires({ amount >= 0.0 && getBalance() >= amount })
    @Ensures({ balance == old.balance - amount })
    def withdraw( def amount )
    {
        balance -= amount
        return amount
    }

    def getBalance()
    {
        balance
    }
}
'''


    @Test
    void two_way_inheritance_path() {
        create_instance_of(source1, ['test'])
        create_instance_of(source2, ['test', 'test2'])

        shouldFail AssertionError, {
            create_instance_of(source2, [null, 'test2'])
        }
    }

    @Test
    void three_way_inheritance_path() {
        create_instance_of(source1, ['test'])
        create_instance_of(source2, ['test', 'test2'])
        create_instance_of(source3, ['test', 'test2', 'test3'])

        shouldFail AssertionError, {
            create_instance_of(source3, [null, 'test2', 'test3'])
        }

        shouldFail AssertionError, {
            create_instance_of(source3, [null, null, 'test3'])
        }

        shouldFail AssertionError, {
            create_instance_of(source3, ['test', null, 'test3'])
        }
    }

    /*
   see: http://gcontracts.lighthouseapp.com/projects/71511/tickets/3-accessing-private-variables-from-invariant
   @Test void with_private_instance_variable_in_super_class()  {
      create_instance_of(source11, ['test'])
      create_instance_of(source12, ['test'])
  
      shouldFail AssertionError, {
        create_instance_of(source12, [''])
      }
    }*/

    @Test
    void invariant_check_on_method_call() {
        create_instance_of(source1, ['test'])
        def b = create_instance_of(source2, ['test', 'test2'])

        shouldFail AssertionError, {
            b.set_values(null, null)
        }

        shouldFail AssertionError, {
            b.set_values(null, '')
        }

        shouldFail AssertionError, {
            b.set_values('', null)
        }
    }

    @Test
    void private_constructor_creation() {
        create_instance_of(source21)
    }

    @Test
    void public_constructor_creation() {
        shouldFail AssertionError, {
            create_instance_of(source21, ['test1', null])
        }
    }

    @Test
    void inherited_class_invariant() {
        add_class_to_classpath(source51)
        def b = create_instance_of(source52, [])

        shouldFail AssertionError, {
            b.set_values(null)
        }
    }

    @Test
    void inherited_class_invariant_with_private_instance_variable() {
        add_class_to_classpath(source61)
        def b = create_instance_of(source62, [])

        shouldFail AssertionError, {
            b.set_values(null)
        }

    }

    @Test
    void recursive_class_invariant() {

        def b = create_instance_of(source71)
        assert b != null
    }

    @Test
    void abstract_method_with_postcondition() {

        add_class_to_classpath """
      package tests

      import groovy.contracts.*

      abstract class Base {
        @Ensures({ result })
        abstract List<String> sources()
      }
      """

        def c = add_class_to_classpath """
      package tests

      class DirectImpl extends Base {

          List<String> sources() { ['a','b','c'] }

      }
      """

        c.newInstance().sources()
    }

    @Test(expected = ClassInvariantViolation)
    void separate_class_invariant() {
        def c = add_class_to_classpath """
            package tests
      
            import groovy.contracts.*
      
            @Invariant({
                i_never_null: i != null
                j_never_null: j != null
            })
            class Test {
                private def i
                private def j
            }
            """

        c.newInstance()
    }
}
