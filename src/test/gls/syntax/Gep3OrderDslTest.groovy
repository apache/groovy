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
package gls.syntax

import groovy.test.GroovyTestCase
import org.codehaus.groovy.control.CompilerConfiguration

class Gep3OrderDslTest extends GroovyTestCase {
    @Override
    protected void setUp() {
        super.setUp()

        // syntax for 200.shares
        Integer.metaClass.getShares = { -> delegate }
    }

    @Override
    protected void tearDown() {
        // remove the getShares method by restoring the original metaclass for Integer
        Integer.metaClass = null

        super.tearDown()
    }

    void testDsl() {
        // use the script binding for silent sentence words like "to", "the"
        def binding = new CustomBinding()

        def config = new CompilerConfiguration()
        config.scriptBaseClass = OrderDslBaseScriptClass.name

        def shell = new GroovyShell(this.class.classLoader, binding, config)

        def script = shell.parse('''
            order to buy 200.shares of GOOG {
                limitPrice       500
                allOrNone        false
                at the value of  { qty * unitPrice - 100 }
            }

            order to sell 150.shares of VMW {
                limitPrice       80
                allOrNone        true
                at the value of  { qty * unitPrice }
            }
        ''')

        script.run()

        List<Order> orders = script.getAllOrders()

        assert orders.size() == 2

        assert orders[0].action == Action.Buy
        assert orders[0].allOrNone == false
        assert orders[0].limitPrice == 500
        assert orders[0].quantity == 200
        assert orders[0].security.name == 'GOOG'

        assert orders[1].action == Action.Sell
        assert orders[1].allOrNone == true
        assert orders[1].limitPrice == 80
        assert orders[1].quantity == 150
        assert orders[1].security.name == 'VMW' 
    }
}

abstract class OrderDslBaseScriptClass extends Script {

    List<Order> allOrders = []

    /** Script helper method for "GOOG {}", "VMW {}", etc. */
    def methodMissing(String name, args) {
        new SecurityAndCharacteristics(
                security: new Security(name: name),
                characteristics: args[0]
        )
    }

    /** Script helper method to make "order to" silent by just creating our current order */
    def order(to) {
        def o = new Order()
        allOrders << o
        return o
    }
}

enum Action { Buy, Sell }

class Order {
    Security security
    Integer quantity, limitPrice
    boolean allOrNone
    Closure valueCalculation
    Action action

    def buy(Integer quantity) {
        this.quantity = quantity
        this.action = Action.Buy
        return this
    }

    def sell(Integer quantity) {
        this.quantity = quantity
        this.action = Action.Sell
        return this
    }

    def limitPrice(Integer limit) {
        this.limitPrice = limit
    }

    def allOrNone(boolean allOrNone) {
        this.allOrNone = allOrNone
    }

    def at(Closure characteristicsClosure) {
        return this
    }

    def value(Closure valueCalculation) {
        this.valueCalculation = valueCalculation
    }

    /** Characteristics of the order: "of GOOG {...}" */
    def of(SecurityAndCharacteristics secAndCharact) {
        security = secAndCharact.security
        Closure c = secAndCharact.characteristics.clone()
        c.resolveStrategy = Closure.DELEGATE_FIRST
        c.delegate = this
        c()
        return this
    }

    /** Valuation closure: "of { qty, unitPrice -> ... }" */
    def of(Closure valueCalculation) {
        // in order to be able to define closures like { qty * unitPrice }
        // without having to explicitely pass the parameters to the closure
        // we can wrap the closure inside another one
        // and that closure sets a delegate to the qty and unitPrice variables
        def wrapped = { qty, unitPrice ->
            Closure cloned = valueCalculation.clone()
            cloned.resolveStrategy = Closure.DELEGATE_FIRST
            cloned.delegate = [qty: qty, unitPrice: unitPrice]
            cloned()
        }
        return wrapped
    }

    String toString() {
        "$action $quantity shares of $security.name at limit price of $limitPrice"
    }
}

class Security {
    String name
}

class SecurityAndCharacteristics {
    Security security
    Closure characteristics
}

class CustomBinding extends Binding {
    def getVariable(String word) {
        // return System.out when the script requests to write to 'out'
        if (word == "out") System.out

        // don't thrown an exception and return null
        // when a silent sentence word is used,
        // like "to" and "the" in our DSL
        null
    }
}