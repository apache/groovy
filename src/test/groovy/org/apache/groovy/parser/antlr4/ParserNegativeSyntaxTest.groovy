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

import static org.apache.groovy.parser.antlr4.TestUtils.expectParseError

/**
 * Minimal complete <em>negative</em> covering set for the Antlr4 parser,
 * distilled from the groovy-parser (Parrot) lab tests. Each method inlines
 * a representative invalid snippet and asserts the full CONVERSION
 * diagnostic, matching the {@code expectParseError} style of
 * {@link SyntaxErrorTest}.
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

}
