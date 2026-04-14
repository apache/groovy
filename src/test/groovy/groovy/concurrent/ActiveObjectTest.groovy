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
package groovy.concurrent

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class ActiveObjectTest {

    @Test
    void testBasicActiveObject() {
        assertScript '''
            import groovy.transform.ActiveObject
            import groovy.transform.ActiveMethod

            @ActiveObject
            class Counter {
                private int count = 0

                @ActiveMethod
                void increment() { count++ }

                @ActiveMethod
                int getCount() { count }
            }

            def c = new Counter()
            c.increment()
            c.increment()
            c.increment()
            assert c.getCount() == 3
        '''
    }

    @Test
    void testNonBlockingMethod() {
        assertScript '''
            import groovy.transform.ActiveObject
            import groovy.transform.ActiveMethod
            import groovy.concurrent.Awaitable

            @ActiveObject
            class Calculator {
                @ActiveMethod(blocking = false)
                def multiply(int a, int b) { a * b }
            }

            def calc = new Calculator()
            def result = calc.multiply(6, 7)
            assert result instanceof Awaitable
            assert await(result) == 42
        '''
    }

    @Test
    void testThreadSafety() {
        assertScript '''
            import groovy.transform.ActiveObject
            import groovy.transform.ActiveMethod
            import java.util.concurrent.CountDownLatch

            @ActiveObject
            class SafeCounter {
                private int count = 0

                @ActiveMethod
                void increment() { count++ }

                @ActiveMethod
                int getCount() { count }
            }

            def counter = new SafeCounter()
            def n = 100
            def latch = new CountDownLatch(n)

            n.times {
                Thread.start {
                    counter.increment()
                    latch.countDown()
                }
            }
            latch.await(5, java.util.concurrent.TimeUnit.SECONDS)

            assert counter.getCount() == n
        '''
    }

    @Test
    void testActiveObjectWithState() {
        assertScript '''
            import groovy.transform.ActiveObject
            import groovy.transform.ActiveMethod

            @ActiveObject
            class Account {
                private double balance = 0

                @ActiveMethod
                void deposit(double amount) { balance += amount }

                @ActiveMethod
                void withdraw(double amount) {
                    if (amount > balance) throw new RuntimeException('Insufficient funds')
                    balance -= amount
                }

                @ActiveMethod
                double getBalance() { balance }
            }

            def account = new Account()
            account.deposit(100)
            account.deposit(50)
            account.withdraw(30)
            assert account.getBalance() == 120.0
        '''
    }

    @Test
    void testExceptionPropagation() {
        assertScript '''
            import groovy.transform.ActiveObject
            import groovy.transform.ActiveMethod

            @ActiveObject
            class Failing {
                @ActiveMethod
                void doWork() { throw new RuntimeException('active boom') }
            }

            def f = new Failing()
            try {
                f.doWork()
                assert false : 'should have thrown'
            } catch (RuntimeException e) {
                assert e.message == 'active boom'
            }
        '''
    }

    @Test
    void testMixOfActiveAndNormalMethods() {
        assertScript '''
            import groovy.transform.ActiveObject
            import groovy.transform.ActiveMethod

            @ActiveObject
            class Mixed {
                private List items = []

                @ActiveMethod
                void add(String item) { items << item }

                @ActiveMethod
                List getItems() { new ArrayList(items) }

                // NOT @ActiveMethod — runs on caller thread
                String description() { "Mixed object" }
            }

            def m = new Mixed()
            assert m.description() == "Mixed object"
            m.add("hello")
            m.add("world")
            assert m.getItems() == ["hello", "world"]
        '''
    }
}
