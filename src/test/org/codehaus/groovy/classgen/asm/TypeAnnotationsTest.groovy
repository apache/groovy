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
package org.codehaus.groovy.classgen.asm

final class TypeAnnotationsTest extends AbstractBytecodeTestCase {

    private final String imports = '''\
        |import java.lang.annotation.*
        |import static java.lang.annotation.ElementType.*
        |import static java.lang.annotation.RetentionPolicy.*
        |'''.stripMargin()

    void testTypeAnnotationsForConstructor1() {
        def bytecode = compile(classNamePattern: 'HasConstructor', method: '<init>', imports + '''
            @Retention(RUNTIME) @Target(CONSTRUCTOR) @interface CtorAnno  { }
            @Retention(RUNTIME) @Target(TYPE_USE)    @interface TypeAnno0 { }
            @Retention(RUNTIME) @Target(TYPE_USE)    @interface TypeAnno1 { }

            class HasConstructor {
                @CtorAnno @TypeAnno0 @TypeAnno1 HasConstructor() { }
            }
        ''')
        assert bytecode.hasSequence([
                'public <init>()V',
                '@LCtorAnno;()',
                '@LTypeAnno0;() : METHOD_RETURN',
                '@LTypeAnno1;() : METHOD_RETURN'
        ])
    }

    void testTypeAnnotationsForConstructor2() {
        def bytecode = compile(classNamePattern: 'Foo.Bar', method: '<init>', imports + '''
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno0 { String value() }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno1 { String value() }

            class Foo {
                class Bar {
                    @TypeAnno0(value="this")
                    Bar(@TypeAnno1(value="that") that) {
                    }
                }
            }
        ''')
        assert bytecode.hasStrictSequence([
                'public <init>(LFoo;Ljava/lang/Object;)V',
                '@LTypeAnno0;(value="this") : METHOD_RETURN',
                '@LTypeAnno1;(value="that") : METHOD_FORMAL_PARAMETER 0, null',
                'L0'
        ])
    }

    // GROOVY-11184
    void testTypeAnnotationsForConstructor3() {
        def bytecode = compile(classNamePattern: 'Foo.Bar', method: '<init>', imports + '''
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno0 { String value() }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno1 { String value() }

            class Foo {
                class Bar {
                    Bar(@TypeAnno0(value="this") Foo this, @TypeAnno1(value="that") that = null) {
                    }
                }
            }
        ''')
        assert bytecode.hasStrictSequence([
                'public <init>(LFoo;Ljava/lang/Object;)V',
                '@LTypeAnno0;(value="this") : METHOD_RECEIVER, null',
                '@LTypeAnno1;(value="that") : METHOD_FORMAL_PARAMETER 0, null',
                'L0'
        ])
        assert bytecode.hasStrictSequence([
                'public <init>(LFoo;)V',
                '@Lgroovy/transform/Generated;()',
                '@LTypeAnno0;(value="this") : METHOD_RECEIVER, null',
                'L0'
        ])
    }

    void testTypeAnnotationsForMethod1() {
        def bytecode = compile(method: 'foo', imports + '''
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno0 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno1 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno2 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno3 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno4 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno5 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno6 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno7 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno8 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno9 { }

            <@TypeAnno0 X extends @TypeAnno1 Number> @TypeAnno2 List<@TypeAnno3 X> foo(
                @TypeAnno4 List<@TypeAnno5 ? super @TypeAnno6 Map<@TypeAnno7 X, @TypeAnno8 ?>> arg
            ) throws @TypeAnno9 Exception {
            }
        ''')
        assert bytecode.hasSequence([
                'public foo(Ljava/util/List;)Ljava/util/List;',
                '@LTypeAnno0;() : METHOD_TYPE_PARAMETER 0, null',
                '@LTypeAnno1;() : METHOD_TYPE_PARAMETER_BOUND 0, 0, null',
                '@LTypeAnno2;() : METHOD_RETURN, null',
                '@LTypeAnno3;() : METHOD_RETURN, 0;',
                '@LTypeAnno4;() : METHOD_FORMAL_PARAMETER 0, null',
                '@LTypeAnno5;() : METHOD_FORMAL_PARAMETER 0, 0;',
                '@LTypeAnno6;() : METHOD_FORMAL_PARAMETER 0, 0;*',
                '@LTypeAnno7;() : METHOD_FORMAL_PARAMETER 0, 0;*0;',
                '@LTypeAnno8;() : METHOD_FORMAL_PARAMETER 0, 0;*1;',
                '@LTypeAnno9;() : THROWS 0, null'
        ])
    }

    void testTypeAnnotationsForMethod2() {
        def bytecode = compile(method: 'foo', imports + '''
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeParameterAnno { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno0 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno1 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno2 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno3 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno4 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno5 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno6 { }

            <@TypeAnno0 @TypeParameterAnno E extends @TypeAnno1 Number & @TypeAnno2 List<@TypeAnno3 E>> void foo(
                @TypeAnno4 E arg1, @TypeAnno5 int arg2, @TypeAnno6 int arg3
            ) {
                // TODO support in code examples below
                try {
                    numbers.addAll(new @TypeAnno5 ArrayList<@TypeAnno6 Integer>());
                } catch (@TypeAnno7 Throwable ignore) {
                }
            }
        ''')
        assert bytecode.hasSequence([
                'public foo(Ljava/lang/Number;II)V',
                '@LTypeAnno0;() : METHOD_TYPE_PARAMETER 0, null',
                '@LTypeParameterAnno;() : METHOD_TYPE_PARAMETER 0, null',
                '@LTypeAnno1;() : METHOD_TYPE_PARAMETER_BOUND 0, 0, null',
                '@LTypeAnno2;() : METHOD_TYPE_PARAMETER_BOUND 0, 1, null',
                '@LTypeAnno3;() : METHOD_TYPE_PARAMETER_BOUND 0, 1, 0;',
                '@LTypeAnno4;() : METHOD_FORMAL_PARAMETER 0, null',
                '@LTypeAnno5;() : METHOD_FORMAL_PARAMETER 1, null',
                '@LTypeAnno6;() : METHOD_FORMAL_PARAMETER 2, null'
        ])
    }

    void testTypeAnnotationsForMethod3() {
        def bytecode = compile(classNamePattern: 'Foo', method: 'get', imports + '''
            @Retention(RUNTIME) @Target(PARAMETER) @interface ParameterAnno { }
            @Retention(RUNTIME) @Target([TYPE_PARAMETER, TYPE_USE]) @interface TypeUseAndParameterAnno { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno0 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno1 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno2 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno3 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno4 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno5 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno6 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno7 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno8 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno9 { }

            class Foo<X, Y> {
                @TypeAnno0 Map<@TypeAnno1 ? extends X, ? super @TypeAnno2 Y>
                get(@TypeAnno3 @ParameterAnno @TypeUseAndParameterAnno List<@TypeAnno4 List<@TypeAnno5 X>> arg1,
                    Map<@TypeAnno6 ? extends @TypeAnno7 X, @TypeAnno8 ? super @TypeAnno9 Y> arg2
                ) throws @TypeAnno0 RuntimeException, @TypeAnno1 IOException {
                    // TODO support in code example below
                    @TypeAnno0 List<@TypeAnno1 Integer> numbers = Arrays.<@TypeAnno3 Integer>asList(5, 3, 50, 24, 40, 2, 9, 18);
                    if (arg2 instanceof @TypeAnno2 Number) {
                        return (@TypeAnno3 Map<@TypeAnno4 ? extends X, ? super @TypeAnno5 Y>) null;
                    }
                    //if (numbers.stream().sorted(@TypeAnno6 Integer::compareTo).count() > 0) return null; // needs grammar tweak
                    return arg2;
                }
            }
        ''')
        assert bytecode.hasSequence([
                'public get(Ljava/util/List;Ljava/util/Map;)Ljava/util/Map; throws java/lang/RuntimeException',
                '@LTypeAnno0;() : METHOD_RETURN, null',
                '@LTypeAnno1;() : METHOD_RETURN, 0;',
                '@LTypeAnno2;() : METHOD_RETURN, 1;*',
                '@LTypeAnno3;() : METHOD_FORMAL_PARAMETER 0, null',
                '@LTypeUseAndParameterAnno;() : METHOD_FORMAL_PARAMETER 0, null',
                '@LTypeAnno4;() : METHOD_FORMAL_PARAMETER 0, 0;',
                '@LTypeAnno5;() : METHOD_FORMAL_PARAMETER 0, 0;0;',
                '@LTypeAnno6;() : METHOD_FORMAL_PARAMETER 1, 0;',
                '@LTypeAnno7;() : METHOD_FORMAL_PARAMETER 1, 0;*',
                '@LTypeAnno8;() : METHOD_FORMAL_PARAMETER 1, 1;',
                '@LTypeAnno9;() : METHOD_FORMAL_PARAMETER 1, 1;*',
                '@LTypeAnno0;() : THROWS 0, null',
                '@LTypeAnno1;() : THROWS 1, null'
        ])
    }

    // GROOVY-9154
    void testTypeAnnotationsForMethod4() {
        def bytecode = compile(classNamePattern: 'Foo', method: 'sizeZeroOrPositive', '''\
            @Grab("net.jqwik:jqwik:1.1.4")
            import net.jqwik.api.ForAll
            import net.jqwik.api.Property
            import net.jqwik.api.constraints.IntRange

            @groovy.transform.CompileStatic
            class Foo {
                @Property
                boolean sizeZeroOrPositive(@ForAll List<@IntRange(min=0, max=10) Integer> items) {
                    items.size() >= 0
                }
            }
        ''')
        assert bytecode.hasSequence([
            'public sizeZeroOrPositive(Ljava/util/List;)Z',
            '@Lnet/jqwik/api/Property;()',
            '@Lnet/jqwik/api/constraints/IntRange;(min=0, max=10) : METHOD_FORMAL_PARAMETER 0, 0;',
            '// annotable parameter count: 1 (visible)',
            '@Lnet/jqwik/api/ForAll;() // parameter 0'
        ])
    }

    // GROOVY-11184
    void testTypeAnnotationsForMethod5() {
        def bytecode = compile(classNamePattern: 'Foo', method: 'bar', imports + '''
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno0 { String value() }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno1 { String value() }

            class Foo {
                def bar(@TypeAnno0(value="this") Foo this, @TypeAnno1(value="that") that = null) {
                }
            }
        ''')
        assert bytecode.hasStrictSequence([
                'public bar(Ljava/lang/Object;)Ljava/lang/Object;',
                '@LTypeAnno0;(value="this") : METHOD_RECEIVER, null',
                '@LTypeAnno1;(value="that") : METHOD_FORMAL_PARAMETER 0, null',
                'L0'
        ])
        assert bytecode.hasStrictSequence([
                'public bar()Ljava/lang/Object;',
                '@Lgroovy/transform/Generated;()',
                '@LTypeAnno0;(value="this") : METHOD_RECEIVER, null',
                'L0'
        ])
    }

    // GROOVY-11479
    void testTypeAnnotationsForClosure() {
        def bytecode = compile(classNamePattern: 'Foo\\$_closure1', method: 'doCall', imports + '''
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno0 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno1 { }

            @groovy.transform.CompileStatic
            class Foo {
                @TypeAnno0 java.util.function.IntUnaryOperator bar = { @TypeAnno1 def i -> 1 }
            }
        ''')
        assert bytecode.hasStrictSequence([
                'public doCall(I)Ljava/lang/Integer;',
                '@LTypeAnno1;() : METHOD_FORMAL_PARAMETER 0, null',
                'L0'
        ])
    }

    // GROOVY-11479
    void testTypeAnnotationsForLambda() {
        def bytecode = compile(classNamePattern: 'Foo\\$_lambda1', method: 'doCall', imports + '''
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno0 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno1 { }

            @groovy.transform.CompileStatic
            class Foo {
                @TypeAnno0 java.util.function.IntUnaryOperator bar = (@TypeAnno1 int i) -> 1
            }
        ''')
        assert bytecode.hasStrictSequence([
                'public doCall(I)I',
                '@LTypeAnno1;() : METHOD_FORMAL_PARAMETER 0, null',
                'L0'
        ])
    }

    void testTypeAnnotationsForField1() {
        def bytecode = compile(classNamePattern: 'Foo', field: 'foo', imports + '''
            @Retention(RUNTIME) @Target(FIELD) @interface FieldAnno { String value() }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno0 { String value() }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno1 { String value() }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno2 { String value() }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno3 { String value() }

            class Foo {
                public static final String FOO = "foo"
                public @FieldAnno(value=Foo.FOO) Map<
                    @TypeAnno0(value=Foo.FOO) ? extends @TypeAnno1(value=Foo.FOO) CharSequence,
                    @TypeAnno2(value=Foo.FOO) List<@TypeAnno3(value=Foo.FOO) ?>
                > foo
            }
        ''')
        assert bytecode.hasSequence([
                'public Ljava/util/Map; foo',
                '@LFieldAnno;(value="foo")',
                '@LTypeAnno0;(value="foo") : FIELD, 0;',
                '@LTypeAnno1;(value="foo") : FIELD, 0;*',
                '@LTypeAnno2;(value="foo") : FIELD, 1;',
                '@LTypeAnno3;(value="foo") : FIELD, 1;0;'
        ])
    }

    void testTypeAnnotationsForField2() {
        def bytecode = compile(classNamePattern: 'Bar', field: 'numbers', imports + '''
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno4 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno5 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno6 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno7 { }

            class Bar {
                // TODO support type annotations for code (TypeAnno7 below)
                public @TypeAnno4 List<@TypeAnno5 ? super @TypeAnno6 Integer> numbers = Arrays.<@TypeAnno7 Integer>asList(5, 3, 50, 24, 40, 2, 9, 18)
            }
        ''')
        assert bytecode.hasSequence([
                'public Ljava/util/List; numbers',
                '@LTypeAnno4;() : FIELD, null',
                '@LTypeAnno5;() : FIELD, 0;',
                '@LTypeAnno6;() : FIELD, 0;*'
        ])
    }

    // GROOVY-11179
    void testTypeAnnotationsForClass() {
        def bytecode = compile(classNamePattern: 'Baz', imports + '''
            @Retention(RUNTIME) @Target(TYPE) @interface TypeAnno { String value() }
            @Retention(RUNTIME) @Target(TYPE_PARAMETER) @interface TypeParameterAnno1 { }
            @Retention(RUNTIME) @Target(TYPE_PARAMETER) @interface TypeParameterAnno2 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeUseAnno0 { String value() }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeUseAnno1 { String value() }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeUseAnno2 { String value() }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeUseAnno3 { String value() }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeUseAnno4 { String value() }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeUseAnno5 { String value() }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeUseAnno6 { String value() }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeUseAnno7 { String value() }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeUseAnno8 { String value() }

            @TypeAnno(value=Baz.VALUE) @TypeUseAnno0(value=Baz.VALUE) @TypeUseAnno1(value=Baz.VALUE)
            class Baz<@TypeParameterAnno1 @TypeParameterAnno2 X, @TypeParameterAnno2 Y extends @TypeUseAnno2(value=Baz.VALUE) File>
                    extends @TypeUseAnno3(value=Baz.VALUE) ArrayList<@TypeUseAnno4(value=Baz.VALUE) X>
                    implements @TypeUseAnno5(value=Baz.VALUE) Serializable, @TypeUseAnno6(value=Baz.VALUE) List<@TypeUseAnno7(value=Baz.VALUE) X> {
                public static final String VALUE = "foo"
            }
        ''')
        assert bytecode.hasSequence([
                'public class Baz extends java/util/ArrayList implements java/io/Serializable java/util/List groovy/lang/GroovyObject {',
                '@LTypeAnno;(value="foo")',
                '@LTypeUseAnno0;(value="foo")',
                '@LTypeUseAnno1;(value="foo")',
                '@LTypeParameterAnno1;() : CLASS_TYPE_PARAMETER 0, null',
                '@LTypeParameterAnno2;() : CLASS_TYPE_PARAMETER 0, null',
                '@LTypeParameterAnno2;() : CLASS_TYPE_PARAMETER 1, null',
                '@LTypeUseAnno2;(value="foo") : CLASS_TYPE_PARAMETER_BOUND 1, 0, null',
                '@LTypeUseAnno3;(value="foo") : CLASS_EXTENDS -1, null',
                '@LTypeUseAnno4;(value="foo") : CLASS_EXTENDS -1, 0;',
                '@LTypeUseAnno5;(value="foo") : CLASS_EXTENDS 0, null',
                '@LTypeUseAnno6;(value="foo") : CLASS_EXTENDS 1, null',
                '@LTypeUseAnno7;(value="foo") : CLASS_EXTENDS 1, 0;'
        ])
    }
}
