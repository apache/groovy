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

import org.apache.groovy.contracts.tests.basic.BaseTestClass
import org.junit.Test

/**
 * <tt>old</tt> variables tests for postconditions.
 *
 * @see groovy.contracts.Ensures
 */

class OldVariablePostconditionTests extends BaseTestClass {

    def templateSourceCode = '''
    package tests

    import groovy.contracts.*

    class OldVariable {

        private $type someVariable

        def OldVariable(final $type other)  {
            someVariable = other
        }

        @Ensures({ old -> old.someVariable != null && old.someVariable != someVariable })
        void setVariable(final $type other)  {
            this.someVariable = other
        }
    }
    '''

    @Test
    void big_decimal() {
        def instance = create_instance_of(createSourceCodeForTemplate(templateSourceCode, [type: BigDecimal.class.getName()]), new BigDecimal(0))
        instance.setVariable new BigDecimal(1)
    }

    @Test
    void big_integer() {
        def instance = create_instance_of(createSourceCodeForTemplate(templateSourceCode, [type: BigInteger.class.getName()]), BigInteger.ZERO)
        instance.setVariable BigInteger.ONE
    }

    @Test
    void string() {
        def instance = create_instance_of(createSourceCodeForTemplate(templateSourceCode, [type: String.class.getName()]), ' ')
        instance.setVariable 'test'
    }

    @Test
    void integer() {
        def instance = create_instance_of(createSourceCodeForTemplate(templateSourceCode, [type: Integer.class.getName()]), Integer.valueOf(0))
        instance.setVariable Integer.valueOf(1)
    }

    @Test
    void test_float() {
        def instance = create_instance_of(createSourceCodeForTemplate(templateSourceCode, [type: Float.class.getName()]), new Float(0))
        instance.setVariable new Float(1)
    }

    @Test
    void test_calendar_date() {
        def now = Calendar.getInstance()
        def not_now = Calendar.getInstance()
        not_now.add(Calendar.DAY_OF_YEAR, 1)

        def instance = create_instance_of(createSourceCodeForTemplate(templateSourceCode, [type: Calendar.class.getName()]), now)
        instance.setVariable not_now

        def date_now = now.getTime()
        def date_not_now = not_now.getTime()

        instance = create_instance_of(createSourceCodeForTemplate(templateSourceCode, [type: Date.class.getName()]), date_now)
        instance.setVariable date_not_now

        def sql_date_now = new java.sql.Date(date_now.getTime())
        def sql_date_not_now = new java.sql.Date(date_not_now.getTime())

        instance = create_instance_of(createSourceCodeForTemplate(templateSourceCode, [type: java.sql.Date.class.getName()]), sql_date_now)
        instance.setVariable sql_date_not_now

        def ts_now = new java.sql.Timestamp(date_now.getTime())
        def ts_not_now = new java.sql.Timestamp(date_not_now.getTime())

        instance = create_instance_of(createSourceCodeForTemplate(templateSourceCode, [type: java.sql.Timestamp.class.getName()]), ts_now)
        instance.setVariable ts_not_now

        //instance = create_instance_of(createSourceCodeForTemplate(dynamic_constructor_class_code, [type: GString.class.getName()]), "${''}")
        //instance.setVariable "${'test' + 1}"
    }

    @Test
    void generate_old_variables_for_super_class() {

        def baseClassSource = '''
        package tests

        import groovy.contracts.*

        class Account {
            protected BigDecimal balance

            def Account( BigDecimal amount = 0.0 ) {
                balance = amount
            }

            void deposit( BigDecimal amount ) {
                balance += amount
            }

            @Requires({ amount >= 0.0 })
            BigDecimal withdraw( BigDecimal amount ) {
                if (balance < amount) return 0.0

                balance -= amount
                return amount
            }

            BigDecimal getBalance() {
                return balance
            }
        }
        '''

        def descendantClassSource = '''
        package tests

        import groovy.contracts.*

        class BetterAccount extends Account {
            @Ensures({ balance == old.balance - (amount * 0.5) })
            BigDecimal withdraw( BigDecimal amount ) {
                if (balance < amount) return 0.0

                balance -= amount * 0.5
                return amount
            }
        }
        '''

        add_class_to_classpath baseClassSource

        def betterAccount = create_instance_of(descendantClassSource)
        betterAccount.deposit(30.0)
        betterAccount.withdraw(10.0)

    }
}