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

class TypeAnnotationsTest extends AbstractBytecodeTestCase {
    void testTypeAnnotationsForMethod1() {
        assert compile(method: 'foo','''
        import java.lang.annotation.*
        import static java.lang.annotation.RetentionPolicy.RUNTIME
        import static java.lang.annotation.ElementType.*

        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno0 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno1 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno2 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno3 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno4 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno5 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno6 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno7 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno8 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno9 { }

        <@TypeAnno0 X extends @TypeAnno1 Number> @TypeAnno2 List<@TypeAnno3 X> foo(
            @TypeAnno4 List<@TypeAnno5 ? super @TypeAnno6 Map<@TypeAnno7 X, @TypeAnno8 ?>> arg
        ) throws @TypeAnno9 Exception {}
        ''').hasSequence([
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
        assert compile(method: 'foo','''
        import java.lang.annotation.*
        import static java.lang.annotation.RetentionPolicy.RUNTIME
        import static java.lang.annotation.ElementType.*

        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeParameterAnno { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno0 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno1 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno2 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno3 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno4 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno5 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno6 { }

        <@TypeAnno0 @TypeParameterAnno E extends @TypeAnno1 Number & @TypeAnno2 List<@TypeAnno3 E>> void foo(
            @TypeAnno4 E arg1, @TypeAnno5 int arg2, @TypeAnno6 int arg3
        ) {
            // TODO support in code examples below
            try {
                numbers.addAll(new @TypeAnno5 ArrayList<@TypeAnno6 Integer>());
            } catch (@TypeAnno7 Throwable ignore) {
            }
        }
        ''').hasSequence([
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
        assert compile(method: 'get', classNamePattern: 'Foo','''
        import java.lang.annotation.*
        import static java.lang.annotation.RetentionPolicy.RUNTIME
        import static java.lang.annotation.ElementType.*

        @Retention(RUNTIME) @Target([PARAMETER]) @interface ParameterAnno { }
        @Retention(RUNTIME) @Target([TYPE_PARAMETER, TYPE_USE]) @interface TypeUseAndParameterAnno { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno0 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno1 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno2 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno3 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno4 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno5 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno6 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno7 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno8 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno9 { }

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
        ''').hasSequence([
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

    void testTypeAnnotationsForConstructor() {
        assert compile(method: '<init>', classNamePattern: 'HasCons', '''
        import java.lang.annotation.*
        import static java.lang.annotation.RetentionPolicy.RUNTIME
        import static java.lang.annotation.ElementType.*

        @Retention(RUNTIME) @Target([CONSTRUCTOR]) @interface ConstructorAnno { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno0 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno1 { }

        class HasCons {
            @ConstructorAnno @TypeAnno0 @TypeAnno1 HasCons() { }
        }
        ''').hasSequence([
                'public <init>()V',
                '@LConstructorAnno;()',
                '@LTypeAnno0;() : METHOD_RETURN',
                '@LTypeAnno1;() : METHOD_RETURN'
        ])
    }

    void testTypeAnnotationsForField1() {
        assert compile(field: 'documents', classNamePattern: 'Foo', '''
        import java.lang.annotation.*
        import static java.lang.annotation.RetentionPolicy.RUNTIME
        import static java.lang.annotation.ElementType.*

        @Retention(RUNTIME) @Target([FIELD]) @interface FieldAnno { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno0 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno1 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno2 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno3 { }

        class Foo {
            public @FieldAnno Map<@TypeAnno0 ? extends @TypeAnno1 CharSequence, @TypeAnno2 List<@TypeAnno3 ?>> documents = null
        }
        ''').hasSequence([
                'public Ljava/util/Map; documents',
                '@LFieldAnno;()',
                '@LTypeAnno0;() : FIELD, 0;',
                '@LTypeAnno1;() : FIELD, 0;*',
                '@LTypeAnno2;() : FIELD, 1;',
                '@LTypeAnno3;() : FIELD, 1;0;'
        ])
    }

    void testTypeAnnotationsForField2() {
        assert compile(field: 'numbers', classNamePattern: 'Bar','''
        import java.lang.annotation.*
        import static java.lang.annotation.RetentionPolicy.RUNTIME
        import static java.lang.annotation.ElementType.*

        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno4 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno5 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno6 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeAnno7 { }

        class Bar {
            // TODO support type annotations for code (TypeAnno7 below)
            public @TypeAnno4 List<@TypeAnno5 ? super @TypeAnno6 Integer> numbers = Arrays.<@TypeAnno7 Integer>asList(5, 3, 50, 24, 40, 2, 9, 18)
        }
        ''').hasSequence([
                'public Ljava/util/List; numbers',
                '@LTypeAnno4;() : FIELD, null',
                '@LTypeAnno5;() : FIELD, 0;',
                '@LTypeAnno6;() : FIELD, 0;*'
        ])
    }

    void testTypeAnnotationsForClass() {
        assert compile(classNamePattern: 'MyClass','''
        import java.lang.annotation.*
        import java.rmi.Remote
        import static java.lang.annotation.RetentionPolicy.RUNTIME
        import static java.lang.annotation.ElementType.*

        @Retention(RUNTIME) @Target([TYPE]) @interface TypeAnno { }
        @Retention(RUNTIME) @Target([TYPE_PARAMETER]) @interface TypeParameterAnno1 { }
        @Retention(RUNTIME) @Target([TYPE_PARAMETER]) @interface TypeParameterAnno2 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeUseAnno0 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeUseAnno1 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeUseAnno2 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeUseAnno3 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeUseAnno4 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeUseAnno5 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeUseAnno6 { }
        @Retention(RUNTIME) @Target([TYPE_USE]) @interface TypeUseAnno7 { }

        @TypeAnno @TypeUseAnno0 @TypeUseAnno1
        class MyClass<@TypeParameterAnno1 @TypeParameterAnno2 X, @TypeParameterAnno2 Y extends @TypeUseAnno2 File>
                extends @TypeUseAnno3 ArrayList<@TypeUseAnno4 X>
                implements @TypeUseAnno5 Remote, @TypeUseAnno6 List<@TypeUseAnno7 X> { }
        ''').hasSequence([
                'public class MyClass extends java/util/ArrayList implements java/rmi/Remote java/util/List groovy/lang/GroovyObject {',
                '@LTypeAnno;()',
                '@LTypeUseAnno0;()',
                '@LTypeUseAnno1;()',
                '@LTypeParameterAnno1;() : CLASS_TYPE_PARAMETER 0, null',
                '@LTypeParameterAnno2;() : CLASS_TYPE_PARAMETER 0, null',
                '@LTypeParameterAnno2;() : CLASS_TYPE_PARAMETER 1, null',
                '@LTypeUseAnno2;() : CLASS_TYPE_PARAMETER_BOUND 1, 0, null',
                '@LTypeUseAnno3;() : CLASS_EXTENDS -1, null',
                '@LTypeUseAnno4;() : CLASS_EXTENDS -1, 0;',
                '@LTypeUseAnno5;() : CLASS_EXTENDS 0, null',
                '@LTypeUseAnno6;() : CLASS_EXTENDS 1, null',
                '@LTypeUseAnno7;() : CLASS_EXTENDS 1, 0;'
        ])
    }
}
