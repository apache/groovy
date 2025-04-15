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
package gls.enums

import gls.CompilableTestSupport

/**
 * Tests various properties of enums.
 */
class EnumTest extends CompilableTestSupport {

    void testValues() {
        assert UsCoin.values().size() == 4
        assert UsCoin.values().toList().sum{ it.value } == 41
    }

    void testNext() {
        def coin = UsCoin.penny
        def coins = [coin++, coin++, coin++, coin++, coin]
        assert coins == [UsCoin.penny, UsCoin.nickel, UsCoin.dime, UsCoin.quarter, UsCoin.penny]
    }

    void testPrevious() {
        def coin = UsCoin.quarter
        def coins = [coin--, coin--, coin--, coin--, coin]
        assert coins == [UsCoin.quarter, UsCoin.dime, UsCoin.nickel, UsCoin.penny, UsCoin.quarter]
    }

    void testRange() {
        def coinRange1 = UsCoin.penny..UsCoin.dime
        assert (UsCoin.nickel in coinRange1)
        assert !(UsCoin.quarter in coinRange1)
    }

    void testMinValue() {
        assert UsCoin.MIN_VALUE == UsCoin.penny
        shouldFail(MissingPropertyException) {
           EmptyEnum.MIN_VALUE
        }
    }

    void testMaxValue() {
        assert UsCoin.MAX_VALUE == UsCoin.quarter
        shouldFail(MissingPropertyException) {
           EmptyEnum.MAX_VALUE
        }
    }

    void testComparators() {
        assert UsCoin.nickel <=> UsCoin.penny  ==  1
        assert UsCoin.nickel <=> UsCoin.nickel ==  0
        assert UsCoin.nickel <=> UsCoin.dime   == -1
        assert UsCoin.nickel <=  UsCoin.nickel
        assert UsCoin.nickel <=  UsCoin.dime
        assert UsCoin.nickel >=  UsCoin.penny
        assert UsCoin.nickel >=  UsCoin.nickel
    }

    void testStepWithRange() {
        def coinRange2 = UsCoin.nickel..UsCoin.quarter
        def coins = coinRange2.toList()
        assert coins == [UsCoin.nickel, UsCoin.dime, UsCoin.quarter]
        coins = coinRange2.step(2)
        assert coins == [UsCoin.nickel, UsCoin.quarter]
        coins = coinRange2.step(3)
        assert coins == [UsCoin.nickel]
        coins = coinRange2.step(4)
        assert coins == [UsCoin.nickel]
        coins = coinRange2.step(-1)
        assert coins == [UsCoin.quarter, UsCoin.dime, UsCoin.nickel]
        coins = coinRange2.step(-2)
        assert coins == [UsCoin.quarter, UsCoin.nickel]
        coins = coinRange2.step(-3)
        assert coins == [UsCoin.quarter]
        coins = coinRange2.step(-4)
        assert coins == [UsCoin.quarter]
    }

    void testStepWithReverseRange() {
        def coinRange2 = UsCoin.quarter..UsCoin.nickel
        def coins = coinRange2.toList()
        assert coins == [UsCoin.quarter, UsCoin.dime, UsCoin.nickel]
        coins = coinRange2.step(2)
        assert coins == [UsCoin.quarter, UsCoin.nickel]
        coins = coinRange2.step(3)
        assert coins == [UsCoin.quarter]
        coins = coinRange2.step(4)
        assert coins == [UsCoin.quarter]
        coins = coinRange2.step(-1)
        assert coins == [UsCoin.nickel, UsCoin.dime, UsCoin.quarter]
        coins = coinRange2.step(-2)
        assert coins == [UsCoin.nickel, UsCoin.quarter]
        coins = coinRange2.step(-3)
        assert coins == [UsCoin.nickel]
        coins = coinRange2.step(-4)
        assert coins == [UsCoin.nickel]
    }

    void testEnumWithSingleListInConstructor() {
        assertScript '''
            enum ListEnum1 {
                ONE([111, 222])
                ListEnum1(Object listArg){
                    assert listArg == [111, 222]
                    assert listArg instanceof ArrayList
                }
            }
            println ListEnum1.ONE
        '''

        assertScript '''
            enum ListEnum2 {
                TWO([234, [567,12]])
                ListEnum2(Object listArg){
                    assert listArg == [234, [567, 12]]
                    assert listArg instanceof ArrayList
                }
            }
            println ListEnum2.TWO
        '''
    }

    // GROOVY-3214
    void testSingleListDoesNoInfluenceMaps() {
        // the fix for GROOVY-2933 caused map["taku"] to become map[(["take])] instead
        assertScript '''
            enum FontFamily {
                ARIAL

                static void obtainMyMap() {
                    Map map = [:]
                    map["taku"] = "dio"
                    assert map.taku == "dio"
                }
            }
            FontFamily.obtainMyMap()
        '''
    }

    // GROOVY-3276
    void testMutipleValuesDontGetWronglyWrappedInList() {
        // the fix for GROOVY-3214 caused multiple values passed in an enum const to get wrapped in an extra ListExpression
        assertScript '''
            enum GROOVY3276 {
               A(1,2), B(3,4)

               GROOVY3276(int xx, int yy) {
                   x=xx
                   y=yy
               }
               public int x
               public int y
           }

           assert GROOVY3276.A.x == 1
           assert GROOVY3276.B.y == 4
        '''
    }

    // GROOVY-3161
    void testStaticEnumFieldWithEnumValues() {
        def allColors = GroovyColors3161.ALL_COLORS
        assert allColors.size() == 3
        assert allColors[0] == GroovyColors3161.red
        assert allColors[1] == GroovyColors3161.blue
        assert allColors[2] == GroovyColors3161.green
    }

    // GROOVY-7025
    void testStaticEnumFieldFromInit() {
        def err = shouldNotCompile '''
            enum E {
                FOO('bar');
                private static final Set<String> names = []
                E(String name) {
                    names.add(name)
                }
            }
        '''
        assert err =~ /Cannot refer to the static enum field 'names' within an initializer/

        shouldCompile '''
            enum E {
                FOO;
                private static final String ONE = 1
                private final value
                E() {
                    value = 1 + ONE
                }
            }
        '''

        shouldCompile '''
            enum E {
                FOO;
                private static final int ONE = 1
                private final value
                E() {
                    value = 1 + ONE
                }
            }
        '''

        shouldNotCompile '''
            enum E {
                FOO;
                private static int ONE = 1
                private final value
                E() {
                    value = 1 + ONE
                }
            }
        '''
    }

    // GROOVY-3283
    void testImportStaticMoreThanOneEnum() {
        assertScript '''
            enum Foo3283 { A,B }
            enum Bar3283 { X,Y }

            import static Foo3283.*
            import static Bar3283.*

            a = A
            x = X
        '''
    }

    // GROOVY-3284
    void testCallBehaviorOnEnum() {
        // test the usage in a non-script class first
        for (f in Foo3284) {
            assert f() == "A"
        }
        assert Foo3284.A.call() == "A"
        assert Foo3284.A() == "A"
        def a = Foo3284.A
        assert a() == "A"

        // now test the usage in a script but this time type Closure not specified explicitly
        assertScript '''
            enum Foo32842 {
                B({ "B" })
                Foo32842(c) {
                  call = c
                }
                def call
            }
            for (f in Foo32842) {
                assert f() == "B"
            }

            assert Foo32842.B.call() == "B"

            assert Foo32842.B() == "B"
            b = Foo32842.B
            assert b() == "B"
        '''
    }

    // GROOVY-3483
    void testClassResolutionForInnerEnumsWithPackageName() {
        assertScript '''
            package familie

            class Mother3483 {
                Mother3483.Child child

                enum Child {
                    Franz,
                    Ferdi,
                    Nand
                }
            }

            def mother = new Mother3483(child: Mother3483.Child.Franz)

            assert mother.child as String == 'Franz'
        '''
    }

    // GROOVY-3110
    void testInnerEnumUsedInDefiningClassWithUnqualifiedEnumNameUsed() {
        assertScript '''
            class C {
                enum E {
                    FOO, BAR
                }
                E enumVar
                void setEnumVar() {
                    enumVar = E.FOO
                }
            }

            def c = new C()
            assert c.enumVar == null

            c.setEnumVar()
            assert c.enumVar == C.E.FOO

            c.setEnumVar(C.E.BAR)
            assert c.enumVar == C.E.BAR
        '''
    }

    // GROOVY-11198
    void testInnerEnumInitWithUnqualifiedOuterClassField() {
        assertScript '''
            class C {
                private static final int ONE = 1
                enum E {
                    FOO(1 + ONE)
                    final value
                    E(value) {
                        this.value = value
                    }
                }
            }

            assert C.E.FOO.value == 2
        '''
    }

    // GROOVY-3693
    void testStaticFieldInitValuesInAStaticBlock() {
        // trigger enum class load to test it - asserts are present in the enum
        GroovyColors3693.r
    }

    // GROOVY-2443
    void testCustomMethodOnEnum() {
        assertScript '''
            enum Day {
                SUNDAY { @Override String activity() { 'Relax' } },
                MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
                String activity() { 'Work' }
            }
            assert 'Work' == Day.MONDAY.activity()
            assert 'Relax' == Day.SUNDAY.activity()
        '''
    }

    // GROOVY-3985
    void testEnumConstantSubClassINITMethodOverrideINITOfEnumClass() {
        try {
            // cause loading of enum that causes its fields to be set and
            // their instance initializer to be executed
            println Color3985
        }catch(ExceptionInInitializerError err) {
            assert err.cause.message == 'Color3985 RED instance initializer called successfully'
        }
    }

    // GROOVY-3996
    void testEnumStaticInitWithAFieldUsingEnumValues() {
        assertScript '''
            enum Color3996 {
                R, G, B
                public static Color3996[] ALL_COLORS = [R, G, B]
            }

            assert Color3996.ALL_COLORS.size() == 3
            assert Color3996.ALL_COLORS[0] == Color3996.R
            assert Color3996.ALL_COLORS[1] == Color3996.G
            assert Color3996.ALL_COLORS[2] == Color3996.B
        '''
    }

    // GROOVY-3986
    void testEnumWithTopLevelNoBracketsMethodCall() {
        assertScript '''
            enum Color3986 {
                RED {
                  String toString() { sprintf '%s', 'foo' }
                },GREEN,BLUE
            }
            assert Color3986.RED.toString() == 'foo'
        '''
    }

    // GROOVY-3047
    void testEnumConstantSeparators() {
        shouldCompile '''
            enum Foo0 { X }
            enum Foo1 {

              Y

            }
            enum Foo2 {
              Z,
            }
            enum Foo3 { X, Y, Z,
            }
            enum Foo4 {
              X
              ,
            }
            enum Foo5 {
              X
              ,
              Y
            }
            enum Foo6 {
              X
              ,
              Y,
            }
            enum Foo7 {
              X
              ,
              Y
              ,
            }
        '''
    }

    // GROOVY-4268
    void testEnumWithSingleValueAndClassField() {
        shouldCompile '''
            enum EnumWithSingleValueAndClassField {
                VALUE
                String toString() { "I'm a value" }
            }
        '''
    }

    // GROOVY-4444
    void testConstructorChainingInEnum() {
        assertScript '''
            enum Foo4444 {
                ONE(1), TWO(1, 2)

                int i
                int j

                Foo4444(int i) {
                    this(i, 0)
                }

                Foo4444(int i, int j) {
                    this.i = i
                    this.j = j
                }
            }

            def foos = [Foo4444.ONE, Foo4444.TWO]

            assert foos.size() == 2

            assert foos[0].i == 1
            assert foos[0].j == 0

            assert foos[1].i == 1
            assert foos[1].j == 2
        '''
    }

    // GROOVY-6065
    void testOverridingMethodsWithExplicitConstructor() {
        assertScript '''
            enum Country {
                Hungary(9_939_000), Italy(61_482_000), Poland(38_383_000) { String getCountryCode() { 'pl' } }
                final int population
                Country(population) { this.population = population }
                String getCountryCode() { name()[0..1].toLowerCase() }
            }

            assert Country.Hungary.countryCode == 'hu'
            assert Country.Italy.countryCode == 'it'
            assert Country.Poland.countryCode == 'pl'
        '''
    }

    // GROOVY-6747
    void testOverridingMethodsWithExplicitConstructor2() {
        assertScript '''
            enum Codes {
                YES('Y') {
                    @Override String getCode() { /*string*/ }
                },
                NO('N') {
                    @Override String getCode() { /*string*/ }
                }

                abstract String getCode()

                private final String string

                private Codes(String string) {
                    this.string = string
                }
            }

            assert Codes.YES.code == null // TODO: 'Y'
        '''
    }

    // GROOVY-4641
    void testAbstractMethodOverriding() {
        assertScript '''
            enum Day {
               SUNDAY {
                  String getAction() { 'Relax' }
               },
               MONDAY {
                   String getAction() { 'Work' }
               }
               abstract String getAction()
            }
            assert 'Relax' ==  Day.SUNDAY.action
        '''
        shouldNotCompile '''
            enum Day {
               SUNDAY {
                  String getAction() { 'Relax' }
               },
               MONDAY
               abstract String getAction()
            }
            assert 'Relax' ==  Day.SUNDAY.action
        '''
        shouldNotCompile '''
            enum Day {
               SUNDAY {
                  String getAction() { 'Relax' }
               },
               MONDAY {}
               abstract String getAction()
            }
            assert 'Relax' ==  Day.SUNDAY.action
        '''
    }

    // GROOVY-5756
    void testInnerClosureDefinitions() {
        assertScript '''
            enum E {
                CONTROL,
                INSTANCE {
                    @Override
                    String foo() {
                        def c1 = { -> 'foo1' }
                        c1.call()
                    }
                };
                String foo() {
                    def c2 = { -> 'foo2' }
                    c2.call()
                }
            }
            assert E.CONTROL .foo() == 'foo2'
            assert E.INSTANCE.foo() == 'foo1'
        '''
    }

    // GROOVY-4794
    void testLenientTypeDefinitions() {
        assertScript '''
            enum E {
              enConst {
                @Lazy pi = 3.14
                def twopi = 6.28
                def foo(){ "" + pi + " " + twopi }
                public bar(){ "" + twopi + " " + pi }
              }
            }
            assert E.enConst.foo() == '3.14 6.28'
            assert E.enConst.bar() == '6.28 3.14'
        '''
    }

    // GROOVY-4485
    void testNamedArgs() {
        assertScript '''
            enum ExportFormat {
                EXCEL_OOXML(mime: "application/vnd.ms-excel", extension: "xlsx"),
                EXCEL_BINARY(mime: "application/vnd.ms-excel", extension: "xls"),
                EXCEL_HTML(mime: "application/vnd.ms-excel", extension: "xls"),
                FOO() // dummy const for testing
                String mime, extension = 'default'
            }

            assert ExportFormat.EXCEL_BINARY.extension == 'xls'
            ExportFormat.values().each{
                assert it == ExportFormat.FOO || it.mime == 'application/vnd.ms-excel'
            }
            assert ExportFormat.EXCEL_HTML.ordinal() == 2
            assert ExportFormat.FOO.extension == 'default'
        '''
    }

    // GROOVY-6250
    void testGenericMethodOverriding() {
        assertScript '''
            interface IVisitor<InputType, OutputType> {
                OutputType visitMe(InputType input)
            }

            class ConcreteVisitor implements IVisitor<Void, String> {
                String visitMe(Void v) { 'I have been visited!' }
            }

            enum MyEnum {
                ENUM1 {
                    @Override
                    <I, O> O accept(IVisitor<I, O> visitor, I input) {
                        visitor.visitMe(input)
                    }
                }
                abstract <I, O> O accept(IVisitor<I, O> visitor, I input)
            }

            assert MyEnum.ENUM1.accept(new ConcreteVisitor(), null) == 'I have been visited!'
        '''
    }

    void testVargsConstructor() {
        assertScript '''
            enum Test {
                TEST1(1, 2, 3)
                public final info

                Test(Integer... ints) {
                    info = ints
                }
            }
            println Test.TEST1.info == [1,2,3]
        '''
    }

    // GROOVY-7342
    void testLastEnumValueIsAnnotatedWithoutTrailingComma() {
        assertScript '''
            import java.lang.annotation.ElementType;
            import java.lang.annotation.Target;

            @Target([ElementType.FIELD])
            @interface Fooann {
            }

            enum Foonum {
                @Fooann
                X,
                @Fooann
                Y
            }

            println Foonum.X
            println Foonum.Y
        '''
    }

    // GROOVY-7773
    void testEnumWithPropertiesAndDanglingComma() {
        assertScript '''
            enum UsState {
                ID('Idaho'),
                IL('Illinois'),
                IN('Indiana'),
                ;
                UsState( String value ) { this.value = value }
                private final String value
                String toString() { value }
            }
            assert UsState.ID.toString() == 'Idaho'
        '''
    }

    void testEnumConstantsTakePrecedenceOverClassProperties() {
        assertScript '''
            @Deprecated
            enum Foo {
                annotations
            }
            assert 'annotations' == Foo.annotations.toString()
            assert Foo.getAnnotations().size() == 1
        '''
    }

    // GROOVY-6747
    void testEnumConstructorHasPrivateModifier() {
        assertScript '''
            enum Foo {
                BAR, BAZ
                Foo() {}
            }
            assert java.lang.reflect.Modifier.isPrivate(Foo.declaredConstructors[0].modifiers)
        '''
    }

    // GROOVY-8360
    void testNestedEnumHasStaticModifier() {
        assertScript '''
            class Foo {
                enum Bar {
                    X('x'), Y
                    String s
                    Bar(String s) { this.s = s }
                    Bar() {}
                }
            }
            assert java.lang.reflect.Modifier.isStatic(Foo.Bar.modifiers)
            assert Foo.Bar.X.s == 'x'
        '''
    }

    // GROOVY-8360
    void testDeeplyNestedEnumHasStaticModifier() {
        assertScript '''
            class Foo {
                class Baz {
                    enum Bar {
                        X('x'), Y
                        String s
                        Bar(String s) { this.s = s }
                        Bar() {}
                    }
                }
            }
            assert java.lang.reflect.Modifier.isStatic(Foo.Baz.Bar.modifiers)
            assert Foo.Baz.Bar.X.s == 'x'
        '''
    }

    // GROOVY-8360
    void testNestedEnumHasStaticModifierSC() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                enum Bar {
                    X('x'), Y
                    String s
                    Bar(String s) { this.s = s }
                    Bar() {}
                }
            }
            @groovy.transform.CompileStatic
            void test() {
                assert java.lang.reflect.Modifier.isStatic(Foo.Bar.getModifiers())
                assert Foo.Bar.X.s == 'x'
            }
            test()
        '''
    }

    // GROOVY-8360
    void testDeeplyNestedEnumHasStaticModifierSC() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                class Baz {
                    enum Bar {
                        X('x'), Y
                        String s
                        Bar(String s) { this.s = s }
                        Bar() {}
                    }
                }
            }
            @groovy.transform.CompileStatic
            void test() {
                assert java.lang.reflect.Modifier.isStatic(Foo.Baz.Bar.getModifiers())
                assert Foo.Baz.Bar.X.s == 'x'
            }
            test()
        '''
    }

    // GROOVY-9523
    void testEnumConstMethodCallsAnEnumPrivateMethod() {
        assertScript '''
            class Foo {
                String x
                enum Bar {
                    SOMETHING {
                        @Override
                        void accept(Foo foo) {
                            foo.x = truncate(foo.x, 2)
                        }
                    }
                    abstract void accept(Foo foo);
                    private String truncate(String string, int maxLength) { string.take(maxLength) }
                }
            }

            def foo = new Foo(x:'123456')
            Foo.Bar.SOMETHING.accept(foo)
            assert foo.getX().size() == 2
        '''
    }

    // GROOVY-7024
    void testEnumConstructorCallsOuterClassStaticMethod() {
        assertScript '''
            class Outer {
                static final Map props = [bar: 10, baz: 20]
                static save(name, value) {
                    props[name] = value
                }
                enum Inner {
                    FOO('foo');
                    Inner(String name) {
                        save(name, 30)
                    }
                }
            }
            def in = Outer.Inner.FOO
            assert Outer.props == [bar: 10, baz: 20, foo: 30]
        '''
    }

    // GROOVY-10811
    void testIllegalModifiers() {
        for (mod in ['','public','@groovy.transform.PackageScope']) {
            shouldCompile """
                $mod enum E {
                }
            """
        }
        for (mod in ['abstract','final','static','private','protected']) {
            def err = shouldNotCompile """
                $mod enum E {
                }
            """
        }

        for (mod in ['','public','private','protected','@groovy.transform.PackageScope','static']) {
            shouldCompile """
                class C {
                    $mod enum E {
                    }
                }
            """
        }
        for (mod in ['abstract','final']) {
            shouldNotCompile """
                class C {
                    $mod enum E {
                    }
                }
            """
        }
    }

    // GROOVY-10811
    void testConstructorCheck() {
        shouldNotCompile '''
            enum E {
                FOO;
                E(x) {
                }
            }
        '''

        shouldCompile '''
            enum E {
                FOO;
                E(x=null) {
                }
            }
        '''
    }

    // GROOVY-10811
    void testSuperCtorCall() {
        shouldNotCompile '''
            enum E {
                FOO;
                E() {
                    super()
                }
            }
        '''
    }
}

enum UsCoin {
    penny(1), nickel(5), dime(10), quarter(25)
    UsCoin(int value) { this.value = value }
    private final int value
    int getValue() { value }
}

enum EmptyEnum{}

enum GroovyColors3161 {
    red, blue, green
    static ALL_COLORS = [red, blue, green]
}

enum Foo3284 {
    A({ "A" })
    Foo3284(Closure c) {
      call = c
    }
    final Closure call
}

enum GroovyColors3693 {
    r, g, b
    static List list = [1, 2]
    static init() {
        assert list == [1, 2]
    }
    static {
        init()
    }
}

enum Color3985 {
    RED {
        {
            throw new RuntimeException('Color3985 RED instance initializer called successfully')
        }
    },
    GREEN,
    BLUE,
}
