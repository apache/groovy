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
package org.apache.groovy.parser.antlr4

import groovy.transform.AutoFinal
import org.junit.jupiter.api.Test

import static org.apache.groovy.parser.antlr4.TestUtils.expectContains
import static org.apache.groovy.parser.antlr4.TestUtils.expectContainsOnce
import static org.apache.groovy.parser.antlr4.TestUtils.expectParseError

/**
 * Negative parser tests for the Antlr4 parser: a compact covering set
 * distilled from the groovy-parser (Parrot) lab, plus the former
 * SyntaxErrorTest and CommonSyntaxErrorTest corpora inlined as
 * {@code expectParseError} / {@code expectContains} snippets.
 * Each method inlines an invalid snippet and asserts the CONVERSION
 * diagnostic.
 */
@AutoFinal
final class ParserNegativeSyntaxTest {

    @Test
    void 'abstract method in class'() {
        expectParseError '''\
            |class A {
            |    def x()
            |}
            |'''.stripMargin(), '''\
            |You defined a method[x] without a body. Try adding a method body, or declare it abstract @ line 2, column 5.
            |       def x()
            |       ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'script method without body'() {
        expectParseError '''\
            |def w()
            |'''.stripMargin(), '''\
            |You cannot define a method[w] without method body in the script. Try  adding a method body @ line 1, column 1.
            |   def w()
            |   ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'abstract method with body in script'() {
        expectParseError '''\
            |abstract u() {}
            |'''.stripMargin(), '''\
            |You cannot define an abstract method[u] in the script. Try removing the 'abstract' @ line 1, column 1.
            |   abstract u() {}
            |   ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'annotation void method'() {
        expectParseError '''\
            |@interface A {
            |    void a()
            |}
            |'''.stripMargin(), '''\
            |annotation method cannot have void return type @ line 2, column 5.
            |       void a()
            |       ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'annotation type parameters'() {
        expectParseError '''\
            |@interface A<T> {}
            |'''.stripMargin(), '''\
            |annotation declaration cannot have type parameters @ line 1, column 13.
            |   @interface A<T> {}
            |               ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'annotation extends'() {
        expectParseError '''\
            |@interface A extends Object {}
            |'''.stripMargin(), '''\
            |No extends clause allowed for annotation declaration @ line 1, column 14.
            |   @interface A extends Object {}
            |                ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'annotation implements'() {
        expectParseError '''\
            |@interface A implements Serializable {}
            |'''.stripMargin(), '''\
            |No implements clause allowed for annotation declaration @ line 1, column 14.
            |   @interface A implements Serializable {}
            |                ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'annotation method with body'() {
        expectParseError '''\
            |@interface A {
            |    String a() {
            |    }
            |}
            |'''.stripMargin(), '''\
            |Annotation type element should not have body @ line 2, column 5.
            |       String a() {
            |       ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'array missing dimension'() {
        expectParseError '''\
            |def foo = new double[][5]
            |'''.stripMargin(), '''\
            |Unexpected input: '5' @ line 1, column 24.
            |   def foo = new double[][5]
            |                          ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'array no dimensions'() {
        expectParseError '''\
            |def foo = new double[]
            |'''.stripMargin(), '''\
            |Missing '{' @ line 2, column 1.
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'assert assignment'() {
        expectParseError '''\
            |def a = 1
            |assert a = 2
            |'''.stripMargin(), '''\
            |Assignment expression is not allowed in the assert statement @ line 2, column 8.
            |   assert a = 2
            |          ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'anonymous class constructor'() {
        expectParseError '''\
            |class Foo {}
            |new Foo() {
            |    Foo() {}
            |}
            |'''.stripMargin(), '''\
            |Invalid method declaration: Foo @ line 3, column 5.
            |       Foo() {}
            |       ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'compact constructor on class'() {
        expectParseError '''\
            |class Person {
            |    String name
            |    public Person {
            |    }
            |}
            |'''.stripMargin(), '''\
            |Only record can have compact constructor @ line 3, column 12.
            |       public Person {
            |              ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'closure list in parens'() {
        expectParseError '''\
            |def x = (1;2;3)
            |'''.stripMargin(), '''\
            |Unexpected input: ';' @ line 1, column 11.
            |   def x = (1;2;3)
            |             ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'command in array initializer'() {
        expectParseError '''\
            |def a(x, y, z) { 1 }
            |def b = 1
            |def c = 2
            |def d = 3
            |new int[] { a b, c, d }
            |'''.stripMargin(), '''\
            |Command chain expression can not be used in array initializer @ line 5, column 13.
            |   new int[] { a b, c, d }
            |               ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'unclosed type argument'() {
        expectParseError '''\
            |List<Integer list2 = new ArrayList<Integer>()
            |'''.stripMargin(), '''\
            |Unexpected input: 'List<Integer' @ line 1, column 1.
            |   List<Integer list2 = new ArrayList<Integer>()
            |   ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'static final constructor'() {
        expectParseError '''\
            |class Foo { static final Foo() {} }
            |'''.stripMargin(), '''\
            |Constructor has an incorrect modifier 'static'. @ line 1, column 13.
            |   class Foo { static final Foo() {} }
            |               ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'do while without braces'() {
        expectParseError '''\
            |do
            |println 123
            |println 123
            |while (false)
            |'''.stripMargin(), '''\
            |Unexpected input: '123\\nprintln' @ line 3, column 1.
            |   println 123
            |   ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'duplicated named parameter'() {
        expectParseError '''\
            |def m(x) { x }
            |m debit: 30, credit: 40, debit: 50, {}
            |'''.stripMargin(), '''\
            |Duplicated named parameter 'debit' found @ line 2, column 26.
            |   m debit: 30, credit: 40, debit: 50, {}
            |                            ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'int call'() {
        expectParseError '''\
            |int()
            |'''.stripMargin(), '''\
            |Primitive type literal: int cannot be used as a method name @ line 1, column 4.
            |   int()
            |      ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'assign to number'() {
        expectParseError '''\
            |1 = 2
            |'''.stripMargin(), '''\
            |The LHS of an assignment should be a variable or a field accessing expression @ line 1, column 1.
            |   1 = 2
            |   ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'spread in for init'() {
        expectParseError '''\
            |for (*a; a.size() < 10;) {}
            |'''.stripMargin(), '''\
            |spread operator is not allowed here @ line 1, column 6.
            |   for (*a; a.size() < 10;) {}
            |        ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'import in method'() {
        expectParseError '''\
            |def m() {
            |    import java.util.*
            |}
            |'''.stripMargin(), '''\
            |Unexpected input: 'import' @ line 2, column 5.
            |       import java.util.*
            |       ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'interface default parameter'() {
        expectParseError '''\
            |interface Foo {
            |    def doit(String param = "Groovy", int o)
            |}
            |'''.stripMargin(), '''\
            |Cannot specify default value for method parameter 'param = Groovy' inside an interface @ line 2, column 14.
            |       def doit(String param = "Groovy", int o)
            |                ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'interface method with body'() {
        expectParseError '''\
            |interface ITest {
            |    def foo(a, b) {
            |        return a + b
            |    }
            |}
            |'''.stripMargin(), '''\
            |You defined an abstract method[foo] with a body. Try removing the method body, or declare it default or private @ line 2, column 5.
            |       def foo(a, b) {
            |       ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'interface implements'() {
        expectParseError '''\
            |interface I implements Serializable {}
            |'''.stripMargin(), '''\
            |No implements clause allowed for interface declaration @ line 1, column 13.
            |   interface I implements Serializable {}
            |               ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'empty list comma'() {
        expectParseError '''\
            |[,]
            |'''.stripMargin(), '''\
            |Empty list constructor should not contain any comma(,) @ line 1, column 2.
            |   [,]
            |    ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'empty method call arg'() {
        expectParseError '''\
            |m(,)
            |'''.stripMargin(), '''\
            |Expression expected @ line 1, column 3.
            |   m(,)
            |     ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'method in closure'() {
        expectParseError '''\
            |{ ->
            |    def say(String msg) {
            |        println(msg)
            |    }
            |}()
            |'''.stripMargin(), '''\
            |Unexpected input: '(' @ line 2, column 12.
            |       def say(String msg) {
            |              ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'duplicate closure param'() {
        expectParseError '''\
            |def c = {a, a -> println a}
            |'''.stripMargin(), '''\
            |Duplicated parameter 'a' found. @ line 1, column 13.
            |   def c = {a, a -> println a}
            |               ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'duplicate method param'() {
        expectParseError '''\
            |def c(a, a) { println a }
            |'''.stripMargin(), '''\
            |Duplicated parameter 'a' found. @ line 1, column 10.
            |   def c(a, a) { println a }
            |            ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'method declaration without def'() {
        expectParseError '''\
            |foo(String a)
            |'''.stripMargin(), '''\
            |Invalid method declaration @ line 1, column 4.
            |   foo(String a)
            |      ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'duplicate def modifier'() {
        expectParseError '''\
            |def def m() {}
            |'''.stripMargin(), '''\
            |Cannot repeat modifier[def] @ line 1, column 5.
            |   def def m() {}
            |       ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'private public field'() {
        expectParseError '''\
            |class A {
            |    private public a
            |}
            |'''.stripMargin(), '''\
            |Cannot specify modifier[public] when access scope has already been defined @ line 2, column 13.
            |       private public a
            |               ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'volatile method'() {
        expectParseError '''\
            |volatile x() {}
            |'''.stripMargin(), '''\
            |Method has an incorrect modifier 'volatile'. @ line 1, column 1.
            |   volatile x() {}
            |   ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'inner new with named args'() {
        expectParseError '''\
            |class Y {
            |    class X {
            |        def name
            |        X(String name) { this.name = name }
            |    }
            |    static X createX(Y y) {
            |        return y.new X(name: 'Daniel')
            |    }
            |}
            |'''.stripMargin(), '''\
            |Creating instance of non-static class does not support named parameters @ line 7, column 23.
            |           return y.new X(name: 'Daniel')
            |                         ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'integer overflow'() {
        expectParseError '''\
            |2147483648I
            |'''.stripMargin(), '''\
            |Number of value 2147483648 does not fit in the range of int, but int was enforced. @ line 1, column 1.
            |   2147483648I
            |   ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'trailing underscore integer'() {
        expectParseError '''\
            |10101_
            |'''.stripMargin(), '''\
            |Number ending with underscores is invalid @ line 1, column 6.
            |   10101_
            |        ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'unclosed paren'() {
        expectParseError '''\
            |(1 + 2
            |'''.stripMargin(), '''\
            |Missing ')' @ line 1, column 7.
            |   (1 + 2
            |         ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'extra paren'() {
        expectParseError '''\
            |(1 + 2))
            |'''.stripMargin(), '''\
            |Unexpected input: ')' @ line 1, column 8.
            |   (1 + 2))
            |          ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'missing close paren in cast'() {
        expectParseError '''\
            |println ((int 123)
            |'''.stripMargin(), '''\
            |Missing ')' @ line 1, column 15.
            |   println ((int 123)
            |                 ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'missing close bracket in list'() {
        expectParseError '''\
            |[1, 2
            |'''.stripMargin(), '''\
            |Missing ']' @ line 1, column 6.
            |   [1, 2
            |        ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'missing close brace in method'() {
        expectParseError '''\
            |def m() {
            |    println 1
            |'''.stripMargin(), '''\
            |Missing '}' @ line 2, column 14.
            |       println 1
            |                ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'record extends'() {
        expectParseError '''\
            |record Fruit(String name, double price) extends Object {}
            |'''.stripMargin(), '''\
            |No extends clause allowed for record declaration @ line 1, column 41.
            |   record Fruit(String name, double price) extends Object {}
            |                                           ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'class with record header'() {
        expectParseError '''\
            |class Fruit(String name, double price) {}
            |'''.stripMargin(), '''\
            |header declaration is only allowed for record declaration @ line 1, column 12.
            |   class Fruit(String name, double price) {}
            |              ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'record without header'() {
        expectParseError '''\
            |record Fruit {}
            |'''.stripMargin(), '''\
            |header declaration of record is expected @ line 1, column 8.
            |   record Fruit {}
            |          ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'sealed record'() {
        expectParseError '''\
            |sealed record Fruit(String name) {}
            |'''.stripMargin(), '''\
            |`sealed` is not allowed for record declaration @ line 1, column 1.
            |   sealed record Fruit(String name) {}
            |   ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'record instance field'() {
        expectParseError '''\
            |record Fruit(String name) {
            |    String price
            |}
            |'''.stripMargin(), '''\
            |Instance field is not allowed in `record` @ line 2, column 5.
            |       String price
            |       ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'compact constructor name mismatch'() {
        expectParseError '''\
            |record Person(String name, int age) {
            |    public Person123 {
            |    }
            |}
            |'''.stripMargin(), '''\
            |Compact constructor should have the same name as record: Person @ line 2, column 12.
            |       public Person123 {
            |              ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'compact constructor assigns this'() {
        expectParseError '''\
            |record Point(int x, int y, String color) {
            |    public Point {
            |        this.x = -x
            |    }
            |}
            |'''.stripMargin(), '''\
            |Cannot assign a value to final variable 'x' @ line 3, column 14.
            |           this.x = -x
            |                ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'sealed annotation'() {
        expectParseError '''\
            |sealed @interface ShapeI permits Circle, Rectangle {}
            |'''.stripMargin(), '''\
            |modifier `sealed` is not allowed for annotation definition @ line 1, column 1.
            |   sealed @interface ShapeI permits Circle, Rectangle {}
            |   ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'sealed enum'() {
        expectParseError '''\
            |sealed enum ShapeI permits Circle, Rectangle {}
            |'''.stripMargin(), '''\
            |modifier `sealed` is not allowed for enum @ line 1, column 1.
            |   sealed enum ShapeI permits Circle, Rectangle {}
            |   ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'non-sealed interface with permits'() {
        expectParseError '''\
            |non-sealed interface ShapeI permits Circle, Rectangle {}
            |'''.stripMargin(), '''\
            |only sealed type declarations should have `permits` clause @ line 1, column 12.
            |   non-sealed interface ShapeI permits Circle, Rectangle {}
            |              ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'sealed non-sealed interface'() {
        expectParseError '''\
            |sealed non-sealed interface ShapeI permits Circle, Rectangle {}
            |'''.stripMargin(), '''\
            |type cannot be defined with both `sealed` and `non-sealed` @ line 1, column 8.
            |   sealed non-sealed interface ShapeI permits Circle, Rectangle {}
            |          ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'unclosed single quote'() {
        expectParseError '''\
            |def a = 'a
            |        '
            |'''.stripMargin(), '''\
            |Unclosed string literal @ line 1, column 9.
            |   def a = 'a
            |           ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'adjacent triple single quotes'() {
        expectParseError "def v = '''123''''''\n", """\
            |Unclosed string literal @ line 1, column 20.
            |   def v = '''123''''''
            |                      ^
            |
            |1 error
            |""".stripMargin()
    }

    @Test
    void 'super call not first'() {
        expectParseError '''\
            |class A {
            |    A(int a) {
            |        println a
            |        super(123)
            |    }
            |}
            |'''.stripMargin(), '''\
            |super (123) should be the first statement in the constructor[A] @ line 4, column 9.
            |           super(123)
            |           ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'this call not first'() {
        expectParseError '''\
            |class A {
            |    A(int a) {
            |        println a
            |        this()
            |    }
            |    A() {}
            |}
            |'''.stripMargin(), '''\
            |this () should be the first statement in the constructor[A] @ line 4, column 9.
            |           this()
            |           ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'empty arrow case'() {
        expectParseError '''\
            |def a = 6
            |switch (a) {
            |    case 8 ->
            |    case 9 -> 'a'
            |}
            |'''.stripMargin(), '''\
            |`case ... ->` does not support falling through cases @ line 3, column 5.
            |       case 8 ->
            |       ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'mixed arrow and colon'() {
        expectParseError '''\
            |def a = 6
            |switch (a) {
            |    case 6 -> 'a'
            |    case 8 : yield 'b'
            |    default -> 'c'
            |}
            |'''.stripMargin(), '''\
            |`->` and `:` cannot be used together @ line 4, column 12.
            |       case 8 : yield 'b'
            |              ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'switch expr colon without yield'() {
        expectParseError '''\
            |def a = 6
            |def r = switch (a) {
            |    case 6 : 'a'
            |    default : 'c'
            |}
            |'''.stripMargin(), '''\
            |`yield` or `throw` is expected @ line 4, column 15.
            |       default : 'c'
            |                 ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'return in switch expr'() {
        expectParseError '''\
            |def a = 6
            |def r = switch (a) {
            |    case 6 : return 'a'
            |    default : return 'c'
            |}
            |'''.stripMargin(), '''\
            |switch expression does not support `return` @ line 3, column 14.
            |       case 6 : return 'a'
            |                ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'break in switch expr'() {
        expectParseError '''\
            |def a = 7
            |while (a-- > 0) {
            |    def r = switch (a) {
            |        case 6 : break
            |        default : return 'c'
            |    }
            |}
            |'''.stripMargin(), '''\
            |switch expression does not support `break` @ line 4, column 18.
            |           case 6 : break
            |                    ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'declaration in arrow switch expr'() {
        expectParseError '''\
            |def a = 6
            |def r = switch (a) {
            |    case 6 -> def x = 'a'; yield x
            |    default -> throw new RuntimeException('z')
            |}
            |'''.stripMargin(), '''\
            |Expect only 1 statement, but 2 statements found @ line 3, column 15.
            |       case 6 -> def x = 'a'; yield x
            |                 ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'empty switch expression'() {
        expectParseError '''\
            |println switch (10) {}
            |'''.stripMargin(), '''\
            |`case` or `default` branches are expected @ line 1, column 21.
            |   println switch (10) {}
            |                       ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'duplicate default in switch'() {
        expectParseError '''\
            |switch (a) {
            |    case 1:
            |        break
            |    default:
            |        break
            |    default:
            |        break
            |}
            |'''.stripMargin(), '''\
            |a switch must only have one default branch @ line 5, column 9.
            |           break
            |           ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'threadsafe method'() {
        expectParseError '''\
            |threadsafe foo() {}
            |'''.stripMargin(), '''\
            |'threadsafe' is not supported @ line 1, column 1.
            |   threadsafe foo() {}
            |   ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'abstract method with body in trait'() {
        expectParseError '''\
            |trait Trait_01 {
            |    abstract m() {}
            |}
            |'''.stripMargin(), '''\
            |Abstract method should not have method body @ line 2, column 5.
            |       abstract m() {}
            |       ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'vararg not last'() {
        expectParseError '''\
            |def foo(String... strs, int i) { i }
            |'''.stripMargin(), '''\
            |The var-arg parameter strs must be the last parameter @ line 1, column 9.
            |   def foo(String... strs, int i) { i }
            |           ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'var as class name'() {
        expectParseError '''\
            |class var {}
            |'''.stripMargin(), '''\
            |var cannot be used for type declarations @ line 1, column 7.
            |   class var {}
            |         ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'var as return type'() {
        expectParseError '''\
            |var someMethod() {}
            |'''.stripMargin(), '''\
            |val/var cannot be used for method return types @ line 1, column 1.
            |   var someMethod() {}
            |   ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'void field'() {
        expectParseError '''\
            |class MyClass {
            |    void field
            |}
            |'''.stripMargin(), '''\
            |void is not allowed here @ line 2, column 5.
            |       void field
            |       ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'cannot extend multiple classes'() {
        expectParseError '''\
            |class C extends Object, Number {}
            |'''.stripMargin(), '''\
            |Cannot extend multiple classes @ line 1, column 9.
            |   class C extends Object, Number {}
            |           ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'enum type parameters'() {
        expectParseError '''\
            |enum E<T> {}
            |'''.stripMargin(), '''\
            |enum declaration cannot have type parameters @ line 1, column 7.
            |   enum E<T> {}
            |         ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'enum extends'() {
        expectParseError '''\
            |enum E extends Object {}
            |'''.stripMargin(), '''\
            |No extends clause allowed for enum declaration @ line 1, column 8.
            |   enum E extends Object {}
            |          ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'typecast super'() {
        expectParseError '''\
            |class A { def m() {} }
            |class B extends A {}
            |class C extends B {
            |    def m() {
            |        ((A) super).m()
            |    }
            |}
            |'''.stripMargin(), '''\
            |Cannot cast or coerce `super` @ line 5, column 10.
            |           ((A) super).m()
            |            ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'illegal dollar in gstring'() {
        expectParseError '''\
            |def Target = "releases$"
            |'''.stripMargin(), '''\
            |Illegal string body character after dollar sign: '"' @ line 1, column 24.
            |   def Target = "releases$"
            |                          ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'zero width space identifier'() {
        expectParseError '''\
            |def \u200Bname = null
            |'''.stripMargin(), '''\
            |Unexpected character: '\\u200b' @ line 1, column 5.
            |   def \u200Bname = null
            |       ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'paren bracket mismatch'() {
        expectParseError '''\
            |(1 + 2]
            |'''.stripMargin(), '''\
            |Missing ')' @ line 1, column 7.
            |   (1 + 2]
            |         ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'missing close paren in method header'() {
        expectParseError '''\
            |def m( {
            |}
            |'''.stripMargin(), '''\
            |Missing ')' @ line 1, column 8.
            |   def m( {
            |          ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'groovy core - List'() {
        expectParseError('''\
            |[,]
            |'''.stripMargin(), '''\
            |Empty list constructor should not contain any comma(,) @ line 1, column 2.
            |   [,]
            |    ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'groovy core - Expression'() {
        expectParseError('''\
            |int()
            |'''.stripMargin(), '''\
            |Primitive type literal: int cannot be used as a method name @ line 1, column 4.
            |   int()
            |      ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |1 = 2
            |'''.stripMargin(), '''\
            |The LHS of an assignment should be a variable or a field accessing expression @ line 1, column 1.
            |   1 = 2
            |   ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |m() = 2
            |'''.stripMargin(), '''\
            |The LHS of an assignment should be a variable or a field accessing expression @ line 1, column 1.
            |   m() = 2
            |   ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |[1, 2] = 2
            |'''.stripMargin(), '''\
            |The LHS of an assignment should be a variable or a field accessing expression @ line 1, column 1.
            |   [1, 2] = 2
            |   ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |[a: 1, b: 2] = 2
            |'''.stripMargin(), '''\
            |The LHS of an assignment should be a variable or a field accessing expression @ line 1, column 1.
            |   [a: 1, b: 2] = 2
            |   ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |"$x" = 2
            |'''.stripMargin(), '''\
            |The LHS of an assignment should be a variable or a field accessing expression @ line 1, column 1.
            |   "$x" = 2
            |   ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |'x' = 2
            |'''.stripMargin(), '''\
            |The LHS of an assignment should be a variable or a field accessing expression @ line 1, column 1.
            |   'x' = 2
            |   ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'groovy core - CommandExpression'() {
        expectParseError('''\
            |def a(x, y, z) { 1 }
            |def b = 1
            |def c = 2
            |def d = 3
            |new int[] { a b, c, d }
            |
            |'''.stripMargin(), '''\
            |Command chain expression can not be used in array initializer @ line 5, column 13.
            |   new int[] { a b, c, d }
            |               ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'groovy core - Switch'() {
        expectParseError('''\
            |switch (a) {
            |    case 1:
            |        break;
            |    default:
            |        break;
            |    default:
            |        break;
            |}
            |
            |
            |'''.stripMargin(), '''\
            |a switch must only have one default branch @ line 5, column 9.
            |           break;
            |           ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'groovy core - UnexpectedCharacter 1'() {
        expectParseError('''\
            |\u001b
            |'''.stripMargin(), [
            'Unexpected character: \'\\u001b\' @ line 1, column 1.',
            '   \u001b',
            '   ^',
            '',
            '1 error'
            ].join('\n'))
    }

    @Test
    void 'groovy core - UnexpectedCharacter 2'() {
        expectParseError '''\
            |def \u200Bname = null
            |'''.stripMargin(), '''\
            |Unexpected character: '\\u200b' @ line 1, column 5.
            |   def \u200Bname = null
            |       ^
            |
            |1 error
            |'''.stripMargin()

        expectParseError '''\
            |def na\u200Bme = null
            |'''.stripMargin(), '''\
            |Unexpected character: '\\u200b' @ line 1, column 7.
            |   def na\u200Bme = null
            |         ^
            |
            |1 error
            |'''.stripMargin()

        expectParseError '''\
            |def na\u000Cme = null
            |'''.stripMargin(), '''\
            |Unexpected character: '\\f' @ line 1, column 7.
            |   def na\u000Cme = null
            |         ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'groovy core - UnexpectedCharacter 3'() {
        expectParseError '''\
            |foo.bar {
            |  println 'Hello
            |}
            |'''.stripMargin(), '''\
            |Unclosed string literal @ line 2, column 11.
            |     println 'Hello
            |             ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'groovy core - GString illegal character after dollar'() {
        // Trailing bare `$` in double-quoted and triple-double-quoted strings.
        // Must name the dollar (Groovy 2 message) rather than only the closing quote.
        expectParseError '''\
            |def Target = "releases$"
            |'''.stripMargin(), '''\
            |Illegal string body character after dollar sign: '"' @ line 1, column 24.
            |   def Target = "releases$"
            |                          ^
            |
            |1 error
            |'''.stripMargin()

        expectParseError '''\
            |def Target = """releases$"""
            |'''.stripMargin(), '''\
            |Illegal string body character after dollar sign: '"' @ line 1, column 26.
            |   def Target = """releases$"""
            |                            ^
            |
            |1 error
            |'''.stripMargin()

        expectParseError '''\
            |def x = "$"
            |'''.stripMargin(), '''\
            |Illegal string body character after dollar sign: '"' @ line 1, column 11.
            |   def x = "$"
            |             ^
            |
            |1 error
            |'''.stripMargin()

        expectParseError '''\
            |def x = "a$ b"
            |'''.stripMargin(), '''\
            |Illegal string body character after dollar sign: ' ' @ line 1, column 12.
            |   def x = "a$ b"
            |              ^
            |
            |1 error
            |'''.stripMargin()

        // newline immediately after `$` (unclosed GString line)
        expectParseError '''\
            |def x = "hello$
            |'''.stripMargin(), '''\
            |Illegal string body character after dollar sign: '\\n' @ line 1, column 16.
            |   def x = "hello$
            |                  ^
            |
            |1 error
            |'''.stripMargin()

        // true EOF immediately after `$` — no character to display in the message
        expectParseError 'def x = "hello$', '''\
            |Illegal string body character after dollar sign @ line 1, column 16.
            |   def x = "hello$
            |                  ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'groovy core - ParExpression'() {
        expectParseError('''\
            |(1 + 2
            |'''.stripMargin(), '''\
            |Missing ')' @ line 1, column 7.
            |   (1 + 2
            |         ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |(1 + 2))
            |'''.stripMargin(), '''\
            |Unexpected input: ')' @ line 1, column 8.
            |   (1 + 2))
            |          ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |(1 + 2]
            |'''.stripMargin(), '''\
            |Missing ')' @ line 1, column 7.
            |   (1 + 2]
            |         ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'groovy core - Parentheses'() {
        expectParseError('''\
            |def a( {
            |}
            |'''.stripMargin(), '''\
            |Missing ')' @ line 1, column 8.
            |   def a( {
            |          ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'groovy core - This'() {
        expectParseError('''\
            |class A {
            |    A(int a) {
            |        println a
            |        this()
            |    }
            |
            |    A() {}
            |}
            |'''.stripMargin(), '''\
            |this () should be the first statement in the constructor[A] @ line 4, column 9.
            |           this()
            |           ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'groovy core - Super'() {
        expectParseError('''\
            |class A {
            |    A(int a) {
            |        println a
            |        super(123)
            |    }
            |}
            |'''.stripMargin(), '''\
            |super (123) should be the first statement in the constructor[A] @ line 4, column 9.
            |           super(123)
            |           ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |class A {
            |    A() {
            |        (Object) super
            |    }
            |}
            |
            |'''.stripMargin(), '''\
            |Cannot cast or coerce `super` @ line 3, column 9.
            |           (Object) super
            |           ^
            |
            |1 error'''.stripMargin())
    }

    // GROOVY-9391
    @Test
    void 'groovy core - Typecast super'() {
        expectParseError '''\
            |class A { def m() {} }
            |class B extends A {  }
            |class C extends B {
            |    def m() {
            |        ((A) super).m()
            |    }
            |}
            |'''.stripMargin(), '''\
            |Cannot cast or coerce `super` @ line 5, column 10.
            |           ((A) super).m()
            |            ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'groovy core - AbstractMethod'() {
        expectParseError('''\
            |class A {
            |    def x()
            |}
            |'''.stripMargin(), '''\
            |You defined a method[x] without a body. Try adding a method body, or declare it abstract @ line 2, column 5.
            |       def x()
            |       ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |enum E {
            |    A, B
            |    def y()
            |}
            |'''.stripMargin(), '''\
            |You defined a method[y] without a body. Try adding a method body, or declare it abstract @ line 3, column 5.
            |       def y()
            |       ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |trait B {
            |    def z()
            |}
            |'''.stripMargin(), '''\
            |You defined a method[z] without a body. Try adding a method body, or declare it abstract @ line 2, column 5.
            |       def z()
            |       ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |def w()
            |'''.stripMargin(), '''\
            |You cannot define a method[w] without method body in the script. Try  adding a method body @ line 1, column 1.
            |   def w()
            |   ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |abstract v()
            |'''.stripMargin(), '''\
            |You cannot define an abstract method[v] without method body in the script. Try removing the 'abstract' and adding a method body @ line 1, column 1.
            |   abstract v()
            |   ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |abstract u() {}
            |'''.stripMargin(), '''\
            |You cannot define an abstract method[u] in the script. Try removing the 'abstract' @ line 1, column 1.
            |   abstract u() {}
            |   ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'groovy core - GROOVY-8150'() {
        expectParseError('''\
            |def a
            |def b = [1]
            |((a)) = b
            |'''.stripMargin(), '''\
            |Nested parenthesis is not allowed in multiple assignment, e.g. ((a)) = b @ line 3, column 1.
            |   ((a)) = b
            |   ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |​println 09​
            |'''.stripMargin(), [
            'Unexpected character: \'\\u200b\' @ line 1, column 1.',
            '   ​println 09​',
            '   ^',
            '',
            '1 error'
            ].join('\n'))
    }

    @Test
    void 'groovy core - DoWhile'() {
        expectParseError('''\
            |do
            |println 123
            |println 123
            |while(false)
            |'''.stripMargin(), [
            'Unexpected input: \'123\\nprintln\' @ line 3, column 1.',
            '   println 123',
            '   ^',
            '',
            '1 error'
            ].join('\n'))
    }

    @Test
    void 'groovy core - For'() {
        expectParseError('''\
            |for (*a; a.size() < 10;) {}
            |'''.stripMargin(), '''\
            |spread operator is not allowed here @ line 1, column 6.
            |   for (*a; a.size() < 10;) {}
            |        ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |for (; a.size() < 10; *a) {}
            |'''.stripMargin(), '''\
            |spread operator is not allowed here @ line 1, column 23.
            |   for (; a.size() < 10; *a) {}
            |                         ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'groovy core - Modifier'() {
        expectParseError('''\
            |def def m() {}
            |'''.stripMargin(), '''\
            |Cannot repeat modifier[def] @ line 1, column 5.
            |   def def m() {}
            |       ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |public public class A {}
            |'''.stripMargin(), '''\
            |Cannot repeat modifier[public] @ line 1, column 8.
            |   public public class A {}
            |          ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |final final int a = 1;
            |'''.stripMargin(), '''\
            |Cannot repeat modifier[final] @ line 1, column 7.
            |   final final int a = 1;
            |         ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |class A {
            |    private public a
            |}
            |'''.stripMargin(), '''\
            |Cannot specify modifier[public] when access scope has already been defined @ line 2, column 13.
            |       private public a
            |               ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |class A {
            |    protected public a
            |}
            |'''.stripMargin(), '''\
            |Cannot specify modifier[public] when access scope has already been defined @ line 2, column 15.
            |       protected public a
            |                 ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |volatile x() {}
            |'''.stripMargin(), '''\
            |Method has an incorrect modifier 'volatile'. @ line 1, column 1.
            |   volatile x() {}
            |   ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'groovy core - ClassDeclaration 1'() {
        expectParseError('''\
            |class Foo {}
            |new Foo() {
            |    Foo() {}
            |}
            |
            |'''.stripMargin(), '''\
            |Invalid method declaration: Foo @ line 3, column 5.
            |       Foo() {}
            |       ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'groovy core - ClassDeclaration 2'() {
        expectParseError('''\
            |package fail
            |
            |class Person {
            |    String name
            |    int age
            |
            |    public Person {
            |        if (name == 'Devil') throw new IllegalArgumentException("Invalid person: $name")
            |        if (age < 18) throw new IllegalArgumentException("Invalid age: $age")
            |    }
            |}
            |
            |'''.stripMargin(), '''\
            |Only record can have compact constructor @ line 7, column 12.
            |       public Person {
            |              ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'groovy core - ClassDeclaration 3'() {
        expectParseError '''\
            |class C extends Object, Number {}
            |'''.stripMargin(), '''\
            |Cannot extend multiple classes @ line 1, column 9.
            |   class C extends Object, Number {}
            |           ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'groovy core - ClassDeclaration 4'() {
        expectParseError('''\
            |package fail
            |
            |class Person {
            |    Person {
            |        // Only record can have compact constructor
            |    }
            |}
            |
            |'''.stripMargin(), '''\
            |Only record can have compact constructor @ line 4, column 5.
            |       Person {
            |       ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'groovy core - EnumDeclaration 1'() {
        expectParseError '''\
            |enum E<T> {}
            |'''.stripMargin(), '''\
            |enum declaration cannot have type parameters @ line 1, column 7.
            |   enum E<T> {}
            |         ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'groovy core - EnumDeclaration 2'() {
        expectParseError '''\
            |enum E extends Object {}
            |'''.stripMargin(), '''\
            |No extends clause allowed for enum declaration @ line 1, column 8.
            |   enum E extends Object {}
            |          ^
            |
            |1 error
            |'''.stripMargin()
    }

    // GROOVY-4438, GROOVY-7773, GROOVY-8507, GROOVY-9301, GROOVY-9306
    @Test
    void 'groovy core - EnumDeclaration 3'() {
        expectParseError '''\
            |enum E {
            |  X, Y,
            |  def z() { }
            |}
            |'''.stripMargin(), '''\
            |Unexpected input: ',\\n  def' @ line 3, column 3.
            |     def z() { }
            |     ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'groovy core - AnnotationDeclaration 1'() {
        expectParseError '''\
            |@interface A {
            |    void a()
            |}
            |'''.stripMargin(), '''\
            |annotation method cannot have void return type @ line 2, column 5.
            |       void a()
            |       ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'groovy core - AnnotationDeclaration 2'() {
        expectParseError '''\
            |@interface A<T> {}
            |'''.stripMargin(), '''\
            |annotation declaration cannot have type parameters @ line 1, column 13.
            |   @interface A<T> {}
            |               ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'groovy core - AnnotationDeclaration 3'() {
        expectParseError '''\
            |@interface A extends Object {}
            |'''.stripMargin(), '''\
            |No extends clause allowed for annotation declaration @ line 1, column 14.
            |   @interface A extends Object {}
            |                ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'groovy core - AnnotationDeclaration 4'() {
        expectParseError '''\
            |@interface A implements Serializable {}
            |'''.stripMargin(), '''\
            |No implements clause allowed for annotation declaration @ line 1, column 14.
            |   @interface A implements Serializable {}
            |                ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'groovy core - AnnotationDeclaration 5'() {
        expectParseError '''\
            |@interface A {
            |    String a() {
            |    }
            |}'''.stripMargin(), '''\
            |Annotation type element should not have body @ line 2, column 5.
            |       String a() {
            |       ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'groovy core - SealedTypeDeclaration'() {
        expectParseError('''\
            |sealed @interface ShapeI permits Circle, Rectangle { }
            |
            |
            |'''.stripMargin(), '''\
            |modifier `sealed` is not allowed for annotation definition @ line 1, column 1.
            |   sealed @interface ShapeI permits Circle, Rectangle { }
            |   ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |sealed enum ShapeI permits Circle, Rectangle { }
            |
            |'''.stripMargin(), '''\
            |modifier `sealed` is not allowed for enum @ line 1, column 1.
            |   sealed enum ShapeI permits Circle, Rectangle { }
            |   ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |non-sealed @interface ShapeI { }
            |
            |
            |'''.stripMargin(), '''\
            |modifier `non-sealed` is not allowed for annotation definition @ line 1, column 1.
            |   non-sealed @interface ShapeI { }
            |   ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |non-sealed enum ShapeI { }
            |
            |
            |'''.stripMargin(), '''\
            |modifier `non-sealed` is not allowed for enum @ line 1, column 1.
            |   non-sealed enum ShapeI { }
            |   ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |non-sealed interface ShapeI permits Circle, Rectangle { }
            |
            |
            |'''.stripMargin(), '''\
            |only sealed type declarations should have `permits` clause @ line 1, column 12.
            |   non-sealed interface ShapeI permits Circle, Rectangle { }
            |              ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |sealed non-sealed interface ShapeI permits Circle, Rectangle { }
            |
            |
            |'''.stripMargin(), '''\
            |type cannot be defined with both `sealed` and `non-sealed` @ line 1, column 8.
            |   sealed non-sealed interface ShapeI permits Circle, Rectangle { }
            |          ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |sealed final interface ShapeI permits Circle, Rectangle { }
            |
            |
            |'''.stripMargin(), '''\
            |type cannot be defined with both `sealed` and `final` @ line 1, column 8.
            |   sealed final interface ShapeI permits Circle, Rectangle { }
            |          ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |non-sealed final interface ShapeI permits Circle, Rectangle { }
            |
            |
            |'''.stripMargin(), '''\
            |type cannot be defined with both `non-sealed` and `final` @ line 1, column 12.
            |   non-sealed final interface ShapeI permits Circle, Rectangle { }
            |              ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'groovy core - MethodDeclaration'() {
        expectParseError('''\
            |{ ->
            |    def say(String msg) {
            |        println(msg)
            |    }
            |}()
            |
            |'''.stripMargin(), '''\
            |Unexpected input: '(' @ line 2, column 12.
            |       def say(String msg) {
            |              ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |def c = {a, a -> println a}
            |c(1, 2)
            |
            |'''.stripMargin(), '''\
            |Duplicated parameter 'a' found. @ line 1, column 13.
            |   def c = {a, a -> println a}
            |               ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |def c(a, a) { println a}
            |c(1, 2)
            |
            |'''.stripMargin(), '''\
            |Duplicated parameter 'a' found. @ line 1, column 10.
            |   def c(a, a) { println a}
            |            ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |foo(String a)
            |
            |'''.stripMargin(), '''\
            |Invalid method declaration @ line 1, column 4.
            |   foo(String a)
            |      ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |foo(int a)
            |
            |'''.stripMargin(), '''\
            |Invalid method declaration @ line 1, column 4.
            |   foo(int a)
            |      ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'groovy core - ConstructorDeclaration'() {
        expectParseError('''\
            |class Foo { static final Foo() {}}
            |'''.stripMargin(), '''\
            |Constructor has an incorrect modifier 'static'. @ line 1, column 13.
            |   class Foo { static final Foo() {}}
            |               ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'groovy core - ClosureListExpression'() {
        expectParseError('''\
            |[].for(1;2;3){println "in loop"}
            |'''.stripMargin(), '''\
            |Unexpected input: '(' @ line 1, column 7.
            |   [].for(1;2;3){println "in loop"}
            |         ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |def x = (1;2;3)
            |'''.stripMargin(), '''\
            |Unexpected input: ';' @ line 1, column 11.
            |   def x = (1;2;3)
            |             ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |[].bar(1;2;3)
            |'''.stripMargin(), '''\
            |Unexpected input: '(' @ line 1, column 7.
            |   [].bar(1;2;3)
            |         ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |class Crasher {
            |    public void m() {
            |        def fields = [1,2,3]
            |        def expectedFieldNames = ["patentnumber", "status"].
            |                for (int i=0; i<fields.size(); i++) {
            |                    Object f = fields[i]
            |                    System.out.println(f);
            |                }
            |    }
            |}
            |'''.stripMargin(), '''\
            |Missing ')' @ line 5, column 26.
            |                   for (int i=0; i<fields.size(); i++) {
            |                            ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'groovy core - InterfaceDeclaration 1'() {
        expectParseError '''\
            |interface Foo {
            |    def doit( String param = "Groovy", int o )
            |}
            |'''.stripMargin(), '''\
            |Cannot specify default value for method parameter 'param = Groovy' inside an interface @ line 2, column 15.
            |       def doit( String param = "Groovy", int o )
            |                 ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'groovy core - InterfaceDeclaration 2'() {
        expectParseError '''\
            |interface I implements Serializable {}
            |'''.stripMargin(), '''\
            |No implements clause allowed for interface declaration @ line 1, column 13.
            |   interface I implements Serializable {}
            |               ^
            |
            |1 error
            |'''.stripMargin()
    }

    // GROOVY-11208
    @Test
    void 'groovy core - InterfaceDeclaration 3'() {
        expectParseError '''\
            |interface I {
            |    def m() default {1}
            |}
            |'''.stripMargin(), '''\
            |Unexpected input: 'default' @ line 2, column 13.
            |       def m() default {1}
            |               ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'groovy core - void'() {
        expectParseError('''\
            |class MyClass {
            |    void field
            |}
            |
            |'''.stripMargin(), '''\
            |void is not allowed here @ line 2, column 5.
            |       void field
            |       ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |class MyClass {
            |    def foo() {
            |        void bar = null
            |    }
            |}
            |
            |'''.stripMargin(), '''\
            |void is not allowed here @ line 3, column 9.
            |           void bar = null
            |           ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'groovy core - Assert'() {
        expectParseError('''\
            |def a = 1
            |assert a = 2
            |
            |'''.stripMargin(), '''\
            |Assignment expression is not allowed in the assert statement @ line 2, column 8.
            |   assert a = 2
            |          ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'groovy core - DuplicatedNamedParameter'() {
        expectParseError('''\
            |def closure = { println it }
            |closure debit: 30, credit: 40, debit: 50, {}
            |
            |'''.stripMargin(), '''\
            |Duplicated named parameter 'debit' found @ line 2, column 32.
            |   closure debit: 30, credit: 40, debit: 50, {}
            |                                  ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |def m(x) { println x }
            |m debit: 30, credit: 40, debit: 50, {}
            |
            |'''.stripMargin(), '''\
            |Duplicated named parameter 'debit' found @ line 2, column 26.
            |   m debit: 30, credit: 40, debit: 50, {}
            |                            ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'groovy core - threadsafe'() {
        expectParseError('''\
            |threadsafe foo() {}
            |
            |'''.stripMargin(), '''\
            |'threadsafe' is not supported @ line 1, column 1.
            |   threadsafe foo() {}
            |   ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'groovy core - VarArgParameter'() {
        expectParseError('''\
            |def foo(String... strs, int i) { println i }
            |
            |foo("me", "you", 42)
            |
            |'''.stripMargin(), '''\
            |The var-arg parameter strs must be the last parameter @ line 1, column 9.
            |   def foo(String... strs, int i) { println i }
            |           ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'groovy core - Number'() {
        expectParseError('''\
            |2147483648I
            |
            |'''.stripMargin(), '''\
            |Number of value 2147483648 does not fit in the range of int, but int was enforced. @ line 1, column 1.
            |   2147483648I
            |   ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |9223372036854775808L
            |
            |'''.stripMargin(), '''\
            |Number of value 9223372036854775808 does not fit in the range of long, but long was enforced. @ line 1, column 1.
            |   9223372036854775808L
            |   ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |10101_
            |
            |'''.stripMargin(), '''\
            |Number ending with underscores is invalid @ line 1, column 6.
            |   10101_
            |        ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |10101_.0
            |
            |'''.stripMargin(), '''\
            |Number ending with underscores is invalid @ line 1, column 6.
            |   10101_.0
            |        ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |10101.0_
            |
            |'''.stripMargin(), '''\
            |Number ending with underscores is invalid @ line 1, column 8.
            |   10101.0_
            |          ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'groovy core - MethodCall'() {
        expectParseError('''\
            |m(,)
            |'''.stripMargin(), '''\
            |Expression expected @ line 1, column 3.
            |   m(,)
            |     ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'groovy core - val'() {
        expectParseError('''\
            |class val {}
            |
            |'''.stripMargin(), '''\
            |val cannot be used for type declarations @ line 1, column 7.
            |   class val {}
            |         ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |val someMethod() {}
            |
            |'''.stripMargin(), '''\
            |val/var cannot be used for method return types @ line 1, column 1.
            |   val someMethod() {}
            |   ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'groovy core - var'() {
        expectParseError('''\
            |class var {}
            |
            |'''.stripMargin(), '''\
            |var cannot be used for type declarations @ line 1, column 7.
            |   class var {}
            |         ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |var someMethod() {}
            |
            |'''.stripMargin(), '''\
            |val/var cannot be used for method return types @ line 1, column 1.
            |   var someMethod() {}
            |   ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'groovy core - String'() {
        expectParseError([
            'def v = \'\'\'123\'\'\'\'\'\''
            ].join('\n') + '\n', [
            'Unclosed string literal @ line 1, column 20.',
            '   def v = \'\'\'123\'\'\'\'\'\'',
            '                      ^',
            '',
            '1 error'
            ].join('\n'))
        expectParseError('''\
            |def v2 = """123""""""
            |'''.stripMargin(), '''\
            |Unclosed string literal @ line 1, column 21.
            |   def v2 = """123""""""
            |                       ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |def a = 'a
            |        '
            |'''.stripMargin(), '''\
            |Unclosed string literal @ line 1, column 9.
            |   def a = 'a
            |           ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |def a = "a
            |        "
            |'''.stripMargin(), '''\
            |Unclosed string literal @ line 1, column 9.
            |   def a = "a
            |           ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'groovy core - NonStaticClass'() {
        expectParseError('''\
            |public class Y {
            |    public class X {
            |        def name
            |
            |        public X(String name) {
            |            this.name = name
            |        }
            |    }
            |
            |    public static X createX(Y y) {
            |        return y.new X(name:'Daniel')
            |    }
            |}
            |assert 'Daniel' == Y.createX(new Y()).name
            |
            |'''.stripMargin(), '''\
            |Creating instance of non-static class does not support named parameters @ line 11, column 23.
            |           return y.new X(name:'Daniel')
            |                         ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'groovy core - Import'() {
        expectParseError('''\
            |{
            |    import java.util.*
            |}
            |
            |'''.stripMargin(), [
            'Unexpected input: \'{\\n    import\' @ line 2, column 5.',
            '       import java.util.*',
            '       ^',
            '',
            '1 error'
            ].join('\n'))
        expectParseError('''\
            |def m() {
            |    import java.util.*
            |}
            |
            |'''.stripMargin(), '''\
            |Unexpected input: 'import' @ line 2, column 5.
            |       import java.util.*
            |       ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'groovy core - Trait'() {
        expectParseError('''\
            |package fail
            |
            |trait Trait_01 {
            |    abstract m() {}
            |}
            |
            |'''.stripMargin(), '''\
            |Abstract method should not have method body @ line 4, column 5.
            |       abstract m() {}
            |       ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'groovy core - Record'() {
        expectParseError('''\
            |package core
            |
            |// can't explicitly extend a class
            |record Fruit(String name, double price) extends Object {}
            |
            |'''.stripMargin(), '''\
            |No extends clause allowed for record declaration @ line 4, column 41.
            |   record Fruit(String name, double price) extends Object {}
            |                                           ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |package core
            |
            |class Fruit(String name, double price) {}
            |
            |'''.stripMargin(), '''\
            |header declaration is only allowed for record declaration @ line 3, column 12.
            |   class Fruit(String name, double price) {}
            |              ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |package core
            |
            |record Fruit {}
            |
            |'''.stripMargin(), '''\
            |header declaration of record is expected @ line 3, column 8.
            |   record Fruit {}
            |          ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |package core
            |
            |sealed record Fruit(String name) {}
            |
            |'''.stripMargin(), '''\
            |`sealed` is not allowed for record declaration @ line 3, column 1.
            |   sealed record Fruit(String name) {}
            |   ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |package core
            |
            |non-sealed record Fruit(String name) {}
            |
            |'''.stripMargin(), '''\
            |`non-sealed` is not allowed for record declaration @ line 3, column 1.
            |   non-sealed record Fruit(String name) {}
            |   ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |package core
            |
            |record Fruit(String name) {
            |    private String price
            |}
            |
            |'''.stripMargin(), '''\
            |Instance field is not allowed in `record` @ line 4, column 5.
            |       private String price
            |       ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |package core
            |
            |record Fruit(String name) {
            |    String price
            |}
            |
            |'''.stripMargin(), '''\
            |Instance field is not allowed in `record` @ line 4, column 5.
            |       String price
            |       ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |package core
            |
            |record Person(String name, int age) {
            |    public Person123 {
            |        if (name == 'Devil') throw new IllegalArgumentException("Invalid person: $name")
            |        if (age < 18) throw new IllegalArgumentException("Invalid age: $age")
            |    }
            |}
            |
            |'''.stripMargin(), '''\
            |Compact constructor should have the same name as record: Person @ line 4, column 12.
            |       public Person123 {
            |              ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |package core
            |
            |record Point(int x, int y, String color) {
            |    public Point {
            |        this.x = -x;
            |    }
            |}
            |
            |'''.stripMargin(), '''\
            |Cannot assign a value to final variable 'x' @ line 5, column 14.
            |           this.x = -x;
            |                ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |package core
            |
            |// C-style array declarations not allowed in record components as per JLS
            |record R1(int x[]) { }
            |
            |'''.stripMargin(), '''\
            |Invalid method declaration @ line 4, column 10.
            |   record R1(int x[]) { }
            |            ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |package core
            |
            |import java.util.function.BiFunction
            |
            |// cannot declare instance variables (non-static fields) in a record class.
            |record Rectangle(double length, double width) {
            |    BiFunction<Double, Double, Double> diagonal = (x, y) -> Math.sqrt(x*x + y*y)
            |}
            |
            |'''.stripMargin(), '''\
            |Instance field is not allowed in `record` @ line 7, column 5.
            |       BiFunction<Double, Double, Double> diagonal = (x, y) -> Math.sqrt(x*x + y*y)
            |       ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |package core
            |
            |import java.util.function.BiFunction
            |
            |// cannot declare instance initializers in a record class.
            |record Rectangle(double length, double width) {
            |    {
            |        BiFunction<Double, Double, Double> diagonal = (x, y) -> Math.sqrt(x*x + y*y)
            |    }
            |}
            |
            |'''.stripMargin(), '''\
            |Instance initializer is not allowed in `record` @ line 7, column 5.
            |       {
            |       ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |package fail
            |
            |record Person(String name, int age) {
            |    Person2 {
            |        // Compact constructor should have the same name as record
            |    }
            |}
            |
            |'''.stripMargin(), '''\
            |Compact constructor should have the same name as record: Person @ line 4, column 5.
            |       Person2 {
            |       ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |package fail
            |
            |record Person(String name, int age) {
            |    Person {
            |        // Cannot assign a value to final variable
            |        this.age = 40
            |    }
            |}
            |
            |'''.stripMargin(), '''\
            |Cannot assign a value to final variable 'age' @ line 6, column 14.
            |           this.age = 40
            |                ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'groovy core - Array'() {
        expectParseError('''\
            |def foo = new double[][5]
            |
            |'''.stripMargin(), '''\
            |Unexpected input: '5' @ line 1, column 24.
            |   def foo = new double[][5]
            |                          ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |def foo = new double[]
            |'''.stripMargin(), '''\
            |Missing '{' @ line 2, column 1.
            |1 error'''.stripMargin())
        expectParseError('''\
            |def foo = new double[2] { 1.0, 2.0 }
            |
            |'''.stripMargin(), '''\
            |Unexpected input: '{' @ line 1, column 25.
            |   def foo = new double[2] { 1.0, 2.0 }
            |                           ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'groovy core - SwitchExpression'() {
        expectParseError('''\
            |// fails: an arrow label must have a body; `case 8 ->` is empty
            |def a = 6
            |switch(a) {
            |    case 8 ->
            |    case 9 -> 'a'
            |}
            |
            |'''.stripMargin(), '''\
            |`case ... ->` does not support falling through cases @ line 4, column 5.
            |       case 8 ->
            |       ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |// fails: an arrow label must have a body; `case 8 ->` is empty
            |def a = 6
            |switch(a) {
            |    case 8 ->
            |    default -> 'b'
            |}
            |
            |'''.stripMargin(), '''\
            |`case ... ->` does not support falling through cases @ line 4, column 5.
            |       case 8 ->
            |       ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |// fails: consecutive empty arrow labels; each `->` needs its own body
            |def a = 6
            |switch(a) {
            |    case 6 ->
            |    case 8 ->
            |    default -> 'b'
            |}
            |
            |'''.stripMargin(), '''\
            |`case ... ->` does not support falling through cases @ line 4, column 5.
            |       case 6 ->
            |       ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |// fails: mixing arrow (`->`) and colon (`:`) labels in one switch is illegal
            |def a = 6
            |switch(a) {
            |    case 6 -> 'a'
            |    case 8 : yield 'b'
            |    default -> 'c'
            |}
            |
            |'''.stripMargin(), '''\
            |`->` and `:` cannot be used together @ line 5, column 12.
            |       case 8 : yield 'b'
            |              ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |// fails: a colon arm must `yield` or `throw`; a lone expression statement is not a result
            |def a = 6
            |def r = switch(a) {
            |    case 6 : 'a'
            |    default : 'c'
            |}
            |
            |'''.stripMargin(), '''\
            |`yield` or `throw` is expected @ line 5, column 15.
            |       default : 'c'
            |                 ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |// fails: `return` is not allowed inside a switch expression (use `yield`)
            |def a = 6
            |def r = switch(a) {
            |    case 6 : return 'a'
            |    default : return 'c'
            |}
            |
            |'''.stripMargin(), '''\
            |switch expression does not support `return` @ line 4, column 14.
            |       case 6 : return 'a'
            |                ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |// fails: `break` cannot leave a switch expression (and `return` is also illegal there)
            |def a = 7
            |while (a-- > 0) {
            |    def r = switch(a) {
            |        case 6 : break
            |        default : return 'c'
            |    }
            |}
            |
            |'''.stripMargin(), '''\
            |switch expression does not support `break` @ line 5, column 18.
            |           case 6 : break
            |                    ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |// fails: `continue` cannot jump out of a switch expression
            |def a = 7
            |while (a-- > 0) {
            |    def r = switch(a) {
            |        case 6 : continue
            |        default : return 'c'
            |    }
            |}
            |
            |'''.stripMargin(), '''\
            |switch expression does not support `continue` @ line 5, column 18.
            |           case 6 : continue
            |                    ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |// fails: a multi-statement arrow arm must be a block `{ ... }`
            |def a = 6
            |def r = switch(a) {
            |    case 6 -> def x = 'a'; yield x
            |    default -> throw new RuntimeException('z')
            |}
            |
            |'''.stripMargin(), '''\
            |Expect only 1 statement, but 2 statements found @ line 4, column 15.
            |       case 6 -> def x = 'a'; yield x
            |                 ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |// fails: a switch expression must have at least one arm
            |println switch (10) {}
            |
            |'''.stripMargin(), '''\
            |`case` or `default` branches are expected @ line 2, column 21.
            |   println switch (10) {}
            |                       ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |// fails: an arrow block must complete with `yield` or `throw` on every path
            |def a = 1
            |def r = switch (a) {
            |    case 1 -> {
            |        if (true) {
            |            println 'no yield'
            |        }
            |    }
            |    default -> 0
            |}
            |
            |'''.stripMargin(), '''\
            |`yield` or `throw` is expected @ line 5, column 9.
            |           if (true) {
            |           ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |// fails: `if` without `else` can complete normally, so the arrow block does not always yield
            |def a = 1
            |def r = switch (a) {
            |    case 1 -> {
            |        if (true) {
            |            yield 1
            |        }
            |    }
            |    default -> 0
            |}
            |
            |'''.stripMargin(), '''\
            |`yield` or `throw` is expected @ line 5, column 9.
            |           if (true) {
            |           ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |// fails: `return` inside a loop still leaves the switch expression
            |def a = 1
            |def r = switch (a) {
            |    case 1 -> {
            |        for (;;) {
            |            return 1
            |        }
            |    }
            |    default -> 0
            |}
            |
            |'''.stripMargin(), '''\
            |switch expression does not support `return` @ line 6, column 13.
            |               return 1
            |               ^
            |
            |1 error'''.stripMargin())
        expectParseError('''\
            |// fails: the last colon arm can complete without `yield`/`throw` (`if` has no `else`)
            |def cond = false
            |def r = switch ('a') {
            |    case 'a':
            |        if (cond) {
            |            yield 1
            |        }
            |}
            |
            |'''.stripMargin(), '''\
            |`yield` or `throw` is expected @ line 4, column 5.
            |       case 'a':
            |       ^
            |
            |1 error'''.stripMargin())
    }

    @Test
    void 'error alternative - Missing ")" 1'() {
        expectParseError '''\
            |println ((int 123)
            |'''.stripMargin(), '''\
            |Missing ')' @ line 1, column 15.
            |   println ((int 123)
            |                 ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'error alternative - Missing ")" 2'() {
        expectParseError '''\
            |def x() {
            |    println((int) 123
            |}
            |'''.stripMargin(), '''\
            |Missing ')' @ line 2, column 22.
            |       println((int) 123
            |                        ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'error alternative - Missing ")" 3'() {
        expectParseError '''\
            |def m( {
            |}
            |'''.stripMargin(), '''\
            |Missing ')' @ line 1, column 8.
            |   def m( {
            |          ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'error alternative - Missing ")" 4'() {
        expectParseError '''\
            |foo(1, 2
            |'''.stripMargin(), '''\
            |Missing ')' @ line 1, column 9.
            |   foo(1, 2
            |           ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'error alternative - Missing ")" 5'() {
        expectParseError '''\
            |def f(int x
            |'''.stripMargin(), '''\
            |Missing ')' @ line 1, column 12.
            |   def f(int x
            |              ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'error alternative - Missing ")" 6'() {
        expectParseError '''\
            |println ((int) 123
            |'''.stripMargin(), '''\
            |Missing ')' @ line 1, column 19.
            |   println ((int) 123
            |                     ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'error alternative - Missing "]" 1'() {
        expectParseError '''\
            |[1, 2
            |'''.stripMargin(), '''\
            |Missing ']' @ line 1, column 6.
            |   [1, 2
            |        ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'error alternative - Missing "]" 2'() {
        expectParseError '''\
            |foo[1
            |'''.stripMargin(), '''\
            |Missing ']' @ line 1, column 6.
            |   foo[1
            |        ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'error alternative - Missing "]" 3'() {
        expectParseError '''\
            |def x = [[1, 2]
            |'''.stripMargin(), '''\
            |Missing ']' @ line 1, column 16.
            |   def x = [[1, 2]
            |                  ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'error alternative - Missing "]" 4'() {
        expectParseError '''\
            |foo([1, 2)
            |'''.stripMargin(), '''\
            |Missing ']' @ line 1, column 10.
            |   foo([1, 2)
            |            ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'error alternative - Missing "]" 5'() {
        expectParseError '''\
            |a?[0
            |'''.stripMargin(), '''\
            |Missing ']' @ line 1, column 5.
            |   a?[0
            |       ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'error alternative - Missing "]" 6'() {
        expectParseError '''\
            |def x() {
            |    a[0
            |}
            |'''.stripMargin(), '''\
            |Missing ']' @ line 2, column 8.
            |       a[0
            |          ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'error alternative - Missing "}" 1'() {
        expectParseError '''\
            |def m() {
            |    println 1
            |'''.stripMargin(), '''\
            |Missing '}' @ line 2, column 14.
            |       println 1
            |                ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'error alternative - Missing "}" 2'() {
        expectParseError '''\
            |class C {
            |    def x
            |'''.stripMargin(), '''\
            |Missing '}' @ line 2, column 10.
            |       def x
            |            ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'error alternative - Missing "}" 3'() {
        expectParseError '''\
            |def c = { it
            |'''.stripMargin(), '''\
            |Missing '}' @ line 1, column 13.
            |   def c = { it
            |               ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'error alternative - Missing "}" 4'() {
        expectParseError '''\
            |if (true) {
            |    x = 1
            |'''.stripMargin(), '''\
            |Missing '}' @ line 2, column 10.
            |       x = 1
            |            ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'error alternative - Missing ")" cast with generic type'() {
        expectParseError '''\
            |def x = (List<String> 1
            |'''.stripMargin(), '''\
            |Missing ')' @ line 1, column 23.
            |   def x = (List<String> 1
            |                         ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'error alternative - Missing ")" empty open paren'() {
        expectParseError '''\
            |(
            |'''.stripMargin(), '''\
            |Missing ')' @ line 1, column 2.
            |   (
            |    ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'error alternative - Missing "]" nested list'() {
        expectParseError '''\
            |[[1]
            |'''.stripMargin(), '''\
            |Missing ']' @ line 1, column 5.
            |   [[1]
            |       ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'error alternative - Missing "}" nested block'() {
        expectParseError '''\
            |{{1}
            |'''.stripMargin(), '''\
            |Missing '}' @ line 1, column 5.
            |   {{1}
            |       ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'error alternative - Missing "}" after multi-line string'() {
        // Caret must sit on the last line of the triple-quoted string, not on
        // the start line with an overshot column (multi-line insertion point).
        expectParseError '''\
            |def m() {
            |  s = """
            |line2
            |line3"""
            |'''.stripMargin(), '''\
            |Missing '}' @ line 4, column 9.
            |   line3"""
            |           ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'error alternative - Missing "}" after multi-line single-quoted string'() {
        // Outer double-quoted long strings avoid clashing with ''' inside the source.
        expectParseError("""\
            |def m() {
            |  s = '''
            |x
            |y'''
            |""".stripMargin(), """\
            |Missing '}' @ line 4, column 5.
            |   y'''
            |       ^
            |
            |1 error
            |""".stripMargin())
    }

    @Test
    void 'unclosed single-quoted string'() {
        expectParseError '''\
            |println 'Hello
            |'''.stripMargin(), '''\
            |Unclosed string literal @ line 1, column 9.
            |   println 'Hello
            |           ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'unclosed double-quoted string'() {
        expectParseError '''\
            |println "Hello
            |'''.stripMargin(), '''\
            |Unclosed string literal @ line 1, column 9.
            |   println "Hello
            |           ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'closed string with windows path illegal escape'() {
        expectParseError 'x = "C:\\Users\\me"', '''\
            |Illegal escape character: '\\U' @ line 1, column 8.
            |   x = "C:\\Users\\me"
            |          ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'closed single-quoted string with illegal escape'() {
        expectContains "s = 'C:\\Users\\me'", "Illegal escape character: '\\U'"
    }

    @Test
    void 'closed string with unknown letter escape'() {
        expectContains 's = "hello\\q"', "Illegal escape character: '\\q'"
    }

    @Test
    void 'incomplete unicode escape in a closed string'() {
        expectContains 's = "\\u12"', "Illegal escape character: '\\u'"
    }

    @Test
    void 'unclosed windows path still names the illegal escape'() {
        expectContains "x = \"C:\\Users", "Illegal escape character: '\\U'"
    }

    @Test
    void 'unclosed triple-quoted string'() {
        expectContains "s = '''hello", 'Unclosed string literal'
    }

    @Test
    void 'unclosed comment'() {
        expectParseError '''\
            |/* comment
            |'''.stripMargin(), '''\
            |Unclosed comment @ line 1, column 1.
            |   /* comment
            |   ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'unclosed comment after a statement'() {
        expectParseError '''\
            |def x = 1
            |/* still open
            |'''.stripMargin(), '''\
            |Unclosed comment @ line 2, column 1.
            |   /* still open
            |   ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'zero-width space is shown as a unicode escape'() {
        expectParseError "def \u200Bname = null\n", '''\
            |Unexpected character: '\\u200b' @ line 1, column 5.
            |   def \u200Bname = null
            |       ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'form feed is shown as \\f'() {
        expectParseError "def na\u000Cme = null\n", '''\
            |Unexpected character: '\\f' @ line 1, column 7.
            |   def na\u000Cme = null
            |         ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'NUL is shown as a unicode escape'() {
        expectContains 'def x\u0000 = 1', "Unexpected character: '\\u0000'"
    }

    @Test
    void 'BOM is shown as a unicode escape'() {
        expectContains "\uFEFFdef x = 1", "Unexpected character: '\\ufeff'"
    }

    @Test
    void 'printable unexpected character is shown as itself'() {
        expectContains 'def `name` = 1', "Unexpected character: '`'"
    }

    @Test
    void 'no-break space is shown as a unicode escape'() {
        expectContains "def \u00A0x = 1", "Unexpected character: '\\u00a0'"
    }

    @Test
    void 'curly quote is shown as a unicode escape'() {
        expectContains "println \u2018hello", "Unexpected character: '\\u2018'"
    }

    @Test
    void 'em dash is shown as a unicode escape'() {
        expectContains "def x = 1\u20142", "Unexpected character: '\\u2014'"
    }

    @Test
    void 'if without parentheses'() {
        expectParseError '''\
            |if true { x = 1 }
            |'''.stripMargin(), '''\
            |Missing '(' @ line 1, column 4.
            |   if true { x = 1 }
            |      ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'while without parentheses'() {
        expectContains 'while true { x = 1 }', "Missing '('"
    }

    @Test
    void 'for without parentheses'() {
        expectContains 'for int i in 1..2 {}', "Missing '('"
    }

    @Test
    void 'incomplete ternary missing colon'() {
        expectParseError 'x ? y', '''\
            |Missing ':' @ line 1, column 6.
            |   x ? y
            |        ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'generic type missing closing angle'() {
        expectContains 'def list1 = new ArrayList<Integer()', "Missing '>'"
    }

    @Test
    void 'const is not supported'() {
        expectParseError '''\
            |const x = 1
            |'''.stripMargin(), '''\
            |'const' is not supported; use 'val' or 'static final' instead @ line 1, column 1.
            |   const x = 1
            |   ^
            |
            |1 error
            |'''.stripMargin()
        // Same sentence in class and method bodies: the token has no parse
        // context cheap enough to pick one replacement, so both are named.
        expectContains 'class C { const x = 1 }', "'const' is not supported; use 'val' or 'static final' instead"
        expectContains 'def m() { const x = 1 }', "'const' is not supported; use 'val' or 'static final' instead"
    }

    @Test
    void 'goto is not supported'() {
        expectParseError '''\
            |goto label
            |'''.stripMargin(), '''\
            |'goto' is not supported @ line 1, column 1.
            |   goto label
            |   ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'else after if with no then-branch still names else'() {
        expectContains 'if (x) else { }', "'else' without 'if'"
    }

    @Test
    void 'else without if'() {
        expectContains 'else { x = 1 }', "'else' without 'if'"
    }

    @Test
    void 'catch without try'() {
        expectContains 'catch (e) { }', "'catch' without 'try'"
    }

    @Test
    void 'finally without try'() {
        expectContains 'finally { }', "'finally' without 'try'"
    }

    @Test
    void 'case outside of switch'() {
        expectContains 'case 1: x = 1', "'case' outside of switch"
    }

    @Test
    void 'default colon outside of switch'() {
        expectContains 'default: x = 1', "'default' outside of switch"
    }

    @Test
    void 'default arrow outside of switch'() {
        expectContains 'default -> x', "'default' outside of switch"
    }

    @Test
    void 'incomplete interface default method is not labelled as outside of switch'() {
        String msg = TestUtils.compileMessage('interface I { default }')
        assert !msg.contains("'default' outside of switch"), msg
    }

    @Test
    void 'threadsafe is not supported'() {
        expectContains 'threadsafe foo() {}', "'threadsafe' is not supported"
        expectContains 'threadsafe', "'threadsafe' is not supported"
    }

    @Test
    void 'throw with nothing after it'() {
        expectContains 'throw', 'Unexpected end of input'
    }

    @Test
    void 'import static with nothing after it'() {
        expectContains 'import static', 'Unexpected end of input'
    }

    @Test
    void 'number ending with underscore'() {
        expectContainsOnce 'def n = 1_', 'Number ending with underscores is invalid'
    }

    @Test
    void 'invalid octal number'() {
        expectContainsOnce 'def n = 08', 'Invalid octal number'
    }

    @Test
    void 'shebang not on the first line'() {
        expectContainsOnce 'x = 1\n#!/usr/bin/env groovy', 'Shebang comment should appear at the first line'
    }

    @Test
    void 'varargs must be the last parameter and names it'() {
        expectContains 'def m(int... a, int b) {}', 'The var-arg parameter a must be the last parameter'
    }

    // Groovy 4 safe index: '?[' is one token and pairs with ']'

    @Test
    void 'unclosed empty safe index'() {
        expectParseError '''\
            |a?[
            |'''.stripMargin(), '''\
            |Missing ']' @ line 1, column 4.
            |   a?[
            |      ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'safe index mismatched closer is still missing bracket'() {
        expectContains 'a?[0)', "Missing ']'"
        expectContains 'a?[0}', "Missing ']'"
    }

    @Test
    void 'nested and chained unclosed safe index name the inner bracket'() {
        expectContains 'a?[b?[0', "Missing ']'"
        expectContains 'a?[0]?[1', "Missing ']'"
        expectContains 'a[b?[0', "Missing ']'"
    }

    @Test
    void 'safe index inside parens still names the missing bracket'() {
        expectContains '(a?[0', "Missing ']'"
        expectContains '(a[0', "Missing ']'"
        expectContains 'foo(a?[0', "Missing ']'"
    }

    @Test
    void 'safe index without a receiver'() {
        expectContains '?[0]', "'?[' requires an expression before it"
    }

    @Test
    void 'safe index after open paren is not a receiver'() {
        expectContains 'foo(?[0])', "'?[' requires an expression before it"
    }

    @Test
    void 'safe index after assignment is not a receiver'() {
        expectContains 'x = ?[0]', "'?[' requires an expression before it"
    }

    @Test
    void 'safe index after a lone dot is not a receiver'() {
        expectContains 'foo.?[0]', "'?[' requires an expression before it"
    }

    @Test
    void 'safe index after a newline is not a path continuation'() {
        expectContains 'a\n?[0]', "'?[' requires an expression before it"
    }

    @Test
    void 'question then safe index is not a receiver'() {
        expectContains 'a??[0]', "'?[' requires an expression before it"
    }

    @Test
    void 'keyword after a dot is still a safe-index receiver'() {
        expectContains 'foo.if?[0)', "Missing ']'"
    }

    @Test
    void 'space between question and bracket is ternary not safe index'() {
        // '?[' is one token; `a? [0]` tokenises as `a` `?` `[0]` (incomplete ternary)
        expectContains 'a? [0]', "Missing ':'"
    }
}
