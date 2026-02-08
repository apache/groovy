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
package org.apache.groovy.contracts.tests.post

import org.apache.groovy.contracts.PostconditionViolation
import org.apache.groovy.contracts.tests.basic.BaseTestClass
import org.junit.Test

import static org.junit.Assert.assertEquals

class SimplePostconditionTests extends BaseTestClass {

    def source_postconditions = '''
@Contracted
package tests

import groovy.contracts.*

class A {

  def property
  def property2

  @Ensures({ property == value })
  void change_property_value(def value)  {
    property = value
  }

  @Ensures({ property == value && property2 == value2 })
  void change_property_values(def value, def value2)  {
    property  = value
    property2 = value2
  }

  @Ensures({ !(property == value) })
  void change_property_value_not(def value)  {
    ;
  }
}
'''

    @Test
    void simple_boolean_expression() {

        def a = create_instance_of(source_postconditions)
        a.change_property_value('test')

        assertEquals 'test', a.property
    }

    @Test
    void binary_boolean_expression() {

        def a = create_instance_of(source_postconditions)
        a.change_property_values('test', 'test2')

        assertEquals 'test', a.property
        assertEquals 'test2', a.property2
    }

    @Test
    void negated_boolean_expression() {

        def a = create_instance_of(source_postconditions)
        a.change_property_value_not('test')
    }


    @Test
    void multiple_return_statements() {

        def source = """
        import groovy.contracts.*

class Account {

   @Ensures({ result == 2 })
   def some_method()  {
     if (true)  {
         return 1
     }

     return 2
   }
}
    """

        def a = create_instance_of(source)
        shouldFail PostconditionViolation, {
            a.some_method()
        }
    }

    @Test
    void multiple_return_statements_with_try_finally() {

        def source = """
        import groovy.contracts.*

class Account {

   @Ensures({ result == 3 })
   def some_method()  {
     if (true)  {
         try {
            throw new Exception ('test')
            return 1
         } finally {
            return 3
         }
     }

     return 2
   }
}
    """

        def a = create_instance_of(source)
        assert a.some_method()
    }

    @Test
    void multiple_return_statements_with_try_finally_violation() {

        def source = """
        import groovy.contracts.*

class Account {

   @Ensures({ result != 3 })
   def some_method()  {
     if (true)  {
         try {
            throw new Exception ('test')
            return 1
         } finally {
            return 3
         }
     }

     return 2
   }
}
    """

        def a = create_instance_of(source)
        shouldFail PostconditionViolation, {
            a.some_method()
        }
    }

    @Test
    void no_postcondition_on_static_methods() {

        def source = """
        import groovy.contracts.*

class Account {

   @Ensures({ amount != null })
   static def withdraw(def amount)  {
     return amount
   }
}
    """

        def clazz = add_class_to_classpath(source)
        assert clazz.withdraw(null) == null
    }

    @Test
    void use_result_with_parameter_value() {


        def source = """

        import groovy.contracts.*

        class A {

            @Ensures({ result.size() == 2 && result.contains(s) && result.contains(s2) })
            List<String> toList(String s, String s2)  {
               [s, s2].sort()
            }

        }
        """

        def a = create_instance_of(source)
        a.toList("a", "b") == ["a", "b"]

    }

    @Test
    void complex_return_statement() {

        def source = """
        import groovy.contracts.*

        class A {

            @Requires({ ( messages != null ) })
            @Ensures({  ( result   != null ) && ( result.size() == messages.size()) })
            List<String> sortForAll ( Collection<String> messages )
            {
                messages.sort {
                    String m1, String m2 ->

                    int  urgencyCompare = ( m1 <=> m2 )
                    if ( urgencyCompare != 0 ){ return urgencyCompare }

                    - ( m1 <=> m2 )
                }
            }

        }

        """

        def a = create_instance_of(source)
        a.sortForAll(["test1", "test2"]) == ["test1", "test2"]

    }

    // this test failed on JDK 7.0
    @Test
    void ensures_with_generic_parameter_type() {

        def source = """
                import groovy.contracts.*

                    class A {

                        @Ensures ({ type.isInstance( result ) })
                        final public <T> T extension( String param, Class<T> type )
                        {
                            (( T ) 'test' )
                        }
                    }

             """

        def a = create_instance_of(source)
        a.extension('Test', String.class)
    }

    @Test
    void ensures_with_default_parameters() {

        def source = """
                import groovy.contracts.*

                    class A {

                        @Ensures({ a == 12 })
                        def m(def a = 12) {}
                    }
             """

        def a = create_instance_of(source)
        a.m()
    }

    @Test(expected = PostconditionViolation.class)
    void ensures_on_private_methods() {

        def source = """
                    import groovy.contracts.*

                        class A {

                            @Ensures({ result != null })
                            private def m(def a) { return null }
                        }
                 """

        def a = create_instance_of(source)
        a.m(null)
    }
}
