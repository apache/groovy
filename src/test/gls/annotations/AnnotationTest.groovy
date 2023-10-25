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
package gls.annotations

import groovy.test.NotYetImplemented
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests various properties of annotation definitions.
 */
final class AnnotationTest {

    private final GroovyShell shell = GroovyShell.withConfig {
        imports {
            star 'java.lang.annotation'
            staticStar 'java.lang.annotation.ElementType'
            staticStar 'java.lang.annotation.RetentionPolicy'
        }
    }

    /**
     * Checks that it is possible to annotate an annotation definition with field and method target elements.
     */
    @Test
    void testAnnotateAnnotationDefinitionWithMethodAndFieldTargetElementTypes() {
        shell.parse '''
            @Retention(RUNTIME)
            @Target([METHOD, FIELD])
            @interface MyAnnotation {
            }
        '''
    }

    @Test
    void testCannotAnnotateAnnotationDefinitionIfTargetIsNotOfTypeOrAnnotationType() {
        shouldFail shell, '''
            // all targets except ANNOTATION_TYPE, TYPE, TYPE_PARAMETER, TYPE_USE and MODULE
            @Target([CONSTRUCTOR, METHOD, FIELD, LOCAL_VARIABLE, PACKAGE, PARAMETER])
            @interface AnAnnotation {
            }

            @AnAnnotation
            @interface AnotherAnnotation {
            }
        '''
    }

    /**
     * The {@code @OneToMany} cascade parameter takes an array of CascadeType.
     * To use this annotation in Java with this parameter, you do <code>@OneToMany(cascade = { CascadeType.ALL })</code>
     * In Groovy, you do <code>@OneToMany(cascade = [ CascadeType.ALL ])</code> (brackets instead of braces)
     * But when there's just one value in the array, the curly braces or brackets can be omitted:
     * <code>@OneToMany(cascade = [ CascadeType.ALL ])</code>
     */
    @Test
    void testOmittingBracketsForSingleValueArrayParameter() {
        shell.parse '''
            import gls.annotations.*

            class Book {}

            class Author {
                @OneToMany(cascade = CascadeType.ALL)
                Set<Book> books
            }

            def annotation = Author.class.getDeclaredField('books').annotations[0]

            assert annotation instanceof OneToMany
            assert annotation.cascade() == [CascadeType.ALL]
        '''
    }

    @Test
    void testPrimitiveDefault() {
        // NOTE: for int anything else than a plain number will fail.
        // For example 1l will fail too, even if it could be converted.
        // If this should be changed, then further discussion is needed.
        shouldFail shell, '''
            @interface X {
                int x() default "1" // must be integer
            }
        '''

        shell.parse '''
            @interface X {
                int x() default 1
            }
        '''
    }

    @Test
    void testConstant() {
        assertScript shell, '''
            class Baz {
                // static final int OTHER = 5
                // below we would like to but can't use:
                // constant field expressions, e.g. OTHER, or
                // constant property expressions, e.g. Short.MAX_VALUE
                @Foo(5) void run() {
                    assert Baz.class.getMethod('run').annotations[0].value() == 5
                }
            }

            import java.lang.annotation.*
            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.METHOD)
            @interface Foo {
                int value() default -3
            }

            new Baz().run()
        '''
    }

    // GROOVY-4811
    @Test
    void testArrayDefault() {
        assertScript shell, '''
            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.TYPE)
            @interface Temp {
                String[] bar() default '1' // coerced to list as per Java but must be correct type
            }

            @Temp
            class Bar {}

            assert Bar.getAnnotation(Temp).bar() == ['1']
        '''

        shouldFail shell, '''
            @interface X {
                String[] x() default ["1",2] // list must contain elements of correct type
            }
        '''

        shell.parse '''
            @interface X {
                String[] x() default ["a","b"]
            }
        '''
    }

    @Test
    void testClassDefault() {
        shouldFail shell, '''
            @interface X {
                Class x() default "1" // must be list
            }
        '''

        shell.parse '''
            @interface X {
                Class x() default Number.class // class with .class
            }
        '''

        shell.parse '''
            @interface X {
                Class x() default Number
            }
        '''
    }

    @Test
    void testEnumDefault() {
        shouldFail shell, '''
            @interface X {
                ElementType x() default "1" // must be Enum
            }
        '''

        shouldFail shell, '''
            @interface X {
                ElementType x() default Target.TYPE // must be Enum of correct type
            }
        '''

        shell.parse '''
            @interface X {
                ElementType x() default ElementType.METHOD
            }
        '''
    }

    @Test
    void testAnnotationDefault() {
        shouldFail shell, '''
            @interface X {
                Target x() default "1" // must be Annotation
            }
        '''

        shouldFail shell, '''
            @interface X {
                Target x() default @Retentention // must be correct type
            }
        '''

        shell.parse '''
            @interface X {
                Target x() default @Target([ElementType.TYPE])
            }
        '''
    }

    @Test
    void testSelfReference() {
        shouldFail shell, '''
            @interface X {
                X x() default @X // self reference must not work
            }
        '''
    }

    @Test
    void testCyclicReference() {
        shouldFail shell, '''
            @interface X {
                Y x() default @Y
            }

            @interface Y {
                X x() default @X
            }
        '''
    }

    @Test
    void testParameter() {
        shouldFail shell, '''
            @interface X {
                String x(int x) default "" // annotation members can't have parameters
            }
        '''
    }

    @Test
    void testThrowsClause() {
        shouldFail shell, '''
            @interface X {
                String x() throws IOException // annotation members can't have exceptions
            }
        '''
    }

    @Test
    void testExtends() {
        shouldFail shell, '''
            // annotation members can't extend
            @interface X extends Serializable {
            }
        '''
    }

    @Test
    void testInvalidMemberType() {
        shouldFail shell, '''
            @interface X {
                Object x() // Object is no type for constants
            }
        '''
    }

    @Test
    void testNull() {
        shouldFail shell, '''
            @interface X {
                String x() default null // null is no constant for annotations
            }
        '''
    }

    @Test
    void testUsage() {
        assertScript shell, '''
            // a random annotation type
            @Retention(RetentionPolicy.RUNTIME)
            @interface MyAnnotation {
                String stringValue()
                int intValue()
                int defaultInt() default 1
                String defaultString() default ""
                Class defaultClass() default Integer.class
                ElementType defaultEnum() default ElementType.TYPE
                Target defaultAnnotation() default @Target([ElementType.TYPE])
            }

            @MyAnnotation(stringValue = "for class", intValue = 100)
            class Foo {}

            Annotation[] annotations = Foo.class.annotations
            assert annotations.size() == 1
            MyAnnotation my = annotations[0]
            assert my.stringValue() == "for class"
            assert my.intValue() == 100
            assert my.defaultInt() == 1
            assert my.defaultString() == ""
            assert my.defaultClass() == Integer

            assert my.defaultEnum() == ElementType.TYPE
            assert my.defaultAnnotation() instanceof Target
        '''
    }

    @Test
    void testJavaAnnotationUsageWithGroovyKeyword() {
        assertScript shell, '''
            package gls.annotations

            @JavaAnnotation(in = 3)
            class Foo {}

            Annotation[] annotations = Foo.class.annotations
            assert annotations.size() == 1
            JavaAnnotation my = annotations[0]
            assert my.in() == 3
        '''
    }

    @Test
    void testUsageOnClass() {
        assertScript shell, '''
            @Deprecated
            class Foo{}

            assert Foo.class.annotations.size() == 1
        '''
    }

    @Test
    void testFieldAndPropertyRuntimeRetention() {
        assertScript shell, '''
            import org.codehaus.groovy.ast.ClassNode

            @Retention(RetentionPolicy.RUNTIME)
            @interface Annotation1 {}

            @Annotation1 class A {
                @Annotation1 method1(){}
                @Annotation1 public field1
                @Annotation1 prop1
            }

            new ClassNode(A).with {
                assert annotations: "ClassNode for class 'A' has an annotation as it should"
                getMethod('method1').with {
                    assert annotations: "Annotation on 'method1' not found"
                }
                getField('field1').with {
                    assert annotations: "Annotation on 'field1' not found"
                }
                getField('prop1').with {
                    assert annotations: "Annotation on 'property1' not found"
                }
            }
        '''
    }

    @Test
    void testSingletonArrayUsage() {
        assertScript shell, '''
            // a random annnotation type
            @Retention(RetentionPolicy.RUNTIME)
            @interface MyAnnotation {
                String[] things()
            }

            @MyAnnotation(things = "x")
            class Foo {}

            Annotation[] annotations = Foo.class.annotations
            assert annotations.size() == 1
            MyAnnotation my = annotations[0]
            assert my.things().size() == 1
            assert my.things()[0] == "x"
        '''
    }

    @Test
    void testSettingAnnotationMemberTwice() {
        shouldFail shell, '''
            package gls.annotations

            @JavaAnnotation(in = 1, in = 2)
            class Foo {}
        '''
    }

    @Test
    void testGetterCallWithSingletonAnnotation() {
        assertScript shell, '''
            @Singleton class MyService3410{}
            assert MyService3410.instance != null
            assert MyService3410.getInstance() != null
        '''
    }

    @Test
    void testAttributeValueConstants1() {
        assertScript shell, '''
            import static Constants.*

            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.FIELD)
            @interface Anno {
                double value() default 0.0d
                String[] array() default []
            }

            class Constants {
                public static final String BAR = "bar"
                public static final APPROX_PI = 3.14d
            }

            interface IConstants {
                String FOO = "foo"
            }

            class ClassWithAnnotationUsingConstant {
                @Anno(array = [IConstants.FOO, BAR, groovy.inspect.Inspector.GROOVY])
                public annotatedStrings

                @Anno(Math.PI)
                public annotatedMath1
                @Anno(APPROX_PI)
                public annotatedMath2
            }

            assert ClassWithAnnotationUsingConstant.getDeclaredField('annotatedStrings').annotations[0].array() == ['foo', 'bar', "GROOVY"]
            assert ClassWithAnnotationUsingConstant.getDeclaredField('annotatedMath1').annotations[0].value() == Math.PI
            assert ClassWithAnnotationUsingConstant.getDeclaredField('annotatedMath2').annotations[0].value() == Constants.APPROX_PI
        '''
    }

    @Test
    void testAttributeValueConstants2() {
        assertScript shell, '''
            import static Constants.*

            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.FIELD)
            @interface Outer {
                Inner value()
                String[] array() default []
            }

            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.FIELD)
            @interface Inner {
                String[] value() default []
            }

            class Constants {
                public static final String BAR = "bar"
                public static final String BAZ = "baz"
            }

            interface IConstants {
                String FOO = "foo"
                String FOOBAR = "foobar"
            }

            class ClassWithNestedAnnotationUsingConstant {
                @Outer(value = @Inner([IConstants.FOOBAR, BAR, groovy.inspect.Inspector.GROOVY]),
                       array = [IConstants.FOO, groovy.inspect.Inspector.GROOVY, BAZ])
                public outer
            }

            assert ClassWithNestedAnnotationUsingConstant.getDeclaredField('outer').annotations[0].array() == ['foo', 'GROOVY', 'baz']
            assert ClassWithNestedAnnotationUsingConstant.getDeclaredField('outer').annotations[0].value().value() == ['foobar', 'bar', 'GROOVY']
        '''
    }

    // GROOVY-3278
    @Test
    void testAttributeValueConstants3() {
        assertScript shell, '''
            import gls.annotations.*

            @ConstAnnotation(ints = 42)
            class Child1 extends Base3278 {}

            class OtherConstants {
                static final Integer CONST3 = 3278
            }

            @ConstAnnotation(ints = [-1, Base3278.CONST, Base3278.CONST1, Base3278.CONST2, OtherConstants.CONST3, Integer.MIN_VALUE],
                          strings = ['foo', Base3278.CONST4, Base3278.CONST5, Base3278.CONST5 + 'bing'])
            class Child3 extends Base3278 {}

            assert new Child1().ints() == [42]
            assert new Child2().ints() == [2147483647]
            new Child3().with {
                assert ints() == [-1, 3278, 2048, 2070, 3278, -2147483648]
                assert strings() == ['foo', 'foobar', 'foobarbaz', 'foobarbazbing']
            }
        '''
    }

    // GROOVY-8898
    @Test
    void testAttributeValueConstants4() {
        assertScript shell, '''
            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.TYPE)
            @interface MyAnnotation {
                String[] groups() default []
                MyEnum alt() default MyEnum.ALT1
            }

            class Base {
                static final String CONST = 'bar'
                def groups() { getClass().annotations[0].groups() }
                def alt() { getClass().annotations[0].alt() }
            }

            enum MyEnum {
                ALT1, ALT2;
                public static final String CONST = 'baz'
            }

            @MyAnnotation(groups = ['foo', Base.CONST, MyEnum.CONST], alt = MyEnum.ALT2)
            class Child extends Base {}

            new Child().with {
                assert groups() == ['foo', 'bar', 'baz']
                assert alt() == MyEnum.ALT2
            }
        '''
    }

    @Test
    void testAttributeValueConstants5() {
        assertScript shell, '''
            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.FIELD)
            @interface Pattern {
                String regexp()
            }

            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.FIELD)
            @interface LimitedDouble {
                double max() default Double.MAX_VALUE
                double min() default Double.MIN_VALUE
                double zero() default (1.0 - 1.0d) * 1L
            }

            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.FIELD)
            @interface IntegerConst {
                int kilo() default 0b1 << 10
                int two() default 64 >> 5
                int nine() default 0b1100 ^ 0b101
                int answer() default 42
            }

            class MyClass {
                private static interface Constants {
                    static final String CONST = 'foo' + 'bar'
                }

                @Pattern(regexp=Constants.CONST)
                String myString

                @LimitedDouble(min=0.0d, max=50.0d * 2)
                double myDouble

                @IntegerConst(answer=(5 ** 2) * (2 << 1))
                int myInteger
            }

            assert MyClass.getDeclaredField('myString').annotations[0].regexp() == 'foobar'

            MyClass.getDeclaredField('myDouble').annotations[0].with {
                assert max() == 100.0d
                assert min() == 0.0d
                assert zero() == 0.0d
            }

            MyClass.getDeclaredField('myInteger').annotations[0].with {
                assert kilo() == 1024
                assert two() == 2
                assert nine() == 9
                assert answer() == 100
            }
        '''
    }

    // GROOVY-10068
    @Test
    void testAttributeValueConstants6() {
        assertScript shell, '''
            @interface A {
                short value()
            }
            class C {
                public static final short ONE = 1
            }

            @A(C.ONE)
            def local
        '''
    }

    // GROOVY-7252
    @Test
    void testAttributeValueConstants7() {
        assertScript shell, '''
            @interface A {
                short value()
            }

            @A(12345)
            def local
        '''
    }

    // GROOVY-9366
    @Test
    void testAttributeValueConstants8() {
        assertScript shell, '''
            @interface A {
                byte value()
            }

            @A(0xFF)
            def local
        '''
    }

    // GROOVY-9206
    @Test
    void testAttributeValueConstants9() {
        assertScript shell, '''
            @interface A {
                char value()
            }

            @A( 'c' )
            def local
        '''
    }

    // GROOVY-10750
    @Test
    void testAttributeValueConstants10() {
        assertScript shell, '''
            @Retention(RUNTIME)
            @interface Foo {
                String value()
            }
            @groovy.transform.CompileStatic
            @Foo(BarController.BASE_URL)
            class BarController {
                public static final String BASE_URL = '/bars'
            }
            @groovy.transform.CompileStatic
            @Foo(BazController.BASE_URL)
            class BazController {
                public static final String BASE_URL = BarController.BASE_URL + '/{barId}/bazs'
            }

            def foo = BazController.getAnnotation(Foo)
            assert foo.value() == '/bars/{barId}/bazs'
        '''
    }

    // GROOVY-11206
    @Test
    void testAttributeValueConstants11() {
        assertScript shell, '''
            @Retention(RUNTIME)
            @interface Foo {
                String value()
            }
            @groovy.transform.CompileStatic
            @Foo(BarController.SOME_URL)
            class BarController {
                public static final String BASE_URL = '/bars'
                public static final String SOME_URL = BASE_URL + '/{barId}/bazs'
            }

            def foo = BarController.getAnnotation(Foo)
            assert foo.value() == '/bars/{barId}/bazs'
        '''
    }

    // GROOVY-11207
    @Test
    void testAttributeValueConstants12() {
        assertScript shell, '''
            @Retention(RUNTIME)
            @interface Foo {
                String value()
            }
            @groovy.transform.CompileStatic
            class Bar {
                public static final String BASE = 'base'
                @Foo('all your ' + BASE)
                def baz() {
                }
            }

            def foo = Bar.getMethod('baz').getAnnotation(Foo)
            assert foo.value() == 'all your base'
        '''
    }

    @Test
    void testRuntimeRetentionAtAllLevels() {
        assertScript shell, '''
            @Retention(RetentionPolicy.RUNTIME)
            @Target([ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.CONSTRUCTOR, ElementType.FIELD])
            @interface MyAnnotation {
                String value() default ""
            }

            @MyAnnotation('class')
            class MyClass {
                @MyAnnotation('field')
                public myField

                @MyAnnotation('constructor')
                MyClass(@MyAnnotation('constructor param') arg){}

                @MyAnnotation('method')
                def myMethod(@MyAnnotation('method param') arg) {}
            }

            def c1 = new MyClass()
            assert c1.class.annotations[0].value() == 'class'

            def field = c1.class.fields.find{ it.name == 'myField' }
            assert field.annotations[0].value() == 'field'

            def method = c1.class.methods.find{ it.name == 'myMethod' }
            assert method.annotations[0].value() == 'method'
            assert method.parameterAnnotations[0][0].value() == 'method param'

            def constructor = c1.class.constructors[0]
            assert constructor.annotations[0].value() == 'constructor'
            assert constructor.parameterAnnotations[0][0].value() == 'constructor param'
        '''
    }

    @Test
    void testAnnotationWithValuesNotGivenForAttributesWithoutDefaults() {
        // compiler should not allow this, because there is no default value for x
        shouldFail shell, '''
            @Retention(RetentionPolicy.RUNTIME)
            @interface Annot3454V1 {
                String x()
            }

            @Annot3454V1
            class Bar {}
        '''

        // compiler should not allow this, because there is no default value for y
        shouldFail shell, '''
            @Retention(RetentionPolicy.RUNTIME)
            @interface Annot3454V2 {
                String x() default 'xxx'
                String y()
            }

            @Annot3454V2
            class Bar {}
        '''

        // compiler should allow this, because there is default value for x and value provided for y
        assertScript shell, '''
            @Retention(RetentionPolicy.RUNTIME)
            @interface Annot3454V3 {
                String x() default 'xxx'
                String y()
            }

            @Annot3454V3(y = 'yyy')
            class Bar {}

            def anno = Bar.class.getAnnotation(Annot3454V3)
            assert anno.x() == 'xxx'
            assert anno.y() == 'yyy'
        '''
    }

    @Test
    void testAllowedTargetsCheckAlsoWorksForAnnotationTypesDefinedOutsideThisCompilationUnit() {
        shell.parse '''
            @Documented
            @interface Foo {
            }
        '''

        shouldFail shell, '''
            @Documented
            class Foo {
                def bar
            }
        '''

        shouldFail shell, '''
            class Foo {
                @Documented
                def bar
            }
        '''

        shell.parse '''
            import org.junit.*

            class Foo {
                @Test
                void foo() {}
            }
        '''

        shouldFail shell, '''
            import org.junit.*

            @Test
            class Foo {
                void foo() {}
            }
        '''
    }

    // GROOVY-6025
    @Test
    void testAnnotationDefinitionDefaultValues() {
        assertScript shell, '''
            @Retention(RetentionPolicy.RUNTIME)
            @Target([ElementType.TYPE, ElementType.METHOD])
            public @interface Foo {
                int i1() default 0
                int i2() default (int)1
                short s1() default 2
                short s2() default (short)3
                short s3() default (Short)4
                short s4() default 5 as short
                byte b() default 6
                char c1() default 65
                char c2() default 'B'
                char c3() default 'C' as char
                char c4() default (char)'D'
                float f1() default 1.0
                float f2() default 1.1f
                float f3() default (float)1.2
                float f4() default 1.3 as float
                double d1() default 2.0
                double d2() default 2.1d
                double d3() default (double)2.2
                double d4() default 2.3 as double
            }

            @Foo method() {}
            def string = getClass().getMethod('method').getAnnotation(Foo).toString()[5..-2].tokenize(', ').sort().join('|')
            assert string == 'b=6|c1=A|c2=B|c3=C|c4=D|d1=2.0|d2=2.1|d3=2.2|d4=2.3|f1=1.0|f2=1.1|f3=1.2|f4=1.3|i1=0|i2=1|s1=2|s2=3|s3=4|s4=5' ||
                    // changed in some jdk9 versions
                   string == "b=6|c1='A'|c2='B'|c3='C'|c4='D'|d1=2.0|d2=2.1|d3=2.2|d4=2.3|f1=1.0f|f2=1.1f|f3=1.2f|f4=1.3f|i1=0|i2=1|s1=2|s2=3|s3=4|s4=5" ||
                   // changed in some jdk14 versions
                   string == "b=(byte)0x06|c1='A'|c2='B'|c3='C'|c4='D'|d1=2.0|d2=2.1|d3=2.2|d4=2.3|f1=1.0f|f2=1.1f|f3=1.2f|f4=1.3f|i1=0|i2=1|s1=2|s2=3|s3=4|s4=5"
        '''
    }

    // GROOVY-6025
    @NotYetImplemented @Test
    void testAnnotationDefinitionDefaultValues2() {
        assertScript shell, '''
            @interface A {
                short s1() default (byte)1
                short s2() default (char)2
                short s3() default (long)3
            }

            assert A.getMethod('s1').defaultValue == 1
            assert A.getMethod('s2').defaultValue == 2
            assert A.getMethod('s3').defaultValue == 3
        '''
    }

    // GROOVY-9205
    @Test
    void testAnnotationDefinitionDefaultValues3() {
        assertScript shell, '''
            @interface A {
                int i1() default (int) 42
                int i2() default 42 as int
                byte b1() default (byte) 42
                byte b2() default 42 as byte
                char c1() default (char) 42
                char c2() default 42 as char
                long l1() default (long) 42
                long l2() default 42 as long
                short s1() default (short) 42
                short s2() default 42 as short
            }

            ['i1', 'i2', 'b1', 'b2', 'c1', 'c2', 'l1', 'l2', 's1', 's2'].each { name ->
                assert A.getMethod(name).defaultValue == 42
            }
        '''
    }

    // GROOVY-9205
    @Test
    void testAnnotationDefinitionDefaultValues4() {
        assertScript shell, '''
            @interface A {
                float f1() default (float) 42
                float f2() default 42 as float
                double d1() default (double) 42
                double d2() default 42 as double
            }

            ['f1', 'f2', 'd1', 'd2'].each { name ->
                assert A.getMethod(name).defaultValue == 42.0
            }
        '''
    }

    // GROOVY-6093
    @Test
    void testAnnotationOnEnumConstant() {
        assertScript shell, '''
            import gls.annotations.XmlEnum
            import gls.annotations.XmlEnumValue

            @XmlEnum
            enum GroovyEnum {
                @XmlEnumValue('good') BAD
            }

            assert GroovyEnum.class.getField('BAD').isAnnotationPresent(XmlEnumValue)
        '''
    }

    // GROOVY-7151
    @Test
    void testAnnotateAnnotationDefinitionWithAnnotationWithTypeTarget() {
        shell.parse codeWithMetaAnnotationWithTarget('TYPE')
    }

    void testAnnotateAnnotationDefinitionWithAnnotationWithAnnotationTypeTarget() {
        shell.parse codeWithMetaAnnotationWithTarget('ANNOTATION_TYPE')
    }

    private static String codeWithMetaAnnotationWithTarget(targetElementTypeName) {
        return """
            import java.lang.annotation.*
            import static java.lang.annotation.RetentionPolicy.*
            import static java.lang.annotation.ElementType.*

            @Retention(RUNTIME)
            @Target(${targetElementTypeName})
            @interface Import {}

            @Retention(RUNTIME)
            @Target([FIELD])
            @Import
            @interface EnableFeature { }
        """
    }

    // GROOVY-7806
    @Test
    void testNewlinesAllowedBeforeBlock() {
        shell.parse '''
            @interface ANNOTATION_A
            {
            }
        '''
    }

    // GROOVY-8226
    @Test
    void testAnnotationOnParameterType() {
        assertScript shell, '''
            @Target([PARAMETER, FIELD, METHOD, ANNOTATION_TYPE, TYPE_USE])
            @Retention(RUNTIME)
            public @interface NonNull { }

            class Foo {
                @NonNull public Integer foo
                @NonNull Integer bar(@NonNull String baz) {}
            }

            def expected = '@NonNull()'
            def foo = Foo.getField('foo')
            assert foo.annotations[0].toString() == expected
            def bar = Foo.getMethod('bar', String)
            assert bar.annotations[0].toString() == expected
            def baz = bar.parameters[0]
            assert baz.annotations[0].toString() == expected
        '''
    }

    // GROOVY-11178
    @Test
    void testAnnotationOnConstructorType() {
        assertScript shell, '''package p
            @Target(TYPE_USE)
            @interface Tag {}

            @groovy.transform.ASTTest(phase=CLASS_GENERATION, value={
                def cce = node.rightExpression
                assert cce.type.typeAnnotations.size() == 1
                assert cce.type.typeAnnotations[0].classNode.name == 'p.Tag'
            })
            Object o = new @Tag Object()
        '''

        def err = shouldFail shell, '''\
            @Target(PARAMETER)
            @interface Tag {}

            Object o = new @Tag Object()
        '''
        assert err =~ /Annotation @Tag is not allowed on element TYPE/
    }

    // GROOVY-9155
    @Test
    void testAnnotationOnTypeArgumentType() {
        def err = shouldFail shell, '''package p
            @Target(TYPE_USE)
            @interface Tag {}

            def m(List<@Tag(foo="") String> strings) {
            }
        '''
        assert err =~ /'foo' is not part of the annotation Tag in @p.Tag/
    }

    // GROOVY-8234
    @Test
    void testAnnotationWithRepeatableSupported() {
        assertScript shell, '''
            class MyClass {
                // TODO confirm the JDK9 behavior is what we expect
                private static final List<String> expected = [
                    '@MyAnnotationArray(value=[@MyAnnotation(value=val1), @MyAnnotation(value=val2)])',    // JDK5-8
                    '@MyAnnotationArray(value={@MyAnnotation(value="val1"), @MyAnnotation(value="val2")})', // JDK9
                    '@MyAnnotationArray({@MyAnnotation("val1"), @MyAnnotation("val2")})' // JDK14
                ]

                // control
                @MyAnnotationArray([@MyAnnotation("val1"), @MyAnnotation("val2")])
                String method1() { 'method1' }

                // duplicate candidate for auto collection
                @MyAnnotation(value = "val1")
                @MyAnnotation(value = "val2")
                String method2() { 'method2' }

                // another control (okay to mix one uncontained with one explicit container)
                @MyAnnotationArray([@MyAnnotation("val1"), @MyAnnotation("val2")])
                @MyAnnotation(value = "val3")
                String method3() { 'method3' }

                static void main(String... args) {
                    MyClass myc = new MyClass()
                    assert 'method1' == myc.method1()
                    assert 'method2' == myc.method2()
                    assert expected.contains(checkAnnos(myc, "method1"))
                    assert expected.contains(checkAnnos(myc, "method2"))
                    assert 'method3' == myc.method3()
                    def m3 = myc.getClass().getMethod('method3')
                    assert m3.getAnnotationsByType(MyAnnotation).size() == 3
                }

                private static String checkAnnos(MyClass myc, String name) {
                    def m = myc.getClass().getMethod(name)
                    List annos = m.getAnnotations()
                    assert annos.size() == 1
                    annos[0].toString()
                }
            }

            @Target(ElementType.METHOD)
            @Retention(RetentionPolicy.RUNTIME)
            @Repeatable(MyAnnotationArray)
            @interface MyAnnotation {
                String value() default "val0"
            }

            @Retention(RetentionPolicy.RUNTIME)
            @interface MyAnnotationArray {
                MyAnnotation[] value()
            }
        '''
    }

    @Test
    void testAnnotationWithRepeatableSupported2() {
        for (policy in ['CLASS', 'SOURCE']) {
            assertScript shell, """
                @Retention(RetentionPolicy.$policy)
                @Repeatable(B)
                @interface A {
                    String value() default "foo"
                }

                @Retention(RetentionPolicy.$policy)
                @interface B {
                }

                @A @A('bar')
                class C {
                }

                // not available at run-time
                assert C.getAnnotationsByType(A).length == 0
                assert C.getAnnotationsByType(B).length == 0
            """
        }
    }

    // GROOVY-9452
    @Test
    void testDuplicationAnnotationOnClassWithParams() {
        def err = shouldFail shell, '''
            @Target(ElementType.TYPE)
            @Retention(RetentionPolicy.RUNTIME)
            @Repeatable(As)
            @interface A {
                String value()
            }

            @Retention(RetentionPolicy.RUNTIME)
            @interface As {
                A[] value()
            }

            @A("a")
            @A("b")
            @As([@A("c")])
            class Foo {}
        '''
        assert err =~ /Cannot specify duplicate annotation/
    }

    // GROOVY-7033
    @Test
    void testClassLiteralsRecognizedForAnonymousInnerClassAnnotationUsage() {
        shell.parse '''
            @interface A {
                Class value()
            }

            def obj = new Object() {
                @A(String) def field
                @A(String) @Override
                boolean equals(@A(String) param) {
                    def type = String
                }
            }
        '''
    }

    @Test
    void testVariableExpressionsReferencingConstantsSeenForAnnotationAttributes() {
        shell.parse '''
            class C {
                public static final String VALUE = 'rawtypes'
                @SuppressWarnings(VALUE)
                def method() { }
            }
        '''
    }

    @Test
    void testAnnotationRetentionMirrorsJava() {
        assertScript shell, '''
            for (retention in ['', '@Retention(SOURCE)', '@Retention(CLASS)', '@Retention(RUNTIME)']) {
                def src = """
                    import java.lang.annotation.Retention;
                    import static java.lang.annotation.RetentionPolicy.*;
                    $retention
                    @interface MyAnnotation {}
                """
                def mag = new GroovyClassLoader().parseClass src
                def maj = new org.apache.groovy.util.JavaShell().compile 'MyAnnotation', src
                assert mag.annotations == maj.annotations
            }
        '''
    }

    @Test
    void testAnnotationWithRepeatableSupportedPrecompiledJava() {
        assertScript shell, '''
            import gls.annotations.*

            class MyClass {
                // TODO confirm the JDK9 behavior is what we expect
                private static final List<String> expected = [
                    '@gls.annotations.Requires(value=[@gls.annotations.Require(value=val1), @gls.annotations.Require(value=val2)])',    // JDK5-8
                    '@gls.annotations.Requires(value={@gls.annotations.Require(value="val1"), @gls.annotations.Require(value="val2")})', // JDK9
                    '@gls.annotations.Requires({@gls.annotations.Require("val1"), @gls.annotations.Require("val2")})' // JDK14
                ]

                // control
                @Requires([@Require("val1"), @Require("val2")])
                String method1() { 'method1' }

                // duplicate candidate for auto collection
                @Require(value = "val1")
                @Require(value = "val2")
                String method2() { 'method2' }

                static void main(String... args) {
                    MyClass myc = new MyClass()
                    assert 'method1' == myc.method1()
                    assert 'method2' == myc.method2()
                    assert expected.contains(checkAnnos(myc, "method1"))
                    assert expected.contains(checkAnnos(myc, "method2"))
                }

                private static String checkAnnos(MyClass myc, String name) {
                    def m = myc.getClass().getMethod(name)
                    List annos = m.getAnnotations()
                    assert annos.size() == 1
                    annos[0].toString()
                }
            }
        '''
    }

    // GROOVY-10857
    @Test
    void testAnnotationWithCircularReference() {
        assertScript shell, '''
            @A @Documented @Retention(RUNTIME) @Target(TYPE)
            @interface A {
            }
            @A @Documented @Retention(RUNTIME) @Target([FIELD,METHOD,PARAMETER])
            @interface B {
            }
            interface I<T> {
                @B T m()
            }
            def obj = new I<Object>() {
                def m() { return 0 }
            }
            assert obj.m() == 0
        '''
    }
}
