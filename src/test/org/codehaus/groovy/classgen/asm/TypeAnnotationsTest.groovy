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
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno0 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno1 { }

            class Foo {
                class Bar {
                    @TypeAnno0
                    Bar(@TypeAnno1 that) {
                    }
                }
            }
        ''')
        assert bytecode.hasStrictSequence([
                'public <init>(LFoo;Ljava/lang/Object;)V',
              //'@LTypeAnno0;() : METHOD_RETURN', TODO
                '@LTypeAnno1;() : METHOD_FORMAL_PARAMETER 1, null', // TODO: 0
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

    void testTypeAnnotationsForField1() {
        def bytecode = compile(classNamePattern: 'Foo', field: 'foo', imports + '''
            @Retention(RUNTIME) @Target(FIELD) @interface FieldAnno { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno0 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno1 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno2 { }
            @Retention(RUNTIME) @Target(TYPE_USE) @interface TypeAnno3 { }

            class Foo {
                public @FieldAnno Map<
                    @TypeAnno0 ? extends @TypeAnno1 CharSequence,
                    @TypeAnno2 List<@TypeAnno3 ?>
                > foo
            }
        ''')
        assert bytecode.hasSequence([
                'public Ljava/util/Map; foo',
                '@LFieldAnno;()',
                '@LTypeAnno0;() : FIELD, 0;',
                '@LTypeAnno1;() : FIELD, 0;*',
                '@LTypeAnno2;() : FIELD, 1;',
                '@LTypeAnno3;() : FIELD, 1;0;'
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
}
