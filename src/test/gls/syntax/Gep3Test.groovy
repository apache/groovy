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

import static Container.*
import static Ingredient.*
import static CookingAction.*
import static Temperature.*

/**
 * Test case for "extended command expressions" (GEP-3) added in Groovy 1.8
 *
 * Simple table presenting what is possible and what is not
 * according to this table old syntax should works the same as now
 *
 * expression              | meaning                   | before extended command expressions (GEP-3)
 *  foo {c}                |  foo({c})                 |  (same meaning)
 *  foo a1                 |  foo(a1)                  |  (same meaning)
 *  foo a1()               |  foo(a1())                |  (same meaning)
 *  foo a1 {c}             |  foo(a1({c}))             |  (same meaning)
 *  foo a1(x) {c}          |  foo(a1(x,{c})            |  (same meaning)
 *  foo a1 a2              |  foo(a1).getA2()          |   not allowed
 *  foo a1() a2            |  foo(a1()).getA2()        |   not allowed
 *  foo a1 a2()            |  foo(a1).a2()             |   not allowed
 *  foo a1 a2 {c}          |  foo(a1).a2{c}            |   not allowed
 *  foo a1 {c} a2          |  foo(a1{c}).getA2()       |   not allowed
 *  foo a1 {c} a2 {c}      |  foo(a1{c}).a2{c}         |   not allowed
 *  foo a1 a2 a3           |  foo(a1).a2(a3)           |   not allowed
 *  foo a1() a2 a3()       |  foo(a1()).a2(a3())       |   not allowed
 *  foo a1 a2() a3         |  foo(a1).a2().getA3()     |   not allowed
 *  foo a1 a2 a3 {c}       |  foo(a1).a2(a3({c}))      |   not allowed
 *  foo a1 a2 a3 a4        |  foo(a1).a2(a3).getA4()   |   not allowed
 *  foo a1 a2 a3 a4 {c}    |  foo(a1).a2(a3).a4{c}     |   not allowed
 *  foo {} a1 {}           |  foo({}).a1({})           |  foo({}).call(a1({}))  <- breaking change
 *  foo {} a1 {} a2 {}     |  foo({}).a1({}).a2({})    |   not allowed
 *  foo a1 a2[1](){} a3 a4 |  foo(a1).a2[1](){}.a3(a4) |   not allowed
 *
 * Summary of the pattern
 * - A command-expression is composed of an even number of elements
 * - The elements are alternating a method name, and its parameters
 *  (can be named and non-named parameters)
 * - A parameter element can be any kind of expression (ie. a method
 *  call foo(), foo{}, or some expression like x+y)
 * - All those pairs of method name and parameters are actually chained
 *  method calls (ie. send "hello" to "Guillaume" is two methods chained
 *  one after the other as send("hello").to("Guillaume"))
 * - extend command expressions to be allowed on the RHS of assignments.
 *  def txt = foo a1() not allowed right now
 */
class Gep3Test extends GroovyTestCase {

    protected void tearDown() {
        Number.metaClass = null
        Integer.metaClass = null
    }

    static String txt = "Lidia is Groovy ;)"

    void testSimpleClassicalCommandExpressions() {
        foo txt
        foo a1()
        foo a2{}
        foo a2{}, and: txt
    }

    static void foo(a) { assert a == txt }
    static void foo(Map m, a) { assert a == txt && m.and == txt }
    static a1() { return txt }
    static a2(Closure c) { return txt }

    void testNewSyntax() {
        def expectedResult = "from:Lidia;to:Guillaume;body:how are you?;"
        def e = new Email()
        e.from "Lidia" to "Guillaume" body "how are you?"
        def result = e.send()

        assert expectedResult == result
    }

    void testContactInfo() {
        def contact = [name: { String name -> assert name == "Guillaume"; [age: { int age -> assert age == 33 }]}]
        contact.name "Guillaume" age 33
    }

    void testArtistPaintingWithAMixOfNamedAndNonNamedParams() {
        Number.metaClass.getPm = { -> "$delegate PM" }

        def artist = [
            paint: { String what ->
                assert what == "wall"
                [
                    with: { Map m, String c1, String c2 ->
                        assert m.and == "Blue"
                        assert c1 == "Red"
                        assert c2 == "Green"
                        [
                            at: { String time ->
                                assert time == "3 PM"
                            }
                        ]
                    }
                ]
            }
        ]

        artist.paint "wall" with "Red", "Green", and: "Blue" at 3.pm
    }

    void testArgWith() {
        def arr = ["he", "ll", "o"]
        def concat = { String s1 -> [with: { String s2 -> [and: { String s3 -> assert s1+s2+s3 == "hello"}]}]}

        concat arr[0] with arr[1] and arr[2]
    }

    void testWaitAndExecuteUsingParamsTakingClosureAsArg() {
        Number.metaClass.getSecond { -> delegate * 1000 }
        def wait = { int t -> [and: { Closure c -> c() }]}

        wait 1.second and execute { assert true }
    }
    static execute(Closure c) { c }


    static String message = ""
    static drugQuantity, drug

    void testMedicine() {
        Number.metaClass.getPills = { -> new DrugQuantity(q: delegate, form: "pills") }
        Number.metaClass.getHours = { -> new Duration(q: delegate, unit: "hours") }

        def chloroquinine = new Drug(name: "Chloroquinine")

        take 3.pills of chloroquinine after 6.hours

        assert message == "Take 3 pills of Chloroquinine after 6 hours"

    }

    def take(DrugQuantity dq) { drugQuantity = dq; this }
    def of(Drug d) { drug = d; this }
    def after(Duration dur) { message = "Take $drugQuantity of $drug after $dur" }

    void testRecipeDsl() {
        def (once, twice) = [1, 2]

        Integer.metaClass.getMinutes { delegate }

        Recipe.instructions {
            take medium_bowl
            combine soy_sauce, vinegar, chili_powder, garlic
            place chicken into sauce
            turn once to coat
            marinate 30.minutes at room_temperature
        }
    }

    void testExtendedCommandExpressionSpanningTwoLinesWithNewlineAfterNamedArg() {
        boolean success = false
        def good = true
        def margherita = [tastes: { boolean b -> success = true }]
        def check = { Map m -> margherita }

        check that:
                margherita tastes good

        assert success
    }

    def check(Map m) { m.that }

    void testExtendedCommandExpressionsOnTheRHS() {
        def ( coffee,   sugar,   milk,   liquor ) =
            ["coffee", "sugar", "milk", "liquor"]
        def drink = Drink.&drink

        def r1 = drink coffee
        assert r1.beverage == coffee && !r1.ingredients

        def r2 = drink coffee with sugar
        assert r2.beverage == coffee && r2.ingredients == [sugar]

        def r3 = drink coffee with sugar, milk
        assert r3.beverage == coffee && r3.ingredients == [sugar, milk]

        r3 = drink coffee with sugar, milk and liquor
        assert r3.beverage == coffee && r3.ingredients == [sugar, milk, liquor]
    }

    /**
     * case             a b  c d
     * equivalent       a(b).c(d)
     */
    void testNominalCase() {

        def turned = false
        def (left, right) = ['left', 'right']
        def turn = { String s -> [then: { turned = true }] }

        turn left then right

        assert turned
    }

    /**
     * For odd number of elements, treat the last element as a call to a getter
     *
     * case            a b  c
     * equivalent      a(b).getC()
     */
    void testTrailingElementAsGetter() {
        def drank = false
        def more = 'more'
        def drink = { String s -> [milk: { drank = true }()] }

        def d = drink more milk

        assert drank
    }

    /**
     * Only closure parameters, enabling interesting control constructs
     *
     * case          a {}  b {}  c {}
     * equivalent    a({}).b({}).c({})
     *
     */
    void testOnlyClosureParameters() {
        def bdd = 0
        def given = { c1 -> c1(); [when: { c2 -> c2(); [then: { c3 -> c3() }] }]}

        given {
            bdd++
        } when {
            bdd++
        } then {
            bdd++
        }

        assert bdd == 3
    }

    /**
     * If the last method takes no arguments, parentheses are required
     *
     * case            a b  c()
     * equivalent      a(b).c()
     */
    void testZeroArgMethodCallAtEndOfChain() {
        def built = false
        def all = 'all'
        def select = { String s -> [build: { built = true }] }

        select all build()

        assert built
    }

    /**
     * A zero-arg method call in the middle of a chain requires parentheses
     *
     * case            a b  c() d e
     * equivalent      a(b).c().d(e)
     */
    void testZeroArgMethodCallInTheMiddleOfTheChain() {
        def uploaded = false
        def (file, here) = ['file', 'here']
        def upload = { String s1 -> [check: {-> [decompress: { String s2 -> uploaded = true }] }]}

        upload file check() decompress here

        assert uploaded
    }

    /**
     * Case where a middle element of the chain is actually a pretty complex exception
     *
     * case            a b  c[1](){} d e
     * equivalent      a(b).c[1](){}.d(e)
     */
    void testComplexCaseWithSubscriptAndMethodCallWithClosureArgument() {
        def resolved = false
        def (cube, topLayer, down) = ['cube', 3, 'down']
        def resolve = { String s1 -> [move: [0, 1, 2, { c -> [upside: { String s2 -> resolved = true }] }]] }

        resolve cube move [topLayer]() {} upside down

        assert resolved
    }
    
    /**
    * Case where an Integer is used as name
    *
    * case            a b 1 2
    * equivalent      a(b)."1"(2)
    */
    void testIntegerAsName() {
        Integer.metaClass."1" = {x-> assert delegate == 10; x}
        def a = {x-> assert x == "b"; 10}
        def b = "b"
        def x = a b 1 2
        assert x == 2
        assert a(b)."1"(2) == 2
    }

    void testMethodAndMethodCallTakingClosureArgument() {
        def valued = { it() }
        def at = { it }

        def res1 = valued at {}
        def res2 = valued at { 42 }

        assert res1 == null
        assert res2 == 42
    }

    // case             a b  c d
    // equivalent       a(b).c(d)
    void testTurnLeftThenRight() {
        def turned = false
        def (left, right) = ['left', 'right']
        def turn = { String s -> [then: { turned = true }] }

        turn left then right

        assert turned
    }
    
    // case         task copy(type: Copy) { 10 }
    // equivalent   task(copy(type:Copy,{10}))
    void testInnerMethodWithClosure() {
        // with simple expression
        assertScript """
            class Copy{}
            def task(x) {
                assert x == 2
            }
            def copy (map, closure) {
                assert map.type == Copy
                assert closure() == 10
                2
            }
            task copy(type: Copy) { 10 }
        """
        // with nested gep3
        assertScript """
            class Copy{}
            def task(x) {
                assert x == 2
            }
            def copy (map, closure) {
                assert map.type == Copy
                assert closure() == 100
                2
            }
            def a(x){this}
            def b(x){x*10}
            task copy(type: Copy) { a 10 b 10 }        
        """
    }
    
    void testGradleDSL() {
        assertScript '''
            def invokeMethod(String name, args) {
                if (name ==~ "/c/.*") {
                    def namedArgs = args[0]
                    def closure = args[1]
                    
                    assert namedArgs instanceof Map
                    assert namedArgs.controller == 'foo'
                    assert namedArgs.action == 'bar'
            
                    closure()
                }
            }
            
            def constraints(Closure c) {
                c.delegate = [authCode: { Map m -> println m }]
                c.resolveStrategy = Closure.DELEGATE_FIRST
                c() 
            }
            
            def authCode(Map m) {
                assert !m.blank
            }
            
            def val = 'xyz'
            
            name xxx: "/c/$val"(controller: 'foo', action: 'bar') {
                constraints {
                    authCode blank: false 
                }
            }        
        '''
    }
    
    void testUsageOfInnerClass() {
        assertScript """
            class Demo  {
               void doit() {
                   execute new Runnable(){
                       void run() {
                           println 'hello'
                       }
                   }
               }
               
               void execute(arg) {
                   arg.run()
               }
            }
            new Demo().doit()
        """
    }
}


class Drink {
    String beverage
    List<String> ingredients = []

    static Drink drink(String beverage) {
        new Drink(beverage: beverage)
    }

    def with(String... ingredients) {
        this.ingredients = ingredients.toList()
        return this
    }

    def and(String ingredient) {
        this.ingredients << ingredient
        return this
    }
}

enum Container { medium_bowl }
enum Ingredient { soy_sauce, vinegar, chili_powder, garlic, chicken, sauce }
enum CookingAction { coat }
enum Temperature { room_temperature }

class Recipe {
    static instructions(Closure c) {
        def clone = c.clone()
        clone.delegate = new Recipe()
        clone.resolveStrategy = Closure.DELEGATE_ONLY
        clone()
    }

    void take(Container cont) {
        assert cont == medium_bowl
    }

    void combine(Ingredient... ingr) {
        assert ingr.every { it in Ingredient.values() }
    }

    def place(Ingredient ingr) {
        assert ingr == Ingredient.chicken
        [into: { Ingredient otherIngr -> assert otherIngr == Ingredient.sauce}]
    }

    def turn(Integer i) {
        assert i == 1
        [to: { CookingAction cAct -> assert cAct == CookingAction.coat }]
    }

    def marinate(Integer minutes) {
        assert minutes == 30
        [at: { Temperature temp -> assert temp == Temperature.room_temperature }]
    }
}

class Drug {
    String name
    String toString() { name }
}

class DrugQuantity {
    Number q
    String form
    String toString() { "$q $form" }
}

class Duration {
    Number q
    String unit
    String toString() { "$q $unit" }
}

class Email {
    String msg = ""

    def from(address) {
        msg += "from:" + address + ";"
        return this
    }

    def to(address) {
        msg += "to:" + address + ";"
        return this
    }

    def body(text) {
        msg += "body:" + text + ";"
        return this
    }

    def send() {
        return msg
    }
}

