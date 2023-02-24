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
package org.codehaus.groovy.classgen

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.RecordComponentNode
import org.codehaus.groovy.ast.decompiled.AsmDecompiler
import org.codehaus.groovy.ast.decompiled.AsmReferenceResolver
import org.codehaus.groovy.ast.decompiled.DecompiledClassNode
import org.codehaus.groovy.control.ClassNodeResolver
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.isAtLeastJdk
import static groovy.test.GroovyAssert.shouldFail
import static org.junit.Assume.assumeTrue

final class RecordTest {

    @Test
    void testNativeRecordOnJDK16_groovy() {
        assumeTrue(isAtLeastJdk('16.0'))

        assertScript shell, '''
            import java.lang.annotation.*
            import java.lang.reflect.RecordComponent

            @Retention(RetentionPolicy.RUNTIME)
            @Target([ElementType.RECORD_COMPONENT])
            @interface NotNull {}

            @Retention(RetentionPolicy.RUNTIME)
            @Target([ElementType.RECORD_COMPONENT, ElementType.TYPE_USE])
            @interface NotNull2 {}

            @Retention(RetentionPolicy.RUNTIME)
            @Target([ElementType.TYPE_USE])
            @interface NotNull3 {}

            record Person(@NotNull @NotNull2 String name, int age, @NotNull2 @NotNull3 List<String> locations, String[] titles) {
            }

            RecordComponent[] rcs = Person.getRecordComponents()
            assert rcs.length == 4

            assert rcs[0].name == 'name' && rcs[0].type == String
            Annotation[] annotations = rcs[0].getAnnotations()
            assert annotations.length == 2
            assert annotations[0].annotationType() == NotNull
            assert annotations[1].annotationType() == NotNull2
            def typeAnnotations = rcs[0].getAnnotatedType().getAnnotations()
            assert typeAnnotations.length == 1
            assert typeAnnotations[0].annotationType() == NotNull2

            assert rcs[1].name == 'age' && rcs[1].type == int

            assert rcs[2].name == 'locations' && rcs[2].type == List
            assert rcs[2].genericSignature == 'Ljava/util/List<Ljava/lang/String;>;'
            assert rcs[2].genericType.toString() == 'java.util.List<java.lang.String>'
            def annotations2 = rcs[2].getAnnotations()
            assert annotations2.length == 1
            assert annotations2[0].annotationType() == NotNull2
            def typeAnnotations2 = rcs[2].getAnnotatedType().getAnnotations()
            assert typeAnnotations2.length == 2
            assert typeAnnotations2[0].annotationType() == NotNull2
            assert typeAnnotations2[1].annotationType() == NotNull3

            assert rcs[3].name == 'titles' && rcs[3].type == String[]
        '''
    }

    @Test
    void testNativeRecordOnJDK16_java() {
        assumeTrue(isAtLeastJdk('16.0'))

        def sourceDir = File.createTempDir()
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: true]
        )
        try {
            def a = new File(sourceDir, 'Person.java')
            a.write '''
                import java.lang.annotation.*;
                import java.util.*;

                public record Person(@NotNull @NotNull2 @NotNull3 String name, int age, @NotNull2 @NotNull3 List<String> locations, String[] titles) {}

                @Retention(RetentionPolicy.RUNTIME)
                @Target({ElementType.RECORD_COMPONENT})
                @interface NotNull {}

                @Retention(RetentionPolicy.RUNTIME)
                @Target({ElementType.RECORD_COMPONENT, ElementType.TYPE_USE})
                @interface NotNull2 {}

                @Retention(RetentionPolicy.RUNTIME)
                @Target({ElementType.TYPE_USE})
                @interface NotNull3 {}
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a)
            cu.compile()

            def personClass   = loader.loadClass('Person')
            def notNullClass  = loader.loadClass('NotNull')
            def notNull2Class = loader.loadClass('NotNull2')
            def notNull3Class = loader.loadClass('NotNull3')

            def personComponents = personClass.recordComponents
            assert personComponents.size() == 4

            assert personComponents[0].name == 'name'
            assert personComponents[0].type == String
            def annotations = personComponents[0].annotations
            assert annotations.size() == 2
            assert annotations[0].annotationType() == notNullClass
            assert annotations[1].annotationType() == notNull2Class
            def typeAnnotations = personComponents[0].annotatedType.annotations
            assert typeAnnotations.size() == 2
            assert typeAnnotations[0].annotationType() == notNull2Class
            assert typeAnnotations[1].annotationType() == notNull3Class

            assert personComponents[1].name == 'age'       && personComponents[1].type == int
            assert personComponents[2].name == 'locations' && personComponents[2].type == List
            assert personComponents[3].name == 'titles'    && personComponents[3].type == String[]

            assert personComponents[2].annotations.size() == 1
            assert personComponents[2].annotations[0].annotationType() == notNull2Class
            assert personComponents[2].genericSignature == 'Ljava/util/List<Ljava/lang/String;>;'
            assert personComponents[2].genericType.toString() == 'java.util.List<java.lang.String>'

            typeAnnotations = personComponents[2].annotatedType.annotations
            assert typeAnnotations.size() == 2
            assert typeAnnotations[0].annotationType() == notNull2Class
            assert typeAnnotations[1].annotationType() == notNull3Class

            def personClassNode   = ClassHelper.make(personClass)
            def notNullClassNode  = ClassHelper.make(notNullClass)
            def notNull2ClassNode = ClassHelper.make(notNull2Class)
            def notNull3ClassNode = ClassHelper.make(notNull3Class)

            def checkNativeRecordClassNode = { ClassNode classNode ->
                assert classNode.isRecord()
                assert classNode.recordComponents.size() == 4
                classNode.recordComponents.eachWithIndex { RecordComponentNode rcn, Integer index ->
                    switch (index) {
                      case 0:
                        assert rcn.name == 'name'
                        assert rcn.type == ClassHelper.STRING_TYPE
                        assert rcn.type !== ClassHelper.STRING_TYPE // GROOVY-10937
                        assert rcn.annotations.size() == 2
                        assert rcn.annotations[0].classNode == notNullClassNode
                        assert rcn.annotations[1].classNode == notNull2ClassNode
                        assert rcn.type.typeAnnotations.size() == 2
                        assert rcn.type.typeAnnotations[0].classNode == notNull2ClassNode
                        assert rcn.type.typeAnnotations[1].classNode == notNull3ClassNode
                        break
                      case 1:
                          assert rcn.name == 'age'
                          assert rcn.type == ClassHelper.int_TYPE
                        break
                      case 2:
                        assert rcn.name == 'locations'
                        assert rcn.type == ClassHelper.LIST_TYPE
                        assert rcn.type !== ClassHelper.LIST_TYPE
                        assert rcn.type.genericsTypes.size() == 1
                        assert rcn.type.genericsTypes[0].type == ClassHelper.STRING_TYPE
                        assert rcn.annotations.size() == 1
                        assert rcn.annotations[0].classNode == notNull2ClassNode
                        assert rcn.type.typeAnnotations.size() == 2
                        assert rcn.type.typeAnnotations[0].classNode == notNull2ClassNode
                        assert rcn.type.typeAnnotations[1].classNode == notNull3ClassNode
                        break
                      case 3:
                        assert rcn.name == 'titles'
                        assert rcn.type == ClassHelper.STRING_TYPE.makeArray()
                    }
                }
            }

            checkNativeRecordClassNode(personClassNode)

            def stub = AsmDecompiler.parseClass(loader.getResource(personClass.getName().replace('.', '/') + '.class'))
            def resolver = new AsmReferenceResolver(new ClassNodeResolver(), new CompilationUnit(loader))
            def personDecompiledClassNode = new DecompiledClassNode(stub, resolver)
            checkNativeRecordClassNode(personDecompiledClassNode)
        } finally {
            sourceDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }

    //--------------------------------------------------------------------------

    private final GroovyShell shell = GroovyShell.withConfig {
        imports {
            star 'groovy.transform'
            staticStar 'java.lang.reflect.Modifier'
            staticMember 'groovy.test.GroovyAssert', 'shouldFail'
            staticMember 'org.codehaus.groovy.ast.ClassHelper', 'make'
        }
    }

    @Test
    void testNativeRecordOnJDK16ByDefault() {
        assumeTrue(isAtLeastJdk('16.0'))

        assertScript shell, '''
            record Person(String name) {}
            assert Person.superclass == java.lang.Record
        '''
    }

    @Test
    void testRecordLikeOnJDK16withTargetBytecode15() {
        assumeTrue(isAtLeastJdk('16.0'))

        shell.@config.targetBytecode = '15'
        assertScript shell, '''
            record Person(String name) {}
            assert Person.superclass != java.lang.Record
        '''
    }

    @Test
    void testAttemptedNativeRecordWithTargetBytecode15ShouldFail() {
        assumeTrue(isAtLeastJdk('16.0'))

        shell.@config.targetBytecode = '15'
        def err = shouldFail shell, '''
            @RecordType(mode=RecordTypeMode.NATIVE)
            class Person {
                String name
            }
        '''
        assert err.message.contains('Expecting JDK16+ but found 15 when attempting to create a native record')
    }

    @Test
    void testNativeRecordWithSuperClassShouldFail() {
        assumeTrue(isAtLeastJdk('16.0'))

        def err = shouldFail shell, '''
            @RecordType
            class Person extends ArrayList {
                String name
            }
        '''
        assert err.message.contains('Invalid superclass for native record found: java.util.ArrayList')
    }

    @Test
    void testNonNativeRecordOnJDK16WhenEmulating() {
        assumeTrue(isAtLeastJdk('16.0'))

        assertScript shell, '''
            @RecordOptions(mode=RecordTypeMode.EMULATE)
            record Person(String name) {
            }
            assert Person.superclass != java.lang.Record
        '''
    }

    @Test
    void testRecordsDefaultParams() {
        assertScript shell, '''
            record Bar (String a = 'a', long b, Integer c = 24, short d, String e = 'e') {
            }

            short one = 1
            assert new Bar(3L, one).toString() == 'Bar[a=a, b=3, c=24, d=1, e=e]'
            assert new Bar('A', 3L, one).toString() == 'Bar[a=A, b=3, c=24, d=1, e=e]'
            assert new Bar('A', 3L, 42, one).toString() == 'Bar[a=A, b=3, c=42, d=1, e=e]'
            assert new Bar('A', 3L, 42, one, 'E').toString() == 'Bar[a=A, b=3, c=42, d=1, e=E]'
        '''
    }

    @Test
    void testInnerRecordIsImplicitlyStatic() {
        assertScript shell, '''
            class Test {
                record Point(int i, int j) {
                }
            }
            assert isStatic(Test$Point.modifiers)
        '''
    }

    @Test
    void testRecordWithDefaultParams() {
        assertScript shell, '''
            record Point(int i = 5, int j = 10) {
            }
            assert new Point().toString() == 'Point[i=5, j=10]'
            assert new Point(50).toString() == 'Point[i=50, j=10]'
            assert new Point(50, 100).toString() == 'Point[i=50, j=100]'
            assert new Point([:]).toString() == 'Point[i=5, j=10]'
            assert new Point(i: 50).toString() == 'Point[i=50, j=10]'
            assert new Point(j: 100).toString() == 'Point[i=5, j=100]'
            assert new Point(i: 50, j: 100).toString() == 'Point[i=50, j=100]'
        '''
    }

    @Test
    void testRecordWithDefaultParamsAndMissingRequiredParam() {
        assertScript shell, '''
            record Point(int i = 5, int j, int k = 10) {
            }
            assert new Point(j: 100).toString() == 'Point[i=5, j=100, k=10]'
            def err = shouldFail {
                new Point(i: 50)
            }
            assert err.message.contains("Missing required named argument 'j'")
        '''
    }

    @Test
    void testBinaryRecordClassNode() {
        assumeTrue(isAtLeastJdk('16.0'))

        assertScript shell, '''
            def cn = make(jdk.net.UnixDomainPrincipal)
            assert cn.isRecord()
            def rcns = cn.getRecordComponents()
            assert rcns.size() == 2
            assert rcns[0].name == 'user' && rcns[0].type.name == 'java.nio.file.attribute.UserPrincipal'
            assert rcns[1].name == 'group' && rcns[1].type.name == 'java.nio.file.attribute.GroupPrincipal'
        '''
    }

    @Test
    void testManyRecordComponents() {
        assumeTrue(isAtLeastJdk('16.0'))

        assertScript shell, '''
            @CompileStatic
            record Record(String name, int x0, int x1, int x2, int x3, int x4,
                                    int x5, int x6, int x7, int x8, int x9, int x10, int x11, int x12, int x13, int x14,
                                    int x15, int x16, int x17, int x18, int x19, int x20) {
                public Record {
                    x1 = -x1
                }
            }

            def r = new Record('someRecord', 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)
            def expected = 'Record[name=someRecord, x0=0, x1=-1, x2=2, x3=3, x4=4, x5=5, x6=6, x7=7, x8=8, x9=9, x10=10, x11=11, x12=12, x13=13, x14=14, x15=15, x16=16, x17=17, x18=18, x19=19, x20=20]'
            assert r.toString() == expected
        '''

        assertScript shell, '''
            @CompileStatic
            @ToString(includeNames=true)
            record Record(String name, int x0, int x1, int x2, int x3, int x4,
                                    int x5, int x6, int x7, int x8, int x9, int x10, int x11, int x12, int x13, int x14,
                                    int x15, int x16, int x17, int x18, int x19, int x20) {
                public Record {
                    x1 = -x1
                }
            }

            def r = new Record('someRecord', 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)
            def expected = 'Record(name:someRecord, x0:0, x1:-1, x2:2, x3:3, x4:4, x5:5, x6:6, x7:7, x8:8, x9:9, x10:10, x11:11, x12:12, x13:13, x14:14, x15:15, x16:16, x17:17, x18:18, x19:19, x20:20)'
            assert r.toString() == expected
        '''
    }

    @Test
    void testShallowImmutability() {
        assertScript shell, '''
            record HasItems(List items) { }

            def itemRec = new HasItems(['a', 'b'])
            assert itemRec.items().size() == 2
            itemRec.items().clear()
            itemRec.items() << 'c'
            assert itemRec.items() == ['c']
            assert itemRec.toString() == 'HasItems[items=[c]]'
        '''
    }

    @Test
    void testCoerce() {
        assertScript shell, '''
            @CompileDynamic
            record PersonDynamic(String name, int age) {
            }
            @CompileStatic
            record PersonStatic(String name, int age) {
            }

            void testDynamic() {
                PersonDynamic p = ['Daniel', 37]
                assert p.name() == 'Daniel'
                assert p.age() == 37

                PersonDynamic p2 = [age: 37, name: 'Daniel']
                assert p2.name() == 'Daniel'
                assert p2.age() == 37

                PersonStatic p3 = ['Daniel', 37]
                assert p3.name() == 'Daniel'
                assert p3.age() == 37

                PersonStatic p4 = [age: 37, name: 'Daniel']
                assert p4.name() == 'Daniel'
                assert p4.age() == 37
            }
            testDynamic()

            @CompileStatic
            void testStatic() {
                PersonStatic p = ['Daniel', 37]
                assert p.name() == 'Daniel'
                assert p.age() == 37

                PersonStatic p2 = [age: 37, name: 'Daniel']
                assert p2.name() == 'Daniel'
                assert p2.age() == 37

                PersonDynamic p3 = ['Daniel', 37]
                assert p3.name() == 'Daniel'
                assert p3.age() == 37

                PersonDynamic p4 = [age: 37, name: 'Daniel']
                assert p4.name() == 'Daniel'
                assert p4.age() == 37
            }
            testStatic()
        '''
    }

    @Test
    void testClassSerialization() {
        // inspired by:
        // https://inside.java/2020/07/20/record-serialization/

        assertScript shell, '''
            @ToString(includeNames=true, includeFields=true)
            class RangeClass implements Serializable {
                private final int lo
                private final int hi
                RangeClass(int lo, int hi) {
                    this.lo = lo
                    this.hi = hi
                    if (lo > hi) throw new IllegalArgumentException("$lo should not be greater than $hi")
                }
                // backdoor to emulate hacking of datastream
                RangeClass(int[] pair) {
                    this.lo = pair[0]
                    this.hi = pair[1]
                }
            }

            var data = File.createTempFile("serial", ".data")
            var rc = [new RangeClass([5, 10] as int[]), new RangeClass([10, 5] as int[])]
            data.withObjectOutputStream { out -> rc.each{ out << it } }
            data.withObjectInputStream(getClass().classLoader) { in ->
                assert in.readObject().toString() == 'RangeClass(lo:5, hi:10)'
                assert in.readObject().toString() == 'RangeClass(lo:10, hi:5)'
            }
        '''
    }

    @Test
    void testNativeRecordSerialization() {
        assumeTrue(isAtLeastJdk('16.0'))

        assertScript shell, '''
            record RangeRecord(int lo, int hi) implements Serializable {
                public RangeRecord {
                    if (lo > hi) throw new IllegalArgumentException("$lo should not be greater than $hi")
                }
                // backdoor to emulate hacking of datastream
                RangeRecord(int[] pair) {
                    this.lo = pair[0]
                    this.hi = pair[1]
                }
            }

            var data = File.createTempFile("serial", ".data")
            var rr = [new RangeRecord([5, 10] as int[]), new RangeRecord([10, 5] as int[])]
            data.withObjectOutputStream { out -> rr.each{ out << it } }
            data.withObjectInputStream(getClass().classLoader) { in ->
                assert in.readObject().toString() == 'RangeRecord[lo=5, hi=10]'
                def ex = shouldFail(InvalidObjectException) { in.readObject() }
                assert ex.message == '10 should not be greater than 5'
            }
        '''
    }

    @Test
    void testCustomizedGetter() {
        assertScript shell, '''
            record Person(String name) {
                String name() {
                    return "name: $name"
                }
            }

            assert new Person('Daniel').name() == 'name: Daniel'
        '''
    }

    @Test
    void testTupleConstructor() {
        for (pair in [['RecordType', 'TupleConstructor'], ['defaults=false', 'defaultsMode=DefaultsMode.OFF']].combinations()) {
            assertScript shell, """
                @${pair[0]}(${pair[1]}, namedVariant=false)
                record Person(String name, Date dob) {
                    //Person(String,Date)
                    //Person(String)  no!
                    //Person(Map)     no!
                    //Person()        no!

                    public Person { // implies @TupleConstructor(pre={...})
                        assert name.length() > 1
                    }

                    Person(Person that) {
                        this(that.name(), that.dob())
                    }

                    //getAt(int i)
                    //toList()
                    //toMap()
                }

                assert Person.declaredConstructors.length == 2 // copy and tuple

                def person = new Person('Frank Grimes', new Date())
                def doppel = new Person(person)
                shouldFail {
                    new Person(name:'Frank Grimes', dob:null)
                }
                shouldFail {
                    new Person('Frank Grimes')
                }
                shouldFail {
                    new Person()
                }
            """
        }
    }

    @Test
    void testGenerics() {
        assertScript shell, '''
            @CompileStatic
            record Person<T extends CharSequence>(T name, int age) {
                Person {
                    if (name.length() == 0) throw new IllegalArgumentException("name can not be empty")
                    if (age < 0) throw new IllegalArgumentException("Invalid age: $age")
                }
            }

            @CompileStatic
            void test() {
                def p = new Person<String>('Daniel', 37)
                assert p.name().toLowerCase() == 'daniel'
                assert p.toString() == 'Person[name=Daniel, age=37]'

                def p2 = new Person<>('Daniel', 37)
                assert p2.name().toLowerCase() == 'daniel'
                assert p2.toString() == 'Person[name=Daniel, age=37]'

                def err = shouldFail(IllegalArgumentException) {
                    new Person<String>('', 1)
                }
                assert err.message == 'name can not be empty'

                err = shouldFail(IllegalArgumentException) {
                    new Person<String>('Unknown', -1)
                }
                assert err.message == 'Invalid age: -1'
            }

            test()
        '''
    }

    // GROOVY-10548
    @Test
    void testProperty() {
        assertScript shell, '''
            record Person(String name) {
            }
            @CompileStatic
            void test() {
                def person = new Person('Frank Grimes')
                assert person.name == 'Frank Grimes'
            }
            test()
        '''
    }

    // GROOVY-10679
    @Test
    void testAnnotationPropogation() {
        assertScript shell, '''
            import java.lang.annotation.*

            @Target([ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.PARAMETER])
            @Retention(RetentionPolicy.RUNTIME)
            public @interface MyAnno { }

            @MyAnno
            record Car(String make, @MyAnno String model) { }

            assert !Car.getAnnotation(MyAnno)
            assert Car.getMethod('model').getAnnotation(MyAnno)
            assert Car.getConstructor(String, String).getAnnotation(MyAnno)
            assert Car.getConstructor(String, String).getParameterAnnotations()[1][0].annotationType() == MyAnno
        '''
    }
}
